/*	PAMGUARD - Passive Acoustic Monitoring GUARDianship.
 * To assist in the Detection Classification and Localisation 
 * of marine mammals (cetaceans).
 *  
 * Copyright (C) 2006 
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package PamController;

import java.awt.Component;
import java.awt.Frame;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import PamModel.PamModuleInfo;
import PamView.ClipboardCopier;
import PamView.PamGui;
import PamView.PamSidePanel;
import PamView.PamTabPanel;
import PamView.PamView;
import PamguardMVC.PamConstants;
import PamguardMVC.PamProcess;

/**
 * @author Doug Gillespie
 * 
 * 
 * <p>
 * PamControlledUnit is an abstract class that is used to contain all the
 * information required for an actual detector, sub detection process or display within
 * Pamguard.
 * <p> 
 * See the <a href="../books/HowToMakePlugins.html">how to make Pamguard plug-ins</a>
 * overview.
 * <p>
 * Each unit will probably have a PamProcess, which will do something
 * like caclulate FFT's, look for whistles, read a sound card, etc. Associated
 * with each process there may or may not be a display or a display component
 * (e.g. a toolbar window for controlling file playback speed, a panel to be
 * added to a GUI, menu's for controlling the algorithm and / or the various
 * bits of the display).
 * <p>
 * Each PamControlledUnit along with one or more PamProcesses and display units
 * comprises a kind of mini MVC, the PamControlledUnit being the Controller, the
 * PamProcesses and their associated data blocks the model and the display
 * comonents the view.
 * <p>
 * Each PamProcess requires a reference to a PamControllerUnit in it's
 * constructor, so PamProcesses can only exist within this framework.
 * <p>
 * Subclasses of PamControlledUnit should handle menu commands associated with
 * any menu's they are responsible for and send those commands on to their
 * processes and other display comonents as necessary.
 * <p>
 * A View in the Pamguard MVC can query for a list of PamControlledUnits, the
 * view can then see which have a display, etc and add them to the view.
 * 
 */
public abstract class PamControlledUnit {

	private PamController pamController;
	/**
	 * List of PamProcesses in this unit
	 */
	private ArrayList<PamProcess> pamProcesses;

	/**
	 * Reference to a PamTabPanel containing informaton on how to set up a
	 * display relevant to the processes in this unit.
	 */
	private PamTabPanel tabPanel;
	
	/**
	 * clipboard copier to go with tabPanel;
	 */
	private ClipboardCopier tabClipCopier;
	
	// protected PamOuterFrame pamOuterFrame;
	/**
	 * Optional additional graphics component to be displayed to the left of the main
	 * tab panel control. 
	 */
	private PamSidePanel sidePanel;

	/**
	 * unitName and unitType are used to identify each PamControlledUnit. \n the
	 * unitType is general for a particular concrete class of PamControlledUnit
	 * such as a click or whistle detector or a fft machine. \n The unitName
	 * should be specific to each instance of the class, e.g. Sperm whale
	 * detector, Beaked Whale detector, etc.
	 */
	private String unitType;

	/**
	 * unitName and unitType are used to identify each PamControlledUnit. \n the
	 * unitType is general for a particular concrete class of PamControlledUnit
	 * such as a click or whistle detector or a fft machine. \n The unitName
	 * should be specific to each instance of the class, e.g. Sperm whale
	 * detector, Beaked Whale detector, etc.
	 */
	private String unitName;

	
	private PamView pamView;
	// private boolean haveSettings;
	
	private PamModuleInfo pamModuleInfo;
	
	protected boolean isViewer;
	/**
	 * Constructor for a new PamControlledUnit
	 * 
	 * @param unitName
	 *            name of unit
	 */
	public PamControlledUnit(String unitType, String unitName) {
		this.unitType = unitType;
		this.unitName = unitName;
		pamProcesses = new ArrayList<PamProcess>();
		isViewer = PamController.getInstance().getRunMode() == PamController.RUN_PAMVIEW;

		if (unitType.length() > PamConstants.MAX_ITEM_NAME_LENGTH) {
			JOptionPane.showMessageDialog(null, "Maximum module type length is " + 
					PamConstants.MAX_ITEM_NAME_LENGTH + " characters");
		}
		if (unitName.length() > PamConstants.MAX_ITEM_NAME_LENGTH) {
			JOptionPane.showMessageDialog(null, "Maximum module name length is " + 
					PamConstants.MAX_ITEM_NAME_LENGTH + " characters");
		}
	}

	/**
	 * @param iP -
	 *            The index of the process
	 * @return Reference to a PamProcess or null if iP is invalid
	 */
	public PamProcess getPamProcess(int iP) {
		if (pamProcesses.size() > iP) {
			return pamProcesses.get(iP);
		}
		return null;
	}

	/**
	 * Gets the total number of PamProcesses controlled by this Controller
	 * 
	 * @return The total number of PamProcesses
	 */
	public int getNumPamProcesses() {
		return pamProcesses.size();
	}

	/**
	 * 
	 * Adds a PamProcess to the ControlledUnit
	 * 
	 * @param pamProcess
	 *            Reference to a PamProcess
	 */
	public void addPamProcess(PamProcess pamProcess) {
		pamProcesses.add(pamProcess);
		PamController.getInstance().notifyModelChanged(PamControllerInterface.ADD_PROCESS);
	}
	
	public boolean removeUnit() {
		// remove the PamControlled unit, deleting all references to it (if they can be found !)
		// first of all, tell the controller that it's not here
		PamController.getInstance().removeControlledUnt(this);
		// tall all PamProcesses that they don't have to do any more
		for (int i = 0; i < pamProcesses.size(); i++) {
			pamProcesses.get(i).destroyProcess();
		}
		PamController.getInstance().notifyModelChanged(PamControllerInterface.REMOVE_CONTROLLEDUNIT);
		return true;
	}
	
	public void rename(String newName) {
		unitName = newName;
		// and tell the displays, etc. 
		if (pamView != null) {
			pamView.renameControlledUnit(this);
		}
		if (getSidePanel() != null) {
			getSidePanel().rename(newName);
		}
		PamController.getInstance().notifyModelChanged(PamControllerInterface.RENAME_CONTROLLED_UNIT);
	}
	/**
	 * Removes a process.
	 * @param pamProcess process to remove
	 */
	public void removePamProcess(PamProcess pamProcess) {
		while (pamProcesses.contains(pamProcess)) {
			pamProcesses.remove(pamProcess);
			PamController.getInstance().notifyModelChanged(PamControllerInterface.REMOVE_PROCESS);
		}
	}

	/**
	 * General notification when the PAMGAURD model changes. 
	 * @param changeType type of change
	 */
	public void notifyModelChanged(int changeType) {
		int nP = getNumPamProcesses();
		for (int i = 0; i < nP; i++) {
			getPamProcess(i).notifyModelChanged(changeType);
		}
	}
	
	/**
	 * Called whenever the array configuration dialog has been called. 
	 */
	public void notifyArrayChanged() {
		
	}

	/**
	 * Gets a reference to a panel to be added to a view
	 * 
	 * @return reference to a PamTabPanel object
	 * @see PamTabPanel
	 * @see PamSidePanel
	 */
	public PamTabPanel getTabPanel() {
		return tabPanel;
	}
	
	private int frameNumber;
	private Component toolbarComponent;
	/**
	 * Get the number of the frame that side and tab panels
	 * for this module should sit on.  
	 * @return frame number. 
	 */
	public int getFrameNumber() {
		return frameNumber;
	}
	
	public void setFrameNumber(int frameNumber) {
		this.frameNumber = frameNumber;
	}
	
	/**
	 * Gets a reference to a small panel to be displayed along the 
	 * left hand edge of the main tab panel. Side panels should be small since 
	 * they are always visible and any space they take will be taken from the main 
	 * tab panel. 
	 * <p>
	 * It is possible for a PamControlled unit to have a side panel without having
	 * a pamTabPanel.
	 *
	 * @return a pamSidePanel object.
	 * @see PamSidePanel
	 * @see PamTabPanel
	 */
	public PamSidePanel getSidePanel() {
		return sidePanel;
	}
	
	public final Component getToolbarComponent() {
		return toolbarComponent;
	}
	
	protected void setToolbarComponent(Component toolbarComponent) {
		this.toolbarComponent = toolbarComponent;
	}

	/**
	 * 
	 * Create a tab specific menu to go with this PamControlledUnit.
	 * <p>
	 * Default is to throw back the standard menu to be used. 
	 * This function should clone the standard menu and then modify
	 * the clone (usually by replacing the Display sub menu)
	 * 
	 * @param standardMenu the standard menu for the Pam GUI.
	 * @return a complete menu bar to be shown while a particular tab is selected
	 */
	public JMenuBar getTabSpecificMenuBar(Frame parentFrame, JMenuBar standardMenu, PamGui pamGui) {
		return standardMenu;
	}

	/**
	 * Create a JMenu object containing MenuItems associated with PamProcesses
	 * 
	 * @param parentFrame
	 *            The owner frame of the menu
	 * @return reference to a JMenu which can be added to an existing menu or
	 *         menu bar
	 *         <p>
	 *         Note that if multiple views are to use the same menu, then they
	 *         should each create a new menu (by setting Create to true) the
	 *         first time they call this method.
	 */
	public JMenuItem createDetectionMenu(Frame parentFrame) {
		return null;
	}

	/**
	 * Create a JMenu object containing MenuItems associated with the view
	 * 
	 * @return reference to a JMenu which can be added to an existing menu or
	 *         menu bar
	 *         <p>
	 *         Note that if multiple views are to use the same menu, then they
	 *         should each create a new menu (by setting Create to true) the
	 *         first time they call this method.
	 */
	public JMenuItem createDisplayMenu(Frame parentFrame) {
		return null;
	}
	
	/**
	 * called for all PamControlledUnits after all units have been created.
	 * This is a good time for the controlled units and processes to find and 
	 * check their source data and the configuration generally since most
	 * onjects (i.e. output data blocks) should be in place
	 *
	 */
	public void setupControlledUnit() {
		for (int i = 0; i < pamProcesses.size(); i++) {
			pamProcesses.get(i).setupProcess();
		}
	}
	
	/**
	 * called just before data acquisition starts. Note that
	 * PamObservers get a call to setSampleRate anyway so this mainly needs
	 * to be used for display elements that may need their scales 
	 * adjusted before startup.
	 *
	 */
	public void pamToStart() {
		
	}
	/**
	 * Called for all controlled units after Pam acquisition has stopped
	 *
	 */
	public void pamHasStopped() {
		
	}
	/**
	 * Called before Pamguard shuts down. Rather than returning false,
	 * this function can be used as a final oportunity to save settings,
	 * write to the database, etc. 
	 * @return true if OK for Pamguard to shut down, false otherwise.
	 */
	public boolean canClose() {
		return true;
	}

	/**
	 * Create a JMenu object containing MenuItems associated with the view
	 * 
	 * @param Create
	 *            causes the creation of a new menu object.
	 * @return reference to a JMenu which can be added to an existing menu or
	 *         menu bar
	 *         <p>
	 *         Note that if multiple views are to use the same menu, then they
	 *         should each create a new menu (by setting Create to true) the
	 *         first time they call this method.
//	 */
//	public JMenu CreateNMEAMenu(boolean Create) {
//		return null;
//	}
	
	/**
	 * Create a JMenu object containing MenuItems associated with the view
	 * 
	 * @param Create
	 *            causes the creation of a new menu object.
	 * @return reference to a JMenu which can be added to an existing menu or
	 *         menu bar
	 *         <p>
	 *         Note that if multiple views are to use the same menu, then they
	 *         should each create a new menu (by setting Create to true) the
	 *         first time they call this method.
//	 */
//	public JMenu CreateAcquisitionMenu(boolean Create) {
//		return null;
//	}

	/**
	 * Returns the name of the unit
	 * 
	 * @return the name of the unit
	 */
	public String getUnitName() {
		return unitName;
	}

	public String getUnitType() {
		return unitType;
	}

	/*
	 * Create a menu for a specific tab <p>
	 * This is intended to generate a menuitem for a different
	 * tab. To generate a manu specific to this tab, use CreateTabSpecificMenu
	 */
	public void addOtherRelatedMenuItems(Frame parentFrame, JMenu menu, String name) {
		
	}
	
	public int addRelatedMenuItems(Frame parentFrame, JMenu menu, String name) {
		int count = 0;
		// go through all PamControlled units and see if any of them have menu items
		// with this name and add them to the menu
		int nUnits = PamController.getInstance().getNumControlledUnits();
		PamControlledUnit aUnit;
		for (int i = 0; i < nUnits; i++) {
			aUnit = PamController.getInstance().getControlledUnit(i);
			aUnit.addOtherRelatedMenuItems(parentFrame, menu, name);
		}
		
		return count;
	}
	
	public boolean gotoTab() {
		if (tabPanel == null) return false;
		if (pamView != null) pamView.showControlledUnit(this);
		return true;
	}

	public PamView getPamView() {
		return pamView;
	}

	/**
	 * Called whenever the frme of a PamControlledunit changes (including
	 * at program startup). 
	 * @param pamView
	 */
	public void setPamView(PamView pamView) {
		this.pamView = pamView;
	}

	public JMenuItem createHelpMenu(Frame parentFrame) {
		// TODO Auto-generated method stub
		return null;
	}
	

	/**
	 * @param parentFrame parent frame for the menu
	 * @return the file menu item
	 */
	public JMenuItem createFileMenu(JFrame parentFrame) {
		return null;
	}

	/**
	 * Sets the side panel for the PamControlledUnit
	 * Side panels are shown down the left hand side of 
	 * the main Pamguard GUI and are always visible, irrespective
	 * of which tab is being viewed on the main tabbed display. 
	 * <p>
	 * Side panels are generally used to display summary information
	 * for the PamControlledUnit or to provide quick access controls. 
	 * @param sidePanel Reference to a PamSidePanel object
	 * @see PamSidePanel
	 */
	public void setSidePanel(PamSidePanel sidePanel) {
		this.sidePanel = sidePanel;
	}

	/**
	 * Sets the tab panel for the PamControlledUnit. 
	 * A tab panel may contain graphics or tables to display 
	 * information of any type.
	 * @param tabPanel
	 */
	public void setTabPanel(PamTabPanel tabPanel) {
		this.tabPanel = tabPanel;
		tabClipCopier = new ClipboardCopier(tabPanel.getPanel());
	}

	/**
	 * @return Returns the pamModuleInfo.
	 */
	public PamModuleInfo getPamModuleInfo() {
		return pamModuleInfo;
	}

	/**
	 * @param pamModuleInfo The pamModuleInfo to set.
	 */
	public void setPamModuleInfo(PamModuleInfo pamModuleInfo) {
		this.pamModuleInfo = pamModuleInfo;
	}

	public ClipboardCopier getTabClipCopier() {
		if (tabClipCopier == null && getTabPanel() != null) {
			tabClipCopier = new ClipboardCopier(getTabPanel().getPanel());
		}
		return tabClipCopier;
	}

	/**
	 * @return the pamController
	 */
	public PamController getPamController() {
		if (pamController == null) {
			pamController = PamController.getInstance();
		}
		return pamController;
	}

	/**
	 * @param pamController the pamController to set
	 */
	public void setPamController(PamController pamController) {
		this.pamController = pamController;
	}

	@Override
	public String toString() {
		return getUnitType() + " " + getUnitName();
	}

	/**
	 * Save data (to binary files and to database)
	 * in viewer mode. 
	 * <p>
	 * This gets called automatically on system exit and can 
	 * also be called from the file menu. 
	 */
	public void saveViewerData() {
		int n = getNumPamProcesses();
		for (int i = 0; i < n; i++) {
			getPamProcess(i).saveViewerData();
		}
	}
	
	/**
	 * Fill an XML element with data about this module. 
	 * <p> this is expected to only include parameters which 
	 * are required for real time data collection on remote platforms
	 * using a stand alone, no GUI pamguard version. 
	 * @param doc 
	 * @param moduleData XML element to fill. 
	 * @return false if this module has no data of interest, otherwise
	 * just add data to the element. 
	 */
	public final boolean fillXMLElement(Document doc, Element moduleData) {
		moduleData.setAttribute("Java.class", this.getClass().getName());
		moduleData.setAttribute("UnitType", unitType);
		moduleData.setAttribute("UnitName", unitName);

		Element paramsEl = doc.createElement("MODULEPARAMETERS");
		if (fillXMLParameters(doc, paramsEl)) {
			moduleData.appendChild(paramsEl);
		}
		
		int nP = getNumPamProcesses();
		Element processData;
		for (int i = 0; i < nP; i++) {
			processData = doc.createElement("PROCESS");
			getPamProcess(i).fillXMLElement(doc, processData, i);
			moduleData.appendChild(processData);
		}
		
		
		return true;
	}
	/**
	 * Add Module specific XML data.
	 * <p>this will generally be the place where most modules
	 * set their xml settings data.  
	 * @param doc
	 * @param paramsEl
	 * @return
	 */
	protected boolean fillXMLParameters(Document doc, Element paramsEl) {
		return false;
	}


	/**
	 * Go into a wait state while any buffers in output data blocks flush through 
	 * in multithread mode. 
	 * <p>
	 * this has been placed in each controlled unit so that it can be overriedden if
	 * if necessary. 
	 * @param maxWait max wait time in seconds
	 * @return true if all flushed, false if the wait tiem was exceeded. 
	 */
	public boolean flushDataBlockBuffers(long maxWait) {
		int errors = 0;
		for (int i = 0; i < getNumPamProcesses(); i++) {
			if (!getPamProcess(i).flushDataBlockBuffers(maxWait)) {
				errors++;
			}
		}
		return (errors == 0);
	}
	
	/**
	 * 
	 * @return true if this module can play sound in reponse to a play
	 * command from the top toolbar. 
	 */
	public boolean canPlayViewerSound() {
		return false;
	}
	
	/**
	 * Start playing sound in reponse to a call from the viewer top toolbar. 
	 */
	public void playViewerSound() {
		
	}
	/**
	 * Stop playing sound in response to a call from the viewer top toolbar. 
	 */
	public void stopViewerSound() {
		
	}
}
