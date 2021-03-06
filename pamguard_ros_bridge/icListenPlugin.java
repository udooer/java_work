package icListenPluginDaqBETA3_4;
/**This is the class that will implement the DaqSystemInterface.
 *It will comes as a jarfile and interpret by the Pamguard system.
 *It uses the Pamguard external plugin architecture to design the interface
 *for user to interact with the Ocean Sonics icListen Devices.
 *@Author Yangfan Chen 
 */

import Acquisition.AcquisitionControl;
import Acquisition.DaqSystem;
import Acquisition.DaqSystemInterface;
import PamModel.PamDependency;
import PamModel.PamPluginInterface;

public class icListenPlugin implements DaqSystemInterface {
	String jarFile;
	/*Most of the developing information here will be displayed under about plugin-icListenPlugin*/

	@Override
	public String getDefaultName() {
		return "icListenPlugin";
	}

	// no help file currently existed.
	@Override
	public String getHelpSetName() {
		return null;
	}

	//can reset the jar file names,but not recommended.
	@Override
	public void setJarFile(String jarFile) {
		this.jarFile = jarFile;
	}

	//get the jar files names
	@Override
	public String getJarFile() {
		return jarFile;
	}

	//the author name of the plugin.
	@Override
	public String getDeveloperName() {
		return "Yangfan Chen";
	}

	//Contact information of author
	@Override
	public String getContactEmail() {
		return "coop.student@oceansonics.com";
	}

	
	//the version the plugin is running on.
	@Override
	public String getVersion() {
		return "2.0014e Beta";
	}

	//the version the plugin is developed on.
	@Override
	public String getPamVerDevelopedOn() {
		return "2.0014e Beta";
	}

	//the version the plugin is tested and worked best on.
	@Override
	public String getPamVerTestedOn() {
		return "2.0014e Beta";
	}

	//the basic functionality of what this plugin can do.
	@Override
	public String getAboutText() {
		return "This device can display spectrogram from the icListen device using PAMGuard MVC";
	}

	// Pamguard will take interpret this class and uses their architecture to
	// initializes a Daq Plugin.
	@Override
	public DaqSystem createDAQControl(AcquisitionControl acObject) {
		return new icListenDaq(acObject);

	}

}
