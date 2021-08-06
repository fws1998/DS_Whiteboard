package server;
/*
 * @author Wangshu Fu
 * @studentID 1112531
 */
import org.json.JSONObject;
import javax.net.ServerSocketFactory;
import javax.swing.*;
import java.io.*;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class Server {
    static String host = "localhost";
    static int port = 1234;
    public static ArrayList<JSONObject> commands = new ArrayList<>();
    public static HashMap<String, Socket> connections = new HashMap<>();


    public Server() {
        ServerSocketFactory factory = ServerSocketFactory.getDefault();
        try {
            ServerSocket socket = factory.createServerSocket(port);
            new ManagerPanel();
            while (true) {
                Socket connection = socket.accept();
                new ReadThread(connection);
            }
        } catch (BindException e) {
            JOptionPane.showMessageDialog(new JLabel("error"), "The port has been occupied, please try another one", "Port occupied", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        } catch (IOException ignore) {
        }
    }


    class ReadThread extends Thread {
        BufferedReader reader;
        String name;

        public ReadThread(Socket connection) {
            try {
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                JSONObject json = new JSONObject(reader.readLine());
                if (json.getInt("command") == 888) {
                    name = json.getString("name");
                    int result = JOptionPane.showConfirmDialog(new JLabel(), name + " want to connect, agree?", "New connection", JOptionPane.YES_NO_OPTION);
                    if (result == JOptionPane.YES_OPTION) {
                        String name = json.getString("name");
                        json.put("text", name + " has connected");
                        json.put("name", "Server");
                        json.put("command", 6);
                        commands.add(json);
                        Server.connections.put(name, connection);
                        ManagerPanel.update();
                        new WriteThread(connection, name);
                    } else {
                        JSONObject reply = new JSONObject();
                        PrintStream printStream = new PrintStream(connection.getOutputStream());
                        reply.put("reply", false);
                        printStream.println(reply);
                        connection.close();
                    }
                }
            } catch (IOException e) {
                JOptionPane.showMessageDialog(new JLabel(), "Unknown error occurred, please try again later!", "Fail", JOptionPane.ERROR_MESSAGE);
            }
            start();
        }

        @Override
        public void run() {
            while (Server.connections.containsKey(name)) {
                try {
                    String buffer = reader.readLine();
                    if (buffer != null) {
                        JSONObject json = new JSONObject(buffer);
                        if (json.getInt("command") == 777){
                            Socket connect = connections.get(json.getString("name"));
                            connect.close();
                            connections.remove(json.getString("name"));
                            json.put("text", json.getString("name") + " has disconnected");
                            json.put("name", "Server");
                            json.put("command", 6);
                        }
                        update(json);
                    }
                    sleep(50);
                } catch (IOException | InterruptedException ignore) {
                }
            }
            try {
                reader.close();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(new JLabel(), "Unknown error occurred, please try again later!", "Fail", JOptionPane.ERROR_MESSAGE);
            }
        }

        public synchronized void update(JSONObject pack) {
            Server.commands.add(pack);
        }
    }


    class WriteThread extends Thread {

        int pointer = 0;
        PrintStream printStream;
        String name;

        public WriteThread(Socket connection, String name) {
            this.name = name;
            JSONObject reply = new JSONObject();

            try {
                ManagerPanel.update();
                printStream = new PrintStream(connection.getOutputStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
            reply.put("reply", true);
            printStream.println(reply);
            start();
        }

        @Override
        public void run() {
            try {
                //Communicate with client
                while (Server.connections.containsKey(name)) {
                    if (pointer < Server.commands.size()) {
                        System.out.println(pointer);
                        printStream.println(Server.commands.get(pointer));
                        printStream.flush();
                        pointer++;
                    }
                    sleep(50);
                }
                while (pointer < Server.commands.size()){
                    JSONObject json = Server.commands.get(pointer);
                    if (json.getString("name").equals(name) && json.getInt("command") == 666){
                        printStream.println(json);
                        printStream.flush();
                        printStream.close();
                        ManagerPanel.update();
                    }else {
                        pointer++;
                    }
                }
            } catch (InterruptedException e) {
                JOptionPane.showMessageDialog(new JLabel(""), "Unknown error occurred, please try again later!", "Fail", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    public static void main(String[] args) {
        try {
            host = args[0];
            port = Integer.parseInt(args[1]);
        }catch (IndexOutOfBoundsException | NumberFormatException e){
            JOptionPane.showMessageDialog(new JLabel("error"), "Illegal parameters, please check it again.", "Illegal parameters", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        if(port>65535 || port<1024){
            JOptionPane.showMessageDialog(new JLabel("error"), "You entered an illegal port number, please restart the application and try again!", "Illegal port number", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        new Server();
    }

    static void quit(){
        System.exit(0);
    }
}
