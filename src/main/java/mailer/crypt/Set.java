package mailer.crypt;

import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import java.util.Properties;

public class Set {
    private static Session emailSession;
    private static String sessionOwner;

    public static Session getEmailSession() {
        return emailSession;
    }

    public static String getSessionOwner() {
        return sessionOwner;
    }

    public static void setEmailSession(String email, String passwd) {
        Properties properties = new Properties();

        //configure smtp
        properties.put("mail.smtp.host", "smtp-mail.outlook.com");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.port", "587");

        // configure imap
        properties.put("mail.store.protocol", "imaps");
        properties.put("mail.imaps.host", "imap.outlook.com");
        properties.put("mail.imaps.starttls.enable", "true");
        properties.put("mail.imaps.port", "993");

        emailSession = Session.getDefaultInstance(properties, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(email, passwd);
            }
        });

        sessionOwner = email;
    }

}

