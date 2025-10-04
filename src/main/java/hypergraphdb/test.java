package hypergraphdb;

import org.hypergraphdb.HyperGraph;

public class test {
    public static void main(String[] args) {
        // Mở hoặc tạo database HyperGraphDB trong thư mục ./db
        HyperGraph graph = new HyperGraph("db");

        System.out.println("HyperGraphDB mở thành công!");
        graph.close();
    }
}
