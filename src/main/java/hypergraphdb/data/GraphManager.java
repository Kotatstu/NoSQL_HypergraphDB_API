package hypergraphdb.data;

import java.util.ArrayList;
import java.util.List;

import org.hypergraphdb.HGHandle;
import org.hypergraphdb.HGQuery.hg;
import org.hypergraphdb.HGSearchResult;
import org.hypergraphdb.HyperGraph;
import org.hypergraphdb.query.HGQueryCondition;

public class GraphManager {
    private static HyperGraph graph;

    // Lấy hoặc khởi tạo HyperGraph
    public static synchronized HyperGraph getGraph() {
        if (graph == null) {
            graph = new HyperGraph("data/hypergraphdb"); // đường dẫn tới DB
        }
        return graph;
    }

    // Thêm đối tượng mới
    public static <T> HGHandle add(T obj) {
        return getGraph().add(obj);
    }

    // Lấy tất cả đối tượng thuộc loại clazz
    public static <T> List<T> getAll(Class<T> clazz) {
        List<T> results = new ArrayList<>();
        HGQueryCondition condition = hg.type(clazz);
        HGSearchResult<HGHandle> rs = getGraph().find(condition);

        try {
            while (rs.hasNext()) {
                HGHandle h = rs.next();
                results.add((T) getGraph().get(h));
            }
        } finally {
            rs.close(); // luôn đóng kết quả sau khi duyệt
        }

        return results;
    }

    // Xóa toàn bộ các đối tượng của một lớp
    public static <T> void removeAll(Class<T> clazz) {
        HGQueryCondition condition = hg.type(clazz);
        HGSearchResult<HGHandle> rs = getGraph().find(condition);

        try {
            while (rs.hasNext()) {
                getGraph().remove(rs.next());
            }
        } finally {
            rs.close();
        }
    }

    // Đóng graph
    public static void close() {
        if (graph != null) {
            graph.close();
            graph = null;
        }
    }
}
