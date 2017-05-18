package it.gioaudino.game.Client;

import com.mashape.unirest.http.exceptions.UnirestException;
import it.gioaudino.game.Entity.ClientStatus;
import it.gioaudino.game.Entity.Game;
import it.gioaudino.game.Entity.Peer;
import it.gioaudino.game.Exception.HTTPException;
import it.gioaudino.game.Service.ClientRESTCommunicationService;

import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;

/**
 * Created by gioaudino on 16/05/17.
 */
public class UserInteractionHandler {
    private static final String[] choices = {"1 - Change your username", "2 - Create new game", "3 - List existing games", "4 - Join existing game", "5 - Get out!"};

    public static void printMenu(ClientObject client, Scanner in, PrintStream out) {
        switch (client.getStatus()) {
            case STATUS_NEW:
                printNewMenu(client, in, out);
                break;
            case STATUS_NOT_PLAYING:
                printNotPlayingMenu(client, in, out);
                break;
            case STATUS_PLAYING:
                break;
        }
    }

    private static void printNewMenu(ClientObject client, Scanner in, PrintStream out) {
        if (client.getUser() == null) {
            out.print("Hi! Welcome to the game. Please tell me your username: ");
            String username = in.nextLine();
            client.buildPeer(username);
            client.setStatus(ClientStatus.STATUS_NOT_PLAYING);
        }
    }

    private static void printNotPlayingMenu(ClientObject client, Scanner in, PrintStream out) {
        out.println("Hello " + client.getUser().getUsername() + ". Please state your intentions: ");
        for (String i : choices) System.out.println(i);

        int choice = in.nextInt();
        in.nextLine();

        switch (choice) {
            case 1: // change username
                changeUsername(client, in, out);
                break;
            case 2: // create new game
                createNewGame(client, in, out);
                break;
            case 3: // get list of games
                getGamesList(out);
                break;
            case 4: // join existing game
                joinExistingGame(client, in, out);
                break;
            case 5: // die
                out.println("Thanks for playing! Goodbye!"); // TODO should I tell the server I died?
                in.close();
                System.exit(0);
                break;
            default:
                out.println("Sorry, I don't know that.");
        }
    }

    private static void changeUsername(ClientObject client, Scanner in, PrintStream out) {
        out.print("Type your new username: ");
        String username = in.nextLine();
        Peer user = client.getUser();
        user.setUsername(username);
        client.setUser(user);
    }

    private static void createNewGame(ClientObject client, Scanner in, PrintStream out) {
        out.print("Choose game name: ");
        String gameName = in.nextLine();
        try {
            if (ClientRESTCommunicationService.tryGameName(gameName)) {
                out.println("The game " + gameName + " exists already.");
            } else {
                int side;
                do {
                    out.print("Choose field side -- should be an even number: ");
                    side = in.nextInt();
                    in.nextLine();
                } while (side % 2 != 0);
                out.print("Choose score goal: ");
                int goal = in.nextInt();
                in.nextLine();
                Game newGame = new Game(gameName, side, goal, client.getUser());
                Game gameResponse = ClientRESTCommunicationService.createNewGame(newGame);
                if (null != gameResponse) {
                    client.setGame(gameResponse);
                    client.setStatus(ClientStatus.STATUS_PLAYING);
                    out.println("Well done! You created " + gameResponse.getName() + "!");
                }
            }
        } catch (UnirestException e) {
            e.printStackTrace();
        } catch (HTTPException e) {
            out.println(e.getMessage());
        }
    }

    private static void getGamesList(PrintStream out) {
        out.println("Here are the games you can join:\n");
        List<Game> games = null;
        try {
            games = ClientRESTCommunicationService.getExistingGames();
        } catch (UnirestException e) {
            e.printStackTrace();
        } catch (HTTPException e) {
            out.println(e.getMessage());
        }

        if (null != games) {
            for (Game g : games) {
                out.println(g.getName());
                out.println("\tGrid side length: " + g.getSize() + "\n\tPoints to win: " + g.getPoints() + "\n\tCreated at: " + g.getCreatedAt());
            }
        }
    }

    private static void joinExistingGame(ClientObject client, Scanner in, PrintStream out) {
        out.print("Which game would you like to join? ");
        String gameName = in.nextLine();
        try {
            if (!ClientRESTCommunicationService.tryGameName(gameName)) {
                out.println("The game " + gameName + " does not exist.");
            } else {
                Game game = ClientRESTCommunicationService.joinExistingGame(gameName, client.getUser());
                client.setGame(game);
                client.setStatus(ClientStatus.STATUS_PLAYING);
                out.println("Welcome to " + game.getName() + ". There are " + (game.getPeers().size() - 1) + " other players");
            }
        } catch (UnirestException e) {
            e.printStackTrace();
        } catch (HTTPException e) {
            out.println(e.getMessage());
        }

    }

}
