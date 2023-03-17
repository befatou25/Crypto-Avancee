package src;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.*;
import javax.mail.Address;


public class Reply2 {
    //**********************************************************************************************

    public static void Reply2(String username, String password, String messageId){
        Properties properties = new Properties();

        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.outlook.com");
        properties.put("mail.smtp.port", "587");
        properties.put("mail.imap.host", "outlook.office365.com");
        properties.put("mail.imap.port", "993");
        properties.setProperty("mail.imap.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        properties.setProperty("mail.imap.socketFactory.fallback","false");
        properties.setProperty("mail.imap.socketFactory.port", "993");
        Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username,password);
            }
        });
        System.out.println("session.getProviders():"+session.getProviders()[0].getType());
        try{
            /**Scanner sc = new Scanner(System.in);
             System.out.println("Veuillez entrez l'id du mail auquel vous voulez répondre : ");
             String IDMail = sc.next();**/



            Store store = session.getStore("imap");
            store.connect(username, password);
            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_ONLY);
            Message[] messages = inbox.getMessages();
            for (int i = 0; i < messages.length; i++) {
                String messageID = ((MimeMessage) messages[i]).getMessageID();
                System.out.println("Voici l'id entré : " +messageId);
                System.out.println("** Voila les ids des mails : " + messageID);
                if (messageID != null && messageID.equals(messageId)) {
                //if (((MimeMessage) messages[i]).getMessageID().equals(messageId)) {
                    //MimeMessage message = (MimeMessage) messages[i];
                    System.out.println("hiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii");
                    //new MimeMessage(session);
                    //MimeMessage reply;
                    MimeMessage reply=new MimeMessage(session);
                    System.out.println("haaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa");
                    //reply = (MimeMessage) message.reply(false);
                    reply.setReplyTo(new Address[] { new InternetAddress(username) });
                    System.out.println("hoooooooooooooooooooooooooooooooooooooooooooooooooooooooooo");
                    reply.setText("Votre réponse ici.......");
                    Transport.send(reply);
                    System.out.println("Réponse envoyée avec succès.");
                    break;
                } else {
                    System.out.println("ERROOOOOOOOOOOOOOOR : Y'a un souciiiiiiiiiis avec ton ID Meuf");
                }
            }
        } catch (MessagingException e) {
            e.printStackTrace();
        }

    }
    //**********************************************************************************************



    public static List<String> NumMail(String username, String password) {

        List<String> messages = new ArrayList<>();

        try {
            Properties properties = new Properties();
            // server setting (it can be pop3 too)
            properties.put("mail.imap.host", "outlook.office365.com");
            properties.put("mail.imap.port", "993");
            properties.setProperty("mail.imap.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            properties.setProperty("mail.imap.socketFactory.fallback", "false");
            properties.setProperty("mail.imap.socketFactory.port", "993");
            Session session = Session.getDefaultInstance(properties);
            Store store = session.getStore("imap");
            store.connect(username, password);
            Folder folderInbox = store.getFolder("INBOX");
            if (!folderInbox.isOpen()) {
                folderInbox.open(Folder.READ_ONLY);
            }
            Message[] arrayMessages = folderInbox.getMessages();

            for (int i = 0; i < arrayMessages.length; i++) {
                MimeMessage message = (MimeMessage) arrayMessages[i];
                String msgID = message.getMessageID(); // obtenir l'ID unique du message
                System.out.println("* Voici l'ID du mail : " + msgID);
                messages.add(msgID);
                Address[] fromAddress = message.getFrom();
                String from = fromAddress[0].toString();
                System.out.println("* Voici d'où provient le mail : " + from);
            }

            System.out.println(messages);

            folderInbox.close(false); // ferme la boîte aux lettres sans supprimer les messages
            store.close(); // ferme la connexion à la boîte aux lettres

        } catch (MessagingException ex) {
            System.out.println("Could not connect to the message store");
            ex.printStackTrace();
        }

        return messages;
    }


}

