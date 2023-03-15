package src;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

public class Reply2 {
    public static void Reply2(String username, String password, String destination){
        Properties properties = new Properties();

        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.outlook.com");
        properties.put("mail.smtp.port", "587");
        Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username,password);
            }
        });
        System.out.println("session.getProviders():"+session.getProviders()[0].getType());
        try{
            MimeMessage message=new MimeMessage(session);
            message.setFrom(username);
            message.setReplyTo(new Address[] { new InternetAddress(username) });
            message.setText("Bonjour, \n Ceci est une réponse au mail ");
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(destination));
            message.setSubject("Reponse");
            Transport.send(message);
            System.out.println("Voila on a une reponse");


        } catch (NoSuchProviderException e) {e.printStackTrace();}
        catch (MessagingException e) {e.printStackTrace();}

    }


    }

