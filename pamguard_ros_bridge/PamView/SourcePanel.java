package PamView;

import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;


import PamController.PamController;
import PamUtils.PamUtils;
import PamguardMVC.PamConstants;
import PamguardMVC.PamDataBlock;

/**
 * Standard panel for dialogs that shows a list of
 * available data sources and, optionally a list of data channels.
 * <p>
 * This is for general use within other dialog panels. 
 *  
 * @author Doug Gillespie
 *
 */
public class SourcePanel implements ActionListener{

	protected JPanel panel;
	protected Class sourceType;
	protected boolean hasChannels;
	protected String borderTitle;
	protected boolean includeSubClasses;
	
	protected JComboBox sourceList;
	protected JCheckBox channelBoxes[];
	
	protected Window ownerWindow;
	
	/**
	 * Flags to specify the minimum localisation data requirements from a data block. 
	 * N.B. Just because a data block says it data MAY have particular localisation information,
	 * that is no guarantee that those information are there within individual data units. 
	 */
	private int localisationRequirements;
	
	protected ArrayList<PamDataBlock> excludedBlocks = new ArrayList<PamDataBlock>();
	
	private ArrayList<SourcePanelMonitor> spMonitors = new ArrayList<SourcePanelMonitor>();
	
	/**
	 * Construct a panel with a titles border
	 * @param borderTitle Title to go in border
	 * @param sourceType Data Source type
	 * @param hasChannels Include a set of checkboxes to list available channels
	 * @param includeSubClasses inlcude all subclasses of sourceType in the list. 
	 */
	public SourcePanel(Window ownerWindow, String borderTitle, Class sourceType, boolean hasChannels, boolean includeSubClasses) {
		this.ownerWindow = ownerWindow;
		this.sourceType = sourceType;
		this.hasChannels = hasChannels;
		this.borderTitle = borderTitle;
		this.includeSubClasses = includeSubClasses;
		createPanel();
		setSourceList();
	}

	/**
	 * Construct a panel without a border
	 * @param ownerWindow Parent window
	 * @param sourceType Data Source type
	 * @param hasChannels Include a set of checkboxes to list available channels
	 * @param include subclasses of the sourceType
	 */
	public SourcePanel(Window ownerWindow, Class sourceType, boolean hasChannels, boolean includeSubClasses) {
		this.ownerWindow = ownerWindow;
		this.sourceType = sourceType;
		this.hasChannels = hasChannels;
		this.includeSubClasses = includeSubClasses;
		createPanel();
		setSourceList();
	}
	
	class SelectionListener implements ActionListener {
		
		private int channel;
		
		public SelectionListener(int channel) {
			super();
			this.channel = channel;
		}
		
		public void actionPerformed(ActionEvent e) {
			selectionChanged(channel);
		}
		
	}
	
	public void setEnabled(boolean enabled) {
		sourceList.setEnabled(enabled);
	}
	
	/**
	 * Add a listener to the data source drop down list
	 * @param listener listener 
	 */
	public void addSelectionListener(ActionListener listener) {
		sourceList.addActionListener(listener);
	}
	
	protected void selectionChanged(int channel) {
		notifySourcePanelMonitors();
	}
	
	protected void createPanel() {
		panel = new JPanel();
		// add stuff to the panel.
		if (borderTitle != null) {
			panel.setBorder(new TitledBorder(borderTitle));
		}
		panel.setLayout(new BorderLayout());
//		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.add(BorderLayout.NORTH, sourceList = new JComboBox());
		sourceList.addActionListener(this);
		if (hasChannels) {
			JPanel channelPanel = new JPanel();
			channelPanel.setLayout(new BoxLayout(channelPanel, BoxLayout.Y_AXIS));
			panel.add(new JLabel("Channel list ..."));
			channelBoxes = new JCheckBox[PamConstants.MAX_CHANNELS];
			for (int i = 0; i < PamConstants.MAX_CHANNELS; i++){
				channelBoxes[i] = new JCheckBox("Channel " + i);
				channelPanel.add(channelBoxes[i]);
				channelBoxes[i].setVisible(false);
				channelBoxes[i].addActionListener(new SelectionListener(i));
				//System.out.println("SourcePanel.java creatPanel"+i);
			}
			panel.add(BorderLayout.CENTER, channelPanel);
		}
	}
	
	/**
	 * action listener to update channel list when a 
	 * a different source is selected
	 */
	public void actionPerformed(ActionEvent e) {
		sourceChanged();
	}
	
	protected void sourceChanged() {
		showChannels();
	}
	
	protected void showChannels() {
		// called when the selection changes - set visibility of the channel list
		if (channelBoxes == null) return;
		int channels = 0;
		PamDataBlock sb = getSource();
		if (sb != null) {
			channels = sb.getChannelMap();
		}
		for (int i = 0; i < Math.min(PamConstants.MAX_CHANNELS, channelBoxes.length); i++) {
			channelBoxes[i].setVisible((channels & 1<<i) != 0); 
		}
		rePackOwner(channels);
	}
	
	private int currentNShown = 0;
	/**
	 * Repack the owner window if the number of channels has changed
	 * @param channelsMap bitmap of used channels. 
	 */
	protected void rePackOwner(int channelsMap) {
		if (currentNShown != PamUtils.getNumChannels(channelsMap)) {
			if (ownerWindow != null) {
				try {
					ownerWindow.pack();
				}
				catch (Exception e) {
					System.out.println("Problems packing SourcePanel owner window");
				}
			}
			currentNShown = PamUtils.getNumChannels(channelsMap);
		}
	}
	
	/**
	 * Set the current data source using the data source name
	 * @param sourceName
	 * @return true if OK
	 */
	public boolean setSource(String sourceName) {
		// search the list for the string
		for (int i = 0; i < sourceList.getItemCount(); i++){
			if (sourceList.getItemAt(i).toString().equals(sourceName)) {
				sourceList.setSelectedIndex(i); 
				//System.out.println("SourcePanel.java :: setSource 1");
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Set the current data source by block reference
	 * @param sourceBlock
	 */
	synchronized public void setSource(PamDataBlock sourceBlock) {
		
		/*
		 * Several dialogs are not calling setSourceList each time
		 * they open. Although some are, I've decided to call this here anyway
		 * since calling it twice is better than not at all.
		 */
		setSourceList();
		/*
		 * The direct way of doing this seems to have stopped working - very worrying !
		 */
		sourceList.setSelectedItem(sourceBlock);
		//System.out.println("SourcePanel.java :: setSource 2");
//		sourceList.setSelectedIndex(2);
	}
	
	/**
	 * Fill the list of available data sources, taking into account 
	 * the list of excluded sources
	 * @return sets a list of available sources. Returns false if no sources are available. 
	 */
	synchronized public boolean setSourceList() {
		return setSourceList(false);
	}
	synchronized public boolean setSourceList(boolean replaceChosen) {
	
		Object selectedItem = null;
		if (replaceChosen) {
			selectedItem = sourceList.getSelectedItem();
		}
		
		sourceList.removeAllItems();
		ArrayList<PamDataBlock> sl = PamController.getInstance().getDataBlocks(sourceType, includeSubClasses);
		if (sl == null || sl.size() <= 0) return false;
		
		int availableLocData;
		
		for (int i = 0; i < sl.size(); i++) {
			
			if (excludedBlocks.contains(sl.get(i))) continue;
			
			availableLocData = sl.get(i).getLocalisationContents();
			
			if ((localisationRequirements & availableLocData) < localisationRequirements) continue;
			
			sourceList.addItem(sl.get(i));
			
		}
		if (replaceChosen && selectedItem != null) {
			sourceList.setSelectedItem(selectedItem);
		}
		if (ownerWindow != null) {
			try {
				ownerWindow.pack();
			}
			catch (Exception e) {
				System.out.println("Problems packing SourcePanel owner window");
			}
		}
		return true;
	}
	
	/**
	 * return the selected data source
	 * @return source data block
	 */
	public PamDataBlock getSource() {
		return (PamDataBlock) sourceList.getSelectedItem();
	}

	/**
	 * Get a list of selected channels
	 * @return bitmap of selected channels
	 */
	public int getChannelList() {
		PamDataBlock sb = getSource();
		if (sb == null) return 0;
		// if channels are not selectable, then return the default map ...
		int sourceChannels = sb.getChannelMap();
		if (hasChannels == false) return sourceChannels;
		int channelList = 0;
		for (int i = 0; i < PamConstants.MAX_CHANNELS; i++) {
			if ((sourceChannels & (1<<i)) != 0 && channelBoxes[i].isSelected()) {
				channelList += (1<<i);
				//System.out.println("1. SourcePanel getchannelList " +i+":"+ channelList);
			}
		}
		return channelList;
	}
	
	/**
	 * Set the current channel selection
	 * @param channelList bitmap of currently selected channels
	 */
	public void setChannelList(int channelList) {
		for (int i = 0; i < PamConstants.MAX_CHANNELS; i++) {
			//channelBoxes[i].setSelected((channelList & (1<<i)) > 0);  //Xiao Yan Deng commented
			//System.out.println("SourcePanel setchannelList " + i +":"+ channelList);
			channelBoxes[i].setSelected((channelList & (1<<i)) > 0);  //Xiao Yan Deng
		}
	}
	
	/**
	 * get the data source type for this SourcePanel
	 * @return data type of a data source
	 */
	public Class getSourceType() {
		return sourceType;
	}
	
	/**
	 * Set the source type for this SourcePanel
	 * @param sourceType
	 */ 
	public void setSourceType(Class sourceType) {
		this.sourceType = sourceType;
		setSourceList();
	}
	
	/**
	 * Get the currently selected source index
	 * @return source index within the source list
	 */
	public int getSourceIndex() {
		return sourceList.getSelectedIndex();
	}
	
	/**
	 * Get a reference to the JPanel containing the controls
	 * @return JPanel container
	 */
	public JPanel getPanel() {
		return panel;
	}
	
	/**
	 * Exclude specific data blocks from the source list. e.g. a process would normally not
	 * be able to use it's own output data block as an input source and should therefore
	 * exclude it from the list.
	 * @param block PamDataBlock to exlcude
	 * @param exclude true - excluse; false - allow
	 */
	public void excludeDataBlock(PamDataBlock block, boolean exclude) {
		if (exclude) {
			if (excludedBlocks.contains(block) == false) {
				excludedBlocks.add(block);
			}
		}
		else {
			excludedBlocks.remove(block);
		}
		setSourceList(true);
	}
	
	/**
	 * Clear the list of excluded data blocks. 
	 *
	 */
	public void clearExcludeList() {
		excludedBlocks.clear();
		setSourceList(true);
	}

	public JCheckBox[] getChannelBoxes() {
		return channelBoxes;
	}

	public int getLocalisationRequirements() {
		return localisationRequirements;
	}

	public void setLocalisationRequirements(int localisationRequirements) {
		this.localisationRequirements = localisationRequirements;
		setSourceList();
	}
	
	/**
	 * Get the number of items in the list. 
	 * @return number of sources of this type of data
	 */
	public int getSourceCount() {
		return sourceList.getItemCount();
	}

	public void addSourcePanelMonitor(SourcePanelMonitor gspm) {
		spMonitors.add(gspm);
	}
	public void removeSourcePanelMonitor(SourcePanelMonitor gspm) {
		spMonitors.remove(gspm);
	}
	private void notifySourcePanelMonitors() {
		for (int i = 0; i < spMonitors.size(); i++) {
			spMonitors.get(i).channelSelectionChanged();
		}
	}

	/**
	 * Set a tooltip text for the source panel. 
	 * @param toolTip Tooltip text. 
	 */
	public void setSourceToolTip(String toolTip) {
		sourceList.setToolTipText(toolTip);
	}
}
