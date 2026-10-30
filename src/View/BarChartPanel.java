package View;

import java.awt.*;
import javax.swing.JPanel;

public class BarChartPanel extends JPanel {
    private String[] nhan = {};
    private int[] nhap = {}, xuat = {};
    private static final Color C_NHAP = new Color(41, 128, 185);
    private static final Color C_XUAT = new Color(230, 126, 34);

    public BarChartPanel() { setBackground(Color.WHITE); }

    public void setData(String[] nhan, int[] nhap, int[] xuat) {
        this.nhan = nhan; this.nhap = nhap; this.xuat = xuat;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        FontMetrics fm = g.getFontMetrics();

        int l = 40, r = 15, t = 35, b = 26;
        int w = getWidth() - l - r, h = getHeight() - t - b;
        if (w <= 0 || h <= 0) { g.dispose(); return; }

        // Chú thích
        g.setColor(C_NHAP); g.fillRect(l, 10, 12, 12);
        g.setColor(Color.DARK_GRAY); g.drawString("Nhập", l + 18, 20);
        g.setColor(C_XUAT); g.fillRect(l + 70, 10, 12, 12);
        g.setColor(Color.DARK_GRAY); g.drawString("Xuất", l + 88, 20);

        // Trục Y
        int max = 4;
        for (int v : nhap) max = Math.max(max, v);
        for (int v : xuat) max = Math.max(max, v);
        int top = ((max + 3) / 4) * 4;
        for (int i = 0; i <= 4; i++) {
            int y = t + h - h * i / 4;
            g.setColor(new Color(225, 225, 225));
            g.drawLine(l, y, l + w, y);
            g.setColor(Color.GRAY);
            String s = String.valueOf(top * i / 4);
            g.drawString(s, l - 8 - fm.stringWidth(s), y + 4);
        }

        // Cột
        int n = nhan.length;
        if (n == 0) { g.dispose(); return; }
        int gw = w / n, bw = Math.max(6, gw / 4);
        for (int i = 0; i < n; i++) {
            int x0 = l + i * gw + (gw - 2 * bw - 4) / 2;
            ve(g, fm, x0, bw, nhap[i], top, t, h, C_NHAP);
            ve(g, fm, x0 + bw + 4, bw, xuat[i], top, t, h, C_XUAT);
            g.setColor(Color.GRAY);
            g.drawString(nhan[i], l + i * gw + (gw - fm.stringWidth(nhan[i])) / 2, t + h + 16);
        }
        g.dispose();
    }

    private void ve(Graphics2D g, FontMetrics fm, int x, int bw, int v, int top, int t, int h, Color c) {
        int bh = h * v / top;
        g.setColor(c);
        g.fillRect(x, t + h - bh, bw, bh);
        if (v > 0) {
            g.setColor(Color.DARK_GRAY);
            String s = String.valueOf(v);
            g.drawString(s, x + (bw - fm.stringWidth(s)) / 2, t + h - bh - 3);
        }
    }
}