package src;


import java.util.Scanner;

//Ceci est une classe pour pouvoir collecter les infos des user et le passeword
public class Informations {
    public static void CollectInfo(){

        Scanner scanner = new Scanner(System.in);
        System.out.println("Veuillez entrez l'adresse mail d'envoie : ");
        String username = scanner.next();

        System.out.println("Veuillez entrez le mdp du mail d'envoie : ");
        String password = scanner.next();

        System.out.println("Veuillez entrez l'adresse mail de réception : ");
        String destination = scanner.next();

        System.out.println("Les informations entrées sont les suivantes : \n " + username + "\n" + password + "\n" + destination);

        scanner.close();


    }
}
