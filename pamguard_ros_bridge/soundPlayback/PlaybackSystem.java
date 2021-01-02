package soundPlayback;

import PamDetection.RawDataUnit;

/**
 * Interface for soundplayback systems. 
 * 
 * @author Doug Gillespie
 *
 */
public interface PlaybackSystem {

	public boolean prepareSystem(PlaybackControl playbackControl, int nChannels, float sampleRate);
	
	public boolean unPrepareSystem();
	
	public int getMaxChannels();
	
	public boolean playData(RawDataUnit[] data);
	
	public PlaybackDialogComponent getDialogComponent();
	
	public String getName();
}
