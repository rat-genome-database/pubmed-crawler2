package edu.mcw.rgd.common.utils;

import java.io.File;

import org.apache.commons.io.FileUtils;

/**
 * helper class, copied from text_mining/Java/TextMiningTools
 */
public class FileEntry {

	private String fileName = "";
	private String fileContent;
	
	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * @param line the fileName to set
	 */
	public void setFileName(String line) {
		fileName = line;
	}

	public String toString() {
		return fileName;
	}
	
	public FileEntry(String file_string) {
		parseString(file_string);
	}
	
	public void parseString(String line) {
		setFileName(line);
	}
	
	public boolean matches(String file_name) {
		return fileName.equals(file_name);
	}
	
	public Boolean load() throws Exception {
    	fileContent = FileUtils.readFileToString(new File(fileName), "UTF-8");
		return true;
	}

	/**
	 * @return the fileContent
	 */
	public String getFileContent() {
		return fileContent;
	}

	/**
	 * @param fileContent the fileContent to set
	 */
	public void setFileContent(String fileContent) {
		this.fileContent = fileContent;
	}
}
