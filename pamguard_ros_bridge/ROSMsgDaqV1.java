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


public class ROSMsgDaq extends DaqSystem{
    public static String plugin_name = "ROS Msg DAQ plugin";

    static AcquisitionControl acquisition_control;
    private ROSMsgDaqPanel ros_msg_daq_panel;

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
        return false;
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
        return true;
    }

    // not finish yet 
    @Override
    public boolean startSystem(AcquisitionControl param_acquisition_control){
        System.out.println("Start system.");
        return true;
    }

    // not finish yet 
    @Override
    public void stopSystem(AcquisitionControl param_acquisition_control){
        System.out.println("Stop system.");
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

    // constructor of ROSMsgDaq
    public ROSMsgDaq(AcquisitionControl acquisition_control){
        System.out.println("ROSMsgDaq init!!!");
        ROSMsgDaq.acquisition_control = acquisition_control;
    }
} 