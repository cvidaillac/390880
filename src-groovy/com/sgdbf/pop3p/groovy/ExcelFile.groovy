package com.sgdbf.pop3p.groovy
import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellAddress;
import com.sgdbf.pop3p.groovy.LogFile;
import java.time.LocalDate
import java.time.LocalDateTime
import com.sgdbf.pop3p.groovy.LocaleUtility;

class ExcelFile {
	def static LogFile log_file=null;
	
	public static List<List<Object>> readExcelFileFirstWorksheet(String file_path, boolean ignore_trailing_columns, boolean trim_spaces, java.util.Locale user_locale) {
		return readExcelFile(file_path, 0, ignore_trailing_columns, trim_spaces, user_locale);
	}
	
	public static List<List<Object>> readExcelInputStreamFirstWorksheet(InputStream input_stream, boolean ignore_trailing_columns, boolean trim_spaces, java.util.Locale user_locale) {
		return readExcelInputStream(input_stream, 0, ignore_trailing_columns, trim_spaces, user_locale);
	}
	
	public static List<List<Object>> readExcelFile(String file_path, int worksheet_num, boolean ignore_trailing_columns, boolean trim_spaces, java.util.Locale user_locale) {
		try {
			FileInputStream file = new FileInputStream(new File(file_path));
			
			return readExcelInputStream(file, worksheet_num, ignore_trailing_columns, trim_spaces, user_locale);
			
		} catch (e) {
			logError("Exception: " + e.toString());
		}
		
		return null;
	}
	
	public static List<List<Object>> readExcelInputStream(InputStream input_stream, int worksheet_num, boolean ignore_trailing_columns, boolean trim_spaces, java.util.Locale user_locale) {
		List<List<Object>> table_res = new ArrayList();
		
		try {
			//Create Workbook instance holding reference to .xlsx file
			XSSFWorkbook workbook = new XSSFWorkbook(input_stream);
			
			//Get first/desired sheet from the workbook
			XSSFSheet sheet = workbook.getSheetAt(worksheet_num);
			
			// Create data formater
			DataFormatter data_formater= new DataFormatter(user_locale);
	
			//Iterate through each rows one by one
			Iterator<Row> rowIterator = sheet.iterator();
			int row_num=0;
			int nb_columns_first_row=0;
			while (rowIterator.hasNext())
			{
				Row row = rowIterator.next();
				List<Object> table_row = new ArrayList();
				boolean is_empty_row = true;
				row_num = row_num+1;
				int nb_columns_current_row=0;
				
				//For each row, iterate through all the columns
				Iterator<Cell> cellIterator = row.cellIterator();
	
				while (cellIterator.hasNext())
				{
					Cell cell = cellIterator.next();
					Object table_cell=null;
					nb_columns_current_row = nb_columns_current_row+1;
					
					// Check for missing empty column
					CellAddress cell_addr = cell.getAddress();
					int cell_column = cell_addr.getColumn();	// Column number starts at 0
					//logDebug("Expected column " + String.valueOf(nb_columns_current_row) + ", found cell " + cell_addr.toString() + " with column " + String.valueOf(cell_column));
					while (cell_column>=nb_columns_current_row) {
						logDebug("Adding empty column " + String.valueOf(nb_columns_current_row) + " on line " + String.valueOf(row_num) + " before cell address " + cell_addr.toString());
						table_row.add(new String(""));
						nb_columns_current_row++;
					}
					
					//Check the cell type and format accordingly
					table_cell = getCellValue(cell, cell.getCellTypeEnum(), trim_spaces, data_formater);
					if ((table_cell != null) && (table_cell.toString().length() > 0)) {
						is_empty_row=false;
						table_row.add(table_cell);
						
						// Count columns on first row
						if (row_num == 1) {
							nb_columns_first_row=nb_columns_current_row;
						}
					} else {
						if (((row_num > 1) && (nb_columns_current_row <= nb_columns_first_row)) || (!ignore_trailing_columns)) {
							table_row.add(table_cell);
							logDebug("Added empty cell on column " + String.valueOf(nb_columns_current_row) + " and line " + String.valueOf(row_num));
						} else {
							logDebug("Ignoring trailing empty cell on column " + String.valueOf(nb_columns_current_row) + " and line " + String.valueOf(row_num));
						}
					}
				}
				
				if (!is_empty_row) {
					table_res.add(table_row);
				} else {
					logDebug("Ignoring empty row on line " + String.valueOf(row_num));
				}
			}
			input_stream.close();
		} catch (e) {
			logError("Exception: " + e.toString());
		}
		
		return table_res;
	}
	
	def public static Map createExcelFile(String sheet_name, List<String> header_row, String excelDateFormat) {
		def String error_message = "";
		def XSSFWorkbook wb = null;
		def XSSFSheet sheet = null;
		def CellStyle dateStyle = null;
		
		try {
			
			// Create sheet
			wb = new XSSFWorkbook();
			sheet = wb.createSheet(sheet_name);
			
			// Add Header line
			Row row = sheet.createRow((short)0);
			
			// Add each field name
			def int nb_fields = header_row.size();
			for (int i=0; i<nb_fields; i++) {
				Cell cell = row.createCell(i);
				cell.setCellValue(header_row.get(i));
			}
			
			// Create Date Style
			CreationHelper createHelper = wb.getCreationHelper();
			dateStyle = wb.createCellStyle();
			dateStyle.setDataFormat(
				createHelper.createDataFormat().getFormat(excelDateFormat));
			
		} catch (e) {
			error_message = "Erreur lors de la création du fichier Excel : " + e.toString();
			logError("Exception createExcelFile: " + error_message);
		}
		
		return ['errorMessage': error_message, 'workbook': wb, 'worksheet' : sheet, 'dateStyle': dateStyle];
	}
	
	def public static addExcelDataRow(XSSFSheet worksheet, Object bdm_object, List<String> fields_list, int row_index, CellStyle dateStyle) {
		def String error_message="";
		
		try {
			def boolean is_first_record = (row_index == 0) ? true : false;
			def List<Object> field_values = BDMUtility.getObjectRecordValues(bdm_object, fields_list, is_first_record);
			error_message = addExcelFieldValuesRow(worksheet, field_values, fields_list, row_index, dateStyle);
			
		} catch (e) {
			error_message = "Erreur lors de la création du fichier Excel : " + e.toString();
			logError("Exception addExcelDataRow: " + error_message);
		}
		
		return error_message;
	}
	
	def public static addExcelFieldValuesRow(XSSFSheet worksheet, List<Object> field_values,  List<String> fields_list, int row_index, CellStyle dateStyle) {
		def String error_message="";

		try {
			def boolean is_first_record = (row_index == 0) ? true : false;
			
			def int nb_fields_values = field_values.size();
			if (nb_fields_values > 0) {
				// Create row
				Row row = worksheet.createRow((short) row_index);
				
				// Add field values
				for (int i=0; i<nb_fields_values; i++) {
					Cell cell = row.createCell(i);
					def Object cell_value = field_values.get(i);
					
					// setCellValue can handle Date value but not LocalDateTime
					if (cell_value instanceof LocalDateTime) {
						cell_value = LocaleUtility.translateLocalDateTimeToDate(cell_value);
					} else if (cell_value instanceof LocalDate) {
						cell_value = LocaleUtility.translateLocalDateToDate(cell_value);
					}
					
					// Special handling for pitchSmart : remove all html formatting
					def String field_name = fields_list.get(i);
					if (field_name.equalsIgnoreCase("pitchSmart") && (cell_value != null) && (cell_value instanceof String)) {
						cell_value = ((String) cell_value).replaceAll("(?s)<[^>]*>(\\s*<[^>]*>)*", " ").trim();
					}

					cell.setCellValue(cell_value);
					if (cell_value instanceof Date) {
						// Format using date cell
						cell.setCellStyle(dateStyle);
					}
				}
			} else {
				logDebug("No fields values found for row [" + String.valueOf(row_index) + "]");
			}
			
		} catch (e) {
			error_message = "Erreur lors de la création du fichier Excel : " + e.toString();
			logError("Exception addExcelFieldValuesRow: " + error_message);
		}
		
		return error_message;
	}
	
	def public static Map writeExcelFile(Workbook wb, String filename, String output_dir) {
		def String error_message = "";
		def String full_path = "";
		
		try {
			def last_path_character = output_dir.substring(output_dir.length()-1);
			if (last_path_character.equals(File.separator)) {
				full_path = output_dir + filename;
			} else {
				full_path = output_dir + File.separator + filename;
			}
			logDebug("Writing output file [" + full_path + "]");
			
			// Get output stream
			FileOutputStream fileOut = new FileOutputStream(full_path);
		
			// Write the Excel file
			wb.write(fileOut);
			fileOut.close();
			
		} catch (e) {
			error_message = "Erreur lors de l'écriture du fichier : " + e.toString();
			logError("Exception: " + error_message);
		}
	
		return ['errorMessage': error_message, 'filepath': full_path];
	}
		
	def private static Object getCellValue(Cell cell, CellType cell_type, boolean trim_spaces, DataFormatter data_formater) {
		Object table_cell=null;
		
		try {
			CellAddress cell_addr = cell.getAddress();
			switch (cell_type)
			{
				case CellType.NUMERIC:
					Double numeric_value = cell.getNumericCellValue();
					
					if (DateUtil.isCellDateFormatted(cell)) {
						table_cell = cell.getDateCellValue();
						logDebug("Cell [" + cell_addr.toString() +"] has as date value");
						
					} else {
						// Formatting number
						CellStyle cell_style = cell.getCellStyle();
						String cell_data_format_string =  LocaleUtility.translateExcelFormatToLocale(cell_style.getDataFormatString(), LocaleUtility.getLocale());
						int cell_data_format_index = cell_style.getDataFormat();
						table_cell = data_formater.formatRawCellContents(numeric_value, cell_data_format_index, cell_data_format_string);
						java.text.Format cell_format = data_formater.getDefaultFormat(cell);
						logDebug("Cell [" + cell_addr.toString() +"] formatted with format [" + cell_data_format_string + "] with index " + String.valueOf(cell_data_format_index));
						
					}
					break;
					
				case CellType.STRING:
					table_cell = new String(cell.getStringCellValue());
					if (trim_spaces) {
						table_cell = table_cell.trim();
					}
					logDebug("Cell [" + cell_addr.toString() +"] has as String value [" + table_cell + "]");
					break;
					
				case CellType.BOOLEAN:
					table_cell = new Boolean(cell.getBooleanCellValue());
					logDebug("Cell [" + cell_addr.toString() +"] has as Boolean value [" + String.valueOf(table_cell) + "]");
					break;
					
				case CellType.BLANK:
					table_cell = new String("");
					logDebug("Cell [" + cell_addr.toString() +"] has a blank value");
					break;
					
				case CellType.FORMULA:
					CellType formula_cell_type = cell.getCachedFormulaResultTypeEnum();
					String formula_string = cell.getCellFormula();
					logDebug("Formula value for cell [" + cell_addr.toString() +"] : " + formula_string);
					table_cell = getCellValue(cell, formula_cell_type, trim_spaces, data_formater);
					break;
				
				case CellType.ERROR:
					String error_value = String.valueOf(cell.getErrorCellValue());
					logDebug("Error value for cell [" + cell_addr.toString() +"] : " + error_value);
					table_cell = "#ERROR# :" + error_value;
					break;
					
				default:
					logError("Unexpected cell type " + String.valueOf(cell_type));
					break;
			}
	
		} catch (e) {
			logError("Exception: " + e.toString());
		}
		
		return table_cell;
	}
	
	private static LogFile getLogFile() {
		if (log_file == null) {
			log_file = LogFile.getLogFile("ExcelFile");
		}
		
		return log_file;
	}
	
	//Function LogMessage
	def private static void logDebug(String message) {
		getLogFile().debug("ExcelFile", message);
	}
	
	def private static void logError(String message) {
		getLogFile().error("ExcelFile", message);
	}
	
}
