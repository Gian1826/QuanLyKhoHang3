package Controller;

import Model.LapPhieuXuatDAO;
import Model.PhienDangNhap;
import View.LapPhieuXuatPanel;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JOptionPane;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

public class LapPhieuXuatController {
    private LapPhieuXuatPanel view;
    private LapPhieuXuatDAO dao;
    private boolean isUpdatingTable = false; // Cờ ngăn chặn lặp vô hạn khi code tự sửa table
    private boolean dangSua = false;         // true = đang sửa phiếu nháp, false = lập phiếu mới
    private Runnable onSaved;                // gọi sau khi lưu xong (để form danh sách tải lại)

    public LapPhieuXuatController(LapPhieuXuatPanel view) {
        this.view = view;
        this.dao = new LapPhieuXuatDAO();
        initController();
    }

    /** Form "Phiếu xuất đã lưu" gọi hàm này để biết khi nào lưu xong mà tải lại danh sách. */
    public void setOnSaved(Runnable onSaved) {
        this.onSaved = onSaved;
    }

    private void initController() {
        // Người lập: tự điền, không cho sửa
        if (PhienDangNhap.hoTen != null) {
            view.getTxtNguoiLap().setText(PhienDangNhap.hoTen);
        }
        view.getTxtNguoiLap().setEditable(true);
        view.getTxtMaPhieu().setEditable(true);
        view.getTxtTongTien().setEditable(false);

        // Nạp combobox Khu xuất từ SQL
        view.getCbKhuXuat().removeAllItems();
        List<String> dsKhu = dao.layDanhSachKhu();
        for (String khu : dsKhu) {
            view.getCbKhuXuat().addItem(khu);
        }

        resetForm();

        // Thêm dòng
        view.getBtnThemDong().addActionListener(e -> {
            DefaultTableModel model = (DefaultTableModel) view.getTableChiTiet().getModel();
            model.addRow(new Object[]{model.getRowCount() + 1, "", "", "", 0, 1, 0.0, 0.0});
        });

        // Xóa dòng
        view.getBtnXoaDong().addActionListener(e -> {
            if (view.getTableChiTiet().isEditing()) {
                view.getTableChiTiet().getCellEditor().stopCellEditing();
            }
            DefaultTableModel model = (DefaultTableModel) view.getTableChiTiet().getModel();
            int selectedRow = view.getTableChiTiet().getSelectedRow();
            if (selectedRow >= 0) {
                model.removeRow(selectedRow);
                capNhatSTTVaTongTien(model);
            } else {
                JOptionPane.showMessageDialog(view, "Vui lòng chọn dòng cần xóa!");
            }
        });

        // Tự tính khi sửa bảng
        DefaultTableModel tableModel = (DefaultTableModel) view.getTableChiTiet().getModel();
        tableModel.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                if (isUpdatingTable) return;

                int row = e.getFirstRow();
                int col = e.getColumn();
                if (row < 0 || col < 0) return;

                isUpdatingTable = true;
                try {
                    // Nhập Mã SP (cột 1) -> load Tên, ĐVT, Tồn, Đơn giá
                    if (col == 1) {
                        String maSP = String.valueOf(tableModel.getValueAt(row, 1)).trim();
                        if (!maSP.isEmpty()) {
                            Object[] info = dao.layThongTinSanPham(maSP);
                            if (info != null) {
                                tableModel.setValueAt(info[0], row, 2); // Tên SP
                                tableModel.setValueAt(info[1], row, 3); // ĐVT
                                tableModel.setValueAt(info[2], row, 4); // Tồn
                                tableModel.setValueAt(info[3], row, 6); // Đơn giá
                                tinhThanhTien(tableModel, row);
                            } else {
                                JOptionPane.showMessageDialog(view, "Mã sản phẩm không tồn tại!");
                            }
                        }
                    }
                    // Sửa SL xuất (5) hoặc Đơn giá (6) -> tính lại Thành tiền
                    else if (col == 5 || col == 6) {
                        tinhThanhTien(tableModel, row);
                    }
                } finally {
                    isUpdatingTable = false;
                }
            }
        });

        view.getBtnLuuNhap().addActionListener(e -> xuLyLuuPhieu(0));   // Nháp
        view.getBtnLuuPhieu().addActionListener(e -> xuLyLuuPhieu(1));  // Lưu thật

        view.getBtnHuy().addActionListener(e -> {
            int c = JOptionPane.showConfirmDialog(view,
                    dangSua ? "Bỏ sửa phiếu này? Thay đổi chưa lưu sẽ mất."
                            : "Xóa trắng form? Dữ liệu chưa lưu sẽ mất.",
                    "Xác nhận", JOptionPane.YES_NO_OPTION);
            if (c == JOptionPane.YES_OPTION) resetForm();
        });
    }

    // =====================================================================
    // SỬA PHIẾU NHÁP: form "Phiếu xuất đã lưu" gọi controller.napPhieuDeSua(maPhieu)
    // =====================================================================
    public boolean napPhieuDeSua(String maPhieu) {
        // DAO trả về {ngayXuat (dd/MM/yyyy), khachHang, tenKhu, ghiChu, trangThai}
        Object[] tt = dao.layThongTinPhieu(maPhieu);
        if (tt == null) {
            JOptionPane.showMessageDialog(view, "Không tìm thấy phiếu " + maPhieu);
            return false;
        }
        if (Integer.parseInt(tt[4].toString()) != 0) {
            JOptionPane.showMessageDialog(view, "Chỉ được sửa phiếu ở trạng thái Nháp!");
            return false;
        }

        dangSua = true;
        view.getTxtMaPhieu().setText(maPhieu);
        view.getTxtNgayXuat().setText(String.valueOf(tt[0]));
        view.getTxtKhachHang().setText(String.valueOf(tt[1]));
        view.getCbKhuXuat().setSelectedItem(tt[2]);
        view.getTxtGhiChu().setText(tt[3] == null ? "" : tt[3].toString());

        // DAO trả về mỗi dòng {maSP, tenSP, dvt, ton, slXuat, donGia}
        DefaultTableModel model = (DefaultTableModel) view.getTableChiTiet().getModel();
        isUpdatingTable = true;
        model.setRowCount(0);
        int stt = 1;
        for (Object[] ct : dao.layChiTietPhieu(maPhieu)) {
            double sl = Double.parseDouble(ct[4].toString());
            double gia = Double.parseDouble(ct[5].toString());
            model.addRow(new Object[]{stt++, ct[0], ct[1], ct[2], ct[3], ct[4], ct[5], sl * gia});
        }
        isUpdatingTable = false;
        capNhatSTTVaTongTien(model);
        return true;
    }

    private void tinhThanhTien(DefaultTableModel model, int row) {
        try {
            int slXuat = Integer.parseInt(model.getValueAt(row, 5).toString().trim());
            double donGia = Double.parseDouble(model.getValueAt(row, 6).toString().trim());
            model.setValueAt(slXuat * donGia, row, 7);
            capNhatSTTVaTongTien(model);
        } catch (Exception ex) {
            // Bỏ qua nếu đang nhập dở
        }
    }

    private void capNhatSTTVaTongTien(DefaultTableModel model) {
        boolean cu = isUpdatingTable;
        isUpdatingTable = true;
        try {
            double tongTien = 0;
            for (int i = 0; i < model.getRowCount(); i++) {
                model.setValueAt(i + 1, i, 0); // STT
                Object tt = model.getValueAt(i, 7);
                if (tt != null) {
                    try {
                        tongTien += Double.parseDouble(tt.toString());
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
            view.getTxtTongTien().setText(String.format("%,.0f", tongTien));
        } finally {
            isUpdatingTable = cu;
        }
    }

    /** trangThai: 0 = nháp, 1 = lưu thật */
    private boolean kiemTraBang(DefaultTableModel model, int trangThai) {
        if (view.getTableChiTiet().isEditing()) {
            view.getTableChiTiet().getCellEditor().stopCellEditing();
        }

        if (trangThai == 1) {
            if (view.getTxtKhachHang().getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(view, "Vui lòng nhập khách hàng / nơi nhận!");
                return false;
            }
            if (view.getCbKhuXuat().getSelectedItem() == null) {
                JOptionPane.showMessageDialog(view, "Vui lòng chọn khu xuất!");
                return false;
            }
        }

        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(view, "Vui lòng nhập ít nhất một sản phẩm!");
            return false;
        }

        Set<String> daCo = new HashSet<>(); // chặn trùng Mã SP
        for (int i = 0; i < model.getRowCount(); i++) {
            Object ma = model.getValueAt(i, 1);
            if (ma == null || ma.toString().trim().isEmpty()) {
                JOptionPane.showMessageDialog(view, "Dòng " + (i + 1) + ": chưa nhập mã sản phẩm!");
                return false;
            }
            String maSP = ma.toString().trim().toUpperCase();
            if (!daCo.add(maSP)) {
                JOptionPane.showMessageDialog(view,
                        "Dòng " + (i + 1) + ": mã " + maSP + " bị trùng. Hãy gộp vào một dòng!");
                return false;
            }
            try {
                int ton = Integer.parseInt(model.getValueAt(i, 4).toString().trim());
                int xuat = Integer.parseInt(model.getValueAt(i, 5).toString().trim());
                if (xuat <= 0) {
                    JOptionPane.showMessageDialog(view, "Dòng " + (i + 1) + ": số lượng xuất phải > 0!");
                    return false;
                }
                if (trangThai == 1 && xuat > ton) {
                    JOptionPane.showMessageDialog(view, "Dòng " + (i + 1) + ": số lượng xuất lớn hơn tồn kho!");
                    return false;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(view, "Dòng " + (i + 1) + ": số lượng không hợp lệ!");
                return false;
            }
        }
        return true;
    }

    private void xuLyLuuPhieu(int trangThai) {
        String maPhieu = view.getTxtMaPhieu().getText().trim();
        String ngayXuat = view.getTxtNgayXuat().getText().trim();
        String khachHang = view.getTxtKhachHang().getText().trim();
        String maKhu = view.getCbKhuXuat().getSelectedItem() != null
                ? view.getCbKhuXuat().getSelectedItem().toString() : "";
        String ghiChu = view.getTxtGhiChu().getText().trim();

        if (maPhieu.isEmpty() || ngayXuat.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Vui lòng nhập Mã phiếu và Ngày xuất!");
            return;
        }

        // Khách hàng: SQL lưu MaKH (KH01, KH02...), người dùng có thể gõ mã hoặc tên
        String maKH = dao.timMaKH(khachHang);
        if (maKH == null) {
            JOptionPane.showMessageDialog(view,
                    "Không tìm thấy khách hàng. Hãy nhập đúng mã (vd: KH01) hoặc tên khách hàng!");
            return;
        }

        // Đổi ngày dd/MM/yyyy -> yyyy-MM-dd cho SQL
        String ngaySQL;
        try {
            ngaySQL = LocalDate.parse(ngayXuat, DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(view, "Ngày xuất không hợp lệ (dd/MM/yyyy)!");
            return;
        }

        DefaultTableModel model = (DefaultTableModel) view.getTableChiTiet().getModel();
        if (!kiemTraBang(model, trangThai)) return;

        double tongTien = 0;
        for (int i = 0; i < model.getRowCount(); i++) {
            tongTien += Double.parseDouble(model.getValueAt(i, 7).toString());
        }

        boolean kq;
        if (dangSua) {
            kq = dao.capNhatPhieuXuat(maPhieu, ngaySQL, maKH, maKhu,
                    tongTien, ghiChu, trangThai, model);
        } else {
            kq = dao.luuPhieuXuat(maPhieu, ngaySQL, maKH, maKhu,
                    tongTien, ghiChu, trangThai, model);
        }

        if (kq) {
            JOptionPane.showMessageDialog(view,
                    trangThai == 1 ? "Lưu phiếu xuất thành công!" : "Lưu nháp thành công!");
            resetForm();
            if (onSaved != null) onSaved.run();
                } else {
            JOptionPane.showMessageDialog(view,
                    "Lỗi lưu phiếu!\n" + dao.loiCuoi);
        }
    }

    /** Đưa form về trạng thái lập phiếu mới. */
    private void resetForm() {
        dangSua = false;
        view.getTxtMaPhieu().setText(dao.layMaPhieuMoi());
        view.getTxtNgayXuat().setText(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        view.getTxtKhachHang().setText("");
        view.getTxtGhiChu().setText("");
        if (view.getCbKhuXuat().getItemCount() > 0) view.getCbKhuXuat().setSelectedIndex(0);
        view.getTxtTongTien().setText("0");

        boolean cu = isUpdatingTable;
        isUpdatingTable = true;
        ((DefaultTableModel) view.getTableChiTiet().getModel()).setRowCount(0);
        isUpdatingTable = cu;
    }
}