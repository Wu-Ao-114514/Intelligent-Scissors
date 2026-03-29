package W14;

import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;

public class MinimalTree {

    public static List<Edge> kruskal( WeightedGraph graph ) {
        PriorityQueue<Edge> pq = new PriorityQueue<>();

        for( int i = 0; i < graph.size(); i ++ )
            pq.addAll(graph.adjacent(i));
        
        LinkedList<Edge> result = new LinkedList<>();
        UnionFind uf = new UnionFind(graph.size());

        while( result.size() < graph.size()-1 && !pq.isEmpty() ) {
            Edge e = pq.poll();
            if( !uf.isConnected(e.from, e.to) ) {
                uf.union(e.from, e.to);
                result.add(e);
            }
        }
        return result;
    }

    public static List<Edge> prim( WeightedGraph graph ) {
        PriorityQueue<Edge> pq = new PriorityQueue<>();
        boolean[] marked = new boolean[graph.size()];  // marked[v] == true is vertex v is in the tree

        LinkedList<Edge> result = new LinkedList<>();

        for( int i = 0; i < graph.size(); i ++ ) {
            if( marked[i] )
                continue;
            marked[i] = true;
            pq.addAll(graph.adjacent(i));
            while( result.size() < graph.size()-1 && !pq.isEmpty() ) {
                Edge e = pq.poll();

                if( !marked[e.to] ) {
                    marked[e.to] = true;
                    pq.addAll(graph.adjacent(e.to));
                    result.add(e);
                }
            }
        }

        return result;
    }

    public static void main( String[] args ) {
        WeightedGraph graph = new WeightedGraph(4);

        graph.addEdge(0, 1, 4);
        graph.addEdge(1, 0, 4);

        graph.addEdge(0, 2, 1);
        graph.addEdge(2, 0, 1);

        graph.addEdge(0, 3, 6);
        graph.addEdge(3, 0, 6);

        graph.addEdge(1, 2, 3);
        graph.addEdge(2, 1, 3);

        graph.addEdge(1, 3, 5);
        graph.addEdge(3, 1, 5);

        graph.addEdge(2, 3, 2);
        graph.addEdge(3, 2, 2);

        for( Edge e : kruskal(graph) )
            System.out.printf("%d->%d %f\n", e.from, e.to, e.weight);

        for( Edge e : prim(graph) )
            System.out.printf("%d->%d %f\n", e.from, e.to, e.weight);
    }
    
}
