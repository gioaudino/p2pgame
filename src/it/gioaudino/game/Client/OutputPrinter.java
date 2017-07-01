package it.gioaudino.game.Client;

import java.io.PrintStream;
import java.text.MessageFormat;

/**
 * Created by gioaudino on 19/06/17.
 * Package it.gioaudino.game.Client in game
 */
public class OutputPrinter {
    private final PrintStream out;

    public OutputPrinter(PrintStream out) {
        this.out = out;
    }

    public void printPlayingHeader(Player player) {
        out.println("\n--------------------------------------------------------------------------");
        MessageFormat format = new MessageFormat("Game `{1}` (grid size: {0}x{0}): {2}/{3} points - {4} \u2014 Position: {5}\n");
        String bomb = player.getBombs().size() == 0 ? "no bombs available" : player.getBombs().peek().getZone() + " bomb available (" + player.getBombs().size() + ")";
        Object[] args = {player.getGame().getSize(), player.getGame().getName(), player.getScore(), player.getGame().getPoints(), bomb, player.getPosition()};
        out.println(format.format(args));
    }

    public void pointScored(Object[] args) {
        MessageFormat format = new MessageFormat("\t\tWell done! You scored one point killing {0}");
        out.println("\n$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$\n");
        out.println(format.format(args));
        out.println("\n$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$\n");
    }

    public void bombPointScored(Object[] args) {
        MessageFormat format = new MessageFormat("\t\tWell done! Your bomb killed {0}");
        out.println("\n$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$\n");
        out.println(format.format(args));
        out.println("\n$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$\n");
    }

    public void bombNoPointsScored(Object[] args) {
        MessageFormat format = new MessageFormat("\t\tYour bomb killed {0} but this bomb gave you enough points");
        out.println("\n====================================================================================\n");
        out.println(format.format(args));
        out.println("\n====================================================================================\n");
    }

    public void print(String arg) {
        out.print(arg);
    }

    public void println(String arg) {
        out.println(arg);
    }

    public void dead(Object[] args) {
        MessageFormat format = new MessageFormat("\t\t\tYou just got killed by {0}{1}");
        out.println("\n====================================================================================\n");
        out.println(format.format(args));
        out.println("\t\t\tPress enter to continue");
        out.println("\n====================================================================================\n");
    }

    public void suicide() {
        out.println("\n====================================================================================\n");
        out.println("\t\t\tYou just committed suicide!");
        out.println("\t\t\tPress enter to continue");
        out.println("\n====================================================================================\n");
    }

    public void win() {
        out.println("\n$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$\n");
        out.println("\t\t\tYou won!! That was an astonishing game, congratulations!");
        out.println("\t\t\tPress enter to continue");
        out.println("\n$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$\n");
    }

    public void endGame(Object[] args) {
        MessageFormat format = new MessageFormat("\t\t\tThe game was won by {0}. Thanks for playing!");
        out.println("\n####################################################################################\n");
        out.println(format.format(args));
        out.println("\t\t\tPress enter to continue");
        out.println("\n####################################################################################\n");
    }

    public void println() {
        out.println();
    }

    public void println(PrintStream stream, String s) {
        synchronized (this.out) {
            stream.println(s);
        }
    }

    public void print(PrintStream stream, String s) {
        stream.print(s);
    }
}
