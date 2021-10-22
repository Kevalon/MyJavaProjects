import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;


public class Graphic extends JFrame {
    private static final int scale = 600;
    private final ArrayList<Double> xs;
    private final ArrayList<Double> ys;

    private class DrawingComponent extends JPanel {

        private float scaleX(double x) {
            return (float) (x * scale + 20);
        }

        private float scaleY(double y) {
            return (float) (scale + 20 - y * scale);
        }

        @Override
        protected void paintComponent(Graphics tmp) {
            Graphics2D g = (Graphics2D) tmp;
            g.drawLine(20, scale + 20, 20, 20);
            g.drawLine(20, scale + 20, scale + 20, scale + 20);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(Color.BLACK);
            for (int i = 0; i < xs.size(); i++) {
                double curX = xs.get(i);
                double curY = ys.get(i);
                g.drawString("•", scaleX(curX), scaleY(curY));
            }
        }
    }

    public Graphic(ArrayList<Double> x, ArrayList<Double> y) {
        super("График");
        this.xs = x;
        this.ys = y;
        JPanel panel = new JPanel(new BorderLayout());
        setContentPane(panel);
        panel.add(new DrawingComponent(), BorderLayout.CENTER);
        setSize(scale + 80, scale + 80);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }
}
