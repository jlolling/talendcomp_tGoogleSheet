package de.jlo.talendcomp.google.sheet;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Clock;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.Sheets.Spreadsheets;
import com.google.api.services.sheets.v4.SheetsRequest;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.Sheet;
import com.google.api.services.sheets.v4.model.Spreadsheet;


public abstract class GoogleSheet {
	
	private static Logger logger = LoggerFactory.getLogger(GoogleSheet.class);
	private static final Map<String, GoogleSheet> clientCache = new HashMap<String, GoogleSheet>();
	private HttpTransport HTTP_TRANSPORT = null;
	private final JsonFactory JSON_FACTORY = new JacksonFactory();
	private File keyFile; // *.p12 key file is needed
	private String clientSecretFile = null;
	private String accountEmail;
	private String applicationName = null;
	private boolean useServiceAccount = true;
	private boolean useApplicationClientID = false;
	private String credentialDataStoreDir = null;
	private long timeMillisOffsetToPast = 10000;
	private int timeoutInSeconds = 120;
	private int maxRetriesInCaseOfErrors = 1;
	private int currentAttempt = 0;
	private long innerLoopWaitInterval = 100;
	private int errorCode = 0;
	private String errorMessage = null;
	// Sheet specific members
	private String spreadsheetId = null;
	private Sheets sheetService = null;
	private Locale defaultLocale = null;
	private boolean debug = false;
	private String sheetName = null;
	
	public GoogleSheet() throws Exception {
		HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
	}
	
	public static void putIntoCache(String key, GoogleSheet client) {
		clientCache.put(key, client);
	}
	
	public static GoogleSheet getFromCache(String key) {
		return clientCache.get(key);
	}
	
	protected Sheets getService() {
		if (sheetService == null) {
			throw new IllegalStateException("Sheets service not initialized! Call initializeService() before.");
		}
		return sheetService;
	}

	public void setKeyFile(String file) {
		keyFile = new File(file);
	}

	public void setAccountEmail(String email) {
		accountEmail = email;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	public void setUseServiceAccount(boolean useServiceAccount) {
		this.useServiceAccount = useServiceAccount;
	}

	public void setClientSecretFile(String clientSecretFile) {
		this.clientSecretFile = clientSecretFile;
	}
	
	private Credential authorizeWithServiceAccount() throws Exception {
		debug("Authorize with service account...");
		if (keyFile == null) {
			throw new Exception("KeyFile not set!");
		}
		if (keyFile.canRead() == false) {
			throw new IOException("keyFile:" + keyFile.getAbsolutePath()
					+ " is not readable");
		}
		if (accountEmail == null || accountEmail.isEmpty()) {
			throw new Exception("account email cannot be null or empty");
		}
		// Authorization.
		return new GoogleCredential.Builder()
				.setTransport(HTTP_TRANSPORT)
				.setJsonFactory(JSON_FACTORY)
				.setServiceAccountId(accountEmail)
				.setServiceAccountScopes(Arrays.asList(SheetsScopes.SPREADSHEETS,SheetsScopes.DRIVE_FILE,SheetsScopes.DRIVE))
				.setServiceAccountPrivateKeyFromP12File(keyFile)
				.setClock(new Clock() {
					@Override
					public long currentTimeMillis() {
						// we must be sure, that we are always in the past from Googles point of view
						// otherwise we get an "invalid_grant" error
						return System.currentTimeMillis() - timeMillisOffsetToPast;
					}
				})
				.build();
	}

	private Credential authorizeWithClientSecret() throws Exception {
		debug("Authorize with client-ID...");
		if (clientSecretFile == null) {
			throw new IllegalStateException("client secret file is not set");
		}
		File secretFile = new File(clientSecretFile);
		if (secretFile.exists() == false) {
			throw new Exception("Client secret file:" + secretFile.getAbsolutePath() + " does not exists or is not readable.");
		}
		Reader reader = new FileReader(secretFile);
		// Load client secrets.
		GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, reader);
		try {
			reader.close();
		} catch (Throwable e) {}
		// Checks that the defaults have been replaced (Default =
		// "Enter X here").
		if (clientSecrets.getDetails().getClientId().startsWith("Enter")
				|| clientSecrets
					.getDetails()
					.getClientSecret()
					.startsWith("Enter ")) {
			throw new Exception("The client secret file does not contains the credentials. At first you have to pass the web based authorization process!");
		}
		credentialDataStoreDir= secretFile.getParent() + "/" + clientSecrets.getDetails().getClientId() + "/";
		File credentialDataStoreDirFile = new File(credentialDataStoreDir);             
		if (credentialDataStoreDirFile.exists() == false && credentialDataStoreDirFile.mkdirs() == false) {
			throw new Exception("Credentedial data dir does not exists or cannot created:" + credentialDataStoreDir);
		}
		debug("Credentedial data dir: " + credentialDataStoreDirFile.getAbsolutePath());
		FileDataStoreFactory fdsf = new FileDataStoreFactory(credentialDataStoreDirFile);
		// Set up authorization code flow.
		GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
				HTTP_TRANSPORT, 
				JSON_FACTORY, 
				clientSecrets, 
				Arrays.asList(SheetsScopes.SPREADSHEETS,SheetsScopes.DRIVE_FILE,SheetsScopes.DRIVE))
			.setDataStoreFactory(fdsf)
			.setClock(new Clock() {
				@Override
				public long currentTimeMillis() {
					// we must be sure, that we are always in the past from Googles point of view
					// otherwise we get an "invalid_grant" error
					return System.currentTimeMillis() - timeMillisOffsetToPast;
				}
			})
			.build();
		// Authorize.
		return new AuthorizationCodeInstalledApp(
				flow,
				new LocalServerReceiver()).authorize(accountEmail);
	}

	/**
	 * Initialize the Drive service client
	 * Depending of the settings a service account or a client-Id for native applications will be used
	 * @throws Exception
	 */
	public void initializeClient() throws Exception {
		// Authorization.
		final Credential credential;
		if (useServiceAccount) {
			credential = authorizeWithServiceAccount();
		} else if (useApplicationClientID) {
			credential = authorizeWithClientSecret();
		} else {
			throw new IllegalStateException("No authorisation method set!");
		}
		sheetService = new Sheets.Builder(
				HTTP_TRANSPORT, 
				JSON_FACTORY, 	
				new HttpRequestInitializer() {
  					@Override
					public void initialize(final HttpRequest httpRequest) throws IOException {
						credential.initialize(httpRequest);
						httpRequest.setConnectTimeout(timeoutInSeconds * 1000);
						httpRequest.setReadTimeout(timeoutInSeconds * 1000);
					}
				})
	     		.setApplicationName(applicationName)
	     		.build();
	}
	
	public void info(String message) {
		if (logger != null) {
			logger.info(message);
		} else {
			System.out.println("INFO: " + message);
		}
	}
	
	public void debug(String message) {
		if (debug) {
			if (logger != null && logger.isDebugEnabled()) {
				logger.debug(message);
			} else {
				System.out.println("DEBUG: " + message);
			}
		} 
	}

	public void warn(String message) {
		if (logger != null) {
			logger.warn(message);
		} else {
			System.err.println("WARN: " + message);
		}
	}
	
	public void error(String message) {
		error(message, null);
	}

	public void error(String message, Exception e) {
		if (logger != null) {
			if (e != null) {
				logger.error(message, e);
			} else {
				logger.error(message);
			}
		} else {
			System.err.println("ERROR: " + message);
		}
	}

	/**
	 * sets the maximum attempts to execute a request in case of errors
	 * @param maxRetriesInCaseOfErrors
	 */
	public void setMaxRetriesInCaseOfErrors(Integer maxRetriesInCaseOfErrors) {
		if (maxRetriesInCaseOfErrors != null) {
			this.maxRetriesInCaseOfErrors = maxRetriesInCaseOfErrors;
		}
	}

	public int getLastHttpStatusCode() {
		return errorCode;
	}

	public String getLastHttpStatusMessage() {
		return errorMessage;
	}

	public int getMaxRetriesInCaseOfErrors() {
		return maxRetriesInCaseOfErrors;
	}

	public String getSpreadsheetId() {
		if (spreadsheetId == null || spreadsheetId.trim().isEmpty()) {
			throw new IllegalStateException("spreadsheetId (or fileId) not set!");
		}
		return spreadsheetId;
	}

	public void setSpreadsheetId(String spreadsheetId) {
		if (spreadsheetId == null || spreadsheetId.trim().isEmpty()) {
			throw new IllegalArgumentException("spreadsheetId cannot be null or empty!");
		}
		this.spreadsheetId = spreadsheetId;
	}

	public boolean isUseApplicationClientID() {
		return useApplicationClientID;
	}

	public void setUseApplicationClientID(boolean useApplicationClientID) {
		this.useApplicationClientID = useApplicationClientID;
	}

	public int getTimeoutInSeconds() {
		return timeoutInSeconds;
	}

	public void setTimeoutInSeconds(Integer timeoutInSeconds) {
		if (timeoutInSeconds != null) {
			this.timeoutInSeconds = timeoutInSeconds;
		}
	}

	public long getTimeMillisOffsetToPast() {
		return timeMillisOffsetToPast;
	}

	public void setTimeMillisOffsetToPast(Long timeMillisOffsetToPast) {
		if (timeMillisOffsetToPast != null) {
			this.timeMillisOffsetToPast = timeMillisOffsetToPast;
		}
	}

	protected com.google.api.client.json.GenericJson execute(SheetsRequest<?> request) throws IOException {
		try {
			Thread.sleep(innerLoopWaitInterval);
		} catch (InterruptedException e) {}
		com.google.api.client.json.GenericJson response = null;
		int waitTime = 1000;
		for (currentAttempt = 0; currentAttempt < maxRetriesInCaseOfErrors; currentAttempt++) {
			errorCode = 0;
			try {
				if (logger.isDebugEnabled()) {
					debug("Request: " + request.toString());
					debug("Request body: " + request.getJsonContent());
				}
				response = (GenericJson) request.execute();
				if (logger.isDebugEnabled()) {
					debug("Response: " + response.toString());
				}
				break;
			} catch (IOException ge) {
				warn("Got error:" + ge.getMessage());
				if (ge instanceof HttpResponseException) {
					errorCode = ((HttpResponseException) ge).getStatusCode();
					errorMessage = ((HttpResponseException) ge).getMessage();
				}
				if (ExceptionUtil.canBeIgnored(ge) == false) {
					error("Stop processing because of the error does not allow a retry.");
					throw ge;
				}
				if (currentAttempt == (maxRetriesInCaseOfErrors - 1)) {
					error("All retries of the request have been failed:" + ge.getMessage(), ge);
					throw ge;
				} else {
					// wait
					try {
						debug("Retry request in " + waitTime + "ms");
						Thread.sleep(waitTime);
					} catch (InterruptedException ie) {}
					int random = (int) Math.random() * 500;
					waitTime = (waitTime * 2) + random;
				}
			}
		}
		return response;
	}

	protected Locale createLocale(String locale) {
		int p = locale.indexOf('_');
		String language = locale;
		String country = "";
		if (p > 0) {
			language = locale.substring(0, p);
			country = locale.substring(p);
		}
		return new Locale(language, country);
	}

	public void setDefaultLocale(String localeStr) {
		if (localeStr != null && localeStr.trim().isEmpty() == false) {
			defaultLocale = createLocale(localeStr);
		}
	}
	
	public Locale getDefaultLocale() {
		if (defaultLocale == null) {
			defaultLocale = Locale.ENGLISH;
		}
		return defaultLocale;
	}

	public String getSheetName() {
		return sheetName;
	}

	public void setSheetName(String sheetName) {
		if (CellUtil.isEmpty(sheetName) == false) {
			this.sheetName = sheetName;
		}
	}
	
	public List<Sheet> listSheets() throws Exception {
		Spreadsheets.Get reqGetSheets = getService().spreadsheets().get(getSpreadsheetId());
		reqGetSheets.setPrettyPrint(false);
		reqGetSheets.setIncludeGridData(false);
		Spreadsheet spreadSheet = (Spreadsheet) execute(reqGetSheets);
		List<Sheet> listSheets = spreadSheet.getSheets();
		return listSheets;
	}

}
