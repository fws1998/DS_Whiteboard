package client;
/*
  @author Wangshu Fu
 * @studentID 1112531
 */
import org.json.JSONException;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client{

    private static String host;
    private static int port;
    static String nickname;
    static PrintStream printStream;
    JFrame frame;
    static WhiteBoard whiteBoard;
    static JTextArea charHistory;
    static Graphics g;
    static Socket socket;
    static boolean connected = false;
    static JLabel history = new JLabel("Last draw by : ");

    private Graphics getUI(){

        JPanel panel = new JPanel(); //Contain buttons
        whiteBoard = new WhiteBoard();

        JTextArea textField = new JTextArea(1, 20);
        JSplitPane chart = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

        panel.add(history);

        JSplitPane sayAndSend = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        charHistory = new JTextArea(30, 15);
        charHistory.setLineWrap(true);
        JTextArea say = new JTextArea(5, 15);
        say.setLineWrap(true);
        JButton send = new JButton("Send");
        send.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String text = say.getText();
                if(!text.equals("")){
                    JSONObject json = new JSONObject();
                    json.put("text", text);
                    json.put("command", 6);
                    send(json);
                }else {
                    JOptionPane.showMessageDialog(panel, "Please enter the message", "Error", JOptionPane.ERROR_MESSAGE);
                }

            }
        });
        sayAndSend.setDividerLocation(50);
        sayAndSend.setTopComponent(say);
        sayAndSend.setBottomComponent(send);
        chart.setTopComponent(charHistory);
        chart.setBottomComponent(sayAndSend);

        //Create a shape button and add the action listener to the shape button
        JButton line = new JButton("Line");
        line.addActionListener(e -> WhiteBoard.action =1);
        JButton circle = new JButton("Circle");
        circle.addActionListener(e -> WhiteBoard.action = 2);
        JButton oval = new JButton("Oval");
        oval.addActionListener(e -> WhiteBoard.action = 3);
        JButton rectangle = new JButton("Rectangle");
        rectangle.addActionListener(e -> WhiteBoard.action =4);
        JButton text = new JButton("Text");
        text.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                WhiteBoard.text = textField.getText();
                WhiteBoard.action = 5;
            }
        });

        JButton colourChooser = new JButton("Colour");
        colourChooser.addActionListener(e -> WhiteBoard.colour = JColorChooser.showDialog(frame, "select colour", null));
        JButton exit = new JButton("Exit");
        exit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int result = JOptionPane.showConfirmDialog(new JLabel(), "Are you sure to quit?", "Exit", JOptionPane.YES_NO_OPTION);
                if(result == JOptionPane.YES_OPTION){
                    JSONObject json = new JSONObject();
                    json.put("command", 777);
                    json.put("name", nickname);
                    send(json);
                    System.exit(0);
                }
            }
        });

        panel.add(line);
        panel.add(circle);
        panel.add(oval);
        panel.add(rectangle);
        panel.add(text);

        panel.add(colourChooser);
        panel.add(exit);

        JSplitPane buttons = new JSplitPane(JSplitPane.VERTICAL_SPLIT, panel, textField);

        frame = new JFrame();
        //frame.add(jp1, BorderLayout.WEST);
        frame.add(buttons, BorderLayout.NORTH);
        frame.add(chart, BorderLayout.EAST);
        //frame.add(jp2, BorderLayout.EAST);
        frame.add(whiteBoard, BorderLayout.CENTER);

        frame.setTitle("White Board");
        frame.setSize(1200, 768);
        frame.setResizable(true);
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.setVisible(true);
        frame.setResizable(false);
        return whiteBoard.getGraphics();

    }

    public static void main(String[] args) {
        try{
            host = args[0];
            port = Integer.parseInt(args[1]);
            nickname = args[2];
        }catch (IndexOutOfBoundsException | NumberFormatException e){
            JOptionPane.showMessageDialog(charHistory, "Illegal parameters, please check it again.", "Illegal parameters", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        if(port>65535 || port<1024){
            JOptionPane.showMessageDialog(charHistory, "You entered an illegal port number, please restart the client and try again!", "Illegal port number", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        try {
            socket = new Socket(host, port);
            whiteBoard = new WhiteBoard();
            g = whiteBoard.getGraphics();
            JSONObject json = new JSONObject();
            json.put("command", 888);
            json.put("name", nickname);
            send(json);
            Client client = new Client();
            g = client.getUI();
            new Read();
        } catch (UnknownHostException e) {
            JOptionPane.showMessageDialog(charHistory, "Cannot resolve the host, please check the host or try again later!", "Fail", JOptionPane.ERROR_MESSAGE);
        }catch (ConnectException e){
            JOptionPane.showMessageDialog(charHistory, "Cannot connect to server, please try again later!", "Fail", JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(charHistory, "Unknown error occurred, please try again later!", "Fail", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void send(JSONObject json){
        try {
            printStream = new PrintStream(Client.socket.getOutputStream());
            json.put("name", Client.nickname);
            printStream.println(json);
            printStream.flush();
            WhiteBoard.action = 0;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(charHistory, "Unknown error occurred, please try again later!", "Fail", JOptionPane.ERROR_MESSAGE);
        }
    }

    static class Read extends Thread{

        BufferedReader bufferedReader;

        public Read() {
            JSONObject cache = null;
            try {
                bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                cache = new JSONObject(bufferedReader.readLine());
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (cache.getBoolean("reply")){
                JOptionPane.showMessageDialog(new JLabel("error"), "Manager has approved your request", "", JOptionPane.INFORMATION_MESSAGE);
                connected = true;
                run();
            }else {
                JOptionPane.showMessageDialog(new JLabel("error"), "Manager denied your request", "Something went wrong", JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            }
        }

        @Override
        public void run() {
            try {
                while (connected) {
                    String buffer = bufferedReader.readLine();
                    if (buffer != null) {
                        JSONObject pack = new JSONObject(buffer);
                        int command = pack.getInt("command");
                        String name = pack.getString("name");
                        int x1 = 0;
                        int y1 = 0;
                        int x2 = 0;
                        int y2 = 0;
                        if (command < 10) {
                            try {
                                x1 = pack.getInt("x1");
                                y1 = pack.getInt("y1");
                                x2 = pack.getInt("x2");
                                y2 = pack.getInt("y2");
                                Color color = new Color(pack.getInt("r"), pack.getInt("g"), pack.getInt("b"));
                                g.setColor(color);
                                history.setText("Last draw by : " + name);
                                if (command == 6 && name.equals("disconnected")) {
                                    connected = false;
                                }
                            } catch (JSONException ignore) {
                            }
                        } else if (command == 666 && name.equals(nickname)) {
                            JOptionPane.showMessageDialog(new JLabel("error"), "Manager has kicked you out", "", JOptionPane.ERROR_MESSAGE);
                            //socket.close();
                            System.exit(0);
                        }
                        switch (command) {
                            case 1 -> g.drawLine(x1, y1, x2, y2);
                            case 2 -> {
                                int max = Math.max(Math.abs(x1 - x2), Math.abs(x1 - x2));
                                g.drawOval(Math.min(x1, x2), Math.min(y1, y2), max, max);
                            }
                            case 3 -> g.drawOval(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x1 - x2), Math.abs(y1 - y2));
                            case 4 -> g.drawRect(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x1 - x2), Math.abs(y1 - y2));
                            case 5 -> g.drawString(pack.getString("text"), x1, y1);
                            case 6 -> charHistory.setText(charHistory.getText() + "\n" + name + ": " + pack.getString("text"));
                        }
                    }
                }
                bufferedReader.close();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(charHistory, "Unknown error occurred, please try again later!", "Fail", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}