package View;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TaiKhoanDAO {

    // ----- Băm mật khẩu -----
    private static String hex(byte[] b) {
        StringBuilder sb = new StringBuilder();
        for (byte x : b) sb.append(String.format("%02x", x));
        return sb.toString();
    }

    public static String taoSalt() {
        byte[] b = new byte[16];
        new SecureRandom().nextBytes(b);
        return hex(b);
    }

    public static String bam(String salt, String matKhau) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return hex(md.digest((salt + matKhau).getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static boolean khop(String a, String b) {
        return MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8),
                                     b.getBytes(StandardCharsets.UTF_8));
    }

    // ----- Đăng nhập: đúng thì ghi vào PhienDangNhap -----
    public static boolean dangNhap(String user, String matKhau) throws SQLException {
        String sql = "SELECT MatKhauHash, Salt, HoTen, VaiTro FROM TaiKhoan "
                   + "WHERE TenDangNhap = ? AND HoatDong = 1";
        try (PreparedStatement ps = KetNoiDB.getConnection().prepareStatement(sql)) {
            ps.setString(1, user);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return false;
                String hashDB = rs.getString(1).trim();
                String salt   = rs.getString(2).trim();
                if (!khop(hashDB, bam(salt, matKhau))) return false;

                PhienDangNhap.tenDangNhap = user;
                PhienDangNhap.hoTen  = rs.getString(3);
                PhienDangNhap.vaiTro = rs.getString(4);
                return true;
            }
        }
    }

    // ----- Tạo tài khoản mới -----
    public static void taoTaiKhoan(String user, String matKhau, String hoTen, String vaiTro)
            throws SQLException {
        String salt = taoSalt();
        String sql = "INSERT INTO TaiKhoan(TenDangNhap, MatKhauHash, Salt, HoTen, VaiTro) "
                   + "VALUES (?,?,?,?,?)";
        try (PreparedStatement ps = KetNoiDB.getConnection().prepareStatement(sql)) {
            ps.setString(1, user);
            ps.setString(2, bam(salt, matKhau));
            ps.setString(3, salt);
            ps.setString(4, hoTen);
            ps.setString(5, vaiTro);
            ps.executeUpdate();
        }
    }

    // ----- Đổi mật khẩu: trả về false nếu mật khẩu cũ sai -----
    public static boolean doiMatKhau(String user, String cu, String moi) throws SQLException {
        Connection c = KetNoiDB.getConnection();
        String hash, salt;
        try (PreparedStatement ps = c.prepareStatement(
                "SELECT MatKhauHash, Salt FROM TaiKhoan WHERE TenDangNhap = ?")) {
            ps.setString(1, user);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return false;
                hash = rs.getString(1).trim();
                salt = rs.getString(2).trim();
            }
        }
        if (!khop(hash, bam(salt, cu))) return false;

        String saltMoi = taoSalt();
        try (PreparedStatement ps = c.prepareStatement(
                "UPDATE TaiKhoan SET MatKhauHash = ?, Salt = ? WHERE TenDangNhap = ?")) {
            ps.setString(1, bam(saltMoi, moi));
            ps.setString(2, saltMoi);
            ps.setString(3, user);
            ps.executeUpdate();
        }
        return true;
    }
}
