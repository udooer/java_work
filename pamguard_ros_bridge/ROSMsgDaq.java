package pamguard_ros_bridge;
/*
 *  Shane 
 *  2021_02_03
 */
import Acquisition.AcquisitionControl;
import Acquisition.AcquisitionDialog;
import Acquisition.AudioDataQueue;
import Acquisition.DaqSystem;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamDetection.RawDataUnit;
import PamView.TopToolBar;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserver;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import soundPlayback.PlaybackControl;
import soundPlayback.PlaybackSystem;


public class ROSMsgDaq extends DaqSystem implements PamSettings, PamObserver{
    public static String plugin_name = "ROS Msg DAQ plugin";

    static AcquisitionControl acquisition_control;
    private ROSMsgDaqPanel ros_msg_daq_panel;
    private ROSMsgParams params = new ROSMsgParams();

    private volatile boolean pam_stop;
    private volatile boolean pam_running;

    // currently return the name of the plugin
    @Override 
    public String getSystemType(){
        return this.plugin_name;
    }
    
    // not sure for this, still need check the source code 
    @Override
    public String getSystemName(){
        return this.plugin_name;
    }
    
    // init the gui part of ROSMsgDaqPanel
    @Override
    public JComponent getDaqSpecificDialogComponent(AcquisitionDialog param_acquisition_dialog){
        if(this.ros_msg_daq_panel == null)
            this.ros_msg_daq_panel = new ROSMsgDaqPanel(param_acquisition_dialog, this.params);
        return this.ros_msg_daq_panel;
    }

    // the method may not be written correctly, it is a good idea
    // not to use it. 
    @Override
    public void dialogSetParams(){
        return;
    }

    // the method may not be written correctly, it is a good idea
    // not to use it.
    @Override
    public boolean dialogGetParams(){
        if(this.ros_msg_daq_panel == null)
            return false;
        else
            return true;
    }

    public void setSelected(boolean select) {}

    public void notifyModelChanged(int changeType) {}

    // return max sample rate 
    @Override
    public int getMaxSampleRate(){
        return 192000;
    }

    // return max channel 
    @Override
    public int getMaxChannels(){
        return 2;
    }

    // return peak to peak voltage
    public double getPeak2PeakVoltage(int param_int){
        return 2;
    }

    // not finish yet 
    @Override
    public boolean prepareSystem(AcquisitionControl param_acquisition_control){
        System.out.println("DaqSystem:Prepare system.");        
        if(!this.params.m_status){
            this.acquisition_control.getDaqProcess().pamStop();
            System.out.println("preparing system failed: connection status is false");
            return false;
        }
        this.acquisition_control = param_acquisition_control;
        this.params.m_audioDataQueue = this.acquisition_control.getDaqProcess().getNewDataQueue();
        return true;
    }

    // not finish yet 
    @Override
    public boolean startSystem(AcquisitionControl param_acquisition_control){
        System.out.println("DaqSystem:Start system.");
        System.out.println("m_status:" + this.params.m_status);
        System.out.println("pam_stop:" + this.pam_stop);
        System.out.println("pam_running:" + this.pam_running);
        if(!this.params.m_status){
            System.out.println("Starting system failed: connection status is false");
            return false;
        }
        // start the stream thread, push data inside AudioDataQueue
        Thread data_stream_thread = new Thread(new DataStreamThread());
        data_stream_thread.start();
        setStreamStatus(STREAM_RUNNING); // method in daqSystem
        TopToolBar.enableStartButton(false);
        TopToolBar.enableStopButton(true);
        return true;
    }

    // not finish yet 
    @Override
    public void stopSystem(AcquisitionControl param_acquisition_control){
        System.out.println("DaqSystem:Stop system.");
        this.pam_stop = true;
        setStreamStatus(STREAM_CLOSED); //method in daqSystem
        TopToolBar.enableStartButton(true);
        TopToolBar.enableStopButton(false);
        return;
    }

    // is real time 
    @Override
    public boolean isRealTime(){
        return true;
    }

    // can't play back, not sure for now.
    @Override
    public boolean canPlayBack(float paramFloat){
        return false;
    }

    // 
    @Override 
    public int getDataUnitSamples(){
        return 1000;
    }

    // public PlaybackSystem getPlaybackSystem(PlaybackControl playbackControl, DaqSystem daqSystem) {
    //     System.out.println("DaqSystem:get play back sysytem");
    //     return null;
    // }

    //
    @Override
    public void daqHasEnded(){
        System.out.println("Daq has ended.");
    }

    public boolean areSampleSettingsOk(int numInputsRequested, float sampleRateRequested) {
        return true;
    }

    public void showSampleSettingsDialog(AcquisitionDialog acquisitionDialog) {}

    public boolean supportsChannelLists() {
        return false;
    }

    // return the plugin name
    @Override
    public String getDeviceName(){
        return this.plugin_name;
    }

    public double getChannelGain(int channel){
        return 0.0D;
    }

    public int getSampleBits() {
        return 24;
    }

    public long getStallCheckSeconds() {
        return 2L;
    }

    public void dialogFXSetParams() {}

    class DataStreamThread implements Runnable{
        DataStreamThread(){}
        public void run(){
            System.out.println("THREAD START");
            ROSMsgDaq.this.params.m_msgList_ch1.clear();
            ROSMsgDaq.this.params.m_msgList_ch2.clear();
            ROSMsgDaq.this.pam_stop = false;
            ROSMsgDaq.this.pam_running = true;

            RawDataUnit newDataUnit[] = new RawDataUnit[2];
            double[][] buffer = new double[2][9600];
            long total_samples = 0L;

            while(!ROSMsgDaq.this.pam_stop && ROSMsgDaq.this.params.m_status){   
                try{
                    buffer[0] = ROSMsgDaq.this.params.m_msgList_ch1.take();
                    buffer[1] = ROSMsgDaq.this.params.m_msgList_ch2.take();
                }catch(InterruptedException e){
                    System.out.println("Exception Happen!!");
                    break;
                }
                long msec = ROSMsgDaq.acquisition_control.getAcquisitionProcess().absSamplesToMilliseconds(total_samples);
                for(int i=0;i<2;i++){
                    newDataUnit[i] = new RawDataUnit(msec, i+1, total_samples, 9600L);
                    newDataUnit[i].setRawData(buffer[i]);
                    ROSMsgDaq.this.params.m_audioDataQueue.addNewData(newDataUnit[i]);
                }
                
                total_samples += 9600L;

                System.out.println("TOTAL TIME:" + (float)total_samples/96000f);
            }
            ROSMsgDaq.this.pam_running = false;
            System.out.println("THREAD END");
        }
    }

    // pamsetting override start
    public String getUnitName() {
        return this.acquisition_control.getUnitName();
    }
  
    public String getUnitType() {
        return "ROS msg DAQ plugin_Params";
    }
  
    public Serializable getSettingsReference() {
        return null;
    }
  
    public long getSettingsVersion() {
        return 0L;
    }
  
    public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
    try {
        System.out.println("restore settings");
    }catch (Exception e) {
        System.out.println("exception happen");
    } 
        return false;
    }
    // pamsetting override end

    // pamobserver override start
    public long getRequiredDataHistory(PamObservable o, Object arg) {
        return 0L;
    }
  
    public void update(PamObservable o, PamDataUnit arg) {}
  
    public void removeObservable(PamObservable o) {
        System.out.println("QQQQQQQQQQQQQQQQQQQQQQQ");
    }
  
    public void setSampleRate(float sampleRate, boolean notify) {}
  
    public void noteNewSettings() {
        System.out.println("ccccccc");
    }
  
    public String getObserverName() {
        return null;
    }
  
    public void masterClockUpdate(long milliSeconds, long sampleNumber) {}
  
    public PamObserver getObserverObject() {
        return null;
    }
    // pamobserver override end

    // constructor of ROSMsgDaq
    public ROSMsgDaq(AcquisitionControl acquisition_control){
        System.out.println("ROSMsgDaq init!!!");
        this.acquisition_control = acquisition_control;
        this.params.m_audioDataQueue = new AudioDataQueue();
        PamSettingManager.getInstance().registerSettings(this);
    }
}