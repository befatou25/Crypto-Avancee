package securitySysteme.autority;

import it.unisa.dia.gas.jpbc.Element;
import it.unisa.dia.gas.jpbc.Pairing;
import it.unisa.dia.gas.plaf.jpbc.pairing.PairingFactory;

import javax.crypto.*;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.SecretKeySpec;
import javax.mail.MessagingException;
import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static securitySysteme.autority.Authentication.confirmConfirmationCode;
import static securitySysteme.autority.Authentication.sendConfirmationMail;


public class ClientKeyGen {
    public static Pairing pairing = PairingFactory.getPairing("src/main/resources/curves/a.properties");
    public static SettingParameters settingParameters = setup();
    public static String id= "projetcrypto23@outlook.fr";


    public static SettingParameters setup(){ // setup phase
        Element p= ClientKeyGen.pairing.getG1().newRandomElement(); // choix d'un générateur
        Element msk= ClientKeyGen.pairing.getZr().newRandomElement(); //choix de la clef du maitre
        Element p_pub=p.duplicate().mulZn(msk); // calcule de la clef publique du système

        return new SettingParameters(p, p_pub, msk); //instanciation d'un objet comportant les parametres du système
    }

    public static KeyPair generateClientsIBEKeys(String id) {

        byte [] bytes=id.getBytes(); // représentation de l'id sous format binaire

        Element Q_id=pairing.getG1().newElementFromHash(bytes, 0, bytes.length); //H_1(id)

        Element sk=Q_id.duplicate().mulZn(settingParameters.getMsk()); // calcule de la clef privée correspandante à id

        return new KeyPair(id, sk); // instanciation d'un objet comportant les composants de la clefs (clef publique=id et clef privée)
    }


    public static List<Object> dHServerConfiguration(PublicKey clientPublicKey) throws NoSuchAlgorithmException, InvalidKeyException, InvalidAlgorithmParameterException {
        var serverParams = new ArrayList<>();
        byte[] sharedSecret;

        // create a Diffie-Hellman key pair generator
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("DH");
        keyPairGenerator.initialize(((DHPublicKey)clientPublicKey).getParams());

        // generate the server's key pair
        java.security.KeyPair serverKeyPair = keyPairGenerator.generateKeyPair();

        // create a key agreement object and initialize it with the server's private key
        KeyAgreement keyAgreement = KeyAgreement.getInstance("DH");
        keyAgreement.init(serverKeyPair.getPrivate());

        // generate the shared secret using the client's public key and the server's private key
        keyAgreement.doPhase(clientPublicKey, true);
        sharedSecret = keyAgreement.generateSecret();

        serverParams.add(serverKeyPair);
        serverParams.add(sharedSecret);

        return serverParams;
    }


    /*
    This method encrypts the client's IBE Keypair then return it with the DH server public key in a list
     */
    public static List<byte[]> configAndAESClientIBESecretKey(PublicKey clientPubKey, KeyPair clientKeyPair) throws NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        List<byte[]> configsAndAESKeyToClient = new ArrayList<>();
        var serverParams = dHServerConfiguration(clientPubKey);
        java.security.KeyPair dHserverKeyPair = (java.security.KeyPair) serverParams.get(0);
        byte[] sharedSecret = (byte[]) serverParams.get(1);
        // The AES secretKeySpec is computed using the shared secret
        SecretKeySpec secretKeySpec = new SecretKeySpec(Arrays.copyOf(sharedSecret, 16), "AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
        Element clientIBESecretKey = clientKeyPair.getSk();
        byte[] aESEncryptedClientIBEKey = cipher.doFinal(clientIBESecretKey.toBytes());

        configsAndAESKeyToClient.add(settingParameters.getP().toBytes());
        configsAndAESKeyToClient.add(settingParameters.getP_pub().toBytes());
        configsAndAESKeyToClient.add(settingParameters.getMsk().toBytes());
        System.out.println("Server's DH PubKey:\n" + dHserverKeyPair.getPublic());
        configsAndAESKeyToClient.add(dHserverKeyPair.getPublic().getEncoded());
        configsAndAESKeyToClient.add(aESEncryptedClientIBEKey);

        return configsAndAESKeyToClient;
    }

    public static List<byte[]> triggerCreationAndSendConfigsAndAESClientIBESk(String id) throws MessagingException, IOException, NoSuchAlgorithmException, InvalidKeySpecException, InterruptedException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        PublicKey clientsPublicKey = null;

        int sentConfirmationCode = sendConfirmationMail(id);
        System.out.println("Waiting 1 min for client's response...");
        try {
            Thread.sleep(60000); // wait for 1 min
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
        System.out.println("Client authenticated successfully...\n");

        var clientsList = confirmConfirmationCode(id);
        if (sentConfirmationCode == (int) clientsList.get(0)) {
            clientsPublicKey = (PublicKey) clientsList.get(1);
        }
        System.out.println("Client's Public Key:\n" + clientsPublicKey);

        KeyPair clientKeyPair = generateClientsIBEKeys(id);

        System.out.println("\nIBE pk: " + clientKeyPair.getPk() + "\nIBE sk: " + clientKeyPair.getSk());
        var sendToClient = configAndAESClientIBESecretKey(clientsPublicKey, clientKeyPair);
        byte[] clientIBEskAES = (byte[]) sendToClient.get(2);
        System.out.println("AESEncryptedClientIBEsecretKey: " + Arrays.toString(clientIBEskAES));

        return sendToClient;
    }
}
