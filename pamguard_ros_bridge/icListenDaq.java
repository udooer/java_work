package icListenPluginDaqBETA3_4;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.JComponent;
import javax.swing.JOptionPane;

import Acquisition.AcquisitionControl;
import Acquisition.AcquisitionDialog;
import Acquisition.AudioDataQueue;
import Acquisition.DaqSystem;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamControllerInterface;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamDetection.RawDataUnit;
import PamView.TopToolBar;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserver;
import soundPlayback.PlaybackControl;
import soundPlayback.PlaybackSystem;

import PamController.PamguardVersionInfo;
import PamController.PamguardVersionInfo.ReleaseType;


public class icListenDaq extends DaqSystem implements PamSettings, PamObserver {
	private static ConcurrentHashMap<String, icListenDaq> icDaqs = new ConcurrentHashMap<String, icListenDaq>();
	public static final String pluginName = "icListen DAQ  plugin";

	private final int MIN_MESSAGE_LEN = 4;
	static AcquisitionControl acquisitionControl;

	private IcListenParams icListenParams = new IcListenParams();

	private IcListenDaqPanel icListenDaqPanel;

	private volatile boolean stopCapture;
	private volatile boolean captureRunning;
	/*
	 * The four constants below are the message types that will be used by the basic
	 * message structure. For more information, please see icListen streaming
	 * telemetry 2.2 for more information.
	 */
	private final char TYPE_DATA = 0x31;
	private final char TYPE_HEADER = 0x32;
	private final char TYPE_START = 0x33;
	private final char TYPE_STOP = 0x34;

	/*
	 * The constants below will be used to build the Event Header and Data messages.
	 * For more information about chunk types, see icListen Telemetry Section 4.
	 */

	private final char EVENT_KEY = 0x41;
	private final char DATA_CHUNK = 0x42;
	private final char STATUS_CHUNK = 0x43;
	private final char DEVICE_CHUNK = 0x44;
	private final char WAVE_SETUP_CHUNK = 0x45;
	private final char SCALING_CHUNK = 0x47;
	private final char TIME_SYNC_CHUNK = 0x50;

	private final int CHUNK_SIZE_OFFSET = 2;/* see section 4 */
	private final int CHUNK_PAYLOAD_OFFSET = 4;/* see section 4 */
	private final int EVENT_SEQUENCE_NUMBER_OFFSET = 4;/* see section 3.4? */
	private final int SCALING_MAX_AMPLITUDE_OFFSET = 16;/* see section 4.7 for details. */
	private final int DATA_SAMPLE_NUMBER_OFFSET = 4; /* see section 4.2 for details, sample # fields */
	private final int DATA_SAMPLE_OFFSET = 10;/* see section 4.2 for details,# of samples */
	private final int DATA_DATA_OFFSET = 12/* see section 4.2 for detail */;
	
	protected static PlaybackControl playbackControl = null;

	// indicates the start of the stream and stop of stream.
	private StreamMessage START_STREAM_MESSAGE = new StreamMessage(TYPE_START, new char[] { 0, 4 },
			new char[] { 0, 0, 0, 0 });
	private StreamMessage STOP_STREAM_MESSAGE = new StreamMessage(TYPE_STOP, new char[] { 0, 0 }, new char[] {});

	// static AudioDataQueue newDataQueue;
	public static final double ICLISTEN_PEAKTOPEARKV = 6.00;

	public icListenDaq(AcquisitionControl acquisitionControl) {
		//might be a great idea to leave the commented code
		//what have been tried, what is the effect.
		icListenDaq.acquisitionControl = acquisitionControl;
		icListenParams.audioDataQueue = new AudioDataQueue();
		PamSettingManager.getInstance().registerSettings(this);


		/*
		 * ArrayList<PamSettings> pamSettings =
		 * PamSettingManager.getInstance().getOwners(); for(int i = 0 ; i <
		 * pamSettings.size();i++) { String s = pamSettings.get(i).getUnitName();
		 * System.out.println("Inside the constructor, the pamSetting Names are"+s); }
		 */
		// icListenParams.audioDataQueue.
		// acquisitionControl.
		// acquisitionControl.
		
		/**
		 * the remaining code will give an idea on how many instances of icListenDaq has been initialized
		 */
		for (int i = 0; i < PamController.getInstance().getDataBlocks().size(); i++) {
			String s = PamController.getInstance().getDataBlocks().get(i).getDataName();
			System.out.println(s);
		}

		System.out.println("icListen DAQ instances!" + icDaqs.size());
		// for(int i = 0 ; i < icDaqs.size();i++) {
		// if(acquisitionControl.getAcquisitionProcess().getObserverName()==null);
		// else
		// System.out.println(acquisitionControl.getAcquisitionProcess().getObserverName());
		// }
		// acquisitionControl.getParentProcess().
		// this.notify();
		// this.notifyModelChanged(0);

	}

	//currently returns the name of the plugin, but should have returned which type of icListen Device is .
	//but it doesn't matter as it is handled elsewhere.
	@Override
	public String getSystemType() {
		return pluginName;
	}

	//should have returned the system name, but currently returns the connection status of the panel: either not
	//coneected to an hydrophone deive, or connected to a hydrophone device with a unique serial device.
	@Override
	public String getSystemName() {
		return icListenParams.connectionStatus;
	}

	//init the gui part of the icListen.
	@Override
	public JComponent getDaqSpecificDialogComponent(AcquisitionDialog acquisitionDialog) {
		System.out.println("DaqSystem:getDaqSpecificDialogComponent");
		if (icListenDaqPanel == null)
			icListenDaqPanel = new IcListenDaqPanel(acquisitionDialog, this, icListenParams);

		return icListenDaqPanel;
	}

	//if the icListenDaqPanel is not initialized yet, returns immediately without any parameter,
	//else set all the required elements
	//note:the method may not be written correctly, it is a good idea not to use it.
	@Override
	public void dialogSetParams() {
		System.out.println("DaqSystem:dialogSetParams");
		if (icListenDaqPanel == null) {
			return;
		}
		icListenDaqPanel.setParams(icListenParams);
	}

	//Similar to dialogGetParams method.
	@Override
	public boolean dialogGetParams() {
		System.out.println("DaqSystem:dialogGetParams");
		if (icListenDaqPanel == null) {
			return false;
		}
		return icListenDaqPanel.getParams(icListenParams);
	}

	//The Max sample rate is -2, might not be correct
	@Override
	public int getMaxSampleRate() {
		System.out.println("DaqSystem:getMaxSampleRate");
		return DaqSystem.PARAMETER_FIXED;
	}

	//return value is -2, may not be correct.
	@Override
	public int getMaxChannels() {
		System.out.println("DaqSystem:getMaxChannels");
		// acquisitionControl.set
		return DaqSystem.PARAMETER_FIXED;
	}

	//return value is -2, might not be correct.
	@Override
	public double getPeak2PeakVoltage(int swChannel) {
		System.out.println("DaqSystem:getPeak2PeakVoltage");
		return DaqSystem.PARAMETER_FIXED;
	}

	//acquistionControl.getDaqProcess().pamStop() forces the program to regain the control of the stop button 
	//initialize important components.
	@Override
	public boolean prepareSystem(AcquisitionControl daqControl) {
		System.out.println("DaqSystem:prepareSystem");
		if (icListenParams.connect == false) {
			JOptionPane.showMessageDialog(null, "You are not connected to a icListen Device!");
			// Button btn = acquisitionControl.getGuiFrame().getParent().get
			acquisitionControl.getDaqProcess().pamStop();
			// TopToolBar.enableStartButton(true);
			System.out.println("xintaibaozha.");
			// TopToolBar.enableStopButton(false);
			return false;
		}
		// in order to recieve stream data, prepare System must return true.
		this.acquisitionControl = daqControl;
		icListenParams.audioDataQueue = acquisitionControl.getDaqProcess().getNewDataQueue();

		return true;
	}

	
	//the stream message to be send has a specific format.
	public boolean sendStreamMessage(StreamMessage msg) {

		int payloadLength;
		int bufferIndex;

		payloadLength = (msg.payloadLength[1] & 0xFF) | (msg.payloadLength[0] & 0xFF << 8);

		// the buffer contains the actual message send including the header
		byte[] buffer = new byte[MIN_MESSAGE_LEN + payloadLength];

		buffer[0] = (byte) (msg.type & 0xFF);
		buffer[1] = (byte) (msg.sync & 0xFF);
		buffer[2] = (byte) (msg.payloadLength[0] & 0xFF);
		buffer[3] = (byte) (msg.payloadLength[1] & 0xFF);

		for (bufferIndex = MIN_MESSAGE_LEN; bufferIndex < payloadLength + MIN_MESSAGE_LEN; bufferIndex++) {
			buffer[bufferIndex] = (byte) (msg.payload[bufferIndex - MIN_MESSAGE_LEN]);
		}

		try {
			icListenParams.strOutStream.write(buffer, 0, (MIN_MESSAGE_LEN + payloadLength));
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;

	}

	//this method will not even be executed if the method above doesn't return true.
	@Override
	public boolean startSystem(AcquisitionControl daqControl) {
		System.out.println("DaqSystem:startSystem");
		if (icListenParams.connect == false)
			return false;

		BlockingQueue<byte[]> queue = new LinkedBlockingQueue<byte[]>();

		Thread captureThread = new Thread(new CaptureThread(queue));
		captureThread.start();

		Thread dataProcessThread = new Thread(new DataProcessThread(queue));
		dataProcessThread.start();

		sendStreamMessage(START_STREAM_MESSAGE);
		setStreamStatus(STREAM_RUNNING);
		//for better stability of the buttons, otherwise they would behave improperly.
		TopToolBar.enableStartButton(false);
		TopToolBar.enableStopButton(true);

		return true;

	}

	//stop the acquisition process immediately.
	@Override
	public void stopSystem(AcquisitionControl daqControl) {
		System.out.println("DaqSystem:stopSystem");
		System.out.println(acquisitionControl.getAcquisitionProcess().getObserverName());
		stopCapture = true;

		int count = 0;
		while (stopCapture && captureRunning && ++count < 100) {
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				System.out.println("Error here.");
				e.printStackTrace();
			}
			sendStreamMessage(STOP_STREAM_MESSAGE);

			setStreamStatus(STREAM_CLOSED);
			
			//this will put the start and stop button into the correct state.
			TopToolBar.enableStartButton(true);

			TopToolBar.enableStopButton(false);

			//thought of using this to close the socket, turns out to be not effective at all.
			if (this instanceof icListenDaq == false) {
				try {
					icListenParams.streamSocket.close();
					icListenParams.commandSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			System.out.println();
			// System.out.println(acquisitionControl.getDataSourceName());
			// icListenParams.commandSocket.close();

			// System.out.println(acquisitionControl.getModuleStatus().toString());
			// System.out.println("Number of pam
			// processors"+acquisitionControl.getNumPamProcesses());
			// System.out.println(acquisitionControl.getSystemList().iterator());
			// acquisitionControl.
			// acquisitionControl.get
			// PamSettingManager.
			// if(PamSettingManager.getInstance().)
			// for(int i = 0 ; i < PamSettingManager.getInstance().)
			/*
			 * if(PamSettingManager.getInstance().) { try {
			 * icListenParams.commandSocket.close(); icListenParams.streamSocket.close();
			 * }catch(IOException e) { e.printStackTrace(); } }
			 * 
			 * }
			 */
			// icListenParams.commandSocket.
		}
	}

	@Override
	public boolean isRealTime() {
		System.out.println("DaqSystem:isRealTime");
		return true;
	}

	@Override
	public boolean canPlayBack(float sampleRate) {
		System.out.println("DaqSystem:canPlayBack");
		return true;
	}

	@Override
	public int getDataUnitSamples() {
		System.out.println("DaqSystem:getDataUnitSamples");
		return 1000;
	}

	/**
	 * @params: changeType: will print message to the console indicating successful operations.
	 * 			look for the constants from PamControllerInterface for different notification types.
	 * 			note:some println statements are only used for dedugging purposes.
	 */
	//@Override
	public void notifyModelChanged(int changeType) {
		System.out.println("DaqSystem:notifyModelChanged");
		// TODO Auto-generated method stub
		//super.notifyModelChanged(changeType);
		System.out.println("there is a model change");
		switch (changeType) {
		
		//case PamControllerInterface.
		case PamControllerInterface.ADD_CONTROLLEDUNIT:
			System.out.println("This statement should only appear when add a sound acquistion moudle.");
			System.out.println(acquisitionControl.getAcquisitionProcess().getObserverName().substring(9));
			icDaqs.put(acquisitionControl.getAcquisitionProcess().getObserverName().substring(9),this );
			break;
		case PamControllerInterface.REMOVE_DATABLOCK:
			System.out.println("WOOOOOO should only appear when remove this modules");
			ArrayList<String> soundControllerArrayList = new ArrayList<String>();
			for(int i = 0 ; i <PamController.getInstance().getDataBlocks().size();i++) {
				String s = PamController.getInstance().getDataBlocks().get(i).getDataName();
				if(s.startsWith("Sound Acquisition"))
					soundControllerArrayList.add(s);
			}
			
			for(int i = 0 ; i < soundControllerArrayList.size(); i++) {
				System.out.print(soundControllerArrayList.get(i)+'\t');
			}
			System.out.println();
			
			
			for(HashMap.Entry<String,icListenDaq> entry: icDaqs.entrySet()) {
				System.out.println("Key = "+entry.getKey()+ "\tvalue\t:");
				
				
				if(!soundControllerArrayList.contains(entry.getKey())) {
					if(entry.getValue().icListenParams.commandSocket!=null) {
						try {
							entry.getValue().icListenParams.commandSocket.close();
							entry.getValue().icListenParams.streamSocket.close();
						}catch(IOException e) {
							e.printStackTrace();
						}
					}
					icDaqs.remove(entry.getKey());
						
				}
			}
			break;
			//the code commented out below were used for testings
			/*try {
				
				System.out.println("Socket has been closed successfully!");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
			// if(this.icListenParams.connect==true) {
			/*
			 * try { this.icListenParams.commandSocket.close();
			 * this.icListenParams.streamSocket.close(); }catch(Exception e) {
			 * e.printStackTrace(); }
			 */

			//break;
		}

		/*
		 * int inst = 0; for(int i = 0;
		 * i<PamController.getInstance().getDataBlocks().size();i++) {
		 * 
		 * String s = PamController.getInstance().getDataBlocks().get(i).getDataName();
		 * //String a = PamController.getInstance().get
		 * if(s.startsWith("Sound Acquisition")) inst++;
		 * 
		 * System.out.println(s); //5System.out.println(a); }
		 * System.out.println("There are "+inst+"in total");
		 * 
		 * /*for(int i = 0 ; i < icDaqs.size();i++) { JPanel jp =
		 * icDaqs.get(i).getIcListenDaqPanel(); }
		 */
		/*
		 * System.out.println("icListen DAQ instances!"+icDaqs.size());
		 * 
		 * 
		 * if(inst<icDaqs.size()) { if(icDaqs.get(inst).icListenParams.commandSocket!=
		 * null) { try{ icDaqs.get(inst).icListenParams.commandSocket.close();
		 * icDaqs.get(inst).icListenParams.streamSocket.close(); }catch(IOException e) {
		 * e.printStackTrace(); } icListenDaq daq = icDaqs.get(inst);
		 * icDaqs.removeLast(); } }
		 */
	}
	
	//was intended to get the swing component, failed attempt
	public IcListenDaqPanel getIcListenDaqPanel() {
		System.out.println("DaqSystem:");
		return icListenDaqPanel;
	}

	//the daq system stops for whatever reasons.
	@Override
	public void daqHasEnded() {
		System.out.println("DaqSystem:");
		System.out.println("DAQ has ended");
	}

	//shouldn't return the plugin name as the device name, but we dont use this method anywhere,
	//only to fulfill the requirements from the interface or the parent class.
	@Override
	public String getDeviceName() {
		System.out.println("DaqSystem:");
		return pluginName;
	}

	//this returns the acquisitionControl component
	@Override
	public String getUnitName() {
		System.out.println("DaqSystem:");
		return acquisitionControl.getUnitName();
	}

	//again not used.
	@Override
	public String getUnitType() {
		System.out.println("DaqSystem:");
		return pluginName + "_Params";
	}

	@Override
	public Serializable getSettingsReference() {
		System.out.println("DaqSystem:");
		return null;
	}

	//dont care methods.
	
	@Override
	public long getSettingsVersion() {
		System.out.println("DaqSystem:");
		return 0;
	}

	//thought of closing the sockets inside the restoring settings, failed attempt again.
	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		System.out.println("DaqSystem:");
		//the comments below were used to restore all the sttings, and used to close the sockets.
		//inside notify model method.
		// icListenParams = ((IcListenParams)
		// pamControlledUnitSettings.getSettings()).clone();
		// System.out.println("Restore settings in progress");
		// return true;
		// pamControlledUnitSettings.
		// acquisitionControl.

		/// PamProcess.class.
		try {
			icListenParams.commandSocket.close();
			icListenParams.streamSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

//Data Process Thread took msg from the BlockingQueue
	class DataProcessThread implements Runnable {
		/* a queue to were the data captured will be stored */
		private BlockingQueue<byte[]> msgList;
		// TODO add variable for the current index

		/**
		 * Constructor that receives a queue to use for the data transfer
		 * 
		 * @param queue
		 */
		public DataProcessThread(BlockingQueue<byte[]> queue) {
			this.msgList = queue;
			// TODO add which index this thread is related to
		}

		/**
		 * Function where the data processing will take place Receives the data from the
		 * queue and performs the proper processing to match the required PAMGUARD
		 * RawDataUnit (-1,+1) and passes it to PAMGUARD using the newDataUnits list
		 */
		public void run() {
			/* buffer to receive data from message queue */
			byte[] buffer = new byte[IcListenParams.MAX_MESSAGE_SIZE];

			/* buffer to put processed samples into pamguard */
			double[] doubleBuf;

			double maxAmplitude = 1;

			int payloadIndex, doubleIndex, chunkIndex;
			/* doubleBufLen also is the number of samples */
			int chunkLen = 0, payloadLen, doubleBufLen;
			int sampleNumber;
			int sample;			

			char[] headerEventKey = null;
			boolean eventKeyMatch = false, sampleTallyInit = false;

			long milliSec, totalSamples = 0;
			long sequenceNumber = 0, sampleTally = 0;

			RawDataUnit newDataUnit = null;
			StreamMessage msg;

			/* Data capture is running */
			stopCapture = false;
			captureRunning = true;
			totalSamples = 0;

			/*
			 * Start capturing data Loop until requested to stop
			 */

			while (!stopCapture && icListenParams.connect) {
				/* Get the next message */

				try {
					buffer = msgList.take();
				} catch (InterruptedException e) {
					e.printStackTrace();
					break;
				}

				// convert it into the streamMessageFormat
				msg = new StreamMessage(buffer);

				// indicating how long the data packet is.
				payloadLen = (int) (((msg.payloadLength[0] & 0xFF) << 8) | (msg.payloadLength[1] & 0xFF));

				// byte offset 0 indicates the event header
				// if the message type is event type header.
				/* Check for the message type */
				if (msg.type == TYPE_HEADER) {
					// loop chunk by chunk
					for (payloadIndex = 0; payloadIndex < payloadLen; payloadIndex += chunkLen + CHUNK_PAYLOAD_OFFSET) {
						/* get the chunk length */
						/*
						 * in the payload which contains chunk, the first two bytes are usually chunk
						 * type and version
						 */
						/* the following two byte determines the chunk length */
						chunkLen = (int) (((msg.payload[payloadIndex + CHUNK_SIZE_OFFSET] & 0xFF) << 8)
								| (msg.payload[payloadIndex + CHUNK_SIZE_OFFSET + 1] & 0xFF));

						/* check the type of chunk */
						switch (msg.payload[payloadIndex]) {
						case EVENT_KEY:
							/* Save a copy of the event key to ensure that it matches the data message */
							headerEventKey = new char[chunkLen];
							// headerEventKey array saves whatever from payload within the chunk to the end
							for (chunkIndex = 0; chunkIndex < chunkLen; chunkIndex++) {
								headerEventKey[chunkIndex] = msg.payload[payloadIndex + CHUNK_PAYLOAD_OFFSET
										+ chunkIndex];
							}

							/* Get the sequence number *//* Retrieve the sequence number, see 4.1 */
							sequenceNumber = (((headerEventKey[EVENT_SEQUENCE_NUMBER_OFFSET] & 0xFF) << 56)
									| ((headerEventKey[EVENT_SEQUENCE_NUMBER_OFFSET + 1] & 0xFF) << 48)
									| ((headerEventKey[EVENT_SEQUENCE_NUMBER_OFFSET + 2] & 0xFF) << 40)
									| ((headerEventKey[EVENT_SEQUENCE_NUMBER_OFFSET + 3] & 0xFF) << 32)
									| ((headerEventKey[EVENT_SEQUENCE_NUMBER_OFFSET + 4] & 0xFF) << 24)
									| ((headerEventKey[EVENT_SEQUENCE_NUMBER_OFFSET + 5] & 0xFF) << 16)
									| ((headerEventKey[EVENT_SEQUENCE_NUMBER_OFFSET + 6] & 0xFF) << 8)
									| (headerEventKey[EVENT_SEQUENCE_NUMBER_OFFSET + 7] & 0xFF));
							/*
							 * check to see if any samples where missed by seeing if the sequence number is
							 * equal to the sample tally
							 */

							if (sequenceNumber != sampleTally) {
								/* check to see if the sample tally has been initialize */
								if (sampleTallyInit) {
									System.out.println(
											"Missed samples!\nSequence number: " + sequenceNumber + " Sample tally: "
													+ sampleTally + " Missed: " + (sequenceNumber - sampleTally));
									sampleTally = sequenceNumber;

									// TODO add code to handle missed samples
								} else {
									/* initialize the sample tally */
									sampleTally = sequenceNumber;
									sampleTallyInit = true;
								}
							}

							break;
						case DEVICE_CHUNK:

							break;
						case STATUS_CHUNK:

							break;
						case WAVE_SETUP_CHUNK:

							break;
						case SCALING_CHUNK:
							/* get the max amplitude */
							maxAmplitude = (double) ((msg.payload[payloadIndex + SCALING_MAX_AMPLITUDE_OFFSET] << 24)
									| ((msg.payload[payloadIndex + SCALING_MAX_AMPLITUDE_OFFSET + 1] & 0xFF) << 16)
									| ((msg.payload[payloadIndex + SCALING_MAX_AMPLITUDE_OFFSET + 2] & 0xFF) << 8)
									| ((msg.payload[payloadIndex + SCALING_MAX_AMPLITUDE_OFFSET + 3] & 0xFF)));
							break;
						}

					}
				} else if (msg.type == TYPE_DATA) {
					/*
					 * Data message received Go through payload looking for the necessary chunks
					 */
					for (payloadIndex = 0; payloadIndex < payloadLen; payloadIndex += chunkLen + CHUNK_PAYLOAD_OFFSET) {
						/* get the length of the chunk */
						chunkLen = (int) (((msg.payload[payloadIndex + CHUNK_SIZE_OFFSET] & 0xFF) << 8)
								| (msg.payload[payloadIndex + CHUNK_SIZE_OFFSET + 1] & 0xFF));

						/* check the chunk type */
						switch (msg.payload[payloadIndex]) {
						case EVENT_KEY:

							/* check to see if the event key matches */
							if (headerEventKey == null) {
								eventKeyMatch = false;
							} else {
								eventKeyMatch = true;
								for (chunkIndex = 0; chunkIndex < chunkLen && eventKeyMatch; chunkIndex++) {
									if (msg.payload[payloadIndex + CHUNK_PAYLOAD_OFFSET
											+ chunkIndex] != headerEventKey[chunkIndex]) {
										eventKeyMatch = false;
									}
								}
							}

							break;
						case DATA_CHUNK:
							/* make sure the event key matches the header */
							if (eventKeyMatch) {
								/* get the number of samples */
								doubleBufLen = (int) (((msg.payload[payloadIndex + DATA_SAMPLE_OFFSET] & 0xFF) << 8)
										| (msg.payload[payloadIndex + DATA_SAMPLE_OFFSET + 1] & 0xFF));

								/*
								 * Check if any samples where missed since the header by comparing the sample
								 * number to the tally minus the sequence number
								 */
								sampleNumber = (((msg.payload[payloadIndex + DATA_SAMPLE_NUMBER_OFFSET] & 0xFF) << 24)
										| ((msg.payload[payloadIndex + DATA_SAMPLE_NUMBER_OFFSET + 1] & 0xFF) << 16)
										| ((msg.payload[payloadIndex + DATA_SAMPLE_NUMBER_OFFSET + 2] & 0xFF) << 8)
										| (msg.payload[payloadIndex + DATA_SAMPLE_NUMBER_OFFSET + 3] & 0xFF));

								/* check if any samples were missed */
								if ((long) sampleNumber != (sampleTally - sequenceNumber)) {
									System.out.println("Missed samples!\nSample number: " + sampleNumber
											+ " Samples since header: " + (sampleTally - sequenceNumber));
									// TODO add code to handle missed samples
								}

								/* update the sample tally */
								sampleTally = sampleTally + (long) doubleBufLen;
								int byteIncrease = icListenDaqPanel.BIT_DEPTH_VALUE/8;
								doubleBuf = new double[doubleBufLen];
								
								/* go through the samples and put them into the RawDataUnit list */
								for (doubleIndex = 0, chunkIndex = (payloadIndex
										+ DATA_DATA_OFFSET); doubleIndex < doubleBufLen; doubleIndex++, chunkIndex += byteIncrease) {
									/* get the samples */
									
									if (byteIncrease == 2)
									{
										sample = (int) (/*((msg.payload[chunkIndex + 2]) & 0xFF)
												|*/ ((msg.payload[chunkIndex + 1] & 0xFF)<<8)
												| ((msg.payload[chunkIndex] & 0xFF) << 16));
									}
									else
									{
										sample = (int) (((msg.payload[chunkIndex + 2]) & 0xFF)
												| ((msg.payload[chunkIndex + 1] & 0xFF) << 8)
												| ((msg.payload[chunkIndex] & 0xFF) << 16));
										
						
									}
									
									/*Check the sign (most significant bit is set) and extend the sign if nesc. */
									if ((msg.payload[chunkIndex] & 0x0080) == 0x0080) {

										/* extend the sign */
										sample = sample | 0xFF000000;

									}

									/* Scale the sample to between -1 and 1 */
									doubleBuf[doubleIndex] = sample / maxAmplitude;

								}
								/* get how much time this covers */
								milliSec = acquisitionControl.getAcquisitionProcess()
										.absSamplesToMilliseconds(totalSamples);

								/* add it as a new data unit */
								// TODO add a shift to the channelBitmap that uses the current channel index
								newDataUnit = new RawDataUnit(milliSec, 0x01, totalSamples, (long) doubleBufLen);
								newDataUnit.setRawData(doubleBuf);
								icListenParams.audioDataQueue.addNewData(newDataUnit);

								/* tally the total samples */
								totalSamples += (long) doubleBufLen;
							}

							break;
						}

					}
				} else {
					/* Can ignore since not an expected message type */
				}
			}
			/* reset any necessary components */
			sampleTallyInit = false;

			/* stop capture and return */
			captureRunning = false;
		} /* end run */

	}

	class CaptureThread implements Runnable {
		/* amount of bytes to read */
		private final int CHECK_READ = 4;

		/* queue to put the received messages */
		private BlockingQueue<byte[]> msgList;

		// TODO add a local DataInputStream

		/**
		 * Constructor that receives a queue to use for the data transfer
		 * 
		 * @param queue
		 */
		public CaptureThread(BlockingQueue<byte[]> queue) {
			this.msgList = queue;
			// TODO Pass this an DataInputStream
		}

		/**
		 * Function where a message will be received and put into the message queue for
		 * future processing
		 */
		public void run() {
			/* Data capture is running */
			stopCapture = false;
			captureRunning = true;

			/* get a message and add it to the queue */
			try {
				while (!stopCapture) {
					msgList.add(getMessage());
				}
			} catch (NullPointerException e) {
				/* if a null was added this catch pulls it out of the capture loop */
				stopCapture = true;
			}
			captureRunning = false;
		}

		/**
		 * Function that gets a message from the icListen and returns it
		 * 
		 * @return
		 */
		public byte[] getMessage() {
			/* Buffer to put the message */
			byte[] buffer = new byte[IcListenParams.MAX_MESSAGE_SIZE];
			/* temporary buffer used when an unaligned message was found */
			byte[] tmpBuf = new byte[CHECK_READ];
			int readSize = CHECK_READ;
			int offset = 0;

			boolean messageFound = false;

			/* run until capture is requested to stop */
			while (!stopCapture) {
				/*
				 * read in bytes from the icListen will either be a check read of 4 bytes or the
				 * rest of a message
				 */
				try {
					// TODO replace this with the local DataInputStream
					icListenParams.strinStream.read(buffer, offset, readSize);
				} catch (IOException e) {
					System.out.println("IOException when reading start system response");
					e.printStackTrace();
					stopCapture = true;
					return null;
				} catch (ArrayIndexOutOfBoundsException e) {
					/*
					 * A wrong message was found once a possible start was found message has not
					 * been found, ensure it doesn't check it for a message start will try to
					 * re-align
					 */
					System.out.println("Array bounds exceeded: Offset: " + offset + " Size: " + readSize);
					messageFound = false;
					buffer[0] = 0;
					buffer[1] = 0;
					buffer[2] = 0;
					buffer[3] = 0;
				}

				if (messageFound) {
					/* the message is found return it */
					return buffer;
				} else {
					/*
					 * The following is a form of unrolled loop that searches through the read in
					 * message to check for a message start. When one is found it will re-order
					 * bytes in the buffer to be correct. It will then reduce the possibility that a
					 * false positive was found. If it is still determined to be a message start,
					 * set up the read for the rest of the message
					 */

					/* no message is currently found, Check for the alignment */
					if ((buffer[0] == IcListenParams.EVENT_HEADER || buffer[0] == IcListenParams.DATA)
							&& buffer[1] == IcListenParams.SYNC) {
						/*
						 * Properly aligned message Get the message length
						 */
						readSize = ((buffer[2] & 0xFF) << 8) | (buffer[3] & 0xFF);

						/*
						 * Make Sure that the read size is valid to reduce the chance that a none
						 * message start was found
						 */
						if (readSize > IcListenParams.MAX_MESSAGE_SIZE) {
							/* not a valid message */
							readSize = CHECK_READ;
							offset = 0;
							messageFound = false;
						} else {
							/* message is now found and aligned */
							messageFound = true;
							offset = 4;
						}
					} else {
						/* unaligned message, Check how desynced the message is */
						if ((buffer[1] == IcListenParams.EVENT_HEADER || buffer[1] == IcListenParams.DATA)
								&& buffer[2] == IcListenParams.SYNC) {
							/* read in the next couple of bytes to use if the message start is found */
							try {
								// TODO replace this with the local DataInputStream
								icListenParams.strinStream.read(tmpBuf, 0, CHECK_READ);
							} catch (IOException e) {
								System.out.println("IOException when reading start system response");
								e.printStackTrace();
								stopCapture = true;
								captureRunning = false;
								return null;
							}

							/* put the read in bytes into the correct location */
							buffer[0] = buffer[1]; /* Type byte */
							buffer[1] = buffer[2]; /* sync byte */
							buffer[2] = buffer[3]; /* high length byte */
							buffer[3] = tmpBuf[0]; /* low length byte */
							/* payload bytes */
							buffer[4] = tmpBuf[1]; /* low length byte */
							buffer[5] = tmpBuf[2]; /* low length byte */
							buffer[6] = tmpBuf[3]; /* low length byte */

							/* Get the message length */
							readSize = (((buffer[2] & 0xFF) << 8) | (buffer[3] & 0xFF)) - 3;

							/*
							 * Make Sure that the read size is valid to reduce the chance that a none
							 * message start was found
							 */
							if (readSize + 3 > IcListenParams.MAX_MESSAGE_SIZE) {
								/* not a valid message */
								readSize = CHECK_READ;
								offset = 0;
								messageFound = false;
							} else {
								/* message is now found and aligned */
								messageFound = true;
								offset = 4 + 3;
							}
						} else if ((buffer[2] == IcListenParams.EVENT_HEADER || buffer[2] == IcListenParams.DATA)
								&& buffer[3] == IcListenParams.SYNC) {
							/* read in the next couple of bytes to use if the message start is found */
							try {
								// TODO replace this with the local DataInputStream
								icListenParams.strinStream.read(tmpBuf, 0, CHECK_READ);
							} catch (IOException e) {
								System.out.println("IOException when reading start system response");
								e.printStackTrace();
								stopCapture = true;
								captureRunning = false;
								return null;
							}

							/* put the read in bytes into the correct location */
							buffer[0] = buffer[2]; /* Type byte */
							buffer[1] = buffer[3]; /* sync byte */
							buffer[2] = tmpBuf[0]; /* high length byte */
							buffer[3] = tmpBuf[1]; /* low length byte */
							/* payload bytes */
							buffer[4] = tmpBuf[2];
							buffer[5] = tmpBuf[3];

							/* Get the message length */
							readSize = (((buffer[2] & 0xFF) << 8) | (buffer[3] & 0xFF)) - 2;

							/*
							 * Make Sure that the read size is valid to reduce the chance that a none
							 * message start was found
							 */
							if (readSize + 2 > IcListenParams.MAX_MESSAGE_SIZE) {
								/* not a valid message */
								readSize = CHECK_READ;
								offset = 0;
								messageFound = false;
							} else {
								/* message is now found and aligned */
								messageFound = true;
								offset = 4 + 2;
							}
						} else if ((buffer[3] == IcListenParams.EVENT_HEADER || buffer[3] == IcListenParams.DATA)
								&& tmpBuf[0] == IcListenParams.SYNC) {
							/* read in the next couple of bytes to use if the message start is found */
							try {
								// TODO replace this with the local DataInputStream
								icListenParams.strinStream.read(tmpBuf, 0, CHECK_READ);
							} catch (IOException e) {
								System.out.println("IOException when reading start system response");
								e.printStackTrace();
								stopCapture = true;
								captureRunning = false;
								return null;
							}

							/* put the read in bytes into the correct location */
							buffer[0] = buffer[3]; /* Type byte */
							buffer[1] = tmpBuf[0]; /* sync byte */
							buffer[2] = tmpBuf[1]; /* high length byte */
							buffer[3] = tmpBuf[2]; /* low length byte */
							/* payload byte */
							buffer[4] = tmpBuf[3]; /* low length byte */

							/* Get the message length */
							readSize = (((buffer[2] & 0xFF) << 8) | (buffer[3] & 0xFF)) - 1;

							/*
							 * Make Sure that the read size is valid to reduce the chance that a none
							 * message start was found
							 */
							if (readSize + 1 > IcListenParams.MAX_MESSAGE_SIZE) {
								/* not a valid message */
								readSize = CHECK_READ;
								offset = 0;
								messageFound = false;
							} else {
								/* message is now found and aligned */
								messageFound = true;
								offset = 4 + 1;
							}
						} else {
							/* No message start found, make sure the offset and size are still set */
							readSize = CHECK_READ;

							offset = 0;
						}
					}
				}
			}
			return null;
		}
	}

	//another wrong attempt to use the Pamguard MVC.
	@Override
	public long getRequiredDataHistory(PamObservable o, Object arg) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void update(PamObservable o, PamDataUnit arg) {
		// TODO Auto-generated method stub

	}

	//used improperly, i think.
	@Override
	public void removeObservable(PamObservable o) {
		// TODO Auto-generated method stub
		System.out.println("QQQQQQQQQQQQQQQQQQQQQQQ");
	}

	
	@Override
	public void setSampleRate(float sampleRate, boolean notify) {
		// TODO Auto-generated method stub

	}

	//maybe this is not used properly in 
	@Override
	public void noteNewSettings() {
		// TODO Auto-generated method stub
		System.out.println("ccccccc");
	}

	@Override
	public String getObserverName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void masterClockUpdate(long milliSeconds, long sampleNumber) {
		// TODO Auto-generated method stub

	}

	//
	@Override
	public PamObserver getObserverObject() {
		// TODO Auto-generated method stub
		return null;
	}
	
	IcListenPlayback_Beta playBackSystem = null;
	@Override
	public PlaybackSystem getPlaybackSystem(PlaybackControl playbackControl, DaqSystem daqSystem) {
		System.out.println("daqSystem:getPlaybackSystem");
		icListenDaq.playbackControl = playbackControl;
		
		playBackSystem = new IcListenPlayback_Beta(playbackControl);
		
		if (playBackSystem == null)
		{
			playBackSystem = (IcListenPlayback_Beta)playbackControl.getFilePlayback();
			System.err.print("Sorry, can only give default!\n");
		}
		
		return playBackSystem;
	}
}/* end inner class DataProcessThread */