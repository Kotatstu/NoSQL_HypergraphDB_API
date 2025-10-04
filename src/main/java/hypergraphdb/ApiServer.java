package hypergraphdb;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.Gson;

import static spark.Spark.get;
import static spark.Spark.port;
import static spark.Spark.post;

public class ApiServer {
    public static void main(String[] args) {
        port(8080); // port cho API Java

        get("/api/hello", (req, res) -> {
            res.type("application/json");
            Map<String, String> response = new HashMap<>();
            response.put("message", "Hello from Java API!");
            return new Gson().toJson(response);
        });

        // Ví dụ POST
        post("/api/add", (req, res) -> {
            res.type("application/json");
            @SuppressWarnings("unchecked")
            Map<String, Object> data = new Gson().fromJson(req.body(), Map.class);
            String value = (String) data.get("value");
            Map<String, String> response = new HashMap<>();
            response.put("message", "Received: " + value);
            return new Gson().toJson(response);
        });
    }
}