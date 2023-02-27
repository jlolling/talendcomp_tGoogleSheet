package de.jlo.talendcomp.google.sheet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.Sheets.Spreadsheets.BatchUpdate;
import com.google.api.services.sheets.v4.model.AddSheetRequest;
import com.google.api.services.sheets.v4.model.AppendValuesResponse;
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateValuesRequest;
import com.google.api.services.sheets.v4.model.BatchUpdateValuesResponse;
import com.google.api.services.sheets.v4.model.DeleteDimensionRequest;
import com.google.api.services.sheets.v4.model.DimensionRange;
import com.google.api.services.sheets.v4.model.GridProperties;
import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.SheetProperties;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.SpreadsheetProperties;
import com.google.api.services.sheets.v4.model.UpdateValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;

public class GoogleSheetOutput extends GoogleSheet {
	
	private static final String VALUE_INPUT_OPTION_RAW = "RAW";
	private static final String INSERT_DATA_OPTION_OVERWRITE = "OVERWRITE";
	private static final String INSERT_DATA_OPTION_INSERT_ROWS = "INSERT_ROWS";
	private int countRows = 0;
	private String range = null;
	private int startRowIndex = 1;
	private int lastRowIndex = 0;
	private String startColumnName = "A";
	private int endColumnIndex = 0;
	private int maxRowWith = -1;
	private String documentTitle = null;
	private ValueRange valueRange = null; 
	private Integer countUpdatedRows = 0;
	private String updatedRange = null;
	private static String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
	private List<Object> row = new ArrayList<Object>();
	private SimpleDateFormat dateFormat = new SimpleDateFormat();
	private boolean appendOnExistingData = false;
	private String valueInputOption = VALUE_INPUT_OPTION_RAW;
	private Sheet currentSheet = null;

	public GoogleSheetOutput() throws Exception {
		super();
	}

	public void createSheetDocument() throws Exception {
		if (documentTitle == null || documentTitle.trim().isEmpty()) {
			throw new IllegalStateException("Title of the new Google Sheet is needed!");
		}
		Spreadsheet requestBody = new Spreadsheet();
		SpreadsheetProperties properties = new SpreadsheetProperties();
		properties.setTitle(documentTitle);
		requestBody.setProperties(properties);
		Sheets.Spreadsheets.Create request = getService().spreadsheets().create(requestBody);
		Spreadsheet spreadSheet = (Spreadsheet) execute(request);
		setSpreadsheetId(spreadSheet.getSpreadsheetId());
		valueRange = null;
	}
	
	public void reset() {
		countRows = 0;
		lastRowIndex = 0;
		range = null;
	}
	
	private ValueRange getValueRange() {
		if (valueRange == null) {
			valueRange = new ValueRange();
			valueRange.setValues(new ArrayList<List<Object>>());
			valueRange.setMajorDimension("ROWS");
		}
		return valueRange;
	}
	
	public void addRow() {
		getValueRange().getValues().add(row);
		countRows++;
		checkForMaxRowWith(row.size() - 1);
		row = new ArrayList<Object>();
	}
	
	public void addValue(Object value, String option) {
		if (value == null) {
			row.add("");
		} else if (value instanceof Date) {
			if (option != null && option.trim().isEmpty() == false) {
				dateFormat.applyPattern(option);
			} else {
				dateFormat.applyPattern(DEFAULT_DATE_FORMAT);
			}
			row.add(dateFormat.format((Date) value));
		} else {
			row.add(value);
		}
	}
	
	private void checkForMaxRowWith(int columnIndex) {
		if (maxRowWith < columnIndex) {
			maxRowWith = columnIndex;
		}
	}
	
	public void initializeSheet() throws Exception {
		if (getSheetName() == null) {
			throw new IllegalStateException("Sheet name must be set!");
		}
		List<Sheet> listSheets = listSheets();
		boolean needCreate = true;
		for (Sheet sheet : listSheets) {
			if (getSheetName().equals(sheet.getProperties().getTitle())) {
				currentSheet = sheet;
				needCreate = false;
				break;
			}
		}
		if (needCreate) {
			SheetProperties sheetProperties = new SheetProperties();
			sheetProperties.setTitle(getSheetName());
			sheetProperties.setSheetType("GRID");
			GridProperties gridProperties = new GridProperties();
			gridProperties.setRowCount(lastRowIndex + 1);
			gridProperties.setColumnCount(endColumnIndex);
			AddSheetRequest addSheetRequest = new AddSheetRequest();
			addSheetRequest.setProperties(sheetProperties);
			BatchUpdateSpreadsheetRequest bur = new BatchUpdateSpreadsheetRequest();
			List<Request> listReq = new ArrayList<Request>();
			listReq.add(new Request().setAddSheet(addSheetRequest));
			bur.setRequests(listReq);
			BatchUpdate request = getService().spreadsheets().batchUpdate(getSpreadsheetId(), bur);
			execute(request);
		}
		listSheets = listSheets();
		for (Sheet sheet : listSheets) {
			if (getSheetName().equals(sheet.getProperties().getTitle())) {
				currentSheet = sheet;
				break;
			}
		}
		if (currentSheet == null) {
			throw new Exception("Sheet name: " + getSheetName() + "could not be found and also be created!");
		}
		lastRowIndex = CellUtil.getLastSheetRowIndex(currentSheet);
	}

	public void executeUpdate() throws Exception {
		if (getSheetName() == null) {
			throw new IllegalStateException("Sheet name must be set!");
		}
		if (startColumnName == null) {
			throw new IllegalStateException("Start column name must be set!");
		}
		if (currentSheet == null) {
			initializeSheet();
		}
		int expectedlastRowIndex = startRowIndex + countRows - 1;
		int endColumnIndex = CellUtil.convertColStringToIndex(startColumnName) + maxRowWith;
		range = CellUtil.buildRange(getSheetName(), startColumnName, endColumnIndex, startRowIndex, expectedlastRowIndex);
		BatchUpdateValuesRequest buvr = new BatchUpdateValuesRequest();
		buvr.setValueInputOption(valueInputOption);
		List<ValueRange> lv = new ArrayList<>();
		// finally set the data range
		getValueRange().setRange(range);
		lv.add(getValueRange());
		buvr.setData(lv);
		Sheets.Spreadsheets.Values.BatchUpdate request = getService().spreadsheets().values().batchUpdate(
				getSpreadsheetId(),
				buvr);
		request.setPrettyPrint(false);
		BatchUpdateValuesResponse response = (BatchUpdateValuesResponse) execute(request);
		countUpdatedRows = response.getTotalUpdatedRows();
		List<UpdateValuesResponse> luvr = response.getResponses();
		if (luvr != null && luvr.size() > 0) {
			updatedRange = response.getResponses().get(0).getUpdatedRange();
			lastRowIndex = CellUtil.getLastRowIndex(updatedRange);
		}
	}

	public void executeAppend() throws Exception {
		if (getSheetName() == null) {
			throw new IllegalStateException("Sheet name must be set!");
		}
		if (startColumnName == null) {
			throw new IllegalStateException("Start column name must be set!");
		}
		initializeSheet(); // update sheet metadata
		int startAppendRow = CellUtil.getLastSheetRowIndex(currentSheet);
		int expectedLastRowIndex = startAppendRow + countRows;
		int endColumnIndex = CellUtil.convertColStringToIndex(startColumnName) + maxRowWith;
		range = CellUtil.buildRange(getSheetName(), startColumnName, endColumnIndex, startAppendRow, expectedLastRowIndex);
		if (currentSheet == null) {
			initializeSheet();
		}
		Sheets.Spreadsheets.Values.Append request = getService().spreadsheets().values().append(
				getSpreadsheetId(),
				range, 
				getValueRange().setRange(range));
		request.setPrettyPrint(false);
		request.setValueInputOption(valueInputOption);
		request.setInsertDataOption(appendOnExistingData ? INSERT_DATA_OPTION_INSERT_ROWS : INSERT_DATA_OPTION_OVERWRITE);
		AppendValuesResponse response = (AppendValuesResponse) execute(request);
		UpdateValuesResponse ur = response.getUpdates();
		if (ur != null) {
			countUpdatedRows = ur.getUpdatedRows();
			updatedRange = ur.getUpdatedRange();
			lastRowIndex = CellUtil.getLastRowIndex(updatedRange);
		} else {
			countUpdatedRows = 0;
			updatedRange = null;
		}
	}

	public void executeDeleteRowsAfterLastWrittenRow() throws Exception {
		if (currentSheet == null) {
			throw new IllegalStateException("No current sheet available!");
		}
		int lastSheetRowIndex = CellUtil.getLastSheetRowIndex(currentSheet);
		int StartRowIndexForDelete = lastRowIndex + 1;
		if (StartRowIndexForDelete <= lastSheetRowIndex) {
			executeDeleteRows(StartRowIndexForDelete, null);
			initializeSheet(); // to refresh the current sheet info
			lastRowIndex = CellUtil.getLastSheetRowIndex(currentSheet);
		}
	}

	public void executeDeleteRows(Integer startRowIndex, Integer endRowIndex) throws Exception {
		if (currentSheet == null) {
			initializeSheet();
		}
		if (startRowIndex != null) {
			startRowIndex = startRowIndex - 1;
		}
		if (endRowIndex != null) {
			endRowIndex = endRowIndex - 1;
		}
		int sheetId = currentSheet.getProperties().getSheetId();
		BatchUpdateSpreadsheetRequest batchRequest = new BatchUpdateSpreadsheetRequest();
        Request request = new Request();
        request.setDeleteDimension(
        	new DeleteDimensionRequest()
                .setRange(new DimensionRange()
                        .setSheetId(sheetId)
                        .setDimension("ROWS")
                        .setStartIndex(startRowIndex)
                        .setEndIndex(endRowIndex)
                )
        );
        List<Request> requests = new ArrayList<>();
        requests.add(request);
        batchRequest.setRequests(requests);
	    Sheets.Spreadsheets.BatchUpdate deleteRequest = getService()
	    		.spreadsheets()
	    		.batchUpdate(getSpreadsheetId(), batchRequest);
	    deleteRequest.execute();
	}

	public String getDocumentTitle() {
		return documentTitle;
	}

	public void setDocumentTitle(String documentTitle) {
		if (documentTitle != null && documentTitle.trim().isEmpty() == false) {
			this.documentTitle = documentTitle;
		}
	}

	public int getCountUpdatedRows() {
		if (countUpdatedRows != null) {
			return countUpdatedRows;
		} else {
			return 0;
		}
	}

	public int getCountWrittenRows() {
		return countRows;
	}
	
	public String getUpdatedRange() {
		return updatedRange;
	}
	
	public void setStartRowIndex(Integer startRowIndex) {
		if (startRowIndex != null) {
			this.startRowIndex = startRowIndex;
		}
	}

	public void setStartColumnName(String startColumnName) {
		if (CellUtil.isEmpty(startColumnName) == false) {
			this.startColumnName = startColumnName;
		}
	}

	public void setStartColumnName(Integer startSheetColumnIndex) {
		if (startSheetColumnIndex != null) {
			this.startColumnName = CellUtil.convertNumToColString(startSheetColumnIndex);
		}
	}

	public boolean isAppendOnExistingData() {
		return appendOnExistingData;
	}

	public void setAppendOnExistingData(boolean appendOnExistingData) {
		this.appendOnExistingData = appendOnExistingData;
	}

	public String getValueInputOption() {
		return valueInputOption;
	}

	public void setValueInputOption(String valueInputOption) {
		if (valueInputOption != null && valueInputOption.trim().isEmpty() == false) {
			if ("INPUT_VALUE_OPTION_UNSPECIFIED".equals(valueInputOption) || "RAW".equals(valueInputOption) || "USER_ENTERED".equals(valueInputOption)) {
				this.valueInputOption = valueInputOption;
			} else {
				throw new IllegalArgumentException("Invalid value for valueInputOption: " + valueInputOption + ". Only: INPUT_VALUE_OPTION_UNSPECIFIED or RAW or USER_ENTERED allowed");
			}
		}
	}

	public int getLastRowIndex() {
		return lastRowIndex;
	}

}