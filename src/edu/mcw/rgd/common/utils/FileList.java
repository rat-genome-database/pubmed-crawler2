package edu.mcw.rgd.common.utils;

import java.io.*;
import java.util.ArrayList;

import org.apache.log4j.Logger;

/**
 * @author wliu
 * /**
 * helper class, copied from text_mining/Java/TextMiningTools
 */
public class FileList {

	private String filePath = "";
	protected static final Logger logger = Logger.getLogger(FileList.class);
	public ArrayList<FileEntry> fileList = new ArrayList<FileEntry>();

	/**
	 * @return the filePath
	 */
	public String getFilePath() {
		return filePath;
	}

	/**
	 * @param filePath the filePath to set
	 */
	public void setFilePath (String filePath) throws Exception {
		setFilePath(filePath, true);
	}

	/**
	 * @param filePath: the filePath to set.
	 * @param createNew: create new file
	 */
	public void setFilePath (String filePath, boolean createNew) throws Exception {
		this.filePath = filePath;
		load(createNew);
	}

	public int load(boolean createNew) throws IOException {
		File file = new File(filePath);
		if (!file.exists()) {
			if (createNew) file.createNewFile();
			return 0;
		}

		BufferedReader br = new BufferedReader(new FileReader(file));
		String line;
		while ((line = br.readLine()) != null && line.length() > 0)
		{
			fileList.add(new FileEntry(line));
		}
		br.close();

		return fileList.size();
	}

	public int save() throws IOException {
		BufferedWriter bw = new BufferedWriter(new java.io.FileWriter(filePath));
		for (int i = 0; i < fileList.size(); i++) {
			FileEntry fe = fileList.get(i);
			bw.write(fe.toString());  
			bw.newLine();
		}
		bw.close();

		return fileList.size();
	}

	public int removeFile(String file_name) {
		int start_index = 0;
		while ((start_index = findFile(file_name, start_index)) >= 0) {
			fileList.remove(start_index);
		}
		return fileList.size();
	}

	public int findFile(String file_name, int start_index) {
		for (int i = start_index; i < fileList.size(); i ++) {
			if (fileList.get(i).matches(file_name)) return i;
		}
		return -1;
	}

	public int addFile(String file_name) {
		if (findFile(file_name, 0) < 0)
			fileList.add(new FileEntry(file_name));
		return fileList.size();
	}

	public void clear() {
		fileList.clear();
	}

	@SuppressWarnings("unchecked")
	public ArrayList<FileEntry> cloneList() {
		return (ArrayList<FileEntry>) fileList.clone();
	}
}
