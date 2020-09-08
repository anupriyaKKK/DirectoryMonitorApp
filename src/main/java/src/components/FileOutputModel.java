package src.components;

import java.nio.file.Path;

/**
 * A model class for storing all the details needs to be sent in the email notification
 */

public class FileOutputModel {
	
	private Path filePath;
	private String fileType;
	private String firstRow;
	private int totalRows;
	private String md5Checcksum;
	
	public FileOutputModel() {
	}

	public FileOutputModel(Path filePath, String fileType, String firstRow, int totalRows, String md5Checcksum) {
		super();
		this.filePath = filePath;
		this.fileType = fileType;
		this.firstRow = firstRow;
		this.totalRows = totalRows;
		this.md5Checcksum = md5Checcksum;
	}

	public Path getFilePath() {
		return filePath;
	}

	public void setFilePath(Path filePath) {
		this.filePath = filePath;
	}

	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}

	public String getFirstRow() {
		return firstRow;
	}

	public void setFirstRow(String firstRow) {
		this.firstRow = firstRow;
	}

	public int getTotalRows() {
		return totalRows;
	}

	public void setTotalRows(int totalRows) {
		this.totalRows = totalRows;
	}

	public String getMd5Checcksum() {
		return md5Checcksum;
	}

	public void setMd5Checcksum(String md5Checcksum) {
		this.md5Checcksum = md5Checcksum;
	}
	
}

