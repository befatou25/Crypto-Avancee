package mailer.crypt;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.MessageIDTerm;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static mailer.crypt.Set.getEmailSession;
import static mailer.crypt.Set.getSessionOwner;


public class Gestion {

    //region send


    public static void sendMail(String destination, String subject, String text, String cc) {
        try {
            MimeMessage message = new MimeMessage(getEmailSession());
            message.setFrom(getSessionOwner());
            message.setText(text);
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(destination));
            if (cc != null && !cc.isEmpty()) {
                message.addRecipient(Message.RecipientType.CC, new InternetAddress(cc));
            }
            message.setSubject(subject);
            Transport.send(message);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }




    public static void sendMail(String destination, String subject, String text, String[] attachmentPaths, String cc) {
        try {
            MimeMessage message = new MimeMessage(getEmailSession());
            message.setFrom(getSessionOwner());
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(destination));

            // Add CC recipient if present
            if (cc != null && !cc.isEmpty()) {
                message.addRecipient(Message.RecipientType.CC, new InternetAddress(cc));
            }

            message.setSubject(subject);

            Multipart emailContent = new MimeMultipart();
            MimeBodyPart bodypart = new MimeBodyPart();
            bodypart.setText(text);

            emailContent.addBodyPart(bodypart);

            // Add attachments
            for (String attachmentPath : attachmentPaths) {
                MimeBodyPart attachmentPart = new MimeBodyPart();
                attachmentPart.attachFile(new File(attachmentPath));
                emailContent.addBodyPart(attachmentPart);
            }

            message.setContent(emailContent);
            Transport.send(message);

        } catch (MessagingException | IOException e) {
            e.printStackTrace();
        }
    }

    //endregion

    public static ObservableList<EmailModel> getEmailsByFolder(String folderName) {
        try {
            Store store = getEmailSession().getStore("imaps");
            store.connect();
            Folder folder = store.getFolder(folderName);
            folder.open(Folder.READ_ONLY);

            Message[] messages = folder.getMessages();
            List<EmailModel> emailModels = new ArrayList<>();
            for (Message m : messages) {
                emailModels.add(new EmailModel(m));
            }
            Collections.reverse(emailModels);

            ObservableList<EmailModel> obsList = FXCollections.observableList(emailModels);

            folder.close(false);
            store.close();
            return obsList;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void deleteMail(String messageID, String folderName) {
        try {
            Store store = getEmailSession().getStore("imaps");
            store.connect();

            Folder folder = store.getFolder(folderName);
            folder.open(Folder.READ_WRITE);

            Message[] messages = folder.search(new MessageIDTerm(messageID));
            if (messages.length == 0) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "No messages found with the specified ID.", ButtonType.OK);
                alert.showAndWait();
                return;
            }

            for (Message message : messages) {
                message.setFlag(Flags.Flag.DELETED, true);
                //Alert alert = new Alert(Alert.AlertType.INFORMATION, "Message deleted successfully.", ButtonType.OK);
                //alert.showAndWait();
            }

            folder.close(true);
            store.close();

        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "An error occurred while deleting the message: " + e.getMessage(), ButtonType.OK);
            alert.showAndWait();
            e.printStackTrace();
        }
    }


    public static void forwardMail(String messageID, String forwardTo) {
        try {
            Store store = getEmailSession().getStore("imaps");
            store.connect();

            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);
            Message[] messages = inbox.getMessages();
            for (Message message : messages) {
                if (message.getHeader("Message-ID")[0].equals(messageID)) {
                    MimeMessage forward = new MimeMessage(getEmailSession());
                    forward.setFrom(new InternetAddress(getSessionOwner()));
                    forward.setRecipient(MimeMessage.RecipientType.TO, new InternetAddress(forwardTo));
                    forward.setSubject("FWD: " + message.getSubject());
                    forward.setSentDate(message.getSentDate());
                    forward.setHeader("Content-Type", message.getContentType());

                    forward.setContent(message.getContent(), message.getContentType());

                    Transport.send(forward);
                    break;
                }
            }

            inbox.close(false);
            store.close();
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void reply(String messageID, String text) {
        try {
            Store store = getEmailSession().getStore("imaps");
            store.connect();

            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);

            Message[] messages = inbox.getMessages();
            for (Message message : messages) {
                if (message.getHeader("Message-ID")[0].equals(messageID)) {

                    MimeMessage reply = (MimeMessage) message.reply(false);
                    reply.setText(text);
                    reply.setSubject("RE: " + message.getSubject());
                    reply.setFrom(new InternetAddress(getSessionOwner()));
                    Transport.send(reply);
                    break;
                }
            }

            inbox.close(false);
            store.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void reply(String messageID, String text, String[] attachmentPaths) {
        try {
            Store store = getEmailSession().getStore("imaps");
            store.connect();

            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);

            Message[] messages = inbox.getMessages();
            for (Message message : messages) {
                if (message.getHeader("Message-ID")[0].equals(messageID)) {

                    MimeMessage reply = (MimeMessage) message.reply(false);
                    Multipart emailContent = new MimeMultipart();

                    MimeBodyPart bodypart = new MimeBodyPart();
                    bodypart.setText(text);

                    reply.setSubject("RE: " + message.getSubject());
                    reply.setFrom(new InternetAddress(getSessionOwner()));

                    emailContent.addBodyPart(bodypart);

                    // Add attachments
                    for (String attachmentPath : attachmentPaths) {
                        MimeBodyPart attachmentPart = new MimeBodyPart();
                        attachmentPart.attachFile(new File(attachmentPath));
                        emailContent.addBodyPart(attachmentPart);
                    }

                    reply.setContent(emailContent);

                    Transport.send(reply);
                    break;
                }
            }

            inbox.close(false);
            store.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
