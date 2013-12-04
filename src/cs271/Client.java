package cs271;

import cs271.Messages.ClientMessage;
import cs271.Messages.ServerMessage;

import java.io.*;
import java.net.InetAddress;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.net.Socket;

/**
 * Created with IntelliJ IDEA.
 * User: Photeinis
 * Date: 11/15/13
 * Time: 11:34 AM
 * To change this template use File | Settings | File Templates.
 */

public class Client {

    private Socket socket;
    private ObjectInputStream in;
    private ObjectOutputStream out;

    public Client(String host){

        // Make connection and initialize streams
        try {
            socket = new Socket(host, 9900);
        } catch (IOException e) {
            log("Exception connecting to server!");
        }

        try {
            in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            log("Exception when creating client-side input object stream!");
        }

        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            checkout((ServerMessage) in.readObject());
        } catch (IOException e) {
            log("Exception when creating client-side output object stream!");
        } catch (ClassNotFoundException e){
            log("Exception when reading object from checkout!");
        }
    }

    private void run(){
        String command;
        Scanner scanner = new Scanner(System.in);
        Pattern pattern = Pattern.compile("[a-z]+\\s*(\".*\")*\\s*");

        while (true){

            log("\nPlease command or exit:");
            command = scanner.nextLine();

            // parse the command
            Matcher m = pattern.matcher(command);

            // send client message
            try {
                if (m.find()) {
                    String[] commArray = command.split("\\s+");

                    if (commArray[0].equals("exit"))
                        break;
                    else if (commArray[0].equals("post"))           {
                        String[] commArray2 = command.split("\"");
                        String tweet = commArray2[1];
                        sendToServer("post", tweet);
                        log("post sent");
                    }
                    else if (commArray[0].equals("read"))
                        sendToServer("read", "");
                    else if (commArray[0].equals("fail"))
                        sendToServer("fail", "");
                    else if (commArray[0].equals("unfail"))
                        sendToServer("unfail", "");
                    else{
                        log("Invalid function or parameters!");
                        continue;
                    }
                }
                else {
                    log("Command pattern not found!");
                    continue;
                }
            } catch (ArrayIndexOutOfBoundsException ex){
                log("Exception when reading commands!");
                continue;
            }

            // checkout server message
            try {
                checkout((ServerMessage) in.readObject());
            } catch (IOException e) {
                log("IOException while trying to read server message Object! ");
            } catch (ClassNotFoundException e) {
                log("ClassNotFoundException while trying to read server message Object! ");
            }
        }
    }

    private void log(String m){
        System.out.println(m);
    }

    private void sendToServer(String func, String para){
        try{
            out.writeObject(new ClientMessage(func, para));
            out.flush();
        } catch (IOException e) {
            log("Exception while sending message to server!");
        }
    }

    private void checkout(ServerMessage message){
        if (message != null) log(message.getMessage());
        else log("Unknown message received!");
    }

    private void instruct() throws IOException{
        log("This is a distributed micro blog client.");
        log("\n" +
                "Command: function [para]");
        log("Example: post \"testString\"");
        log("      1: post tweet (wrap tweet with \"\")");
        log("      2: read");
        log("      3: fail");
        log("      4: unfail");
    }

    public static void main(String[] args) throws IOException {
        Client client = new Client(args[0]);
        client.instruct();
        client.run();
    }
}
