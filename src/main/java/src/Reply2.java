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
    
    public static void NumMail(String username, String password) {

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
            List<Integer> nbrList = new ArrayList<Integer>();
            for (int i = 0; i < arrayMessages.length; i++) {
                int nbr = i + 1;
                nbrList.add(nbr);
                System.out.println("*************Voici le numero de mail : " + nbr);
            }
            System.out.println(nbrList);
            Scanner sc = new Scanner(System.in);
            System.out.println("Veuillez choisir le numero du mail auquel vous voulez répondre : ");
            int numMail = sc.nextInt();
            System.out.println("voici le num entré : " + numMail);
            sc.close();
            if (nbrList.contains(numMail)) {
                try{
                    folderInbox.getMessage(numMail - 1);
                    Scanner sc1 = new Scanner(System.in);
                    System.out.println("Veuillez entrez l'adresse mail de réception  de la réponse : ");
                    String destination = sc1.next();
                    Reply2(username,password,destination, numMail);
                    sc1.close();


                }catch (MessagingException ex) {
                    System.out.println("Could not connect to the message store");
                    ex.printStackTrace();
                }
            }

            folderInbox.close(false); // ferme la boîte aux lettres sans supprimer les messages
            store.close(); // ferme la connexion à la boîte aux lettres
        } catch (MessagingException ex) {
            System.out.println("Could not connect to the message store");
            ex.printStackTrace();
        }

    }


    }

