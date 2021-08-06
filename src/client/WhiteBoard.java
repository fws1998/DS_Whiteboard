package client;
/*
 * @author Wangshu Fu
 * @studentID 1112531
 */
import org.json.JSONObject;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class WhiteBoard extends JPanel{
    int x1;
    int y1;
    int x2;
    int y2;
    static int action = 0;

    static Color colour = Color.BLACK;
    static String text;

    public WhiteBoard() {
        this.setBackground(Color.WHITE);
        this.addMouseListener(new MouseListener() {

        @Override
        public void mouseClicked(MouseEvent e) {
        }

        @Override
        public void mousePressed(MouseEvent e) {
            x1 = e.getX();
            y1 = e.getY();
        }

        //The coordinates of the point when the mouse is released
        public void mouseReleased(MouseEvent e) {
            x2 = e.getX();
            y2 = e.getY();

            if (x1 != x2 && action != 0) {
                JSONObject json = new JSONObject();
                json.put("command", action);
                json.put("x1", x1);
                json.put("x2", x2);
                json.put("y1", y1);
                json.put("y2", y2);
                json.put("r", colour.getRed());
                json.put("g", colour.getGreen());
                json.put("b", colour.getBlue());
                if (text != null) {
                    json.put("text", text);
                    text = null;
                }
                Client.send(json);
            }
        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }
    });
    }
}


