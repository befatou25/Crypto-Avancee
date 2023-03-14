import java.util.Scanner;

import static src.Mailsendreceivetest.downloadEmailAttachments;
import static src.Mailsendreceivetest.sendmessage;
import static src.Mailsendreceivetest.sendmessagewithattachement;
import static src.Reply2.Reply2;
import static src.Informations.CollectInfo;

public class main {
    public static void main(String[] args)  {

        Scanner scanner = new Scanner(System.in);
        System.out.println("Veuillez entrez l'adresse mail d'envoie : ");
        String username = scanner.next();

        System.out.println("Veuillez entrez le mdp du mail d'envoie : ");
        String password = scanner.next();

        System.out.println("Veuillez entrez l'adresse mail de réception : ");
        String destination = scanner.next();

        System.out.println("Les informations entrées sont les suivantes : \n " + username + "\n" + password + "\n" + destination);

        scanner.close();
        //String host = "outlook.office365.com";//change accordingly
        /**String username= "projetcrypto23@outlook.fr";
        String password= "projetCrypto2023*";//change accordingly
        String destination = "khaoulaaitka10@gmail.com";**/
        //sendmessage(username, password, destination);

        String path="C:\\Users\\khawla\\Desktop\\Study\\S8\\Crypto_Avancée\\Cours\\Cours IBE.pptx";

        sendmessagewithattachement(username, password, destination,path);

        System.out.println("message sent ...");

        Scanner sc=new Scanner(System.in);
        System.out.println("type something ....");

        sc.nextLine();

        //downloadEmailAttachments(username, password);
        System.out.println("**********Maintenant je vais répondre à l'email***********");
        Reply2(username,password,destination);
    }
}
