package W12;

import edu.princeton.cs.algs4.Graph;

public class a {
    public static void main(String[] args) {
        Graph graph = new Graph(5); // 创建一个有 5 个顶点的图

        // 添加一些边
        graph.addEdge(0, 1);
        graph.addEdge(0, 2);
        graph.addEdge(1, 2);
        graph.addEdge(3, 4);

        // 查询顶点 1 的邻接顶点
        System.out.println("顶点 1 的邻接顶点：");
        for (Integer neighbor : graph.adj(1)) {
            System.out.println(neighbor);
        }
    }
}