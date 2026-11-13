package de.jlo.talendcomp.google.sheet;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.api.services.sheets.v4.model.GridProperties;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.SheetProperties;

public class CellUtil {
	
    /** The character ($) that signifies a row or column value is absolute instead of relative */
    private static final char ABSOLUTE_REFERENCE_MARKER = '$';
    /** The character (!) that separates sheet names from cell references */
    private static final char SHEET_NAME_DELIMITER = '!';

    /**
     * takes in a column reference portion of a CellRef and converts it from
     * ALPHA-26 number format to 0-based base 10.
     * 'A' -> 0
     * 'Z' -> 25
     * 'AA' -> 26
     * 'IV' -> 255
     * @return zero based column index
     */
    public static int convertColStringToIndex(String ref) {
        int retval=0;
        char[] refs = ref.toUpperCase(Locale.ROOT).toCharArray();
        for (int k=0; k<refs.length; k++) {
            char thechar = refs[k];
            if (thechar == ABSOLUTE_REFERENCE_MARKER) {
                if (k != 0) {
                    throw new IllegalArgumentException("Bad col ref format '" + ref + "'");
                }
                continue;
            }

            // Character is uppercase letter, find relative value to A
            retval = (retval * 26) + (thechar - 'A' + 1);
        }
        return retval-1;
    }
    
    /**
     * Takes in a 0-based base-10 column and returns a ALPHA-26
     *  representation.
     *  eg column #3 -> D
     */
    public static String convertNumToColString(int col) {
        // Excel counts column A as the 1st column, we
        //  treat it as the 0th one
        int excelColNum = col + 1;

        StringBuilder colRef = new StringBuilder(2);
        int colRemain = excelColNum;

        while(colRemain > 0) {
            int thisPart = colRemain % 26;
            if(thisPart == 0) { thisPart = 26; }
            colRemain = (colRemain - thisPart) / 26;
            // The letter A is at 65
            char colChar = (char)(thisPart+64);
            colRef.insert(0, colChar);
        }
        return colRef.toString();
    }

    public static boolean isEmpty(String s) {
    	if (s == null || s.trim().isEmpty()) {
    		return true;
    	} else if ("null".equals(s)) {
    		return true;
    	} else {
    		return false;
    	}
    }

    public static int getLastSheetRowIndex(Sheet sheet) {
    	SheetProperties props = sheet.getProperties();
    	GridProperties gridProps = props.getGridProperties();
    	Integer lastRowIndex = 0;
    	if (gridProps != null) {
    		lastRowIndex = gridProps.getRowCount();
    	}
    	return lastRowIndex;
    }
    
    public static int getLastSheetColumnIndex(Sheet sheet) {
    	SheetProperties props = sheet.getProperties();
    	GridProperties gridProps = props.getGridProperties();
    	Integer lastColumnIndex = 0;
    	if (gridProps != null) {
    		lastColumnIndex = gridProps.getColumnCount();
    	}
    	return lastColumnIndex;
    }

    public static String buildRange(String sheetName, String firstColumnName, int lastColumnIndex, Integer firstRowIndex, Integer lastRowIndex) {
    	StringBuilder sb = new StringBuilder();
    	boolean hasSheetName = false;
    	if (isEmpty(sheetName) == false) {
    		sb.append(sheetName);
    		hasSheetName = true;
    	}
    	if (isEmpty(firstColumnName) == false) {
    		if (hasSheetName) {
    			sb.append(SHEET_NAME_DELIMITER);
    		}
			sb.append(firstColumnName);
			if (firstRowIndex != null) {
				if (firstRowIndex < 1) {
					throw new IllegalArgumentException("The first row index must start with 1. Current value: " + firstRowIndex);
				}
				sb.append(firstRowIndex);
			}
			if (lastColumnIndex > 0) {
				sb.append(":");
				sb.append(convertNumToColString(lastColumnIndex));
				if (lastRowIndex != null) {
					if (firstRowIndex != null && lastRowIndex < firstRowIndex) {
						throw new IllegalArgumentException("The last row index must greater than the first row index. firstRowIndex: " + firstRowIndex + " lastRowIndex: " + lastRowIndex);
					}
					if (lastRowIndex < 1) {
						throw new IllegalArgumentException("The last row index must start with 1. Current value: " + lastRowIndex);
					}
					sb.append(lastRowIndex);
				}
			}
    	}
    	if (sb.length() > 0) {
    		return sb.toString();
    	} else {
    		return null;
    	}
    }
    
    /**
     * Extracts from a ref string the last row index.
     * @param ref
     * @return
     */
    public static int getLastRowIndex(String ref) {
    	if (ref == null || ref.trim().isEmpty()) {
    		throw new IllegalArgumentException("ref cannot be null or empty");
    	} else {
    		String regex = "([0-9]{1,})$";
			Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
	        Matcher matcher = pattern.matcher(ref);
	        while (matcher.find()) {
	            if (matcher.start() < matcher.end()) {
	            	String numberStr = matcher.group(1);
	            	return Integer.parseInt(numberStr);
	            }
	        }
    	}
    	return 0;
    }
    
}
