package it.gioaudino.game.Client;

import com.mashape.unirest.http.exceptions.UnirestException;
import it.gioaudino.game.Entity.*;
import it.gioaudino.game.Exception.ExitClientException;
import it.gioaudino.game.Exception.HTTPException;
import it.gioaudino.game.Exception.IllegalMoveException;
import it.gioaudino.game.Service.ClientRESTCommunicationService;
import it.gioaudino.game.Service.GameManager;

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
    private static final Pattern inputPattern = Pattern.compile("[WASDIQBP]", Pattern.CASE_INSENSITIVE);

    public static void printMenu(Player player) throws InterruptedException, ExitClientException {
        if (player.checkWonGame())
            player.win();

        try {
            switch (player.getStatus()) {
                case STATUS_NEW:
                    printNewMenu(player);
                    break;
                case STATUS_NOT_PLAYING:
                    printNotPlayingMenu(player);
                    break;
                case STATUS_PLAYING:
                    printPlaying(player);
                    break;
            }
        } catch (ExitClientException e) {
            in.close();
            throw e;
        }
    }

    private static void printNewMenu(Player player) {
        if (player.getUser() == null) {
            player.getOutputPrinter().print("Hi! Welcome to the game. Please tell me your username: ");
            String username = in.nextLine();
            if (username.length() > 0) {
                if (username.equals("clear")) GameManager.getInstance().reset();
                else {
                    player.buildPeer(username);
                    player.setStatus(ClientStatus.STATUS_NOT_PLAYING);
                }
            }
        }
    }

    private static void printNotPlayingMenu(Player player) throws ExitClientException {
        player.getOutputPrinter().println("Hello " + player.getUser().getUsername() + ". Please state your intentions: ");
        player.getOutputPrinter().println();
        for (String i : notPlayingChoices) player.getOutputPrinter().println(i);
        try {
            int choice = in.nextInt();
            in.nextLine();

            switch (choice) {
                case 1: // change username
                    changeUsername(player);
                    break;
                case 2: // create new game
                    createNewGame(player);
                    break;
                case 3: // get list of games
                    getGamesList(player);
                    break;
                case 4: // join existing game
                    joinExistingGame(player);
                    break;
                case 5: // die
                    player.getOutputPrinter().println("Thanks for playing! Goodbye!");
                    throw new ExitClientException();
                case 0:
                    player.getOutputPrinter().println(player.toString());
                default:
                    player.getOutputPrinter().println(System.err, "Sorry, I don't know that.");
            }
        } catch (InputMismatchException e) {
            player.getOutputPrinter().println(System.err, "Sorry, I don't know that!");
            in.nextLine();
        }

    }

    private static void changeUsername(Player player) {
        player.getOutputPrinter().print("Type your new username: ");
        String username = in.nextLine();
        User user = player.getUser();
        user.setUsername(username);
        player.setUser(user);
    }

    private static void createNewGame(Player player) {
        player.getOutputPrinter().print("Choose game name: ");
        String gameName = in.nextLine();
        try {
            if (ClientRESTCommunicationService.tryGameName(gameName)) {
                player.getOutputPrinter().println("The game " + gameName + " exists already.");
            } else {
                int side;
                do {
                    player.getOutputPrinter().print("Choose field side -- should be a positive even number: ");
                    side = in.nextInt();
                    in.nextLine();
                } while (side <= 0 || side % 2 != 0);
                player.getOutputPrinter().print("Choose score goal: ");
                int goal = in.nextInt();
                in.nextLine();
                Game newGame = new Game(gameName, side, goal, player.getUser());
                Game gameResponse = ClientRESTCommunicationService.createNewGame(newGame);
                if (null != gameResponse) {
                    player.prepareForNewGame(gameResponse);
                }
            }
        } catch (UnirestException e) {
            e.printStackTrace();
        } catch (HTTPException e) {
            player.getOutputPrinter().println(e.getMessage());
        }
    }

    private static void getGamesList(Player player) {
        List<Game> games = null;
        try {
            games = ClientRESTCommunicationService.getExistingGames();
        } catch (UnirestException e) {
            e.printStackTrace();
        } catch (HTTPException e) {
            player.getOutputPrinter().println(e.getMessage());
        }

        if (null != games && games.size() > 0) {
            player.getOutputPrinter().println("Here are the games you can join:\n");
            for (Game g : games) {
                player.getOutputPrinter().println(g.getName());
                player.getOutputPrinter().println(
                        "\tGrid size: " + g.getSize() + "x" + g.getSize() +
                                "\n\tPoints to win: " + g.getPoints() +
                                "\n\tCreated at: " + g.getCreatedAt() + "\n");
            }
        } else {
            player.getOutputPrinter().println("There are no games to join right now. Why don't you create one?");
        }
    }

    private static void joinExistingGame(Player player) {
        player.getOutputPrinter().print("Which game would you like to join? ");
        String gameName = in.nextLine();
        try {
            if (!ClientRESTCommunicationService.tryGameName(gameName)) {
                player.getOutputPrinter().println("The game " + gameName + " does not exist.");
            } else {
                Game game = ClientRESTCommunicationService.joinExistingGame(gameName, player.getUser());
                player.setGame(game);
                player.setStatus(ClientStatus.STATUS_PLAYING);
                player.prepareToJoinGame();

                player.getOutputPrinter().println("Welcome to " + game.getName() + ". There are " + (game.getUsers().size() - 1) + " other players");
            }
        } catch (UnirestException e) {
            e.printStackTrace();
        } catch (HTTPException e) {
            player.getOutputPrinter().println(e.getMessage());
        }
    }

    private static void printPlaying(Player player) throws ExitClientException {
        if (player.getStatus() != ClientStatus.STATUS_PLAYING) return;
        String input = null;
        do {
            if (null != input && !inputPattern.matcher(input).matches() && player.getStatus() == ClientStatus.STATUS_PLAYING) {
                player.getOutputPrinter().println(System.err, "Sorry, I don't know that");
            }
            for (String i : playingChoices) player.getOutputPrinter().println(i);
            input = in.nextLine();
        } while (!inputPattern.matcher(input).matches() && player.getStatus() == ClientStatus.STATUS_PLAYING);

        if (player.getStatus() == ClientStatus.STATUS_PLAYING) {
            char choice = input.toUpperCase().charAt(0);
            Move move;
            switch (choice) {
                case 'W':
                    try {
                        move = new Move(player.getPosition(), Direction.UP);
                        waitAndFireMove(player, move);
                    } catch (IllegalMoveException e) {
                        player.getOutputPrinter().println(System.err, "Illegal move. You can't go UP from " + player.getPosition());
                    }
                    break;
                case 'A':
                    try {
                        move = new Move(player.getPosition(), Direction.LEFT);
                        waitAndFireMove(player, move);
                    } catch (IllegalMoveException e) {
                        player.getOutputPrinter().println(System.err, "Illegal move. You can't go LEFT from " + player.getPosition());
                    }
                    break;
                case 'S':
                    try {
                        move = new Move(player.getPosition(), Direction.DOWN);
                        waitAndFireMove(player, move);
                    } catch (IllegalMoveException e) {
                        player.getOutputPrinter().println(System.err, "Illegal move. You can't go DOWN from " + player.getPosition());
                    }
                    break;
                case 'D':
                    try {
                        move = new Move(player.getPosition(), Direction.RIGHT);
                        waitAndFireMove(player, move);
                    } catch (IllegalMoveException e) {
                        player.getOutputPrinter().println(System.err, "Illegal move. You can't go RIGHT from " + player.getPosition());
                    }
                    break;
                case 'B':
                    player.throwNext();
                    break;
                case 'I':
                    player.getOutputPrinter().printPlayingHeader(player);
                    break;
                case 'Q':
                    player.quitGame();
                    break;
                case 'P':
                    player.getOutputPrinter().println(player.toString());
                    break;
            }
        }

    }

    private static void waitAndFireMove(Player player, Move move) {
        player.setMove(move);
    }


}
