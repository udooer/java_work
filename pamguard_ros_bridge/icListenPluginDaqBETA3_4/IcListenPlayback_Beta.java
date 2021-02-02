package icListenPluginDaqBETA3_4;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.lang.ClassLoader;

import soundPlayback.FilePlayback;
import soundPlayback.FilePlaybackDevice;
import soundPlayback.swing.FilePlaybackDialogComponent;
import soundPlayback.PlaybackControl;
import soundPlayback.swing.PlaybackDialogComponent;
import soundPlayback.SoundCardFilePlayback;
import Acquisition.DaqSystem;
import Filters.FIRFilter;
import Filters.FIRFilterMethod;
import Filters.FilterBand;
import Filters.FilterParams;
import PamDetection.RawDataUnit;

/**
 * Playback of sound from icListen Hydrophones. 
 * Essentially the FilePlayback class with icListen included
 * And buffer sizes manipulated.
 * Bandwidth higher than 25.6 kHz caused feedback from the buffer 
 * being fed too fast and lower than 800 Hz caused long lag in playback.
 * @author Aishat Mohammed
 * @see SoundPlayback.FilePlayback
 *
 */
class IcListenPlayback_Beta extends FilePlayback {

	/**
	 * Playback of sound from wav files. 
	 * Other real time sound sources must handle their own playback so that 
	 * timing of in and out is synchronised correctly. 
	 * @author Doug Gillespie
	 * @see DaqSystem
	 *
	 */

		private PlaybackControl playbackControl;
				
		private FilePlaybackDialogComponent filePlaybackDialogComponent;
		
		protected ArrayList<FilePlaybackDevice> filePBDevices = new ArrayList<FilePlaybackDevice>();
		
		private FilePlaybackDevice currentDevice;

		private boolean realTimePlayback;
		
		List<RawDataUnit[]> realTimeQueue;

		private double maxQueueLength;

		private RealTimeQueueReader realTimeQueueReader;

		private IcListenSideBar playbackSideBar;
		
		protected final int MAX_SOUND_CARD_RATE = 32000;
		
		protected boolean decimateBool = false;
		
		FIRFilter decimator[] = null;
		
		private int sampVal = 0;
		
		private int nChan = 0;
		
		public IcListenPlayback_Beta(PlaybackControl playbackControl) {
			super(playbackControl);
			this.playbackControl = playbackControl;
			filePBDevices.add(currentDevice = new SoundCardFilePlayback(this));
			//Don't need these but can add back if implemented ATM 28012020
			//filePBDevices.add(new NIFilePlayback(this));
			//filePBDevices.add(new ASIOFilePlaybackSystem(this));
			filePlaybackDialogComponent = new FilePlaybackDialogComponent(this);
		}

		@Override
		public int getMaxChannels() {
			FilePlaybackDevice device = filePBDevices.get(playbackControl.getPlaybackParameters().deviceType);
			return device.getNumPlaybackChannels(getDeviceNumber());
		}
		
		@Override
		public int getDeviceNumber() {
			return playbackControl.getPlaybackParameters().deviceNumber;
		}

		@Override
		synchronized public boolean prepareSystem(PlaybackControl playbackControl,
				int nChannels, float sampleRate) {

			unPrepareSystem();
			
			realTimePlayback = playbackControl.isRealTimePlayback();
			if (realTimePlayback) {
				realTimeQueue = Collections.synchronizedList(new LinkedList<RawDataUnit[]>());
				
				//Decimate if sample rate too large
				if (sampleRate > MAX_SOUND_CARD_RATE )
				{
					decimator = new FIRFilter[nChannels];
					//set to use lowpass filter and change playback rate
					decimateBool = true;
					FilterParams filterParams = new FilterParams();
					filterParams.lowPassFreq = (float)(0.45 * MAX_SOUND_CARD_RATE);
					filterParams.highPassFreq = 0;
					filterParams.filterBand = FilterBand.LOWPASS;

					FIRFilterMethod firFilterMethod = new FIRFilterMethod(sampleRate, filterParams);
					for (int i = 0; i < nChannels; i++)
					{
						decimator[i] = new FIRFilter(firFilterMethod, i, MAX_SOUND_CARD_RATE); //change channels
					}
					
					//change 
					playbackControl.getPlaybackParameters().setPlaybackRate(MAX_SOUND_CARD_RATE);
					
					double tempSampVal = (int) ((int) sampleRate/playbackControl.getPlaybackParameters().getPlaybackRate());
					if (tempSampVal < 2)
						sampVal = 2;
					else
						sampVal = (int)Math.ceil((tempSampVal * (10/100)) + tempSampVal);
					
					System.err.println("Soundcard can only safely output up to " 
					+ MAX_SOUND_CARD_RATE + "Hz\nWill output at " 
							+ MAX_SOUND_CARD_RATE + "Hz\n"+"IcListenPlayback.java in IcListenPlugin");
				}
				
				if (sampleRate <= 4000)
					maxQueueLength = 10 * nChannels; // hold about a second of data. 
				else if (sampleRate > 4000 && sampleRate < 32000)
					maxQueueLength = 30 * nChannels;
				else
					maxQueueLength = 50 * nChannels;
				
				realTimeQueueReader = new RealTimeQueueReader();
				
				nChan = nChannels;
				Thread t = new Thread(realTimeQueueReader);
				t.start();
			}
			
			currentDevice = filePBDevices.get(playbackControl.getPlaybackParameters().deviceType);
			return currentDevice.preparePlayback(playbackControl.getPlaybackParameters());
			

		}

		@Override
		synchronized public boolean unPrepareSystem() {
			if (currentDevice != null) {
				return currentDevice.stopPlayback();
			}
			return false;
		}

		@Override
		public boolean playData(RawDataUnit[] data, double gain) {
			
			if (currentDevice == null) {
				return false;
			}
			if (realTimePlayback) {
				RawDataUnit[] output = null;
				if (decimateBool) //decimate data before putting it in the queue
				{
					output = decimator(data);
				}
				else
					output = data;
				
				if (realTimeQueue.size() < maxQueueLength) {					
					if (output != null)
						realTimeQueue.add(output.clone());
					else
						System.err.println("Output signal was null\nLine 177 IcListenPlayback.java in IcListenPlugin");
				}
				else {
					System.out.println("Dumping playback data since output running too slow for input.\n Buffer size: " + realTimeQueue.size() + "\n");
					realTimeQueue.clear();
				}
				return true;
			}
			else {
				playbackControl.getPlaybackParameters().playbackGain = (int)gain;
				return currentDevice.playData(data.clone());
			}

		}
		
		class RealTimeQueueReader implements Runnable {

			private boolean keepRunning = true;
			
			@Override
			public void run() {
				RawDataUnit[] data;
				while (keepRunning) {
					try {
						while (realTimeQueue.size() > 0) {
							data = realTimeQueue.remove(0);
							currentDevice.playData(data);
						}
					}
					catch (IndexOutOfBoundsException e) {
						// can happen if all data were deleted at just that moment due to queue getting 
						// too large. 
						//System.err.print("Data lost, IcLiPlayback.java in icListenPluginDaq3_4\n");;
					}
					try {
						Thread.sleep(50);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			
			public void stopThread() {
				keepRunning = false;
			}
			
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
		
		public RawDataUnit[] decimator(RawDataUnit[] input)
		{
			double[] rawData = null;
			
			for(int i = 0; i < input.length; i++)
			{
				int size = input[i].getRawData().length;
				int reSize = size/(int)sampVal;
				rawData = new double[size];
				for (int chan = 0; chan < nChan; chan++)
					decimator[chan].runFilter(input[i].getRawData(), rawData);
				input[i].setRawData(resample(rawData, reSize));
			}
			
			return input;
		}
		
		private double[] resample(double[] rawData, int reSize)
		{
			double[] usedData = new double[reSize];
			
			for (int resampleIt = 0,j = 0; resampleIt < reSize; resampleIt++, j += sampVal)
				usedData[resampleIt] = rawData[j];
			
			return usedData;
		}

}
