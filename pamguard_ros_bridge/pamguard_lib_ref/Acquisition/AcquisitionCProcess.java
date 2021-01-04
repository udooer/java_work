package Acquisition;

import java.io.File;
import java.util.List;

import PamController.pamBuoyGlobals;
import PamDetection.RawDataUnit;
import backendCInterfase.BackendProcess;

public class AcquisitionCProcess extends BackendProcess {

	private AcquisitionControl acquisitionControl;

	private AcquisitionProcess acquisitionProcess;

	private DaqSystem runningDaqSystem;

	private long totalSamples;

	protected List<RawDataUnit> newDataUnits;

	private AcquisitionCParameters acquisitionCParameters;

	public AcquisitionCProcess(AcquisitionProcess parentProcess) {
		super(parentProcess);
		acquisitionProcess = parentProcess;
		acquisitionControl = parentProcess.acquisitionControl;
	}

	@Override
	public boolean prepareProcess() {
		runningDaqSystem = acquisitionProcess.getRunningSystem();
		if (runningDaqSystem == null){
			return false;
		}
		totalSamples = 0;
		this.newDataUnits = acquisitionControl.getDaqProcess().getNewDataUnits();
		return true;
	}

	@Override
	public Object getParams() {
		acquisitionCParameters = new AcquisitionCParameters();
		AcquisitionParameters mainParams = acquisitionProcess.acquisitionControl.acquisitionParameters;
		acquisitionCParameters.setDaqType(mainParams.getDaqSystemType());
		acquisitionCParameters.deviceNumber = 0;
		acquisitionCParameters.nChan = Math.min(AcquisitionCParameters.MAXCHAN, mainParams.getNChannels());
		acquisitionCParameters.sampleRate = (int) mainParams.sampleRate;
		for (int i = 0; i < acquisitionCParameters.nChan; i++) {
			acquisitionCParameters.channelList[i] = mainParams.getChannelList(i);
		}
		if (acquisitionCParameters.deviceType == AcquisitionCParameters.WAVFILE) {
			File currFile = ((FileInputSystem) acquisitionProcess.getRunningSystem()).getCurrentFile();
			if (currFile != null) {
				acquisitionCParameters.soundFile = currFile.getAbsolutePath();
			}
		}
		/* Setup the ALSA sound card!
		 * 
		 * */
		acquisitionCParameters.spareString1=pamBuoyGlobals.remoteDeviceName;
		acquisitionCParameters.spareString2=pamBuoyGlobals.remoteDeviceName;
		acquisitionCParameters.spareInt1=pamBuoyGlobals.alsaBufferSize;
		acquisitionCParameters.spareInt2=pamBuoyGlobals.alsaBufferFreq;
		
		return acquisitionCParameters;
	}

	@Override
	public void setStatus(int status) {
		switch (status) {
		case DaqSystem.STREAM_CLOSED:
			System.out.println("Java: Stream Closed");
			break;
		case DaqSystem.STREAM_OPEN:
			System.out.println("Java: Stream Open");
			break;
		case DaqSystem.STREAM_RUNNING:
			System.out.println("Java: Stream Running");
			break;
		case DaqSystem.STREAM_PAUSED:
			System.out.println("Java: Stream Paused");
			break;
		case DaqSystem.STREAM_ENDED:
			System.out.println("Java: Stream Ended");
			break;
		}
		runningDaqSystem.setStreamStatus(status);

	}

	@Override
	synchronized public void newData(int dataType, int channelMap, short[] data, int dataLenBytes) {
		
		if (acquisitionCParameters.deviceType == 2) {
			// need to wait since it's a wav file. 
			while (newDataUnits.size() > 10) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		synchronized (newDataUnits) {
//			System.out.println(String.format("New acquisition data type %d, length %d shorts or %d bytes",
//					dataType, data.length, dataLenBytes));
			int nChan = acquisitionProcess.getAcquisitionControl().acquisitionParameters.nChannels;
			int nSamps = data.length / nChan;
			double newDataBuffer[];
			int index;
			double scale = 1<<15;
			RawDataUnit rawDataUnit;
			long millis = acquisitionProcess.absSamplesToMilliseconds(totalSamples);
			for (int i = 0; i < nChan; i++) {
				index = i;
				newDataBuffer = new double[nSamps];
				for (int s = 0; s < nSamps; s++) {
					newDataBuffer[s] = data[index] / scale;
					index += nChan;
//					newDataBuffer[s] = s/scale;
				}
				rawDataUnit = new RawDataUnit(millis, 1<<i, totalSamples, nSamps);
				rawDataUnit.setRawData(newDataBuffer, true);
				newDataUnits.add(rawDataUnit);
			}
			totalSamples += nSamps;
		}
//		System.out.println("LEave Acquisition new data");
	}


}
