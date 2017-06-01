package it.gioaudino.game.Client;

import com.mashape.unirest.http.exceptions.UnirestException;
import it.gioaudino.game.Entity.ClientStatus;
import it.gioaudino.game.Entity.Game;
import it.gioaudino.game.Entity.Peer;
import it.gioaudino.game.Exception.ExitClientException;
import it.gioaudino.game.Exception.HTTPException;
import it.gioaudino.game.Service.ClientRESTCommunicationService;

import java.text.MessageFormat;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Created by gioaudino on 16/05/17.
 */
public class UserInteractionHandler {

    private static Scanner in = new Scanner(System.in);
    private static final String[] notPlayingChoices = {"1 - Change your username", "2 - Create new game", "3 - List existing games", "4 - Join existing game", "5 - Get out!"};
    private static final String[] playingChoices = {"I. Move with WASD", "II. throw bomb with B", "III. Quit with Q"};
    private static final Pattern inputPattern = Pattern.compile("[WASDQB]", Pattern.CASE_INSENSITIVE);

    public static void printMenu(ClientObject client) throws InterruptedException, ExitClientException {
        try {
            switch (client.getStatus()) {
                case STATUS_NEW:
                    printNewMenu(client);
                    break;
                case STATUS_NOT_PLAYING:
                    printNotPlayingMenu(client);
                    break;
                case STATUS_PLAYING:
                    printPlaying(client);
                    Thread.sleep(2000);
                    throw new ExitClientException();
            }
        } catch (ExitClientException e) {
            in.close();
            throw e;
        }
    }

    private static void printNewMenu(ClientObject client) {
        if (client.getUser() == null) {
            System.out.print("Hi! Welcome to the game. Please tell me your username: ");
            String username = in.nextLine();
            client.buildPeer(username);
            client.setStatus(ClientStatus.STATUS_NOT_PLAYING);
        }
    }

    private static void printNotPlayingMenu(ClientObject client) throws ExitClientException {
        System.out.println("Hello " + client.getUser().getUsername() + ". Please state your intentions: ");
        System.out.println();
        for (String i : notPlayingChoices) System.out.println(i);
        try {
            int choice = in.nextInt();
            in.nextLine();

            switch (choice) {
                case 1: // change username
                    changeUsername(client);
                    break;
                case 2: // create new game
                    createNewGame(client);
                    break;
                case 3: // get list of games
                    getGamesList();
                    break;
                case 4: // join existing game
                    joinExistingGame(client);
                    break;
                case 5: // die
                    System.out.println("Thanks for playing! Goodbye!");
                    throw new ExitClientException();
                default:
                    System.out.println("Sorry, I don't know that.");
            }
        } catch (Exception e) {
            System.out.println("Sorry, I don't know that.");
        }
    }

    private static void changeUsername(ClientObject client) {
        System.out.print("Type your new username: ");
        String username = in.nextLine();
        Peer user = client.getUser();
        user.setUsername(username);
        client.setUser(user);
    }

    private static void createNewGame(ClientObject client) {
        System.out.print("Choose game name: ");
        String gameName = in.nextLine();
        try {
            if (ClientRESTCommunicationService.tryGameName(gameName)) {
                System.out.println("The game " + gameName + " exists already.");
            } else {
                int side;
                do {
                    System.out.print("Choose field side -- should be an even number: ");
                    side = in.nextInt();
                    in.nextLine();
                } while (side % 2 != 0);
                System.out.print("Choose score goal: ");
                int goal = in.nextInt();
                in.nextLine();
                Game newGame = new Game(gameName, side, goal, client.getUser());
                Game gameResponse = ClientRESTCommunicationService.createNewGame(newGame);
                if (null != gameResponse) {
                    client.setGame(gameResponse);
                    client.setStatus(ClientStatus.STATUS_PLAYING);
                    System.out.println("Well done! You created " + gameResponse.getName() + "!");
                }
            }
            new Thread(new MovePerformerRunnable(client)).start();
        } catch (UnirestException e) {
            e.printStackTrace();
        } catch (HTTPException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void getGamesList() {
        System.out.println("Here are the games you can join:\n");
        List<Game> games = null;
        try {
            games = ClientRESTCommunicationService.getExistingGames();
        } catch (UnirestException e) {
            e.printStackTrace();
        } catch (HTTPException e) {
            System.out.println(e.getMessage());
        }

        if (null != games) {
            for (Game g : games) {
                System.out.println(g.getName());
                System.out.println("\tGrid side length: " + g.getSize() + "\n\tPoints to win: " + g.getPoints() + "\n\tCreated at: " + g.getCreatedAt());
            }
        }
    }

    private static void joinExistingGame(ClientObject client) {
        System.out.print("Which game would you like to join? ");
        String gameName = in.nextLine();
        try {
            if (!ClientRESTCommunicationService.tryGameName(gameName)) {
                System.out.println("The game " + gameName + " does not exist.");
            } else {
                Game game = ClientRESTCommunicationService.joinExistingGame(gameName, client.getUser());
                client.setGame(game);
                client.setStatus(ClientStatus.STATUS_PLAYING);
                new Thread(client::prepareToJoinGame).start();

                System.out.println("Welcome to " + game.getName() + ". There are " + (game.getPeers().size() - 1) + " other players");
            }
        } catch (UnirestException e) {
            e.printStackTrace();
        } catch (HTTPException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void printPlaying(ClientObject client) {
        System.out.println("--------------------");
        MessageFormat format = new MessageFormat("{0} playing game *{1}*: {2}/{3} points - {4} bombs --- Position: {5}");
        Object[] args = {client.getUser().getUsername(), client.getGame().getName(), client.getScore(), client.getGame().getPoints(), "#", client.getPosition()};
        System.out.println(format.format(args));
        String input;
        do {
            for (String i : playingChoices) System.out.println(i);
            input = in.nextLine();
        } while (!inputPattern.matcher(input).matches());


    }


}
