package securitySysteme.autority;

import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import securitySysteme.ClientsMail.IBEBasicIdent;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.FromTerm;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

public class Authentication {

    public static final String TRUSTEDAUTHORITY = "autoritedeconfiance@outlook.com";
    public static final String TRUSTEDAUTHORITYPASSWORD = "AutoriteConfiance2023*";
    public static final Pairing pairing = PairingFactory.getPairing("src/main/resources/curves/a.properties"); // chargement des paramètres de la courbe elliptique
    // la configuration A offre un pairing symetrique ce qui correspond à l'implementation du schema basicID
    // qui est basé sur l'utilisation du pairing symetrique
    public static final SettingParameters sp = IBEBasicIdent.setup(pairing);
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
    static int getNumbers(String s) {

        String[] n = s.split(""); //array of strings
        StringBuilder f = new StringBuilder(); // buffer to store numbers

        for (String value : n) {
            if ((value.matches("\\d+"))) { // validating numbers
                f.append(value); //appending
            } else {
                //parsing to int and returning value
                return Integer.parseInt(f.toString());
            }
        }
        return 0;
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
            message.setSubject("Code de confirmation");
            message.setText("Votre code de confirmation est : " + CONFIRMATIONCODE + "\nVeuillez répondre à ce mail en entrant votre code");

            // Envoi de l'e-mail
            Transport.send(message);

            System.out.println("L'e-mail avec le code de confirmation a été envoyé avec succès.");

        } catch (MessagingException e) {
            System.out.println("Une erreur s'est produite lors de l'envoi de l'e-mail de confirmation : " + e.getMessage());
        }
        return CONFIRMATIONCODE;
    }


    public static List<Object> confirmConfirmationCode(String id) throws MessagingException, IOException, InterruptedException {
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
        var receivedClientPublicKey = "";
        var sender = new FromTerm(new InternetAddress(id));

        while (true) {
            Session session = Session.getDefaultInstance(properties);
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
                            receivedClientPublicKey = new String(receivedClientPublicKeyBytes);
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

    public static boolean verifyAuthentication(String id) throws MessagingException, IOException, InterruptedException {
        int sentConfirmationCode = sendConfirmationMail(id);
        System.out.println("Waiting 1 min for client's response...");
        try {
            Thread.sleep(60000); // wait for 1 min
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
        return sentConfirmationCode == (int) confirmConfirmationCode(id).get(0);
    }

    public static PublicKey parsePublicKey(String publicKeyString) throws Exception {
        byte[] publicKeyBytes = new byte[publicKeyString.length() / 2];
        for (int i = 0; i < publicKeyString.length(); i += 2) {
            publicKeyBytes[i / 2] = (byte) ((Character.digit(publicKeyString.charAt(i), 16) << 4) + Character.digit(publicKeyString.charAt(i+1), 16));
        }

        // Create a PublicKey object from the byte array
        KeyFactory keyFactory = KeyFactory.getInstance("DiffieHellman");
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
        PublicKey clientPublicKey = keyFactory.generatePublic(keySpec);

        // Generate a public key object from the public key specification and return it
        return clientPublicKey;
    }

    public static PublicKey getClientsDHPublicKey(String id) throws Exception {
        String clientsDHPublicKeyString = (String) confirmConfirmationCode(id).get(1);
        byte[] clientsDHPublicKeyBytes = clientsDHPublicKeyString.getBytes();
        String base64ClientsDHPublicKeyString = Base64.getEncoder().encodeToString(clientsDHPublicKeyBytes);

        return parsePublicKey(base64ClientsDHPublicKeyString);
    }


    public static void main(String[] args) throws Exception {
        /*var id = "projetcrypto23@outlook.fr";
        PublicKey clientsPublicKey = null;
        if (verifyAuthentication(id)) {
            clientsPublicKey = getClientsDHPublicKey(id);
        };*/
        PublicKey p = parsePublicKey("2c2f20db08f88719c9d80532203f7fbc6fdc77ec0a6873de280d6751981c2a3463d8c366cb133c21ede81958450d17f6608e964d3d799105637ab7c405af7b1f88a6dabfc8457341df8fc79484d80f3c0f3429835cd868c78c3fc8fc0996104de8b403c59976e906956193a1e366a40c043dd1aea6127c7c778ccf9fb2c9f04746dbc6645ea90a3fec83e71a1d24466fe725dbf4d3a988f5beeb2714dfb108c2f5caa80ea96819ff226bfe270c509c594a82e0c522117baaacbe743f172c3cd024a5db25533e4b243e302de71760bdf94a6905f5e281b1a8d9a03e3376965b6de77d5bf857fa76d851365d8d957050daa4545dc7ad387ed8bdb7b11e30529795");
        System.out.println("\nClient's Public Key:\n" + p);

    }

}