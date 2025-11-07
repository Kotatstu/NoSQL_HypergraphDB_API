package hypergraphdb.models;

import java.io.Serializable;

public class DiaDiem implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String tenDiaDiem;
    private String moTa;
    private String hinhAnh;

    public DiaDiem() {}

    public DiaDiem(String id, String tenDiaDiem, String moTa, String hinhAnh) {
        this.id = id;
        this.tenDiaDiem = tenDiaDiem;
        this.moTa = moTa;
        this.hinhAnh = hinhAnh;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTenDiaDiem() { return tenDiaDiem; }
    public void setTenDiaDiem(String tenDiaDiem) { this.tenDiaDiem = tenDiaDiem; }

    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }

    public String getHinhAnh() { return hinhAnh; }
    public void setHinhAnh(String hinhAnh) { this.hinhAnh = hinhAnh; }

    @Override
    public String toString() {
        return "DiaDiem{id='" + id + "', ten='" + tenDiaDiem + "'}";
    }
}
