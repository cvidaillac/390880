package com.sgdbf.pop3p.groovy

class StringUtility {
	def static LogFile log_file=null;
	
	public static String stripAccents(String input_str) {
		char[] out = new char[input_str.length()];
		input_str = java.text.Normalizer.normalize(input_str, java.text.Normalizer.Form.NFD);
		int j = 0;
		int nb_cars = input_str.length()
		for (int i=0; i<nb_cars; ++i) {
			char c = input_str.charAt(i);
			if (c <= '\u007F') {
				out[j++] = c;
			}
		}
		return new String(out, 0, j);
	}
	
	public static String stripAccentsAndSpaces(String input_str) {
		char[] out = new char[input_str.length()];
		input_str = java.text.Normalizer.normalize(input_str, java.text.Normalizer.Form.NFD);
		int j = 0;
		int nb_cars = input_str.length()
		for (int i=0; i<nb_cars; ++i) {
			char c = input_str.charAt(i);
			if ((c <= '\u007F') && (!Character.isWhitespace(c))) {
				out[j++] = c;
			}
		}
		return new String(out, 0, j);
	}
	public static String stripAccentsAndNonAlphanumeric(String input_str) {
		char[] out = new char[input_str.length()];
		input_str = java.text.Normalizer.normalize(input_str, java.text.Normalizer.Form.NFD);
		int j = 0;
		int nb_cars = input_str.length()
		for (int i=0; i<nb_cars; ++i) {
			char c = input_str.charAt(i);
			if (((c <= '\u007A') && (c >= '\u0041')) || Character.isDigit(c) ) {
				out[j++] = c;
			}
		}
		return new String(out, 0, j);
	}
	
	public static stripNonNumericCharacters(String input_str) {
		if (input_str==null) {
			return null;
		} else {
			return input_str.replaceAll( "[^\\d]", "" );
		}
	}
	
	public static boolean equalsIgnoringCaseAccentAndSpaces(String input_str1, String input_str2) {
		boolean is_equal=false;
		
		if ((input_str1 == null) || (input_str2 == null)) {
			if ((input_str1 == null) && (input_str2 == null)) {
				is_equal=true;
			} else {
				is_equal=false;
			}
		} else {
			def String normalized_str1 = stripAccentsAndSpaces(input_str1);
			def String normalized_str2 = stripAccentsAndSpaces(input_str2);
			
			is_equal = normalized_str1.equalsIgnoreCase(normalized_str2);
		}
		
		return is_equal;
	}
	
	public static int countNumberOfOccurences(String input_string, String searched_string) {
		int nb_occurences=0;
		
		if ((searched_string != null) && (input_string != null)) {
			String[] split_array = input_string.split(searched_string);
			nb_occurences = split_array.size();
		}
		
		return nb_occurences
	}
	
	public static String formatNumber(String numeric_value, String language, String country, String currency_code, int nb_decimal_digits) {
		// For France: language=fr, country=FR, currecy_code=EUR
		def String formatted_value = numeric_value;
		def boolean is_numeric=true;
		def Number number_value=0;
		
		try {
			if ((numeric_value != null) && (numeric_value.length() > 0)) {
				def java.text.NumberFormat df = java.text.NumberFormat.getCurrencyInstance(new java.util.Locale(language, country));
				def Currency cur = Currency.getInstance(currency_code);
				df.setCurrency(cur);
				
				try {
					number_value = df.parse(numeric_value);
				} catch (java.text.ParseException ex) {
					is_numeric=false;
				}
				
				if (!is_numeric) {
					def String parsed_value = numeric_value.replace(" ", "");
					parsed_value = parsed_value.replace(cur.getSymbol(), "");
					logDebug("Parsing value [" + parsed_value + "] ...");
					try {
						number_value = Double.valueOf(parsed_value);
						is_numeric=true;
					} catch(java.lang.NumberFormatException ex) {
						is_numeric=false;
					}
					if (!is_numeric) {
						try {
							number_value = Long.valueOf(parsed_value);
							is_numeric=true;
						} catch(java.lang.NumberFormatException ex) {
							is_numeric=false;
						}
					}
				}
				
				if (is_numeric) {
					df.setMinimumFractionDigits(nb_decimal_digits);
					df.setMaximumFractionDigits(nb_decimal_digits);
					formatted_value = df.format(number_value);
				} else {
					logDebug("Value [" + numeric_value + "] is not a number")
				}
			}
			
		} catch(e) {
			logError("formatNumber exception: " + e.toString());
		}
		
		return 	formatted_value;
	}
	
	public static String removeDisplayNameFromEmailAddress(String email_address) {
		def String route_address=email_address;
		
		try {
			def int pos_start_route=email_address.indexOf("<");
			if (pos_start_route >= 0) {
				def int pos_end_route=email_address.lastIndexOf(">");
				if (pos_end_route > pos_start_route) {
					route_address = email_address.substring(pos_start_route+1, pos_end_route);
					logDebug("removeDisplayNameFromEmailAddress: route email address is [" + route_address + "] for email address [" + email_address + "]");
				} else {
					logDebug("removeDisplayNameFromEmailAddress: No end of route address found in email address [" + email_address + "]");
				}
			} else {
				logDebug("removeDisplayNameFromEmailAddress: No display name found in email address [" + email_address + "]");
			}
		} catch(e) {
			logError("removeDisplayNameFromEmailAddress exception: " + e.toString());
		}
		
		return route_address;
	}
	
	public static String getDisplayNameFromEmailAddress(String email_address) {
		def String displayName="";
		
		try {
			def int pos_start_route=email_address.indexOf("<");
			if (pos_start_route >= 0) {
				def int pos_end_route=email_address.lastIndexOf(">");
				if (pos_end_route > pos_start_route) {
					if (pos_start_route > 0) {
						displayName = email_address.substring(0, pos_start_route);
					}
					def int address_length = email_address.length();
					if (pos_end_route<address_length-1) {
						displayName += email_address.substring(pos_end_route+1, address_length);
					}
					logDebug("getDisplayNameFromEmailAddress: dislay name is [" + displayName + "] for email address [" + email_address + "]");
				} else {
					logDebug("getDisplayNameFromEmailAddress: No end of route address found in email address [" + email_address + "]");
				}
			} else {
				logDebug("getDisplayNameFromEmailAddress: No display name found in email address [" + email_address + "]");
			}
		} catch(e) {
			logError("getDisplayNameFromEmailAddress exception: " + e.toString());
		}
		
		return displayName;
	}
	
	public static String buildListOfEmailAddresses(List<String> email_addresses) {
		def emails_list = "";
		
		try {
			if (email_addresses != null) {
				// Build list of addresse with []
				emails_list = email_addresses.toString();
				
				// Remove []
				emails_list = emails_list.substring(1, emails_list.length()-1);
				
			}
		} catch(e) {
			logError("buildListOfEmailAddresses exception: " + e.toString());
		}
		
		return emails_list;
	}
	
	public static left(String original_string, int nb_characters) {
		def String truncated_string="";
		
		try {
			if (original_string != null) {
				def int string_len = original_string.length();
				if (string_len > 0) {
					if (string_len <= nb_characters) {
						truncated_string = original_string;
					} else {
						truncated_string = original_string.substring(0, nb_characters);
					}
				}
			}
		} catch(e) {
			logError("left exception: " + e.toString());
		}
	
		return truncated_string;
	}
	
	public static Date parseDate(String date_dd_mm_yyyy) {
		def Date date_value=null;
		
		try {
			java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
			date_value = sdf.parse(date_dd_mm_yyyy);
			
		} catch(e) {
			logError("parseDate exception: " + e.toString());
		}
		
		return date_value;
	}
	
	/**
	 * Calculates the similarity (a number within 0 and 1) between two strings.
	 */
	public static double evaluateSimilarity(String s1, String s2) {
	  String longer = s1, shorter = s2;
	  if (s1.length() < s2.length()) { // longer should always have greater length
		longer = s2; shorter = s1;
	  }
	  int longerLength = longer.length();
	  if (longerLength == 0) { return 1.0; /* both strings are zero length */ }
	  return (longerLength - editDistance(longer, shorter)) / (double) longerLength;
	}
	
	private static int editDistance(String s1, String s2) {
		s1 = s1.toLowerCase();
		s2 = s2.toLowerCase();
	
		int[] costs = new int[s2.length() + 1];
		for (int i = 0; i <= s1.length(); i++) {
		  int lastValue = i;
		  for (int j = 0; j <= s2.length(); j++) {
			if (i == 0)
			  costs[j] = j;
			else {
			  if (j > 0) {
				int newValue = costs[j - 1];
				if (s1.charAt(i - 1) != s2.charAt(j - 1))
				  newValue = Math.min(Math.min(newValue, lastValue), costs[j]) + 1;
				costs[j - 1] = lastValue;
				lastValue = newValue;
			  }
			}
		  }
		  if (i > 0)
			costs[s2.length()] = lastValue;
		}
		return costs[s2.length()];
	}
	
	private static LogFile getLogFile() {
		if (log_file == null) {
			log_file = LogFile.getLogFile("StringUtility");
		}
		
		return log_file;
	}
	
	//Function LogMessage
	def private static void logDebug(String message) {
		getLogFile().debug("StringUtility", message);
	}
	
	def private static void logError(String message) {
		getLogFile().error("StringUtility", message);
	}
}
