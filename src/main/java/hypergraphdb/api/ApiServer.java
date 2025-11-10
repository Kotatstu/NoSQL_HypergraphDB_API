package hypergraphdb.api;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.HyperGraph;

import com.google.gson.Gson;

import hypergraphdb.models.DanhGia;
import hypergraphdb.models.DatTour;
import hypergraphdb.models.DiaDiem;
import hypergraphdb.models.HoaDon;
import hypergraphdb.models.NhaToChuc;
import hypergraphdb.models.Tour;
import hypergraphdb.models.User;
import static spark.Spark.before;
import static spark.Spark.get;
import static spark.Spark.options;
import static spark.Spark.port;
import static spark.Spark.post;

public class ApiServer {

    private static final String DB_PATH = "db/mainDB";
    private static final Gson gson = new Gson();

    // Lưu user đăng nhập tạm thời
    private static User currentUser = null;
    
    //========================= Các hàm seed dữ liệu ==========================
    //seed nhà tổ chức
    private static void seedNhaToChuc(HyperGraph graph) 
    {
        List<NhaToChuc> existing = graph.getAll(hg.type(NhaToChuc.class));
        if (existing.isEmpty()) {
            NhaToChuc n1 = new NhaToChuc(
                "NTC001",
                "Công ty du lịch ABC",
                "abc.travel@gmail.com",
                "0905123456",
                "12 Nguyễn Huệ, Quận 1, TP.HCM",
                "Chuyên tổ chức tour trong nước và quốc tế."
            );

            NhaToChuc n2 = new NhaToChuc(
                "NTC002",
                "TravelNow",
                "contact@travelnow.vn",
                "0987123456",
                "25 Trần Hưng Đạo, Hà Nội",
                "Công ty chuyên về tour nghỉ dưỡng cao cấp."
            );

            NhaToChuc n3 = new NhaToChuc(
                "NTC003",
                "AmazingTrip",
                "info@amazingtrip.vn",
                "0978123123",
                "45 Bạch Đằng, Đà Nẵng",
                "Mang đến trải nghiệm du lịch tuyệt vời khắp Việt Nam."
            );

            graph.add(n1);
            graph.add(n2);
            graph.add(n3);
        }
    }

    //Seed users
    public static void seedUsers(HyperGraph graph) {
        // Kiểm tra nếu đã có dữ liệu thì bỏ qua để tránh trùng
        List<User> existing = graph.getAll(hg.type(User.class));
        if (!existing.isEmpty()) {
            return;
        }

        List<User> users = new ArrayList<>();
        users.add(new User("Nguyễn Hoàng Long", "long@gmail.com", "123456"));
        users.add(new User("Tô Minh Lợi", "loi@gmail.com", "123456"));

        for (User user : users) {
            graph.add(user);
        }
    }

    //seed tour
    public static void seedTours(HyperGraph graph) 
    {
        // Kiểm tra nếu đã có dữ liệu thì bỏ qua để tránh trùng
        List<Tour> existing = graph.getAll(hg.type(Tour.class));
        if (!existing.isEmpty()) {
            return;
        }

        List<Tour> tours = new ArrayList<>();

        tours.add(new Tour(
            "T001",
            "Du lịch Hạ Long 3N2Đ",
            "Khám phá vịnh Hạ Long – kỳ quan thiên nhiên thế giới.",
            3500000,
            "3 ngày 2 đêm",
            "Hà Nội",
            "Quảng Ninh",
            "Xe du lịch",
            "halong.jpg",
            "NTC001"
        ));

        tours.add(new Tour(
            "T002",
            "Khám phá Đà Lạt 4N3Đ",
            "Trải nghiệm không khí se lạnh và cảnh đẹp thơ mộng tại Đà Lạt.",
            4200000,
            "4 ngày 3 đêm",
            "TP. Hồ Chí Minh",
            "Đà Lạt",
            "Máy bay + Xe du lịch",
            "dalat.jpg",
            "NTC002"
        ));

        tours.add(new Tour(
            "T003",
            "Du lịch Phú Quốc 3N2Đ",
            "Tận hưởng biển xanh, cát trắng và hải sản tươi ngon.",
            5500000,
            "3 ngày 2 đêm",
            "Hà Nội",
            "Phú Quốc",
            "Máy bay",
            "phuquoc.jpg",
            "NTC001"
        ));

        tours.add(new Tour(
            "T004",
            "Khám phá Sapa 2N1Đ",
            "Chinh phục Fansipan – nóc nhà Đông Dương.",
            2800000,
            "2 ngày 1 đêm",
            "Hà Nội",
            "Sapa",
            "Tàu hỏa",
            "sapa.jpg",
            "NTC003"
        ));

        for (Tour tour : tours) {
            graph.add(tour);
        }
    }

    //seed Địa Điểm
    public static void seedDiaDiem(HyperGraph graph) {
        // Kiểm tra nếu đã có dữ liệu thì bỏ qua để tránh trùng
        List<DiaDiem> existing = graph.getAll(hg.type(DiaDiem.class));
        if (!existing.isEmpty()) {
            return;
        }

        List<DiaDiem> diaDiems = new ArrayList<>();

        diaDiems.add(new DiaDiem(
            "DD001",
            "Vịnh Hạ Long",
            "Kỳ quan thiên nhiên thế giới nổi tiếng với hàng nghìn hòn đảo đá vôi kỳ vĩ.",
            "halong.jpg"
        ));

        diaDiems.add(new DiaDiem(
            "DD002",
            "Thành phố Đà Lạt",
            "Thành phố ngàn hoa với khí hậu mát mẻ quanh năm, điểm đến lý tưởng cho du lịch nghỉ dưỡng.",
            "dalat.jpg"
        ));

        diaDiems.add(new DiaDiem(
            "DD003",
            "Đảo Phú Quốc",
            "Hòn đảo lớn nhất Việt Nam, nổi tiếng với biển xanh, cát trắng và đặc sản nước mắm.",
            "phuquoc.jpg"
        ));

        diaDiems.add(new DiaDiem(
            "DD004",
            "Thị trấn Sapa",
            "Điểm đến vùng cao với ruộng bậc thang và đỉnh Fansipan – nóc nhà Đông Dương.",
            "sapa.jpg"
        ));

        diaDiems.add(new DiaDiem(
            "DD005",
            "Phố cổ Hội An",
            "Di sản văn hóa thế giới với kiến trúc cổ kính và đèn lồng rực rỡ về đêm.",
            "hoian.jpg"
        ));

        for (DiaDiem dd : diaDiems) {
            graph.add(dd);
        }
    }

    // seed đặt tour
    public static void seedDatTour(HyperGraph graph) {
        // Kiểm tra nếu đã có dữ liệu thì bỏ qua
        List<DatTour> existing = graph.getAll(hg.type(DatTour.class));
        if (!existing.isEmpty()) {
            return;
        }

        List<DatTour> datTours = new ArrayList<>();

        datTours.add(new DatTour(
            "DT001",
            "long@gmail.com",     
            "T001",               
            2,                    
            LocalDate.of(2025, 11, 1),
            LocalDate.of(2025, 12, 10), 
            "Paid"
        ));

        datTours.add(new DatTour(
            "DT002",
            "loi@gmail.com",
            "T002",
            4,
            LocalDate.of(2025, 11, 3),
            LocalDate.of(2026, 1, 5),
            "Pending"
        ));

        datTours.add(new DatTour(
            "DT003",
            "long@gmail.com",
            "T003",
            3,
            LocalDate.of(2025, 11, 4),
            LocalDate.of(2026, 2, 15),
            "Cancelled"
        ));

        datTours.add(new DatTour(
            "DT004",
            "loi@gmail.com",
            "T004",
            5,
            LocalDate.of(2025, 10, 20),
            LocalDate.of(2025, 12, 25),
            "Paid"
        ));

        datTours.add(new DatTour(
            "DT005",
            "long@gmail.com",
            "T005",
            2,
            LocalDate.of(2025, 11, 5),
            LocalDate.of(2026, 3, 1),
            "Pending"
        ));

        for (DatTour dt : datTours) {
            graph.add(dt);
        }
    }

    //seed Hóa Đơn
    public static void seedHoaDon(HyperGraph graph) {
        // Kiểm tra nếu đã có dữ liệu thì bỏ qua để tránh trùng lặp
        List<HoaDon> existing = graph.getAll(hg.type(HoaDon.class));
        if (!existing.isEmpty()) {
            return;
        }

        List<HoaDon> hoaDons = new ArrayList<>();

        hoaDons.add(new HoaDon(
            "HD001",
            "DT001",            
            5000000,            
            "MoMo",             
            "Paid",            
            LocalDateTime.of(2025, 11, 1, 10, 30)
        ));

        hoaDons.add(new HoaDon(
            "HD002",
            "DT002",
            12000000,
            "Card",
            "Unpaid",
            LocalDateTime.of(2025, 11, 3, 14, 45)
        ));

        hoaDons.add(new HoaDon(
            "HD003",
            "DT003",
            7500000,
            "Cash",
            "Refunded",
            LocalDateTime.of(2025, 11, 4, 9, 15)
        ));

        hoaDons.add(new HoaDon(
            "HD004",
            "DT004",
            15000000,
            "Card",
            "Paid",
            LocalDateTime.of(2025, 10, 20, 16, 10)
        ));

        hoaDons.add(new HoaDon(
            "HD005",
            "DT005",
            9500000,
            "MoMo",
            "Unpaid",
            LocalDateTime.of(2025, 11, 5, 11, 5)
        ));

        for (HoaDon hd : hoaDons) {
            graph.add(hd);
        }
    }

    //seed Đánh giá
    public static void seedDanhGia(HyperGraph graph) {
        // Kiểm tra nếu đã có dữ liệu thì bỏ qua
        List<DanhGia> existing = graph.getAll(hg.type(DanhGia.class));
        if (!existing.isEmpty()) {
            return;
        }

        List<DanhGia> danhGias = new ArrayList<>();

        danhGias.add(new DanhGia(
            "DG001",
            "long@gmail.com",   // khachHangEmail
            "T001",             // tourId
            5,                  // diemDanhGia
            "Tour rất tuyệt vời, hướng dẫn viên thân thiện!",
            LocalDateTime.of(2025, 11, 2, 9, 15)
        ));

        danhGias.add(new DanhGia(
            "DG002",
            "loi@gmail.com",
            "T002",
            4,
            "Lịch trình hợp lý, đồ ăn ngon nhưng khách sạn hơi xa trung tâm.",
            LocalDateTime.of(2025, 11, 4, 14, 20)
        ));

        danhGias.add(new DanhGia(
            "DG003",
            "long@gmail.com",
            "T003",
            3,
            "Tour ổn, nhưng phương tiện di chuyển chưa thoải mái.",
            LocalDateTime.of(2025, 11, 6, 10, 45)
        ));

        danhGias.add(new DanhGia(
            "DG004",
            "loi@gmail.com",
            "T004",
            5,
            "Tổ chức rất chuyên nghiệp, tôi sẽ quay lại!",
            LocalDateTime.of(2025, 11, 7, 18, 0)
        ));

        danhGias.add(new DanhGia(
            "DG005",
            "long@gmail.com",
            "T005",
            2,
            "Chuyến đi không như mong đợi, thời tiết xấu và hướng dẫn viên thiếu nhiệt tình.",
            LocalDateTime.of(2025, 11, 8, 11, 30)
        ));

        for (DanhGia dg : danhGias) {
            graph.add(dg);
        }
    }





    public static void main(String[] args) {
        port(8080); // port cho API Java
        final HyperGraph graph = new HyperGraph(DB_PATH);

        //Khai báo các model
        // List<User> users = graph.getAll(hg.type(User.class));

        // ==================== KHỞI TẠO DỮ LIỆU MẪU ====================
        //Chỉ khởi tạo khi db trống
        
        // Thêm Users mẫu
        seedUsers(graph);

        // Thêm NhaToChuc mẫu
        seedNhaToChuc(graph);

        // Thêm DiaDiem mẫu
        seedDiaDiem(graph);

        // Thêm Tour mẫu
        seedTours(graph);

        // Thêm DatTour mẫu
        seedDatTour(graph);

        // Thêm HoaDon mẫu
        seedHoaDon(graph);

        // Thêm DanhGia mẫu
        seedDanhGia(graph);
        
        //========================TEST API==========================
        get("/api/hello", (req, res) -> {
            res.type("application/json");
            Map<String, String> response = new HashMap<>();
            response.put("message", "Hello from Java API!");
            return new Gson().toJson(response);
        });

        //=========================USER==============================
        //POST
        post("/api/add", (req, res) -> {
            res.type("application/json");
            @SuppressWarnings("unchecked")
            Map<String, Object> data = new Gson().fromJson(req.body(), Map.class);
            String value = (String) data.get("value");
            Map<String, String> response = new HashMap<>();
            response.put("message", "Received: " + value);
            return new Gson().toJson(response);
        });

        before((req, res) -> {
            res.header("Access-Control-Allow-Origin", "*");
            res.header("Access-Control-Allow-Methods", "GET,POST,OPTIONS");
            res.header("Access-Control-Allow-Headers", "Content-Type");
        });
        options("/*", (req, res) -> "OK");

        //API xem danh sách user
        get("/api/users", (req, res) -> {
            res.type("application/json");
            List<User> users = graph.getAll(hg.type(User.class));
            return gson.toJson(users);
        });

        //API đăng ký
        post("/api/register", (req, res) -> {
            res.type("application/json");
            Map<String, Object> body = gson.fromJson(req.body(), Map.class);
            String name = (String) body.get("name");
            String email = (String) body.get("email");
            String password = (String) body.get("password");

            List<User> users = graph.getAll(hg.type(User.class));
            for (User u : users) {
                if (u.getEmail().equalsIgnoreCase(email)) {
                    res.status(400);
                    Map<String, String> err = new HashMap<>();
                    err.put("message", "Email đã tồn tại!");
                    return gson.toJson(err);
                }
            }

            User newUser = new User(name, email, password);
            graph.add(newUser);

            Map<String, Object> resp = new HashMap<>();
            resp.put("message", "Đăng ký thành công!");
            resp.put("user", newUser);
            return gson.toJson(resp);
        });

        //API đăng nhập
        post("/api/login", (req, res) -> {
            res.type("application/json");
            Map<String, Object> body = gson.fromJson(req.body(), Map.class);
            String email = (String) body.get("email");
            String password = (String) body.get("password");

            List<User> users = graph.getAll(hg.type(User.class));
            for (User u : users) {
                if (u.getEmail().equalsIgnoreCase(email) && u.getPassword().equals(password)) {
                    currentUser = u; // ✅ lưu user đăng nhập tạm thời
                    Map<String, Object> resp = new HashMap<>();
                    resp.put("message", "Đăng nhập thành công!");

                    Map<String, String> userData = new HashMap<>();
                    userData.put("name", u.getName());
                    userData.put("email", u.getEmail());
                    resp.put("user", userData);

                    return gson.toJson(resp);
                }
            }

            res.status(401);
            Map<String, String> err = new HashMap<>();
            err.put("message", "Sai email hoặc mật khẩu!");
            return gson.toJson(err);
        });

        //API lấy user hiện tại
        get("/api/currentUser", (req, res) -> {
            res.type("application/json");
            if (currentUser != null) {
                Map<String, Object> resp = new HashMap<>();
                resp.put("loggedIn", true);

                Map<String, String> userData = new HashMap<>();
                userData.put("name", currentUser.getName());
                userData.put("email", currentUser.getEmail());
                resp.put("user", userData);

                return gson.toJson(resp);
            } else {
                res.status(401);
                Map<String, String> err = new HashMap<>();
                err.put("message", "Chưa đăng nhập");
                return gson.toJson(err);
            }
        });

        //API đăng xuất
        post("/api/logout", (req, res) -> {
            currentUser = null;
            Map<String, String> msg = new HashMap<>();
            msg.put("message", "Đã đăng xuất");
            return gson.toJson(msg);
        });

        // ========== API TOUR ==========
        get("/api/tours", (req, res) -> {
            res.type("application/json");
            List<Tour> tours = graph.getAll(hg.type(Tour.class));
            return gson.toJson(tours);
        });

        post("/api/tours", (req, res) -> {
            res.type("application/json");
            Tour tour = gson.fromJson(req.body(), Tour.class);
            graph.add(tour);
            Map<String, String> resp = new HashMap<>();
            resp.put("message", "Đã thêm tour mới!");
            return gson.toJson(resp);
        });

        // ========== API DIA DIEM ==========
        get("/api/diadiem", (req, res) -> {
            res.type("application/json");
            List<DiaDiem> list = graph.getAll(hg.type(DiaDiem.class));
            return gson.toJson(list);
        });

        post("/api/diadiem", (req, res) -> {
            res.type("application/json");
            DiaDiem dd = gson.fromJson(req.body(), DiaDiem.class);
            graph.add(dd);
            Map<String, String> resp = new HashMap<>();
            resp.put("message", "Đã thêm địa điểm mới!");
            return gson.toJson(resp);
        });

        // ========== API NHA TO CHUC ==========
        get("/api/nhatochuc", (req, res) -> {
            res.type("application/json");
            List<NhaToChuc> list = graph.getAll(hg.type(NhaToChuc.class));
            return gson.toJson(list);
        });

        post("/api/nhatochuc", (req, res) -> {
            res.type("application/json");
            NhaToChuc ntc = gson.fromJson(req.body(), NhaToChuc.class);
            graph.add(ntc);
            Map<String, String> resp = new HashMap<>();
            resp.put("message", "Đã thêm nhà tổ chức mới!");
            return gson.toJson(resp);
        });

        // ========== API DAT TOUR ==========
        get("/api/dattour", (req, res) -> {
            res.type("application/json");
            List<DatTour> list = graph.getAll(hg.type(DatTour.class));
            return gson.toJson(list);
        });

        post("/api/dattour", (req, res) -> {
            res.type("application/json");
            DatTour dt = gson.fromJson(req.body(), DatTour.class);
            graph.add(dt);
            Map<String, String> resp = new HashMap<>();
            resp.put("message", "Đã đặt tour thành công!");
            return gson.toJson(resp);
        });

        // ========== API HOA DON ==========
        get("/api/hoadon", (req, res) -> {
            res.type("application/json");
            List<HoaDon> list = graph.getAll(hg.type(HoaDon.class));
            return gson.toJson(list);
        });

        post("/api/hoadon", (req, res) -> {
            res.type("application/json");
            HoaDon hd = gson.fromJson(req.body(), HoaDon.class);
            graph.add(hd);
            Map<String, String> resp = new HashMap<>();
            resp.put("message", "Đã thêm hóa đơn!");
            return gson.toJson(resp);
        });

        // ========== API DANH GIA ==========
        get("/api/danhgia", (req, res) -> {
            res.type("application/json");
            List<DanhGia> list = graph.getAll(hg.type(DanhGia.class));
            return gson.toJson(list);
        });

        post("/api/danhgia", (req, res) -> {
            res.type("application/json");
            DanhGia dg = gson.fromJson(req.body(), DanhGia.class);
            graph.add(dg);
            Map<String, String> resp = new HashMap<>();
            resp.put("message", "Đã thêm đánh giá!");
            return gson.toJson(resp);
        });

        //=========================KẾT HỢP==============================
        // API: Lấy chi tiết 1 tour kèm thông tin nhà tổ chức
        get("/api/tour/:id", (req, res) -> {
            res.type("application/json");
            String id = req.params(":id");

            // Lấy danh sách tour và nhà tổ chức
            List<Tour> tours = graph.getAll(hg.type(Tour.class));
            List<NhaToChuc> nhaToChucs = graph.getAll(hg.type(NhaToChuc.class));

            for (Tour t : tours) {
                if (t.getId() != null && t.getId().equalsIgnoreCase(id)) {

                    // Tìm nhà tổ chức tương ứng
                    NhaToChuc foundNTC = null;
                    for (NhaToChuc ntc : nhaToChucs) {
                        if (ntc.getId() != null && ntc.getId().equalsIgnoreCase(t.getNhaToChucId())) {
                            foundNTC = ntc;
                            break;
                        }
                    }

                    Map<String, Object> tour = new HashMap<>();
                    tour.put("id", t.getId());
                    tour.put("tenTour", t.getTenTour());
                    tour.put("moTa", t.getMoTa());
                    tour.put("gia", t.getGia());
                    tour.put("thoiGian", t.getThoiGian());
                    tour.put("noiKhoiHanh", t.getDiemKhoiHanh());
                    tour.put("diemDen", t.getDiemDen());
                    tour.put("phuongTien", t.getPhuongTien());
                    tour.put("hinhAnh", t.getHinhAnh());

                    if (foundNTC != null) {
                        Map<String, Object> ntcInfo = new HashMap<>();
                        ntcInfo.put("id", foundNTC.getId());
                        ntcInfo.put("ten", foundNTC.getTen());
                        ntcInfo.put("email", foundNTC.getEmail());
                        ntcInfo.put("soDienThoai", foundNTC.getSdt());
                        ntcInfo.put("diaChi", foundNTC.getDiaChi());
                        tour.put("nhaToChuc", ntcInfo);
                    } else {
                        tour.put("nhaToChuc", null);
                    }

                    return gson.toJson(tour);
                }
            }

            res.status(404);
            Map<String, String> err = new HashMap<>();
            err.put("message", "Tour không tìm thấy");
            return gson.toJson(err);
        });


        //==========================ĐÓNG SERVER=========================
        //Đóng DB khi tắt server
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            graph.close();
            System.out.println("Đã đóng HyperGraphDB");
        }));
    }
}

//GET /api/hell0 -> gửi về dòng Hello from Java