package W14;

import java.util.LinkedList;
import java.util.List;

public class WeightedGraph {

    private final LinkedList<Edge>[] adjacency;

    public WeightedGraph( int number ) {  //  0, 1, ..., number-1
        adjacency = new LinkedList[number];
        for( int i = 0; i < number; i ++ )
            adjacency[i] = new LinkedList<>();
    }

    public void addEdge( int v1, int v2, double weight ) {
        Edge e = new Edge(v1, v2, weight);
        if( !adjacency[v1].contains(e) )
            adjacency[v1].add(e);
    }

    public List<Edge> adjacent(int v ) {
        return (List<Edge>)adjacency[v].clone();
    }

    public boolean isAdjacent( int v1, int v2 ) {
        for( Edge e : adjacency[v1] )
            if( e.to == v2 )
                return true;
        return false;
    }

    public int size() {
        return adjacency.length;
    }

}