package hypergraphdb.models;

import java.io.Serializable;
import java.time.LocalDateTime;

public class HoaDon implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String datTourId;
    private double tongTien;
    private String phuongThucThanhToan; // MoMo, Card, Cash...
    private String trangThaiThanhToan; // Paid / Unpaid / Refunded
    private LocalDateTime ngayThanhToan;

    public HoaDon() {}

    public HoaDon(String id, String datTourId, double tongTien, String phuongThucThanhToan,
                  String trangThaiThanhToan, LocalDateTime ngayThanhToan) {
        this.id = id;
        this.datTourId = datTourId;
        this.tongTien = tongTien;
        this.phuongThucThanhToan = phuongThucThanhToan;
        this.trangThaiThanhToan = trangThaiThanhToan;
        this.ngayThanhToan = ngayThanhToan;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getDatTourId() { return datTourId; }
    public void setDatTourId(String datTourId) { this.datTourId = datTourId; }

    public double getTongTien() { return tongTien; }
    public void setTongTien(double tongTien) { this.tongTien = tongTien; }

    public String getPhuongThucThanhToan() { return phuongThucThanhToan; }
    public void setPhuongThucThanhToan(String phuongThucThanhToan) { this.phuongThucThanhToan = phuongThucThanhToan; }

    public String getTrangThaiThanhToan() { return trangThaiThanhToan; }
    public void setTrangThaiThanhToan(String trangThaiThanhToan) { this.trangThaiThanhToan = trangThaiThanhToan; }

    public LocalDateTime getNgayThanhToan() { return ngayThanhToan; }
    public void setNgayThanhToan(LocalDateTime ngayThanhToan) { this.ngayThanhToan = ngayThanhToan; }

    @Override
    public String toString() {
        return "HoaDon{id='" + id + "', datTourId='" + datTourId + "', tongTien=" + tongTien + "}";
    }
}
