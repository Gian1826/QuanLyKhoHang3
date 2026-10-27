package Controller;

import Model.PhienDangNhap;
import Model.PhieuHoanTraDAO;
import View.PhieuHoanTraPanel; // Đảm bảo đúng tên View
import java.awt.event.ItemEvent;
import java.util.List;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

public class PhieuHoanTraController {
    private PhieuHoanTraPanel view;
    private PhieuHoanTraDAO dao;

    public PhieuHoanTraController(PhieuHoanTraPanel view) {
        this.view = view;
        this.dao = new PhieuHoanTraDAO();
        initController();
    }

    private void initController() {
        // Khởi tạo thông tin tĩnh
        if(PhienDangNhap.hoTen != null) {
            view.getTxtNguoiLap().setText(PhienDangNhap.hoTen);
            view.getTxtNguoiLap().setEditable(false);
        }
        
        loadComboBoxPhieuXuat();
        setupTableTinhTrang(); // Cấu hình combobox cho cột Tình trạng

        // Sự kiện khi chọn Phiếu Xuất Gốc
        view.getCbPhieuXuatGoc().addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                String maPhieuXuat = e.getItem().toString();
                if (!maPhieuXuat.equals("— Chọn phiếu xuất —")) {
                    DefaultTableModel model = (DefaultTableModel) view.getTableChiTiet().getModel();
                    dao.layChiTietPhieuXuat(maPhieuXuat, model);
                } else {
                    ((DefaultTableModel) view.getTableChiTiet().getModel()).setRowCount(0);
                }
            }
        });

        // Xử lý nút Lưu Nháp (Trạng thái = 0)
        view.getBtnLuuNhap().addActionListener(e -> xuLyLuu(0));

        // Xử lý nút Lưu Phiếu (Trạng thái = 1)
        view.getBtnLuuPhieu().addActionListener(e -> xuLyLuu(1));

        // Xử lý nút Hủy
        view.getBtnHuy().addActionListener(e -> xoaTrangForm());
    }

    private void loadComboBoxPhieuXuat() {
        view.getCbPhieuXuatGoc().removeAllItems();
        view.getCbPhieuXuatGoc().addItem("— Chọn phiếu xuất —");
        List<String> dsPhieu = dao.layDanhSachPhieuXuat();
        for (String phieu : dsPhieu) {
            view.getCbPhieuXuatGoc().addItem(phieu);
        }
    }

    // Biến cột Tình trạng trong JTable thành JComboBox
    private void setupTableTinhTrang() {
        JComboBox<String> cbTinhTrang = new JComboBox<>(new String[]{"Nguyên vẹn", "Lỗi"});
        TableColumn colTinhTrang = view.getTableChiTiet().getColumnModel().getColumn(5); // Cột thứ 6 (index 5)
        colTinhTrang.setCellEditor(new DefaultCellEditor(cbTinhTrang));
    }

    private void xuLyLuu(int trangThai) {
        String maPhieu = view.getTxtMaPhieu().getText().trim();
        String ngayHoanTra = view.getTxtNgayHoanTra().getText().trim();
        String lyDo = view.getTxtLyDoHoanTra().getText().trim();
        int idxCb = view.getCbPhieuXuatGoc().getSelectedIndex();

        if (maPhieu.isEmpty() || ngayHoanTra.isEmpty() || idxCb <= 0) {
            JOptionPane.showMessageDialog(view, "Vui lòng nhập Mã phiếu, Ngày trả và chọn Phiếu xuất gốc!");
            return;
        }

        String maPhieuXuatGoc = view.getCbPhieuXuatGoc().getSelectedItem().toString();
        DefaultTableModel model = (DefaultTableModel) view.getTableChiTiet().getModel();
        
        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(view, "Không có dữ liệu mặt hàng!");
            return;
        }

        boolean coHangHoanTra = false;
        // Validate dữ liệu số lượng hoàn trả
        for (int i = 0; i < model.getRowCount(); i++) {
            try {
                int slDeXuat = Integer.parseInt(model.getValueAt(i, 3).toString());
                int slHoan = Integer.parseInt(model.getValueAt(i, 4).toString());

                if (slHoan < 0) {
                    JOptionPane.showMessageDialog(view, "Dòng " + (i+1) + ": Số lượng hoàn trả không được âm!");
                    return;
                }
                if (slHoan > slDeXuat) {
                    JOptionPane.showMessageDialog(view, "Dòng " + (i+1) + ": Số lượng hoàn trả lớn hơn số lượng xuất ban đầu!");
                    return;
                }
                if (slHoan > 0) {
                    coHangHoanTra = true;
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(view, "Dòng " + (i+1) + ": Số lượng hoàn trả phải là số!");
                return;
            }
        }

        if (!coHangHoanTra) {
            JOptionPane.showMessageDialog(view, "Vui lòng nhập số lượng hoàn trả lớn hơn 0 cho ít nhất một mặt hàng!");
            return;
        }

        int maTaiKhoan = 1; // Giả sử ID tài khoản từ phiên đăng nhập

        if (dao.luuPhieuHoanTra(maPhieu, ngayHoanTra, maPhieuXuatGoc, maTaiKhoan, lyDo, trangThai, model)) {
            String msg = trangThai == 1 ? "Lưu phiếu hoàn trả thành công!" : "Lưu nháp thành công!";
            JOptionPane.showMessageDialog(view, msg);
            xoaTrangForm();
        } else {
            JOptionPane.showMessageDialog(view, "Lỗi lưu dữ liệu. Vui lòng kiểm tra lại (có thể trùng Mã phiếu)!");
        }
    }

    private void xoaTrangForm() {
        view.getTxtMaPhieu().setText("");
        view.getTxtNgayHoanTra().setText("");
        view.getTxtLyDoHoanTra().setText("");
        view.getCbPhieuXuatGoc().setSelectedIndex(0);
        ((DefaultTableModel) view.getTableChiTiet().getModel()).setRowCount(0);
    }
}