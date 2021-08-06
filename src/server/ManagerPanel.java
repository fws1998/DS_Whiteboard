package server;
/*
 * @author Wangshu Fu
 * @studentID 1112531
 */
import org.json.JSONException;
import org.json.JSONObject;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ManagerPanel {
    static JList<String> jList = new JList<>();
    static String[] name = Server.connections.keySet().toArray(new String[0]);

    public ManagerPanel() {
        JLabel host = new JLabel();
        host.setText("Host: " + Server.host);
        JLabel port = new JLabel( "Port: " + Server.port);

        jList.setListData(name);
        jList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JButton kick = new JButton("Kick Out");
        kick.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int i = jList.getSelectedIndex();
                if(i >= 0){
                    JSONObject json = new JSONObject();
                    json.put("name", name[i]);
                    json.put("command", 666);
                    Server.commands.add(json);
                    Server.connections.remove(name[i], Server.connections.get(name[i]));
                }
            }
        });
        JPanel info = new JPanel();
        info.setSize(50, 300);
        info.add(port, BorderLayout.NORTH);
        info.add(host, BorderLayout.SOUTH);

        JButton exit = new JButton("Exit");
        exit.addActionListener(e -> quit());

        JButton save = new JButton("Save");
        save.addActionListener(e -> save("whiteboard.png"));

        JButton saveas = new JButton("Save As");
        saveas.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.showSaveDialog(null);
            File file =chooser.getSelectedFile();
            String path = file.getAbsolutePath();
            if (!(path.endsWith(".png"))){
                path += ".png";
            }
            save(path);
        });

        JPanel button = new JPanel();
        Box box = Box.createVerticalBox();
        box.add(kick);
        box.add(save);
        box.add(saveas);
        box.add(exit);
        button.add(box);

        JFrame frame = new JFrame();

        frame.add(info, BorderLayout.SOUTH);
        frame.add(jList, BorderLayout.CENTER);
        frame.add(button, BorderLayout.EAST);
        frame.setVisible(true);
        frame.setTitle("Manager Panel");
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.setSize(300, 300);
    }

    static void update(){
        name = Server.connections.keySet().toArray(new String[0]);
        jList.setListData(name);
    }

    synchronized void quit(){
        int result = JOptionPane.showConfirmDialog(new JLabel(), "Are you sure to quit? Others will disconnect", "Warning", JOptionPane.YES_NO_OPTION);
        if (result == JOptionPane.YES_OPTION) {
            JSONObject json = new JSONObject();
            json.put("text", "Manager has closed the server, you will auto disconnect in 15 seconds");
            json.put("name", "disconnected");
            json.put("command", 6);

            Server.commands.add(json);
            try {
                wait(10000);
            } catch (InterruptedException interruptedException) {
                interruptedException.printStackTrace();
            }
            Server.quit();
        }
    }
    
    private void save(String path){
        BufferedImage pic = new BufferedImage(1024, 768, BufferedImage.TYPE_INT_RGB);
        Graphics graphics = pic.getGraphics();
        graphics.fillRect(0, 0, 1024, 768);
        for (JSONObject pack: Server.commands
             ) {
            int command = pack.getInt("command");
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
                    graphics.setColor(color);
                } catch (JSONException ignore) {
                }
            }
            switch (command) {
                case 1 -> graphics.drawLine(x1, y1, x2, y2);
                case 2 -> {
                    int max = Math.max(Math.abs(x1 - x2), Math.abs(x1 - x2));
                    graphics.drawOval(Math.min(x1, x2), Math.min(y1, y2), max, max);
                }
                case 3 -> graphics.drawOval(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x1 - x2), Math.abs(y1 - y2));
                case 4 -> graphics.drawRect(Math.min(x1, x2), Math.min(y1, y2), Math.abs(x1 - x2), Math.abs(y1 - y2));
                case 5 -> graphics.drawString(pack.getString("text"), x1, y1);
            }
        }
        try {
            ImageIO.write(pic, "PNG", new File(path));
            JOptionPane.showMessageDialog(new JLabel(), "Picture successfully saved!", "Picture saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(new JLabel(), "Error occurs when saving the picture", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
