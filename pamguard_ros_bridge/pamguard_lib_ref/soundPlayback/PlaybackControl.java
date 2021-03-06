package soundPlayback;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.JMenuItem;
import javax.swing.SwingWorker;

import Acquisition.AcquisitionProcess;
import Acquisition.DaqSystem;
import Acquisition.offlineFuncs.OfflineFileServer;
import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamController;
import PamController.PamControllerInterface;
import PamController.PamSettingManager;
import PamController.PamSettings;
import PamView.MenuItemEnabler;
import PamView.TopToolBar;
import PamguardMVC.LoadObserver;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;

/**
 * Main Pam Controller for sound playback modules.
 * @author Doug Gillespie
 *
 */
public class PlaybackControl extends PamControlledUnit implements PamSettings {

	protected PlaybackParameters playbackParameters = new PlaybackParameters();
	
	protected PlaybackProcess playbackProcess;
	
	protected FilePlayback filePlayback;
	
	protected PlaybackSystem playbackSystem;

	private LoadDataWorker loadWorker;
	
	private static PlaybackControl viewerPlayback;
	
	private static MenuItemEnabler playButtonEnabler = new MenuItemEnabler();
	private static MenuItemEnabler stopButtonEnabler = new MenuItemEnabler();
	
//	private JButton viewerStopButton;
	
	public PlaybackControl(String unitName) {
		
		super("Sound Playback", unitName);
		
		filePlayback = new FilePlayback(this);
		
		addPamProcess(playbackProcess = new PlaybackProcess(this));

		newSettings();

		PamSettingManager.getInstance().registerSettings(this);
		
		if (viewerPlayback == null) {
			viewerPlayback = this;
		}	
//		TopToolBar toolbar = TopToolBar.getToolBar();
//		toolbar.add(viewerStopButton = new JButton(new ImageIcon(ClassLoader
//				.getSystemResource("Resources/playbackStop.png"))));
//		viewerStopButton.setToolTipText("Stop sound playback");
//		viewerStopButton.addActionListener(new StopPlayback());
//		
		enablePlayControls(false);
	}
	
	/**
	 * Static easy access to the playback module in PAMGUARD viewer 
	 * mode. Other modules will be sending commands to this module
	 * instructing it to play sections of data out through 
	 * the speakers. <p>
	 * This may involve allowing the playback module to use it's
	 * own data source, but it may also be instructed to take 
	 * data from elsewhere - e.g. reconstructed clicks from the 
	 * click detector. 
	 * @return
	 */
	public static PlaybackControl getViewerPlayback() {
		return viewerPlayback;
	}

	public static void registerPlayButton(AbstractButton button) {
		playButtonEnabler.addMenuItem(button);
	}
	
	public static void registerStopButton(AbstractButton button) {
		stopButtonEnabler.addMenuItem(button);
	}

	public Serializable getSettingsReference() {
		return playbackParameters;
	}

	public long getSettingsVersion() {
		return PlaybackParameters.serialVersionUID;
	}

	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {

		playbackParameters = ((PlaybackParameters) pamControlledUnitSettings.getSettings()).clone();
		newSettings();
		return true;
	}
	
	protected void newSettings() {
		// work out the data source and process.
		PamDataBlock sourceData = PamController.getInstance().getRawDataBlock(playbackParameters.dataSource);
		playbackSystem = findPlaybackSystem(sourceData);
		playbackProcess.noteNewSettings();
	}

	@Override
	public JMenuItem createDetectionMenu(Frame parentFrame) {
		JMenuItem playbackMenu;
		playbackMenu = new JMenuItem(getUnitName() + " ...");
		playbackMenu.addActionListener(new PlaybackSettings(this, parentFrame));
		return playbackMenu;
	}
	
	public PlaybackSystem findPlaybackSystem(PamDataBlock sourceDataBlock) {
		
		if (sourceDataBlock == null) return null;
		
		// from the data source, work out what type of playbacksystem is being used, if any.
		PamProcess sourceProcess = sourceDataBlock.getSourceProcess();
		// this should be an Acquisition process, but catch exception if it isn't. 
		if (sourceProcess.getClass() != AcquisitionProcess.class) return null;
		AcquisitionProcess daqProcess = null;
		try {
			daqProcess = (AcquisitionProcess) sourceProcess;
		}
		catch (Exception ex) {
			return null;
		}
		if (daqProcess == null) return null;
		DaqSystem daqSystem = daqProcess.getAcquisitionControl().findDaqSystem(null);
		if (daqSystem == null) return null;
		if (daqSystem.isRealTime() == false){
			return filePlayback;
		}
		else {
			return daqSystem.getPlaybackSystem();
		}
	}
	
	public int getMaxPlaybackChannels(PlaybackSystem playbackSystem) {
		if (playbackSystem == null) return 0;
		return playbackSystem.getMaxChannels();
	}
	
	public float choseSampleRate() {
		if (playbackSystem == null) return 0;
		else if (playbackSystem == filePlayback) {
			if (playbackParameters.playbackRate == 0 || playbackParameters.defaultSampleRate) {
				return playbackProcess.getSampleRate();
			}
			else {
				return playbackParameters.playbackRate;
			}
		}
		return playbackProcess.getSampleRate();
	}
	
	class PlaybackSettings implements ActionListener {

		Frame parentFrame;
		
		PlaybackControl playbackControl;
		
		public PlaybackSettings(PlaybackControl playbackControl, Frame parentFrame) {
			this.playbackControl = playbackControl;
			this.parentFrame = parentFrame;
		}

		public void actionPerformed(ActionEvent e) {

			PlaybackParameters newParams = PlaybackDialog.showDialog(parentFrame, playbackParameters, playbackControl);
			if (newParams != null) {
				playbackParameters = newParams.clone();
				playbackProcess.noteNewSettings();
			}
			
		}
		
	}
	
	class StopPlayback implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			stopViewerPlayback();
		}
	}

	@Override
	public void notifyModelChanged(int changeType) {
		switch(changeType) {
		case PamControllerInterface.CHANGED_PROCESS_SETTINGS:
		case PamControllerInterface.OFFLINE_DATA_LOADED:
			newSettings();
		}
	}

	/**
	 * 
	 * @return playback parameters. 
	 */
	public PlaybackParameters getPlaybackParameters() {
		return playbackParameters;
	}

	/**
	 * Play back raw audio data for a specific channel. 
	 * @param channelMap
	 * @param startMillis
	 * @param endMillis
	 * @param playbackProgressMonitor
	 * @return true if playback seems to have started OK
	 */
	public boolean playViewerData(int channelMap, long startMillis, long endMillis, 
			PlaybackProgressMonitor playbackProgressMonitor) {

		PamDataBlock dataBlock = playbackProcess.getParentDataBlock();
		if (dataBlock == null) {
			return false;
		}
		enablePlayControls(true);
		int currentChannels = playbackParameters.channelBitmap;
		playbackParameters.channelBitmap = channelMap;
		playbackProcess.noteNewSettings();
		this.playbackProgressMonitor = playbackProgressMonitor;
		this.playbackStartMillis = startMillis;
		this.playbackEndMillis = endMillis;
		playbackProcess.prepareProcess();
		playbackProcess.pamStart();
		dataBlock.orderOfflineData(playbackProcess, new PlayLoadObserver(), 
				startMillis, endMillis, 
				PamDataBlock.OFFLINE_DATA_CANCEL, true);
		if (playbackProgressMonitor != null) {
			playbackProgressMonitor.setStatus(PlaybackProgressMonitor.PLAY_START);
		}

//		playbackParameters.channelBitmap = currentChannels;
		return true;
	}
	
	/**
	 * Enable any controls associated with playback
	 * @param playing true if playback active
	 */
	private static void enablePlayControls(boolean playing) {
		playButtonEnabler.enableItems(!playing);
		stopButtonEnabler.enableItems(playing);
		TopToolBar.enableStopButton(playing);
		TopToolBar.enableStartButton(!playing);
	}

	/**
	 * The simplest of the viewer instructions simply 
	 * instructs the playback module to play data from 
	 * it's own data source between the given times. 
	 * @param startMillis start time
	 * @param endMillis  end time
	 * @param playbackProgressMonitor progress monitor
	 * @return true if playback seems to have started OK
	 */
	public boolean playViewerData(long startMillis, long endMillis, 
			PlaybackProgressMonitor playbackProgressMonitor) {
		
		int defaultChannels = playbackParameters.channelBitmap;
		return playViewerData(defaultChannels, startMillis, endMillis, playbackProgressMonitor);
		
	}
	
	/**
	 * This one is for playing back non-raw data through the sound card. 
	 * e.g. the click detector will play back clicks spaced with zeros between
	 * them. 
	 * <p>A new thread will be launched which will call back into playDataServer. 
	 * playDataServer will have to prepare rawDataUnits which get passed on to the 
	 * PlaybackProcess. These rawDataUnits will have to follow the basic form of 
	 * any normal rawDataUnit, but can probably vary a bit in length if necessary. 
	 * @param channelMap
	 * @param startMillis
	 * @param endMillis
	 * @param playbackProgressMonitor
	 * @param playDataServer
	 * @return true if playback started successfully. 
	 */
	public boolean playViewerData(int channelMap, long startMillis, long endMillis, 
			PlaybackProgressMonitor playbackProgressMonitor, PlaybackDataServer playDataServer) {

		enablePlayControls(true);
		int currentChannels = playbackParameters.channelBitmap;
		playbackParameters.channelBitmap = channelMap;
		playbackProcess.noteNewSettings();
		this.playbackProgressMonitor = playbackProgressMonitor;
		this.playbackStartMillis = startMillis;
		this.playbackEndMillis = endMillis;
		playbackProcess.prepareProcess();
		playbackProcess.pamStart();
		loadWorker = new LoadDataWorker(channelMap, startMillis, endMillis, playbackProgressMonitor, playDataServer);
		loadWorker.execute();
		if (playbackProgressMonitor != null) {
			playbackProgressMonitor.setStatus(PlaybackProgressMonitor.PLAY_START);
		}

		return true;
	}
	
	class LoadDataWorker extends SwingWorker<Integer, ProgressData> implements PlaybackProgressMonitor {
		private int channelMap;
		private long startMillis;
		private long endMillis;
		private PlaybackProgressMonitor playbackProgressMonitor;
		private PlaybackDataServer playDataServer;
		private LoadDataWorker(int channelMap, long startMillis, long endMillis, 
			PlaybackProgressMonitor playbackProgressMonitor, PlaybackDataServer playDataServer) {
			this.channelMap = channelMap;
			this.startMillis = startMillis;
			this.endMillis = endMillis;
			this.playbackProgressMonitor = playbackProgressMonitor;
			this.playDataServer = playDataServer;
		}
		
		@Override
		protected Integer doInBackground() {
			playDataServer.orderPlaybackData(playbackProcess, this, 
					startMillis, endMillis);
			return null;
		}

		@Override
		protected void done() {
			super.done();
		}

		@Override
		protected void process(List<ProgressData> chunks) {
			int n = chunks.size();
			ProgressData pd;
			for (int i = 0; i < n; i++) {
				pd = chunks.get(i);
				switch (pd.type) {
				case ProgressData.TYPE_PROGRESS:
					this.playbackProgressMonitor.setProgress(pd.channels, pd.millis, pd.percent);
					break;
				case ProgressData.TYPE_STATUS:
					this.playbackProgressMonitor.setStatus(pd.status);
					if (pd.status == PlaybackProgressMonitor.PLAY_END) {
						enablePlayControls(false);
					}
					break;
				}
			}
		}

		@Override
		public void setProgress(int channels, long timeMillis, double percent) {
			ProgressData pd = new ProgressData(channels, timeMillis, percent);
			publish(pd);
		}

		@Override
		public void setStatus(int status) {
			ProgressData pd = new ProgressData(status);
			publish(pd);
		}

		public void cancelLoad() {
			this.cancel(false);
			this.playDataServer.cancelPlaybackData();
		}
		
	}
	
	class ProgressData {
		static final int TYPE_STATUS = 0;
		static final int TYPE_PROGRESS = 1;
		public ProgressData(int status) {
			this.type = TYPE_STATUS;
			this.status = status;
		}
		public ProgressData(int channels, long timeMillis, double percent) {
			this.type = TYPE_PROGRESS;
			this.channels = channels;
			this.millis = timeMillis;
			this.percent = percent;
		}
		int type;
		int status;
		int channels;
		long millis;
		double percent;
	}
	
	/**
	 * Stop viewer playback. 
	 */
	public void stopViewerPlayback() {
		PamDataBlock dataBlock = playbackProcess.getParentDataBlock();
		if (dataBlock == null) {
			return;
		}		
		dataBlock.cancelDataOrder();
		
		if (loadWorker != null) {
			loadWorker.cancelLoad();
		}
	}
	
	/**
	 * @return true if there is a valid data source and the options to play 
	 * data are checked. 
	 */
	public boolean hasPlayDataSource() {
		PamDataBlock dataBlock = playbackProcess.getParentDataBlock();
		if (dataBlock == null) {
			return false;
		}
		PamProcess sourceProcess = dataBlock.getSourceProcess();
		if (sourceProcess.getClass() != AcquisitionProcess.class) {
			return false;
		}
		AcquisitionProcess daqProcess = (AcquisitionProcess) sourceProcess;
		OfflineFileServer daqOffline = daqProcess.getAcquisitionControl().getOfflineFileServer();
		if (daqOffline == null) {
			return false;
		}
		return daqOffline.getOfflineFileParameters().enable;
	}
	
	protected long playbackStartMillis, playbackEndMillis;
	protected PlaybackProgressMonitor playbackProgressMonitor;
	protected void setPlaybackProgress(long timeMillis) {
		if (playbackProgressMonitor == null) {
			return;
		}
		double percent = (double) (timeMillis-playbackStartMillis) * 100 / (playbackEndMillis - playbackStartMillis);
		playbackProgressMonitor.setProgress(playbackParameters.channelBitmap, timeMillis, percent);
	}
	
	class PlayLoadObserver implements LoadObserver {

		@Override
		public void setLoadStatus(int loadState) {
//			switch(loadState) {
//			case PamDataBlock.REQUEST_DATA_LOADED:
//				
//			}
			playbackProcess.pamStop();
			if (playbackProgressMonitor != null) {
				playbackProgressMonitor.setStatus(PlaybackProgressMonitor.PLAY_END);
			}
			enablePlayControls(false);
		}
		
	}

}
