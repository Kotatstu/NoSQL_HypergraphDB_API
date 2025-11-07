package hypergraphdb.models;

import java.io.Serializable;

public class Tour implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String tenTour;
    private String moTa;
    private double gia;
    private String thoiGian; // "3 ngày 2 đêm"
    private String diemKhoiHanh;
    private String diemDen;
    private String phuongTien;
    private String hinhAnh;
    private String nhaToChucId; // tham chiếu tới NhaToChuc

    public Tour() {}

    public Tour(String id, String tenTour, String moTa, double gia, String thoiGian,
                String diemKhoiHanh, String diemDen, String phuongTien, String hinhAnh, String nhaToChucId) {
        this.id = id;
        this.tenTour = tenTour;
        this.moTa = moTa;
        this.gia = gia;
        this.thoiGian = thoiGian;
        this.diemKhoiHanh = diemKhoiHanh;
        this.diemDen = diemDen;
        this.phuongTien = phuongTien;
        this.hinhAnh = hinhAnh;
        this.nhaToChucId = nhaToChucId;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTenTour() { return tenTour; }
    public void setTenTour(String tenTour) { this.tenTour = tenTour; }

    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }

    public double getGia() { return gia; }
    public void setGia(double gia) { this.gia = gia; }

    public String getThoiGian() { return thoiGian; }
    public void setThoiGian(String thoiGian) { this.thoiGian = thoiGian; }

    public String getDiemKhoiHanh() { return diemKhoiHanh; }
    public void setDiemKhoiHanh(String diemKhoiHanh) { this.diemKhoiHanh = diemKhoiHanh; }

    public String getDiemDen() { return diemDen; }
    public void setDiemDen(String diemDen) { this.diemDen = diemDen; }

    public String getPhuongTien() { return phuongTien; }
    public void setPhuongTien(String phuongTien) { this.phuongTien = phuongTien; }

    public String getHinhAnh() { return hinhAnh; }
    public void setHinhAnh(String hinhAnh) { this.hinhAnh = hinhAnh; }

    public String getNhaToChucId() { return nhaToChucId; }
    public void setNhaToChucId(String nhaToChucId) { this.nhaToChucId = nhaToChucId; }

    @Override
    public String toString() {
        return "Tour{id='" + id + "', tenTour='" + tenTour + "', gia=" + gia + "}";
    }
}
