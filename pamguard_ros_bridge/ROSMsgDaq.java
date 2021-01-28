package pamguard_ros_bridge;

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
        System.out.println("Get system type.");
        return this.plugin_name;
    }
    
    // not sure for this, still need check the source code 
    @Override
    public String getSystemName(){
        System.out.println("Get system name.");
        return this.plugin_name;
    }
    
    // init the gui part of ROSMsgDaqPanel
    @Override
    public JComponent getDaqSpecificDialogComponent(AcquisitionDialog param_acquisition_dialog){
        System.out.println("Get Daq specific dialog component.");
        if(this.ros_msg_daq_panel == null)
            this.ros_msg_daq_panel = new ROSMsgDaqPanel(param_acquisition_dialog);
        return this.ros_msg_daq_panel;
    }

    // the method may not be written correctly, it is a good idea
    // not to use it. 
    @Override
    public void dialogSetParams(){
        System.out.println("Dialog set params.");
        return;
    }

    // the method may not be written correctly, it is a good idea
    // not to use it.
    @Override
    public boolean dialogGetParams(){
        System.out.println("Dialog get params.");
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
        System.out.println("Get max sample rate.");
        return 192000;
    }

    // return max channel 
    @Override
    public int getMaxChannels(){
        System.out.println("Get max channels.");
        return 2;
    }

    // return peak to peak voltage
    public double getPeak2PeakVoltage(int param_int){
        System.out.println("Get peak to peak voltage.");
        return 2;
    }

    // not finish yet 
    @Override
    public boolean prepareSystem(AcquisitionControl param_acquisition_control){
        System.out.println("Prepare system.");
        if(!this.params.m_status){
            this.acquisition_control.getDaqProcess().pamStop();
            System.out.println("preparing system failed: connection status is false");
            return false;
        }
        this.acquisition_control = param_acquisition_control;
        this.params.m_audioDataQueue_ch1 = this.acquisition_control.getDaqProcess().getNewDataQueue();
        return true;
    }

    // not finish yet 
    @Override
    public boolean startSystem(AcquisitionControl param_acquisition_control){
        System.out.println("Start system.");
        if(!this.params.m_status){
            System.out.println("Starting system failed: connection status is false");
            return false;
        }
        // start the stream thread, push data inside AudioDataQueue
        params.m_msgList.clear();
        Thread data_stream_thread = new Thread(new DataStreamThread(params.m_msgList));
        data_stream_thread.start();
        setStreamStatus(STREAM_RUNNING); // method in daqSystem
        TopToolBar.enableStartButton(false);
        TopToolBar.enableStopButton(true);
        return true;
    }

    // not finish yet 
    @Override
    public void stopSystem(AcquisitionControl param_acquisition_control){
        System.out.println("Stop system.");
        pam_stop = true;
        setStreamStatus(STREAM_CLOSED); //method in daqSystem
        // this.params.m_ws.close();
        TopToolBar.enableStartButton(true);
        TopToolBar.enableStopButton(false);
        return;
    }

    // is real time 
    @Override
    public boolean isRealTime(){
        System.out.println("Is it real time?");
        return true;
    }

    // can play back, not sure for now.
    @Override
    public boolean canPlayBack(float paramFloat){
        System.out.println("Can it play back?");
        return true;
    }

    // 
    @Override 
    public int getDataUnitSamples(){
        System.out.println("Get data unit samples.");
        return 1000;
    }

    public PlaybackSystem getPlaybackSystem(PlaybackControl playbackControl, DaqSystem daqSystem) {
    return null;
    }

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
        System.out.println("Get device name.");
        return this.plugin_name;
    }

    public double getChannelGain(int channel) {
    return 0.0D;
    }

    public int getSampleBits() {
    return 16;
    }

    public long getStallCheckSeconds() {
    return 2L;
    }

    public void dialogFXSetParams() {}

    class DataStreamThread implements Runnable{
        private BlockingQueue<double[]> msgList;
        public DataStreamThread(BlockingQueue<double[]> msgList){
            this.msgList = msgList;
        }
        public void run(){
            RawDataUnit newDataUnit = null;
            ROSMsgDaq.this.pam_stop = false;
            ROSMsgDaq.this.pam_running = true;
            double[] buffer = new double[9600];
            long total_samples = 0L;
            while(!ROSMsgDaq.this.pam_stop && ROSMsgDaq.this.params.m_status){
                if(this.msgList.size()!=0){   
                    try{
                        buffer = this.msgList.take();
                    }catch(InterruptedException e){
                        e.printStackTrace();
                        break;
                    }
                }
                long msec = ROSMsgDaq.acquisition_control.getAcquisitionProcess().absSamplesToMilliseconds(total_samples);
                newDataUnit = new RawDataUnit(msec, 1, total_samples, 9600L);
                newDataUnit.setRawData(buffer);
                ROSMsgDaq.this.params.m_audioDataQueue_ch1.addNewData(newDataUnit);
                total_samples += 9600L;
            }
            ROSMsgDaq.this.pam_running = false;
        }
    }

    // // pamsetting override start
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
        this.params.m_ws.close();
    }catch (Exception e) {
        e.printStackTrace();
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
    // //pamobserver override end

    // constructor of ROSMsgDaq
    public ROSMsgDaq(AcquisitionControl acquisition_control){
        System.out.println("ROSMsgDaq init!!!");
        ROSMsgDaq.acquisition_control = acquisition_control;
        this.params.m_audioDataQueue_ch1 = new AudioDataQueue();
        PamSettingManager.getInstance().registerSettings(this);
    }
} 