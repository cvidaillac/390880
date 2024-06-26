package com.sgdbf.pop3p.groovy

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class LocaleUtility {
	def static LogFile log_file=null;
	
	public static java.util.Locale getLocale() {
		return new java.util.Locale("fra", "FR");
	}
	
	public static ZoneId getLocalZoneId() {
		return ZoneId.of("Europe/Paris");
	}
	
	public static Date translateLocalDateTimeToDate(LocalDateTime local_date_time) {
		def Date date_at_zone = null;

		if (local_date_time != null) {
			date_at_zone = Date.from(local_date_time.atZone(getLocalZoneId()).toInstant());
		}
		
		return date_at_zone;
	}

	public static Date translateLocalDateToDate(LocalDate local_date) {
		def Date date_at_zone = null;

		if (local_date != null) {
			date_at_zone = Date.from(local_date.atStartOfDay(getLocalZoneId()).toInstant());
		}
		
		return date_at_zone;
	}

	public static String translateExcelFormatToLocale(String formatString, java.util.Locale target_locale) {
		String localized_format_string = formatString;
		
		if (target_locale.getISO3Language() == "fra") {
			int nb_commas = StringUtility.countNumberOfOccurences(formatString, ",");
			if (nb_commas>1) {
				logDebug("Found " + String.valueOf(nb_commas) + " withing format string [" + formatString + "], replacing them");
				localized_format_string = formatString.replace(',', ' ').replace('.', ',');
				logDebug("New localized format: " + localized_format_string);
			}
		}
	
		return localized_format_string;
	}
	
	private static LogFile getLogFile() {
		if (log_file == null) {
			log_file = LogFile.getLogFile("LocaleUtility");
		}
		
		return log_file;
	}
	
	//Function LogMessage
	def private static void logDebug(String message) {
		getLogFile().debug("LocaleUtility", message);
	}
}
