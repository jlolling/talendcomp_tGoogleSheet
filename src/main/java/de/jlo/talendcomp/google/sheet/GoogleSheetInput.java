package de.jlo.talendcomp.google.sheet;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.api.services.sheets.v4.Sheets.Spreadsheets.Values;
import com.google.api.services.sheets.v4.model.ValueRange;

public class GoogleSheetInput extends GoogleSheet {

	private List<List<Object>> values = null;
	private Map<Integer, String> namesFromSchema = new HashMap<Integer, String>();
	private Map<Integer, Boolean> ignoreMissingMap = new HashMap<Integer, Boolean>();
	private Map<Integer, Integer> columnIndexes = new HashMap<Integer, Integer>();
	private Set<Integer> missingColumns = new TreeSet<Integer>();
	private boolean findColumnByHeader = false;
	private boolean findHeaderPosByRegex = false;
	private List<Object> currentRow = null;
	private Map<Integer, Object> lastValueMap = new HashMap<Integer, Object>();
	private int countRows = -1;
	private int currentRowIndex = -1;
	private TypeUtil typeUtil = new TypeUtil();
	private String range = null;
	private int startRowIndex = 1;
	private Integer lastRowIndex = null;
	private String startColumnName = "A";
	private int lastColumnIndex = -1;
	private List<String> headerRow = null;
	private Integer limit = null;
	
	public GoogleSheetInput() throws Exception {
		super();
	}
	
	public Integer getLastRowIndex() {
		if (limit != null && limit.intValue() > 0) {
			return startRowIndex + limit.intValue();
		} else {
			return lastRowIndex;
		}
	}
	
	public void executeQuery() throws Exception {
		debug("Execute query...");
		range = CellUtil.buildRange(getSheetName(), startColumnName, lastColumnIndex, startRowIndex, getLastRowIndex());
		if (range == null || range.trim().isEmpty()) {
			throw new Exception("No range set. Please refer to Google Spreadsheet A1-notation for ranges!");
		}
		debug("    spreadsheet-Id: " + getSpreadsheetId());
		debug("    startRowIndex: " + startRowIndex);
		debug("    range: " + range);
		Values.Get reqGetValues = getService().spreadsheets().values().get(getSpreadsheetId(), range);
		reqGetValues.setPrettyPrint(false);
		reqGetValues.setMajorDimension("ROWS");
		reqGetValues.setDateTimeRenderOption("FORMATTED_STRING");
		reqGetValues.setValueRenderOption("UNFORMATTED_VALUE");
		ValueRange valueRange = (ValueRange) execute(reqGetValues);
		values = valueRange.getValues();
		countRows = values.size();
		debug("    count received rows: " + countRows);
		currentRow = null;
		currentRowIndex = -1;
		if (findColumnByHeader) {
			fetchHeaderRow();
			configColumnPositions();
		}
		lastValueMap = new HashMap<Integer, Object>();
		debug("    first data row index: " + (getCurrentRowIndex() + 1));
	}
	
	public void reset() {
		countRows = -1;
		startColumnName = "A";
		startRowIndex = 1;
		range = null;
		currentRowIndex = -1;
		values = null;
	}

	public void fetchHeaderRow() {
		currentRowIndex++;
		debug("Fetch header row at: " + getCurrentRowIndex());
		headerRow = new ArrayList<String>();
		if (values == null) {
			throw new IllegalStateException("No values are retrieved! Call retrieveValues before.");
		}
		if (values.size() > 0) {
			List<Object> row = values.get(currentRowIndex);
			for (Object val : row) {
				headerRow.add(String.valueOf(val));
			}
		}
		
	}
	
	public boolean next() {
		if (countRows == -1) {
			throw new IllegalStateException("No values has been fetched before!");
		}
		currentRowIndex++;
		if (currentRowIndex < countRows) {
			currentRow = values.get(currentRowIndex);
			return true;
		} else {
			currentRow = null;
			return false;
		}
	}
	
	public int getCurrentRowIndex() {
		return startRowIndex + currentRowIndex;
	}

	public int getCountRows() {
		return countRows;
	}
	
	protected int getCellIndex(int columnIndex) {
		Integer cellIndex = columnIndexes.get(columnIndex);
		if (cellIndex == null) {
			cellIndex = columnIndex;
		}
		return cellIndex;
	}
	
	public boolean isRowEmpty() {
		return currentRow.size() == 0;
	}
	
	public boolean isCellValueEmpty(int schemaColumnIndex) {
		try {
			Object value = getRawCellValue(schemaColumnIndex, true, false);
			return value == null;
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	
	public int getCurrentRowSize() {
		return currentRow.size();
	}
	
	private Object getRawCellValue(int schemaColumnIndex, boolean isNullable, boolean useLast) throws Exception {
		if (currentRow == null) {
			throw new IllegalStateException("No current row available. Check if there are results received and take care you call next() before.");
		}
		Object current = null;
		if (schemaColumnIndex < currentRow.size()) {
			Integer sheetColumnIndex = columnIndexes.get(schemaColumnIndex);
			if (sheetColumnIndex == null) {
				throw new IllegalStateException("No sheetColumnIndex found for schemaColumnIndex: " +schemaColumnIndex);
			}
			if (sheetColumnIndex < currentRow.size()) {
				current = currentRow.get(sheetColumnIndex);
			}
			// Google returns empty Strings in case of they have to fill the row array with "null" values
			if (current instanceof String) {
				if (((String) current).isEmpty()) {
					current = null;
				}
			}
		}
		Object value = null;
		if (current != null) {
			value = current;
			lastValueMap.put(schemaColumnIndex, value);
		} else if (useLast) {
			value = lastValueMap.get(schemaColumnIndex);
		} else if (isNullable == false) {
			throwNullValueException(schemaColumnIndex);
		}
		return value;
	}

	private void throwNullValueException(int columnIndex) throws Exception {
		throw new Exception("Null value detected in the not nullable column with index: " + columnIndex + " in the row: " + currentRowIndex);
	}
	
	public String getStringCellValue(int schemaColumnIndex, boolean isNullable, boolean useLast, boolean trim) throws Exception {
		Object value = getRawCellValue(schemaColumnIndex, isNullable, useLast);
		if (value != null) {
			String result = typeUtil.convertToString(value, null);
			if (trim && result != null) {
				return result.trim();
			} else {
				return result;
			}
		}
		return null;
	}
	
	public Short getShortCellValue(int schemaColumnIndex, boolean isNullable, boolean useLast) throws Exception {
		Object value = getRawCellValue(schemaColumnIndex, isNullable, useLast);
		if (value != null) {
			return typeUtil.convertToShort(value, null);
		}
		return null;
	}

	public Integer getIntegerCellValue(int schemaColumnIndex, boolean isNullable, boolean useLast) throws Exception {
		Object value = getRawCellValue(schemaColumnIndex, isNullable, useLast);
		if (value != null) {
			return typeUtil.convertToInteger(value, null);
		}
		return null;
	}

	public Long getLongCellValue(int schemaColumnIndex, boolean isNullable, boolean useLast) throws Exception {
		Object value = getRawCellValue(schemaColumnIndex, isNullable, useLast);
		if (value != null) {
			return typeUtil.convertToLong(value, null);
		}
		return null;
	}

	public BigDecimal getBigDecimalCellValue(int schemaColumnIndex, boolean isNullable, boolean useLast) throws Exception {
		Object value = getRawCellValue(schemaColumnIndex, isNullable, useLast);
		if (value != null) {
			return typeUtil.convertToBigDecimal(value, null);
		}
		return null;
	}

	public Double getDoubleCellValue(int schemaColumnIndex, boolean isNullable, boolean useLast) throws Exception {
		Object value = getRawCellValue(schemaColumnIndex, isNullable, useLast);
		if (value != null) {
			return typeUtil.convertToDouble(value, null);
		}
		return null;
	}

	public Float getFloatCellValue(int schemaColumnIndex, boolean isNullable, boolean useLast) throws Exception {
		Object value = getRawCellValue(schemaColumnIndex, isNullable, useLast);
		if (value != null) {
			return typeUtil.convertToFloat(value, null);
		}
		return null;
	}

	public Date getDateCellValue(int schemaColumnIndex, boolean isNullable, boolean useLast, String pattern) throws Exception {
		Object value = getRawCellValue(schemaColumnIndex, isNullable, useLast);
		if (value != null) {
			return typeUtil.convertToDate(value, pattern, getDefaultLocale());
		}
		return null;
	}

	public Boolean getBooleanCellValue(int schemaColumnIndex, boolean isNullable, boolean useLast) throws Exception {
		Object value = getRawCellValue(schemaColumnIndex, isNullable, useLast);
		if (value != null) {
			return typeUtil.convertToBoolean(value, null);
		}
		return null;
	}

	public int getStartRowIndex() {
		return startRowIndex;
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

	public void configColumnPositions() throws Exception {
		if (headerRow != null) {
			debug("Configure column positions...");
			Map<String, Integer> namesFromHeaderRow = new HashMap<String, Integer>();
			for (int i = 0; i < headerRow.size(); i++) {
				String name = headerRow.get(i);
				namesFromHeaderRow.put(name.trim().toLowerCase(), i);
			}
			for (Map.Entry<Integer, String> nameFromSchema : namesFromSchema.entrySet()) {
				Boolean ignoreMissing = ignoreMissingMap.get(nameFromSchema.getKey());
				if (ignoreMissing == null) {
					ignoreMissing = false;
				}
				Integer targetIndex = findPosition(namesFromHeaderRow, nameFromSchema.getValue());
				if (targetIndex != null) {
					columnIndexes.put(nameFromSchema.getKey(), targetIndex);
					debug("    schema column: " + nameFromSchema.getKey() + " new target cell index: " + targetIndex);
				} else if (ignoreMissing) {
					missingColumns.add(nameFromSchema.getKey());
				} else {
					if (findHeaderPosByRegex) {
						throw new Exception("Column with pattern: " + nameFromSchema.getValue() + " does not exists in header: " + namesFromHeaderRow);
					} else {
						throw new Exception("Column with name: " + nameFromSchema.getValue() + " does not exists in header: " + namesFromHeaderRow);
					}
				}
			}
		} else {
			throw new IllegalStateException("Header row not fetched. Take care you have set findColumnByHeader to true");
		}
	}
	
	private Integer findPosition(Map<String, Integer> namesFromHeaderRow, String pattern) {
		if (findHeaderPosByRegex) {
			if (pattern.startsWith("^") == false) {
				pattern = "^" + pattern;
			}
			if (pattern.endsWith("$") == false) {
				pattern = pattern + "$";
			}
			Pattern p = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
			for (Map.Entry<String, Integer> entry : namesFromHeaderRow.entrySet()) {
				String header = entry.getKey();
				Integer index = entry.getValue();
				Matcher m = p.matcher(header);
				if (m.find()) {
					return index;
				}
			}
			return null;
		} else {
			return namesFromHeaderRow.get(pattern.toLowerCase());
		}
	}
	
	private void checkForLastColumnIndex(int columnIndex) {
		if (lastColumnIndex < columnIndex) {
			lastColumnIndex = columnIndex;
		}
	}

	/**
	 * map the value in the row column to an sheet column
	 * @param schemaColumnIndex index (0-based) in the data row (parameter of writeRow method)
	 * @param columnName 'A' or 'BC'
	 */
	public void setDataColumnPositionByHeader(int schemaColumnIndex, String columnName, boolean ignoreMissing) {
		if (columnName != null) {
			namesFromSchema.put(schemaColumnIndex, columnName);
			ignoreMissingMap.put(schemaColumnIndex, ignoreMissing);
		} else {
			checkForLastColumnIndex(schemaColumnIndex);
			columnIndexes.put(schemaColumnIndex, schemaColumnIndex);
		}
	}
	
	/**
	 * map the value in the row column to an sheet column
	 * @param schemaColumnIndex index in the data row (parameter of writeRow method)
	 * @param columnIndex 0 - n
	 */
	public void setDataColumnPosition(int schemaColumnIndex, Integer columnIndex) {
		if (columnIndex != null) {
			checkForLastColumnIndex(columnIndex);
			columnIndexes.put(schemaColumnIndex, columnIndex);
		} else {
			checkForLastColumnIndex(schemaColumnIndex);
			columnIndexes.put(schemaColumnIndex, schemaColumnIndex);
		}
	}

	/**
	 * map the value in the row column to an sheet column
	 * @param schemaColumnIndex index in the data row (parameter of writeRow method)
	 * @param columnIndex 0 - n
	 */
	public void setDataColumnPosition(int schemaColumnIndex, String columnIndex) {
		if (columnIndex != null) {
			int cellColumnIndex = CellUtil.convertColStringToIndex(columnIndex);
			checkForLastColumnIndex(cellColumnIndex);
			columnIndexes.put(schemaColumnIndex, cellColumnIndex);
		} else {
			checkForLastColumnIndex(schemaColumnIndex);
			columnIndexes.put(schemaColumnIndex, schemaColumnIndex);
		}
	}

	public String getRange() {
		return range;
	}

	public boolean isFindColumnByHeader() {
		return findColumnByHeader;
	}

	public void setFindColumnByHeader(boolean findColumnByHeader, String lastColumnToCheck) {
		this.findColumnByHeader = findColumnByHeader;
		if (CellUtil.isEmpty(lastColumnToCheck) == false) {
			int cellColumnIndex = CellUtil.convertColStringToIndex(lastColumnToCheck);
			checkForLastColumnIndex(cellColumnIndex);
		}
	}

	public Integer getLimit() {
		return limit;
	}

	public void setLimit(Integer limit) {
		this.limit = limit;
	}

}
