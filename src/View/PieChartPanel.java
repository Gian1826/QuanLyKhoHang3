
package View;
/**
 *
 * @author Cong
 */
import javax.swing.JPanel;
import java.awt.*;

public class PieChartPanel extends JPanel {
    private String[] ten = {};
    private double[] giaTri = {};
    private final Color[] mau = {
        new Color(41, 128, 185), new Color(39, 174, 96),
        new Color(230, 126, 34), new Color(142, 68, 173)
    };

    public PieChartPanel() {
        setBackground(Color.WHITE);
    }

    public void setData(String[] ten, double[] giaTri) {
        this.ten = ten;
        this.giaTri = giaTri;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int size = Math.min(getWidth() / 2, getHeight()) - 40;
        int x = 30, y = (getHeight() - size) / 2;

        double tong = 0;
        for (double v : giaTri) tong += v;

        // Chưa có dữ liệu: vẽ vòng tròn xám + chữ
        if (tong == 0) {
            g2.setColor(new Color(225, 225, 225));
            g2.setStroke(new BasicStroke(2));
            g2.drawOval(x, y, size, size);
            g2.setColor(Color.GRAY);
            String msg = "Chưa có dữ liệu";
            int tw = g2.getFontMetrics().stringWidth(msg);
            g2.drawString(msg, x + (size - tw) / 2, y + size / 2);
            return;
        }

        double goc = 90;
        for (int i = 0; i < giaTri.length; i++) {
            double arc = giaTri[i] / tong * 360;
            g2.setColor(mau[i % mau.length]);
            g2.fillArc(x, y, size, size, (int) goc, (int) -Math.ceil(arc));
            goc -= arc;

            // Chú thích bên phải
            int ly = y + size / 2 - 30 + i * 25;
            g2.fillRect(x + size + 30, ly, 14, 14);
            g2.setColor(Color.DARK_GRAY);
            g2.drawString(ten[i] + " (" + Math.round(giaTri[i] / tong * 100) + "%)",
                          x + size + 50, ly + 12);
        }
    }
}
