package PamController;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;

import SoundRecorder.RecorderControl;
import SoundRecorder.RecorderTrigger;
import SoundRecorder.RecorderTriggerData;

public class NetworkController {

	
	private PamController pamController;
	
	private ListenerThread listenerThread;
	
	private DatagramSocket receiveSocket;

	private DatagramPacket udpPacket;
	
	private boolean initialisationComplete = false;
	
	static private final int MAX_COMMAND_LENGTH = 256;
	
	private byte[] byteBuffer = new byte[MAX_COMMAND_LENGTH];
	
	private NetworkRecorderTrigger[] recorderTriggers;

	public NetworkController(PamController pamController) {
		super();
		this.pamController = pamController;
		
		listenerThread = new ListenerThread();
		Thread aThread = new Thread(listenerThread);
		aThread.start();
	}
	
	class ListenerThread implements Runnable {

		@Override
		public void run() {
			if (openUDPPort() == false) {
				return;
			}
			sitInLoop();
			closeUDPPort();
		}
		
	}
	
	/**
	 * Open the UDP port. 
	 * @return true if opened OK
	 */
	public boolean openUDPPort() {
		int udpPort = pamBuoyGlobals.networkControlPort;
		try {
			receiveSocket = new DatagramSocket(udpPort);
			receiveSocket.setSoTimeout(0);
			udpPacket = new DatagramPacket(byteBuffer, MAX_COMMAND_LENGTH);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * Close the UDP port. 
	 */
	public void closeUDPPort() {
		// TODO Auto-generated method stub
		if (receiveSocket == null) {
			return;
		}
		receiveSocket.close();
		receiveSocket = null;
	}

	/**
	 * Infinite loop. The program sits here waiting for
	 * commands and interpreting them as needs. 
	 * <br>It will exit when InterpretCommand returns
	 * false, which it should only do when the exit 
	 * program command has been sent. 
	 */
	public void sitInLoop() {
		
		String udpCommand = null;
		
		while (true) {
			udpCommand = getCommand();
			if (udpCommand == null) {
				continue;
			}
			if (interpretCommand(udpCommand) == false) {
				break;
			}
		}
	}

	/**
	 * Interpret and act on a udp command string. 
	 * @param command command string
	 * @return false if the command was to exit
	 * the program (in which case this thread will
	 * exit and close the port). True otherwise. 
	 */
	private boolean interpretCommand(String command) {
		//System.out.println(String.format("New UDP Command %s", command));
		
		command = command.toLowerCase();
		if (command.equals("pgstart")) {
			sendData("PgAck " + "pgstart");
			pamController.pamStart();
		}
		else if (command.equals("pgstop")) {
			sendData("PgAck " + "pgstop");
			pamController.pamStop();
		}
		else if (command.equals("pgping")) {
			sendData("PgAck " + "pgping");
		}
		else if (command.equals("pgstatus")) {
			sendData("PgAck Status " + pamController.getPamStatus());
		}
		else if (command.equals("pgsetrec")) {
			sendData("PgAck pgsetrec");
			
			//triggerRecording(String name, int seconds);
		}
		else if (command.equals("pgexit")) {
			sendData("Exiting PAMGUARD");
			System.exit(0);
			return false;
		}else{
			sendData("PgAck " + "Cmd Not Recognised.");
		}

		
		return true;
	}
	
	private boolean sendData(String dataString) {
		DatagramPacket packet = new DatagramPacket(dataString.getBytes(), dataString.length());
		packet.setAddress(udpPacket.getAddress());
		packet.setPort(udpPacket.getPort());
		try {
			receiveSocket.send(packet);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * Blocking function to wait for a command string to be sent 
	 * over UDP
	 * @return command string or null if should exit. 
	 */
	private String getCommand() {
		try {
			receiveSocket.receive(udpPacket);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return new String(udpPacket.getData(), 0, udpPacket.getLength());
	}

	public void notifyModelChanged(int changeType) {
		switch (changeType){
		case PamControllerInterface.INITIALIZATION_COMPLETE:
			initialisationComplete = true;
			createRecorderTriggers();
			break;
		}
		
	}
	
	private void createRecorderTriggers() {
		ArrayList<PamControlledUnit> recorders = pamController.findControlledUnits(RecorderControl.recorderUnitType);
		if (recorders.size() == 0) {
			return;
		}
		int n = recorders.size();
		recorderTriggers = new NetworkRecorderTrigger[n];
		for (int i = 0; i < n; i++) {
			recorderTriggers[i] = new NetworkRecorderTrigger((RecorderControl) recorders.get(i));
			RecorderControl.registerRecorderTrigger(recorderTriggers[i]);
		}
	}
	
	private boolean triggerRecording(String name, int seconds) {
		NetworkRecorderTrigger t = findTrigger(name);
		if (t == null) {
			return false;
		}
		RecorderTriggerData rtd = new RecorderTriggerData(0, seconds);
		t.setRecorderTriggerData(rtd);
		RecorderControl.actionRecorderTrigger(t);
		return true;
	}
	
	private NetworkRecorderTrigger findTrigger(String name) {
		if (recorderTriggers == null) {
			return null;
		}
		for (int i = 0; i < recorderTriggers.length; i++) {
			if (recorderTriggers[i].recorderControl.getUnitName().equals(name)) {
				return recorderTriggers[i];
			}
		}
		return null;
	}

	private class NetworkRecorderTrigger implements RecorderTrigger {

		private String name;
		
		private RecorderControl recorderControl;
		
		private RecorderTriggerData recorderTriggerData;
		
		/**
		 * @param name
		 */
		public NetworkRecorderTrigger(RecorderControl recorderControl) {
			super();
			this.recorderControl = recorderControl;
			this.name = "Network Control " + recorderControl.getUnitName();
			recorderTriggerData = new RecorderTriggerData(0,0);
		}

		@Override
		public RecorderTriggerData getTriggerData() {
			return recorderTriggerData;
		}

		@Override
		public String getTriggerName() {
			return name;
		}

		/**
		 * @param recorderTriggerData the recorderTriggerData to set
		 */
		public void setRecorderTriggerData(RecorderTriggerData recorderTriggerData) {
			this.recorderTriggerData = recorderTriggerData;
		}
		
	}
}
