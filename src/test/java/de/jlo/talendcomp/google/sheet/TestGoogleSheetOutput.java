package de.jlo.talendcomp.google.sheet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

public class TestGoogleSheetOutput {
	
	private Map<String, Object> globalMap = new HashMap<>();

	@Before
	public void testIntializeClient() throws Exception {
		// use own client
		de.jlo.talendcomp.google.sheet.GoogleSheetOutput tGoogleSheetOutput_1 = null;
		if (tGoogleSheetOutput_1 == null) {
			tGoogleSheetOutput_1 = new de.jlo.talendcomp.google.sheet.GoogleSheetOutput();
			// create new drive client
			tGoogleSheetOutput_1.setUseServiceAccount(false);
			tGoogleSheetOutput_1.setUseApplicationClientID(true);
			// setup client with client-Id for native applications
			tGoogleSheetOutput_1.setAccountEmail("jan.lolling@gmail.com");
			tGoogleSheetOutput_1.setClientSecretFile("/Data/Talend/testdata/ga/config/client_secret_503880615382-a7rop1easl2maqbul8u7arnd42hgiolu.apps.googleusercontent.com.json");
			tGoogleSheetOutput_1.setTimeoutInSeconds(240);
			// prevent token validation problems caused by time
			// differences between own host and Google
			tGoogleSheetOutput_1.setTimeMillisOffsetToPast(10000l);
			tGoogleSheetOutput_1.setApplicationName("GoogleSheet Talend Job");
			try {
				// initialize drive client
				tGoogleSheetOutput_1.initializeClient();
			} catch (Exception e) {
				globalMap.put("tGoogleSheetOutput_1_ERROR_MESSAGE", e.getMessage());
				throw e;
			}
		} // (tGoogleDrive_2 == null)
		tGoogleSheetOutput_1.setDebug(true);
		globalMap.put("tGoogleSheetOutput_1", tGoogleSheetOutput_1);
		tGoogleSheetOutput_1.setMaxRetriesInCaseOfErrors(5);
		assertTrue(true);
	}

	@Test
	public void testAppendRows() throws Exception {
		de.jlo.talendcomp.google.sheet.GoogleSheetOutput gs = (GoogleSheetOutput) globalMap.get("tGoogleSheetOutput_1");
		gs.setDebug(true);
		String spreadsheetId = "1unqwDlz1GrPpVUjET-JkUA0FaXkaqMGDX2UV6ll8at0";
		gs.setSpreadsheetId(spreadsheetId);
		gs.setSheetName("Sheet3");
		gs.setStartRowIndex(1);
		int countInserts = 0;
		for (int i = 1; i < 10; i++) {
			gs.addValue("String_value_üöä_" + i, null);
			Date dv = new Date();
			gs.addValue(dv, null);
			gs.addValue((i % 2) == 0 ? true : false, null);
			gs.addValue(i, null);
			gs.addValue(Long.valueOf(i), null);
			gs.addValue(new BigDecimal(i + "." + i), null);
			gs.addRow();
			countInserts++;
		}
		gs.executeAppend();
		int countUpdated = gs.getCountWrittenRows();
		assertEquals(countInserts, countUpdated);
	}

	@Test
	public void testUpdateRows() throws Exception {
		de.jlo.talendcomp.google.sheet.GoogleSheetOutput gs = (GoogleSheetOutput) globalMap.get("tGoogleSheetOutput_1");
		gs.setDebug(false);
		String spreadsheetId = "1unqwDlz1GrPpVUjET-JkUA0FaXkaqMGDX2UV6ll8at0";
		gs.setSpreadsheetId(spreadsheetId);
		gs.setSheetName("Sheet3");
		gs.setStartRowIndex(30);
		Date now = new Date();
		int countInserts = 0;
		for (int i = 1; i < 50000; i++) {
			gs.addValue("String_value_üöä_" + i, null);
			Date dv = new Date();
			gs.addValue(dv, null);
			gs.addValue((i % 2) == 0 ? true : false, null);
			gs.addValue(i, null);
			gs.addValue(Long.valueOf(i), null);
			gs.addValue(new BigDecimal(i + "." + i), null);
			gs.addRow();
			countInserts++;
		}
		gs.executeUpdate();
		int countUpdated = gs.getCountWrittenRows();
		assertEquals(countInserts, countUpdated);
	}

	@Test
	public void testUpdateRowsCreateNewSS() throws Exception {
		de.jlo.talendcomp.google.sheet.GoogleSheetOutput gs = (GoogleSheetOutput) globalMap.get("tGoogleSheetOutput_1");
		gs.setDebug(false);
		gs.setDocumentTitle("Testsheet-2");
		gs.createSheetDocument();
		gs.setSheetName("Sheet3");
		gs.setStartRowIndex(30);
		int countInserts = 0;
		for (int i = 1; i < 50000; i++) {
			gs.addValue("String_value_üöä_" + i, null);
			Date dv = new Date();
			gs.addValue(dv, null);
			gs.addValue((i % 2) == 0 ? true : false, null);
			gs.addValue(i, null);
			gs.addValue(Long.valueOf(i), null);
			gs.addValue(new BigDecimal(i + "." + i), null);
			gs.addRow();
			countInserts++;
		}
		gs.executeUpdate();
		int countUpdated = gs.getCountWrittenRows();
		assertEquals(countInserts, countUpdated);
	}

}
