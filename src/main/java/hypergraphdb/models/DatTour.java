package hypergraphdb.models;

import java.io.Serializable;

public class DatTour implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String khachHangEmail; // tham chiếu tới User bằng email hoặc id
    private String tourId;
    private int soNguoi;
    private String ngayDat; // Thay LocalDate thành String
    private String ngayKhoiHanh; // Thay LocalDate thành String
    private String trangThai; // Pending / Paid / Cancelled

    public DatTour() {}

    public DatTour(String id, String khachHangEmail, String tourId, int soNguoi,
                   String ngayDat, String ngayKhoiHanh, String trangThai) {
        this.id = id;
        this.khachHangEmail = khachHangEmail;
        this.tourId = tourId;
        this.soNguoi = soNguoi;
        this.ngayDat = ngayDat;
        this.ngayKhoiHanh = ngayKhoiHanh;
        this.trangThai = trangThai;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getKhachHangEmail() { return khachHangEmail; }
    public void setKhachHangEmail(String khachHangEmail) { this.khachHangEmail = khachHangEmail; }

    public String getTourId() { return tourId; }
    public void setTourId(String tourId) { this.tourId = tourId; }

    public int getSoNguoi() { return soNguoi; }
    public void setSoNguoi(int soNguoi) { this.soNguoi = soNguoi; }

    public String getNgayDat() { return ngayDat; }
    public void setNgayDat(String ngayDat) { this.ngayDat = ngayDat; }

    public String getNgayKhoiHanh() { return ngayKhoiHanh; }
    public void setNgayKhoiHanh(String ngayKhoiHanh) { this.ngayKhoiHanh = ngayKhoiHanh; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    @Override
    public String toString() {
        return "DatTour{id='" + id + "', tourId='" + tourId + "', khachHang='" + khachHangEmail + "'}";
    }
}
