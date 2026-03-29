package W14;

public class Edge implements Comparable<Edge> {
    
    public final int from;
    public final int to;
    public final double weight;

    public Edge( int f, int t, double w ) {
        from = f;
        to = t;
        weight = w;
    }

    @Override
    public boolean equals( Object o ) {
        if( !(o instanceof Edge e) )
            return false;
        return from == e.from && to == e.to && weight == e.weight;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(from) ^ Integer.hashCode(to) ^ Double.hashCode(weight);
    }

    @Override
    public int compareTo(Edge o) {
        if( weight < o.weight )
            return -1;
        else if( weight > o.weight )
            return 1;
        return 0;
    }


}
