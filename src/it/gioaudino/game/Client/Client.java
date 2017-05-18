package it.gioaudino.game.Client;

import com.mashape.unirest.http.exceptions.UnirestException;
import it.gioaudino.game.Exception.HTTPException;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;

/**
 * Created by gioaudino on 15/05/17.
 */
public class Client {
    public static void main(String[] args) throws IOException, HTTPException, UnirestException {
        ClientObject client = new ClientObject();

        Scanner in = new Scanner(System.in);
        PrintStream out = System.out;
        while (true) UserInteractionHandler.printMenu(client, in, out);

    }
}
