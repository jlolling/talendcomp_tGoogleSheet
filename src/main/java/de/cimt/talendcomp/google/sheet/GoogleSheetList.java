package de.cimt.talendcomp.google.sheet;

import java.util.List;

import com.google.api.services.sheets.v4.Sheets.Spreadsheets;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.Spreadsheet;

public class GoogleSheetList extends GoogleSheet {

	public GoogleSheetList() throws Exception {
		super();
	}

	public List<Sheet> listSheets() throws Exception {
		Spreadsheets.Get reqGetSheets = getService().spreadsheets().get(getSpreadsheetId());
		reqGetSheets.setPrettyPrint(false);
		reqGetSheets.setPp(false);
		reqGetSheets.setIncludeGridData(false);
		Spreadsheet spreadSheet = (Spreadsheet) execute(reqGetSheets);
		List<Sheet> listSheets = spreadSheet.getSheets();
		return listSheets;
	}
	
}
