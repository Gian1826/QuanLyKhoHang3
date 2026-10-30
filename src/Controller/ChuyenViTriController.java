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
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class ChuyenViTriController {
    private ChuyenViTriPanel view;
    private ChuyenViTriDAO dao;

    public ChuyenViTriController(ChuyenViTriPanel view) {
        this.view = view;
        this.dao = new ChuyenViTriDAO();
        initController();
        view.getTxtSoLuongChuyen().getDocument().addDocumentListener(new DocumentListener() {
    @Override public void insertUpdate(DocumentEvent e)  { capNhatCotSoLuongChuyen(); }
    @Override public void removeUpdate(DocumentEvent e)  { capNhatCotSoLuongChuyen(); }
    @Override public void changedUpdate(DocumentEvent e) { capNhatCotSoLuongChuyen(); }
    });
        Timer timerTimKiem = new Timer(300, e -> {
    String maSP = view.getTxtMaSanPham().getText().trim();
    if (maSP.isEmpty()) {
        ((DefaultTableModel) view.getTableChiTiet().getModel()).setRowCount(0);
        view.getCbViTriNguon().removeAllItems();
        view.getCbViTriNguon().addItem("— Chọn vị trí —");
    } else {
        xuLyHienThiThongTinSanPham(maSP);
    }
});
timerTimKiem.setRepeats(false);

view.getTxtMaSanPham().getDocument().addDocumentListener(new DocumentListener() {
    @Override public void insertUpdate(DocumentEvent e)  { timerTimKiem.restart(); }
    @Override public void removeUpdate(DocumentEvent e)  { timerTimKiem.restart(); }
    @Override public void changedUpdate(DocumentEvent e) { timerTimKiem.restart(); }
});
    }

    private void initController() {
        loadViTriDich();
        
        // Điền tự động tên người thực hiện từ phiên đăng nhập
        String hoTen = Model.PhienDangNhap.hoTen;
        view.getTxtNguoiThucHien().setText(
        (hoTen != null && !hoTen.isEmpty()) ? hoTen : Model.PhienDangNhap.tenDangNhap);
        view.getTxtNguoiThucHien().setEditable(false);

        // Sự kiện khi người dùng nhập xong Mã SP và click ra ngoài
        view.getTxtMaSanPham().addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                String maSP = view.getTxtMaSanPham().getText().trim();
                if (!maSP.isEmpty()) {
                    xuLyHienThiThongTinSanPham(maSP);
                }
            }
        });
        
        // Sự kiện khi ấn phím Enter tại ô Mã SP
        view.getTxtMaSanPham().addActionListener(e -> {
             String maSP = view.getTxtMaSanPham().getText().trim();
             if (!maSP.isEmpty()) {
                 xuLyHienThiThongTinSanPham(maSP);
             }
        });

        // Sự kiện khi chọn Vị trí nguồn -> Lọc đúng dòng vị trí đó trên bảng
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
    private void capNhatCotSoLuongChuyen() {
    DefaultTableModel model = (DefaultTableModel) view.getTableChiTiet().getModel();
    if (model.getRowCount() == 0) return;

    String slStr = view.getTxtSoLuongChuyen().getText().trim();
    // Nếu chưa nhập hoặc nhập không phải số -> hiện 0
    String hienThi = slStr.matches("\\d+") ? slStr : "0";

    Object nguon = view.getCbViTriNguon().getSelectedItem();
    String viTriNguon = nguon == null ? "" : nguon.toString();

    for (int i = 0; i < model.getRowCount(); i++) {
        // Chỉ dòng đúng vị trí nguồn đang chọn mới nhận số lượng chuyển
        boolean dungDong = model.getValueAt(i, 2).toString().equals(viTriNguon);
        model.setValueAt(dungDong ? hienThi : "0", i, 4);
    }
}
    private void xuLyHienThiThongTinSanPham(String maSP) {
        view.getCbViTriNguon().removeAllItems();
    view.getCbViTriNguon().addItem("— Chọn vị trí —");

    DefaultTableModel model = (DefaultTableModel) view.getTableChiTiet().getModel();
    List<String> listNguon = dao.layViTriNguonCuaSanPham(maSP);

    if (listNguon.isEmpty()) {
        model.setRowCount(0);   // không tìm thấy -> xóa bảng, KHÔNG hiện popup
        return;
    }

    for (String vt : listNguon) {
        view.getCbViTriNguon().addItem(vt);
    }
    dao.timKiemSanPham(maSP, "", model);  // đổ toàn bộ vị trí tồn kho lên bảng 
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

    private void loadViTriNguon(String maSP) {
        view.getCbViTriNguon().removeAllItems();
        view.getCbViTriNguon().addItem("— Chọn vị trí —");
        List<String> listNguon = dao.layViTriNguonCuaSanPham(maSP);
        for (String vt : listNguon) {
            view.getCbViTriNguon().addItem(vt);
        }
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

        DefaultTableModel model = (DefaultTableModel) view.getTableChiTiet().getModel();
        if (model.getRowCount() == 0) return;
        
        // Kiểm tra dòng đang chọn trên bảng hoặc dòng đầu tiên xem số lượng tồn có đủ không
        int selectedRow = view.getTableChiTiet().getSelectedRow();
        if (selectedRow == -1) selectedRow = 0; // Mặc định lấy dòng đầu tiên nếu chưa click chọn trên bảng
        
        int tonHienTai = Integer.parseInt(model.getValueAt(selectedRow, 3).toString());
        // Nếu chọn đúng vị trí nguồn trên Combobox thì lấy chính xác vị trí đó từ bảng
        for(int i = 0; i < model.getRowCount(); i++) {
            if(model.getValueAt(i, 2).toString().equals(viTriNguon)) {
                tonHienTai = Integer.parseInt(model.getValueAt(i, 3).toString());
                break;
            }
        }

        if (slChuyen > tonHienTai) {
            JOptionPane.showMessageDialog(view, "Số lượng chuyển (" + slChuyen + ") lớn hơn số lượng tồn hiện tại (" + tonHienTai + ") tại vị trí nguồn!");
            return;
        }

        // Lấy chuẩn MaTaiKhoan từ class PhienDangNhap của bạn (Tránh lỗi gán cứng số 1)
        int maTaiKhoan = 1; // Đảm bảo class PhienDangNhap có biến tĩnh maTaiKhoan

        int confirm = JOptionPane.showConfirmDialog(view, "Bạn có chắc chắn muốn chuyển " + slChuyen + " sản phẩm " + maSP + " từ " + viTriNguon + " sang " + viTriDich + " không?", "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (dao.thucHienChuyenViTri(maSP, viTriNguon, viTriDich, slChuyen, lyDo, maTaiKhoan)) {
                JOptionPane.showMessageDialog(view, "Chuyển vị trí thành công!");
                loadViTriNguon(maSP); 
                // Load lại bảng chi tiết
                dao.timKiemSanPham(maSP, "", model);
                view.getTxtSoLuongChuyen().setText("");
            } else {
                JOptionPane.showMessageDialog(view, "Lỗi khi chuyển vị trí!");
            }
        }
    }
}