package View;
import Model.KetNoiDB;
import View.PieChartPanel;
import View.BarChartPanel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.TitledBorder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import View.*;
public class TrangChuPanel extends javax.swing.JPanel {
    private BarChartPanel bar = new BarChartPanel();
    private PieChartPanel pie = new PieChartPanel();
    private DefaultTableModel modelCanhBao;
    private DefaultTableModel modelHoatDong;
    private JTable tblCanhBao, tblHoatDong;
    private Timer timerLamMoi;
    public TrangChuPanel() {
        initComponents();
        BtnNhapKho.addActionListener(e -> di("lapphieu"));
        BtnLuuTru.addActionListener(e -> di("luutru"));
        BtnXuatKho.addActionListener(e -> di("xuatkho"));
        BtnKiemKe.addActionListener(e -> di("kiemke"));
        BtnBaoCao.addActionListener(e -> di("baocao"));
        DateTimeFormatter f = DateTimeFormatter.ofPattern("dd/MM/yyyy  HH:mm:ss");
        LblTime.setText(LocalDateTime.now().format(f));
        new javax.swing.Timer(1000, e -> LblTime.setText(LocalDateTime.now().format(f))).start();

        BieuDoNhapXuat.add(bar, java.awt.BorderLayout.CENTER);
        DanhMucTonKho.add(pie, java.awt.BorderLayout.CENTER);

        BieuDoNhapXuat.revalidate();
        DanhMucTonKho.revalidate();
        taoBangCanhBao();
        taoBangHoatDong();
        khoiDongLamMoi();
    }
    private void caiDatBang(JTable tbl) {
        tbl.setRowHeight(26);
        tbl.setShowGrid(true);
        tbl.getTableHeader().setReorderingAllowed(false);
        ((DefaultTableCellRenderer) tbl.getTableHeader().getDefaultRenderer())
                .setHorizontalAlignment(SwingConstants.CENTER);
    }
    private void taoBangCanhBao() {
        modelCanhBao = new DefaultTableModel(
                new String[]{"Mã SP", "Tên sản phẩm", "Tồn", "Tối thiểu", "Trạng thái"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        tblCanhBao = new JTable(modelCanhBao);
        caiDatBang(tblCanhBao);

        // Tô màu hàng theo trạng thái, căn giữa cột số và trạng thái
        tblCanhBao.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, focus, row, col);
                String tt = String.valueOf(t.getValueAt(row, 4));
                if (tt.equals("Hết hàng")) {
                    setBackground(new Color(250, 219, 216));
                    setForeground(new Color(192, 57, 43));
                } else {
                    setBackground(new Color(253, 235, 208));
                    setForeground(new Color(175, 96, 26));
                }
                setHorizontalAlignment(col >= 2 ? SwingConstants.CENTER : SwingConstants.LEFT);
                return this;
            }
        });

        CanhBaoTonKho.setLayout(new BorderLayout());
        CanhBaoTonKho.add(new JScrollPane(tblCanhBao), BorderLayout.CENTER);
    }
    private void taoBangHoatDong() {
        modelHoatDong = new DefaultTableModel(
                new String[]{"Mã phiếu", "Loại", "Thời gian", "Người lập"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        tblHoatDong = new JTable(modelHoatDong);
        caiDatBang(tblHoatDong);

        // Chữ "Nhập" màu xanh lá, "Xuất" màu cam, căn giữa cột Loại
        tblHoatDong.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v,
                    boolean sel, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(t, v, sel, focus, row, col);
                setBackground(Color.WHITE);
                setForeground(Color.BLACK);
                setHorizontalAlignment(SwingConstants.LEFT);
                if (col == 1) {
                    setHorizontalAlignment(SwingConstants.CENTER);
                    if ("Nhập".equals(v)) setForeground(new Color(39, 174, 96));
                    else if ("Xuất".equals(v)) setForeground(new Color(230, 126, 34));
                }
                return this;
            }
        });

        HoatDong.setLayout(new BorderLayout());
        HoatDong.add(new JScrollPane(tblHoatDong), BorderLayout.CENTER);
    }
    public void themCanhBao(String ma, String ten, int ton, int toiThieu) {
        if (ton >= toiThieu) return;
        String trangThai = (ton == 0) ? "Hết hàng" : "Sắp hết";
        modelCanhBao.addRow(new Object[]{ma, ten, ton, toiThieu, trangThai});
    }
    public void themHoatDong(String maPhieu, String loai, String thoiGian, String nguoiLap) {
        modelHoatDong.insertRow(0, new Object[]{maPhieu, loai, thoiGian, nguoiLap});
        while (modelHoatDong.getRowCount() > 5) {
            modelHoatDong.removeRow(modelHoatDong.getRowCount() - 1);
        }
    }
    public void xoaBangCanhBao() {
        modelCanhBao.setRowCount(0);
    }
    public void xoaBangHoatDong() {
        modelHoatDong.setRowCount(0);
    }
    private void khoiDongLamMoi() {
    napDuLieu();
    timerLamMoi = new Timer(5000, e -> napDuLieu());
    timerLamMoi.start();
    BtnLamMoi.addActionListener(e -> napDuLieu());
    }
    private void napDuLieu() {
        napTheSoLieu();
        // phần bảng và biểu đồ sẽ thêm sau
    }
    private void napTheSoLieu() {
        new SwingWorker<Object[], Void>() {
            @Override
            protected Object[] doInBackground() throws Exception {
                Connection con = KetNoiDB.getConnection();   // kết nối dùng chung, KHÔNG đóng

                    int soMatHang = layInt(con,
                        "SELECT COUNT(*) FROM SanPham WHERE TrangThai = 1");

                    double giaTriTon = layDouble(con,
                        "SELECT COALESCE(SUM(t.SoLuongTon * s.GiaNhap), 0) " +
                        "FROM TonKho t JOIN SanPham s ON s.MaSanPham = t.MaSanPham");

                    int soNhap = layInt(con,
                        "SELECT COUNT(*) FROM PhieuNhap " +
                        "WHERE TrangThai <> 2 AND CAST(NgayNhap AS DATE) = CAST(GETDATE() AS DATE)");

                    int soXuat = layInt(con,
                        "SELECT COUNT(*) FROM PhieuXuat " +
                        "WHERE TrangThai <> 2 AND CAST(NgayXuat AS DATE) = CAST(GETDATE() AS DATE)");

                    int soSapHet = layInt(con,
                        "SELECT COUNT(*) FROM SanPham s WHERE s.TrangThai = 1 AND " +
                        "COALESCE((SELECT SUM(t.SoLuongTon) FROM TonKho t " +
                        "WHERE t.MaSanPham = s.MaSanPham), 0) < s.MucTonToiThieu");

                    return new Object[]{soMatHang, giaTriTon, soNhap, soXuat, soSapHet};
            }

            @Override
            protected void done() {
                try {
                    Object[] r = get();
                    SLMatHang.setText(String.valueOf(r[0]));
                    SLTonKho.setText(dinhDangTien((double) r[1]));
                    SLNhapXuat.setText(r[2] + " / " + r[3]);
                    SLSapHet.setText(String.valueOf(r[4]));
                } catch (Exception ex) {
                    ex.printStackTrace();
                    SLMatHang.setText("--");
                    SLTonKho.setText("--");
                    SLNhapXuat.setText("-- / --");
                    SLSapHet.setText("--");
                }
            }
        }.execute();
    }
    private int layInt(Connection con, String sql) throws Exception {
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }
    private double layDouble(Connection con, String sql) throws Exception {
        try (PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getDouble(1) : 0;
        }
    }
    private String dinhDangTien(double v) {
        DecimalFormat df = new DecimalFormat("#,##0.##");
        if (v >= 1_000_000_000) return df.format(v / 1_000_000_000) + " tỷ";
        if (v >= 1_000_000)     return df.format(v / 1_000_000) + " triệu";
        return new DecimalFormat("#,##0").format(v) + " đ";
    }
     private java.util.function.Consumer<String> dieuHuong;

    public void setDieuHuong(java.util.function.Consumer<String> dieuHuong) {
        this.dieuHuong = dieuHuong;
    }

    private void di(String trang) {
        if (dieuHuong != null) dieuHuong.accept(trang);
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        PnlTop = new javax.swing.JPanel();
        PnlChao = new javax.swing.JPanel();
        LblChao = new javax.swing.JLabel();
        LblTime = new javax.swing.JLabel();
        PnlThe = new javax.swing.JPanel();
        PnlTheMatHang = new javax.swing.JPanel();
        TongMatHang = new javax.swing.JLabel();
        SLMatHang = new javax.swing.JLabel();
        PnlTheGiaTri = new javax.swing.JPanel();
        TonKho = new javax.swing.JLabel();
        SLTonKho = new javax.swing.JLabel();
        PnlThePhieu = new javax.swing.JPanel();
        NhapXuat = new javax.swing.JLabel();
        SLNhapXuat = new javax.swing.JLabel();
        PnlTheSapHet = new javax.swing.JPanel();
        MatHang = new javax.swing.JLabel();
        SLSapHet = new javax.swing.JLabel();
        PnlCenter = new javax.swing.JPanel();
        BieuDoNhapXuat = new javax.swing.JPanel();
        DanhMucTonKho = new javax.swing.JPanel();
        CanhBaoTonKho = new javax.swing.JPanel();
        HoatDong = new javax.swing.JPanel();
        PnlThaoTac = new javax.swing.JPanel();
        BtnNhapKho = new javax.swing.JButton();
        BtnLuuTru = new javax.swing.JButton();
        BtnXuatKho = new javax.swing.JButton();
        BtnKiemKe = new javax.swing.JButton();
        BtnBaoCao = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JSeparator();
        BtnLamMoi = new javax.swing.JButton();

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        setLayout(new java.awt.BorderLayout());

        PnlTop.setPreferredSize(new java.awt.Dimension(100, 150));
        PnlTop.setLayout(new java.awt.BorderLayout());

        PnlChao.setPreferredSize(new java.awt.Dimension(1200, 40));
        PnlChao.setLayout(new java.awt.BorderLayout());

        LblChao.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        LblChao.setText("Xin chào, Guest");
        PnlChao.add(LblChao, java.awt.BorderLayout.LINE_START);

        LblTime.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        LblTime.setText("19/10/2026 14:44:27");
        PnlChao.add(LblTime, java.awt.BorderLayout.LINE_END);

        PnlTop.add(PnlChao, java.awt.BorderLayout.NORTH);

        PnlThe.setPreferredSize(new java.awt.Dimension(1200, 80));
        PnlThe.setLayout(new java.awt.GridLayout(1, 0));

        PnlTheMatHang.setBackground(new java.awt.Color(46, 134, 193));
        PnlTheMatHang.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 16, 10, 16));
        PnlTheMatHang.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        PnlTheMatHang.setLayout(new java.awt.BorderLayout());

        TongMatHang.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        TongMatHang.setForeground(new java.awt.Color(255, 255, 255));
        TongMatHang.setText("Tổng số mặt hàng");
        TongMatHang.setPreferredSize(new java.awt.Dimension(38, 25));
        PnlTheMatHang.add(TongMatHang, java.awt.BorderLayout.PAGE_END);

        SLMatHang.setFont(new java.awt.Font("Segoe UI", 1, 30)); // NOI18N
        SLMatHang.setForeground(new java.awt.Color(255, 255, 255));
        PnlTheMatHang.add(SLMatHang, java.awt.BorderLayout.CENTER);

        PnlThe.add(PnlTheMatHang);

        PnlTheGiaTri.setBackground(new java.awt.Color(39, 174, 96));
        PnlTheGiaTri.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 16, 10, 16));
        PnlTheGiaTri.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        PnlTheGiaTri.setLayout(new java.awt.BorderLayout());

        TonKho.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        TonKho.setForeground(new java.awt.Color(255, 255, 255));
        TonKho.setText("Giá trị tồn kho");
        TonKho.setPreferredSize(new java.awt.Dimension(38, 25));
        PnlTheGiaTri.add(TonKho, java.awt.BorderLayout.PAGE_END);

        SLTonKho.setFont(new java.awt.Font("Segoe UI", 1, 30)); // NOI18N
        SLTonKho.setForeground(new java.awt.Color(255, 255, 255));
        PnlTheGiaTri.add(SLTonKho, java.awt.BorderLayout.CENTER);

        PnlThe.add(PnlTheGiaTri);

        PnlThePhieu.setBackground(new java.awt.Color(230, 126, 34));
        PnlThePhieu.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 16, 10, 16));
        PnlThePhieu.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        PnlThePhieu.setLayout(new java.awt.BorderLayout());

        NhapXuat.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        NhapXuat.setForeground(new java.awt.Color(255, 255, 255));
        NhapXuat.setText("Phiếu nhập/xuất hôm nay");
        PnlThePhieu.add(NhapXuat, java.awt.BorderLayout.PAGE_END);

        SLNhapXuat.setFont(new java.awt.Font("Segoe UI", 1, 30)); // NOI18N
        SLNhapXuat.setForeground(new java.awt.Color(255, 255, 255));
        PnlThePhieu.add(SLNhapXuat, java.awt.BorderLayout.CENTER);

        PnlThe.add(PnlThePhieu);

        PnlTheSapHet.setBackground(new java.awt.Color(192, 57, 43));
        PnlTheSapHet.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 16, 10, 16));
        PnlTheSapHet.setLayout(new java.awt.BorderLayout());

        MatHang.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        MatHang.setForeground(new java.awt.Color(255, 255, 255));
        MatHang.setText("Mặt hàng sắp hết");
        PnlTheSapHet.add(MatHang, java.awt.BorderLayout.PAGE_END);

        SLSapHet.setFont(new java.awt.Font("Segoe UI", 1, 30)); // NOI18N
        SLSapHet.setForeground(new java.awt.Color(255, 255, 255));
        PnlTheSapHet.add(SLSapHet, java.awt.BorderLayout.CENTER);

        PnlThe.add(PnlTheSapHet);

        PnlTop.add(PnlThe, java.awt.BorderLayout.CENTER);

        add(PnlTop, java.awt.BorderLayout.PAGE_START);

        PnlCenter.setPreferredSize(new java.awt.Dimension(1200, 500));
        PnlCenter.setLayout(new java.awt.GridLayout(2, 2, 10, 10));

        BieuDoNhapXuat.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Nhập/xuất 7 ngày gần nhất", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 1, 12))); // NOI18N
        BieuDoNhapXuat.setLayout(new java.awt.BorderLayout());
        PnlCenter.add(BieuDoNhapXuat);

        DanhMucTonKho.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Tồn kho theo danh mục", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 1, 12))); // NOI18N
        DanhMucTonKho.setLayout(new java.awt.BorderLayout());
        PnlCenter.add(DanhMucTonKho);

        CanhBaoTonKho.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Cảnh báo tồn kho thấp", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 1, 12))); // NOI18N
        CanhBaoTonKho.setLayout(new java.awt.BorderLayout());
        PnlCenter.add(CanhBaoTonKho);

        HoatDong.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Hoạt động gần đây", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 1, 12))); // NOI18N
        HoatDong.setLayout(new java.awt.BorderLayout());
        PnlCenter.add(HoatDong);

        add(PnlCenter, java.awt.BorderLayout.CENTER);

        PnlThaoTac.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Thao tác nhanh", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 1, 12))); // NOI18N
        PnlThaoTac.setPreferredSize(new java.awt.Dimension(1200, 70));
        PnlThaoTac.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 12, 8));

        BtnNhapKho.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        BtnNhapKho.setText("Nhập kho");
        BtnNhapKho.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        BtnNhapKho.setFocusPainted(false);
        BtnNhapKho.addActionListener(this::BtnNhapKhoActionPerformed);
        PnlThaoTac.add(BtnNhapKho);

        BtnLuuTru.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        BtnLuuTru.setText("Lưu trữ");
        BtnLuuTru.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        BtnLuuTru.setFocusPainted(false);
        BtnLuuTru.setPreferredSize(new java.awt.Dimension(100, 25));
        PnlThaoTac.add(BtnLuuTru);

        BtnXuatKho.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        BtnXuatKho.setText("Xuất kho");
        BtnXuatKho.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        BtnXuatKho.setFocusPainted(false);
        BtnXuatKho.setPreferredSize(new java.awt.Dimension(100, 25));
        PnlThaoTac.add(BtnXuatKho);

        BtnKiemKe.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        BtnKiemKe.setText("Kiểm kê");
        BtnKiemKe.setFocusPainted(false);
        BtnKiemKe.setPreferredSize(new java.awt.Dimension(100, 25));
        PnlThaoTac.add(BtnKiemKe);

        BtnBaoCao.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        BtnBaoCao.setText("Báo cáo");
        BtnBaoCao.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        BtnBaoCao.setFocusPainted(false);
        BtnBaoCao.setPreferredSize(new java.awt.Dimension(100, 25));
        PnlThaoTac.add(BtnBaoCao);

        jSeparator1.setPreferredSize(new java.awt.Dimension(30, 10));
        PnlThaoTac.add(jSeparator1);

        BtnLamMoi.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        BtnLamMoi.setText("Làm mới");
        BtnLamMoi.setPreferredSize(new java.awt.Dimension(100, 25));
        PnlThaoTac.add(BtnLamMoi);

        add(PnlThaoTac, java.awt.BorderLayout.PAGE_END);
    }// </editor-fold>//GEN-END:initComponents

    private void BtnNhapKhoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BtnNhapKhoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_BtnNhapKhoActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel BieuDoNhapXuat;
    private javax.swing.JButton BtnBaoCao;
    private javax.swing.JButton BtnKiemKe;
    private javax.swing.JButton BtnLamMoi;
    private javax.swing.JButton BtnLuuTru;
    private javax.swing.JButton BtnNhapKho;
    private javax.swing.JButton BtnXuatKho;
    private javax.swing.JPanel CanhBaoTonKho;
    private javax.swing.JPanel DanhMucTonKho;
    private javax.swing.JPanel HoatDong;
    private javax.swing.JLabel LblChao;
    private javax.swing.JLabel LblTime;
    private javax.swing.JLabel MatHang;
    private javax.swing.JLabel NhapXuat;
    private javax.swing.JPanel PnlCenter;
    private javax.swing.JPanel PnlChao;
    private javax.swing.JPanel PnlThaoTac;
    private javax.swing.JPanel PnlThe;
    private javax.swing.JPanel PnlTheGiaTri;
    private javax.swing.JPanel PnlTheMatHang;
    private javax.swing.JPanel PnlThePhieu;
    private javax.swing.JPanel PnlTheSapHet;
    private javax.swing.JPanel PnlTop;
    private javax.swing.JLabel SLMatHang;
    private javax.swing.JLabel SLNhapXuat;
    private javax.swing.JLabel SLSapHet;
    private javax.swing.JLabel SLTonKho;
    private javax.swing.JLabel TonKho;
    private javax.swing.JLabel TongMatHang;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator1;
    // End of variables declaration//GEN-END:variables
}
