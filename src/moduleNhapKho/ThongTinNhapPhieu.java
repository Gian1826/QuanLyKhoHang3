package moduleNhapKho;
import View.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
public class ThongTinNhapPhieu extends javax.swing.JPanel {
    private DefaultTableModel model;
    public ThongTinNhapPhieu() {
        initComponents();
        txtMaPhieu.setText(" ");
        txtMaPhieu.setEditable(false);
        khoiTaoBang();
        txtNgayNhap.setText(new java.text.SimpleDateFormat("dd-MM-yyyy").format(new java.util.Date()));
    }
    private void khoiTaoBang() {
        model = new DefaultTableModel(
                new Object[]{"Mã sản phẩm", "Tên sản phẩm", "Số lượng", "Đơn giá", "Thành tiền"}, 0);
        tblChiTiet.setModel(model);
    }
    private void luuPhieu() {
        DefaultTableModel model = (DefaultTableModel) tblChiTiet.getModel();
    if (model.getRowCount() == 0) return;
    
    String tenNhaCungCap = txtNhaCungCap.getText().trim();
    Connection conn = null;
    try {
        conn = KetNoiDB.getConnection();
        
        // 1. DÒ TÌM MÃ NHÀ CUNG CẤP
        int maNhaCungCap = -1;
        PreparedStatement psTimNCC = conn.prepareStatement("SELECT MaNhaCungCap FROM NhaCungCap WHERE TenNhaCungCap = ?");
        psTimNCC.setString(1, tenNhaCungCap);
        ResultSet rsNCC = psTimNCC.executeQuery();
        if (rsNCC.next()) {
            maNhaCungCap = rsNCC.getInt("MaNhaCungCap");
        } else {
            JOptionPane.showMessageDialog(this, "Nhà cung cấp không tồn tại!");
            return;
        }
        
        conn.setAutoCommit(false); 
        String ngayNhap = java.time.LocalDate.now().toString();
        double tongTien = 0; // Tính tổng...
        
        // 2. LƯU PHIẾU NHẬP (TRUYỀN ID INT VÀO)
        String sqlPhieu = "INSERT INTO PhieuNhap (NgayNhap, MaNhaCungCap, MaNhanVien, TongTien) VALUES (?, ?, ?, ?)";
        PreparedStatement psPhieu = conn.prepareStatement(sqlPhieu, Statement.RETURN_GENERATED_KEYS);
        psPhieu.setString(1, ngayNhap);
        psPhieu.setInt(2, maNhaCungCap); // Mã ID dạng số
        psPhieu.setInt(3, 1); // Mã nhân viên mặc định
        psPhieu.setDouble(4, tongTien);
        psPhieu.executeUpdate();

        ResultSet rs = psPhieu.getGeneratedKeys();
        int maPhieuMoi = 0; if (rs.next()) maPhieuMoi = rs.getInt(1);

        // 3. LƯU CHI TIẾT
        String sqlChiTiet = "INSERT INTO ChiTietPhieuNhap (MaPhieuNhap, MaSanPham, SoLuongNhap, DonGiaNhap) VALUES (?, ?, ?, ?)";
        PreparedStatement psChiTiet = conn.prepareStatement(sqlChiTiet);
        
        for (int i = 0; i < model.getRowCount(); i++) {
            psChiTiet.setInt(1, maPhieuMoi);
            psChiTiet.setInt(2, Integer.parseInt(model.getValueAt(i, 0).toString())); // Lấy Mã SP từ cột 0 của JTable
            psChiTiet.setDouble(3, Double.parseDouble(model.getValueAt(i, 2).toString())); // Số lượng
            psChiTiet.setDouble(4, Double.parseDouble(model.getValueAt(i, 3).toString())); // Đơn giá
            psChiTiet.executeUpdate();
        }

        conn.commit(); 
        JOptionPane.showMessageDialog(this, "Lưu thành công!");
        } catch (Exception e) {
        }
    }
    private void baoLoi(SQLException e) {
        JOptionPane.showMessageDialog(this, "Lỗi CSDL:\n" + e.getMessage());
    }
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlThongTin = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        txtMaPhieu = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        txtNgayNhap = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        txtNhaCungCap = new javax.swing.JTextField();
        pnlGiua = new javax.swing.JPanel();
        pnlChonHang = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        txtSanPham = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        txtSoLuong = new javax.swing.JTextField();
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

        jLabel2.setText("Ngày nhập:");

        txtNgayNhap.setColumns(12);

        jLabel3.setText("Nhà cung cấp:");

        txtNhaCungCap.setColumns(20);

        javax.swing.GroupLayout pnlThongTinLayout = new javax.swing.GroupLayout(pnlThongTin);
        pnlThongTin.setLayout(pnlThongTinLayout);
        pnlThongTinLayout.setHorizontalGroup(
            pnlThongTinLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlThongTinLayout.createSequentialGroup()
                .addGroup(pnlThongTinLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlThongTinLayout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(jLabel3)
                        .addGap(18, 18, 18)
                        .addComponent(txtNhaCungCap, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlThongTinLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jLabel1)
                        .addGap(18, 18, 18)
                        .addComponent(txtMaPhieu, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(87, 87, 87)))
                .addComponent(jLabel2)
                .addGap(18, 18, 18)
                .addComponent(txtNgayNhap, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(338, Short.MAX_VALUE))
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
                    .addComponent(txtNhaCungCap, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(7, Short.MAX_VALUE))
        );

        add(pnlThongTin, java.awt.BorderLayout.PAGE_START);

        pnlGiua.setLayout(new java.awt.BorderLayout());

        pnlChonHang.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Chọn hàng nhập", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 1, 14))); // NOI18N
        pnlChonHang.setPreferredSize(new java.awt.Dimension(600, 120));

        jLabel4.setText("Sản phẩm:");

        txtSanPham.setColumns(20);

        jLabel5.setText("Số lượng:");

        txtSoLuong.setColumns(20);
        txtSoLuong.addActionListener(this::txtSoLuongActionPerformed);

        jLabel6.setText("Đơn giá:");

        txtDonGia.setColumns(12);
        txtDonGia.addActionListener(this::txtDonGiaActionPerformed);

        btnThem.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnThem.setText("Thêm vào phiếu");
        btnThem.addActionListener(this::btnThemActionPerformed);

        btnXoa.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnXoa.setText("Xóa dòng");
        btnXoa.addActionListener(this::btnXoaActionPerformed);

        javax.swing.GroupLayout pnlChonHangLayout = new javax.swing.GroupLayout(pnlChonHang);
        pnlChonHang.setLayout(pnlChonHangLayout);
        pnlChonHangLayout.setHorizontalGroup(
            pnlChonHangLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlChonHangLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(pnlChonHangLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlChonHangLayout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addGap(18, 18, 18)
                        .addComponent(txtSanPham, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel5)
                        .addGap(18, 18, 18)
                        .addComponent(txtSoLuong, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(225, 225, 225)
                        .addComponent(jLabel6))
                    .addComponent(btnThem, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlChonHangLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(txtDonGia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnXoa))
                .addGap(24, 24, 24))
        );
        pnlChonHangLayout.setVerticalGroup(
            pnlChonHangLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlChonHangLayout.createSequentialGroup()
                .addGroup(pnlChonHangLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jLabel5)
                    .addComponent(jLabel6)
                    .addComponent(txtDonGia, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtSanPham, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtSoLuong, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(pnlChonHangLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnThem)
                    .addComponent(btnXoa))
                .addContainerGap(26, Short.MAX_VALUE))
        );

        pnlGiua.add(pnlChonHang, java.awt.BorderLayout.PAGE_START);

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

        pnlGiua.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        add(pnlGiua, java.awt.BorderLayout.CENTER);

        pnlCuoi.setPreferredSize(new java.awt.Dimension(600, 60));

        lblTong.setFont(new java.awt.Font("Segoe UI", 1, 16)); // NOI18N
        lblTong.setText("Tổng tiền");

        btnLamMoi.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnLamMoi.setText("Làm mới");
        btnLamMoi.addActionListener(this::btnLamMoiActionPerformed);

        btnLuu.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        btnLuu.setText("Lưu phiếu");
        btnLuu.addActionListener(this::btnLuuActionPerformed);

        javax.swing.GroupLayout pnlCuoiLayout = new javax.swing.GroupLayout(pnlCuoi);
        pnlCuoi.setLayout(pnlCuoiLayout);
        pnlCuoiLayout.setHorizontalGroup(
            pnlCuoiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlCuoiLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblTong)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 556, Short.MAX_VALUE)
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

    private void btnThemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnThemActionPerformed
        try {
        String tenSanPham = txtSanPham.getText().trim();
        int soLuong = Integer.parseInt(txtSoLuong.getText());
        double donGia = Double.parseDouble(txtDonGia.getText());
        double thanhTien = soLuong * donGia;
        if (tenSanPham.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập tên sản phẩm!");
            return;
        }
        int maSP = -1;
        Connection conn = KetNoiDB.getConnection();
        String sqlTimSP = "SELECT MaSanPham FROM SanPham WHERE TenSanPham = ?";
        PreparedStatement psTim = conn.prepareStatement(sqlTimSP);
        psTim.setString(1, tenSanPham);
        ResultSet rs = psTim.executeQuery();
        
        if (rs.next()) {
            maSP = rs.getInt("MaSanPham"); // Lấy được mã ID chuẩn
        } else {
            JOptionPane.showMessageDialog(this, "Sản phẩm không tồn tại trong kho!");
            return;
        }
        rs.close(); psTim.close(); conn.close();
        DefaultTableModel model = (DefaultTableModel) tblChiTiet.getModel();
        model.addRow(new Object[]{maSP, tenSanPham, soLuong, donGia, thanhTien});
        
        CapNhatTongTien();
        txtSanPham.setText(""); txtSoLuong.setText("0"); txtDonGia.setText("");
        
    } catch (Exception e) {
        JOptionPane.showMessageDialog(this, "Lỗi: Kiểm tra lại số lượng hoặc đơn giá!");
    }
    }//GEN-LAST:event_btnThemActionPerformed

    private void btnXoaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnXoaActionPerformed
        DefaultTableModel model = (DefaultTableModel) tblChiTiet.getModel();
        int row = tblChiTiet.getSelectedRow();
        if (row < 0){
            JOptionPane.showMessageDialog(this, "Hãy chọn một dòng trên bảng để xóa!");
            return;
        }
        model.removeRow(row); //xóa dòng được chọn
        CapNhatTongTien(); // tính lại tổng tiền
    }//GEN-LAST:event_btnXoaActionPerformed

    private void btnLuuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLuuActionPerformed
        luuPhieu();
    }//GEN-LAST:event_btnLuuActionPerformed

    private void btnLamMoiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLamMoiActionPerformed
        DefaultTableModel model = (DefaultTableModel) tblChiTiet.getModel();
    model.setRowCount(0);
    // Xóa trắng ô nhập
    txtNhaCungCap.setText("");
    txtSanPham.setText("");
    txtSoLuong.setText("");
    txtDonGia.setText("");
    txtMaPhieu.setText(" ");
    // Cập nhật ngày mới nhất và tổng tiền về 0
    txtNgayNhap.setText(new java.text.SimpleDateFormat("dd-MM-yyyy").format(new java.util.Date()));
    lblTong.setText("Tổng tiền: 0 VNĐ");
    }//GEN-LAST:event_btnLamMoiActionPerformed

    private void txtDonGiaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtDonGiaActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtDonGiaActionPerformed

    private void txtSoLuongActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSoLuongActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtSoLuongActionPerformed
    private void CapNhatTongTien(){
        double tong = 0;
        DefaultTableModel model = (DefaultTableModel) tblChiTiet.getModel();
        for(int i = 0; i< model.getRowCount(); i++) {
            tong += Double.parseDouble(model.getValueAt(i, 3).toString());
        }
        lblTong.setText(tong + " VND");
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnLamMoi;
    private javax.swing.JButton btnLuu;
    private javax.swing.JButton btnThem;
    private javax.swing.JButton btnXoa;
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
    private javax.swing.JTable tblChiTiet;
    private javax.swing.JTextField txtDonGia;
    private javax.swing.JTextField txtMaPhieu;
    private javax.swing.JTextField txtNgayNhap;
    private javax.swing.JTextField txtNhaCungCap;
    private javax.swing.JTextField txtSanPham;
    private javax.swing.JTextField txtSoLuong;
    // End of variables declaration//GEN-END:variables
}
