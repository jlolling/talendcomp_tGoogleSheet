package de.cimt.talendcomp.google.sheet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Before;
import org.junit.Test;

import de.cimt.talendcomp.test.TalendFakeJob;

public class TestGoogleSheetInput extends TalendFakeJob {

	@Before
	public void testIntializeClient() throws Exception {
		// use own client
		de.cimt.talendcomp.google.sheet.GoogleSheetInput tGoogleSheetInput_1 = null;
		if (tGoogleSheetInput_1 == null) {
			tGoogleSheetInput_1 = new de.cimt.talendcomp.google.sheet.GoogleSheetInput();
			// create new drive client
			tGoogleSheetInput_1.setUseServiceAccount(false);
			tGoogleSheetInput_1.setUseApplicationClientID(true);
			// setup client with client-Id for native applications
			tGoogleSheetInput_1.setAccountEmail("jan.lolling@gmail.com");
			tGoogleSheetInput_1.setClientSecretFile("/Volumes/Data/Talend/testdata/ga/config/client_secret_503880615382-ve9ac3176d2acre79tevkirt0v6pa91v.apps.googleusercontent.com.json");
			tGoogleSheetInput_1.setTimeoutInSeconds(240);
			// prevent token validation problems caused by time
			// differences between own host and Google
			tGoogleSheetInput_1.setTimeMillisOffsetToPast(10000l);
			tGoogleSheetInput_1.setApplicationName("GoogleSheet Talend Job");
			try {
				// initialize drive client
				tGoogleSheetInput_1.initializeClient();
			} catch (Exception e) {
				globalMap.put("tGoogleSheetInput_1_ERROR_MESSAGE", e.getMessage());
				throw e;
			}
		} // (tGoogleDrive_2 == null)
		tGoogleSheetInput_1.setDebug(true);
		globalMap.put("tGoogleSheetInput_1", tGoogleSheetInput_1);
		tGoogleSheetInput_1.setMaxRetriesInCaseOfErrors(5);
		assertTrue(true);
	}
	
//	@Test
//	public void testListSheets() throws Exception {
//		de.cimt.talendcomp.google.sheet.GoogleSheetList tGoogleSheetInput_1 = (GoogleSheetList) globalMap.get("tGoogleSheetInput_1");
//		String spreadsheetId = "1unqwDlz1GrPpVUjET-JkUA0FaXkaqMGDX2UV6ll8at0";
//		tGoogleSheetInput_1.setSpreadsheetId(spreadsheetId);
//		List<Sheet> sheetList = tGoogleSheetInput_1.listSheets();
//		for (Sheet sheet : sheetList) {
//			//System.out.println(sheet.toString());
//		}
//		assertTrue(sheetList.size() == 2);
//	}
	
	@Test
	public void testFetchValuesByName() throws Exception {
		de.cimt.talendcomp.google.sheet.GoogleSheetInput gs = (GoogleSheetInput) globalMap.get("tGoogleSheetInput_1");
		gs.setDebug(true);
		String spreadsheetId = "1unqwDlz1GrPpVUjET-JkUA0FaXkaqMGDX2UV6ll8at0";
		gs.setSpreadsheetId(spreadsheetId);
		gs.setSheetName("Sheet2");
		gs.setStartRowIndex(2);
		gs.setDataColumnPosition(0, "G");
		gs.setDataColumnPosition(1, "B");
		gs.setDataColumnPosition(2, "C");
		gs.setDataColumnPosition(3, "D");
		gs.setDataColumnPosition(4, "E");
		gs.setDataColumnPosition(5, "F");
		gs.executeQuery();
		assertEquals("Range does not match", "Sheet2!A2:G", gs.getRange());
		assertEquals(65408, gs.getCountRows());
		int index = 0;
		while (gs.next()) {
			index++;
			if (index > 10) {
				break;
			}
			String a = gs.getStringCellValue(0, true, true, false);
			Date b = null;
			try {
				b = gs.getDateCellValue(1, true, false, null);
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}
			Integer c = gs.getIntegerCellValue(2, true, true);
			Double d = gs.getDoubleCellValue(3, true, false);
			Boolean e = gs.getBooleanCellValue(4, true, false);
			Long f = gs.getLongCellValue(5, true, false);
			System.out.println("--------------------- empty: " + gs.isRowEmpty());
			System.out.println(a);
			System.out.println(b);
			System.out.println(c);
			System.out.println(d);
			System.out.println(e);
			System.out.println(f);
			boolean notEmpty = true;
			if (gs.isRowEmpty() == false) {
				boolean rowHasValues = false;
				if (rowHasValues
						|| gs.isCellValueEmpty(0) == false) {
					rowHasValues = true;
				}
				if (rowHasValues
						|| gs.isCellValueEmpty(1) == false) {
					rowHasValues = true;
				}
				if (rowHasValues
						|| gs.isCellValueEmpty(2) == false) {
					rowHasValues = true;
				}
				if (rowHasValues
						|| gs.isCellValueEmpty(3) == false) {
					rowHasValues = true;
				}
				if (rowHasValues
						|| gs.isCellValueEmpty(4) == false) {
					rowHasValues = true;
				}
				if (rowHasValues
						|| gs.isCellValueEmpty(5) == false) {
					rowHasValues = true;
				}
				notEmpty = rowHasValues;
			} else {
				notEmpty = false;
			}
			if (gs.getCurrentRowIndex() == 5) {
				assertTrue(notEmpty == false);
			} else {
				assertTrue(notEmpty);
			}
		}
		assertEquals(11, index);
	}

	@Test
	public void testFetchValuesById() throws Exception {
		de.cimt.talendcomp.google.sheet.GoogleSheetInput gs = (GoogleSheetInput) globalMap.get("tGoogleSheetInput_1");
		String spreadsheetId = "1unqwDlz1GrPpVUjET-JkUA0FaXkaqMGDX2UV6ll8at0";
		gs.setSpreadsheetId(spreadsheetId);
		gs.setSheetName("Sheet2");
		gs.setStartRowIndex(2);
		gs.setDataColumnPosition(0, 6);
		gs.setDataColumnPosition(1, 1);
		gs.setDataColumnPosition(2, 2);
		gs.setDataColumnPosition(3, 3);
		gs.setDataColumnPosition(4, 4);
		gs.setDataColumnPosition(5, 5);
		gs.executeQuery();
		assertEquals("Range does not match", "Sheet2!A2:G", gs.getRange());
		assertEquals(65408, gs.getCountRows());
		int index = 0;
		while (gs.next()) {
			index++;
			if (index > 10) {
				break;
			}
			String a = gs.getStringCellValue(0, true, true, true);
			Date b = null;
			try {
				b = gs.getDateCellValue(1, true, false, null);
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}
			Integer c = gs.getIntegerCellValue(2, true, true);
			Double d = gs.getDoubleCellValue(3, true, true);
			Boolean e = gs.getBooleanCellValue(4, true, true);
			Long f = gs.getLongCellValue(5, true, true);
			System.out.println("---------------------");
			System.out.println(a);
			System.out.println(b);
			System.out.println(c);
			System.out.println(d);
			System.out.println(e);
			System.out.println(f);
		}
	}

	@Test
	public void testFetchValuesColumnConfigByHeader() throws Exception {
		de.cimt.talendcomp.google.sheet.GoogleSheetInput gs = (GoogleSheetInput) globalMap.get("tGoogleSheetInput_1");
		String spreadsheetId = "1unqwDlz1GrPpVUjET-JkUA0FaXkaqMGDX2UV6ll8at0";
		gs.setSpreadsheetId(spreadsheetId);
		gs.setSheetName("Sheet2");
		gs.setStartRowIndex(1);
		gs.setFindColumnByHeader(true, "G");
		gs.setDataColumnPositionByHeader(0, "Column String", false);
		gs.setDataColumnPositionByHeader(1, "Column Date", false);
		gs.executeQuery();
		assertEquals("Range does not match", "Sheet2!A1:G", gs.getRange());
		assertEquals(65409, gs.getCountRows());
		int index = 0;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		while (gs.next()) {
			index++;
			if (index > 10) {
				break;
			}
			String a = gs.getStringCellValue(0, true, true, true);
			Date b = null;
			try {
				b = gs.getDateCellValue(1, true, false, null);
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}
			System.out.println("---------------------");
			System.out.println(a);
			if (b != null) {
				System.out.println(sdf.format(b));
			}
		}
	}
	
}
