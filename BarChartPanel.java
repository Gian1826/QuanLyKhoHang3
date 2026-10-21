
package View;
/**
 *
 * @author Cong
 */
import javax.swing.JPanel;
import java.awt.*;

public class BarChartPanel extends JPanel {
    private String[] nhan = {};      // nhãn trục X (13/10, 14/10...)
    private double[] nhap = {};
    private double[] xuat = {};

    public BarChartPanel() {
        setBackground(Color.WHITE);
    }

    public void setData(String[] nhan, double[] nhap, double[] xuat) {
        this.nhan = nhan;
        this.nhap = nhap;
        this.xuat = xuat;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth(), h = getHeight();
        int left = 40, right = 20, top = 40, bottom = 30;
        int bw = w - left - right, bh = h - top - bottom;

        // Giá trị lớn nhất (mặc định 20 khi chưa có dữ liệu)
        double max = 20;
        for (double v : nhap) max = Math.max(max, v);
        for (double v : xuat) max = Math.max(max, v);

        // Lưới ngang + số trục Y
        g2.setFont(getFont().deriveFont(11f));
        for (int i = 0; i <= 4; i++) {
            int y = top + bh - (int) (bh * i / 4.0);
            g2.setColor(new Color(225, 225, 225));
            g2.drawLine(left, y, left + bw, y);
            g2.setColor(Color.GRAY);
            g2.drawString(String.valueOf((int) (max * i / 4)), 10, y + 4);
        }

        // Chú thích
        g2.setColor(new Color(41, 128, 185));
        g2.fillRect(left, 12, 12, 12);
        g2.setColor(Color.DARK_GRAY);
        g2.drawString("Nhập", left + 18, 23);
        g2.setColor(new Color(230, 126, 34));
        g2.fillRect(left + 70, 12, 12, 12);
        g2.setColor(Color.DARK_GRAY);
        g2.drawString("Xuất", left + 88, 23);

        // Chưa có dữ liệu
        if (nhan.length == 0) {
            g2.setColor(Color.GRAY);
            String msg = "Chưa có dữ liệu";
            int tw = g2.getFontMetrics().stringWidth(msg);
            g2.drawString(msg, left + (bw - tw) / 2, top + bh / 2);
            return;
        }

        // Vẽ cột
        int n = nhan.length;
        int group = bw / n;
        int col = Math.max(8, group / 3);
        for (int i = 0; i < n; i++) {
            int x = left + i * group + (group - 2 * col) / 2;
            int h1 = (int) (bh * nhap[i] / max);
            int h2 = (int) (bh * xuat[i] / max);

            g2.setColor(new Color(41, 128, 185));
            g2.fillRect(x, top + bh - h1, col, h1);
            g2.setColor(new Color(230, 126, 34));
            g2.fillRect(x + col, top + bh - h2, col, h2);

            g2.setColor(Color.DARK_GRAY);
            g2.drawString(nhan[i], x, top + bh + 18);
        }
    }
}
