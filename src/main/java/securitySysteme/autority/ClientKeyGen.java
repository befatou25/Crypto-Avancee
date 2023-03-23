package securitySysteme.autority;

import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;
import securitySysteme.ClientsMail.IBEBasicIdent;

import javax.crypto.KeyAgreement;
import java.security.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientKeyGen {
    //Authentication authentication = new Authentication();

    static Pairing pairing = PairingFactory.getPairing("src/main/resources/curves/a.properties");

    static SettingParameters settingParameters = IBEBasicIdent.setup(pairing);


    public static KeyPair generateClientsIBEKeys(String id) {
        KeyPair keys = null;
        try {
            keys = IBEBasicIdent.keygen(pairing, settingParameters.getMsk(), id);
        } catch (NoSuchAlgorithmException e) {
            Logger.getLogger(ClientKeyGen.class.getName()).log(Level.SEVERE, "NO SUCH ALGORITHM EXCEPTION", e);
        }
        return keys;
    }

    public static List<Object> diffieHellmanKeys(PublicKey clientPublicKey) throws NoSuchAlgorithmException, InvalidKeyException {
        var algo = "DiffieHellman";
        var dhList = new ArrayList<>();
        // Generate public and private keys for the server
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(algo);
        keyPairGen.initialize(2048);
        java.security.KeyPair keyPair = keyPairGen.generateKeyPair();
        PublicKey publicKey = keyPair.getPublic();
        PrivateKey privateKey = keyPair.getPrivate();

        // Receive the client's public key and generate shared secret
        KeyAgreement keyAgreement = KeyAgreement.getInstance(algo);
        keyAgreement.init(privateKey);
        keyAgreement.doPhase(clientPublicKey, true);
        byte[] sharedSecret = keyAgreement.generateSecret();
        System.out.println("Clients PK: " + clientPublicKey );

        dhList.add(publicKey);
        dhList.add(sharedSecret);
        return dhList;
    }


 /*   public static int sendClientsIBEKeys(String id) {
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
*/

    public static void main(String[] args) {
        var id = "projetcrypto23@outlook.fr";
        System.out.println(generateClientsIBEKeys(id));
    }

}
