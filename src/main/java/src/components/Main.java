package src.components;

import javax.mail.MessagingException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;

/**
 * @author Anupriya Kaushik
 * This is the Main class. It reads the directory path from the Command Line Arguments
 * if CLI is valid it calls WatchService for the directory
 * else it will exit the program
 */

public class Main {
	
	/**
	 * It reads directory path from the CLI and creates a Path object and then creates WatchDirectoryService object and process the directory events
	 * @throws IOException
	 * @throws NoSuchAlgorithmException
	 * @throws MessagingException
	 */
	public static void main(String []args) throws IOException, NoSuchAlgorithmException, MessagingException {
				

        if (args.length == 0 || args.length > 1) {
        	System.out.println("Please provide correct directory path.");
        	System.exit(-1);
        }
        
        Path directoryPath = Paths.get(args[0]);
        
        try {
	        WatchDirectoryService watchDirectoryService = new WatchDirectoryService(directoryPath, true);
	        watchDirectoryService.processEvents();
        }
        catch(Exception ex) {
        	ex.printStackTrace();
        }
       
	}
    
}
