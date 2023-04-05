package securitySysteme.autority;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.mail.MessagingException;
import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static securitySysteme.autority.ClientKeyGen.triggerCreationAndSendConfigsAndAESClientIBESk;

/**
 * La classe ServerHTTP définit le seveur HTTP
 * Elle permet de gérer les paramètres du système
 * Et génèreer les clés de chiffrement
 *
 *

 */
public class ServerHTTP {

    public static void main(String[] args) {

        try {
            // InetSocketAddress s = new InetSocketAddress("localhost", 8080);
            System.out.println("my address:"+ InetAddress.getLocalHost());
            InetSocketAddress s = new InetSocketAddress(InetAddress.getLocalHost(), 8090);
            //  InetSocketAddress s = new InetSocketAddress("localhost", 8080);


            HttpServer server = HttpServer.create(s, 1000);
            System.out.println(server.getAddress());
            server.createContext("/service", new HttpHandler()
            {
                public void handle(HttpExchange he) throws IOException {
                    byte[] bytes1 = new byte[Integer.parseInt(he.getRequestHeaders().getFirst("Content-length"))];
                    he.getRequestBody().read(bytes1);

                    // la récupération de l'id qui est l'adresse mail du client
                    String id = new String(bytes1);

                    System.out.println("id reçu " + id);
                    he.getRequestBody().close();

                    // L'authentification du client, la generation de la clé IBE, le chiffrer la clé IBE avec la clé public du client
                    List<byte[]> param;
                    try {
                        param = triggerCreationAndSendConfigsAndAESClientIBESk(id);
                    } catch (MessagingException | BadPaddingException | IllegalBlockSizeException |
                             NoSuchAlgorithmException | InvalidKeySpecException | InvalidAlgorithmParameterException | NoSuchPaddingException | InterruptedException | InvalidKeyException e) {
                        throw new RuntimeException(e);
                    }
                    File F = new File("Parametres");
                    F.createNewFile();
                    FileOutputStream  v = new FileOutputStream(F);
                    ObjectOutputStream o = new ObjectOutputStream(v);
                    o.writeObject(param);
                    v.close();

                    FileInputStream l = new FileInputStream(F);
                    byte[] fileContent = new byte[l.available()];
                    l.read(fileContent);

                    // l'envoi de la clé chiffrée à l'aide la clé public du client à ce dernier
                    he.sendResponseHeaders(200, fileContent.length);
                    OutputStream os = he.getResponseBody();
                    os.write(fileContent);

                    System.out.println("Clé envoyée au client");
                    os.close();

                }
            });

            server.start();
        } catch (IOException ex) {
            Logger.getLogger(ServerHTTP.class.getName()).log(Level.SEVERE, null, ex);
        }


    }


}
