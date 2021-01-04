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

import generalDatabase.DBControl;
import generalDatabase.DBControlSettings;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

//import sun.jdbc.odbc.OdbcDef;
import tipOfTheDay.TipOfTheDayManager;
//import javax.swing.filechooser.FileFilter;
//import javax.swing.filechooser.FileNameExtensionFilter;

import PamUtils.PamCalendar;
import PamUtils.PamFileFilter;
import PamUtils.Splash;

//import PamUtils.PamFileFilter;


/**
 * @author Doug Gillespie
 * 
 * Singleton class for managing Pam settings - where and how they are stored in
 * a persistent way between runs.
 * 
 * Any class that wants is settings saved should register with the
 * PamSettingsManager.
 * <p>
 * When the GUI closes, SaveSettings is called, SaveSettings goes through the
 * list of registered objects and asks each one to give it a reference to an
 * Object containing the settings (this MUST implement serialisable). This can
 * be the object itself, but will more likely be a reference to another object
 * just containing settings parameters. The class implementing PamSettings must
 * also provide functions getUnitType, getUnitName and getSettingsVersion. These
 * four pieces of information are then bundled into a PamControlledUnitSettings
 * which is added to an array list which is then stored in a serialised file.
 * <p>
 * When PAMGUARD starts, after all detectors have been created, the serialised
 * file is reopened. Each PamControlledUnitSettings is taken in turn and
 * compared with the list of registered objects to find one with the same name,
 * type and settings version. Once one is found, it is given the reference to
 * the settings data which t is responsible for casting into whatever class it
 * requires.
 * 
 * 
 */
public class PamSettingManager {

	private static PamSettingManager pamSettingManager;

	/**
	 * List of modules that have / want PAMGUARD Settings
	 * which get stored in the psf file and / or the database store. 
	 */
	private ArrayList<PamSettings> owners;
	
	/**
	 * List of modules that specifically use settings from the database
	 * storage. 
	 */
	private ArrayList<PamSettings> databaseOwners;

	/**
	 * List of settings used by 'normal' modules. 
	 */
	ArrayList<PamControlledUnitSettings> initialSettingsList;

	/**
	 * List of settings used specifically by databases. 
	 * This list never get's stored anywhere, but is just held
	 * in memory so that the database identified at startup in 
	 * viewer and mixed modes gets reloaded later on . 
	 */
	ArrayList<PamControlledUnitSettings> databaseSettingsList;
	
//	static public final String oldFileEnd = "PamSettingsFiles.ser";
	
	static public final String fileEnd = "psf";
	
	/**
	 * Name of the file that contains a list of recent psf files. 
	 */
	transient public String settingsListFile = "PamSettingsFiles.psg";
	
	/**
	 * Name of a list of recent database informations (probably just the last one)
	 */
	transient public String databaseListFile = "recentDatabase.psg";
	
	/**
	 * Identifier for modules that go in the 'normal' list
	 * (everything apart from database modules)
	 */
	public static final int LIST_UNITS = 0x1;
	/**
	 * Identifier for modules which are part of the database system. 
	 */
	public static final int LIST_DATABASESTUFF = 0x2;
	
	/**
	 * Save settings to a psf file
	 */
	static private final int SAVE_PSF = 0x1;
	/**
	 * Save settings to database tables (if available). 
	 */
	static private final int SAVE_DATABASE = 0x2;
	
	/**
	 * running in remote mode, default normal
	 */
	static public boolean RUN_REMOTE = false;
	static public String  remote_psf = "NULL";
	static public String  external_wav = "NULL";
	
	private boolean loadingLocalSettings;
	
//	File currentFile; // always use firstfile from the settingsFileData
	
	private ArrayList<File> recentFiles;
	
	private boolean[] settingsUsed;
	private boolean userNotifiedAbsentSettingsFile = false;
	private boolean userNotifiedAbsentDefaultSettingsFile = false;
	
	private boolean programStart = true;
	
	SettingsFileData settingsFileData;

	private PamSettingManager() {
		owners = new ArrayList<PamSettings>();
		databaseOwners = new ArrayList<PamSettings>();
//		setCurrentFile(new File(defaultFile));
	}

	public static PamSettingManager getInstance() {
		if (pamSettingManager == null) {
			pamSettingManager = new PamSettingManager();
		}
		return pamSettingManager;
	}
	
	/**
	 * Clear all settings from the manager
	 */
	public void reset() {
		initialSettingsList = null;
		databaseSettingsList = null;
		owners = new ArrayList<PamSettings>();
		databaseOwners = new ArrayList<PamSettings>();
		
	}

	/*
	 * Flag to indicate that initialisation of PAMGUARD has completed.
	 */
	private boolean initializationComplete = false;
	
	/**
	 * Called everytime anything in the model changes. 
	 * @param changeType type of change
	 */
	public void notifyModelChanged(int changeType) {
		if (changeType == PamControllerInterface.INITIALIZATION_COMPLETE) {
			initializationComplete = true;
		}
	}

	/**
	 * Register a PAMGAURD module that wants to store settings in a 
	 * serialised file (.psf file) and / or have those settings stored
	 * in the database settings table. 
	 * <p>Normally, all modules will 
	 * call this for at least one set of settings. Often the PamSettings
	 * is implemented by the class that extends PamControlledunit, but 
	 * it's also possible to have multiple sub modules, processes or displays
	 * implemnt PamSettings so that different settings for different bits of 
	 * a PamControlledUnit are stored separately.
	 * @see PamSettings
	 * @see PamControlledUnit
	 * @param pamUnit Reference to a PamSettings module
	 */
	public boolean registerSettings(PamSettings pamUnit) {
		return registerSettings(pamUnit, LIST_UNITS);
	}
	
	/**
	 * Register modules that have settings information that 
	 * should be stored in serialised form in 
	 * psf files and database Pamguard_Settings tables. 
	 * @param pamUnit Unit containing the settings
	 * @param whichLists which lists to store the settings in. <p>
	 * N.B. These are internal lists and not the external storage. Basically 
	 * any database modules connected with settings should to in LIST_DATABASESTUFF
	 * everything else (including the normal database) should go to LISTS_UNITS 
	 * @return true if settings registered sucessfully. 
	 */
	public boolean registerSettings(PamSettings pamUnit, int whichLists) {
		
		if ((whichLists & LIST_UNITS) != 0) {
			owners.add(pamUnit);
		}
		if ((whichLists & LIST_DATABASESTUFF) != 0) {
			databaseOwners.add(pamUnit);
		}
		
		PamControlledUnitSettings settings = findSettings(pamUnit, whichLists);
		if (settings != null && settings.getSettings() != null) {
			return pamUnit.restoreSettings(settings);
		}
		return false;
	}
	
	/**
	 * Find settings for a particular user in one or more lists. 
	 * @param user PamSettings user. 
	 * @param whichLists lists to search 
	 * @return settings object. 
	 */
	private PamControlledUnitSettings findSettings(PamSettings user, int whichLists) {
		PamControlledUnitSettings settings = null;
		
		if ((whichLists & LIST_UNITS) != 0) {
			settings = findSettings(initialSettingsList, settingsUsed, user);
		}
		
		if (settings == null && (whichLists & LIST_DATABASESTUFF) != 0) {
			settings = findGeneralSettings(user.getUnitType());
		}
		
		return settings;
	}
	/**
	 * Find settings in a list of settings, ignoring settings which have
	 * already been used by a module. 
	 * @param settingsList settings list
	 * @param usedSettings list of settings that have already been used. 
	 * @param user module that uses the settings. 
	 * @return Settings object. 
	 */
	private PamControlledUnitSettings findSettings(ArrayList<PamControlledUnitSettings> settingsList, 
			boolean[] usedSettings,	PamSettings user) {
		if (settingsList == null) return null;
		// go through the list and see if any match this module. Avoid repeats.
		for (int i = 0; i < settingsList.size(); i++) {
			if (usedSettings != null && usedSettings[i]) continue; 
			if (isSettingsUnit(user, settingsList.get(i))) {
				if (usedSettings != null) {
					usedSettings[i] = true;
				}
				return settingsList.get(i);
			}
		}
		return null;
	}
	
	/**
	 * Searches a list of settings for settings with a
	 * specific type. 
	 * @param unitType
	 * @return PamControlledUnitSettings or null if none found
	 * @see PamControlledUnitSettings
	 */
	public PamControlledUnitSettings findGeneralSettings(String unitType) {
		if (databaseSettingsList == null) {
			return null;
		}
		for (int i = 0; i < databaseSettingsList.size(); i++) {
			if (databaseSettingsList.get(i).getUnitType().equalsIgnoreCase(unitType)) {
				return databaseSettingsList.get(i);
			}
		}
		return null;
	}
	
	/**
	 * Find settings in a list of settings by name and by type. 
	 * @param settingsList settings list to search 
	 * @param unitType unit name
	 * @param unitName unit type
	 * @return settings object
	 */
	public PamControlledUnitSettings findSettings(ArrayList<PamControlledUnitSettings> settingsList,
			String unitType, String unitName) {
		
		if (settingsList == null) {
			return null;
		}
		PamControlledUnitSettings aSet;
		
		
		for (int i = 0; i < settingsList.size(); i++) {
			aSet = settingsList.get(i);
			if (aSet.getUnitType().equals(unitType) & (unitName == null | aSet.getUnitName().equals(unitName))) {
				return aSet;
			}
		}
		
		return null;
	}

	/**
	 * Call just before PAMGUARD exits to save the settings
	 * either to psf and / or database tables. 
	 * @return true if settings saved sucessfully. 
	 */
	public boolean saveFinalSettings() {
		int runMode = PamController.getInstance().getRunMode();
		switch (runMode) {
		case PamController.RUN_NORMAL:
			return saveSettings(SAVE_PSF | SAVE_DATABASE);
		case PamController.RUN_PAMVIEW:
			return saveSettings(SAVE_DATABASE);
		case PamController.RUN_MIXEDMODE:
			return saveSettings(SAVE_DATABASE);
		case PamController.RUN_NOTHING:
			return saveSettings(SAVE_PSF);
		}
		return false;
	}

	/**
	 * Save settings to a psf file and / or the database tables. 
	 * @param saveWhere
	 * @return true if sucessful 
	 */
	public boolean saveSettings(int saveWhere) {
		
		if (initializationComplete == false) {
			// if PAMGAURD hasn't finished loading, then don't save the settings
			// or the file will get wrecked (bug tracker 2269579)
			System.out.println("Settings have not yet loaded. Dontt save file");
			return false;
		}

//		saveSettingToDatabase();
		
		if ((saveWhere & SAVE_PSF) != 0) {
			saveSettingsToFile();
			saveSettingsFileData();
		}

		if ((saveWhere & SAVE_DATABASE) != 0) {
			saveSettingsToDatabase();
			saveDatabaseFileData();
		}
		
		
		return true;

	}

	/**
	 * Save settings to the default (most recently used) psf file. 
	 * @return true if successful. 
	 */
	public boolean saveSettingsToFile() {

		/*
		 * Create a new list of settings in case they have changed
		 */

		ArrayList<PamControlledUnitSettings> pamSettingsList;
		pamSettingsList = new ArrayList<PamControlledUnitSettings>();
		for (int i = 0; i < owners.size(); i++) {
			pamSettingsList
			.add(new PamControlledUnitSettings(owners.get(i)
					.getUnitType(), owners.get(i).getUnitName(), 
					owners.get(i).getSettingsVersion(), 
					owners.get(i).getSettingsReference()));
		}
		int nUsed = pamSettingsList.size();
		/*
		 * Then go through the initialSettings, that were read in and any that were not used
		 * add to the current settings output so that they may be used next time around incase
		 * a module reappears that was temporarily not used.
		 */
		if (initialSettingsList != null) {
			for (int i = 0; i < initialSettingsList.size(); i++) {
				if (settingsUsed != null && settingsUsed.length > i && settingsUsed[i]) continue;
				pamSettingsList.add(initialSettingsList.get(i));
			}
		}
		/*
		 * then save it to a single serialized file
		 */
		ObjectOutputStream file = openOutputFile();
		if (file == null)
			return false;
		try {
			for (int i = 0; i < pamSettingsList.size(); i++){
				file.writeObject(pamSettingsList.get(i));
			}
			file.close();
		} catch (Exception Ex) {
			Ex.printStackTrace();
			return false;
		}
		
//		try { // experimenting with xml output. 
//			FileOutputStream fos = new FileOutputStream("pamguard.xml");
//			XMLEncoder xe = new XMLEncoder(fos);
//			for (int i = 0; i < nUsed; i++) {
//				xe.writeObject(pamSettingsList.get(i).getUnitName());
//			}
//			xe.flush();
//			xe.close();
//			fos.close();
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		// and save the settings file list to that's file
		return true;

	}

	/**
	 * Load the PAMGAURD settings either from psf file or from 
	 * a database, depending on the run mode and type of settings required. 
	 * @param runMode
	 * @return OK if load was successful. 
	 */
	public boolean loadPAMSettings(int runMode) {
		boolean ans = false;
		switch(runMode) {
		case PamController.RUN_NORMAL:
			ans = loadNormalSettings();
			break;
		case PamController.RUN_PAMVIEW:
			ans = loadViewerSettings();
			break;
		case PamController.RUN_MIXEDMODE:
			ans = loadMixedModeSettings();
			break;
		case PamController.RUN_REMOTE:
			PamSettingManager.RUN_REMOTE = true;
			ans = loadNormalSettings();
			break;
		case PamController.RUN_NOTHING:
			ans = loadNormalSettings();
			break;
		default:
			return false;	
		}
		if (ans) {
			initialiseRegisteredModules();
		}
		return ans;
	}
	
	/**
	 * Load settings perfectly 'normally' from a psf file. 
	 * @return OK whether or not any settings were loaded. 
	 */
	private boolean loadNormalSettings() {
		loadPSFSettings();
		return true;
	}
	
	/**
	 * Load settings for viewer mode. These must come from 
	 * an old PAMGUARD database containing settings information. 
	 * @return true if settings loaded sucessfully. 
	 */
	private boolean loadViewerSettings() {
		return loadDBSettings();
	}
	
	/**
	 * Load settings for mixed mode. These must come from 
	 * an old PAMGUARD database containing settings information. 
	 * @return true if settings loaded sucessfully. 
	 */
	private boolean loadMixedModeSettings() {
		return loadDBSettings();
	}
	
	/**
	 * Some modules may have already registered before the 
	 * settings were loaded, so this function is called
	 * as soon as they are loaded which sends settings to 
	 * all modules in the list. 
	 */
	private void initialiseRegisteredModules() {
		if (owners == null) {
			return;
		}
		PamControlledUnitSettings settings = null;
		for (int i = 0; i < owners.size(); i++) {
			settings = findSettings(initialSettingsList, settingsUsed, owners.get(i));
			if (settings != null) {
				try {
					owners.get(i).restoreSettings(settings);
				}
				catch (ClassCastException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Open the file that contains a list of files and optionally open a dialog
	 * giving the list of recent files. 
	 * <p>
	 * Unfortunately, as soon as this gets called th efirst time, it tries to 
	 * open a database to get more settings information and different database
	 * plug ins all start trying to get more settings and it goes round and round and
	 * round. Need to ensure that these loop around only get given the general settings
	 * information. 
	 * @return
	 */
	private boolean loadPSFSettings() {
		if (PamSettingManager.RUN_REMOTE == false) {
		if (settingsFileData == null) {
			loadLocalSettings();
		}
		if (loadingLocalSettings) return false;
		if (settingsFileData.showFileList && programStart) {
			SettingsFileData newData = SettingsFileDialog.showDialog(null, settingsFileData);
			if (newData != null) {
				settingsFileData = newData.clone();
			}
			programStart = false;
		}
		File ff = settingsFileData.getFirstFile();
		}
		
		initialSettingsList = loadSettingsFromFile();
		
		return initialSettingsList != null;
		
	}
	
	/**
	 * Load data from settings files.
	 * <p>
	 * This is just the general data - the list of recently used
	 * psf files and recent database files.  
	 */
	public boolean loadLocalSettings() {
		
		loadingLocalSettings = true;

		loadSettingsFileData();
		
		if (PamSettingManager.RUN_REMOTE == false) {
		if (settingsFileData != null) {
			TipOfTheDayManager.getInstance().setShowAtStart(settingsFileData.showTipAtStartup);
			if (settingsFileData.showTipAtStartup) {	
				TipOfTheDayManager.getInstance().showTip(null, null);
			}
		}
		}
		boolean ok = true; // always ok if non - database settings are used. 
//
//		if (PamController.getInstance().getRunMode() != PamController.RUN_NORMAL) {
//			ok = loadDBSettings();		
//		}

		loadingLocalSettings = false;
		
		return ok;

	}
	
	
	/**
	 * Try to get settings information from a valid databse. If none are
	 * loaded, then return null and Pamguard will try to get them from a psf file.
	 */
	public boolean loadDBSettings() {
		

		if (settingsFileData == null) {
			loadLocalSettings();
		}
		
		loadDatabaseFileData();
		
		// try to find the database settings...
//		PamControlledUnitSettings dbSettings = findGeneralSettings(DBControl.getDbUnitType());
		
		DBControlSettings dbControlSettings = new DBControlSettings();
		
		/*
		 * Get settings from the database from either the Pamguard_Settings_Last
		 * or from the Pamguard_Settings table. 
		 */
		initialSettingsList = dbControlSettings.loadSettingsFromDB();
		
		/**
		 *  now need to get parameters back from the listed modules in databaseOwners
		 *  so that the correct settings can be passed over to the initialSettingsList. 
		 */
		
		if (initialSettingsList != null) {
			/* reading settings from the database was sucessful. Now the problem we have is that
			*  this database closes, and when the 'real' database opens up later, it won't be pointing 
			*  at the same place !
			*  Two options are 1) try to keep this version of the database alive
			*  2) frig the generalsettings so that the 'real' database gets the same ones.
			*  
			*  Trouble is that there are multiple settings in the settings database stuff.
			*  Copy them all back into the generalSettings list
			*/ 
			PamControlledUnitSettings aSet, generalSet;
			/**
			 * Don't take these out of databaseSettingsList - go throuh 
			 */
			PamSettings dbOwner;
			databaseSettingsList.clear();
			if (databaseOwners != null) {
				for (int i = 0; i < databaseOwners.size(); i++) {
					dbOwner = databaseOwners.get(i);
					aSet = new PamControlledUnitSettings(dbOwner.getUnitType(),
							dbOwner.getUnitName(), dbOwner.getSettingsVersion(), dbOwner.getSettingsReference());
					databaseSettingsList.add(aSet);
					// see if there is any settings with the same type and name
					// in the general list and copy settings object over. 
					generalSet = findSettings(initialSettingsList, aSet.getUnitType(), null);
					if (generalSet != null) {
						generalSet.setSettings(aSet.getSettings());
					}
				}
			}
		}
		return (initialSettingsList != null);
	}
	
	/**
	 * See if there is a database module in PAMGUARD and if so, save the 
	 * settings in serialised from in the Pamguard_Settings and Pamguard_Settings_Last
	 * tables. 
	 * @return true if successful. 
	 */
	private boolean saveSettingsToDatabase() {
		// see if there is an existing database module and if there is, then 
		// it will know how to save settings. 
		DBControl dbControl = (DBControl) PamController.getInstance().findControlledUnit(DBControl.getDbUnitType());
		if (dbControl == null) {
			return false;
		}
		return dbControl.saveSettingsToDB();
	}
	
	/**
	 * Find the owner of some PAMGUARD settings. 
	 * @param ownersList which list to search 
	 * @param unitType unit type
	 * @param unitName unit name
	 * @return owner of the settings. 
	 */
	private PamSettings findOwner(ArrayList<PamSettings> ownersList, String unitType, String unitName) {
		PamSettings owner;
		for (int i = 0; i < ownersList.size(); i++) {
			owner = ownersList.get(i);
			if (owner.getUnitType().equals(unitType) == false) continue;
			if (unitName != null && owner.getUnitName().equals(unitName) == false) continue;
			return owner;
		}
		
		return null;
	}
	
	/**
	 * Load PAMGUARD settings from a psf file. 
	 * @return Array list of settings. 
	 */
	private ArrayList<PamControlledUnitSettings> loadSettingsFromFile() {

		ArrayList<PamControlledUnitSettings> newSettingsList = 
			new ArrayList<PamControlledUnitSettings>();
		
		PamControlledUnitSettings newSetting;
				
		ObjectInputStream file = openInputFile();
		
		if (file == null) return null;
		
		Object j;
		while (true) {
			try {
				j = file.readObject();
				newSetting = (PamControlledUnitSettings) j;
				newSettingsList.add(newSetting);
			}
			catch (EOFException eof){
				break;
			}
			catch (IOException io){
				io.printStackTrace();
				break;
			}
			catch (ClassNotFoundException Ex){
				// print and continue - there may be othere things we can deal with.
				Ex.printStackTrace();
			}
			catch (Exception Ex) {
				Ex.printStackTrace();
			}
		}
		try {
			file.close();
		}
		catch (Exception Ex) {
//			Ex.printStackTrace();
		}

		return newSettingsList;
	}
	
	/**
	 * See if a particular PamControlledUnitSettings object is the right one
	 * for a particular module that wants some settings. 
	 * @param settingsUser User of settings
	 * @param settings Settings object. 
	 * @return true if matched. 
	 */
	public boolean isSettingsUnit(PamSettings settingsUser, PamControlledUnitSettings settings) {
		if (settings.getUnitName() == null || settingsUser.getUnitName() == null) return false;
		if (settings.getUnitType() == null || settingsUser.getUnitType() == null) return false;
		if (settings.getUnitName().equals(settingsUser.getUnitName())
				&& settings.getUnitType().equals(settingsUser.getUnitType()) 
				&& settings.versionNo == settingsUser.getSettingsVersion()){
			return true;
		}
		return false;
	}

	/**
	 * Open psf file for settings serialised output. 
	 * @return stream handle. 
	 */
	public ObjectOutputStream openOutputFile() {
		try {
			return new ObjectOutputStream(new FileOutputStream(
					getSettingsFileName()));
			
		} catch (Exception Ex) {
			System.out.println(Ex);
			return null;
		}
	}

	/**
	 * Open psf file for settings input. 
	 * @return stream handle. 
	 */
	private ObjectInputStream openInputFile() {
//		System.out.println("Loading settings from " + getSettingsFileName());
		try {
			return new ObjectInputStream(new FileInputStream(
					getSettingsFileName()));
	
			
		} catch (Exception Ex) {
			//Ex.printStackTrace();
			if(!userNotifiedAbsentSettingsFile){
				System.out.println("Serialized settings file not found in JAR, Possibly not being run from standalone JAR file e.g. in Eclipse ?");
				Splash.setStartupErrors(true);
				JOptionPane.showMessageDialog(null,
	                "Cannot Load: " + getSettingsFileName() +"\nAttempting to load defaults!"
	                +"\nThis is expected on first use."
	                ,
	                "PamSettingManager",
	                JOptionPane.WARNING_MESSAGE);  
				userNotifiedAbsentSettingsFile= true;
			}
			return openInputFileResource();
		}
	}

	/**
	 * Returns total gobbledygook - need to improve the way
	 * PAMGAURD creates new psf files. 
	 * @return lies. 
	 */
	private ObjectInputStream openInputFileResource() {
		try {
			return new ObjectInputStream( //new FileInputStream(
					ClassLoader.getSystemResourceAsStream("DefaultPamguardSettings.psf"));		
		} catch (Exception Ex) {
			//Ex.printStackTrace();
			System.out.println("Serialized default settings file not found!");
			if(!userNotifiedAbsentDefaultSettingsFile){
			JOptionPane.showMessageDialog(null,
	                "No Default Settings Found",
	                "PamSettingManager",
	                JOptionPane.ERROR_MESSAGE);
			}
			userNotifiedAbsentDefaultSettingsFile= true;
			return null;
		}
	}

	/**
	 * Now that the database is becoming much more fundamental to settings
	 * storage and retrieval, the latest database settings should go into 
	 * the main settings file. This contains a list of recent databases. The trouble is,
	 * the settings are spread amongst several different settings object (e.g. one that 
	 * tells us what type of database, another that tells us a list of recent databases 
	 * for a specific database type, etc. 
	 * <p>
	 * We therefore need some modules (i.e. database ones) to also store their settings
	 * in a general settings list so that they can be read in before any other settings
	 * are read in. So each unit when it registers, says whether it should be included in
	 * the general list as well as the specific data file. 
	 * 
	 */
	public boolean loadSettingsFileData() {
		ObjectInputStream is = null;
		settingsFileData = new SettingsFileData();
		try {
			is = new ObjectInputStream(new FileInputStream(settingsListFile));
			settingsFileData = (SettingsFileData) is.readObject();
			
		} catch (Exception Ex) {
//			System.out.println(Ex);
			System.out.println("Unable to open " + settingsListFile + " this is normal on first use");
		}
		try {
			if (is != null) {
				is.close();
			}
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}
		if (settingsFileData == null){
			settingsFileData = new SettingsFileData();
			settingsFileData.createDefaultList();
			return false;
		}
		
		return true;
	}
	
	/**
	 * Save the list of recently used settings files. 
	 * @return true if write OK.
	 */
	private boolean saveSettingsFileData() {
		
		if (settingsFileData == null) {
			return false;
		}
		if (PamSettingManager.RUN_REMOTE == false) {
		settingsFileData.showTipAtStartup = TipOfTheDayManager.getInstance().isShowAtStart();
		}
		
		ObjectOutputStream os;
		try {
			os = new ObjectOutputStream(new FileOutputStream(settingsListFile));
			os.writeObject(settingsFileData);
		} catch (Exception Ex) {
			System.out.println(Ex);
			return false;
		}
		try {
			os.close();
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}
		return true;
	}

	/**
	 * Loads the details of the last database to be opened. This will 
	 * probably be in the form of multiple serialised objects since
	 * the database information is spread amongst several plug in sub-modules. 
	 * @return true if settings data loaded ok
	 */
	private boolean loadDatabaseFileData() {
		
		ObjectInputStream is;

		PamControlledUnitSettings newSetting;
		databaseSettingsList = new ArrayList<PamControlledUnitSettings>();
		Object j;
		try {
			is = new ObjectInputStream(new FileInputStream(databaseListFile));
		} catch (Exception Ex) {
			return false;
		}
		while (true) {
			try {
				j = is.readObject();
				newSetting = (PamControlledUnitSettings) j;
				databaseSettingsList.add(newSetting);
			}
			catch (EOFException eof){
				break;
			}
			catch (IOException io){
				break;
			}
			catch (ClassNotFoundException Ex){
				// print and continue - there may be othere things we can deal with.
				Ex.printStackTrace();
			}
			catch (Exception Ex) {
				Ex.printStackTrace();
			}
		}
		try {
			is.close();
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}
		
		return true;
	}
	/**
	 * Save the details of the most recently used database. 
	 * @return true if successful. 
	 */
	private boolean saveDatabaseFileData() {

		if (databaseOwners == null || databaseOwners.size() == 0) {
			return false;
		}
		databaseSettingsList.clear();
		PamSettings dbOwner;
		PamControlledUnitSettings aSet;
		for (int i = 0; i < databaseOwners.size(); i++) {
			dbOwner = databaseOwners.get(i);
			aSet = new PamControlledUnitSettings(dbOwner.getUnitType(),
					dbOwner.getUnitName(), dbOwner.getSettingsVersion(), dbOwner.getSettingsReference());
			databaseSettingsList.add(aSet);
		}
		
		ObjectOutputStream os;
		try {
			os = new ObjectOutputStream(new FileOutputStream(databaseListFile));
		} catch (Exception Ex) {
			return false;
		}

//		write out the settings for all units in the general owners list. 
		ArrayList<PamControlledUnitSettings> generalSettingsList;
		generalSettingsList = new ArrayList<PamControlledUnitSettings>();
		for (int i = 0; i < databaseOwners.size(); i++) {
			generalSettingsList
					.add(new PamControlledUnitSettings(databaseOwners.get(i)
							.getUnitType(), databaseOwners.get(i).getUnitName(), 
							databaseOwners.get(i).getSettingsVersion(), 
							databaseOwners.get(i).getSettingsReference()));
		}
		try {
			for (int i = 0; i < generalSettingsList.size(); i++){
				os.writeObject(generalSettingsList.get(i));
			}
		} catch (Exception Ex) {
			Ex.printStackTrace();
			return false;
		}
		
		
		try {
			os.close();
		}
		catch (IOException ex) {
			ex.printStackTrace();
		}
		return true;
	}
	
	/**
	 * Get the most recently used settings file name. We have added a switch in here
	 * to allow for the direct setting of the psf used from the command line. This
	 * can be used in remote on non remote deployments.
	 * @return File name string. 
	 */
	public String getSettingsFileName() {
		if (PamSettingManager.RUN_REMOTE) {
			System.out.println("Running in REMOTE mode with settings settings from " + remote_psf);
			return remote_psf;
		} else {
		if (settingsFileData == null || settingsFileData.getFirstFile() == null) {
			return null;
		}
		return settingsFileData.getFirstFile().getAbsolutePath();
	}
	}
	
	public String getDefaultFile() {
		String fn = getSettingsFileName();
		if (fn == null) {
			fn = "PamguardSettings.psf";
		}
		return fn;
	}
	


	/**
	 * saves settings in the current file
	 * @param frame GUI frame (needed for dialog, can be null)
	 */
	public void saveSettings(JFrame frame) {
		saveFinalSettings();
	}
	
	/**
	 * Save settings to a new psf file. 
	 * @param frame parent frame for dialog.
	 */
	public void saveSettingsAs(JFrame frame) {
		/*
		 * get a new file name, set that as the current file
		 * then write all settings to it.
		 */
		File file = null;
		if (settingsFileData != null) {
			file = settingsFileData.getFirstFile();
		}
		JFileChooser jFileChooser = new JFileChooser(file);
//		jFileChooser.setFileFilter(new SettingsFileFilter());
		jFileChooser.setApproveButtonText("Select");
		jFileChooser.addChoosableFileFilter(new PamFileFilter("PAMGUARD Settings files", fileEnd));
//		jFileChooser.setFileFilter(new FileNameExtensionFilter("PAMGUARD Settings files", defaultFile));
		int state = jFileChooser.showSaveDialog(frame);
		if (state != JFileChooser.APPROVE_OPTION) return;
		File newFile = jFileChooser.getSelectedFile();
		if (newFile == null) return;
//		newFile.getAbsoluteFile().
		newFile = PamFileFilter.checkFileEnd(newFile, fileEnd, true);
		
		System.out.println(newFile.getAbsolutePath());
		
		setDefaultFile(newFile.getAbsolutePath());
		
		saveSettings(SAVE_PSF);
		
		PamController.getInstance().getGuiFrameManager().sortFrameTitles();
		
	}
	

	/**
	 * Set the default (first) file in the settings file data. 
	 * @param defaultFile File name string. 
	 */
	public void setDefaultFile(String defaultFile) {
		
		/**
		 * If saving from viewer or mixed mode, then the 
		 * settingsFileData may not have been loaded, in which case
		 * load it now so that old psf names remain in the list. 
		 */
		if (settingsFileData == null) {
			loadSettingsFileData();
			if (settingsFileData == null) {
				settingsFileData = new SettingsFileData();
			}
		}
		settingsFileData.setFirstFile(new File(defaultFile));
		
	}

	
	/**
	 * pop up the dialog that's shown at start up to show
	 * a list of recent settings file and give the opportunity
	 * for browsing for more. IF the new settings file is
	 * different from the current one, then send a command off 
	 * to the Controller to re-do the entire Pamguard system model
	 * @param frame parent frame for dialog (can be null)
	 */
	public void loadSettingsFrom(JFrame frame) {
		/*
		 */
		File currentFile = null;
		if (settingsFileData != null) {
			currentFile = settingsFileData.getFirstFile();
		}
		SettingsFileData newData = SettingsFileDialog.showDialog(null, settingsFileData);
		if (newData == null) {
			return;
		}
		settingsFileData = newData.clone();
		if (settingsFileData.getFirstFile() != currentFile) {
			saveSettingsFileData();
			// rebuild the entire model. 
			PamControllerInterface pamController = PamController.getInstance();
			if (pamController == null) return;
			pamController.totalModelRebuild();
		}
		
	}
	
	/**
	 * Import a configuration during viewer mode operation. 
	 * @param frame
	 */
	public void importSettings(JFrame frame) {
		if (settingsFileData == null) {
			loadLocalSettings();
		}
		File currentFile = null;		
		if (settingsFileData != null) {
			currentFile = settingsFileData.getFirstFile();
		}
		SettingsFileData newData = SettingsFileDialog.showDialog(null, settingsFileData);
		if (newData == null) {
			return;
		}
		/*
		 * Should now have a valid settings file. Import the data from it. 
		 */
		PamSettingsGroup pamSettingsGroup = new PamSettingsGroup(System.currentTimeMillis());
		/**
		 * Load all the settings from the latest psf and add them to this settings group
		 * then incorporate them into the model in the same way as is done from 
		 * the Dataview settings strip. 
		 */

		ObjectInputStream file = openInputFile();
		
		if (file == null) return;
		PamControlledUnitSettings newSetting;
		
		Object j;
		while (true) {
			try {
				j = file.readObject();
				newSetting = (PamControlledUnitSettings) j;
				pamSettingsGroup.addSettings(newSetting);
//				newSettingsList.add(newSetting);
			}
			catch (EOFException eof){
				break;
			}
			catch (IOException io){
				io.printStackTrace();
				break;
			}
			catch (ClassNotFoundException Ex){
				// print and continue - there may be othere things we can deal with.
				Ex.printStackTrace();
			}
			catch (Exception Ex) {
				Ex.printStackTrace();
			}
		}
		try {
			file.close();
		}
		catch (Exception Ex) {
//			Ex.printStackTrace();
		}

		PamController.getInstance().loadOldSettings(pamSettingsGroup);
		
	}
	public void exportSettings(JFrame frame) {
		
	}

	public ArrayList<PamSettings> getOwners() {
		return owners;
	}
	
	/**
	 * 
	 * @return everything about every set of settings currently loaded. 
	 */
	public PamSettingsGroup getCurrentSettingsGroup() {
		PamSettingsGroup psg = new PamSettingsGroup(PamCalendar.getTimeInMillis());
		PamControlledUnitSettings pcus;
		PamSettings ps;
		for (int i = 0; i < owners.size(); i++) {
			ps = owners.get(i);
			pcus = new PamControlledUnitSettings(ps.getUnitType(), ps.getUnitName(), 
					ps.getSettingsVersion(), ps.getSettingsReference());
			psg.addSettings(pcus);
		}
		return psg;
	}
	
	/**
	 * Load some old settings into all modules.
	 * <p>Currently used in viewer mode to load reloaded settings
	 * from binary files and the database. 
	 * @param settingsGroup settings group to load.
	 * @param send these new settings round to all existing modules.  
	 */
	public void loadSettingsGroup(PamSettingsGroup settingsGroup, boolean notifyExisting) {
		initialSettingsList = settingsGroup.getUnitSettings();
		if (notifyExisting) {
			initialiseRegisteredModules();
		}
	}
}
