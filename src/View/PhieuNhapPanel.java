
package View;
public class PhieuNhapPanel extends javax.swing.JPanel {
    public PhieuNhapPanel() {
        initComponents();
    }
public javax.swing.JTextField getTxtMaPhieu() { 
            return txtMaPhieu; 
            }
public javax.swing.JTextField getTxtNgayNhap() { 
            return txtNgayNhap; 
            }
public javax.swing.JComboBox<String> getCbNguoiLap() { 
            return cboNguoiLap;    
            }
public javax.swing.JComboBox<String> getCbNhaCungCap() { 
            return cboNhaCungCap; 
            }
public javax.swing.JComboBox<String> getCbKhuNhan() { return cboKhuNhan; }
public javax.swing.JTextField getTxtGhiChu() { return txtGhiChu; }

public javax.swing.JButton getBtnThemDong() { return btnThemDong; }
public javax.swing.JButton getBtnXoaDong() { return btnXoaDong; }
public javax.swing.JTable getTableChiTiet() { return TableChiTiet; }

public javax.swing.JLabel getLblTongTien() { return lblTongTien; } // Label hiển thị "Tổng tiền: 1.800.000 đ"
public javax.swing.JButton getBtnHuy() { return btnHuy; }
public javax.swing.JButton getBtnLuuNhap() { return btnLuuNhap; }
public javax.swing.JButton getBtnLuuPhieu() { return btnLuuPhieu; }
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        txtMaPhieu = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        txtNgayNhap = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        cboNguoiLap = new javax.swing.JComboBox<>();
        jLabel5 = new javax.swing.JLabel();
        cboNhaCungCap = new javax.swing.JComboBox<>();
        jLabel6 = new javax.swing.JLabel();
        cboKhuNhan = new javax.swing.JComboBox<>();
        jLabel7 = new javax.swing.JLabel();
        txtGhiChu = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        btnThemDong = new javax.swing.JButton();
        btnXoaDong = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        TableChiTiet = new javax.swing.JTable();
        lblTongTien = new javax.swing.JLabel();
        btnLuuPhieu = new javax.swing.JButton();
        btnLuuNhap = new javax.swing.JButton();
        btnHuy = new javax.swing.JButton();

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel1.setText("Lập phiếu nhập");

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Thông tin phiếu", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 1, 12))); // NOI18N

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel2.setText("Mã Phiếu");

        txtMaPhieu.setColumns(25);

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel3.setText("Ngày Nhập");

        txtNgayNhap.setColumns(25);

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel4.setText("Người lập");

        cboNguoiLap.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Nhân viên", "admin" }));

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel5.setText("Nhà Cung Cấp");

        cboNhaCungCap.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Nhà Cung Cấp Gạo", "Nhà Cung Cấp Bột mì", "Nhà Cung Cấp Sữa", "Nhà Cung Cấp Đồ ăn", "Nhà Cung Cấp Trứng" }));

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel6.setText("Khu nhận");

        cboKhuNhan.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Khu A", "Khu B", "Khu C", "Khu D" }));

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabel7.setText("Ghi Chú");

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Chi tiết hàng nhập", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 1, 12))); // NOI18N

        btnThemDong.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnThemDong.setText("Thêm dòng");
        btnThemDong.addActionListener(this::btnThemDongActionPerformed);

        btnXoaDong.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnXoaDong.setText("Xóa Dòng");

        TableChiTiet.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "STT", "Mã SP", "Tên Sản Phẩm", "ĐVT", "Số lượng", "Đơn giá", "Thành Tiền "
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Integer.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Integer.class, java.lang.Double.class, java.lang.Double.class
            };
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(TableChiTiet);

        lblTongTien.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        lblTongTien.setText("Tổng tiền: 1.800.000 đ");

        btnLuuPhieu.setBackground(new java.awt.Color(0, 51, 204));
        btnLuuPhieu.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnLuuPhieu.setForeground(new java.awt.Color(255, 255, 255));
        btnLuuPhieu.setText("Lưu Phiếu");

        btnLuuNhap.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnLuuNhap.setText("Lưu nháp");
        btnLuuNhap.addActionListener(this::btnLuuNhapActionPerformed);

        btnHuy.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        btnHuy.setText("Hủy");
        btnHuy.addActionListener(this::btnHuyActionPerformed);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(btnThemDong, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnXoaDong, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(jScrollPane1)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addComponent(lblTongTien)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 279, Short.MAX_VALUE)
                .addComponent(btnHuy, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnLuuNhap, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(btnLuuPhieu, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap(12, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnThemDong)
                    .addComponent(btnXoaDong))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 252, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnHuy)
                    .addComponent(btnLuuNhap)
                    .addComponent(btnLuuPhieu)
                    .addComponent(lblTongTien)))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel2)
                            .addComponent(txtMaPhieu, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                            .addComponent(jLabel5)
                            .addComponent(cboNhaCungCap, 0, 190, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(cboKhuNhan, javax.swing.GroupLayout.Alignment.LEADING, 0, 190, Short.MAX_VALUE)
                                .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(txtNgayNhap, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE))
                            .addComponent(jLabel6))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jLabel4)
                            .addComponent(jLabel7)
                            .addComponent(txtGhiChu)
                            .addComponent(cboNguoiLap, 0, 190, Short.MAX_VALUE))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jLabel3)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtMaPhieu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtNgayNhap, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cboNguoiLap, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jLabel6)
                    .addComponent(jLabel7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cboKhuNhan, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtGhiChu, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cboNhaCungCap, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnThemDongActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnThemDongActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnThemDongActionPerformed

    private void btnLuuNhapActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnLuuNhapActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnLuuNhapActionPerformed

    private void btnHuyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHuyActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_btnHuyActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable TableChiTiet;
    private javax.swing.JButton btnHuy;
    private javax.swing.JButton btnLuuNhap;
    private javax.swing.JButton btnLuuPhieu;
    private javax.swing.JButton btnThemDong;
    private javax.swing.JButton btnXoaDong;
    private javax.swing.JComboBox<String> cboKhuNhan;
    private javax.swing.JComboBox<String> cboNguoiLap;
    private javax.swing.JComboBox<String> cboNhaCungCap;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel lblTongTien;
    private javax.swing.JTextField txtGhiChu;
    private javax.swing.JTextField txtMaPhieu;
    private javax.swing.JTextField txtNgayNhap;
    // End of variables declaration//GEN-END:variables
}
