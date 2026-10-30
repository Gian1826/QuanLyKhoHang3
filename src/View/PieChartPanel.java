package View;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.JPanel;

public class PieChartPanel extends JPanel {
    private Map<String, Double> data = new LinkedHashMap<>();
    private static final Color[] MAU = {
        new Color(41, 128, 185), new Color(39, 174, 96), new Color(230, 126, 34),
        new Color(192, 57, 43), new Color(142, 68, 173), new Color(22, 160, 133),
        new Color(241, 196, 15), new Color(127, 140, 141)
    };

    public PieChartPanel() { setBackground(Color.WHITE); }

    public void setData(Map<String, Double> data) {
        this.data = data;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        FontMetrics fm = g.getFontMetrics();

        double tong = 0;
        for (double v : data.values()) tong += v;
        if (tong <= 0) {
            g.setColor(Color.GRAY);
            String s = "Chưa có dữ liệu";
            g.drawString(s, (getWidth() - fm.stringWidth(s)) / 2, getHeight() / 2);
            g.dispose();
            return;
        }

        int size = Math.min(getHeight() - 20, getWidth() / 2 - 10);
        int x = 15, y = (getHeight() - size) / 2;
        double goc = 90;
        int i = 0;
        for (double v : data.values()) {
            double cung = -360.0 * v / tong;
            g.setColor(MAU[i % MAU.length]);
            g.fill(new Arc2D.Double(x, y, size, size, goc, cung, Arc2D.PIE));
            goc += cung;
            i++;
        }

        // Chú thích bên phải
        int lx = x + size + 25, ly = Math.max(15, (getHeight() - data.size() * 22) / 2);
        i = 0;
        for (Map.Entry<String, Double> e : data.entrySet()) {
            g.setColor(MAU[i % MAU.length]);
            g.fillRect(lx, ly + i * 22, 12, 12);
            g.setColor(Color.DARK_GRAY);
            g.drawString(e.getKey() + " (" + Math.round(e.getValue() * 100 / tong) + "%)",
                    lx + 18, ly + i * 22 + 11);
            i++;
        }
        g.dispose();
    }
}