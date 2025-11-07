package hypergraphdb.models;

import java.io.Serializable;

public class NhaToChuc implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String ten;
    private String email;
    private String sdt;
    private String diaChi;
    private String moTa;

    public NhaToChuc() {}

    public NhaToChuc(String id, String ten, String email, String sdt, String diaChi, String moTa) {
        this.id = id;
        this.ten = ten;
        this.email = email;
        this.sdt = sdt;
        this.diaChi = diaChi;
        this.moTa = moTa;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTen() { return ten; }
    public void setTen(String ten) { this.ten = ten; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSdt() { return sdt; }
    public void setSdt(String sdt) { this.sdt = sdt; }

    public String getDiaChi() { return diaChi; }
    public void setDiaChi(String diaChi) { this.diaChi = diaChi; }

    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }

    @Override
    public String toString() {
        return "NhaToChuc{id='" + id + "', ten='" + ten + "'}";
    }
}
