package PamController;

/**
 * Class to hold static license and release information for PAMGUARD.
 * Should be updated for each major and minor release. 
 * 
 * @author Douglas Gillespie
 *
 */
public class PamguardVersionInfo {

	static public final int RELEASE_CORE = 0;
	static public final int RELEASE_BETA = 1;
	static public final int RELEASE_SMRU = 2;
	static public final int RELEASE_OTHER = 3;
	/**
	 * Version number, major version.minorversion.sub-release.
	 */
	static public final String version = "1.10.00c";
	
	/**
	 * Release type - Beta or Core
	 */
	static public final String release = "Core";
	
	static public final String revisionString = "$Rev: 26 $";
	
	/**
	 * GNU License statement
	 */
	static public final String license = "This program is free software: you can redistribute it and/or modify " +
    "it under the terms of the GNU General Public License as published by " +
    "the Free Software Foundation, either version 3 of the License, or " +
    "(at your option) any later version. " +
    "\n\n" +
    "This program is distributed in the hope that it will be useful, " +
    "but WITHOUT ANY WARRANTY; without even the implied warranty of " +
    "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the " +
    "GNU General Public License for more details. " +
    "\n\n" +
    "You should have received a copy of the GNU General Public License " +
    "along with this program.  If not, see <http://www.gnu.org/licenses/>." +
    "";
	
	/**
	 * Pamguard web address
	 */
	static public final String webAddress = "www.pamguard.org";
	
	/**
	 * Release data
	 */
	static public final String date = "2 May 2011";
	
	/**
	 * Type of release - used to switch off and on some features. 
	 * @return release type 
	 */
	static public int getReleaseType() {
		return RELEASE_CORE;
	}
	
	/**
	 * @return the code revision number from the SVN repository
	 */
	static public int getRevision() {
		try {
			int spacePos = revisionString.indexOf(' ');
			if (spacePos == -1) {
				return 0;
			}
			String newStr = revisionString.substring(spacePos+1);
			spacePos = newStr.indexOf(' ');
			if (spacePos > 0) {
				newStr = newStr.substring(0, spacePos);
			}
			return Integer.valueOf(newStr);
		}
		catch (NumberFormatException e) {
			return 0;
		}
	}

}
