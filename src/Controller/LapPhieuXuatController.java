package Controller;

import Model.LapPhieuXuatDAO;
import Model.PhienDangNhap;
import View.LapPhieuXuatPanel; // Cập nhật đúng tên lớp View của bạn
import java.awt.event.ActionEvent;
import javax.swing.JOptionPane;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

public class LapPhieuXuatController {
    private LapPhieuXuatPanel view;
    private LapPhieuXuatDAO dao;
    private boolean isUpdatingTable = false; // Cờ ngăn chặn lặp vô hạn khi code tự sửa table

    public LapPhieuXuatController(LapPhieuXuatPanel view) {
        this.view = view;
        this.dao = new LapPhieuXuatDAO();
        initController();
    }

    private void initController() {
        // Tự động điền người lập
        if(PhienDangNhap.hoTen != null) {
            view.getTxtNguoiLap().setText(PhienDangNhap.hoTen);
            view.getTxtNguoiLap().setEditable(false);
        }

        // Sự kiện Thêm dòng
        view.getBtnThemDong().addActionListener(e -> {
            DefaultTableModel model = (DefaultTableModel) view.getTableChiTiet().getModel();
            model.addRow(new Object[]{model.getRowCount() + 1, "", "", "", 0, 1, 0.0, 0.0});
        });

        // Sự kiện Xóa dòng
        view.getBtnXoaDong().addActionListener(e -> {
            DefaultTableModel model = (DefaultTableModel) view.getTableChiTiet().getModel();
            int selectedRow = view.getTableChiTiet().getSelectedRow();
            if (selectedRow >= 0) {
                model.removeRow(selectedRow);
                capNhatSTTVaTongTien(model);
            } else {
                JOptionPane.showMessageDialog(view, "Vui lòng chọn dòng cần xóa!");
            }
        });

        // Bắt sự kiện thay đổi dữ liệu trên JTable để tính toán tự động
        DefaultTableModel tableModel = (DefaultTableModel) view.getTableChiTiet().getModel();
        tableModel.addTableModelListener(new TableModelListener() {
            @Override
            public void tableChanged(TableModelEvent e) {
                if (isUpdatingTable) return;
                
                int row = e.getFirstRow();
                int col = e.getColumn();
                if (row < 0) return;

                isUpdatingTable = true;
                
                try {
                    // Nếu người dùng nhập Mã SP (Cột 1) -> Tự động load Tên, ĐVT, Tồn, Giá
                    if (col == 1) {
                        String maSP = tableModel.getValueAt(row, 1).toString().trim();
                        if (!maSP.isEmpty()) {
                            Object[] info = dao.layThongTinSanPham(maSP);
                            if (info != null) {
                                tableModel.setValueAt(info[0], row, 2); // Tên SP
                                tableModel.setValueAt(info[1], row, 3); // ĐVT
                                tableModel.setValueAt(info[2], row, 4); // Tồn
                                tableModel.setValueAt(info[3], row, 6); // Đơn Giá
                                tinhThanhTien(tableModel, row);
                            } else {
                                JOptionPane.showMessageDialog(view, "Mã sản phẩm không tồn tại!");
                            }
                        }
                    }
                    // Nếu sửa SL Xuất (Cột 5) hoặc Đơn Giá (Cột 6) -> Tính lại Thành Tiền
                    else if (col == 5 || col == 6) {
                        tinhThanhTien(tableModel, row);
                    }
                } finally {
                    isUpdatingTable = false;
                }
            }
        });

        // Sự kiện Lưu Nháp (Trạng thái = 0)
        view.getBtnLuuNhap().addActionListener(e -> xuLyLuuPhieu(0));

        // Sự kiện Lưu Phiếu (Trạng thái = 1)
        view.getBtnLuuPhieu().addActionListener(e -> xuLyLuuPhieu(1));

        // Sự kiện Hủy (Xóa trắng Form)
        view.getBtnHuy().addActionListener(e -> xoaTrangForm());
    }

    private void tinhThanhTien(DefaultTableModel model, int row) {
        try {
            int slXuat = Integer.parseInt(model.getValueAt(row, 5).toString());
            double donGia = Double.parseDouble(model.getValueAt(row, 6).toString());
            double thanhTien = slXuat * donGia;
            model.setValueAt(thanhTien, row, 7); // Cột Thành tiền
            capNhatSTTVaTongTien(model);
        } catch (Exception ex) {
            // Bỏ qua lỗi ép kiểu nếu đang nhập dở
        }
    }

    private void capNhatSTTVaTongTien(DefaultTableModel model) {
        double tongTien = 0;
        for (int i = 0; i < model.getRowCount(); i++) {
            model.setValueAt(i + 1, i, 0); // Cập nhật lại STT
            Object ttObj = model.getValueAt(i, 7);
            if (ttObj != null) {
                tongTien += Double.parseDouble(ttObj.toString());
            }
        }
        view.getTxtTongTien().setText(String.format("%,.0f", tongTien));
    }

    private void xuLyLuuPhieu(int trangThai) {
        // 1. Lấy dữ liệu cơ bản
        String maPhieu = view.getTxtMaPhieu().getText().trim();
        String ngayXuat = view.getTxtNgayXuat().getText().trim();
        String khachHang = view.getTxtKhachHang().getText().trim(); // Tạm dùng Text, nếu CSDL dùng Mã KH cần combobox
        String maKhu = view.getCbKhuXuat().getSelectedItem() != null ? view.getCbKhuXuat().getSelectedItem().toString() : "";
        String ghiChu = view.getTxtGhiChu().getText().trim();
        
        if (maPhieu.isEmpty() || ngayXuat.isEmpty()) {
            JOptionPane.showMessageDialog(view, "Vui lòng nhập Mã phiếu và Ngày xuất!");
            return;
        }

        DefaultTableModel model = (DefaultTableModel) view.getTableChiTiet().getModel();
        if (model.getRowCount() == 0 || model.getValueAt(0, 1) == null || model.getValueAt(0, 1).toString().isEmpty()) {
            JOptionPane.showMessageDialog(view, "Vui lòng nhập ít nhất một sản phẩm!");
            return;
        }
        
        // Loại bỏ dấu phẩy khỏi chuỗi Tổng tiền trước khi parse
        double tongTien = 0;
        try {
             String tienStr = view.getTxtTongTien().getText().replace(",", "").replace(".", "");
             tongTien = Double.parseDouble(tienStr);
        } catch (Exception ignored) {}

        // Kiểm tra tồn kho trước khi cho phép lưu thật
        if (trangThai == 1) {
            for (int i = 0; i < model.getRowCount(); i++) {
                if (model.getValueAt(i, 1) == null || model.getValueAt(i, 1).toString().isEmpty()) continue;
                int ton = Integer.parseInt(model.getValueAt(i, 4).toString());
                int xuat = Integer.parseInt(model.getValueAt(i, 5).toString());
                if (xuat > ton) {
                    JOptionPane.showMessageDialog(view, "Dòng " + (i+1) + ": Số lượng xuất lớn hơn tồn kho!");
                    return;
                }
            }
        }

        int maTaiKhoan = 1; // Lấy từ PhienDangNhap

        // Gọi DAO lưu phiếu
        boolean kq = dao.luuPhieuXuat(maPhieu, ngayXuat, khachHang, maTaiKhoan, maKhu, tongTien, ghiChu, trangThai, model);
        
        if (kq) {
            String msg = trangThai == 1 ? "Lưu phiếu xuất thành công!" : "Lưu nháp thành công!";
            JOptionPane.showMessageDialog(view, msg);
            xoaTrangForm();
        } else {
            JOptionPane.showMessageDialog(view, "Lỗi lưu dữ liệu. Có thể trùng Mã phiếu!");
        }
    }

    private void xoaTrangForm() {
        view.getTxtMaPhieu().setText("");
        view.getTxtNgayXuat().setText("");
        view.getTxtKhachHang().setText("");
        view.getTxtGhiChu().setText("");
        view.getTxtTongTien().setText("0");
        ((DefaultTableModel) view.getTableChiTiet().getModel()).setRowCount(0);
    }
}