package View;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.JOptionPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.table.DefaultTableModel;
public class Viewnhaphang extends javax.swing.JPanel {

    /**
     * Creates new form Viewnhaphang
     */
        // ===== BIẾN DÙNG CHUNG =====
    private DefaultTableModel model;

    // Lớp nhỏ để combo box vừa hiện tên, vừa giữ mã
    private static class Item {
        int id; String ten; double gia;
        Item(int id, String ten, double gia) { this.id = id; this.ten = ten; this.gia = gia; }
        @Override public String toString() { return ten; }
    }

    // ===== CONSTRUCTOR =====
    public Viewnhaphang() {
        initComponents();

        // Ô mã phiếu: tự sinh khi lưu nên không cho gõ
        txtMaPhieu.setText("(Tự động)");
        txtMaPhieu.setEditable(false);

        khoiTaoBang();
        spnSoLuong.setModel(new SpinnerNumberModel(1, 1, 100000, 1));
        txtNgayNhap.setText(new java.text.SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()));
        napComboNCC();
        napComboSanPham();

        // Chọn sản phẩm thì tự điền đơn giá
        cboSanPham.addActionListener(e -> {
            Item sp = (Item) cboSanPham.getSelectedItem();
            if (sp != null) txtDonGia.setText(String.valueOf(sp.gia));
        });
        btnThem.addActionListener(e -> themVaoPhieu());
        btnXoa.addActionListener(e -> xoaDong());
        btnLuu.addActionListener(e -> luuPhieu());
        btnLamMoi.addActionListener(e -> lamMoi());

        // Điền đơn giá cho sản phẩm đầu tiên (đặt SAU các addActionListener)
        Item sp0 = (Item) cboSanPham.getSelectedItem();
        if (sp0 != null) txtDonGia.setText(String.valueOf(sp0.gia));
    }

    // ===== BẢNG =====
    private void khoiTaoBang() {
        model = new DefaultTableModel(
                new Object[]{"Mã SP", "Tên SP", "Số lượng", "Đơn giá", "Thành tiền"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tblChiTiet.setModel(model);
    }

    // ===== NẠP DỮ LIỆU CHO COMBO =====
    private void napComboNCC() {
        cboNCC.removeAllItems();
        try (Statement st = KetNoiDB.getConnection().createStatement();
             ResultSet rs = st.executeQuery("SELECT MaNhaCungCap, TenNhaCungCap FROM NhaCungCap")) {
            while (rs.next()) cboNCC.addItem(new Item(rs.getInt(1), rs.getString(2), 0));
        } catch (SQLException e) { baoLoi(e); }
    }

    private void napComboSanPham() {
        cboSanPham.removeAllItems();
        try (Statement st = KetNoiDB.getConnection().createStatement();
             ResultSet rs = st.executeQuery("SELECT MaSanPham, TenSanPham, GiaBan FROM SanPham")) {
            while (rs.next())
                cboSanPham.addItem(new Item(rs.getInt(1), rs.getString(2), rs.getDouble(3)));
        } catch (SQLException e) { baoLoi(e); }
    }

    // ===== THÊM / XÓA DÒNG =====
    private void themVaoPhieu() {
        Item sp = (Item) cboSanPham.getSelectedItem();
        if (sp == null) return;
        int sl = (Integer) spnSoLuong.getValue();
        double gia;
        try {
            gia = Double.parseDouble(txtDonGia.getText().trim());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Giá bán không hợp lệ!");
            return;
        }
        model.addRow(new Object[]{sp.id, sp.ten, sl, gia, sl * gia});
        tinhTong();
    }

    private void xoaDong() {
        int r = tblChiTiet.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Hãy chọn một dòng để xóa!");
            return;
        }
        model.removeRow(tblChiTiet.convertRowIndexToModel(r));
        tinhTong();
    }

    private double tinhTong() {
        double tong = 0;
        for (int i = 0; i < model.getRowCount(); i++)
            tong += ((Number) model.getValueAt(i, 4)).doubleValue();
        lblTong.setText("Tổng tiền: " + String.format("%,.0f", tong) + " VNĐ");
        return tong;
    }

    // ===== LƯU PHIẾU (transaction) =====
    private void luuPhieu() {
        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "Phiếu chưa có hàng nào!");
            return;
        }
        Item ncc = (Item) cboNCC.getSelectedItem();
        if (ncc == null) {
            JOptionPane.showMessageDialog(this, "Hãy chọn nhà cung cấp!");
            return;
        }
        Connection c = null;
        try {
            c = KetNoiDB.getConnection();
            c.setAutoCommit(false);          // bắt đầu transaction

            int maPN = 0;
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO PhieuNhap(NgayNhap, MaNhaCungCap, TongTien) VALUES (?,?,?)",
                    Statement.RETURN_GENERATED_KEYS)) {
                ps.setDate(1, java.sql.Date.valueOf(txtNgayNhap.getText().trim()));
                ps.setInt(2, ncc.id);
                ps.setDouble(3, tinhTong());
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    rs.next();
                    maPN = rs.getInt(1);
                }
            }

            try (PreparedStatement ct = c.prepareStatement(
                    "INSERT INTO ChiTietPhieuNhap(MaPhieuNhap, MaSanPham, SoLuong, GiaBan) VALUES (?,?,?,?)");
                 PreparedStatement ton = c.prepareStatement(
                    "UPDATE SanPham SET SoLuongTon = SoLuongTon + ? WHERE MaSP = ?")) {
                for (int i = 0; i < model.getRowCount(); i++) {
                    int maSP = (Integer) model.getValueAt(i, 0);
                    int sl   = (Integer) model.getValueAt(i, 2);
                    double g = ((Number) model.getValueAt(i, 3)).doubleValue();

                    ct.setInt(1, maPN); ct.setInt(2, maSP);
                    ct.setInt(3, sl);   ct.setDouble(4, g);
                    ct.addBatch();

                    ton.setInt(1, sl); ton.setInt(2, maSP);   // cộng tồn kho
                    ton.addBatch();
                }
                ct.executeBatch();
                ton.executeBatch();
            }

            c.commit();
            JOptionPane.showMessageDialog(this, "Đã lưu phiếu nhập số " + maPN);
            lamMoi();
        } catch (SQLException | IllegalArgumentException e) {
            try { if (c != null) c.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            JOptionPane.showMessageDialog(this,
                    "Lưu thất bại (ngày phải có dạng yyyy-MM-dd):\n" + e.getMessage());
        } finally {
            try { if (c != null) c.setAutoCommit(true); } catch (SQLException ex) { ex.printStackTrace(); }
        }
    }

    // ===== LÀM MỚI =====
    private void lamMoi() {
        model.setRowCount(0);
        spnSoLuong.setValue(1);
        tinhTong();
        napComboSanPham();
    }

    private void baoLoi(SQLException e) {
        JOptionPane.showMessageDialog(this, "Lỗi CSDL:\n" + e.getMessage());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlThongTin = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        txtMaPhieu = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        txtNgayNhap = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        cboNCC = new javax.swing.JComboBox<>();
        pnlGiua = new javax.swing.JPanel();
        pnlChonHang = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        cboSanPham = new javax.swing.JComboBox<>();
        jLabel5 = new javax.swing.JLabel();
        spnSoLuong = new javax.swing.JSpinner();
        jLabel6 = new javax.swing.JLabel();
        txtDonGia = new javax.swing.JTextField();
        btnThem = new javax.swing.JButton();
        btnXoa = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tblChiTiet = new javax.swing.JTable();
        pnlCuoi = new javax.swing.JPanel();
        lblTong = new javax.swing.JLabel();
        btnLamMoi = new javax.swing.JButton();
        btnLuu = new javax.swing.JButton();

        setLayout(new java.awt.BorderLayout());

        pnlThongTin.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Thông tin phiếu nhập", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 1, 14))); // NOI18N
        pnlThongTin.setPreferredSize(new java.awt.Dimension(600, 110));

        jLabel1.setText("Mã phiếu:");

        txtMaPhieu.setColumns(12);
        txtMaPhieu.setText("Tự động");

        jLabel2.setText("Ngày nhập:");

        txtNgayNhap.setColumns(12);

        jLabel3.setText("Nhà cung cấp:");

        javax.swing.GroupLayout pnlThongTinLayout = new javax.swing.GroupLayout(pnlThongTin);
        pnlThongTin.setLayout(pnlThongTinLayout);
        pnlThongTinLayout.setHorizontalGroup(
            pnlThongTinLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlThongTinLayout.createSequentialGroup()
                .addGroup(pnlThongTinLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlThongTinLayout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(cboNCC, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlThongTinLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel1)
                        .addGap(18, 18, 18)
                        .addComponent(txtMaPhieu, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(87, 87, 87)
                .addComponent(jLabel2)
                .addGap(18, 18, 18)
                .addComponent(txtNgayNhap, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(114, Short.MAX_VALUE))
        );
        pnlThongTinLayout.setVerticalGroup(
            pnlThongTinLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlThongTinLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlThongTinLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2)
                    .addComponent(txtNgayNhap, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtMaPhieu))
                .addGap(18, 18, 18)
                .addGroup(pnlThongTinLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(cboNCC, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        add(pnlThongTin, java.awt.BorderLayout.PAGE_START);

        pnlGiua.setLayout(new java.awt.BorderLayout());

        pnlChonHang.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Chọn hàng nhập", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 1, 14))); // NOI18N
        pnlChonHang.setPreferredSize(new java.awt.Dimension(600, 120));

        jLabel4.setText("Sản phẩm:");

        jLabel5.setText("Số lượng:");

        spnSoLuong.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        jLabel6.setText("Đơn giá:");

        txtDonGia.setColumns(12);

        btnThem.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnThem.setText("Thêm vào phiếu");

        btnXoa.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnXoa.setText("Xóa dòng");

        tblChiTiet.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(tblChiTiet);

        javax.swing.GroupLayout pnlChonHangLayout = new javax.swing.GroupLayout(pnlChonHang);
        pnlChonHang.setLayout(pnlChonHangLayout);
        pnlChonHangLayout.setHorizontalGroup(
            pnlChonHangLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlChonHangLayout.createSequentialGroup()
                .addContainerGap(13, Short.MAX_VALUE)
                .addGroup(pnlChonHangLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlChonHangLayout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(cboSanPham, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(26, 26, 26)
                        .addComponent(jLabel5)
                        .addGap(18, 18, 18)
                        .addComponent(spnSoLuong, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(65, 65, 65)
                        .addComponent(jLabel6))
                    .addComponent(btnThem, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlChonHangLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnXoa)
                    .addComponent(txtDonGia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(24, 24, 24))
            .addComponent(jScrollPane1)
        );
        pnlChonHangLayout.setVerticalGroup(
            pnlChonHangLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlChonHangLayout.createSequentialGroup()
                .addGroup(pnlChonHangLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(cboSanPham, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel5)
                    .addComponent(spnSoLuong, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6)
                    .addComponent(txtDonGia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(pnlChonHangLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnThem)
                    .addComponent(btnXoa))
                .addGap(38, 38, 38)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 211, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pnlGiua.add(pnlChonHang, java.awt.BorderLayout.PAGE_START);

        add(pnlGiua, java.awt.BorderLayout.CENTER);

        pnlCuoi.setPreferredSize(new java.awt.Dimension(600, 60));

        lblTong.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblTong.setText("Tổng tiền: 0 VNĐ");

        btnLamMoi.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnLamMoi.setText("Làm mới");

        btnLuu.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnLuu.setText("Lưu phiếu");

        javax.swing.GroupLayout pnlCuoiLayout = new javax.swing.GroupLayout(pnlCuoi);
        pnlCuoi.setLayout(pnlCuoiLayout);
        pnlCuoiLayout.setHorizontalGroup(
            pnlCuoiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCuoiLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblTong)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 187, Short.MAX_VALUE)
                .addComponent(btnLamMoi)
                .addGap(29, 29, 29)
                .addComponent(btnLuu)
                .addGap(61, 61, 61))
        );
        pnlCuoiLayout.setVerticalGroup(
            pnlCuoiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCuoiLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(pnlCuoiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblTong)
                    .addComponent(btnLamMoi)
                    .addComponent(btnLuu))
                .addContainerGap(15, Short.MAX_VALUE))
        );

        add(pnlCuoi, java.awt.BorderLayout.PAGE_END);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnLamMoi;
    private javax.swing.JButton btnLuu;
    private javax.swing.JButton btnThem;
    private javax.swing.JButton btnXoa;
    private javax.swing.JComboBox<Item> cboNCC;
    private javax.swing.JComboBox<Item> cboSanPham;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblTong;
    private javax.swing.JPanel pnlChonHang;
    private javax.swing.JPanel pnlCuoi;
    private javax.swing.JPanel pnlGiua;
    private javax.swing.JPanel pnlThongTin;
    private javax.swing.JSpinner spnSoLuong;
    private javax.swing.JTable tblChiTiet;
    private javax.swing.JTextField txtDonGia;
    private javax.swing.JTextField txtMaPhieu;
    private javax.swing.JTextField txtNgayNhap;
    // End of variables declaration//GEN-END:variables
}
