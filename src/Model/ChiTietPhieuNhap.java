/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Model;

/**
 *
 * @author HP
 */
public class ChiTietPhieuNhap {
    private String maSP;
    private String tenSP;
    private String dvt;
    private int soLuong;
    private double donGia;

    // Getters, Setters, Constructors
    public double getThanhTien() {
        return soLuong * donGia;
    }
}
