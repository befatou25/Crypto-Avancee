package securitySysteme.autority;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.ElementPowPreProcessing;
import it.unisa.dia.gas.jpbc.Field;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import securitySysteme.ClientsMail.IBEBasicIdent;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.search.FromTerm;
import javax.mail.search.SearchTerm;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Properties;
import java.util.Random;

public class Authentication {

    public static final String trustedauthority = "autoritedeconfiance@outlook.com";
    public static final String trustedauthorityPassword = "AutoriteConfiance2023*";
    public static final Pairing pairing = PairingFactory.getPairing("src/main/resources/curves/a.properties"); // chargement des paramètres de la courbe elliptique
    // la configuration A offre un pairing symmetrique ce qui correspond à l'implementation du schema basicID
    // qui est basé sur l'utilisation du pairing symmetrique
    public static final SettingParameters sp = IBEBasicIdent.setup(pairing);

    public static final int ConfirmationCode = generateConfirmationCode();

    private static int generateConfirmationCode() {
        Random random = new Random();
        return random.nextInt(999999);
    }

    public static int sendConfirmationMail (String id) {
        Properties properties = new Properties();

        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.office365.com");
        properties.put("mail.smtp.port", "587");
        Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(trustedauthority,trustedauthorityPassword);
            }
        });
        System.out.println("session.getProviders():"+session.getProviders()[0].getType());

        try {
            // Création de l'e-mail
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(trustedauthority));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(id));
            message.setSubject("Code de confirmation");
            message.setText("Votre code de confirmation est : " + ConfirmationCode + "\nVeuillez répondre à ce mail en entrant votre code");

            // Envoi de l'e-mail
            Transport.send(message);

            System.out.println("L'e-mail a été envoyé avec succès.");

        } catch (MessagingException e) {
            System.out.println("Une erreur s'est produite lors de l'envoi de l'e-mail : " + e.getMessage());
        }
        return ConfirmationCode;
    }

    static int getNumbers(String s) {

        String[] n = s.split(""); //array of strings
        StringBuffer f = new StringBuffer(); // buffer to store numbers

        for (int i = 0; i < n.length; i++) {
            if((n[i].matches("[0-9]+"))) {// validating numbers
                f.append(n[i]); //appending
            }else {
                //parsing to int and returning value
                return Integer.parseInt(f.toString());
            }
        }
        return 0;
    }
    public static int confirmConfirmationCode(String id) {
        Properties properties = new Properties();

        // server setting (it can be pop3 too
        properties.put("mail.imap.host", "outlook.office365.com");
        properties.put("mail.imap.port", "993");
        properties.setProperty("mail.imap.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        properties.setProperty("mail.imap.socketFactory.fallback","false");
        properties.setProperty("mail.imap.socketFactory.port", "993");


        Session session = Session.getDefaultInstance(properties);

        int receivedConfirmationCode = 0;

        try {
            // connects to the message store imap or pop3
            //     Store store = session.getStore("pop3");
            Store store = session.getStore("imap");

            store.connect(trustedauthority, trustedauthorityPassword);

            // opens the inbox folder
            Folder folderInbox = store.getFolder("INBOX");
            folderInbox.open(Folder.READ_ONLY);
            // fetches new messages from server

            SearchTerm sender = new FromTerm(new InternetAddress(id));
            Message[] arrayMessages = folderInbox.search(sender);

            Message lastMessage = arrayMessages[arrayMessages.length -1];
            String from = id;
            String subject = lastMessage.getSubject();
            String sentDate = lastMessage.getSentDate().toString();
            String contentType = lastMessage.getContentType();
            String messageContent = "";
            boolean message_seen=lastMessage.getFlags().contains(Flags.Flag.SEEN);

            if (contentType.contains("text/plain")
                    || contentType.contains("text/html")) {
                Object content = lastMessage.getContent();
                if (content != null) {
                    messageContent = content.toString();
                }
            }
/*
            System.out.println("message seen ?:"+message_seen);
            System.out.println("\t From: " + from);
            System.out.println("\t Subject: " + subject);
            System.out.println("\t Sent Date: " + sentDate);
            System.out.println("\t Message: " + messageContent);*/

            int receivedCode = getNumbers(messageContent);
            receivedConfirmationCode = receivedCode;
            /*if (receivedCode == ConfirmationCode) {
                System.out.println("Meme code");
            }else {
                System.out.println(ConfirmationCode);
                System.out.println("Pas meme code");
            }*/

            // disconnect
            folderInbox.close(false);
            store.close();
        } catch (NoSuchProviderException ex) {
            System.out.println("No provider for imap.");
            ex.printStackTrace();
        } catch (MessagingException ex) {
            System.out.println("Could not connect to the message store");
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return receivedConfirmationCode;
    }

    public static void main(String[] args) {
        //int sentConfirmationCode = sendConfirmationMail("projetcrypto23@outlook.fr");
        int receivedConfirmationCode = confirmConfirmationCode("projetcrypto23@outlook.fr");
        //System.out.println("Sent = " +sentConfirmationCode + "\nReceived = " +receivedConfirmationCode);
        System.out.println("\nReceived = " +receivedConfirmationCode);
    }

    public KeyPair authentication (String id) {

        String a = "";
        Element b = new Element() {
            @Override
            public Field getField() {
                return null;
            }

            @Override
            public int getLengthInBytes() {
                return 0;
            }

            @Override
            public boolean isImmutable() {
                return false;
            }

            @Override
            public Element getImmutable() {
                return null;
            }

            @Override
            public Element duplicate() {
                return null;
            }

            @Override
            public Element set(Element element) {
                return null;
            }

            @Override
            public Element set(int i) {
                return null;
            }

            @Override
            public Element set(BigInteger bigInteger) {
                return null;
            }

            @Override
            public BigInteger toBigInteger() {
                return null;
            }

            @Override
            public Element setToRandom() {
                return null;
            }

            @Override
            public Element setFromHash(byte[] bytes, int i, int i1) {
                return null;
            }

            @Override
            public int setFromBytes(byte[] bytes) {
                return 0;
            }

            @Override
            public int setFromBytes(byte[] bytes, int i) {
                return 0;
            }

            @Override
            public byte[] toBytes() {
                return new byte[0];
            }

            @Override
            public byte[] toCanonicalRepresentation() {
                return new byte[0];
            }

            @Override
            public Element setToZero() {
                return null;
            }

            @Override
            public boolean isZero() {
                return false;
            }

            @Override
            public Element setToOne() {
                return null;
            }

            @Override
            public boolean isEqual(Element element) {
                return false;
            }

            @Override
            public boolean isOne() {
                return false;
            }

            @Override
            public Element twice() {
                return null;
            }

            @Override
            public Element square() {
                return null;
            }

            @Override
            public Element invert() {
                return null;
            }

            @Override
            public Element halve() {
                return null;
            }

            @Override
            public Element negate() {
                return null;
            }

            @Override
            public Element add(Element element) {
                return null;
            }

            @Override
            public Element sub(Element element) {
                return null;
            }

            @Override
            public Element mul(Element element) {
                return null;
            }

            @Override
            public Element mul(int i) {
                return null;
            }

            @Override
            public Element mul(BigInteger bigInteger) {
                return null;
            }

            @Override
            public Element mulZn(Element element) {
                return null;
            }

            @Override
            public Element div(Element element) {
                return null;
            }

            @Override
            public Element pow(BigInteger bigInteger) {
                return null;
            }

            @Override
            public Element powZn(Element element) {
                return null;
            }

            @Override
            public ElementPowPreProcessing getElementPowPreProcessing() {
                return null;
            }

            @Override
            public Element sqrt() {
                return null;
            }

            @Override
            public boolean isSqr() {
                return false;
            }

            @Override
            public int sign() {
                return 0;
            }
        };
        return new KeyPair(a, b);
    }

}
