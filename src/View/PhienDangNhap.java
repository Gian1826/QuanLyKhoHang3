package View;
public class PhienDangNhap {
    public static String tenDangNhap;
    public static String hoTen;
    public static String vaiTro;

    public static boolean laAdmin() {
        return "Admin".equalsIgnoreCase(vaiTro);
    }

    public static void xoa() {
        tenDangNhap = null;
        hoTen = null;
        vaiTro = null;
    }
}
