package Acquisition;

import java.io.Serializable;

import Array.Preamplifier;
import PamguardMVC.PamConstants;

public class AcquisitionParameters implements Serializable, Cloneable {

	static final long serialVersionUID = 2;
	
	String daqSystemType = "Sound Card";
	
	public float sampleRate = 48000;
	
	public int nChannels = 2;
	
	public double voltsPeak2Peak = 5;
	
	/**
	 * List of channels data are acquired from (not necessarily 0,1,2, etc.) 
	 * With NI boards, this has become a pain since if multiple boards are
	 * used, there may be repeats within this list.  
	 */
	private int channelList[];// = new int[PamConstants.MAX_CHANNELS]; //Xiao Yan Deng
	
	public Preamplifier preamplifier = new Preamplifier(10, new double[] {10, 20000});

	/**
	 * Hydrophone list is a short list of length equal to the number of channels, so if
	 * your channel list does not start at zero, you have to first use the 
	 * channelListIndexes before using this lookup table. 
	 */
	private int[] hydrophoneList;
	
	/**
	 * list of indexes for each hardware channel in channelList (i.e. opposite LUT)
	 */
	transient int[] channelListIndexes; 

	public AcquisitionParameters() {
		getHardwareChannelList(); // automatically create a channellist.
	}
	@Override
	protected AcquisitionParameters clone() {
		try {
			AcquisitionParameters ap = (AcquisitionParameters) super.clone();
			if (ap.preamplifier != null) {
				ap.preamplifier = ap.preamplifier.clone();
			}
			return ap;
		}
		catch (CloneNotSupportedException Ex) {
			Ex.printStackTrace();
		}
		return null;
	}

	public String getDaqSystemType() {
		return daqSystemType;
	}

	public void setDaqSystemType(String daqSystemType) {
		this.daqSystemType = daqSystemType;
	}

	/**
	 * Gets a list of hydrophones from channel Indexes (not channel numbers)
	 * @return list of hydrophones. 
	 */
	public int[] getHydrophoneList() {
		if ((hydrophoneList == null || hydrophoneList.length < nChannels) && nChannels > 0) {
			hydrophoneList = new int[nChannels];
			for (int i = 0; i < nChannels; i++) {
				hydrophoneList[i] = i;
			}
		}
		return hydrophoneList;
	}

	/**
	 * Set a hydrophone list. 
	 * @param hydrophoneList
	 */
	public void setHydrophoneList(int[] hydrophoneList) {
		this.hydrophoneList = hydrophoneList;
	}

	/** 
	 * Gets a hydrophone number from a channel number (not channel index) 
	 * @param channel software channel number
	 * @return a specific hydrophone number from the selected array
	 */
	public int getHydrophone(int channel) {
		// first convert the software channel to a channel index (i.e.
		// if we're reading ch 4 and 5, convert to 0 or 1.
		if (hydrophoneList == null) {
			hydrophoneList = getHydrophoneList();
		}
		if (channelListIndexes == null) {
			sortChannelListIndexes();
		}
		if (channelListIndexes.length <= channel) return -1;
		channel = this.channelListIndexes[channel];
		if (channel < 0) {
			return -1;
		}
		if (hydrophoneList.length <= channel) return -1;
		return hydrophoneList[channel];
	}
	
	public int getNChannels() {
		return nChannels;
	}

	public void setNChannels(int channels) {
		nChannels = channels;
	}
	
	public int[] getNChannelList(){
		return channelList;
	}

	public Preamplifier getPreamplifier() {
		return preamplifier;
	}

	public void setPreamplifier(Preamplifier preamplifier) {
		this.preamplifier = preamplifier;
	}

	public float getSampleRate() {
		return sampleRate;
	}

	public void setSampleRate(float sampleRate) {
		this.sampleRate = sampleRate;
	}

	public double getVoltsPeak2Peak() {
		return voltsPeak2Peak;
	}

	public void setVoltsPeak2Peak(double voltsPeak2Peak) {
		this.voltsPeak2Peak = voltsPeak2Peak;
	}

	/**
	 * Gets / creates a list of hardware channels used. <p>
	 * i.e. converts from channel indexes to channel numbers. 
	 * @return List of channel numbers
	 */
	public int[] getHardwareChannelList() {
		if (channelList == null || ((channelList.length < nChannels) && nChannels > 0)) {
			int[] newChannelList = new int[PamConstants.MAX_CHANNELS];
			for (int i = 0; i < PamConstants.MAX_CHANNELS; i++) {
				newChannelList[i] = i;
			}
			// use old list numbers if they exist.
			if (channelList != null) {
				for (int i = 0; i < Math.min(nChannels, channelList.length); i++) {
					newChannelList[i] = channelList[i];
				}
			}
			// now replace the old with the new.
			channelList = newChannelList;
		}
		return channelList;
	}

	public void setChannelList(int[] channelList) {
		this.channelList = channelList;
		sortChannelListIndexes();
	}
	
	public void setChannelList(int index, int channelNumber) {
		getHardwareChannelList();
		if (index >= channelList.length) {
			return;
		}
		channelList[index] = channelNumber;
		sortChannelListIndexes();
	}
	
	/**
	 * Creates a default channel list 0,1,2,3,4 etc.
	 */
	public void setDefaultChannelList() {
		channelList = new int[PamConstants.MAX_CHANNELS];
		for (int i = 0; i < PamConstants.MAX_CHANNELS; i++) {
			channelList[i] = i;
		}
		sortChannelListIndexes();
	}
	
	/**
	 * Gets the channel number for a particular channel index. 
	 * @param index channel index
	 * @return channel number
	 */
	public int getChannelList(int index) {
		return getHardwareChannelList()[index];
	}
	
	/**
	 * Gets the complete list of channel indexes. 
	 * @return list of channel indexes. 
	 */
	public int[] getChannelListIndexes() {
		if (channelListIndexes == null) {
			sortChannelListIndexes();
		}
		return channelListIndexes;
	}
	
	/**
	 * Sets the channel list indeces
	 * @param channelListIndexes
	 */
	public void setChannelListIndexes(int[] channelListIndexes) {
		this.channelListIndexes = channelListIndexes;
	}
	
	/**
	 * Gets the channel index for a particular hardware channel 
	 * @param channel
	 * @return channel index or -1 if it doesn't exist. 
	 */
	public int getChannelListIndexes(int channel) {
		if (channelListIndexes == null) {
			sortChannelListIndexes();
		}
		if (channel >= 0 && channel < channelListIndexes.length) {
			return channelListIndexes[channel];
		}
		else return -1;
	}
	
	/**
	 * Creates a set of easily accessible channel indexes
	 * which can be used to convert from channel numbers to 
	 * channel index e.g. used channel numbers might be 3 and 4
	 * so the listIndexes will be {-1 -1 -1 0 1]
	 */
	private void sortChannelListIndexes() {
		int max = 0;
		getHardwareChannelList();
		for (int i = 0; i < nChannels; i++) {
			max = Math.max(max, channelList[i]);
		}
		channelListIndexes = new int[max+1];
		for (int i = 0; i < channelListIndexes.length; i++) {
			channelListIndexes[i] = -1;
		}
		for (int i = 0; i < nChannels; i++) {
			channelListIndexes[channelList[i]] = i;
		}
	}
//	public void setChannelListIndexes(int[] channelListIndexes) {
//		this.channelListIndexes = channelListIndexes;
//	}
}
