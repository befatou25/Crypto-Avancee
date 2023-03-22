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
import javax.mail.internet.MimeMultipart;
import javax.mail.search.FromTerm;
import javax.mail.search.SearchTerm;
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

    public static int sendConfirmationMail(String id) {
        var properties = new Properties();

        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.office365.com");
        properties.put("mail.smtp.port", "587");
        Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(trustedauthority, trustedauthorityPassword);
            }
        });
        System.out.println("session.getProviders():" + session.getProviders()[0].getType());

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
            if ((n[i].matches("[0-9]+"))) {// validating numbers
                f.append(n[i]); //appending
            } else {
                //parsing to int and returning value
                return Integer.parseInt(f.toString());
            }
        }
        return 0;
    }

    public static int confirmConfirmationCode(String id) throws Exception {
        var properties = new Properties();

        // server setting (it can be pop3 too
        properties.put("mail.imap.host", "outlook.office365.com");
        properties.put("mail.imap.port", "993");
        properties.setProperty("mail.imap.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        properties.setProperty("mail.imap.socketFactory.fallback", "false");
        properties.setProperty("mail.imap.socketFactory.port", "993");

        int receivedConfirmationCode = 0;
        SearchTerm sender = new FromTerm(new InternetAddress(id));

        while (true) {
            Session session = Session.getDefaultInstance(properties);
            Store store = session.getStore("imap");
            store.connect(trustedauthority, trustedauthorityPassword);
            Folder folderInbox = store.getFolder("INBOX");
            folderInbox.open(Folder.READ_ONLY);
            Message[] arrayMessages = folderInbox.search(sender);


            if (arrayMessages.length > 0) {
                Message lastMessage = arrayMessages[arrayMessages.length -1];
                String contentType = lastMessage.getContentType();
                String messageContent = "";

                if (contentType.contains("multipart/")) {
                    MimeMultipart multipart = (MimeMultipart) lastMessage.getContent();
                    for (int i = 0; i < multipart.getCount(); i++) {
                        BodyPart bodyPart = multipart.getBodyPart(i);
                        if (bodyPart.getContentType().contains("text/plain")) {
                            Object content = bodyPart.getContent();
                            if (content != null) {
                                messageContent = content.toString();
                                receivedConfirmationCode = getNumbers(messageContent);

                            }
                        }
                    }
                }

                if (contentType.contains("text/plain")
                        || contentType.contains("text/html")) {
                    Object content = lastMessage.getContent();
                    if (content != null) {
                        messageContent = content.toString();
                        receivedConfirmationCode = getNumbers(messageContent);
                    }
                }
                break;
            }

            Thread.sleep(10000);

            // disconnect
            folderInbox.close(false);
            store.close();
        }

            return receivedConfirmationCode;
    }



    public static void main(String[] args) throws Exception {
        int sentConfirmationCode = sendConfirmationMail("projetcrypto23@outlook.fr");
       System.out.println("Starting...");
        try {
            Thread.sleep(30000); // wait for 0.5 min
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int receivedConfirmationCode = confirmConfirmationCode("projetcrypto23@outlook.fr");
        System.out.println("Sent = " +sentConfirmationCode + "\nReceived = " +receivedConfirmationCode);
        //System.out.println("\nReceived = " +receivedConfirmationCode);
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
