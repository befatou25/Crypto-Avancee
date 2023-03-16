package src;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Scanner;

public class Reply2 {
    public static void Reply2(String username, String password){
        ArrayList<Integer> mailsList = MailsList(username, password);
        Message[] arrayMessages = ArrayMessages(username, password);

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
        Scanner sc = new Scanner(System.in);
        try{
            MimeMessage message=new MimeMessage(session);
            message.setFrom(username);
            message.setReplyTo(new Address[] { new InternetAddress(username) });
            message.setText("Bonjour, \n Ceci est une réponse au mail ");

            System.out.println(mailsList);
            System.out.println("Veuillez choisir le numero du mail auquel vous voulez répondre : ");
            int idMail = sc.nextInt();
            while (!mailsList.contains(idMail)) {
                System.out.println("Veuillez choisir un bon numero : ");
                idMail = sc.nextInt();
            }
            System.out.println("voici le num entré : " + idMail);
            sc.close();

            message.addRecipient(Message.RecipientType.TO, new InternetAddress(arrayMessages[idMail].getFrom()[idMail].toString()));
            message.setSubject("Reponse");
            Transport.send(message);
            System.out.println("Voila on a une reponse");


        } catch (NoSuchProviderException e) {e.printStackTrace();}
        catch (MessagingException e) {e.printStackTrace();}

    }

    public static ArrayList<Integer> MailsList(String username, String password) {
        ArrayList<Integer> nbrList = new ArrayList<>();

        try {
            Properties properties = new Properties();
            // server setting (it can be pop3 too
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
                int nbr = i + 1;
                nbrList.add(nbr);
            }

        } catch (NoSuchProviderException e) {
            throw new RuntimeException(e);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
        return nbrList;
    }

    public static Message[] ArrayMessages(String username, String password) {
        Message[] arrayMessages = null;
        try {
            Properties properties = new Properties();
            // server setting (it can be pop3 too
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
            arrayMessages = folderInbox.getMessages();
           /* List<Integer> nbrList = new ArrayList<>();
            for (int i = 0; i < arrayMessages.length; i++) {
                int nbr = i + 1;
                nbrList.add(nbr);
                System.out.println("************* Voici le numero de mail : " + nbr);
                Message message = arrayMessages[i];
                Address[] fromAddress = message.getFrom();
                String from = fromAddress[0].toString();
                System.out.println("************* Voici d'où provient le mail : " + from);

            }*/
            folderInbox.close(false); // ferme la boîte aux lettres sans supprimer les messages
            store.close(); // ferme la connexion à la boîte aux lettres
        } catch (MessagingException ex) {
            System.out.println("Could not connect to the message store");
            ex.printStackTrace();
        }
        return arrayMessages;

    }


}

