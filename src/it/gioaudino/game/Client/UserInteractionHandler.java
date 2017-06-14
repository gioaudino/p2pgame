package it.gioaudino.game.Client;

import com.mashape.unirest.http.exceptions.UnirestException;
import it.gioaudino.game.Entity.*;
import it.gioaudino.game.Exception.ExitClientException;
import it.gioaudino.game.Exception.HTTPException;
import it.gioaudino.game.Exception.IllegalMoveException;
import it.gioaudino.game.Service.ClientRESTCommunicationService;

import java.text.MessageFormat;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Created by gioaudino on 16/05/17.
 */
public class UserInteractionHandler {

    private static Scanner in = new Scanner(System.in);
    private static final String[] notPlayingChoices = {"1 \u2014 Change your username", "2 \u2014 Create new game", "3 \u2014 List existing games", "4 \u2014 Join existing game", "5 \u2014 Get out!"};
    private static final String[] playingChoices = {"\u00B7 Move with WASD", "\u00B7 Print info with I", "\u00B7 Throw bomb with B", "\u00B7 Quit with Q"};
    private static final Pattern inputPattern = Pattern.compile("[WASDIQB]", Pattern.CASE_INSENSITIVE);

    public static void printMenu(ClientObject client) throws InterruptedException, ExitClientException {
        if (client.checkWonGame())
            client.win();

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
                    break;
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
            if (username.length() > 0) {
                client.buildPeer(username);
                client.setStatus(ClientStatus.STATUS_NOT_PLAYING);
            }
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
                    System.err.println("Sorry, I don't know that.");
            }
        } catch (InputMismatchException e) {
            System.err.println("Sorry, I don't know that!");
            in.nextLine();
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
                    System.out.print("Choose field side -- should be a positive even number: ");
                    side = in.nextInt();
                    in.nextLine();
                } while (side <= 0 || side % 2 != 0);
                System.out.print("Choose score goal: ");
                int goal = in.nextInt();
                in.nextLine();
                Game newGame = new Game(gameName, side, goal, client.getUser());
                Game gameResponse = ClientRESTCommunicationService.createNewGame(newGame);
                if (null != gameResponse) {
                    client.prepareForNewGame(gameResponse);
                }
            }
        } catch (UnirestException e) {
            e.printStackTrace();
        } catch (HTTPException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void getGamesList() {
        List<Game> games = null;
        try {
            games = ClientRESTCommunicationService.getExistingGames();
        } catch (UnirestException e) {
            e.printStackTrace();
        } catch (HTTPException e) {
            System.out.println(e.getMessage());
        }

        if (null != games && games.size() > 0) {
            System.out.println("Here are the games you can join:\n");
            for (Game g : games) {
                System.out.println(g.getName());
                System.out.println("\tGrid side length: " + g.getSize() + "\n\tPoints to win: " + g.getPoints() + "\n\tCreated at: " + g.getCreatedAt());
            }
        } else {
            System.out.println("There are no games you can join right now. Create one!");
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
                client.prepareToJoinGame();

                System.out.println("Welcome to " + game.getName() + ". There are " + (game.getPeers().size() - 1) + " other players");
            }
        } catch (UnirestException e) {
            e.printStackTrace();
        } catch (HTTPException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void printPlaying(ClientObject client) throws ExitClientException {
        if (client.getStatus() != ClientStatus.STATUS_PLAYING) return;
        String input = null;
        do {
            if (null != input && !inputPattern.matcher(input).matches() && client.getStatus() == ClientStatus.STATUS_PLAYING) {
                System.err.println("Sorry, I don't know that");
            }
            for (String i : playingChoices) System.out.println(i);
            input = in.nextLine();
        } while (!inputPattern.matcher(input).matches() && client.getStatus() == ClientStatus.STATUS_PLAYING);

        if (client.getStatus() == ClientStatus.STATUS_PLAYING) {
            char choice = input.toUpperCase().charAt(0);
            Move move;
            switch (choice) {
                case 'W':
                    try {
                        move = new Move(client.getPosition(), Direction.UP);
                        waitAndFireMove(client, move);
                    } catch (IllegalMoveException e) {
                        System.err.println("Illegal move. You can't go UP from " + client.getPosition());
                    }
                    break;
                case 'A':
                    try {
                        move = new Move(client.getPosition(), Direction.LEFT);
                        waitAndFireMove(client, move);
                    } catch (IllegalMoveException e) {
                        System.err.println("Illegal move. You can't go LEFT from " + client.getPosition());
                    }
                    break;
                case 'S':
                    try {
                        move = new Move(client.getPosition(), Direction.DOWN);
                        waitAndFireMove(client, move);
                    } catch (IllegalMoveException e) {
                        System.err.println("Illegal move. You can't go DOWN from " + client.getPosition());
                    }
                    break;
                case 'D':
                    try {
                        move = new Move(client.getPosition(), Direction.RIGHT);
                        waitAndFireMove(client, move);
                    } catch (IllegalMoveException e) {
                        System.err.println("Illegal move. You can't go RIGHT from " + client.getPosition());
                    }
                    break;
                case 'B':
                    System.out.println("In a perfect world you would have just thrown a bomb");
                    break;
                case 'I':
                    printPlayingHeader(client);
                    break;
                case 'Q':
                    System.out.println("I WANNA GET FAR AWAY");
                    client.quitGame();
                    break;
            }
        }

    }

    private static void waitAndFireMove(ClientObject client, Move move) {
//        while (client.getMove() != null)
//            // EMPTY WHILE LOOP - WAITS FOR CURRENT MOVE TO BE PROCESSED
//            System.out.print(".");
        client.setMove(move);
    }

    public static void printPlayingHeader(ClientObject client) {
        System.out.println("\n--------------------------------------------------------------------------");
        MessageFormat format = new MessageFormat("User `{0}` in game `{1}`: {2}/{3} points - {4} bombs \u2014 Position: {5}\n");
        Object[] args = {client.getUser().getUsername(), client.getGame().getName(), client.getScore(), client.getGame().getPoints(), "#", client.getPosition()};
        System.out.println(format.format(args));
    }


}
