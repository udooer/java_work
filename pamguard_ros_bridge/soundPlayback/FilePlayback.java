package soundPlayback;

import java.util.ArrayList;

import nidaqdev.NIFilePlayback;
import Acquisition.DaqSystem;
import PamDetection.RawDataUnit;

/**
 * Playback of sound from wav files. 
 * Other real time sound sources must handle their own playback so that 
 * timing of in and out is synchronised correctly. 
 * @author Doug Gillespie
 * @see DaqSystem
 *
 */
public class FilePlayback implements PlaybackSystem {

	private PlaybackControl playbackControl;
			
	private FilePlaybackDialogComponent filePlaybackDialogComponent;
	
	protected ArrayList<FilePlaybackDevice> filePBDevices = new ArrayList<FilePlaybackDevice>();
	
	private FilePlaybackDevice currentDevice;
	
	public FilePlayback(PlaybackControl playbackControl) {
		this.playbackControl = playbackControl;
		filePBDevices.add(currentDevice = new SoundCardFilePlayback(this));
		filePBDevices.add(new NIFilePlayback(this));
		filePlaybackDialogComponent = new FilePlaybackDialogComponent(this);
	}

	public int getMaxChannels() {
		return 2;
	}
	
	public int getDeviceNumber() {
		return playbackControl.playbackParameters.deviceNumber;
	}

	synchronized public boolean prepareSystem(PlaybackControl playbackControl,
			int nChannels, float sampleRate) {

		unPrepareSystem();
		currentDevice = filePBDevices.get(playbackControl.playbackParameters.deviceType);
		return currentDevice.preparePlayback(playbackControl.playbackParameters);
		
//		if (nChannels <= 0 || nChannels > getMaxChannels()) return false;
//		
//		audioFormat = new AudioFormat(sampleRate, 16, nChannels, true, true);
//
//		ArrayList<Mixer.Info> mixerinfos = SoundCardSystem.getOutputMixerList();
//		Mixer.Info thisMixerInfo = mixerinfos.get(getDeviceNumber());
//		Mixer thisMixer = AudioSystem.getMixer(thisMixerInfo);
//		if (thisMixer.getSourceLineInfo().length <= 0){
//			thisMixer.getLineInfo();
//			return false;
//		}
//		
//		try {
//			// try to get the device of choice ...
//			sourceDataLine = (SourceDataLine) thisMixer.getLine(thisMixer.getSourceLineInfo()[0]);
//			sourceDataLine.open(audioFormat);
//			sourceDataLine.start();
//		} catch (Exception Ex) {
//			Ex.printStackTrace();
//			sourceDataLine = null;
//			return false;
//		}
//		
//		return true;
	}

	synchronized public boolean unPrepareSystem() {
		if (currentDevice != null) {
			return currentDevice.stopPlayback();
		}
		return false;
	}

	public boolean playData(RawDataUnit[] data) {
		
		if (currentDevice == null) {
			return false;
		}
		return currentDevice.playData(data);

	}

	public PlaybackDialogComponent getDialogComponent() {
		return filePlaybackDialogComponent;
	}

	@Override
	public String getName() {
		return "Soundcard Playback";
	}

	public PlaybackControl getPlaybackControl() {
		return playbackControl;
	}


}
