package Controller;

import Model.PhienDangNhap;
import Model.PhieuKiemKeDAO;
import View.PhieuKiemKePanel; // Đổi tên class View theo thực tế của bạn
import java.awt.event.ItemEvent;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

public class PhieuKiemKeController {
    private PhieuKiemKePanel view;
    private PhieuKiemKeDAO dao;
    private boolean isUpdatingTable = false;

    public PhieuKiemKeController(PhieuKiemKePanel view) {
        this.view = view;
        this.dao = new PhieuKiemKeDAO();
        initController();
    }

    private void initController() {
        // Tự động điền người kiểm kê
        if(PhienDangNhap.hoTen != null){
             view.getTxtNguoiKiemKe().setText(PhienDangNhap.hoTen);
        }
        view.getTxtNguoiKiemKe().setEditable(false);

        loadComboBoxKhu();

        // Sự kiện: Khi đổi Khu -> Load danh sách sản phẩm trong khu đó
        view.getCbKhuKiemKe().addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                String maKhu = e.getItem().toString();
                DefaultTableModel model = (DefaultTableModel) view.getTableKetQua().getModel();
                if (!maKhu.equals("— Chọn khu —")) {
                    dao.layDanhSachHangTrongKhu(maKhu, model);
                    capNhatSoMatHangLech(model);
                } else {
                    model.setRowCount(0);
                    view.getLblSoMatHangLech().setText("Số mặt hàng lệch: 0");
                }
            }
        });

        // Bắt sự kiện người dùng nhập số lượng thực tế -> Tính Chênh lệch
        DefaultTableModel tableModel = (DefaultTableModel) view.getTableKetQua().getModel();
        tableModel.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                if (isUpdatingTable) return;
                
                int row = e.getFirstRow();
                int col = e.getColumn();
                // Nếu thay đổi cột "SL thực tế" (index 5)
                if (row >= 0 && col == 5) {
                    isUpdatingTable = true;
                    try {
                        Object heThongObj = tableModel.getValueAt(row, 4);
                        Object thucTeObj = tableModel.getValueAt(row, 5);
                        
                        if (heThongObj != null && thucTeObj != null && !thucTeObj.toString().trim().isEmpty()) {
                            int slHeThong = Integer.parseInt(heThongObj.toString());
                            int slThucTe = Integer.parseInt(thucTeObj.toString().trim());
                            int chenhLech = slThucTe - slHeThong;
                            
                            tableModel.setValueAt(chenhLech, row, 6); // Set cột Chênh lệch
                        } else {
                            tableModel.setValueAt(0, row, 6);
                        }
                        capNhatSoMatHangLech(tableModel);
                    } catch (NumberFormatException ex) {
                        // Bỏ qua nếu nhập chưa xong hoặc nhập chữ
                    } finally {
                        isUpdatingTable = false;
                    }
                }
            }
        });

        // Xử lý nút Hủy
        view.getBtnHuy().addActionListener(e -> resetForm());

        // Xử lý nút Lưu Nháp (Trạng thái = 0)
        view.getBtnLuuNhap().addActionListener(e -> thucHienLuu(0));

        // Xử lý nút Hoàn tất kiểm kê (Trạng thái = 1)
        view.getBtnHoanTat().addActionListener(e -> thucHienLuu(1));
    }

    private void loadComboBoxKhu() {
        view.getCbKhuKiemKe().removeAllItems();
        view.getCbKhuKiemKe().addItem("— Chọn khu —");
        List<String> listKhu = dao.layDanhSachKhu();
        for (String khu : listKhu) {
            view.getCbKhuKiemKe().addItem(khu);
        }
    }

    // Đếm số lượng dòng có chênh lệch khác 0
    private void capNhatSoMatHangLech(DefaultTableModel model) {
        int count = 0;
        for (int i = 0; i < model.getRowCount(); i++) {
            Object lechObj = model.getValueAt(i, 6);
            if (lechObj != null) {
                try {
                    int lech = Integer.parseInt(lechObj.toString());
                    if (lech != 0) count++;
                } catch (NumberFormatException ignored) {}
            }
        }
        view.getLblSoMatHangLech().setText("Số mặt hàng lệch: " + count);
    }

    private void resetForm() {
        view.getTxtMaPhieu().setText("");
        view.getTxtNgayKiemKe().setText("");
        view.getTxtGhiChu().setText("");
        view.getCbKhuKiemKe().setSelectedIndex(0); // Tự động clear bảng nhờ sự kiện Listener
    }

    private void thucHienLuu(int trangThai) {
        String maPhieu = view.getTxtMaPhieu().getText().trim();
        String ngayKiemKe = view.getTxtNgayKiemKe().getText().trim();
        String ghiChu = view.getTxtGhiChu().getText().trim();
        
        if (maPhieu.isEmpty() || ngayKiemKe.isEmpty() || view.getCbKhuKiemKe().getSelectedIndex() <= 0) {
            JOptionPane.showMessageDialog(view, "Vui lòng nhập Mã phiếu, Ngày kiểm kê và chọn Khu!");
            return;
        }

        String maKhu = view.getCbKhuKiemKe().getSelectedItem().toString();
        DefaultTableModel model = (DefaultTableModel) view.getTableKetQua().getModel();

        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(view, "Khu vực này không có hàng hóa để kiểm kê!");
            return;
        }

        // Validate dữ liệu nhập
        for (int i = 0; i < model.getRowCount(); i++) {
            Object thucTeObj = model.getValueAt(i, 5);
            if (thucTeObj != null && !thucTeObj.toString().trim().isEmpty()) {
                try {
                    int slThucTe = Integer.parseInt(thucTeObj.toString().trim());
                    if (slThucTe < 0) {
                        JOptionPane.showMessageDialog(view, "Dòng " + (i+1) + ": Số lượng thực tế không được âm!");
                        return;
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(view, "Dòng " + (i+1) + ": Số lượng thực tế phải là số!");
                    return;
                }
            } else if (trangThai == 1) { // Nếu hoàn tất thì bắt buộc phải nhập hết
                 JOptionPane.showMessageDialog(view, "Dòng " + (i+1) + ": Vui lòng nhập số lượng thực tế trước khi hoàn tất!");
                 return;
            }
        }

        int maTaiKhoan = 1; // Giả sử ID tài khoản từ phiên đăng nhập

        String actionMsg = trangThai == 1 ? "Hoàn tất kiểm kê sẽ cập nhật lại số lượng tồn kho theo số thực tế.\nBạn có chắc chắn?" : "Bạn có muốn lưu nháp phiếu này?";
        int confirm = JOptionPane.showConfirmDialog(view, actionMsg, "Xác nhận", JOptionPane.YES_NO_OPTION);
        
        if (confirm == JOptionPane.YES_OPTION) {
            if (dao.luuPhieuKiemKe(maPhieu, ngayKiemKe, maKhu, maTaiKhoan, ghiChu, trangThai, model)) {
                String successMsg = trangThai == 1 ? "Đã hoàn tất kiểm kê và cập nhật tồn kho!" : "Lưu nháp thành công!";
                JOptionPane.showMessageDialog(view, successMsg);
                resetForm();
            } else {
                JOptionPane.showMessageDialog(view, "Lỗi khi lưu dữ liệu. Vui lòng kiểm tra lại (có thể trùng mã phiếu)!");
            }
        }
    }
}