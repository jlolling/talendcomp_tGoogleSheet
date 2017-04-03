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
package de.jlo.talendcomp.google.sheet;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public final class TypeUtil {
	
	private final Map<String, DecimalFormat> numberformatMap = new HashMap<String, DecimalFormat>();
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	
	public TypeUtil() {}
	
	public DecimalFormat getNumberFormat(String localeStr) {
		if (localeStr == null || localeStr.trim().isEmpty()) {
			localeStr = "en_UK";
		}
		DecimalFormat nf = numberformatMap.get(localeStr);
		if (nf == null) {
			Locale locale = new Locale(localeStr);
			nf = (DecimalFormat) NumberFormat.getInstance(locale);
			numberformatMap.put(localeStr, nf);
		}
		return nf;
	}
	
	public Object convertToDatatype(String value, String dataType, String options) throws Exception {
		if ("String".equals(dataType)) {
			if (value == null || value.isEmpty()) {
				return null;
			} else {
				return value;
			}
		} else if ("Integer".equals(dataType)) {
			return convertToInteger(value, options);
		} else if ("Boolean".equals(dataType)) {
			return convertToBoolean(value);
		} else if ("Date".equals(dataType)) {
			return convertToDate(value, options, null);
		} else if ("BigDecimal".equals(dataType)) {
			return convertToBigDecimal(value, options);
		} else if ("Double".equals(dataType)) {
			return convertToDouble(value, options);
		} else if ("Float".equals(dataType)) {
			return convertToFloat(value, options);
		} else if ("Long".equals(dataType)) {
			return convertToLong(value, options);
		} else if ("Short".equals(dataType)) {
			return convertToShort(value, options);
		} else if ("Character".equals(dataType)) {
			return convertToChar(value, options);
		} else {
			throw new Exception("Unsupported dataType:" + dataType);
		}
	}
	
	public Boolean convertToBoolean(String value) throws Exception {
		if (value == null) {
			return null;
		}
		value = value.toLowerCase();
		if ("true".equals(value)) {
			return Boolean.TRUE;
		} else if ("false".equals(value)) {
			return Boolean.FALSE;
		} else if ("t".equals(value)) {
			return Boolean.FALSE;
		} else if ("f".equals(value)) {
			return Boolean.FALSE;
		} else if ("1".equals(value)) {
			return Boolean.TRUE;
		} else if ("0".equals(value)) {
			return Boolean.FALSE;
		} else if ("yes".equals(value)) {
			return Boolean.TRUE;
		} else if ("y".equals(value)) {
			return Boolean.TRUE;
		} else if ("sí".equals(value)) {
			return Boolean.TRUE;
		} else if ("да".equals(value)) {
			return Boolean.TRUE;
		} else if ("no".equals(value)) {
			return Boolean.FALSE;
		} else if ("нет".equals(value)) {
			return Boolean.FALSE;
		} else if ("n".equals(value)) {
			return Boolean.FALSE;
		} else if ("ja".equals(value)) {
			return Boolean.TRUE;
		} else if ("j".equals(value)) {
			return Boolean.TRUE;
		} else if ("nein".equals(value)) {
			return Boolean.FALSE;
		} else if ("oui".equals(value)) {
			return Boolean.TRUE;
		} else if ("non".equals(value)) {
			return Boolean.FALSE;
		} else if ("ok".equals(value)) {
			return Boolean.TRUE;
		} else if ("x".equals(value)) {
			return Boolean.TRUE;
		} else if ("-".equals(value)) {
			return Boolean.FALSE;
		} else if (value != null) {
			return Boolean.FALSE;
		} else {
			return null;
		}
	}

	public Boolean convertToBoolean(Object value, String locale) throws Exception {
		if (value instanceof String) {
			String valueStr = (String) value;
			if (valueStr.isEmpty()) {
				return null;
			}
			return convertToBoolean(valueStr);
		} else if (value instanceof Boolean) {
			return (Boolean) value;
		} else if (value instanceof Number) {
			Number valueNum = (Number) value;
			return valueNum.longValue() > 0;
		} else if (value != null) {
			throw new Exception("Cannot convert: " + value + " of type: " + value.getClass().getName() + " into Boolean");
		} else {
			return null;
		}
	}

	/**
	 * concerts the string format into a Date
	 * @param value
	 * @param pattern
	 * @return the resulting Date
	 */
	public Date convertToDate(Object value, String pattern, Locale local) throws Exception {
		if (value instanceof String) {
			String valueStr = (String) value;
			if (valueStr.isEmpty()) {
				return null;
			}
			try {
				return GenericDateUtil.parseDate(valueStr, local, pattern);
			} catch (Throwable t) {
				throw new Exception("Failed to convert string to date:" + t.getMessage(), t);
			}
		} else if (value instanceof Date) {
			return (Date) value;
		} else if (value != null) {
			throw new Exception("Cannot convert: " + value + " of type: " + value.getClass().getName() + " into Date");
		} else {
			return null;
		}
	}
	
	public String convertToString(Object value, String locale) throws Exception {
		if (value instanceof String) {
			String valueStr = (String) value;
			if (valueStr.isEmpty()) {
				return null;
			}
			return (String) value;
		} else if (value instanceof Number) {
			DecimalFormat decfrm = getNumberFormat(locale);
			return decfrm.format(value);
		} else if (value instanceof Date) {
			return sdf.format((Date) value);
		} else if (value != null) {
			return String.valueOf(value);
		} else {
			return null;
		}
	}

	public Character convertToChar(Object value, String locale) throws Exception {
		if (value instanceof String) {
			String valueStr = (String) value;
			if (valueStr.isEmpty()) {
				return null;
			}
			return valueStr.charAt(0);
		} else if (value instanceof Character) {
			return (Character) value;
		} else if (value != null) {
			throw new Exception("Cannot convert: " + value + " of type: " + value.getClass().getName() + " into Character");
		} else {
			return null;
		}
	}

	public Double convertToDouble(Object value, String locale) throws Exception {
		if (value instanceof String) {
			String valueStr = (String) value;
			if (value == null || valueStr.isEmpty()) {
				return null;
			}
			DecimalFormat decfrm = getNumberFormat(locale);
			decfrm.setParseBigDecimal(false);
			return decfrm.parse(valueStr).doubleValue();
		} else if (value instanceof Number) {
			Number valueNum = (Number) value;
			return valueNum.doubleValue();
		} else if (value != null) {
			throw new Exception("Cannot convert: " + value + " of type: " + value.getClass().getName() + " into Double");
		} else {
			return null;
		}
	}

	public Integer convertToInteger(Object value, String locale) throws Exception {
		if (value instanceof String) {
			String valueStr = (String) value;
			if (value == null || valueStr.isEmpty()) {
				return null;
			}
			DecimalFormat decfrm = getNumberFormat(locale);
			decfrm.setParseBigDecimal(false);
			return decfrm.parse(valueStr).intValue();
		} else if (value instanceof Number) {
			Number valueNum = (Number) value;
			return valueNum.intValue();
		} else if (value != null) {
			throw new Exception("Cannot convert: " + value + " of type: " + value.getClass().getName() + " into Integer");
		} else {
			return null;
		}
	}

	public Short convertToShort(Object value, String locale) throws Exception {
		if (value instanceof String) {
			String valueStr = (String) value;
			if (value == null || valueStr.isEmpty()) {
				return null;
			}
			DecimalFormat decfrm = getNumberFormat(locale);
			decfrm.setParseBigDecimal(false);
			return decfrm.parse(valueStr).shortValue();
		} else if (value instanceof Number) {
			Number valueNum = (Number) value;
			return valueNum.shortValue();
		} else if (value != null) {
			throw new Exception("Cannot convert: " + value + " of type: " + value.getClass().getName() + " into Short");
		} else {
			return null;
		}
	}

	public Float convertToFloat(Object value, String locale) throws Exception {
		if (value instanceof String) {
			String valueStr = (String) value;
			if (value == null || valueStr.isEmpty()) {
				return null;
			}
			DecimalFormat decfrm = getNumberFormat(locale);
			decfrm.setParseBigDecimal(false);
			return decfrm.parse(valueStr).floatValue();
		} else if (value instanceof Number) {
			Number valueNum = (Number) value;
			return valueNum.floatValue();
		} else if (value != null) {
			throw new Exception("Cannot convert: " + value + " of type: " + value.getClass().getName() + " into Float");
		} else {
			return null;
		}
	}

	public Long convertToLong(Object value, String locale) throws Exception {
		if (value instanceof String) {
			String valueStr = (String) value;
			if (value == null || valueStr.isEmpty()) {
				return null;
			}
			DecimalFormat decfrm = getNumberFormat(locale);
			decfrm.setParseBigDecimal(false);
			return decfrm.parse(valueStr).longValue();
		} else if (value instanceof Number) {
			Number valueNum = (Number) value;
			return valueNum.longValue();
		} else if (value != null) {
			throw new Exception("Cannot convert: " + value + " of type: " + value.getClass().getName() + " into Long");
		} else {
			return null;
		}
	}

	public BigDecimal convertToBigDecimal(Object value, String locale) throws Exception {
		if (value instanceof String) {
			String valueStr = (String) value;
			if (valueStr == null || valueStr.isEmpty()) {
				return null;
			}
			DecimalFormat decfrm = getNumberFormat(locale);
			decfrm.setParseBigDecimal(true);
			ParsePosition pp = new ParsePosition(0);
			return (BigDecimal) decfrm.parse(valueStr, pp);
		} else if (value instanceof BigDecimal) {
			return (BigDecimal) value;
		} else if (value instanceof Long) {
			Number valueNum = (Number) value;
			return new BigDecimal(valueNum.longValue());
		} else if (value instanceof Integer) {
			Number valueNum = (Number) value;
			return new BigDecimal(valueNum.intValue());
		} else if (value instanceof Short) {
			Number valueNum = (Number) value;
			return new BigDecimal(valueNum.shortValue());
		} else if (value instanceof Double) {
			Number valueNum = (Number) value;
			return new BigDecimal(valueNum.doubleValue());
		} else if (value instanceof Float) {
			Number valueNum = (Number) value;
			return new BigDecimal(valueNum.floatValue());
		} else if (value != null) {
			throw new Exception("Cannot convert: " + value + " of type: " + value.getClass().getName() + " into BigDecimal");
		} else {
			return null;
		}
	}

	public double roundScale(double value, int scale) {
    	double d = Math.pow(10, scale);
        return Math.round(value * d) / d;
    }
 
}
