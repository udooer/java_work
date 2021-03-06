package Acquisition;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;


/**
 * Implementation of FileDate to read the standard file
 * date format that I use in Logger. I'd have thought that
 * others would be keen to add alternatives, but no one has !
 * 
 * @author Doug Gillespie
 *
 */
public class StandardFileDate implements FileDate {

	public boolean doSettings() {
		return false;
	}

	public String getDescription() {
		return "Standard yyyymmdd_hhmmss file dates as used by Pamguard and Logger recorders";
	}

	public String getName() {
		return "Standard Data Format";
	}

	public long getTimeFromFile(File file) {
		// find the first and last numeric characters in the file name
		String fileName = file.getName();
		if (fileName.length() == 0) return 0;
		int firstNum = -1, lastNum = -1;
		char c;
		for (int i = 0; i < fileName.length(); i++) {
			c = fileName.charAt(i);
			if (c >= '0' && c <= '9') {
				if (firstNum == -1) {
					firstNum = i;
				}
				lastNum = i;
			}
		}
		if (lastNum < 0) return 0;
		String numbers = fileName.substring(firstNum, lastNum+1);
//		System.out.println(numbers);
		
		Date dt = tryParseDate(numbers);
		if (dt != null) {
			Calendar cl = Calendar.getInstance();
			cl.setTimeZone(TimeZone.getTimeZone("GMT"));
			cl.setTime(dt);
			return cl.getTimeInMillis();
		}
		//None of the formats worked.
		System.out.println("Unparsable date format: " + numbers);
		return 0;
	}
	
	/** For each digit substring in the file name (i.e., substring starting
	 * with a digit that does not have a digit preceding), try a bunch of
	 * format strings and see if any of them match.  If there is a match,
	 * return the corresponding Date, else return null.
	 */
	private Date tryParseDate(String inputString)
	{
		/* Formats have various permutations of 4-digit vs. 2-digit year, 
		 * '-' vs. '_' as the separator between date and time, seconds or
		 * not, and dots between adjacent numbers or not.  Also yearday
		 * vs. month-and-day, though formats using yearday don't allow for
		 * 4-digit years.
		 */  
		String numstr = new String(inputString);
		/*
		 * Inez has some files in the format 0222.DSG_RAWD_HMS_21_50_ 0__DMY_ 1_ 7_10.wav.
		 * Look for the .DSG early in the string and get rid of it !
		 */
		int subInd = numstr.indexOf(".DSG");
		if (subInd >= 0) {
			// find the first number after that and hope it's OK from there ...
			numstr = numstr.substring(subInd); // 
		}
		/*
		 * The DCL 5 dataset has some long names which contians the date, but also some othernumbers which 
		 * really screws this up. Search for multiple groups of numbers and keep the last two which are > 6 characters long.   
		 */
		numstr = findDateSubstring(numstr);
		
		final String[] formats = {
				"HH_mm_ss'__DMY_'dd_MM_yy", // Inez DSG files egCopy of _RAWD_HMS_01_27_00__DMY_09_05_2010.wav
				"HH'h'mm'm'ss's'ddMMMyyyy", // Inez CRSS files
				"yyyy.MM.dd_HH.mm.ss", 
				"yyyy.MM.dd-HH.mm.ss",
				"yyyyMMdd_HHmmss", 
				"yyyyMMdd-HHmmss",
				"yy.MM.dd_HH.mm.ss", 
				"yy.MM.dd-HH.mm.ss",
				"yyMMdd_HHmmss", 
				"yyMMdd-HHmmss",
				"yyyy.MM.dd_HH.mm", 
				"yyyy.MM.dd-HH.mm",
				"yyyyMMdd_HHmm", 
				"yyyyMMdd-HHmm",
				"yy.MM.dd_HH.mm", 
				"yy.MM.dd-HH.mm",
				"yyMMdd_HHmm", 
				"yyMMdd-HHmm",
				"yy.DDD_HH.mm.ss", 
				"yy.DDD-HH.mm.ss",
				"yyDDD_HHmmss", 
				"yyDDD-HHmmss",
				"yy.DDD_HH.mm", 
				"yy.DDD-HH.mm",
				"yyDDD_HHmm", 
				"yyDDD-HHmm",
				"yyyy-MM-dd HH_mm_ss", // Avisoft.
				"yyyy-MM-dd_HH-mm-ss" // y2000 Cornell pop up data
		};
		boolean prevWasDigit = false;
		for (int i = 0; i < numstr.length(); i++) {
			boolean isDigit = java.lang.Character.isDigit(numstr.charAt(i));
			if (isDigit && !prevWasDigit) {
				String str = numstr.substring(i);
j:				for (int j = 0; j < formats.length; j++) {
					String fmt = formats[j];
					//parse() doesn't check that all the digit format characters in
					//fmt line up with digit characters in 'numbers', so we have to.
					//First check that 'numbers' is long enough.
					if (j > 2) {
					if (str.length() < fmt.length())
						continue j;
					for (int k = 0; k < fmt.length(); k++) {
						if (java.lang.Character.isLetter(fmt.charAt(k))) {
							char ch = str.charAt(k);
							boolean t1 = (ch >= '0');
							boolean t2 = (ch <= '9');
							if (!(t1 && t2))
								continue j;
						}
					}
					}
					//Also, we don't like this format if the character in 'numbers'
					//just after the end of the formatted string is another digit;
					//that probably means the format is the wrong one.
					if (str.length() >= fmt.length()+1)
						if (java.lang.Character.isDigit(str.charAt(fmt.length())))
							continue j;
					//Now see if DateFormat can parse it.
					try {
						SimpleDateFormat df = new SimpleDateFormat(fmt);
						df.setTimeZone(TimeZone.getTimeZone("GMT"));
						Date d = df.parse(str);  //throws ParseException if no match
						return d;     /////////////////////////////////found one!
					}
					catch (java.text.ParseException ex) {
						//No problem, just go on to next format to try.
					}
				}
			}
			prevWasDigit = isDigit;
		}
		return null;
	}
	
	private String findDateSubstring(String numstr) {
		boolean prevWasDigit = false;
		boolean isDigit;
		char aChar;
		ArrayList<String> stringBits = new ArrayList<String>();
		String aString = new String();
		int n = numstr.length();
		for (int i = 0; i < n; i++) {
			isDigit = java.lang.Character.isDigit(aChar = numstr.charAt(i));
			if (isDigit) {
				aString += aChar;
			}
			else if (aString.length() > 0) {
				if (aString.length() == 6 || aString.length() == 8) {
					stringBits.add(aString);
				}
				aString = new String();
			}
		}
		int nBits = stringBits.size();
		if (nBits < 2) {
			return numstr;
		}
		else {
			return stringBits.get(nBits-2) + "_" + stringBits.get(nBits-1); 
		}
		
	}

	public boolean hasSettings() {
		return false;
	}

}
