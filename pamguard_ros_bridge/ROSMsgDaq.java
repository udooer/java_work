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

    // currently return the name of the plugin
    @Override 
    public String getSystemType(){
        return plugin_name;
    }
    
    // not sure for this, still need check the source code 
    @Override
    public String getSystemName(){
        return plugin_name;
    }
    
    public abstract JComponent getDaqSpecificDialogComponent(AcquisitionDialog paramAcquisitionDialog);

    public ChannelListPanel getDaqSpecificChannelListPanel(AcquisitionDialog acquisitionDialog) {
    return null;
    }

    public abstract void dialogSetParams();

    public abstract boolean dialogGetParams();

    public void setSelected(boolean select) {}

    public void notifyModelChanged(int changeType) {}

    public abstract int getMaxSampleRate();

    public abstract int getMaxChannels();

    public abstract double getPeak2PeakVoltage(int paramInt);

    public abstract boolean prepareSystem(AcquisitionControl paramAcquisitionControl);

    public abstract boolean startSystem(AcquisitionControl paramAcquisitionControl);

    public abstract void stopSystem(AcquisitionControl paramAcquisitionControl);

    public abstract boolean isRealTime();

    public abstract boolean canPlayBack(float paramFloat);

    public abstract int getDataUnitSamples();

    public PlaybackSystem getPlaybackSystem(PlaybackControl playbackControl, DaqSystem daqSystem) {
    return null;
    }

    public abstract void daqHasEnded();

    public int getStreamStatus() {
    return this.streamStatus;
    }

    public void setStreamStatus(int streamStatus) {
    this.streamStatus = streamStatus;
    }

    public boolean areSampleSettingsOk(int numInputsRequested, float sampleRateRequested) {
    return true;
    }

    public void showSampleSettingsDialog(AcquisitionDialog acquisitionDialog) {}

    public Component getStatusBarComponent() {
    return null;
    }

    public boolean supportsChannelLists() {
    return false;
    }

    public int getInputChannelMap(AcquisitionParameters acquisitionParameters) {
    return PamUtils.makeChannelMap(acquisitionParameters.nChannels);
    }

    public abstract String getDeviceName();

    public double getChannelGain(int channel) {
    return 0.0D;
    }

    public int getSampleBits() {
    return 16;
    }

    public long getStallCheckSeconds() {
    return 2L;
    }

    public ChannelListPanel getDaqSpecificChannelListNode(AcquisitionPaneFX acquisitionPaneFX) {
    return null;
    }

    public Node getDaqSpecificNode(AcquisitionPaneFX acquisitionPaneFX) {
    return null;
    }

    public void dialogFXSetParams() {}
} 