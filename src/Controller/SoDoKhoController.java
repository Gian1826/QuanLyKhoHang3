package Controller;

import Model.SoDoKhoDAO;
import View.SoDoKhoPanel;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.HierarchyEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class SoDoKhoController {
    private final SoDoKhoPanel view;
    private final SoDoKhoDAO dao;

    private static final int SO_O_MOI_KHU = 24;
    private static final String[] CAC_KHU = {"A", "B", "C", "D"};

    private final Color COLOR_TRONG  = Color.WHITE;
    private final Color COLOR_COHANG = new Color(173, 216, 242);
    private final Color COLOR_DAY    = new Color(23, 64, 110);

    public SoDoKhoController(SoDoKhoPanel view) {
        this.view = view;
        this.dao = new SoDoKhoDAO();
        initController();
        veSoDoKho();
    }

    private JPanel[] cacPanel() {
        return new JPanel[]{view.getPnlKhuA(), view.getPnlKhuB(),
                            view.getPnlKhuC(), view.getPnlKhuD()};
    }

    private void initController() {
        // Mỗi khu: 6 hàng x 4 cột = 24 ô
        for (JPanel p : cacPanel()) {
            p.setLayout(new GridLayout(6, 4, 5, 5));
        }

        view.getTxtViTri().setEditable(false);
        view.getTxtSanPham().setEditable(false);
        view.getTxtSoLuong().setEditable(false);
        view.getTxtSucChua().setEditable(false);

        // Nạp lại danh sách lọc (bỏ các mục thừa "Khu C " và " " trong giao diện)
        view.getCbKhu().removeAllItems();
        view.getCbKhu().addItem("Tất cả");
        view.getCbKhu().addItem("Khu A");
        view.getCbKhu().addItem("Khu B");
        view.getCbKhu().addItem("Khu C");
        view.getCbKhu().addItem("Khu D");
        view.getCbKhu().addActionListener(e -> veSoDoKho());

        // Mở lại màn hình thì vẽ lại để thấy số liệu mới
        view.addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && view.isShowing()) {
                veSoDoKho();
            }
        });
    }
    private void veSoDoKho() {
        JPanel[] panels = cacPanel();
        for (JPanel p : panels) p.removeAll();

        // Khu đang lọc: null = tất cả
        Object chon = view.getCbKhu().getSelectedItem();
        String khuLoc = null;
        if (chon != null) {
            String s = chon.toString().trim();
            if (!s.isEmpty() && !s.toLowerCase().contains("tất cả")) {
                khuLoc = s.substring(s.length() - 1).toUpperCase();   // "Khu A" -> "A"
            }
        }

        // Lấy dữ liệu từng khu bằng DAO của bạn (truyền mã khu "A", "B"...)
        // map: MaViTri -> {SucChua, TongTon}
        Map<String, int[]> du_lieu = new HashMap<>();
        for (String maKhu : CAC_KHU) {
            List<Object[]> ds = dao.layDanhSachViTri(maKhu);
            for (Object[] vt : ds) {
                du_lieu.put(vt[0].toString(), new int[]{(int) vt[2], (int) vt[3]});
            }
        }

        // Luôn vẽ đủ 24 ô mỗi khu
        for (int k = 0; k < CAC_KHU.length; k++) {
            String maKhu = CAC_KHU[k];
            if (khuLoc != null && !khuLoc.equals(maKhu)) continue;

            for (int i = 1; i <= SO_O_MOI_KHU; i++) {
                String maViTri = maKhu + "-" + String.format("%02d", i);
                int[] d = du_lieu.get(maViTri);
                int sucChua = d == null ? 100 : d[0];
                int tongTon = d == null ? 0   : d[1];
                panels[k].add(taoO(maViTri, tongTon, sucChua));
            }
        }

        for (JPanel p : panels) {
            p.revalidate();
            p.repaint();
        }
    }

    private JButton taoO(String maViTri, int tongTon, int sucChua) {
        JButton btn = new JButton(maViTri);
        btn.setOpaque(true);
        btn.setFocusPainted(false);

        if (tongTon <= 0) {
            btn.setBackground(COLOR_TRONG);
            btn.setForeground(Color.BLACK);
        } else if (tongTon >= sucChua) {
            btn.setBackground(COLOR_DAY);
            btn.setForeground(Color.WHITE);
        } else {
            btn.setBackground(COLOR_COHANG);
            btn.setForeground(Color.BLACK);
        }

        btn.setToolTipText(maViTri + ": " + tongTon + "/" + sucChua);
        btn.addActionListener(e -> hienThiChiTiet(maViTri, tongTon, sucChua));
        return btn;
    }

    private void hienThiChiTiet(String maViTri, int tongTon, int sucChua) {
        view.getTxtViTri().setText(maViTri);
        view.getTxtSoLuong().setText(String.valueOf(tongTon));
        view.getTxtSucChua().setText(String.valueOf(sucChua));
        // DAO trả về nhiều dòng; ô JTextField chỉ có 1 dòng nên nối bằng dấu phẩy
        view.getTxtSanPham().setText(dao.layChiTietSanPhamTaiViTri(maViTri).replace("\n", ", "));
    }
}