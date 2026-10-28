
package View;
import Controller.PhieuNhapController;
import Controller.TaiKhoanController;
import Model.PhienDangNhap;
import Model.*;
import View.*;
import Controller.*;
public class Main extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Main.class.getName());
    private final java.awt.Color BG      = new java.awt.Color(34, 45, 50);   
    private final java.awt.Color BG_SUB  = new java.awt.Color(44, 59, 65);   
    private final java.awt.Color BG_CHON = new java.awt.Color(30, 40, 44);   

    private javax.swing.JButton dangChon;
    private javax.swing.JPanel[] panelCon;  

        public Main() {
        initComponents();
    javax.swing.JButton[] nutCha = {
        btnNhapKho, btnLuuTru, btnLayHang, btnXuatKho,
        btnHoanTra, btnKiemKe, btnBaoCao, btnTaiKhoan
    };
    panelCon = new javax.swing.JPanel[]{
        pnlNhapKho, pnlLuuTru, pnlLayHang, pnlXuatKho,
        pnlHoanTra, pnlKiemKe, pnlBaoCao, pnlTaiKhoan
    };
    for (int i = 0; i < nutCha.length; i++) {
        final javax.swing.JPanel p = panelCon[i];
        nutCha[i].setBackground(BG);                 // sửa nền các nút cha đang bị sai màu
        p.setBackground(BG_SUB);
        p.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
        p.setVisible(false);                         // thu gọn mặc định
        nutCha[i].addActionListener(e -> toggle(p)); // bấm để mở/đóng
    }

    // ===== Nút con: nền, opaque, căn trái =====
    javax.swing.JButton[] nutCon = {
        btnLapPhieu, btnDSPhieu,
        btnCatHangVaoViTri, btnSoDoViTriKho, btnChuyenViTri,
        btnDanhSachLayHang, btnLichSuLayHang,
        btnLapPhieuXuat, btnPhieuXuatDaLuu,
        btnLapPhieuHoanTra, btnPhieuHoanTraDaLuu,
        btnLapPhieuKiemKe, btnLichSuKiemKe,
        btnBaoCaoTonKho, btnBaoCaoNhapXuat, btnBaoCaoHangSapHet,
        btnTaiKhoanCuaToi, btnDangXuat
    };
    for (javax.swing.JButton b : nutCon) {
        b.setBackground(BG_SUB);
        b.setOpaque(true);
    }
    java.util.List<javax.swing.JButton> tatCa = new java.util.ArrayList<>();
    tatCa.add(btnTrangChu);
    tatCa.addAll(java.util.Arrays.asList(nutCha));
    tatCa.addAll(java.util.Arrays.asList(nutCon));
    for (javax.swing.JButton b : tatCa) {
    b.setAlignmentX(java.awt.Component.LEFT_ALIGNMENT);
    themHover(b);
}
        setSize(1200, 700);
            setTitle("Quản Lý Kho Hàng");
        setLocationRelativeTo(null);

        // Hiện thông tin người đăng nhập ở header
        String ten = PhienDangNhap.tenDangNhap != null ? PhienDangNhap.tenDangNhap : "guest";
        lblTen.setText(ten);
        lblTen.setToolTipText(PhienDangNhap.hoTen);
        lblAvatar.setText(ten.substring(0, Math.min(2, ten.length())).toUpperCase());
        // Vùng nội dung bên phải: mỗi màn hình là một "card"
        pnlContent.setLayout(new java.awt.CardLayout());
        //Trang chủ
        TrangChuPanel trangChu = new TrangChuPanel();
        pnlContent.add(new TrangChuPanel(), "home");
        themTrang("home", btnTrangChu, new TrangChuPanel());
        //Nhập kho
        //Lập phiếu nhập
        PhieuNhapPanel pnView = new PhieuNhapPanel();
        PhieuNhapController pnController = new PhieuNhapController(pnView);
        themTrang("lapphieunhap",    btnLapPhieu,    pnView);
        //danh sách phiếu nhập
        PhieuNhapDaLuuPanel dsView = new PhieuNhapDaLuuPanel();
        new PhieuNhapDaLuuController(dsView);
        themTrang("dsphieunhap",     btnDSPhieu,     dsView);
        // --- Lưu trữ ---
        themTrang("sodokho",     btnSoDoViTriKho,    new SoDoKhoPanel());
        themTrang("cathang",     btnCatHangVaoViTri, new CatVaoViTriPanel());
        themTrang("chuyenvitri", btnChuyenViTri,     new ChuyenViTriPanel());
        // --- Lấy hàng ---
        themTrang("dslayhang", btnDanhSachLayHang, new DanhSachLayHangPanel());
        themTrang("lslayhang", btnLichSuLayHang,   new LichSuLayHangPanel());
        // --- Xuất kho ---
        themTrang("lapphieuxuat", btnLapPhieuXuat,    new LapPhieuXuatPanel());
        themTrang("dsphieuxuat",  btnPhieuXuatDaLuu,  new PhieuXuatDaLuuPanel());
        // --- Hoàn trả ---
        themTrang("lapphieuhoantra", btnLapPhieuHoanTra,     new PhieuHoanTraPanel());
        themTrang("dsphieuhoantra",  btnPhieuHoanTraDaLuu,   new PhieuHoanTraDaLuuPanel());
        // --- Kiểm kê ---
        themTrang("lapphieukiemke", btnLapPhieuKiemKe, new PhieuKiemKePanel());
        themTrang("lskiemke",       btnLichSuKiemKe,   new LichSuKiemKePanel());
        // --- Báo cáo ---
        themTrang("bctonkho",   btnBaoCaoTonKho,     new BaoCaoTonKhoPanel());
        themTrang("bcnhapxuat", btnBaoCaoNhapXuat,   new BaoCaoNhapXuatPanel());
        themTrang("bcsaphet",   btnBaoCaoHangSapHet, new BaoCaoHangSapHetPanel());
        //tài khoản
        ThongTinTaiKhoanPanel view = new ThongTinTaiKhoanPanel();
        view.setVisible(true);
        new Controller.TaiKhoanController(view);
        //TaiKhoanController tkController = new TaiKhoanController(view);
        themTrang("taikhoan", btnTaiKhoanCuaToi, view);
        btnTaiKhoanCuaToi.addActionListener(e -> {
        //view.napThongTin(); // Tải họ tên, vai trò hiển thị lên màn hình
        hienThi("taikhoancuatoi", btnTaiKhoanCuaToi); 
});
        btnDangXuat.addActionListener(e -> dangXuat());
        hienThi("home", btnTrangChu);
        trangChu.setDieuHuong(t -> {
    switch (t) {
        case "lapphieu" -> hienThi("lapphieu", btnLapPhieu);
        case "luutru"   -> hienThi("sodokho", btnSoDoViTriKho);
        case "xuatkho"  -> hienThi("lapphieuxuat", btnLapPhieuXuat);
        case "kiemke"   -> hienThi("lapphieukiemke", btnLapPhieuKiemKe);
        case "baocao"   -> hienThi("bctonkho", btnBaoCaoTonKho);
                }
    });
}

    // Đăng xuất: hỏi xác nhận rồi quay về màn hình đăng nhập
    private void dangXuat() {
        int c = javax.swing.JOptionPane.showConfirmDialog(this,
                "Bạn muốn đăng xuất?", "Đăng xuất",
                javax.swing.JOptionPane.YES_NO_OPTION);
        if (c == javax.swing.JOptionPane.YES_OPTION) {
            PhienDangNhap.xoa();
            dispose();
            new DangNhapFrame().setVisible(true);
        }
    }

    // Đóng/mở nhóm menu con
    private void toggle(javax.swing.JPanel p) {
        boolean mo = !p.isVisible();
        for (javax.swing.JPanel q : panelCon) q.setVisible(false);
        p.setVisible(mo);
        pnlItems.revalidate();
        pnlItems.repaint();
    }
    // Thêm panel vào CardLayout, gắn nút mở, tùy chọn nạp lại dữ liệu mỗi lần mở
private void themTrang(String card, javax.swing.JButton btn,
                       javax.swing.JPanel panel, Runnable napLai) {
    pnlContent.add(panel, card);
    btn.addActionListener(e -> {
        if (napLai != null) napLai.run();
        hienThi(card, btn);
    });
}

private void themTrang(String card, javax.swing.JButton btn, javax.swing.JPanel panel) {
    themTrang(card, btn, panel, null);
}

// Màn hình tạm cho panel chưa nhận được
private javax.swing.JPanel trangTam(String ten) {
    javax.swing.JPanel p = new javax.swing.JPanel(new java.awt.GridBagLayout());
    p.add(new javax.swing.JLabel(ten + " – đang phát triển"));
    return p;
}
    // Hiện màn hình tương ứng và đánh dấu nút đang chọn
    private void hienThi(String card, javax.swing.JButton btn) {
        ((java.awt.CardLayout) pnlContent.getLayout()).show(pnlContent, card);
        if (dangChon != null) {
            dangChon.setBackground(nenGoc(dangChon));
        }
        btn.setBackground(BG_CHON);
        dangChon = btn;
    }

    // Nút cấp 1 dùng màu BG, nút con dùng màu BG_SUB
    private java.awt.Color nenGoc(javax.swing.JButton b) {
        return b.getParent() == pnlItems ? BG : BG_SUB;
    }

    // Đổi màu nền khi rê chuột vào/ra
    private void themHover(javax.swing.JButton b) {
        b.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                b.setBackground(BG_CHON);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (b != dangChon) {
                    b.setBackground(nenGoc(b));
                }
            }
        });
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlMenu = new javax.swing.JPanel();
        pnlHeader = new javax.swing.JPanel();
        lblAvatar = new javax.swing.JLabel();
        lblTen = new javax.swing.JLabel();
        pnlItems = new javax.swing.JPanel();
        btnTrangChu = new javax.swing.JButton();
        btnNhapKho = new javax.swing.JButton();
        pnlNhapKho = new javax.swing.JPanel();
        btnLapPhieu = new javax.swing.JButton();
        btnDSPhieu = new javax.swing.JButton();
        btnLuuTru = new javax.swing.JButton();
        pnlLuuTru = new javax.swing.JPanel();
        btnCatHangVaoViTri = new javax.swing.JButton();
        btnSoDoViTriKho = new javax.swing.JButton();
        btnChuyenViTri = new javax.swing.JButton();
        btnLayHang = new javax.swing.JButton();
        pnlLayHang = new javax.swing.JPanel();
        btnDanhSachLayHang = new javax.swing.JButton();
        btnLichSuLayHang = new javax.swing.JButton();
        btnXuatKho = new javax.swing.JButton();
        pnlXuatKho = new javax.swing.JPanel();
        btnLapPhieuXuat = new javax.swing.JButton();
        btnPhieuXuatDaLuu = new javax.swing.JButton();
        btnHoanTra = new javax.swing.JButton();
        pnlHoanTra = new javax.swing.JPanel();
        btnLapPhieuHoanTra = new javax.swing.JButton();
        btnPhieuHoanTraDaLuu = new javax.swing.JButton();
        btnKiemKe = new javax.swing.JButton();
        pnlKiemKe = new javax.swing.JPanel();
        btnLapPhieuKiemKe = new javax.swing.JButton();
        btnLichSuKiemKe = new javax.swing.JButton();
        btnBaoCao = new javax.swing.JButton();
        pnlBaoCao = new javax.swing.JPanel();
        btnBaoCaoTonKho = new javax.swing.JButton();
        btnBaoCaoNhapXuat = new javax.swing.JButton();
        btnBaoCaoHangSapHet = new javax.swing.JButton();
        btnTaiKhoan = new javax.swing.JButton();
        pnlTaiKhoan = new javax.swing.JPanel();
        btnTaiKhoanCuaToi = new javax.swing.JButton();
        btnDangXuat = new javax.swing.JButton();
        pnlContent = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        pnlMenu.setBackground(new java.awt.Color(34, 45, 50));
        pnlMenu.setPreferredSize(new java.awt.Dimension(230, 600));
        pnlMenu.setLayout(new java.awt.BorderLayout());

        pnlHeader.setBackground(new java.awt.Color(34, 45, 50));
        pnlHeader.setPreferredSize(new java.awt.Dimension(230, 72));

        lblAvatar.setBackground(new java.awt.Color(108, 138, 155));
        lblAvatar.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        lblAvatar.setForeground(new java.awt.Color(255, 255, 255));
        lblAvatar.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblAvatar.setText("AD");
        lblAvatar.setOpaque(true);

        lblTen.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        lblTen.setForeground(new java.awt.Color(255, 255, 255));
        lblTen.setText("admin");

        javax.swing.GroupLayout pnlHeaderLayout = new javax.swing.GroupLayout(pnlHeader);
        pnlHeader.setLayout(pnlHeaderLayout);
        pnlHeaderLayout.setHorizontalGroup(
            pnlHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlHeaderLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(lblAvatar, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
                .addComponent(lblTen, javax.swing.GroupLayout.PREFERRED_SIZE, 90, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(56, Short.MAX_VALUE))
        );
        pnlHeaderLayout.setVerticalGroup(
            pnlHeaderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlHeaderLayout.createSequentialGroup()
                .addGap(16, 16, 16)
                .addComponent(lblAvatar, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(16, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlHeaderLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblTen)
                .addGap(24, 24, 24))
        );

        pnlMenu.add(pnlHeader, java.awt.BorderLayout.PAGE_START);

        pnlItems.setBackground(new java.awt.Color(34, 45, 50));
        pnlItems.setLayout(new javax.swing.BoxLayout(pnlItems, javax.swing.BoxLayout.Y_AXIS));

        btnTrangChu.setBackground(new java.awt.Color(34, 45, 50));
        btnTrangChu.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnTrangChu.setForeground(new java.awt.Color(184, 199, 206));
        btnTrangChu.setText("Trang chủ");
        btnTrangChu.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 18, 0, 0));
        btnTrangChu.setBorderPainted(false);
        btnTrangChu.setContentAreaFilled(false);
        btnTrangChu.setFocusPainted(false);
        btnTrangChu.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnTrangChu.setMaximumSize(new java.awt.Dimension(32767, 42));
        btnTrangChu.setOpaque(true);
        btnTrangChu.addActionListener(this::btnTrangChuActionPerformed);
        pnlItems.add(btnTrangChu);

        btnNhapKho.setBackground(new java.awt.Color(34, 45, 50));
        btnNhapKho.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnNhapKho.setForeground(new java.awt.Color(184, 199, 206));
        btnNhapKho.setText("Nhập kho");
        btnNhapKho.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 18, 0, 0));
        btnNhapKho.setBorderPainted(false);
        btnNhapKho.setContentAreaFilled(false);
        btnNhapKho.setFocusPainted(false);
        btnNhapKho.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnNhapKho.setMaximumSize(new java.awt.Dimension(32767, 42));
        btnNhapKho.setOpaque(true);
        pnlItems.add(btnNhapKho);

        pnlNhapKho.setBackground(new java.awt.Color(44, 59, 65));
        pnlNhapKho.setLayout(new javax.swing.BoxLayout(pnlNhapKho, javax.swing.BoxLayout.Y_AXIS));

        btnLapPhieu.setBackground(new java.awt.Color(44, 59, 65));
        btnLapPhieu.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnLapPhieu.setForeground(new java.awt.Color(184, 199, 206));
        btnLapPhieu.setText("Lập phiếu nhập");
        btnLapPhieu.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 44, 0, 0));
        btnLapPhieu.setBorderPainted(false);
        btnLapPhieu.setContentAreaFilled(false);
        btnLapPhieu.setFocusPainted(false);
        btnLapPhieu.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnLapPhieu.setMaximumSize(new java.awt.Dimension(32767, 34));
        btnLapPhieu.setOpaque(true);
        btnLapPhieu.addActionListener(this::btnLapPhieuActionPerformed);
        pnlNhapKho.add(btnLapPhieu);

        btnDSPhieu.setBackground(new java.awt.Color(44, 59, 65));
        btnDSPhieu.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnDSPhieu.setForeground(new java.awt.Color(184, 199, 206));
        btnDSPhieu.setText("Phiếu nhập đã lưu");
        btnDSPhieu.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 44, 0, 0));
        btnDSPhieu.setBorderPainted(false);
        btnDSPhieu.setContentAreaFilled(false);
        btnDSPhieu.setFocusPainted(false);
        btnDSPhieu.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnDSPhieu.setMaximumSize(new java.awt.Dimension(32767, 34));
        btnDSPhieu.setOpaque(true);
        pnlNhapKho.add(btnDSPhieu);

        pnlItems.add(pnlNhapKho);

        btnLuuTru.setBackground(new java.awt.Color(34, 45, 50));
        btnLuuTru.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnLuuTru.setForeground(new java.awt.Color(184, 199, 206));
        btnLuuTru.setText("Lưu trữ");
        btnLuuTru.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 18, 0, 0));
        btnLuuTru.setBorderPainted(false);
        btnLuuTru.setContentAreaFilled(false);
        btnLuuTru.setFocusPainted(false);
        btnLuuTru.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnLuuTru.setMaximumSize(new java.awt.Dimension(32767, 42));
        btnLuuTru.setOpaque(true);
        pnlItems.add(btnLuuTru);

        pnlLuuTru.setBackground(new java.awt.Color(44, 59, 65));
        pnlLuuTru.setForeground(new java.awt.Color(184, 199, 206));
        pnlLuuTru.setLayout(new javax.swing.BoxLayout(pnlLuuTru, javax.swing.BoxLayout.Y_AXIS));

        btnCatHangVaoViTri.setBackground(new java.awt.Color(44, 59, 65));
        btnCatHangVaoViTri.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnCatHangVaoViTri.setForeground(new java.awt.Color(184, 199, 206));
        btnCatHangVaoViTri.setText("Cất hàng vào vị trí");
        btnCatHangVaoViTri.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 44, 0, 0));
        btnCatHangVaoViTri.setBorderPainted(false);
        btnCatHangVaoViTri.setContentAreaFilled(false);
        btnCatHangVaoViTri.setFocusPainted(false);
        btnCatHangVaoViTri.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnCatHangVaoViTri.setMaximumSize(new java.awt.Dimension(32767, 34));
        btnCatHangVaoViTri.setOpaque(true);
        pnlLuuTru.add(btnCatHangVaoViTri);

        btnSoDoViTriKho.setBackground(new java.awt.Color(44, 59, 65));
        btnSoDoViTriKho.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnSoDoViTriKho.setForeground(new java.awt.Color(184, 199, 206));
        btnSoDoViTriKho.setText("Sơ đồ vị trí kho");
        btnSoDoViTriKho.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 44, 0, 0));
        btnSoDoViTriKho.setBorderPainted(false);
        btnSoDoViTriKho.setContentAreaFilled(false);
        btnSoDoViTriKho.setFocusPainted(false);
        btnSoDoViTriKho.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnSoDoViTriKho.setMaximumSize(new java.awt.Dimension(32767, 34));
        btnSoDoViTriKho.setOpaque(true);
        pnlLuuTru.add(btnSoDoViTriKho);

        btnChuyenViTri.setBackground(new java.awt.Color(44, 59, 65));
        btnChuyenViTri.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnChuyenViTri.setForeground(new java.awt.Color(184, 199, 206));
        btnChuyenViTri.setText("Chuyển vị trí");
        btnChuyenViTri.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 44, 0, 0));
        btnChuyenViTri.setBorderPainted(false);
        btnChuyenViTri.setContentAreaFilled(false);
        btnChuyenViTri.setFocusPainted(false);
        btnChuyenViTri.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnChuyenViTri.setMaximumSize(new java.awt.Dimension(32767, 34));
        btnChuyenViTri.setOpaque(true);
        pnlLuuTru.add(btnChuyenViTri);

        pnlItems.add(pnlLuuTru);

        btnLayHang.setBackground(new java.awt.Color(44, 59, 65));
        btnLayHang.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnLayHang.setForeground(new java.awt.Color(184, 199, 206));
        btnLayHang.setText("Lấy hàng");
        btnLayHang.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 18, 0, 0));
        btnLayHang.setBorderPainted(false);
        btnLayHang.setContentAreaFilled(false);
        btnLayHang.setFocusPainted(false);
        btnLayHang.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnLayHang.setMaximumSize(new java.awt.Dimension(32767, 42));
        btnLayHang.setOpaque(true);
        btnLayHang.addActionListener(this::btnLayHangActionPerformed);
        pnlItems.add(btnLayHang);

        pnlLayHang.setBackground(new java.awt.Color(44, 59, 65));
        pnlLayHang.setForeground(new java.awt.Color(184, 199, 206));
        pnlLayHang.setLayout(new javax.swing.BoxLayout(pnlLayHang, javax.swing.BoxLayout.Y_AXIS));

        btnDanhSachLayHang.setBackground(new java.awt.Color(44, 59, 65));
        btnDanhSachLayHang.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnDanhSachLayHang.setForeground(new java.awt.Color(184, 199, 206));
        btnDanhSachLayHang.setText("Danh sách lấy hàng");
        btnDanhSachLayHang.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 44, 0, 0));
        btnDanhSachLayHang.setBorderPainted(false);
        btnDanhSachLayHang.setContentAreaFilled(false);
        btnDanhSachLayHang.setFocusPainted(false);
        btnDanhSachLayHang.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnDanhSachLayHang.setMaximumSize(new java.awt.Dimension(32767, 34));
        btnDanhSachLayHang.setOpaque(true);
        pnlLayHang.add(btnDanhSachLayHang);

        btnLichSuLayHang.setBackground(new java.awt.Color(44, 59, 65));
        btnLichSuLayHang.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnLichSuLayHang.setForeground(new java.awt.Color(184, 199, 206));
        btnLichSuLayHang.setText("Lịch sử Lấy Hàng");
        btnLichSuLayHang.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 44, 0, 0));
        btnLichSuLayHang.setBorderPainted(false);
        btnLichSuLayHang.setContentAreaFilled(false);
        btnLichSuLayHang.setFocusPainted(false);
        btnLichSuLayHang.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnLichSuLayHang.setMaximumSize(new java.awt.Dimension(32767, 34));
        btnLichSuLayHang.setOpaque(true);
        btnLichSuLayHang.addActionListener(this::btnLichSuLayHangActionPerformed);
        pnlLayHang.add(btnLichSuLayHang);

        pnlItems.add(pnlLayHang);

        btnXuatKho.setBackground(new java.awt.Color(44, 59, 65));
        btnXuatKho.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnXuatKho.setForeground(new java.awt.Color(184, 199, 206));
        btnXuatKho.setText("Xuất kho");
        btnXuatKho.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 18, 0, 0));
        btnXuatKho.setBorderPainted(false);
        btnXuatKho.setContentAreaFilled(false);
        btnXuatKho.setFocusPainted(false);
        btnXuatKho.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnXuatKho.setMaximumSize(new java.awt.Dimension(32767, 42));
        btnXuatKho.setOpaque(true);
        pnlItems.add(btnXuatKho);

        pnlXuatKho.setBackground(new java.awt.Color(44, 59, 65));
        pnlXuatKho.setForeground(new java.awt.Color(184, 199, 206));
        pnlXuatKho.setLayout(new javax.swing.BoxLayout(pnlXuatKho, javax.swing.BoxLayout.Y_AXIS));

        btnLapPhieuXuat.setBackground(new java.awt.Color(44, 59, 65));
        btnLapPhieuXuat.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnLapPhieuXuat.setForeground(new java.awt.Color(184, 199, 206));
        btnLapPhieuXuat.setText("Lập phiếu xuất");
        btnLapPhieuXuat.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 44, 0, 0));
        btnLapPhieuXuat.setBorderPainted(false);
        btnLapPhieuXuat.setContentAreaFilled(false);
        btnLapPhieuXuat.setFocusPainted(false);
        btnLapPhieuXuat.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnLapPhieuXuat.setMaximumSize(new java.awt.Dimension(32767, 34));
        btnLapPhieuXuat.setOpaque(true);
        pnlXuatKho.add(btnLapPhieuXuat);

        btnPhieuXuatDaLuu.setBackground(new java.awt.Color(44, 59, 65));
        btnPhieuXuatDaLuu.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnPhieuXuatDaLuu.setForeground(new java.awt.Color(184, 199, 206));
        btnPhieuXuatDaLuu.setText("Phiếu xuất đã lưu");
        btnPhieuXuatDaLuu.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 44, 0, 0));
        btnPhieuXuatDaLuu.setBorderPainted(false);
        btnPhieuXuatDaLuu.setContentAreaFilled(false);
        btnPhieuXuatDaLuu.setFocusPainted(false);
        btnPhieuXuatDaLuu.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnPhieuXuatDaLuu.setMaximumSize(new java.awt.Dimension(32767, 34));
        btnPhieuXuatDaLuu.setOpaque(true);
        pnlXuatKho.add(btnPhieuXuatDaLuu);

        pnlItems.add(pnlXuatKho);

        btnHoanTra.setBackground(new java.awt.Color(44, 59, 65));
        btnHoanTra.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnHoanTra.setForeground(new java.awt.Color(184, 199, 206));
        btnHoanTra.setText("Hoàn trả");
        btnHoanTra.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 18, 0, 0));
        btnHoanTra.setBorderPainted(false);
        btnHoanTra.setContentAreaFilled(false);
        btnHoanTra.setFocusPainted(false);
        btnHoanTra.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnHoanTra.setMaximumSize(new java.awt.Dimension(32767, 42));
        btnHoanTra.setOpaque(true);
        pnlItems.add(btnHoanTra);

        pnlHoanTra.setBackground(new java.awt.Color(44, 59, 65));
        pnlHoanTra.setForeground(new java.awt.Color(184, 199, 206));
        pnlHoanTra.setLayout(new javax.swing.BoxLayout(pnlHoanTra, javax.swing.BoxLayout.Y_AXIS));

        btnLapPhieuHoanTra.setBackground(new java.awt.Color(44, 59, 65));
        btnLapPhieuHoanTra.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnLapPhieuHoanTra.setForeground(new java.awt.Color(184, 199, 206));
        btnLapPhieuHoanTra.setText("Lập phiếu hoàn trả");
        btnLapPhieuHoanTra.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 44, 0, 0));
        btnLapPhieuHoanTra.setBorderPainted(false);
        btnLapPhieuHoanTra.setContentAreaFilled(false);
        btnLapPhieuHoanTra.setFocusPainted(false);
        btnLapPhieuHoanTra.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnLapPhieuHoanTra.setMaximumSize(new java.awt.Dimension(32767, 34));
        pnlHoanTra.add(btnLapPhieuHoanTra);

        btnPhieuHoanTraDaLuu.setBackground(new java.awt.Color(44, 59, 65));
        btnPhieuHoanTraDaLuu.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnPhieuHoanTraDaLuu.setForeground(new java.awt.Color(184, 199, 206));
        btnPhieuHoanTraDaLuu.setText("Phiếu hoàn trả đã lưu");
        btnPhieuHoanTraDaLuu.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 44, 0, 0));
        btnPhieuHoanTraDaLuu.setBorderPainted(false);
        btnPhieuHoanTraDaLuu.setContentAreaFilled(false);
        btnPhieuHoanTraDaLuu.setFocusPainted(false);
        btnPhieuHoanTraDaLuu.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnPhieuHoanTraDaLuu.setMaximumSize(new java.awt.Dimension(32767, 34));
        pnlHoanTra.add(btnPhieuHoanTraDaLuu);

        pnlItems.add(pnlHoanTra);

        btnKiemKe.setBackground(new java.awt.Color(44, 59, 65));
        btnKiemKe.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnKiemKe.setForeground(new java.awt.Color(184, 199, 206));
        btnKiemKe.setText("Kiểm kê");
        btnKiemKe.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 18, 0, 0));
        btnKiemKe.setBorderPainted(false);
        btnKiemKe.setContentAreaFilled(false);
        btnKiemKe.setFocusPainted(false);
        btnKiemKe.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnKiemKe.setMaximumSize(new java.awt.Dimension(32767, 42));
        btnKiemKe.setOpaque(true);
        pnlItems.add(btnKiemKe);

        pnlKiemKe.setBackground(new java.awt.Color(44, 59, 65));
        pnlKiemKe.setForeground(new java.awt.Color(184, 199, 206));
        pnlKiemKe.setLayout(new javax.swing.BoxLayout(pnlKiemKe, javax.swing.BoxLayout.Y_AXIS));

        btnLapPhieuKiemKe.setBackground(new java.awt.Color(44, 59, 65));
        btnLapPhieuKiemKe.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnLapPhieuKiemKe.setForeground(new java.awt.Color(184, 199, 206));
        btnLapPhieuKiemKe.setText("Lập phiếu kiểm kê");
        btnLapPhieuKiemKe.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 44, 0, 0));
        btnLapPhieuKiemKe.setBorderPainted(false);
        btnLapPhieuKiemKe.setContentAreaFilled(false);
        btnLapPhieuKiemKe.setFocusPainted(false);
        btnLapPhieuKiemKe.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnLapPhieuKiemKe.setMaximumSize(new java.awt.Dimension(32767, 34));
        btnLapPhieuKiemKe.setOpaque(true);
        pnlKiemKe.add(btnLapPhieuKiemKe);

        btnLichSuKiemKe.setBackground(new java.awt.Color(44, 59, 65));
        btnLichSuKiemKe.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnLichSuKiemKe.setForeground(new java.awt.Color(184, 199, 206));
        btnLichSuKiemKe.setText("Lịch sử kiểm kê");
        btnLichSuKiemKe.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 44, 0, 0));
        btnLichSuKiemKe.setBorderPainted(false);
        btnLichSuKiemKe.setContentAreaFilled(false);
        btnLichSuKiemKe.setFocusPainted(false);
        btnLichSuKiemKe.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnLichSuKiemKe.setMaximumSize(new java.awt.Dimension(32767, 34));
        btnLichSuKiemKe.setOpaque(true);
        pnlKiemKe.add(btnLichSuKiemKe);

        pnlItems.add(pnlKiemKe);

        btnBaoCao.setBackground(new java.awt.Color(44, 59, 65));
        btnBaoCao.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnBaoCao.setForeground(new java.awt.Color(184, 199, 206));
        btnBaoCao.setText("Báo cáo");
        btnBaoCao.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 18, 0, 0));
        btnBaoCao.setBorderPainted(false);
        btnBaoCao.setContentAreaFilled(false);
        btnBaoCao.setFocusPainted(false);
        btnBaoCao.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnBaoCao.setMaximumSize(new java.awt.Dimension(32767, 42));
        btnBaoCao.setOpaque(true);
        pnlItems.add(btnBaoCao);

        pnlBaoCao.setBackground(new java.awt.Color(44, 59, 65));
        pnlBaoCao.setForeground(new java.awt.Color(184, 199, 206));
        pnlBaoCao.setLayout(new javax.swing.BoxLayout(pnlBaoCao, javax.swing.BoxLayout.Y_AXIS));

        btnBaoCaoTonKho.setBackground(new java.awt.Color(44, 59, 65));
        btnBaoCaoTonKho.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnBaoCaoTonKho.setForeground(new java.awt.Color(184, 199, 206));
        btnBaoCaoTonKho.setText("Báo cáo tồn kho");
        btnBaoCaoTonKho.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 44, 0, 0));
        btnBaoCaoTonKho.setBorderPainted(false);
        btnBaoCaoTonKho.setContentAreaFilled(false);
        btnBaoCaoTonKho.setFocusPainted(false);
        btnBaoCaoTonKho.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnBaoCaoTonKho.setMaximumSize(new java.awt.Dimension(32767, 34));
        btnBaoCaoTonKho.setOpaque(true);
        pnlBaoCao.add(btnBaoCaoTonKho);

        btnBaoCaoNhapXuat.setBackground(new java.awt.Color(44, 59, 65));
        btnBaoCaoNhapXuat.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnBaoCaoNhapXuat.setForeground(new java.awt.Color(184, 199, 206));
        btnBaoCaoNhapXuat.setText("Báo cáo nhập xuất");
        btnBaoCaoNhapXuat.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 44, 0, 0));
        btnBaoCaoNhapXuat.setBorderPainted(false);
        btnBaoCaoNhapXuat.setContentAreaFilled(false);
        btnBaoCaoNhapXuat.setFocusPainted(false);
        btnBaoCaoNhapXuat.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnBaoCaoNhapXuat.setMaximumSize(new java.awt.Dimension(32767, 34));
        btnBaoCaoNhapXuat.setOpaque(true);
        btnBaoCaoNhapXuat.addActionListener(this::btnBaoCaoNhapXuatActionPerformed);
        pnlBaoCao.add(btnBaoCaoNhapXuat);

        btnBaoCaoHangSapHet.setBackground(new java.awt.Color(44, 59, 65));
        btnBaoCaoHangSapHet.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnBaoCaoHangSapHet.setForeground(new java.awt.Color(184, 199, 206));
        btnBaoCaoHangSapHet.setText("Báo cáo hàng sắp hết");
        btnBaoCaoHangSapHet.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 44, 0, 0));
        btnBaoCaoHangSapHet.setBorderPainted(false);
        btnBaoCaoHangSapHet.setContentAreaFilled(false);
        btnBaoCaoHangSapHet.setFocusPainted(false);
        btnBaoCaoHangSapHet.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnBaoCaoHangSapHet.setMaximumSize(new java.awt.Dimension(32767, 34));
        btnBaoCaoHangSapHet.setOpaque(true);
        pnlBaoCao.add(btnBaoCaoHangSapHet);

        pnlItems.add(pnlBaoCao);

        btnTaiKhoan.setBackground(new java.awt.Color(44, 59, 65));
        btnTaiKhoan.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnTaiKhoan.setForeground(new java.awt.Color(184, 199, 206));
        btnTaiKhoan.setText("Tài khoản");
        btnTaiKhoan.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 18, 0, 0));
        btnTaiKhoan.setBorderPainted(false);
        btnTaiKhoan.setContentAreaFilled(false);
        btnTaiKhoan.setFocusPainted(false);
        btnTaiKhoan.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnTaiKhoan.setMaximumSize(new java.awt.Dimension(32767, 42));
        btnTaiKhoan.setOpaque(true);
        btnTaiKhoan.addActionListener(this::btnTaiKhoanActionPerformed);
        pnlItems.add(btnTaiKhoan);

        pnlTaiKhoan.setBackground(new java.awt.Color(44, 59, 65));
        pnlTaiKhoan.setForeground(new java.awt.Color(184, 199, 206));
        pnlTaiKhoan.setLayout(new javax.swing.BoxLayout(pnlTaiKhoan, javax.swing.BoxLayout.Y_AXIS));

        btnTaiKhoanCuaToi.setBackground(new java.awt.Color(44, 59, 65));
        btnTaiKhoanCuaToi.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnTaiKhoanCuaToi.setForeground(new java.awt.Color(184, 199, 206));
        btnTaiKhoanCuaToi.setText("Tài khoản của tôi");
        btnTaiKhoanCuaToi.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 44, 0, 0));
        btnTaiKhoanCuaToi.setBorderPainted(false);
        btnTaiKhoanCuaToi.setContentAreaFilled(false);
        btnTaiKhoanCuaToi.setFocusPainted(false);
        btnTaiKhoanCuaToi.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnTaiKhoanCuaToi.setMaximumSize(new java.awt.Dimension(32767, 34));
        btnTaiKhoanCuaToi.setOpaque(true);
        btnTaiKhoanCuaToi.addActionListener(this::btnTaiKhoanCuaToiActionPerformed);
        pnlTaiKhoan.add(btnTaiKhoanCuaToi);

        btnDangXuat.setBackground(new java.awt.Color(44, 59, 65));
        btnDangXuat.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        btnDangXuat.setForeground(new java.awt.Color(184, 199, 206));
        btnDangXuat.setText("Đăng xuất");
        btnDangXuat.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 44, 0, 0));
        btnDangXuat.setBorderPainted(false);
        btnDangXuat.setContentAreaFilled(false);
        btnDangXuat.setFocusPainted(false);
        btnDangXuat.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        btnDangXuat.setMaximumSize(new java.awt.Dimension(32767, 34));
        btnDangXuat.setOpaque(true);
        btnDangXuat.addActionListener(this::btnDangXuatActionPerformed);
        pnlTaiKhoan.add(btnDangXuat);

        pnlItems.add(pnlTaiKhoan);

        pnlMenu.add(pnlItems, java.awt.BorderLayout.CENTER);

        getContentPane().add(pnlMenu, java.awt.BorderLayout.LINE_START);

        javax.swing.GroupLayout pnlContentLayout = new javax.swing.GroupLayout(pnlContent);
        pnlContent.setLayout(pnlContentLayout);
        pnlContentLayout.setHorizontalGroup(
            pnlContentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 750, Short.MAX_VALUE)
        );
        pnlContentLayout.setVerticalGroup(
            pnlContentLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 647, Short.MAX_VALUE)
        );

        getContentPane().add(pnlContent, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btnDangXuatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDangXuatActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnDangXuatActionPerformed

    private void btnTaiKhoanCuaToiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTaiKhoanCuaToiActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnTaiKhoanCuaToiActionPerformed

    private void btnTaiKhoanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTaiKhoanActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnTaiKhoanActionPerformed

    private void btnLayHangActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLayHangActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnLayHangActionPerformed

    private void btnLapPhieuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLapPhieuActionPerformed
        
    }//GEN-LAST:event_btnLapPhieuActionPerformed

    private void btnTrangChuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnTrangChuActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnTrangChuActionPerformed

    private void btnLichSuLayHangActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLichSuLayHangActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnLichSuLayHangActionPerformed

    private void btnBaoCaoNhapXuatActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBaoCaoNhapXuatActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnBaoCaoNhapXuatActionPerformed

    /**
     * @param args the command line arguments
     */
        public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(() -> new Main().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnBaoCao;
    private javax.swing.JButton btnBaoCaoHangSapHet;
    private javax.swing.JButton btnBaoCaoNhapXuat;
    private javax.swing.JButton btnBaoCaoTonKho;
    private javax.swing.JButton btnCatHangVaoViTri;
    private javax.swing.JButton btnChuyenViTri;
    private javax.swing.JButton btnDSPhieu;
    private javax.swing.JButton btnDangXuat;
    private javax.swing.JButton btnDanhSachLayHang;
    private javax.swing.JButton btnHoanTra;
    private javax.swing.JButton btnKiemKe;
    private javax.swing.JButton btnLapPhieu;
    private javax.swing.JButton btnLapPhieuHoanTra;
    private javax.swing.JButton btnLapPhieuKiemKe;
    private javax.swing.JButton btnLapPhieuXuat;
    private javax.swing.JButton btnLayHang;
    private javax.swing.JButton btnLichSuKiemKe;
    private javax.swing.JButton btnLichSuLayHang;
    private javax.swing.JButton btnLuuTru;
    private javax.swing.JButton btnNhapKho;
    private javax.swing.JButton btnPhieuHoanTraDaLuu;
    private javax.swing.JButton btnPhieuXuatDaLuu;
    private javax.swing.JButton btnSoDoViTriKho;
    private javax.swing.JButton btnTaiKhoan;
    private javax.swing.JButton btnTaiKhoanCuaToi;
    private javax.swing.JButton btnTrangChu;
    private javax.swing.JButton btnXuatKho;
    private javax.swing.JLabel lblAvatar;
    private javax.swing.JLabel lblTen;
    private javax.swing.JPanel pnlBaoCao;
    private javax.swing.JPanel pnlContent;
    private javax.swing.JPanel pnlHeader;
    private javax.swing.JPanel pnlHoanTra;
    private javax.swing.JPanel pnlItems;
    private javax.swing.JPanel pnlKiemKe;
    private javax.swing.JPanel pnlLayHang;
    private javax.swing.JPanel pnlLuuTru;
    private javax.swing.JPanel pnlMenu;
    private javax.swing.JPanel pnlNhapKho;
    private javax.swing.JPanel pnlTaiKhoan;
    private javax.swing.JPanel pnlXuatKho;
    // End of variables declaration//GEN-END:variables
}
