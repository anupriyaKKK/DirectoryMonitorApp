package src.components;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;


/**
 * The MailingService class is sending email notification to the given recipient using Gmail service.
 * The mail is sent for every new file creation event. 
 */
public class MailingService {
	
	/**
	 * Sends an email from SMTP server using Gmail account
	 * arg0 - FileOutPutModel object to retrieve output values to be set as the email body
	 */
	public static void sendMail(FileOutputModel fileOutputModel) throws MessagingException {
		
		String to = "assessment-test@180bytwo.com";
		String from = "assessment.180bytwo@gmail.com";
		
		final String username = "assessment.180bytwo@gmail.com";
		final String password = "Password@123";
		
		String host = "smtp.gmail.com";
		
		Properties properties = setProperties(host);

		Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
			
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
			 
		});
		
		Message message = prepareMessage(session, from, to, fileOutputModel);
		
		Transport.send(message);
		
		System.out.println("Sent message successfully....");
		
	}
	
	/**
	 * Setting SMTP properties for the gmail service
	 * arg0 - SMTP host value 
	 */
	public static Properties setProperties(String host) {
		
		Properties properties = new Properties();
		properties.put("mail.smtp.auth", "true");
		properties.put("mail.smtp.starttls.enable", "true");
		properties.put("mail.smtp.host", host);
		properties.put("mail.smtp.port", "587");
		
		return properties;
	}
	
	/**
	 * It creates and returns Message object which contains all the email details
	 * arg0 - Session object
	 * arg1 - sender's email address
	 * arg2 - recipient's email address
	 * arg3 - FileOutputModel object which contains the notification body information
	 */
	public static Message prepareMessage(Session session, String from, String to, FileOutputModel fileOutputModel) {
		try {

		    Message message = new MimeMessage(session);
		 
		    message.setFrom(new InternetAddress(from));

		    message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));

			message.setSubject("New File Details");
			
			message.setText("File Path: " + fileOutputModel.getFilePath() 
					  + " \nFile Type: " + fileOutputModel.getFileType()
					  + " \nFirst Row: " + fileOutputModel.getFirstRow()
					  + " \nTotal Rows: " + fileOutputModel.getTotalRows()
					  + " \nMD5 Checksum: " + fileOutputModel.getMd5Checcksum()
							);
	
			return message;
			
		} 
		catch (MessagingException e) {
			throw new RuntimeException(e);
		}
	}
}
