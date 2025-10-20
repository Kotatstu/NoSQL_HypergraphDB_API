package hypergraphdb;

import com.google.gson.Gson;
import org.hypergraphdb.HyperGraph;
import java.util.*;
import static org.hypergraphdb.HGQuery.hg;
import static spark.Spark.*;

public class ApiServerTest {
    private static final String DB_PATH = "db/usersDB";
    private static final Gson gson = new Gson();

    // 🟢 Lưu user đăng nhập tạm thời
    private static User currentUser = null;

    public static void main(String[] args) {
        port(8080);
        final HyperGraph graph = new HyperGraph(DB_PATH);

        before((req, res) -> {
            res.header("Access-Control-Allow-Origin", "*");
            res.header("Access-Control-Allow-Methods", "GET,POST,OPTIONS");
            res.header("Access-Control-Allow-Headers", "Content-Type");
        });
        options("/*", (req, res) -> "OK");

        // 🟢 API kiểm tra server
        get("/api/hello", (req, res) -> {
            res.type("application/json");
            Map<String, String> data = new HashMap<>();
            data.put("message", "Hello from HyperGraphDB API!");
            return gson.toJson(data);
        });

        // 🟢 API xem danh sách user
        get("/api/users", (req, res) -> {
            res.type("application/json");
            List<User> users = graph.getAll(hg.type(User.class));
            return gson.toJson(users);
        });

        // 🟢 API đăng ký
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

        // 🟠 API đăng nhập
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

        // 🟢 API lấy user hiện tại
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

        // 🔴 API đăng xuất
        post("/api/logout", (req, res) -> {
            currentUser = null;
            Map<String, String> msg = new HashMap<>();
            msg.put("message", "Đã đăng xuất");
            return gson.toJson(msg);
        });

        // 🔒 Đóng DB khi tắt server
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            graph.close();
            System.out.println("🔒 Đã đóng HyperGraphDB");
        }));
    }
}
