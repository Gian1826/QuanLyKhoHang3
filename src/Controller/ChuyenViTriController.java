package Controller;

import Model.ChuyenViTriDAO;
import Model.PhienDangNhap;
import View.ChuyenViTriPanel;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

public class ChuyenViTriController {
    private ChuyenViTriPanel view;
    private ChuyenViTriDAO dao;

    public ChuyenViTriController(ChuyenViTriPanel view) {
        this.view = view;
        this.dao = new ChuyenViTriDAO();
        initController();
    }

    private void initController() {
        loadViTriDich();
        
        // Điền tự động tên người thực hiện
        if(PhienDangNhap.hoTen != null){
             view.getTxtNguoiThucHien().setText(PhienDangNhap.hoTen);
        }
        view.getTxtNguoiThucHien().setEditable(false);

        // Sự kiện khi người dùng nhập xong Mã SP và click ra ngoài (hoặc ấn Enter)
        view.getTxtMaSanPham().addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                String maSP = view.getTxtMaSanPham().getText().trim();
                if (!maSP.isEmpty()) {
                    loadViTriNguon(maSP);
                }
            }
        });
        
        // Có thể bắt thêm sự kiện phím Enter cho ô Mã SP
        view.getTxtMaSanPham().addActionListener(e -> {
             String maSP = view.getTxtMaSanPham().getText().trim();
             if (!maSP.isEmpty()) {
                 loadViTriNguon(maSP);
             }
        });

        // Sự kiện khi chọn Vị trí nguồn -> Hiển thị chi tiết SP lên bảng
        view.getCbViTriNguon().addItemListener(e -> {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                String viTriNguon = e.getItem().toString();
                String maSP = view.getTxtMaSanPham().getText().trim();
                if (!viTriNguon.contains("Chọn vị trí") && !maSP.isEmpty()) {
                    DefaultTableModel model = (DefaultTableModel) view.getTableChiTiet().getModel();
                    dao.timKiemSanPham(maSP, viTriNguon, model);
                }
            }
        });

        // Nút Hủy
        view.getBtnHuy().addActionListener(e -> resetForm());

        // Nút Chuyển vị trí
        view.getBtnChuyenViTri().addActionListener(e -> xuLyChuyenViTri());
    }

    private void loadViTriNguon(String maSP) {
        view.getCbViTriNguon().removeAllItems();
        view.getCbViTriNguon().addItem("— Chọn vị trí —");
        List<String> listNguon = dao.layViTriNguonCuaSanPham(maSP);
        if (listNguon.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Sản phẩm này không có tồn kho ở bất kỳ vị trí nào!");
            return;
        }
        for (String vt : listNguon) {
            view.getCbViTriNguon().addItem(vt);
        }
    }

    private void loadViTriDich() {
        view.getCbViTriDich().removeAllItems();
        view.getCbViTriDich().addItem("— Chọn vị trí —");
        List<String> listDich = dao.layTatCaViTri();
        for (String vt : listDich) {
            view.getCbViTriDich().addItem(vt);
        }
    }

    private void resetForm() {
        view.getTxtMaSanPham().setText("");
        view.getCbViTriNguon().removeAllItems();
        view.getCbViTriNguon().addItem("— Chọn vị trí —");
        view.getCbViTriDich().setSelectedIndex(0);
        view.getTxtSoLuongChuyen().setText("");
        view.getTxtLyDo().setText("");
        ((DefaultTableModel) view.getTableChiTiet().getModel()).setRowCount(0);
    }

    private void xuLyChuyenViTri() {
        String maSP = view.getTxtMaSanPham().getText().trim();
        String slStr = view.getTxtSoLuongChuyen().getText().trim();
        String lyDo = view.getTxtLyDo().getText().trim();
        
        if (maSP.isEmpty() || view.getCbViTriNguon().getSelectedIndex() <= 0 || view.getCbViTriDich().getSelectedIndex() <= 0) {
            JOptionPane.showMessageDialog(view, "Vui lòng nhập mã SP và chọn đầy đủ vị trí nguồn, đích!");
            return;
        }

        String viTriNguon = view.getCbViTriNguon().getSelectedItem().toString();
        String viTriDich = view.getCbViTriDich().getSelectedItem().toString();

        if (viTriNguon.equals(viTriDich)) {
            JOptionPane.showMessageDialog(view, "Vị trí nguồn và vị trí đích phải khác nhau!");
            return;
        }

        int slChuyen;
        try {
            slChuyen = Integer.parseInt(slStr);
            if (slChuyen <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(view, "Số lượng chuyển phải là số nguyên dương!");
            return;
        }

        // Lấy số lượng tồn từ bảng giao diện (đã được load khi chọn vị trí nguồn)
        DefaultTableModel model = (DefaultTableModel) view.getTableChiTiet().getModel();
        if (model.getRowCount() == 0) return;
        
        int tonHienTai = Integer.parseInt(model.getValueAt(0, 3).toString());
        if (slChuyen > tonHienTai) {
            JOptionPane.showMessageDialog(view, "Số lượng chuyển (" + slChuyen + ") lớn hơn số lượng tồn hiện tại (" + tonHienTai + ")!");
            return;
        }

        // Thực hiện chuyển trong DAO
        // Giả sử lấy MaTaiKhoan từ class tĩnh PhienDangNhap (cần đổi theo logic đăng nhập của bạn)
        int maTaiKhoan = 1; 

        int confirm = JOptionPane.showConfirmDialog(view, "Bạn có chắc chắn muốn chuyển " + slChuyen + " sản phẩm " + maSP + " từ " + viTriNguon + " sang " + viTriDich + " không?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (dao.thucHienChuyenViTri(maSP, viTriNguon, viTriDich, slChuyen, lyDo, maTaiKhoan)) {
                JOptionPane.showMessageDialog(view, "Chuyển vị trí thành công!");
                // Cập nhật lại số lượng chuyển hiển thị trên bảng
                model.setValueAt(slChuyen, 0, 4);
                // Load lại combo nguồn để cập nhật trạng thái mới nhất
                loadViTriNguon(maSP); 
                view.getTxtSoLuongChuyen().setText("");
            } else {
                JOptionPane.showMessageDialog(view, "Lỗi khi chuyển vị trí!");
            }
        }
    }
}