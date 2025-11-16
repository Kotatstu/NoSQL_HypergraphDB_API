package hypergraphdb.api;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.HyperGraph;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializer;

import hypergraphdb.models.DanhGia;
import hypergraphdb.models.DatTour;
import hypergraphdb.models.DiaDiem;
import hypergraphdb.models.HoaDon;
import hypergraphdb.models.NhaToChuc;
import hypergraphdb.models.Tour;
import hypergraphdb.models.User;
import static spark.Spark.before;
import static spark.Spark.delete;
import static spark.Spark.get;
import static spark.Spark.options;
import static spark.Spark.port;
import static spark.Spark.post;
import static spark.Spark.put;

public class ApiServer {

    private static final String DB_PATH = "db/mainDB";

    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDate.class,
                    (JsonSerializer<LocalDate>) (src, type, ctx) -> new JsonPrimitive(src.toString()))
            .registerTypeAdapter(LocalDate.class,
                    (JsonDeserializer<LocalDate>) (json, type, ctx) -> LocalDate.parse(json.getAsString()))
            .registerTypeAdapter(LocalDateTime.class,
                    (JsonSerializer<LocalDateTime>) (src, type, ctx) -> new JsonPrimitive(src.toString()))
            .registerTypeAdapter(LocalDateTime.class,
                    (JsonDeserializer<LocalDateTime>) (json, type, ctx) -> LocalDateTime.parse(json.getAsString()))
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    // Lưu user đăng nhập tạm thời
    private static User currentUser = null;

    // Hàm hỗ trợ bỏ dấu tiếng khi tìm kiếm
    public static String unaccent(String text) {
        if (text == null)
            return "";
        return Normalizer.normalize(text, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .replaceAll("đ", "d")
                .replaceAll("Đ", "D")
                .toLowerCase();
    }

    // ========================= Các hàm seed dữ liệu ==========================
    // seed nhà tổ chức
    private static void seedNhaToChuc(HyperGraph graph) {
        List<NhaToChuc> existing = graph.getAll(hg.type(NhaToChuc.class));
        if (existing.isEmpty()) {
            NhaToChuc n1 = new NhaToChuc(
                    "NTC001",
                    "Công ty du lịch ABC",
                    "abc.travel@gmail.com",
                    "0905123456",
                    "12 Nguyễn Huệ, Quận 1, TP.HCM",
                    "Chuyên tổ chức tour trong nước và quốc tế.");

            NhaToChuc n2 = new NhaToChuc(
                    "NTC002",
                    "TravelNow",
                    "contact@travelnow.vn",
                    "0987123456",
                    "25 Trần Hưng Đạo, Hà Nội",
                    "Công ty chuyên về tour nghỉ dưỡng cao cấp.");

            NhaToChuc n3 = new NhaToChuc(
                    "NTC003",
                    "AmazingTrip",
                    "info@amazingtrip.vn",
                    "0978123123",
                    "45 Bạch Đằng, Đà Nẵng",
                    "Mang đến trải nghiệm du lịch tuyệt vời khắp Việt Nam.");

            graph.add(n1);
            graph.add(n2);
            graph.add(n3);
        }
    }

    // Seed users
    public static void seedUsers(HyperGraph graph) {
        // Kiểm tra nếu đã có dữ liệu thì bỏ qua để tránh trùng
        List<User> existing = graph.getAll(hg.type(User.class));
        if (!existing.isEmpty()) {
            return;
        }

        List<User> users = new ArrayList<>();
        users.add(new User("Nguyễn Hoàng Long", "long@gmail.com", "123456", "admin"));
        users.add(new User("Tô Minh Lợi", "loi@gmail.com", "123456", "admin"));

        for (User user : users) {
            graph.add(user);
        }
    }

    // seed tour
    public static void seedTours(HyperGraph graph) {
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
                "NTC001"));

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
                "NTC002"));

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
                "NTC001"));

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
                "NTC003"));

        for (Tour tour : tours) {
            graph.add(tour);
        }
    }

    // seed Địa Điểm
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
                "halong.jpg"));

        diaDiems.add(new DiaDiem(
                "DD002",
                "Thành phố Đà Lạt",
                "Thành phố ngàn hoa với khí hậu mát mẻ quanh năm, điểm đến lý tưởng cho du lịch nghỉ dưỡng.",
                "dalat.jpg"));

        diaDiems.add(new DiaDiem(
                "DD003",
                "Đảo Phú Quốc",
                "Hòn đảo lớn nhất Việt Nam, nổi tiếng với biển xanh, cát trắng và đặc sản nước mắm.",
                "phuquoc.jpg"));

        diaDiems.add(new DiaDiem(
                "DD004",
                "Thị trấn Sapa",
                "Điểm đến vùng cao với ruộng bậc thang và đỉnh Fansipan – nóc nhà Đông Dương.",
                "sapa.jpg"));

        diaDiems.add(new DiaDiem(
                "DD005",
                "Phố cổ Hội An",
                "Di sản văn hóa thế giới với kiến trúc cổ kính và đèn lồng rực rỡ về đêm.",
                "hoian.jpg"));

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

        // Dữ liệu mới với ngày là String
        datTours.add(new DatTour(
                "DT001",
                "long@gmail.com",
                "T001",
                2,
                "2025-11-01", // Đổi thành chuỗi
                "2025-12-10", // Đổi thành chuỗi
                "Paid"));

        datTours.add(new DatTour(
                "DT002",
                "loi@gmail.com",
                "T002",
                4,
                "2025-11-03", // Đổi thành chuỗi
                "2026-01-05", // Đổi thành chuỗi
                "Pending"));

        datTours.add(new DatTour(
                "DT003",
                "long@gmail.com",
                "T003",
                3,
                "2025-11-04", // Đổi thành chuỗi
                "2026-02-15", // Đổi thành chuỗi
                "Cancelled"));

        datTours.add(new DatTour(
                "DT004",
                "loi@gmail.com",
                "T004",
                5,
                "2025-10-20", // Đổi thành chuỗi
                "2025-12-25", // Đổi thành chuỗi
                "Paid"));

        datTours.add(new DatTour(
                "DT005",
                "long@gmail.com",
                "T005",
                2,
                "2025-11-05", // Đổi thành chuỗi
                "2026-03-01", // Đổi thành chuỗi
                "Pending"));

        // Lưu các đối tượng DatTour vào graph
        for (DatTour dt : datTours) {
            graph.add(dt);
        }
    }

    // seed Hóa Đơn
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
                "2025-11-01T10:30:00"));

        hoaDons.add(new HoaDon(
                "HD002",
                "DT002",
                12000000,
                "Card",
                "Unpaid",
                "2025-11-03T14:45:00"));

        hoaDons.add(new HoaDon(
                "HD003",
                "DT003",
                7500000,
                "Cash",
                "Refunded",
                "2025-11-04T09:15:00"));

        hoaDons.add(new HoaDon(
                "HD004",
                "DT004",
                15000000,
                "Card",
                "Paid",
                "2025-10-20T16:10:00"));

        hoaDons.add(new HoaDon(
                "HD005",
                "DT005",
                9500000,
                "MoMo",
                "Unpaid",
                "2025-11-05T11:05:00"));

        for (HoaDon hd : hoaDons) {
            graph.add(hd);
        }
    }

    // seed Đánh giá
    public static void seedDanhGia(HyperGraph graph) {
        // Kiểm tra nếu đã có dữ liệu thì bỏ qua
        List<DanhGia> existing = graph.getAll(hg.type(DanhGia.class));
        if (!existing.isEmpty()) {
            return;
        }

        List<DanhGia> danhGias = new ArrayList<>();

        danhGias.add(new DanhGia(
                "DG001",
                "long@gmail.com", // khachHangEmail
                "T001", // tourId
                5, // diemDanhGia
                "Tour rất tuyệt vời, hướng dẫn viên thân thiện!",
                "2025-11-02T09:15" // Ngày đánh giá dưới dạng String
        ));

        danhGias.add(new DanhGia(
                "DG002",
                "loi@gmail.com",
                "T002",
                4,
                "Lịch trình hợp lý, đồ ăn ngon nhưng khách sạn hơi xa trung tâm.",
                "2025-11-04T14:20"));

        danhGias.add(new DanhGia(
                "DG003",
                "long@gmail.com",
                "T003",
                3,
                "Tour ổn, nhưng phương tiện di chuyển chưa thoải mái.",
                "2025-11-06T10:45"));

        danhGias.add(new DanhGia(
                "DG004",
                "loi@gmail.com",
                "T004",
                5,
                "Tổ chức rất chuyên nghiệp, tôi sẽ quay lại!",
                "2025-11-07T18:00"));

        danhGias.add(new DanhGia(
                "DG005",
                "long@gmail.com",
                "T005",
                2,
                "Chuyến đi không như mong đợi, thời tiết xấu và hướng dẫn viên thiếu nhiệt tình.",
                "2025-11-08T11:30"));

        for (DanhGia dg : danhGias) {
            graph.add(dg);
        }
    }

    public static void main(String[] args) {
        port(8080); // port cho API Java
        final HyperGraph graph = new HyperGraph(DB_PATH);

        // Khai báo các model
        // List<User> users = graph.getAll(hg.type(User.class));

        // ==================== KHỞI TẠO DỮ LIỆU MẪU ====================
        // Chỉ khởi tạo khi db trống

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

        // ========================TEST API==========================
        get("/api/hello", (req, res) -> {
            res.type("application/json; charset=UTF-8");
            res.raw().setCharacterEncoding("UTF-8");
            Map<String, String> response = new HashMap<>();
            response.put("message", "Hello from Java API!");
            return new Gson().toJson(response);
        });

        // =========================USER==============================
        // POST
        post("/api/add", (req, res) -> {
            res.type("application/json; charset=UTF-8");
            res.raw().setCharacterEncoding("UTF-8");
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
            res.header("Access-Control-Allow-Headers", "Content-Type Authorization");
        });
        options("/*", (req, res) -> "OK");

        // API xem danh sách user
        get("/api/users", (req, res) -> {
            res.type("application/json; charset=UTF-8");
            res.raw().setCharacterEncoding("UTF-8");

            List<User> users = graph.getAll(hg.type(User.class));
            return gson.toJson(users);
        });

        // API GET: Lấy thông tin người dùng theo email
        get("/api/user/:email", (req, res) -> {
            res.type("application/json; charset=UTF-8");
            res.raw().setCharacterEncoding("UTF-8");
            String email = req.params(":email");

            // Lấy danh sách người dùng từ database
            List<User> users = graph.getAll(hg.type(User.class));

            // Tìm người dùng theo email
            for (User user : users) {
                if (user.getEmail().equalsIgnoreCase(email)) {
                    // Trả về thông tin người dùng dưới dạng JSON
                    return gson.toJson(user);
                }
            }

            // Nếu không tìm thấy người dùng
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Không tìm thấy người dùng với email: " + email);
            return gson.toJson(errorResponse);
        });

        // API PUT: Cập nhật thông tin người dùng theo email
        put("/api/user/:email", (req, res) -> {
            res.type("application/json; charset=UTF-8");
            res.raw().setCharacterEncoding("UTF-8");
            String email = req.params(":email");

            // Parse dữ liệu người dùng từ request body
            User updatedUser = gson.fromJson(req.body(), User.class);

            // Lấy danh sách người dùng từ database
            List<User> users = graph.getAll(hg.type(User.class));
            boolean isUserFound = false;

            // Tìm người dùng theo email và cập nhật thông tin
            for (User user : users) {
                if (user.getEmail().equalsIgnoreCase(email)) {
                    isUserFound = true;

                    // Cập nhật thông tin người dùng nếu có thay đổi
                    if (updatedUser.getName() != null) {
                        user.setName(updatedUser.getName());
                    }
                    if (updatedUser.getPassword() != null) {
                        user.setPassword(updatedUser.getPassword());
                    }
                    if (updatedUser.getRole() != null) {
                        user.setRole(updatedUser.getRole());
                    }

                    // Cập nhật lại thông tin người dùng trong database
                    graph.update(user);

                    Map<String, String> response = new HashMap<>();
                    response.put("message", "Cập nhật người dùng thành công!");
                    return gson.toJson(response);
                }
            }

            // Nếu không tìm thấy người dùng
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Không tìm thấy người dùng với email: " + email);
            return gson.toJson(errorResponse);
        });

        // API DELETE: Xóa người dùng theo email
        delete("/api/user/:email", (req, res) -> {
            res.type("application/json; charset=UTF-8");
            res.raw().setCharacterEncoding("UTF-8");
            String email = req.params(":email");

            // Lấy danh sách người dùng từ database
            List<User> users = graph.getAll(hg.type(User.class));
            boolean isUserFound = false;

            // Tìm người dùng theo email và xóa
            for (User user : users) {
                if (user.getEmail().equalsIgnoreCase(email)) {
                    isUserFound = true;

                    // Xóa người dùng
                    HGHandle handle = graph.getHandle(user);
                    graph.remove(handle);

                    Map<String, String> response = new HashMap<>();
                    response.put("message", "Đã xóa người dùng thành công!");
                    return gson.toJson(response);
                }
            }

            // Nếu không tìm thấy người dùng
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Không tìm thấy người dùng với email: " + email);
            return gson.toJson(errorResponse);
        });

        // API đăng ký
        post("/api/register", (req, res) -> {
            res.type("application/json; charset=UTF-8");
            res.raw().setCharacterEncoding("UTF-8");
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

        // API đăng nhập
        post("/api/login", (req, res) -> {
            res.type("application/json; charset=UTF-8");
            res.raw().setCharacterEncoding("UTF-8");
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
                    userData.put("role", u.getRole());
                    resp.put("user", userData);

                    return gson.toJson(resp);
                }
            }

            res.status(401);
            Map<String, String> err = new HashMap<>();
            err.put("message", "Sai email hoặc mật khẩu!");
            return gson.toJson(err);
        });

        // API lấy user hiện tại
        get("/api/currentUser", (req, res) -> {
            res.type("application/json; charset=UTF-8");
            res.raw().setCharacterEncoding("UTF-8");
            if (currentUser != null) {
                Map<String, Object> resp = new HashMap<>();
                resp.put("loggedIn", true);

                Map<String, String> userData = new HashMap<>();
                userData.put("name", currentUser.getName());
                userData.put("email", currentUser.getEmail());
                userData.put("role", currentUser.getRole());
                resp.put("user", userData);

                return gson.toJson(resp);
            } else {
                res.status(401);
                Map<String, String> err = new HashMap<>();
                err.put("message", "Chưa đăng nhập");
                return gson.toJson(err);
            }
        });

        // API đăng xuất
        post("/api/logout", (req, res) -> {
            currentUser = null;
            Map<String, String> msg = new HashMap<>();
            msg.put("message", "Đã đăng xuất");
            return gson.toJson(msg);
        });

        // ========== API TOUR ==========
        get("/api/tours", (req, res) -> {
            res.type("application/json; charset=UTF-8");
            res.raw().setCharacterEncoding("UTF-8");
            List<Tour> tours = graph.getAll(hg.type(Tour.class));
            return gson.toJson(tours);
        });

        post("/api/tours", (req, res) -> {
            res.type("application/json; charset=UTF-8");
            res.raw().setCharacterEncoding("UTF-8");
            Tour tour = gson.fromJson(req.body(), Tour.class);
            graph.add(tour);
            Map<String, String> resp = new HashMap<>();
            resp.put("message", "Đã thêm tour mới!");
            return gson.toJson(resp);
        });

        // Lấy tour theo ID
        get("/api/tours/:id", (req, res) -> {
            res.type("application/json; charset=UTF-8");
            res.raw().setCharacterEncoding("UTF-8");

            // Lấy ID từ URL
            String id = req.params(":id");

            // Lấy tất cả các tour từ database
            List<Tour> tours = graph.getAll(hg.type(Tour.class));

            // Tìm tour có ID khớp với ID truyền vào
            for (Tour tour : tours) {
                if (tour.getId() != null && tour.getId().equalsIgnoreCase(id)) {
                    // Nếu tìm thấy tour, trả về thông tin tour dưới dạng JSON
                    Map<String, Object> tourData = new HashMap<>();
                    tourData.put("id", tour.getId());
                    tourData.put("tenTour", tour.getTenTour());
                    tourData.put("moTa", tour.getMoTa());
                    tourData.put("gia", tour.getGia());
                    tourData.put("thoiGian", tour.getThoiGian());
                    tourData.put("diemKhoiHanh", tour.getDiemKhoiHanh());
                    tourData.put("diemDen", tour.getDiemDen());
                    tourData.put("phuongTien", tour.getPhuongTien());
                    tourData.put("hinhAnh", tour.getHinhAnh());
                    tourData.put("nhaToChucId", tour.getNhaToChucId()); // Thêm trường nhaToChucId

                    return gson.toJson(tourData);
                }
            }

            // Nếu không tìm thấy tour, trả về lỗi 404
            res.status(404);
            Map<String, String> err = new HashMap<>();
            err.put("message", "Tour không tìm thấy");
            return gson.toJson(err);
        });

        // Cập nhật thông tin Tour
        put("/api/tours/:id", (req, res) -> {
            res.type("application/json; charset=UTF-8");
            res.raw().setCharacterEncoding("UTF-8");
            String id = req.params(":id");

            // Parse thông tin Tour từ request body
            Tour updatedTour = gson.fromJson(req.body(), Tour.class);

            // Lấy danh sách tours và tìm tour theo ID
            List<Tour> tours = graph.getAll(hg.type(Tour.class));
            boolean isTourFound = false;

            for (Tour tour : tours) {
                if (tour.getId() != null && tour.getId().equalsIgnoreCase(id)) {
                    isTourFound = true;

                    // Cập nhật các trường thông tin tour nếu có
                    if (updatedTour.getTenTour() != null)
                        tour.setTenTour(updatedTour.getTenTour());
                    if (updatedTour.getMoTa() != null)
                        tour.setMoTa(updatedTour.getMoTa());
                    if (updatedTour.getGia() > 0)
                        tour.setGia(updatedTour.getGia());
                    if (updatedTour.getThoiGian() != null)
                        tour.setThoiGian(updatedTour.getThoiGian());
                    if (updatedTour.getDiemKhoiHanh() != null)
                        tour.setDiemKhoiHanh(updatedTour.getDiemKhoiHanh());
                    if (updatedTour.getDiemDen() != null)
                        tour.setDiemDen(updatedTour.getDiemDen());
                    if (updatedTour.getPhuongTien() != null)
                        tour.setPhuongTien(updatedTour.getPhuongTien());
                    if (updatedTour.getHinhAnh() != null)
                        tour.setHinhAnh(updatedTour.getHinhAnh());
                    if (updatedTour.getNhaToChucId() != null)
                        tour.setNhaToChucId(updatedTour.getNhaToChucId()); // Thêm nhaToChucId vào

                    // Lưu vào database
                    graph.update(tour);

                    Map<String, String> response = new HashMap<>();
                    response.put("message", "Cập nhật tour thành công!");
                    return gson.toJson(response);
                }
            }

            if (!isTourFound) {
                res.status(404);
                Map<String, String> err = new HashMap<>();
                err.put("message", "Tour không tìm thấy");
                return gson.toJson(err);
            }
            return null;
        });

        // Xóa tour theo ID
        delete("/api/tours/:id", (req, res) -> {
            res.type("application/json; charset=UTF-8");
            res.raw().setCharacterEncoding("UTF-8");
            String id = req.params(":id");

            // Tìm tour theo ID
            List<Tour> tours = graph.getAll(hg.type(Tour.class));
            boolean isTourFound = false;

            for (Tour tour : tours) {
                if (tour.getId() != null && tour.getId().equalsIgnoreCase(id)) {
                    // Xoá tour nếu tìm thấy
                    HGHandle handle = graph.getHandle(tour);
                    graph.remove(handle);
                    isTourFound = true;
                    break;
                }
            }

            // Kiểm tra nếu không tìm thấy tour
            if (isTourFound) {
                Map<String, String> response = new HashMap<>();
                response.put("message", "Xóa tour thành công!");
                return gson.toJson(response);
            } else {
                res.status(404);
                Map<String, String> err = new HashMap<>();
                err.put("message", "Tour không tìm thấy");
                return gson.toJson(err);
            }
        });

        // Tìm kiếm theo tên tour
        get("/api/search", (req, res) -> {
            res.type("application/json; charset=UTF-8");
            res.raw().setCharacterEncoding("UTF-8");

            String keyword = req.queryParams("name");
            Map<String, Object> resp = new HashMap<>();

            if (keyword == null || keyword.trim().isEmpty()) {
                resp.put("status", "error");
                resp.put("message", "Thiếu tham số tìm kiếm 'name'");
                return gson.toJson(resp);
            }

            String normalizedKeyword = unaccent(keyword.trim().toLowerCase());

            List<Tour> tours = graph.getAll(hg.type(Tour.class));

            List<Tour> result = tours.stream()
                    .filter(t -> t.getTenTour() != null) // tránh null
                    .filter(t -> {
                        String title = unaccent(t.getTenTour().toLowerCase());
                        return title.contains(normalizedKeyword);
                    })
                    .collect(Collectors.toList());

            resp.put("status", "success");
            resp.put("count", result.size());
            resp.put("data", result);

            return gson.toJson(resp);
        });

        // ========== API DIA DIEM ==========
        get("/api/diadiem", (req, res) -> {
            res.type("application/json; charset=UTF-8");
            res.raw().setCharacterEncoding("UTF-8");
            List<DiaDiem> list = graph.getAll(hg.type(DiaDiem.class));
            return gson.toJson(list);
        });

        post("/api/diadiem", (req, res) -> {
            res.type("application/json; charset=UTF-8");
            res.raw().setCharacterEncoding("UTF-8");
            DiaDiem dd = gson.fromJson(req.body(), DiaDiem.class);
            graph.add(dd);
            Map<String, String> resp = new HashMap<>();
            resp.put("message", "Đã thêm địa điểm mới!");
            return gson.toJson(resp);
        });

        // Thêm endpoint GET cho địa điểm theo ID
        get("/api/diadiem/:id", (req, res) -> {
            res.type("application/json; charset=UTF-8");
            res.raw().setCharacterEncoding("UTF-8");

            String id = req.params(":id"); // Lấy ID từ đường dẫn

            // Tìm địa điểm theo ID trong cơ sở dữ liệu
            List<DiaDiem> diaDiems = graph.getAll(hg.type(DiaDiem.class)); // Lấy tất cả các địa điểm từ cơ sở dữ liệu

            DiaDiem foundDiaDiem = null;

            for (DiaDiem diaDiem : diaDiems) {
                if (diaDiem.getId() != null && diaDiem.getId().equals(id)) {
                    foundDiaDiem = diaDiem; // Nếu tìm thấy địa điểm theo ID
                    break;
                }
            }

            if (foundDiaDiem != null) {
                // Nếu tìm thấy địa điểm, trả về thông tin của địa điểm dưới dạng JSON
                return gson.toJson(foundDiaDiem);
            } else {
                // Nếu không tìm thấy, trả về mã lỗi 404
                res.status(404);
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Không tìm thấy địa điểm với ID: " + id);
                return gson.toJson(errorResponse);
            }
        });

        // API cập nhật địa điểm theo ID
        put("/api/diadiem/:id", (req, res) -> {
            res.type("application/json; charset=UTF-8");
            res.raw().setCharacterEncoding("UTF-8");
            String id = req.params(":id");

            // Parse thông tin Địa Điểm từ request body
            DiaDiem updatedDiaDiem = gson.fromJson(req.body(), DiaDiem.class);

            // Lấy danh sách địa điểm và tìm địa điểm theo ID
            List<DiaDiem> diaDiems = graph.getAll(hg.type(DiaDiem.class));
            boolean isDiaDiemFound = false;

            for (DiaDiem diaDiem : diaDiems) {
                if (diaDiem.getId() != null && diaDiem.getId().equals(id)) { // Dùng equals thay vì equalsIgnoreCase
                    isDiaDiemFound = true;

                    // Cập nhật các trường thông tin địa điểm nếu có
                    if (updatedDiaDiem.getTenDiaDiem() != null)
                        diaDiem.setTenDiaDiem(updatedDiaDiem.getTenDiaDiem());
                    if (updatedDiaDiem.getMoTa() != null)
                        diaDiem.setMoTa(updatedDiaDiem.getMoTa());
                    if (updatedDiaDiem.getHinhAnh() != null)
                        diaDiem.setHinhAnh(updatedDiaDiem.getHinhAnh());

                    // Lưu vào database
                    graph.update(diaDiem);

                    Map<String, String> response = new HashMap<>();
                    response.put("message", "Cập nhật địa điểm thành công!");
                    return gson.toJson(response); // Trả về phản hồi sau khi cập nhật thành công
                }
            }

            // Nếu không tìm thấy địa điểm với ID tương ứng
            Map<String, String> response = new HashMap<>();
            response.put("message", "Địa điểm không tồn tại!");
            return gson.toJson(response);
        });

        delete("/api/diadiem/:id", (req, res) -> {
            res.type("application/json; charset=UTF-8");
            res.raw().setCharacterEncoding("UTF-8");
            String id = req.params(":id");

            // Tìm địa điểm theo ID
            List<DiaDiem> diaDiems = graph.getAll(hg.type(DiaDiem.class));
            boolean isDiaDiemFound = false;

            for (DiaDiem diaDiem : diaDiems) {
                if (diaDiem.getId() != null && diaDiem.getId().equalsIgnoreCase(id)) {
                    isDiaDiemFound = true;

                    // Xóa địa điểm: lấy HGHandle rồi remove bằng handle
                    HGHandle handle = graph.getHandle(diaDiem);
                    if (handle != null) {
                        graph.remove(handle);
                    }

                    // Trả về kết quả thành công
                    Map<String, String> response = new HashMap<>();
                    response.put("message", "Đã xóa địa điểm thành công!");
                    return gson.toJson(response);
                }
            }

            // Nếu không tìm thấy địa điểm, trả về lỗi
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Không tìm thấy địa điểm với ID: " + id);
            return gson.toJson(errorResponse);
        });

        // ========== API NHA TO CHUC ==========
        get("/api/nhatochuc", (req, res) -> {
            res.type("application/json; charset=UTF-8");
            res.raw().setCharacterEncoding("UTF-8");
            List<NhaToChuc> list = graph.getAll(hg.type(NhaToChuc.class));
            return gson.toJson(list);
        });

        post("/api/nhatochuc", (req, res) -> {
            res.type("application/json; charset=UTF-8");
            res.raw().setCharacterEncoding("UTF-8");
            NhaToChuc ntc = gson.fromJson(req.body(), NhaToChuc.class);
            graph.add(ntc);
            Map<String, String> resp = new HashMap<>();
            resp.put("message", "Đã thêm nhà tổ chức mới!");
            return gson.toJson(resp);
        });

        // Thêm endpoint GET cho nhà tổ chức theo ID
        get("/api/nhatochuc/:id", (req, res) -> {
            res.type("application/json; charset=UTF-8");
            res.raw().setCharacterEncoding("UTF-8");

            String id = req.params(":id"); // Lấy ID từ đường dẫn

            // Tìm nhà tổ chức theo ID trong cơ sở dữ liệu
            List<NhaToChuc> nhaToChucs = graph.getAll(hg.type(NhaToChuc.class)); // Lấy tất cả nhà tổ chức từ cơ sở dữ
                                                                                 // liệu

            NhaToChuc foundNhaToChuc = null;

            for (NhaToChuc nhaToChuc : nhaToChucs) {
                if (nhaToChuc.getId() != null && nhaToChuc.getId().equals(id)) {
                    foundNhaToChuc = nhaToChuc; // Nếu tìm thấy nhà tổ chức theo ID
                    break;
                }
            }

            if (foundNhaToChuc != null) {
                // Nếu tìm thấy nhà tổ chức, trả về thông tin của nhà tổ chức dưới dạng JSON
                return gson.toJson(foundNhaToChuc);
            } else {
                // Nếu không tìm thấy, trả về mã lỗi 404
                res.status(404);
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Không tìm thấy nhà tổ chức với ID: " + id);
                return gson.toJson(errorResponse);
            }
        });

        // API cập nhật nhà tổ chức theo ID
        put("/api/nhatochuc/:id", (req, res) -> {
            res.type("application/json; charset=UTF-8");
            res.raw().setCharacterEncoding("UTF-8");
            String id = req.params(":id");
            // Parse thông tin Nhà Tổ Chức từ request body
            NhaToChuc updatedNhaToChuc = gson.fromJson(req.body(), NhaToChuc.class);

            // Lấy danh sách nhà tổ chức và tìm nhà tổ chức theo ID
            List<NhaToChuc> nhaToChucs = graph.getAll(hg.type(NhaToChuc.class));
            boolean isNhaToChucFound = false;

            for (NhaToChuc nhaToChuc : nhaToChucs) {
                if (nhaToChuc.getId() != null && nhaToChuc.getId().equals(id)) {
                    isNhaToChucFound = true;

                    // Cập nhật các trường thông tin nhà tổ chức nếu có
                    if (updatedNhaToChuc.getTen() != null)
                        nhaToChuc.setTen(updatedNhaToChuc.getTen());
                    if (updatedNhaToChuc.getEmail() != null)
                        nhaToChuc.setEmail(updatedNhaToChuc.getEmail());
                    if (updatedNhaToChuc.getSdt() != null)
                        nhaToChuc.setSdt(updatedNhaToChuc.getSdt());
                    if (updatedNhaToChuc.getDiaChi() != null)
                        nhaToChuc.setDiaChi(updatedNhaToChuc.getDiaChi());
                    if (updatedNhaToChuc.getMoTa() != null)
                        nhaToChuc.setMoTa(updatedNhaToChuc.getMoTa());

                    // Lưu vào cơ sở dữ liệu
                    graph.update(nhaToChuc);

                    Map<String, String> response = new HashMap<>();
                    response.put("message", "Cập nhật nhà tổ chức thành công!");
                    return gson.toJson(response); // Trả về phản hồi sau khi cập nhật thành công
                }
            }

            // Nếu không tìm thấy nhà tổ chức với ID tương ứng
            Map<String, String> response = new HashMap<>();
            response.put("message", "Nhà tổ chức không tồn tại!");
            return gson.toJson(response);
        });

        // Xóa nhà tổ chức theo ID
        delete("/api/nhatochuc/:id", (req, res) -> {
            res.type("application/json; charset=UTF-8");
            res.raw().setCharacterEncoding("UTF-8");
            String id = req.params(":id");

            // Tìm nhà tổ chức theo ID
            List<NhaToChuc> nhaToChucs = graph.getAll(hg.type(NhaToChuc.class));
            boolean isNhaToChucFound = false;

            for (NhaToChuc nhaToChuc : nhaToChucs) {
                if (nhaToChuc.getId() != null && nhaToChuc.getId().equals(id)) {
                    isNhaToChucFound = true;

                    // Xoá nhà tổ chức: lấy HGHandle rồi remove bằng handle
                    HGHandle handle = graph.getHandle(nhaToChuc);
                    if (handle != null) {
                        graph.remove(handle);
                    }

                    // Trả về kết quả thành công
                    Map<String, String> response = new HashMap<>();
                    response.put("message", "Đã xóa nhà tổ chức thành công!");
                    return gson.toJson(response);
                }
            }

            // Nếu không tìm thấy nhà tổ chức, trả về lỗi
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Không tìm thấy nhà tổ chức với ID: " + id);
            return gson.toJson(errorResponse);
        });

        // ========== API DAT TOUR ==========
        get("/api/dattour", (req, res) -> {
            res.type("application/json; charset=UTF-8");
            res.raw().setCharacterEncoding("UTF-8");
            List<DatTour> list = graph.getAll(hg.type(DatTour.class));
            return gson.toJson(list);
        });

        post("/api/dattour", (req, res) -> {
            res.type("application/json; charset=UTF-8");
            res.raw().setCharacterEncoding("UTF-8");
            Map<String, Object> resp = new HashMap<>();

            try {
                // Debug để kiểm tra JSON thực nhận
                String body = req.body();
                System.out.println("=== Body nhận được từ Laravel ===");
                System.out.println(body);

                // Parse JSON thành DatTour
                DatTour dt = gson.fromJson(body, DatTour.class);

                if (dt == null) {
                    res.status(400);
                    resp.put("message", "Không thể parse dữ liệu JSON (null)!");
                    return gson.toJson(resp);
                }

                // Kiểm tra dữ liệu đầu vào
                if (dt.getKhachHangEmail() == null || dt.getKhachHangEmail().isEmpty()) {
                    res.status(400);
                    resp.put("message", "Thiếu thông tin khách hàng (email)!");
                    return gson.toJson(resp);
                }

                if (dt.getTourId() == null || dt.getTourId().isEmpty()) {
                    res.status(400);
                    resp.put("message", "Thiếu mã tour!");
                    return gson.toJson(resp);
                }

                // Tự sinh ID: DT001, DT002...
                List<DatTour> existingTours = graph.getAll(hg.type(DatTour.class));
                int nextId = 1;
                for (DatTour existing : existingTours) {
                    String existingId = existing.getId();
                    if (existingId != null && existingId.startsWith("DT")) {
                        try {
                            int num = Integer.parseInt(existingId.substring(2));
                            if (num >= nextId)
                                nextId = num + 1;
                        } catch (Exception ignored) {
                        }
                    }
                }
                String newId = String.format("DT%03d", nextId);
                dt.setId(newId);

                // Ngày đặt mặc định = hôm nay nếu null
                if (dt.getNgayDat() == null || dt.getNgayDat().isEmpty()) {
                    dt.setNgayDat(LocalDate.now().toString()); // Convert thành chuỗi
                }

                // Trạng thái mặc định nếu null
                if (dt.getTrangThai() == null || dt.getTrangThai().isEmpty()) {
                    dt.setTrangThai("Pending");
                }

                // Lưu vào HyperGraphDB
                graph.add(dt);

                // Trả về phản hồi thành công
                res.status(200);
                resp.put("message", "Đặt tour thành công!");
                resp.put("newId", newId);
                resp.put("tourId", dt.getTourId());
                resp.put("khachHangEmail", dt.getKhachHangEmail());
                resp.put("ngayDat", dt.getNgayDat()); // Thêm ngày đặt
                resp.put("trangThai", dt.getTrangThai()); // Thêm trạng thái
                return gson.toJson(resp);

            } catch (Exception e) {
                e.printStackTrace();
                res.status(500);
                resp.put("message", "Lỗi khi xử lý yêu cầu: " + e.getMessage());
                return gson.toJson(resp);
            }
        });

        get("/api/dattour/:email", (req, res) -> {
            res.type("application/json; charset=UTF-8");
            res.raw().setCharacterEncoding("UTF-8");
            Map<String, Object> resp = new HashMap<>();

            try {
                String email = req.params(":email");

                if (email == null || email.isEmpty()) {
                    res.status(400);
                    resp.put("message", "Thiếu email người dùng!");
                    return new Gson().toJson(resp);
                }

                // Lấy tất cả DatTour từ HyperGraphDB
                List<DatTour> allTours = graph.getAll(hg.type(DatTour.class));

                // Lọc theo email
                List<DatTour> userTours = allTours.stream()
                        .filter(dt -> dt.getKhachHangEmail() != null &&
                                dt.getKhachHangEmail().equalsIgnoreCase(email))
                        .collect(Collectors.toList());

                if (userTours.isEmpty()) {
                    res.status(404);
                    resp.put("message", "Không tìm thấy tour nào cho email: " + email);
                    return new Gson().toJson(resp);
                }

                // Trả về danh sách kết quả JSON
                res.status(200);

                return gson.toJson(userTours);

            } catch (Exception e) {
                e.printStackTrace();
                res.status(500);
                resp.put("message", "Lỗi khi lấy danh sách tour: " + e.getMessage());
                return new Gson().toJson(resp);
            }
        });

        // API đổi trạng thái thành Paid
        put("/api/dattour/:email/:id/paid", (req, res) -> {
            res.type("application/json; charset=UTF-8");
            res.raw().setCharacterEncoding("UTF-8");
            Map<String, Object> resp = new HashMap<>();

            try {
                String email = req.params(":email");
                String id = req.params(":id");

                if (email == null || email.isEmpty() || id == null || id.isEmpty()) {
                    res.status(400);
                    resp.put("message", "Thiếu email hoặc ID tour!");
                    return new Gson().toJson(resp);
                }

                List<DatTour> allTours = graph.getAll(hg.type(DatTour.class));
                DatTour found = null;
                HGHandle handle = null;

                for (DatTour dt : allTours) {
                    if (dt.getId().equals(id) && dt.getKhachHangEmail().equalsIgnoreCase(email)) {
                        found = dt;
                        handle = graph.getHandle(dt);
                        break;
                    }
                }

                if (found == null || handle == null) {
                    res.status(404);
                    resp.put("message", "Không tìm thấy tour cho email: " + email + " với ID: " + id);
                    return new Gson().toJson(resp);
                }

                found.setTrangThai("Paid");
                graph.replace(handle, found);

                res.status(200);
                resp.put("message", "Đã cập nhật trạng thái thành Paid!");
                resp.put("id", id);
                resp.put("email", email);
                return new Gson().toJson(resp);

            } catch (Exception e) {
                e.printStackTrace();
                res.status(500);
                resp.put("message", "Lỗi khi cập nhật trạng thái: " + e.getMessage());
                return new Gson().toJson(resp);
            }
        });

        // API đổi trạng thái thành Cancelled
        put("/api/dattour/:email/:id/cancelled", (req, res) -> {
            res.type("application/json; charset=UTF-8");
            res.raw().setCharacterEncoding("UTF-8");
            Map<String, Object> resp = new HashMap<>();

            try {
                String email = req.params(":email");
                String id = req.params(":id");

                if (email == null || email.isEmpty() || id == null || id.isEmpty()) {
                    res.status(400);
                    resp.put("message", "Thiếu email hoặc ID tour!");
                    return new Gson().toJson(resp);
                }

                List<DatTour> allTours = graph.getAll(hg.type(DatTour.class));
                DatTour found = null;
                HGHandle handle = null;

                for (DatTour dt : allTours) {
                    if (dt.getId().equals(id) && dt.getKhachHangEmail().equalsIgnoreCase(email)) {
                        found = dt;
                        handle = graph.getHandle(dt);
                        break;
                    }
                }

                if (found == null || handle == null) {
                    res.status(404);
                    resp.put("message", "Không tìm thấy tour cho email: " + email + " với ID: " + id);
                    return new Gson().toJson(resp);
                }

                found.setTrangThai("Cancelled");
                graph.replace(handle, found);

                res.status(200);
                resp.put("message", "Đã cập nhật trạng thái thành Cancelled!");
                resp.put("id", id);
                resp.put("email", email);
                return new Gson().toJson(resp);

            } catch (Exception e) {
                e.printStackTrace();
                res.status(500);
                resp.put("message", "Lỗi khi cập nhật trạng thái: " + e.getMessage());
                return new Gson().toJson(resp);
            }
        });

        // Lấy đặt tour theo ID
        get("/api/dattour/:id", (req, res) -> {
            res.type("application/json; charset=UTF-8");
            res.raw().setCharacterEncoding("UTF-8");
            String id = req.params(":id");

            // Lấy danh sách các đặt tour
            List<DatTour> datTours = graph.getAll(hg.type(DatTour.class));

            // Tìm đặt tour theo ID
            for (DatTour datTour : datTours) {
                if (datTour.getId() != null && datTour.getId().equalsIgnoreCase(id)) {
                    // Trả về thông tin đặt tour nếu tìm thấy
                    return gson.toJson(datTour);
                }
            }

            // Trả về thông báo lỗi nếu không tìm thấy
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Không tìm thấy đặt tour với ID: " + id);
            return gson.toJson(errorResponse);
        });

        put("/api/dattour/:id", (req, res) -> {
            res.type("application/json; charset=UTF-8");
            res.raw().setCharacterEncoding("UTF-8");
            String id = req.params(":id");

            // Parse thông tin đặt tour từ request body
            DatTour updatedDatTour = gson.fromJson(req.body(), DatTour.class);

            // Lấy danh sách đặt tour và tìm đặt tour theo ID
            List<DatTour> datTours = graph.getAll(hg.type(DatTour.class));
            boolean isDatTourFound = false;

            for (DatTour datTour : datTours) {
                // Sử dụng equals để so sánh ID
                if (datTour.getId() != null && datTour.getId().equals(id)) {
                    isDatTourFound = true;

                    // Cập nhật các trường thông tin đặt tour nếu có giá trị mới
                    if (updatedDatTour.getKhachHangEmail() != null) {
                        datTour.setKhachHangEmail(updatedDatTour.getKhachHangEmail());
                    }
                    if (updatedDatTour.getTourId() != null) {
                        datTour.setTourId(updatedDatTour.getTourId());
                    }
                    if (updatedDatTour.getSoNguoi() > 0) {
                        datTour.setSoNguoi(updatedDatTour.getSoNguoi());
                    }
                    if (updatedDatTour.getNgayDat() != null) {
                        datTour.setNgayDat(updatedDatTour.getNgayDat()); // Đã là string rồi
                    }
                    if (updatedDatTour.getNgayKhoiHanh() != null) {
                        datTour.setNgayKhoiHanh(updatedDatTour.getNgayKhoiHanh()); // Đã là string rồi
                    }
                    if (updatedDatTour.getTrangThai() != null) {
                        datTour.setTrangThai(updatedDatTour.getTrangThai());
                    }

                    // Lưu thông tin cập nhật vào database
                    graph.update(datTour);

                    Map<String, String> response = new HashMap<>();
                    response.put("message", "Cập nhật đặt tour thành công!");
                    return gson.toJson(response);
                }
            }

            // Nếu không tìm thấy đặt tour với ID
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Không tìm thấy đặt tour với ID: " + id);
            return gson.toJson(errorResponse);
        });

        // Xóa đặt tour theo ID
        delete("/api/dattour/:id", (req, res) -> {
            res.type("application/json; charset=UTF-8");
            res.raw().setCharacterEncoding("UTF-8");
            String id = req.params(":id");

            // Tìm đặt tour theo ID
            List<DatTour> datTours = graph.getAll(hg.type(DatTour.class));
            boolean isDatTourFound = false;

            for (DatTour datTour : datTours) {
                if (datTour.getId() != null && datTour.getId().equalsIgnoreCase(id)) {
                    isDatTourFound = true;

                    // Xóa đặt tour
                    HGHandle handle = graph.getHandle(datTour);
                    graph.remove(handle);

                    Map<String, String> response = new HashMap<>();
                    response.put("message", "Đã xóa đặt tour thành công!");
                    return gson.toJson(response);
                }
            }

            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Không tìm thấy đặt tour với ID: " + id);
            return gson.toJson(errorResponse);
        });

        // ========== API HOA DON ==========
        get("/api/hoadon", (req, res) -> {
            res.type("application/json; charset=UTF-8");
            res.raw().setCharacterEncoding("UTF-8");
            List<HoaDon> list = graph.getAll(hg.type(HoaDon.class));
            return gson.toJson(list);
        });

        post("/api/hoadon", (req, res) -> {
            res.type("application/json; charset=UTF-8");
            res.raw().setCharacterEncoding("UTF-8");
            HoaDon hd = gson.fromJson(req.body(), HoaDon.class);
            graph.add(hd);
            Map<String, String> resp = new HashMap<>();
            resp.put("message", "Đã thêm hóa đơn!");
            return gson.toJson(resp);
        });

        // Lấy hóa đơn theo ID
        get("/api/hoadon/:id", (req, res) -> {
            res.type("application/json; charset=UTF-8");
            res.raw().setCharacterEncoding("UTF-8");
            String id = req.params(":id"); // Lấy ID từ URL

            // Lấy danh sách hóa đơn
            List<HoaDon> hoaDons = graph.getAll(hg.type(HoaDon.class));
            HoaDon foundHoaDon = null;

            for (HoaDon hoaDon : hoaDons) {
                if (hoaDon.getId() != null && hoaDon.getId().equals(id)) {
                    foundHoaDon = hoaDon; // Nếu tìm thấy hóa đơn
                    break;
                }
            }

            if (foundHoaDon != null) {
                return gson.toJson(foundHoaDon); // Trả về hóa đơn nếu tìm thấy
            } else {
                res.status(404);
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "Không tìm thấy hóa đơn với ID: " + id);
                return gson.toJson(errorResponse);
            }
        });

        // Cập nhật hóa đơn theo ID
        put("/api/hoadon/:id", (req, res) -> {
            res.type("application/json; charset=UTF-8");
            res.raw().setCharacterEncoding("UTF-8");
            String id = req.params(":id");

            // Parse thông tin hóa đơn từ request body
            HoaDon updatedHoaDon = gson.fromJson(req.body(), HoaDon.class);

            // Lấy danh sách hóa đơn và tìm hóa đơn theo ID
            List<HoaDon> hoaDons = graph.getAll(hg.type(HoaDon.class));
            boolean isHoaDonFound = false;

            for (HoaDon hoaDon : hoaDons) {
                if (hoaDon.getId() != null && hoaDon.getId().equals(id)) {
                    isHoaDonFound = true;

                    // Cập nhật các trường thông tin hóa đơn nếu có
                    if (updatedHoaDon.getDatTourId() != null)
                        hoaDon.setDatTourId(updatedHoaDon.getDatTourId());
                    if (updatedHoaDon.getTongTien() > 0)
                        hoaDon.setTongTien(updatedHoaDon.getTongTien());
                    if (updatedHoaDon.getPhuongThucThanhToan() != null)
                        hoaDon.setPhuongThucThanhToan(updatedHoaDon.getPhuongThucThanhToan());

                    // Trạng thái thanh toán
                    if (updatedHoaDon.getTrangThaiThanhToan() != null) {
                        hoaDon.setTrangThaiThanhToan(updatedHoaDon.getTrangThaiThanhToan());
                    }

                    // Lưu vào cơ sở dữ liệu
                    graph.update(hoaDon);

                    Map<String, String> response = new HashMap<>();
                    response.put("message", "Cập nhật hóa đơn thành công!");
                    return gson.toJson(response);
                }
            }

            // Nếu không tìm thấy hóa đơn
            Map<String, String> response = new HashMap<>();
            response.put("message", "Hóa đơn không tồn tại!");
            return gson.toJson(response);
        });

        // Xóa hóa đơn theo ID
        delete("/api/hoadon/:id", (req, res) -> {
            res.type("application/json; charset=UTF-8");
            res.raw().setCharacterEncoding("UTF-8");
            String id = req.params(":id");

            // Tìm hóa đơn theo ID
            List<HoaDon> hoaDons = graph.getAll(hg.type(HoaDon.class));
            boolean isHoaDonFound = false;
            for (HoaDon hoaDon : hoaDons) {
                if (hoaDon.getId() != null && hoaDon.getId().equals(id)) {
                    isHoaDonFound = true;

                    // Xóa hóa đơn: lấy HGHandle rồi remove bằng handle
                    HGHandle handle = graph.getHandle(hoaDon);
                    if (handle != null) {
                        graph.remove(handle);
                    }

                    // Trả về kết quả thành công
                    Map<String, String> response = new HashMap<>();
                    response.put("message", "Đã xóa hóa đơn thành công!");
                    return gson.toJson(response);
                }
            }

            // Nếu không tìm thấy hóa đơn
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Không tìm thấy hóa đơn với ID: " + id);
            return gson.toJson(errorResponse);
        });

        // ========== API DANH GIA ==========
        get("/api/danhgia", (req, res) -> {
            res.type("application/json; charset=UTF-8");
            res.raw().setCharacterEncoding("UTF-8");
            List<DanhGia> list = graph.getAll(hg.type(DanhGia.class));
            return gson.toJson(list);
        });

        // post("/api/danhgia", (req, res) -> {
        // res.type("application/json; charset=UTF-8");
        // res.raw().setCharacterEncoding("UTF-8");
        // DanhGia dg = gson.fromJson(req.body(), DanhGia.class);
        // graph.add(dg);
        // Map<String, String> resp = new HashMap<>();
        // resp.put("message", "Đã thêm đánh giá!");
        // return gson.toJson(resp);
        // });

        // API: Lấy danh sách đánh giá theo TourID
        get("/api/tour/:id/danhgia", (req, res) -> {
            res.type("application/json; charset=UTF-8");
            res.raw().setCharacterEncoding("UTF-8");
            String tourId = req.params(":id");

            // Lấy danh sách đánh giá và người dùng
            List<DanhGia> danhGias = graph.getAll(hg.type(DanhGia.class));
            List<User> users = graph.getAll(hg.type(User.class));

            List<Map<String, Object>> result = new ArrayList<>();

            for (DanhGia dg : danhGias) {
                if (dg.getTourId() != null && dg.getTourId().equalsIgnoreCase(tourId)) {

                    // Tìm thông tin người đánh giá
                    User foundUser = null;
                    for (User u : users) {
                        if (u.getEmail().equalsIgnoreCase(dg.getKhachHangEmail())) {
                            foundUser = u;
                            break;
                        }
                    }

                    Map<String, Object> item = new HashMap<>();
                    item.put("id", dg.getId());
                    item.put("diemDanhGia", dg.getDiemDanhGia());
                    item.put("binhLuan", dg.getBinhLuan());
                    item.put("ngayDanhGia", dg.getNgayDanhGia().toString());

                    if (foundUser != null) {
                        Map<String, Object> userInfo = new HashMap<>();
                        userInfo.put("ten", foundUser.getName());
                        userInfo.put("email", foundUser.getEmail());
                        item.put("nguoiDanhGia", userInfo);
                    } else {
                        item.put("nguoiDanhGia", null);
                    }

                    result.add(item);
                }
            }

            return gson.toJson(result);
        });

        // Tạo đánh giá mới
        post("/api/tour/:id/danhgia", (req, res) -> {
            res.type("application/json; charset=UTF-8");
            res.raw().setCharacterEncoding("UTF-8");
            String tourId = req.params(":id");

            // Đọc dữ liệu từ request body
            Map<String, Object> body = gson.fromJson(req.body(), Map.class);

            String email = (String) body.get("email");
            Double diem = body.containsKey("diemDanhGia") ? (Double) body.get("diemDanhGia") : null;
            String binhLuan = (String) body.get("binhLuan");

            // Kiểm tra dữ liệu bắt buộc có trong request không
            if (email == null || diem == null || binhLuan == null) {
                res.status(400); // Bad Request
                Map<String, String> errorResp = new HashMap<>();
                errorResp.put("error",
                        "Thông tin đánh giá chưa đầy đủ. Các trường bắt buộc: email, diemDanhGia, binhLuan.");
                return gson.toJson(errorResp);
            }

            // Tạo ID tự động cho đánh giá
            String newId = "DG" + System.currentTimeMillis();

            // Tạo đối tượng DanhGia mới
            DanhGia newDG = new DanhGia(
                    newId,
                    email,
                    tourId,
                    diem.intValue(), // Chuyển đổi từ Double sang int
                    binhLuan,
                    LocalDateTime.now().toString() // Chuyển LocalDateTime thành String
            );

            // Thêm đánh giá vào cơ sở dữ liệu (HyperGraphDB)
            try {
                graph.add(newDG);
            } catch (Exception e) {
                res.status(500); // Internal Server Error
                Map<String, String> errorResp = new HashMap<>();
                errorResp.put("error", "Không thể lưu đánh giá. Lỗi hệ thống: " + e.getMessage());
                return gson.toJson(errorResp);
            }

            // Trả về phản hồi thành công
            Map<String, String> resp = new HashMap<>();
            resp.put("message", "Đã thêm đánh giá mới!");
            return gson.toJson(resp);
        });

        // API cập nhật đánh giá theo ID
        put("/api/danhgia/:id", (req, res) -> {
            res.type("application/json; charset=UTF-8");
            res.raw().setCharacterEncoding("UTF-8");
            String id = req.params(":id");

            // Parse thông tin đánh giá từ request body
            DanhGia updatedDanhGia = gson.fromJson(req.body(), DanhGia.class);

            // Lấy danh sách đánh giá và tìm đánh giá theo ID
            List<DanhGia> danhGias = graph.getAll(hg.type(DanhGia.class));
            boolean isDanhGiaFound = false;

            for (DanhGia danhGia : danhGias) {
                if (danhGia.getId() != null && danhGia.getId().equals(id)) {
                    isDanhGiaFound = true;

                    // Cập nhật các trường thông tin đánh giá nếu có
                    if (updatedDanhGia.getDiemDanhGia() > 0)
                        danhGia.setDiemDanhGia(updatedDanhGia.getDiemDanhGia());
                    if (updatedDanhGia.getBinhLuan() != null)
                        danhGia.setBinhLuan(updatedDanhGia.getBinhLuan());
                    if (updatedDanhGia.getKhachHangEmail() != null)
                        danhGia.setKhachHangEmail(updatedDanhGia.getKhachHangEmail());
                    if (updatedDanhGia.getTourId() != null)
                        danhGia.setTourId(updatedDanhGia.getTourId());
                    if (updatedDanhGia.getNgayDanhGia() != null)
                        danhGia.setNgayDanhGia(updatedDanhGia.getNgayDanhGia());

                    // Lưu vào cơ sở dữ liệu
                    graph.update(danhGia);

                    Map<String, String> response = new HashMap<>();
                    response.put("message", "Cập nhật đánh giá thành công!");
                    return gson.toJson(response); // Trả về phản hồi sau khi cập nhật thành công
                }
            }

            // Nếu không tìm thấy đánh giá với ID tương ứng
            Map<String, String> response = new HashMap<>();
            response.put("message", "Đánh giá không tồn tại!");
            return gson.toJson(response);
        });

        // API xóa đánh giá theo ID
        delete("/api/danhgia/:id", (req, res) -> {
            res.type("application/json; charset=UTF-8");
            res.raw().setCharacterEncoding("UTF-8");
            String id = req.params(":id");

            // Tìm kiếm và xóa đánh giá
            List<DanhGia> danhGias = graph.getAll(hg.type(DanhGia.class));
            boolean isDanhGiaFound = false;

            for (DanhGia danhGia : danhGias) {
                if (danhGia.getId() != null && danhGia.getId().equals(id)) {
                    isDanhGiaFound = true;

                    // Xóa đánh giá: lấy HGHandle rồi remove bằng handle
                    HGHandle handle = graph.getHandle(danhGia);
                    if (handle != null) {
                        graph.remove(handle);
                    }

                    // Trả về kết quả thành công
                    Map<String, String> response = new HashMap<>();
                    response.put("message", "Đánh giá đã được xóa thành công!");
                    return gson.toJson(response);
                }
            }

            // Nếu không tìm thấy đánh giá
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Không tìm thấy đánh giá với ID: " + id);
            return gson.toJson(errorResponse);
        });

        // =========================KẾT HỢP==============================
        // API: Lấy chi tiết 1 tour kèm thông tin nhà tổ chức
        get("/api/tour/:id", (req, res) -> {
            res.type("application/json; charset=UTF-8");
            res.raw().setCharacterEncoding("UTF-8");
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

        // API tạo một HOADON mới
        post("/api/hoadon/:email/:datTourId/pay", (req, res) -> {
            res.type("application/json; charset=UTF-8");
            res.raw().setCharacterEncoding("UTF-8");
            Map<String, Object> resp = new HashMap<>();

            try {
                String email = req.params(":email");
                String datTourId = req.params(":datTourId");

                // Parse phương thức thanh toán từ body
                Map<String, String> body = gson.fromJson(req.body(), Map.class);
                String phuongThucThanhToan = body.getOrDefault("phuongThucThanhToan", "Cash");

                // ===== Lấy DatTour =====
                HGHandle datTourHandle = graph.findOne(
                        hg.and(hg.type(DatTour.class), hg.eq("id", datTourId)));

                if (datTourHandle == null) {
                    res.status(404);
                    resp.put("error", "Không tìm thấy đặt tour có ID: " + datTourId);
                    return gson.toJson(resp);
                }

                DatTour datTour = (DatTour) graph.get(datTourHandle);

                if (!datTour.getKhachHangEmail().equalsIgnoreCase(email)) {
                    res.status(400);
                    resp.put("error", "Email không khớp với đặt tour này!");
                    return gson.toJson(resp);
                }

                // ===== Lấy Tour tương ứng =====
                HGHandle tourHandle = graph.findOne(
                        hg.and(hg.type(Tour.class), hg.eq("id", datTour.getTourId())));

                if (tourHandle == null) {
                    res.status(404);
                    resp.put("error", "Không tìm thấy tour tương ứng với đặt tour này.");
                    return gson.toJson(resp);
                }

                Tour tour = (Tour) graph.get(tourHandle);

                // Tính tổng tiền
                double tongTien = tour.getGia() * datTour.getSoNguoi();

                // ===== Tạo ID hóa đơn nối tiếp =====
                List<HGHandle> allHoaDonHandles = graph.findAll(hg.type(HoaDon.class));
                int maxId = 0;
                for (HGHandle handle : allHoaDonHandles) {
                    HoaDon hd = (HoaDon) graph.get(handle);
                    String id = hd.getId(); // ví dụ: "HD002" hoặc UUID cũ
                    if (id.startsWith("HD")) {
                        try {
                            int num = Integer.parseInt(id.substring(2)); // lấy phần số sau "HD"
                            if (num > maxId)
                                maxId = num;
                        } catch (NumberFormatException e) {
                            // bỏ qua nếu không phải số
                        }
                    }
                }
                String newHoaDonId = String.format("HD%03d", maxId + 1);

                // ===== Tạo hóa đơn mới =====
                HoaDon hoaDon = new HoaDon(
                        newHoaDonId,
                        datTourId,
                        tongTien,
                        phuongThucThanhToan,
                        "Paid", // trạng thái thanh toán
                        LocalDateTime.now().toString());

                // Lưu hóa đơn vào HyperGraphDB
                graph.add(hoaDon);

                // Trả về thông tin hóa đơn
                resp.put("message", "Thanh toán thành công!");
                resp.put("hoaDonId", hoaDon.getId());
                resp.put("datTourId", datTourId);
                resp.put("tongTien", tongTien);
                resp.put("phuongThucThanhToan", phuongThucThanhToan);
                resp.put("trangThai", "Paid");

                res.status(200);
                return gson.toJson(resp);

            } catch (Exception e) {
                e.printStackTrace();
                res.status(500);
                resp.put("error", "Lỗi trong quá trình tạo thanh toán: " + e.getMessage());
                return gson.toJson(resp);
            }
        });

        // API lấy danh sách hóa đơn cơ bản của một người dùng theo email
        get("/api/hoadon/:email", (req, res) -> {
            res.type("application/json; charset=UTF-8");
            res.raw().setCharacterEncoding("UTF-8");
            String email = req.params(":email");

            // Lấy danh sách hóa đơn từ database
            List<HoaDon> hoaDons = graph.getAll(hg.type(HoaDon.class));
            List<HoaDon> userHoaDons = new ArrayList<>();

            // Lọc hóa đơn của người dùng theo email
            for (HoaDon hoaDon : hoaDons) {
                if (hoaDon.getDatTourId().equals(email)) { // Chắc chắn rằng đây là trường liên kết đúng với người dùng
                    userHoaDons.add(hoaDon);
                }
            }

            if (!userHoaDons.isEmpty()) {
                // Trả về danh sách hóa đơn dưới dạng JSON
                Map<String, Object> response = new HashMap<>();
                response.put("hoadon", userHoaDons);
                return gson.toJson(response);
            }

            // Nếu không có hóa đơn
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Không tìm thấy hóa đơn cho người dùng với email: " + email);
            return gson.toJson(errorResponse);
        });

        // API tính tổng doanh thu từ các hóa đơn đã thanh toán
        get("/api/statistical/totalRevenue", (req, res) -> {

            List<DatTour> datTours = graph.getAll(hg.type(DatTour.class));
            List<HoaDon> hoaDons = graph.getAll(hg.type(HoaDon.class));
            List<Tour> tours = graph.getAll(hg.type(Tour.class));

            // Map TourId -> Giá
            Map<String, Double> tourPrices = new HashMap<>();
            for (Tour t : tours) {
                tourPrices.put(t.getId(), t.getGia());
            }

            // Map DatTourId -> DatTour
            Map<String, DatTour> datTourMap = new HashMap<>();
            for (DatTour dt : datTours) {
                datTourMap.put(dt.getId(), dt);
            }

            double totalRevenue = 0.0;

            for (HoaDon hd : hoaDons) {
                if (!"Paid".equalsIgnoreCase(hd.getTrangThaiThanhToan()))
                    continue;

                DatTour dt = datTourMap.get(hd.getDatTourId());
                if (dt == null)
                    continue;

                double price = tourPrices.getOrDefault(dt.getTourId(), 0.0);
                double amount = price * dt.getSoNguoi();

                totalRevenue += amount;
            }

            res.type("application/json; charset=UTF-8");
            res.raw().setCharacterEncoding("UTF-8");

            // Trả về JSON đơn giản
            Map<String, Double> result = new HashMap<>();
            result.put("totalRevenue", totalRevenue);

            return gson.toJson(result);
        });

        // API tạo hóa đơn thanh toán
        post("/api/hoadon/:email/:datTourId/pay", (req, res) -> {
            res.type("application/json; charset=UTF-8");
            res.raw().setCharacterEncoding("UTF-8");

            String email = req.params(":email");
            String datTourId = req.params(":datTourId");

            Map<String, Object> resp = new HashMap<>();

            try {
                // 1. Tìm đặt tour
                List<DatTour> datTours = graph.getAll(hg.type(DatTour.class));
                DatTour found = null;
                for (DatTour dt : datTours) {
                    if (dt.getId().equals(datTourId) &&
                            dt.getKhachHangEmail().equalsIgnoreCase(email)) {
                        found = dt;
                        break;
                    }
                }

                if (found == null) {
                    res.status(404);
                    resp.put("message", "Không tìm thấy đặt tour!");
                    return gson.toJson(resp);
                }

                // 2. Tạo ID hóa đơn mới
                List<HoaDon> hoaDons = graph.getAll(hg.type(HoaDon.class));
                int nextId = hoaDons.size() + 1;
                String newId = String.format("HD%03d", nextId);

                // 3. Tạo hóa đơn
                HoaDon newHD = new HoaDon(
                        newId,
                        datTourId,
                        found.getSoNguoi() * 1000000, // bạn tự thay giá
                        "MoMo",
                        "Paid",
                        LocalDateTime.now().toString());

                graph.add(newHD);

                // 4. Cập nhật trạng thái DatTour
                found.setTrangThai("Paid");
                graph.update(found);

                resp.put("message", "Thanh toán thành công!");
                resp.put("hoaDonId", newId);
                return gson.toJson(resp);

            } catch (Exception e) {
                res.status(500);
                resp.put("message", "Lỗi server: " + e.getMessage());
                return gson.toJson(resp);
            }
        });

        get("/api/hoadon/user/:email", (req, res) -> {
            res.type("application/json; charset=UTF-8");
            res.raw().setCharacterEncoding("UTF-8");

            String email = req.params(":email");

            List<HoaDon> hoaDons = graph.getAll(hg.type(HoaDon.class));
            List<DatTour> datTours = graph.getAll(hg.type(DatTour.class));

            List<Map<String, Object>> result = new ArrayList<>();

            for (HoaDon hd : hoaDons) {
                for (DatTour dt : datTours) {
                    if (dt.getId().equals(hd.getDatTourId()) &&
                            dt.getKhachHangEmail().equalsIgnoreCase(email)) {

                        Map<String, Object> item = new HashMap<>();
                        item.put("hoaDonId", hd.getId());
                        item.put("tongTien", hd.getTongTien());
                        item.put("phuongThuc", hd.getPhuongThucThanhToan());
                        item.put("trangThai", hd.getTrangThaiThanhToan());
                        item.put("ngayThanhToan", hd.getNgayThanhToan());
                        item.put("datTourId", dt.getId());
                        item.put("soNguoi", dt.getSoNguoi());

                        result.add(item);
                    }
                }
            }

            return gson.toJson(result);
        });

        // API: Lấy danh sách đặt tour "Pending" cho 1 user (email)
        get("/api/dattour/user/:email/pending", (req, res) -> {
            res.type("application/json; charset=UTF-8");
            res.raw().setCharacterEncoding("UTF-8");

            String email = req.params(":email");
            Map<String, Object> resp = new HashMap<>();

            try {
                if (email == null || email.trim().isEmpty()) {
                    res.status(400);
                    resp.put("message", "Thiếu email người dùng!");
                    return gson.toJson(resp);
                }

                // Lấy tất cả DatTour và Tour
                List<DatTour> allDatTours = graph.getAll(hg.type(DatTour.class));
                List<Tour> allTours = graph.getAll(hg.type(Tour.class));

                List<Map<String, Object>> result = new ArrayList<>();

                for (DatTour dt : allDatTours) {
                    if (dt.getKhachHangEmail() == null)
                        continue;

                    boolean sameEmail = dt.getKhachHangEmail().equalsIgnoreCase(email);

                    // chấp nhận 2 dạng trạng thái phổ biến: "Pending" hoặc "Chờ thanh toán"
                    String trangThai = dt.getTrangThai() == null ? "" : dt.getTrangThai().trim();
                    boolean isPending = trangThai.equalsIgnoreCase("Pending")
                            || trangThai.equalsIgnoreCase("Chờ thanh toán")
                            || trangThai.equalsIgnoreCase("Cho thanh toan")
                            || trangThai.equalsIgnoreCase("ChoThanhToan");

                    if (sameEmail && isPending) {
                        Map<String, Object> item = new HashMap<>();
                        item.put("id", dt.getId());
                        item.put("tourId", dt.getTourId());
                        item.put("soNguoi", dt.getSoNguoi());
                        item.put("ngayDat", dt.getNgayDat());
                        item.put("trangThai", dt.getTrangThai());

                        // Tìm thông tin tour để lấy tenTour và gia (nếu có)
                        Tour foundTour = null;
                        for (Tour t : allTours) {
                            if (t.getId() != null && t.getId().equalsIgnoreCase(dt.getTourId())) {
                                foundTour = t;
                                break;
                            }
                        }
                        if (foundTour != null) {
                            item.put("tourName", foundTour.getTenTour());
                            // Nếu giá tour là >0 thì tính tổng, nếu không có thì trả null
                            if (foundTour.getGia() > 0) {
                                long tong = (long) foundTour.getGia() * Math.max(1, dt.getSoNguoi());
                                item.put("tongTien", tong);
                            } else {
                                item.put("tongTien", 0);
                            }
                        } else {
                            item.put("tourName", "Không xác định");
                            item.put("tongTien", 0);
                        }

                        result.add(item);
                    }
                }

                // Trả về kết quả
                return gson.toJson(result);

            } catch (Exception e) {
                e.printStackTrace();
                res.status(500);
                resp.put("message", "Lỗi server: " + e.getMessage());
                return gson.toJson(resp);
            }
        });

        // ==========================ĐÓNG SERVER=========================
        // Đóng DB khi tắt server
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            graph.close();
            System.out.println("Đã đóng HyperGraphDB");
        }));
    }
}
