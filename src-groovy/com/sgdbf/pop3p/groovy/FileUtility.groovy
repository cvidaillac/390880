package com.sgdbf.pop3p.groovy
import java.nio.file.*;

class FileUtility {
	def static LogFile log_file=null;
	
	public static String getFilenameFromPath(String fullpath) {
		def String filename="";
		
		try {
			def int pos_start = fullpath.lastIndexOf("\\");
			if (pos_start < 0) {
				pos_start = fullpath.lastIndexOf("/");
			}
			
			if (pos_start < 0) {
				filename = fullpath;
			} else {
				filename = fullpath.substring(pos_start+1);
			}
		} catch(e) {
			logError("Exception: " + e.toString());
		}
	
		return filename;
	}
	
	public static String getFileExtension(String filename) {
		def String file_ext="";
		
		try {
			def int pos_start = filename.lastIndexOf(".");
			
			if (pos_start < 0) {
				file_ext = "";
			} else {
				file_ext = filename.substring(pos_start+1);
			}
		} catch(e) {
			logError("Exception: " + e.toString());
		}
	
		return file_ext;
	}
	
	public static boolean deleteFile(String file_path) {
		def is_deleted=false;
		
		try {
			logDebug("deleteFile: Deleting file [" + file_path + "]");
			def File file = new File(file_path);
			is_deleted = file.delete();
		} catch(e) {
			logError("Exception deleteFile: " + e.toString());
		}
		
		return is_deleted;
	}
	
	public static void removeDocument(long document_storage_id, org.bonitasoft.engine.api.DocumentAPI doc_api) {
		
		try {
			// Archive document
			logDebug("removeDocument: Archiving document [" + String.valueOf(document_storage_id) + "]");
			def org.bonitasoft.engine.bpm.document.Document archived_doc = doc_api.removeDocument(document_storage_id);
			def Long archived_doc_id = Long.valueOf(archived_doc.getContentStorageId());
			
			// Remove content of archived document
			logDebug("removeDocument: Deleting content of archived document [" + String.valueOf(archived_doc_id) + "]");
			doc_api.deleteContentOfArchivedDocument(archived_doc_id);
			
		} catch(e) {
			logError("Exception removeDocument: " + e.toString());
		}
	}
	
	/**
	 * load the file give in parameters and return a byteArray or a null value
	 *
	 * @param fileName
	 * @return
	 */
	public static ByteArrayOutputStream loadFile(String file_path) {
		FileInputStream file = null;
		ByteArrayOutputStream byteArray = null;
		int read;
		
		try {
			file = new FileInputStream(file_path);
	
			byteArray = new ByteArrayOutputStream();
			byte[] content = new byte[2048 * 16 * 16];
	
			while ((read = file.read(content)) > 0) {
					byteArray.write(content, 0, read);
			}
	
		} catch (FileNotFoundException e) {
			logError("Exception loadFile: File [" + file_path + "] not found: " + e.toString());
			byteArray=null;
			
		} catch (IOException e) {
			logError("Exception loadFile: Failed to read file [" + file_path + "] : " + e.toString());
			byteArray=null;
			
		} catch (e) {
			logError("Exception loadFile: Error while reading file [" + file_path + "] : " + e.toString());
			byteArray=null;
			
		} finally {
			if (file != null)
				try {
					file.close();
				} catch (IOException e) {
			}
		}
		
		return byteArray;
	}
	
	public static Long createDocument(String document_name, String file_path, String mime_type, long case_id, org.bonitasoft.engine.api.DocumentAPI doc_api) {
		def Long document_id=null;
		
		try {
			logDebug("Creating document from file [" + file_path + "]...");
			def ByteArrayOutputStream output_stream = loadFile(file_path);
			def String filename = getFilenameFromPath(file_path);
			if (output_stream != null) {
				def org.bonitasoft.engine.bpm.document.Document document = doc_api.attachDocument(case_id, document_name, filename, mime_type, output_stream.toByteArray());
				document_id = document.getId();
				logDebug("Created document [" + filename + "] with id [" + String.valueOf(document_id) + "] for case [" + String.valueOf(case_id) + "]");
			}
			
		} catch (e) {
				logError("Exception createDocument: " + e.toString());
				document_id=null;
		}
		
		return document_id;
	}
	
	private static LogFile getLogFile() {
		if (log_file == null) {
			log_file = LogFile.getLogFile("FileUtility");
		}
		
		return log_file;
	}
	
	//Function LogMessage
	def private static void logDebug(String message) {
		getLogFile().debug("FileUtility", message);
	}
	
	def private static void logError(String message) {
		getLogFile().error("FileUtility", message);
	}
}
