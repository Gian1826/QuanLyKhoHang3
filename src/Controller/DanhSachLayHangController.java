package Controller;

import Model.DanhSachLayHangDAO;
import Model.PhienDangNhap;
import View.DanhSachLayHangPanel;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

public class DanhSachLayHangController {
    private final DanhSachLayHangPanel view;
    private final DanhSachLayHangDAO dao;
    private final Timer timerLoc;

    public DanhSachLayHangController(DanhSachLayHangPanel view) {
        this.view = view;
        this.dao = new DanhSachLayHangDAO();
        this.timerLoc = new Timer(300, e -> loadData());
        this.timerLoc.setRepeats(false);
        initController();
        loadData();
        
    }
    private boolean daDangNhap() {
    String ten = PhienDangNhap.tenDangNhap;
    if (ten == null || ten.trim().isEmpty() || ten.equalsIgnoreCase("guest")) return false;
    return dao.layMaTaiKhoan(ten) != -1;   // phải có thật trong bảng TaiKhoan
    }
    private void initController() {
        // Gõ ngày -> bảng hiện ngay (debounce 300ms)
        
        view.getTableDanhSach().setDefaultEditor(Object.class, null);
        view.getTableDanhSach().setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        view.getTableDanhSach().getTableHeader().setReorderingAllowed(false);
        // Đổi Khu / Trạng thái -> lọc ngay
        
        view.getBtnTimKiem().addActionListener(e -> loadData());
        view.getBtnBatDauLay().addActionListener(e -> thucHienBatDauLay());
        view.getBtnXacNhanDaLay().addActionListener(e -> thucHienXacNhanLay());
    }

    /** Trả về "" nếu ô trống, "yyyy-MM-dd" nếu hợp lệ, null nếu chưa hợp lệ. */
    private String chuanHoaNgay(String s) {
        if (s.isEmpty()) return "";
        DateTimeFormatter[] dinhDang = {
            DateTimeFormatter.ofPattern("d/M/uuuu"),
            DateTimeFormatter.ofPattern("uuuu-M-d")
        };
        for (DateTimeFormatter f : dinhDang) {
            try {
                return LocalDate.parse(s, f).toString();   // yyyy-MM-dd
            } catch (Exception ignored) { }
        }
        return null;
    }

    public void loadData() {
    String ngay = chuanHoaNgay(view.getTxtNgay().getText().trim());
    if (ngay == null) return;   // đang gõ dở, giữ nguyên bảng

    String khu = view.getCbKhu().getSelectedItem() != null
            ? view.getCbKhu().getSelectedItem().toString().trim() : "Tất cả";
    String trangThai = view.getCbTrangThai().getSelectedItem() != null
            ? view.getCbTrangThai().getSelectedItem().toString() : "Tất cả";

    DefaultTableModel model = (DefaultTableModel) view.getTableDanhSach().getModel();
    // Truyền "Tất cả" cho DAO để DAO không lọc theo khu của phiếu xuất
    dao.timKiemDanhSachLayHang(ngay, "Tất cả", trangThai, model);

    // Lọc khu theo vị trí lấy hàng (A-03, B-11...) ngay tại controller
    if (!khu.isEmpty() && !khu.equalsIgnoreCase("Tất cả")) {
        String chuKhu = khu.substring(khu.length() - 1).toUpperCase();   // "Khu A" hoặc "A" -> "A"
        for (int i = model.getRowCount() - 1; i >= 0; i--) {
            Object vt = model.getValueAt(i, 3);
            String viTri = vt == null ? "" : vt.toString().trim().toUpperCase();
            if (!viTri.startsWith(chuKhu + "-")) {
                model.removeRow(i);
            }
        }
    }
}
    private Integer layMaNguoiLay() {
    int ma = dao.layMaTaiKhoan(PhienDangNhap.tenDangNhap);
    return ma == -1 ? null : ma;   // không có tài khoản thì không ghi người lấy
    }

    private void thucHienBatDauLay() {
        int row = view.getTableDanhSach().getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(view, "Vui lòng chọn một dòng lệnh để bắt đầu!");
            return;
        }
        String maLenh = view.getTableDanhSach().getValueAt(row, 0).toString();
        String ttHienTai = view.getTableDanhSach().getValueAt(row, 5).toString();

        if (!ttHienTai.equals("Chờ lấy")) {
            JOptionPane.showMessageDialog(view, "Chỉ có thể bắt đầu các lệnh đang ở trạng thái 'Chờ lấy'!");
            return;
        }
        Integer maNguoiLay = layMaNguoiLay();

        if (dao.capNhatTrangThaiLenh(maLenh, 1, maNguoiLay)) {
            JOptionPane.showMessageDialog(view, "Đã bắt đầu lấy hàng cho lệnh: " + maLenh);
            loadData();
        } else {
            JOptionPane.showMessageDialog(view, "Có lỗi xảy ra khi cập nhật!");
        }
    }

    private void thucHienXacNhanLay() {
        int row = view.getTableDanhSach().getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(view, "Vui lòng chọn một dòng lệnh để xác nhận!");
            return;
        }
        String maLenh = view.getTableDanhSach().getValueAt(row, 0).toString();
        String ttHienTai = view.getTableDanhSach().getValueAt(row, 5).toString();

        if (!ttHienTai.equals("Đang lấy")) {
            JOptionPane.showMessageDialog(view, "Chỉ có thể xác nhận các lệnh đang ở trạng thái 'Đang lấy'!");
            return;
        }
        Integer maNguoiLay = layMaNguoiLay();

        int confirm = JOptionPane.showConfirmDialog(view,
                "Xác nhận đã lấy đủ hàng cho lệnh " + maLenh + "?",
                "Xác nhận", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            if (dao.capNhatTrangThaiLenh(maLenh, 2, maNguoiLay)) {
                JOptionPane.showMessageDialog(view, "Đã hoàn thành lệnh: " + maLenh);
                loadData();
            } else {
                JOptionPane.showMessageDialog(view, "Có lỗi xảy ra khi cập nhật!");
            }
        }
    }
}