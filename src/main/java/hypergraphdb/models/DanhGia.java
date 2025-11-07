package hypergraphdb.models;

import java.io.Serializable;
import java.time.LocalDateTime;

public class DanhGia implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String khachHangEmail;
    private String tourId;
    private int diemDanhGia; // 1-5
    private String binhLuan;
    private LocalDateTime ngayDanhGia;

    public DanhGia() {}

    public DanhGia(String id, String khachHangEmail, String tourId, int diemDanhGia, String binhLuan, LocalDateTime ngayDanhGia) {
        this.id = id;
        this.khachHangEmail = khachHangEmail;
        this.tourId = tourId;
        this.diemDanhGia = diemDanhGia;
        this.binhLuan = binhLuan;
        this.ngayDanhGia = ngayDanhGia;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getKhachHangEmail() { return khachHangEmail; }
    public void setKhachHangEmail(String khachHangEmail) { this.khachHangEmail = khachHangEmail; }

    public String getTourId() { return tourId; }
    public void setTourId(String tourId) { this.tourId = tourId; }

    public int getDiemDanhGia() { return diemDanhGia; }
    public void setDiemDanhGia(int diemDanhGia) { this.diemDanhGia = diemDanhGia; }

    public String getBinhLuan() { return binhLuan; }
    public void setBinhLuan(String binhLuan) { this.binhLuan = binhLuan; }

    public LocalDateTime getNgayDanhGia() { return ngayDanhGia; }
    public void setNgayDanhGia(LocalDateTime ngayDanhGia) { this.ngayDanhGia = ngayDanhGia; }

    @Override
    public String toString() {
        return "DanhGia{id='" + id + "', tourId='" + tourId + "', diem=" + diemDanhGia + "}";
    }
}
