package srcBis;

import java.util.Scanner;

import static src.Mailsendreceivetest.sendmessagewithattachement;
import static srcBis.Reply2Bis.NumMail;

public class MainBis {
    public static void main(String[] args)  {

        Scanner scanner = new Scanner(System.in);
        System.out.println("Veuillez entrez l'adresse mail d'envoie : ");
        String usernamesend = scanner.next();

        System.out.println("Veuillez entrez le mdp du mail d'envoie : ");
        String passwordsend = scanner.next();

        System.out.println("Veuillez entrez l'adresse mail de réception : ");
        String destinationsend = scanner.next();

        //System.out.println("Les informations entrées sont les suivantes : \n " + username + "\n" + password + "\n" + destination);


        //String host = "outlook.office365.com";//change accordingly
        /**Pour le test :
         usernamesendsend= "projetcrypto23@outlook.fr";
         passwordsend= "projetCrypto2023*";
         destinationsend = testcrypto23@outlook.fr;
         *******************************************
         usernamereply = testcrypto23@outlook.fr;
         passwordreply = testcryptogra23*;
         destinationreply = projetcrypto23@outlook.fr**/

        //sendmessage(username, password, destination);

        String path="C:\\Users\\befat\\OneDrive\\Bureau\\img.jpg";

        sendmessagewithattachement(usernamesend, passwordsend, destinationsend,path);

        System.out.println("message sent ...");

        //Scanner sc=new Scanner(System.in);
        //System.out.println("type something ....");
        //sc.nextLine();

        //downloadEmailAttachments(username, password);
        System.out.println("**********Maintenant je vais répondre à l'email***********");

        Scanner scanner2 = new Scanner(System.in);


        System.out.println("Veuillez entrez l'adresse mail d'envoie de la réponse : ");
        String usernamereply = scanner2.next();

        System.out.println("Veuillez entrez le mdp du mail d'envoie de la réponse : ");
        String passwordreply = scanner2.next();

        //System.out.println("Veuillez entrez l'adresse mail de réception  de la réponse : ");
        //String destinationreply = scanner2.next();

        scanner.close();
        scanner2.close();

        //Reply2(usernamereply,passwordreply,destinationreply);

        /**String usernamereply = "testcrypto23@outlook.fr";
         String passwordreply = "testcryptogra23*";**/


        NumMail(usernamereply,passwordreply);
        Scanner sc = new Scanner(System.in);
        System.out.println("Veuillez entrez l'id du mail auquel vous voulez répondre 2 : ");
        //String IDMail = sc.next();
        //Reply2(usernamereply,passwordreply, sc);

        Reply2Bis.Reply2(usernamereply,passwordreply, sc);
        sc.close();
    }
}