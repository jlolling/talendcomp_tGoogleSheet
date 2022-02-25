package de.jlo.talendcomp.google.sheet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class TestGoogleSheetInput {

	private Map<String, Object> globalMap = new HashMap<>();
	
	//@Before
	public void testIntializeWithClientID() throws Exception {
		// use own client
		de.jlo.talendcomp.google.sheet.GoogleSheetInput tGoogleSheetInput_1 = null;
		if (tGoogleSheetInput_1 == null) {
			tGoogleSheetInput_1 = new de.jlo.talendcomp.google.sheet.GoogleSheetInput();
			// create new drive client
			tGoogleSheetInput_1.setUseServiceAccount(false);
			tGoogleSheetInput_1.setUseApplicationClientID(true);
			// setup client with client-Id for native applications
			tGoogleSheetInput_1.setAccountEmail("jan.lolling@gmail.com");
			tGoogleSheetInput_1.setClientSecretFile("/var/testdata/ga/config/client_secret_503880615382-a7rop1easl2maqbul8u7arnd42hgiolu.apps.googleusercontent.com.json");
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
				e.printStackTrace();
				throw e;
			}
		} // (tGoogleDrive_2 == null)
		globalMap.put("tGoogleSheetInput_1", tGoogleSheetInput_1);
		tGoogleSheetInput_1.setMaxRetriesInCaseOfErrors(5);
		assertTrue(true);
	}
	
	@Before
	public void testIntializeWithServiceAccount() throws Exception {
		// use own client
		de.jlo.talendcomp.google.sheet.GoogleSheetInput tGoogleSheetInput_1 = null;
		if (tGoogleSheetInput_1 == null) {
			tGoogleSheetInput_1 = new de.jlo.talendcomp.google.sheet.GoogleSheetInput();
			// create new drive client
			tGoogleSheetInput_1.setUseServiceAccount(true);
			tGoogleSheetInput_1.setUseApplicationClientID(false);
			// setup client with client-Id for native applications
			tGoogleSheetInput_1.setAccountEmail("digitalreporting-gsheet@digirep-gsheet-to-datalab.iam.gserviceaccount.com");
			tGoogleSheetInput_1.setKeyFile(
					"/Users/jan/development/testdata/ga/config/digirep-gsheet-to-datalab-f42f5c8957c4.p12");
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
				e.printStackTrace();
				throw e;
			}
		} // (tGoogleDrive_2 == null)
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
		de.jlo.talendcomp.google.sheet.GoogleSheetInput gs = (GoogleSheetInput) globalMap.get("tGoogleSheetInput_1");
		String spreadsheetId = "1Li7rq_SIbmSZSWnt9lD4QzCiJ9IIq4vPxVXxeKdQo1U_";
		gs.setSpreadsheetId(spreadsheetId);
		gs.setSheetName("Sheet1");
		gs.setStartRowIndex(3);
		gs.setDataColumnPosition(0, "G");
		gs.setDataColumnPosition(1, "B");
		gs.setDataColumnPosition(2, "C");
		gs.setDataColumnPosition(3, "D");
		gs.setDataColumnPosition(4, "E");
		gs.setDataColumnPosition(5, "F");
		gs.executeQuery();
		assertEquals("Range does not match", "Sheet1!A3:G", gs.getRange());
		assertEquals(50000, gs.getCountRows());
		int index = 0;
		while (gs.next()) {
			index++;
			if (index > 10) {
				break;
			}
			Double a = gs.getDoubleCellValue(0, true, false);
			Date b = null;
			try {
				b = gs.getDateCellValue(1, true, false, null);
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}
			String c = gs.getStringCellValue(2, true, true, true);
			Double d = gs.getDoubleCellValue(3, true, false);
			Boolean e = gs.getBooleanCellValue(4, true, false);
			Long f = gs.getLongCellValue(5, true, false);
			System.out.println(gs.getCurrentRowIndex() + " --------------------- empty: " + gs.isRowEmpty());
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
		de.jlo.talendcomp.google.sheet.GoogleSheetInput gs = (GoogleSheetInput) globalMap.get("tGoogleSheetInput_1");
		String spreadsheetId = "1unqwDlz1GrPpVUjET-JkUA0FaXkaqMGDX2UV6ll8at0";
		gs.setSpreadsheetId(spreadsheetId);
		gs.setSheetName("Sheet1");
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
	public void testFetchValuesById2() throws Exception {
		de.jlo.talendcomp.google.sheet.GoogleSheetInput gs = (GoogleSheetInput) globalMap.get("tGoogleSheetInput_1");
		String spreadsheetId = "1CE-AFdtttl5t5V3_JuIHmOSc-Agkj8yjIQciMdhGkco";
		gs.setSpreadsheetId(spreadsheetId);
		gs.setSheetName("FIN KPIs");
		gs.setStartRowIndex(3);
		gs.setDataColumnPosition(0, 0);
		gs.setDataColumnPosition(1, 1);
		gs.setDataColumnPosition(2, 2);
		gs.setDataColumnPosition(3, 3);
		gs.setDataColumnPosition(4, 4);
		gs.executeQuery();
		int index = 0;
		while (gs.next()) {
			index++;
			if (index > 10) {
				break;
			}
			String a = gs.getStringCellValue(0, true, false, true);
			String b = gs.getStringCellValue(1, true, false, true);
			Double c = gs.getDoubleCellValue(2, true, false);
			Integer d = gs.getIntegerCellValue(3, true, false);
			Integer e = gs.getIntegerCellValue(4, true, false);
			System.out.println(gs.getCurrentRowIndex() + " --------------------- empty: " + gs.isRowEmpty());
			System.out.println(a);
			System.out.println(b);
			System.out.println(c);
			System.out.println(d);
			System.out.println(e);
		}
	}

	@Test
	public void testFetchValuesColumnConfigByHeader() throws Exception {
		de.jlo.talendcomp.google.sheet.GoogleSheetInput gs = (GoogleSheetInput) globalMap.get("tGoogleSheetInput_1");
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
