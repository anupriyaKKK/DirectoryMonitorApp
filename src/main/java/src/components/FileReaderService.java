package src.components;

import javax.mail.MessagingException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.GZIPInputStream;

/**
 * This class process the file
 * 1. Check if file is compressed and decompress it
 * 2. Read the file and print first row
 * 3. Calculate the total number of rows in the file
 * 4. Calculate MD5 checksum of the file
 * 5. Call the Mailing service to send email notification
 */
public class FileReaderService {
	
	private GZIPInputStream gzipInputStream = null;
	private FileInputStream fileInputStream = null;
	private File file = null;
	private FileOutputModel fileOutputModel;
	
	/**
	 * Call other methods to decompress the file if compressed, read the file and call MailingService sendMail() method
	 * @param source - path of the newly created file
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws MessagingException
	 */
	synchronized public void processFile(Path source) throws IOException, NoSuchAlgorithmException, MessagingException {
		
		fileOutputModel = new FileOutputModel();
		setFilePathAndFileType(source);
		
		file = source.toFile();		
		fileInputStream = new FileInputStream(source.toString());
		
		boolean isCompressed = isCompressed(file);
		try {
			InputStream inputStream = null;
			
			if(isCompressed) {			
				gzipInputStream = new GZIPInputStream(fileInputStream);
				inputStream = gzipInputStream;	
			}
			else {
				inputStream = fileInputStream;
			}
			
			readFile(inputStream);
			
			MailingService.sendMail(fileOutputModel);
			
		}
		catch(IOException ex) {
			System.out.println("Error in reading compressed file" + ex.toString());
		}
		finally {
			if(gzipInputStream != null) {
				gzipInputStream.close();
			}
			if(fileInputStream != null) {
				fileInputStream.close();
			}
		}

	}
	
	/**
	 * It sets the filePath and fileType of FileOutputModel class
	 * @param source - path of the new file
	 * @throws IOException
	 */
	public void setFilePathAndFileType(Path source) throws IOException {
		
		System.out.println("File path: " + source); 
		fileOutputModel.setFilePath(source);
    	
    	String fileName = source.getFileName().toString();
    	if(fileName.contains(".gz")) {
    		fileName = fileName.replace(".gz", "");   
		}
    	
    	String fileType = Files.probeContentType(Paths.get(fileName));
    	
    	System.out.println("File Type: " + fileType);
    	fileOutputModel.setFileType(fileType);
    	
	}
	
	/**
	 * It checks if the new file is compressed in GZIP format or not
	 * @param file - File object of new file
	 * @return true - if file is compressed
	 * @return false - if file is not compressed
	 * @throws IOException
	 */
	public boolean isCompressed(File file) throws IOException {
		InputStream inputStream = null;
	       
		try {
			inputStream = new FileInputStream(file);
	        byte [] signature = new byte[2];
			int nread = inputStream.read(signature); 
	        return nread == 2 && signature[ 0 ] == (byte) 0x1f && signature[ 1 ] == (byte) 0x8b;
	    } 
	    catch (IOException e) {
	    	System.out.println(e.toString());
	        return false;
	    }
		finally {
			if(inputStream != null) {
				inputStream.close();
			}
		}

	}
	
	/**
	 * It reads the file using byte array, set the firstRow and totalRows of FileOutputModel class
	 * Calls the findMD5Checksum()
	 * @param inputStream - stream to read the file
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 */
	    	
    public void readFile(InputStream inputStream) throws IOException, NoSuchAlgorithmException {
   	
    	try {
    		byte []allBytes = inputStream.readAllBytes(); 
    		
    		StringBuilder firstRow = new StringBuilder();     	 
        	int rows = 0;
        	
        	for(int i=0; i<allBytes.length; i++) {
	        	if (allBytes[i] == '\n') {
	        		rows++;
	        		continue;
	        	}
	        	if(rows == 1 && allBytes[i] != '\r') {
	        		firstRow.append((char)allBytes[i]);
	            }
        	}
        	
        	rows -= 1;
        	 
        	if(rows == 0) {
        		System.out.println("File is empty");
        	}
        	else {
        		System.out.println(firstRow.toString());
        		fileOutputModel.setFirstRow(firstRow.toString());        	
        	
       	 		System.out.println("No of rows: " + rows);
       	 		fileOutputModel.setTotalRows(rows);
       	 	
        	}       	 	
       	 	findMD5Checksum(allBytes);
    	
    	}    	    	
		catch(IOException ex) {
			System.out.println("Error occured while reading the file " + ex.toString());
		}
    	finally {
			if(inputStream != null) {
				inputStream.close();
			}
    	}

    }
    
    /**
     * Calculate the MD5 checksum from the given bytes of the file 
     * sets the md5Checksum of FileOutput class
     * @param bytes[] - contains the file data in byte array
     * @throws NoSuchAlgorithmException
     */
    public void findMD5Checksum(byte []bytes) throws NoSuchAlgorithmException {
    	
    	if(bytes.length >= 0) {
    	
	    	MessageDigest messageDigest = MessageDigest.getInstance("MD5");
	        
	   	 	byte[] md5ByteArray = messageDigest.digest(bytes);
	        
	        StringBuilder md5Checksum = new StringBuilder();
	        for(int i=0; i< md5ByteArray.length ;i++)
	        {
	        	md5Checksum.append(Integer.toString((md5ByteArray[i] & 0xff) + 0x100, 16).substring(1));
	        }
	        
	        System.out.println("MD5 Checksum: " + md5Checksum.toString());
	        fileOutputModel.setMd5Checcksum(md5Checksum.toString());
    	}
    	
    }

}