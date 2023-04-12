package mailer.controllers;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import java.util.Properties;


public class OutlookMailUtils {
    public static boolean verifyOutlookCredentials(String username, String password) {
        Properties imapProperties = new Properties();
        imapProperties.put("mail.store.protocol", "imaps");
        imapProperties.put("mail.imap.host", "outlook.office365.com");
        imapProperties.put("mail.imap.port", "993");
        imapProperties.put("mail.imap.ssl.enable", "true");
        Session session = Session.getInstance(imapProperties);
        Store store = null;

        try {
            store = session.getStore("imaps");
            store.connect("outlook.office365.com", username, password);
            return true;
        } catch (MessagingException e) {
            return false;
        } finally {
            if (store != null) {
                try {
                    store.close();
                } catch (MessagingException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
