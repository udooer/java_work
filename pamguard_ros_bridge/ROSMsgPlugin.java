package pamguard_ros_bridge;
/*
 *  This is the class that will implement the DaqSystemInterface.
 *  It will comes a jarfile and interpret by the Pamguard system.
 *  It uses the Pamguard external plugin arch to design the interface
 *  for user to interact with ROS self-defined HydrophoneData msg.  
 *  Shane
 *  2020_12_24
 */

import Acquisition.AcquisitionControl;
import Acquisition.DaqSystem;
import Acquisition.DaqSystemInterface;

import pamguard_ros_bridge.ROSMsgDaq;

public class ROSMsgPlugin implements DaqSystemInterface{
    public String jarfile;
    @Override
    public String getDefaultName() {
        return "ROSMsgPlugin";
    }

    // no help file currently existed.
    @Override
    public String getHelpSetName() {
        return null;
    }

    //can reset the jar file names,but not recommended.
    @Override
    public void setJarFile(String jarFile) {
        this.jarfile = jarfile;
    }

    //get the jar files names
    @Override
    public String getJarFile() {
        return jarfile;
    }

    //the author name of the plugin.
    @Override
    public String getDeveloperName() {
        return "Shane";
    }

    //Contact information of author
    @Override
    public String getContactEmail() {
        return "hunghsuyong114shane@gmail.com";
    }

    
    //the version the plugin is running on.
    @Override
    public String getVersion() {
        return "1.00 Beta";
    }

    //the version the plugin is developed on.
    @Override
    public String getPamVerDevelopedOn() {
        return "1.00 Beta";
    }

    //the version the plugin is tested and worked best on.
    @Override
    public String getPamVerTestedOn() {
        return "1.00 Beta";
    }

    //the basic functionality of what this plugin can do.
    @Override
    public String getAboutText() {
        return "This device can display spectrogram from the PAM buoy device using PAMGuard MVC";
    }

    // Pamguard will take interpret this class and uses their architecture to
    // initializes a Daq Plugin.
    @Override
    public DaqSystem createDAQControl(AcquisitionControl acObject) {
        return new ROSMsgDaq(acObject);
    }
}