package Controller;

import Model.PhieuHoanTraDAO;
import View.PhieuHoanTraDaLuuPanel;
import java.awt.Dimension;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

public class PhieuHoanTraDaLuuController {

    private static final DateTimeFormatter DMY = DateTimeFormatter.ofPattern("d/M/yyyy");

    private final PhieuHoanTraDaLuuPanel view;
    private final PhieuHoanTraDAO dao = new PhieuHoanTraDAO();
    private Consumer<String> onSua;

    public PhieuHoanTraDaLuuController(PhieuHoanTraDaLuuPanel view) {
        this.view = view;

        // Bảng chỉ để xem, không cho sửa ô
        view.getTableDanhSach().setModel(new DefaultTableModel(new Object[][]{},
                new String[]{"Mã Phiếu", "Ngày", "Phiếu Xuất Gốc", "Người Lập", "Số Mặt Hàng", "Trạng Thái"}) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        });

        view.getCbTrangThai().removeAllItems();
        for (String s : new String[]{"Tất cả", "Nháp", "Đã lưu", "Đã hủy"}) {
            view.getCbTrangThai().addItem(s);
        }

        view.getBtnTimKiem().addActionListener(e -> loadData());
        view.getBtnXemChiTiet().addActionListener(e -> xemChiTiet());
        view.getBtnSua().addActionListener(e -> sua());
        view.getBtnInPhieu().addActionListener(e -> inPhieu());
        view.getBtnHuyPhieu().addActionListener(e -> huyPhieu());

        loadData();
    }

    public void setOnSua(Consumer<String> onSua) {
        this.onSua = onSua;
    }

    public void taiLai() {
        loadData();
    }

    // Nhận dd/MM/yyyy hoặc yyyy-MM-dd, trả về yyyy-MM-dd (rỗng thì null)
    private String chuanHoa(String s) {
        s = s.trim();
        if (s.isEmpty()) return null;
        return s.contains("/") ? LocalDate.parse(s, DMY).toString() : LocalDate.parse(s).toString();
    }

    private static String tenTrangThai(int tt) {
        return tt == 0 ? "Nháp" : tt == 1 ? "Đã lưu" : "Đã hủy";
    }

    private void loadData() {
        String tu, den;
        try {
            tu = chuanHoa(view.getTxtTuNgay().getText());
            den = chuanHoa(view.getTxtDenNgay().getText());
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(view, "Ngày không hợp lệ! Nhập dd/MM/yyyy hoặc yyyy-MM-dd.");
            return;
        }
        String tuKhoa = view.getTxtTuKhoa().getText().trim();
        int idx = view.getCbTrangThai().getSelectedIndex();
        Integer trangThai = idx > 0 ? idx - 1 : null;   // 0 nháp, 1 đã lưu, 2 đã hủy

        DefaultTableModel model = (DefaultTableModel) view.getTableDanhSach().getModel();
        model.setRowCount(0);
        for (Object[] r : dao.timKiem(tu, den, tuKhoa, trangThai)) {
            r[5] = tenTrangThai((Integer) r[5]);
            model.addRow(r);
        }
    }

    private String maDangChon() {
        JTable t = view.getTableDanhSach();
        int row = t.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(view, "Vui lòng chọn một phiếu!");
            return null;
        }
        return t.getValueAt(row, 0).toString();
    }

    private String trangThaiDangChon() {
        JTable t = view.getTableDanhSach();
        return t.getValueAt(t.getSelectedRow(), 5).toString();
    }

    private void xemChiTiet() {
        String ma = maDangChon();
        if (ma == null) return;
        List<Object[]> ds = dao.layChiTietDaLuu(ma);
        JTable bang = new JTable(new DefaultTableModel(ds.toArray(new Object[0][]),
                new String[]{"Mã SP", "Tên SP", "SL Hoàn Trả", "Tình Trạng"}));
        JScrollPane sp = new JScrollPane(bang);
        sp.setPreferredSize(new Dimension(520, 220));
        JOptionPane.showMessageDialog(view, sp, "Chi tiết phiếu " + ma, JOptionPane.PLAIN_MESSAGE);
    }

    private void sua() {
        String ma = maDangChon();
        if (ma == null) return;
        if (!"Nháp".equals(trangThaiDangChon())) {
            JOptionPane.showMessageDialog(view, "Chỉ được sửa phiếu ở trạng thái Nháp!");
            return;
        }
        if (onSua != null) onSua.accept(ma);
    }

    private void inPhieu() {
        String ma = maDangChon();
        if (ma != null) JOptionPane.showMessageDialog(view, "In phiếu: " + ma);
    }

    private void huyPhieu() {
        String ma = maDangChon();
        if (ma == null) return;
        if ("Đã hủy".equals(trangThaiDangChon())) {
            JOptionPane.showMessageDialog(view, "Phiếu này đã bị hủy!");
            return;
        }
        int c = JOptionPane.showConfirmDialog(view,
                "Bạn có chắc chắn muốn hủy phiếu " + ma + " không?",
                "Xác nhận hủy", JOptionPane.YES_NO_OPTION);
        if (c != JOptionPane.YES_OPTION) return;

        if (dao.huyPhieu(ma)) {
            JOptionPane.showMessageDialog(view, "Đã hủy phiếu thành công!");
            loadData();
        } else {
            JOptionPane.showMessageDialog(view, "Hủy phiếu thất bại!\n" + dao.loiCuoi);
        }
    }
}