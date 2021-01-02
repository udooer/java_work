package PamController;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

/*
 * Class containing data first read in on Pamguard start up
 * which will tell us where settings were last stored and 
 * what to do - just load default, or pop up options list
 */
public class SettingsFileData implements Serializable, Cloneable {

	static final long serialVersionUID = 1;
	
	protected boolean showFileList = true;
	
	protected int maxFiles = 10;
	
	protected ArrayList<File> recentFiles = new ArrayList<File>();
	
	protected boolean showTipAtStartup = true;
	
	public static transient String defaultFile = "PamguardSettings." + PamSettingManager.fileEnd;
	
	public static transient String oldDefaultFile = "PamguardSettings.psf";
		
	public SettingsFileData() {
		recentFiles.add(new File(defaultFile));
	}

	public void setFirstFile(File firstFile) {
		recentFiles.remove(firstFile);
		recentFiles.add(0, firstFile);
	}
	
	public void trimList() {
		int l;
		maxFiles = Math.max(maxFiles, 1);
		while ((l = recentFiles.size()) > maxFiles) {
			recentFiles.remove(l-1);
		}
	}
	
	public File getFirstFile() {
		if (recentFiles.size() < 1) return getDefaultFile();
		return recentFiles.get(0);
	}
	
	public void createDefaultList() {
		setFirstFile(getDefaultFile());
	}
	
	private File getDefaultFile() {
		// check if default file is there, if not, try to copy the 
		// old default file (that had a different ending to it)
		File f = new File(defaultFile);
		if (f.exists()) return f;
//		f = null;
		File oldF = new File(oldDefaultFile);
		if (oldF.exists()) {
			if (oldF.renameTo(f)) {
				return new File(defaultFile);
			}
		}
		return oldF;
	}

	@Override
	protected SettingsFileData clone()  {
		try {
			return (SettingsFileData) super.clone();
		}
		catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
