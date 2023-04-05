package securitySysteme.autority;

import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

import javax.crypto.spec.DHPublicKeySpec;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.FromTerm;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Authentication {

    public static final String TRUSTEDAUTHORITY = "autoritedeconfiance@outlook.com";
    public static final String TRUSTEDAUTHORITYPASSWORD = "AutoriteConfiance2023*";
    public static final Pairing pairing = PairingFactory.getPairing("src/main/resources/curves/a.properties"); // chargement des paramètres de la courbe elliptique
    // la configuration A offre un pairing symetrique ce qui correspond à l'implementation du schema basicID
    // qui est basé sur l'utilisation du pairing symetrique
    public static final int CONFIRMATIONCODE = generateConfirmationCode();


    /**
     * This method generates a random number that will be used as the confirmation code of a client
     * */
    private static int generateConfirmationCode() {
        Random rand = new Random();
        return rand.nextInt(999999);
    }

    /**
     * This method returns the first number of a given string
     * */
    public static int getNumbers(String s) {
        StringBuilder numbers = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (Character.isDigit(c)) {
                numbers.append(c);
            } else {
                break;
            }
        }
        return Integer.parseInt(numbers.toString());
    }

    /**
     * This method sends a confirmation code via an e-mail to the client with id as the mail address
     *  It returns the CONFIRMATIONCODE sent in order to be able to perform comparison with the code the authority
     *  will receive from the client.
      */
    public static int sendConfirmationMail(String id) {
        var properties = new Properties();

        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.host", "smtp.office365.com");
        properties.put("mail.smtp.port", "587");
        Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(TRUSTEDAUTHORITY, TRUSTEDAUTHORITYPASSWORD);
            }
        });
        System.out.println("session.getProviders():" + session.getProviders()[0].getType());

        try {
            // Création de l'e-mail
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(TRUSTEDAUTHORITY));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(id));
            message.setSubject("Code de vérification");
            message.setText("Votre code de vérification est : " + CONFIRMATIONCODE + "\nVous avez 3 min pour répondre à ce mail avec le même code de vérification en y joignant votre clef publique DH.");

            // Envoi de l'e-mail
            Transport.send(message);

            System.out.println("L'e-mail avec le code de vérification a été envoyé avec succès.");

        } catch (MessagingException e) {
            System.out.println("Une erreur s'est produite lors de l'envoi de l'e-mail de confirmation : " + e.getMessage());
        }
        return CONFIRMATIONCODE;
    }


    public static List<Object> confirmConfirmationCode(String id) throws MessagingException, IOException, InterruptedException, NoSuchAlgorithmException, InvalidKeySpecException {
        var properties = new Properties();

        // server setting (it can be pop3 too
        properties.put("mail.imap.host", "outlook.office365.com");
        properties.put("mail.imap.port", "993");
        properties.setProperty("mail.imap.socketFactory.class",
                "javax.net.ssl.SSLSocketFactory");
        properties.setProperty("mail.imap.socketFactory.fallback", "false");
        properties.setProperty("mail.imap.socketFactory.port", "993");

        var clientResponse = new ArrayList<>();
        var receivedConfirmationCode = 0;
        PublicKey receivedClientPublicKey = null;
        var sender = new FromTerm(new InternetAddress(id));

        Session session = Session.getDefaultInstance(properties);
        while (true) {
            Store store = session.getStore("imap");
            store.connect(TRUSTEDAUTHORITY, TRUSTEDAUTHORITYPASSWORD);
            Folder folderInbox = store.getFolder("INBOX");
            folderInbox.open(Folder.READ_ONLY);
            Message[] arrayMessages = folderInbox.search(sender);


            if (arrayMessages.length > 0) {
                var lastMessage = arrayMessages[arrayMessages.length -1];
                var contentType = lastMessage.getContentType();
                var messageContent = "";

                if (contentType.contains("multipart/")) {
                    var multipart = (MimeMultipart) lastMessage.getContent();
                    for (int i = 0; i < multipart.getCount(); i++) {
                        var bodyPart = multipart.getBodyPart(i);
                        if (bodyPart.getContentType().contains("text/plain") || bodyPart.getContentType().contains("text/html")) {
                            var content = bodyPart.getContent();
                            if (content != null) {
                                messageContent = content.toString();
                                if (receivedConfirmationCode == 0) {
                                    receivedConfirmationCode = getNumbers(messageContent);
                                }

                            }
                        }
                        if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())) {
                            var clientPublicKeyStream = bodyPart.getInputStream();
                            var receivedClientPublicKeyBytes = clientPublicKeyStream.readAllBytes();
                            var receivedClientPublicKeyString = new String(receivedClientPublicKeyBytes);
                            receivedClientPublicKey = getDHPublicKeyFromPublicKeyString(receivedClientPublicKeyString);
                        }
                    }
                }
                break;
            }

            Thread.sleep(10000);

            // disconnect
            folderInbox.close(false);
            store.close();
        }
        clientResponse.add(receivedConfirmationCode);
        clientResponse.add(receivedClientPublicKey);

        return clientResponse;
    }

    public static PublicKey getDHPublicKeyFromPublicKeyString(String publicKeyString) throws NoSuchAlgorithmException, InvalidKeySpecException, RuntimeException {
        PublicKey clientPublicKey;
        // Extract the values of y, p, g, and l using regular expressions
        Pattern yPattern = Pattern.compile("y:\r?\n\\s*([0-9a-f\\s]+)");
        Matcher yMatcher = yPattern.matcher(publicKeyString);
        if (!yMatcher.find()) {
            throw new RuntimeException("Could not find y");
        }
        String ystr = yMatcher.group(1).replaceAll("\\s+", "");
        BigInteger y = new BigInteger(ystr, 16);

        Pattern pPattern = Pattern.compile("p:\r?\n\\s*([0-9a-f\\s]+)");
        Matcher pMatcher = pPattern.matcher(publicKeyString);
        if (!pMatcher.find()) {
            throw new RuntimeException("Could not find p");
        }
        String pstr = pMatcher.group(1).replaceAll("\\s+", "");
        BigInteger p = new BigInteger(pstr, 16);

        Pattern gPattern = Pattern.compile("g:\r?\n\\s*([0-9a-f\\s]+)");
        Matcher gMatcher = gPattern.matcher(publicKeyString);
        if (!gMatcher.find()) {
            throw new RuntimeException("Could not find g");
        }
        String gstr = gMatcher.group(1).replaceAll("\\s+", "");
        BigInteger g = new BigInteger(gstr, 16);

        Pattern lPattern = Pattern.compile("l:\r?\n\\s*(\\d+)");
        Matcher lMatcher = lPattern.matcher(publicKeyString);
        if (!lMatcher.find()) {
            throw new RuntimeException("Could not find l");
        }
        int l = Integer.parseInt(lMatcher.group(1));

        // Print the values of y, p, g, and l
        System.out.println("y: " + y);
        System.out.println("p: " + p);
        System.out.println("g: " + g);
        System.out.println("l: " + l);

        // create a public key from the retrieved y value and the DH parameter specification
        KeyFactory keyFactory = KeyFactory.getInstance("DH");
        DHPublicKeySpec dhPublicKeySpec = new DHPublicKeySpec(y, p, g);
        clientPublicKey = keyFactory.generatePublic(dhPublicKeySpec);

        return clientPublicKey;
    }
}