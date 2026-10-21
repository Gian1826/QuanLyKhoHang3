package moduleTaiKhoan;
import View.PhienDangNhap;
import View.KetNoiDB;
import View.Main;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.swing.JOptionPane;
public class ThongTinTaiKhoan extends javax.swing.JPanel {   
        public ThongTinTaiKhoan() {
        initComponents();
        napThongTin();
        /*try {
            Connection conn = KetNoiDB.getConnection();
            String sql = "SELECT TOP 1 TenTaiKhoan FROM TaiKhoan";
            PreparedStatement st = conn.prepareStatement(sql);
            ResultSet rs = st.executeQuery();
            if(rs.next()){
                String TenTaiKhoan = rs.getString("TenTaiKhoan");
                txtTenDN.setText(TenTaiKhoan);
            }
            rs.close();
            st.close();
            conn.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Loi:" + e.getMessage());
        }*/
    }

    // Gọi mỗi lần mở màn hình để hiện thông tin mới nhất
    public void napThongTin() {
        txtTenDN.setText(PhienDangNhap.tenDangNhap);
        txtHoTen.setText(PhienDangNhap.hoTen);
        if("Admin".equalsIgnoreCase(PhienDangNhap.vaiTro)){
            txtVaiTro.setText("Quản trị viên");
        } else {
            txtVaiTro.setText("Nhân viên");
        }
        txtTenDN.setEditable(false);
        txtHoTen.setEditable(false);
        txtVaiTro.setEditable(false);
    }

    private void thongBao(String s, boolean thanhCong) {
        lblKetQua.setForeground(thanhCong
                ? new java.awt.Color(0x2E, 0x7D, 0x32)
                : new java.awt.Color(0xD3, 0x2F, 0x2F));
        lblKetQua.setText(s);
    }
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlThongTin = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        txtTenDN = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        txtHoTen = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        txtVaiTro = new javax.swing.JTextField();
        pnlDoiMK = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        txtMatKhauCu = new javax.swing.JPasswordField();
        jLabel5 = new javax.swing.JLabel();
        txtMatKhauMoi = new javax.swing.JPasswordField();
        jLabel6 = new javax.swing.JLabel();
        txtNhapLaiMatKhauMoi = new javax.swing.JPasswordField();
        btnDoiMK = new javax.swing.JButton();
        lblKetQua = new javax.swing.JLabel();

        setLayout(new java.awt.BorderLayout());

        pnlThongTin.setBorder(javax.swing.BorderFactory.createTitledBorder("Thông tin tài khoản"));
        pnlThongTin.setPreferredSize(new java.awt.Dimension(600, 150));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel1.setText("Tên đăng nhập:");

        txtTenDN.setColumns(25);
        txtTenDN.addActionListener(this::txtTenDNActionPerformed);

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel2.setText("Họ tên:");

        txtHoTen.setColumns(25);

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel3.setText("Vai trò:");

        txtVaiTro.setColumns(25);

        javax.swing.GroupLayout pnlThongTinLayout = new javax.swing.GroupLayout(pnlThongTin);
        pnlThongTin.setLayout(pnlThongTinLayout);
        pnlThongTinLayout.setHorizontalGroup(
            pnlThongTinLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlThongTinLayout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addGroup(pnlThongTinLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlThongTinLayout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addGap(80, 80, 80)
                        .addComponent(txtVaiTro, javax.swing.GroupLayout.PREFERRED_SIZE, 297, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(pnlThongTinLayout.createSequentialGroup()
                        .addGroup(pnlThongTinLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel1))
                        .addGap(35, 35, 35)
                        .addGroup(pnlThongTinLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtHoTen, javax.swing.GroupLayout.DEFAULT_SIZE, 297, Short.MAX_VALUE)
                            .addComponent(txtTenDN))))
                .addContainerGap(309, Short.MAX_VALUE))
        );
        pnlThongTinLayout.setVerticalGroup(
            pnlThongTinLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlThongTinLayout.createSequentialGroup()
                .addGap(0, 13, Short.MAX_VALUE)
                .addGroup(pnlThongTinLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(txtTenDN, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlThongTinLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(txtHoTen, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlThongTinLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(txtVaiTro, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(24, 24, 24))
        );

        add(pnlThongTin, java.awt.BorderLayout.PAGE_START);

        pnlDoiMK.setBorder(javax.swing.BorderFactory.createTitledBorder("Đổi mật khẩu"));

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel4.setText("Mật khẩu cũ:");

        txtMatKhauCu.setColumns(25);

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel5.setText("Mật khẩu mới:");

        txtMatKhauMoi.setColumns(25);

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel6.setText("Nhập lại mật khẩu mới:");

        txtNhapLaiMatKhauMoi.setColumns(25);

        btnDoiMK.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnDoiMK.setText("Đổi mật khẩu");
        btnDoiMK.addActionListener(this::btnDoiMKActionPerformed);

        javax.swing.GroupLayout pnlDoiMKLayout = new javax.swing.GroupLayout(pnlDoiMK);
        pnlDoiMK.setLayout(pnlDoiMKLayout);
        pnlDoiMKLayout.setHorizontalGroup(
            pnlDoiMKLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDoiMKLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlDoiMKLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlDoiMKLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(lblKetQua)
                        .addGap(94, 94, 94))
                    .addGroup(pnlDoiMKLayout.createSequentialGroup()
                        .addGroup(pnlDoiMKLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(pnlDoiMKLayout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addGap(61, 61, 61)
                                .addComponent(txtMatKhauMoi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(pnlDoiMKLayout.createSequentialGroup()
                                .addComponent(jLabel6)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(txtNhapLaiMatKhauMoi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pnlDoiMKLayout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(txtMatKhauCu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 177, Short.MAX_VALUE)))
                .addComponent(btnDoiMK)
                .addGap(28, 28, 28))
        );
        pnlDoiMKLayout.setVerticalGroup(
            pnlDoiMKLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlDoiMKLayout.createSequentialGroup()
                .addGap(22, 22, 22)
                .addGroup(pnlDoiMKLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlDoiMKLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(lblKetQua))
                    .addGroup(pnlDoiMKLayout.createSequentialGroup()
                        .addGroup(pnlDoiMKLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(txtMatKhauCu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(53, 53, 53)
                        .addGroup(pnlDoiMKLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel5)
                            .addComponent(txtMatKhauMoi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(67, 67, 67)
                        .addGroup(pnlDoiMKLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel6)
                            .addComponent(txtNhapLaiMatKhauMoi, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 114, Short.MAX_VALUE)
                        .addComponent(btnDoiMK)
                        .addContainerGap())))
        );

        add(pnlDoiMK, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void btnDoiMKActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDoiMKActionPerformed
        try {
            String MatKhauCu = new String(txtMatKhauCu.getPassword());
            String MatKhauMoi = new String(txtMatKhauMoi.getPassword());
            String NhapMatKhauMoi = new String(txtNhapLaiMatKhauMoi.getPassword());
            if(MatKhauCu.isEmpty() || MatKhauMoi.isEmpty() || NhapMatKhauMoi.isEmpty()){
                JOptionPane.showMessageDialog(this, "Vui lòng không để trống thông tin");
                return;
            }
        Connection conn = KetNoiDB.getConnection();
        String sqlCheck = "SELECT COUNT(*) FROM TaiKhoan WHERE TenTaiKhoan = ? AND MatKhau = ?";
        PreparedStatement st = conn.prepareStatement(sqlCheck);
        st.setString(1, PhienDangNhap.tenDangNhap);
        st.setString(2, MatKhauCu);
        ResultSet rs = st.executeQuery();
        if (rs.next() && rs.getInt(1) > 0){
            String sqlU ="UPDATE TaiKhoan SET MatKhau = ? WHERE TentaiKhoan = ?";
            PreparedStatement stU = conn.prepareStatement(sqlU);
            stU.setString(1, NhapMatKhauMoi);
            stU.setString(2, PhienDangNhap.tenDangNhap);
            int TrangThai = stU.executeUpdate();
            if (TrangThai > 0){
                JOptionPane.showMessageDialog(this, "Đổi mật khẩu thành công!");
            }
            stU.close();
        }else {
            JOptionPane.showMessageDialog(this, "Đổi mật khẩu thất bại!");
        }
        rs.close();
        st.close();
        conn.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Lỗi:" + e.getMessage());
        }
    }//GEN-LAST:event_btnDoiMKActionPerformed

    private void txtTenDNActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtTenDNActionPerformed
        
    }//GEN-LAST:event_txtTenDNActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnDoiMK;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel lblKetQua;
    private javax.swing.JPanel pnlDoiMK;
    private javax.swing.JPanel pnlThongTin;
    private javax.swing.JTextField txtHoTen;
    private javax.swing.JPasswordField txtMatKhauCu;
    private javax.swing.JPasswordField txtMatKhauMoi;
    private javax.swing.JPasswordField txtNhapLaiMatKhauMoi;
    private javax.swing.JTextField txtTenDN;
    private javax.swing.JTextField txtVaiTro;
    // End of variables declaration//GEN-END:variables
}
