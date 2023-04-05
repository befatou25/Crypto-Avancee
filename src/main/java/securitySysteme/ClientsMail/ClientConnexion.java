package securitySysteme.ClientsMail;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientConnexion {

    static String id = "testcrypto23@outlook.fr";
    public static void main(String[] args) {


        try {
            URL url = new URL("http://192.168.56.1:8090/service");
            // URL url = new URL("https://www.google.com");

            URLConnection urlConn = url.openConnection();
            urlConn.setDoInput(true);
            urlConn.setDoOutput(true);
            /*
            Rajouter la creation du fichier myPubKey qui stocke la clef pub qui devra tre envoyée au serveur avec generateDHclientKeyPair
             */
            OutputStream out=urlConn.getOutputStream();
            //out.write(user_name.getBytes());
            System.out.println("Demande de la clef IBE envoyée ...");
            out.write(id.getBytes());

            InputStream dis = urlConn.getInputStream();
            byte[] b=new byte[Integer.parseInt(urlConn.getHeaderField("Content-length"))];
            dis.read(b);

            File F = new File("Parametres");
            F.createNewFile();
            FileOutputStream v = new FileOutputStream(F);
            v.write(b);
            v.close();

            FileInputStream l = new FileInputStream(F);
            ObjectInputStream o = new ObjectInputStream(l);
            List<byte[]> param = (List<byte[]>) o.readObject();

            System.out.println("message reçu du serveur:"+param);

        } catch (MalformedURLException ex) {
            Logger.getLogger(ClientConnexion.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ClientConnexion.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
