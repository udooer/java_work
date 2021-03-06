package PamController;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import PamUtils.PamFileFilter;
import PamView.PamDialog;
import PamView.PamGridBagContraints;

public class SettingsFileDialog extends PamDialog {

	private static SettingsFileDialog singleInstance; 
	
	private SettingsFileData settingsFileData;
	
	JComboBox fileList;
	JButton browseButton, newButton;
	JCheckBox alwaysShow;
	
	private SettingsFileDialog(Frame parentFrame, String title, boolean hasDefault) {
		super(parentFrame, title, hasDefault);
		JPanel p = new JPanel();
		p.setLayout(new BorderLayout());
		p.setBorder(new TitledBorder("Recent configuration files"));
		p.add(BorderLayout.CENTER, fileList = new JComboBox());
		Dimension d = fileList.getPreferredSize();
		d.width = 400;
		fileList.setPreferredSize(d);
//		fileList.

		JPanel q = new JPanel();
		q.setLayout(new BorderLayout());
		q.setBorder(new TitledBorder("Options"));
		q.add(BorderLayout.CENTER, alwaysShow = new JCheckBox("Always show at start up"));
		JPanel e = new JPanel();
		e.setLayout(new GridBagLayout());
		GridBagConstraints c = new PamGridBagContraints();
		addComponent(e, browseButton = new JButton("Browse"), c);
		c.gridx++;
//		addComponent(e, newButton = new JButton("New ..."), c);
		q.add(BorderLayout.EAST, e);
		browseButton.addActionListener(new BrowseButton());
//		newButton.addActionListener(new NewButton());
		
		JPanel pan = new JPanel();
		pan.setLayout(new BorderLayout());
		pan.add(BorderLayout.CENTER, p);
		pan.add(BorderLayout.SOUTH, q);
		
		setDialogComponent(pan);
	}
	
	public static SettingsFileData showDialog(Frame frame, SettingsFileData settingsFileData) {
		if (singleInstance == null || singleInstance.getOwner() != frame) {
			singleInstance = new SettingsFileDialog(frame, "Load PAMGUARD configuration from ...", false);
		}
		singleInstance.settingsFileData = settingsFileData;
		singleInstance.setParams();
		singleInstance.setVisible(true);
		return singleInstance.settingsFileData;
	}

	
	private class BrowseButton implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == browseButton) {
				browseForFiles();
			}
		}
	}
	
	private void browseForFiles() {

		JFileChooser jFileChooser = new JFileChooser(settingsFileData.getFirstFile());
		jFileChooser.setApproveButtonText("Select");
//		jFileChooser.set
		jFileChooser.addChoosableFileFilter(new PamFileFilter("PAMGUARD Settings files", PamSettingManager.fileEnd));
		int state = jFileChooser.showOpenDialog(this);
		if (state != JFileChooser.APPROVE_OPTION) return;
		File newFile = jFileChooser.getSelectedFile();
		if (newFile == null) return;
		newFile = PamFileFilter.checkFileEnd(newFile, PamSettingManager.fileEnd, true);
		settingsFileData.recentFiles.add(0, newFile);
		fillFileList();
		// shouldn't need this line, but it seems to make all the difference in updating
		// the list
//		fileList.setSelectedItem(newFile.getName());
		fileList.setSelectedIndex(0);
	}
	private class NewButton implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			if (e.getSource() == browseButton) {
				newConfig();
			}
		}
	}
	
	private void newConfig() {
		JFileChooser jFileChooser = new JFileChooser(settingsFileData.getFirstFile());
		jFileChooser.setApproveButtonText("Select");
//		jFileChooser.set
		jFileChooser.addChoosableFileFilter(new PamFileFilter("PAMGUARD Settings files", PamSettingManager.fileEnd));
//		jFileChooser.set
		int state = jFileChooser.showOpenDialog(this);
		if (state != JFileChooser.APPROVE_OPTION) return;
		File newFile = jFileChooser.getSelectedFile();
		if (newFile == null) return;
		newFile = PamFileFilter.checkFileEnd(newFile, PamSettingManager.fileEnd, true);
		settingsFileData.recentFiles.add(0, newFile);
		fillFileList();
		// shouldn't need this line, but it seems to make all the difference in updating
		// the list
//		fileList.setSelectedItem(newFile.getName());
		fileList.setSelectedIndex(0);
	}

	@Override
	public void cancelButtonPressed() {
		settingsFileData = null;
	}

	private void setParams() {
		fillFileList();
		if (settingsFileData != null) {
			alwaysShow.setSelected(settingsFileData.showFileList);
		}
	}
	private void fillFileList() {
		fileList.removeAllItems();
		if (settingsFileData != null) {
			for (int i = 0; i < settingsFileData.recentFiles.size(); i++){
				fileList.addItem(settingsFileData.recentFiles.get(i).getName());
			}
			fileList.setSelectedIndex(0);
		}
	}
	@Override
	public boolean getParams() {

//		System.out.println("Sel index = " + fileList.getSelectedIndex());
		File selFile = settingsFileData.recentFiles.get(fileList.getSelectedIndex());
		settingsFileData.setFirstFile(selFile);
		settingsFileData.showFileList = alwaysShow.isSelected();
		
		return true;
	}

	@Override
	public void restoreDefaultSettings() {
		// TODO Auto-generated method stub
		
	}

}
