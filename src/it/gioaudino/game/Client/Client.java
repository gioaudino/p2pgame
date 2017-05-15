package it.gioaudino.game.Client;

import it.gioaudino.game.Entity.Peer;
import it.gioaudino.game.Exception.BadRequestException;
import it.gioaudino.game.Service.ClientRESTCommunicationService;
import it.gioaudino.game.Service.GsonService;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Scanner;

/**
 * Created by gioaudino on 15/05/17.
 */
public class Client {

    private static final String[] choices = {"1 - Change your username", "2 - Create new game", "3 - List existing games", "4 - Join existing game", "5 - Get out!"};

    public static void main(String[] args) throws IOException, Exception {
        ServerSocket s = new ServerSocket(0);
        Scanner input = new Scanner(System.in);
        System.out.print("Hi! Welcome to the game. Please state your username: ");
        String username = input.nextLine();
        Peer user = new Peer(username, s.getInetAddress().getHostAddress(), s.getLocalPort());

        System.out.println("Hello " + username + ". Please state your intentions: ");
        for(String i: choices) System.out.println(i);

        int choice = input.nextInt();
        if(choice == 4) System.exit(0);

        System.out.println("Good choice! - " + choices[choice-1].substring(4));
        ClientRESTCommunicationService cs = new ClientRESTCommunicationService(user);
        cs.getGames();
        System.exit(10);
        cs.getExistingGames();
        System.out.println(GsonService.getSimpleInstance().toJson(user));

        input.close();
    }
}
