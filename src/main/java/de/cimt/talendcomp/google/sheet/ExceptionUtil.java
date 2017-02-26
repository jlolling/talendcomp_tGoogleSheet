/**
 * Copyright 2015 Jan Lolling jan.lolling@gmail.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.cimt.talendcomp.google.sheet;

import java.io.IOException;
import java.util.List;

import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonError.ErrorInfo;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;

public class ExceptionUtil {
	
	private static IgnorableError[] listIgnorableErrors = {
		new IgnorableError(403, "userRateLimitExceeded"),
		new IgnorableError(403, "quotaExceeded"),
		new IgnorableError(405, null),
		new IgnorableError(500, null),
		new IgnorableError(503, null)
	};
	
	public static boolean canBeIgnored(IOException e) {
		boolean ignore = false;
		if (e instanceof GoogleJsonResponseException) {
			GoogleJsonResponseException gre = (GoogleJsonResponseException) e;
			GoogleJsonError gje = gre.getDetails();
			if (gje != null) {
				List<ErrorInfo> errors = gje.getErrors();
				if (errors != null && errors.isEmpty() == false) {
					ErrorInfo ei = errors.get(0);
					for (IgnorableError error : listIgnorableErrors) {
						if (error.code == gre.getStatusCode()) {
							if (error.reason == null
									|| error.reason.equals(ei.getReason())) {
								ignore = true;
								break;
							}
						}
					}
				}
			}
		}
		return ignore;
	}
	
	public static class IgnorableError {
		
		public IgnorableError(int code, String reason) {
			this.code = code;
			this.reason = reason;
		}
		
		private int code = 0;
		private String reason = null;
		
		public int getCode() {
			return code;
		}
		public String getReason() {
			return reason;
		}
	}


}
