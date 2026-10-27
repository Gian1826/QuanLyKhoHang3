package Controller;

import Model.SoDoKhoDAO;
import View.SoDoKhoPanel; 
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class SoDoKhoController {
    private SoDoKhoPanel view;
    private SoDoKhoDAO dao;

    // Định nghĩa màu sắc theo chú thích trên giao diện
    private final Color COLOR_TRONG = Color.WHITE;
    private final Color COLOR_COHANG = new Color(173, 216, 230); // Xanh dương nhạt
    private final Color COLOR_DAY = new Color(25, 25, 112);      // Xanh dương đậm

    public SoDoKhoController(SoDoKhoPanel view) {
        this.view = view;
        this.dao = new SoDoKhoDAO();
        initController();
        veSoDoKho(); // Vẽ sơ đồ lần đầu
    }

    private void initController() {
        // Cài đặt Layout dạng lưới cho các Panel Khu A, B, C, D
        view.getPnlKhuA().setLayout(new GridLayout(0, 4, 5, 5)); // 4 cột, khoảng cách 5px
        view.getPnlKhuB().setLayout(new GridLayout(0, 4, 5, 5));
        view.getPnlKhuC().setLayout(new GridLayout(0, 4, 5, 5));
        view.getPnlKhuD().setLayout(new GridLayout(0, 4, 5, 5));

        // Sự kiện bộ lọc Khu
        view.getCbKhu().addActionListener(e -> veSoDoKho());

        // Sự kiện nút Cất hàng vào đây
        view.getBtnCatHangVaoDay().addActionListener(e -> {
            String viTri = view.getTxtViTri().getText();
            if (viTri.isEmpty()) {
                JOptionPane.showMessageDialog(view, "Vui lòng chọn một vị trí trên sơ đồ!");
            } else {
                JOptionPane.showMessageDialog(view, "Chuyển đến màn hình Cất Hàng cho vị trí: " + viTri);
                // Có thể tích hợp logic chuyển thẻ (CardLayout) sang màn hình Cất Hàng tại đây
            }
        });
    }

    private void veSoDoKho() {
        // Xóa các ô cũ
        view.getPnlKhuA().removeAll();
        view.getPnlKhuB().removeAll();
        view.getPnlKhuC().removeAll();
        view.getPnlKhuD().removeAll();

        String khuLoc = view.getCbKhu().getSelectedItem().toString();
        List<Object[]> dsViTri = dao.layDanhSachViTri(khuLoc);

        for (Object[] vt : dsViTri) {
            String maViTri = vt[0].toString();
            String maKhu = vt[1].toString();
            int sucChua = (int) vt[2];
            int tongTon = (int) vt[3];

            JButton btnO = new JButton(maViTri);
            btnO.setPreferredSize(new Dimension(80, 50));
            
            // Xử lý màu sắc dựa trên số lượng tồn
            if (tongTon == 0) {
                btnO.setBackground(COLOR_TRONG);
                btnO.setForeground(Color.BLACK);
            } else if (tongTon >= sucChua) {
                btnO.setBackground(COLOR_DAY);
                btnO.setForeground(Color.WHITE);
            } else {
                btnO.setBackground(COLOR_COHANG);
                btnO.setForeground(Color.BLACK);
            }

            // Gắn sự kiện click hiển thị chi tiết bên phải
            btnO.addActionListener(e -> hienThiChiTiet(maViTri, tongTon, sucChua));

            // Thêm nút vào Panel tương ứng
            switch (maKhu) {
                case "A": view.getPnlKhuA().add(btnO); break;
                case "B": view.getPnlKhuB().add(btnO); break;
                case "C": view.getPnlKhuC().add(btnO); break;
                case "D": view.getPnlKhuD().add(btnO); break;
            }
        }

        // Cập nhật lại giao diện
        view.revalidate();
        view.repaint();
    }

    private void hienThiChiTiet(String maViTri, int tongTon, int sucChua) {
        view.getTxtViTri().setText(maViTri);
        view.getTxtSoLuong().setText(tongTon + "");
        view.getTxtSucChua().setText(sucChua + "");
        
        // Lấy tên sản phẩm cụ thể
        String chiTietSP = dao.layChiTietSanPhamTaiViTri(maViTri);
        view.getTxtSanPham().setText(chiTietSP);
    }
}