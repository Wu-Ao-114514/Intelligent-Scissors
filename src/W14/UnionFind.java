package W14;

public class UnionFind {
    
    private final int[] parent;

    public UnionFind( int number ) {
        parent = new int[number];
        for( int i = 0; i < number; i ++ )
            parent[i] = i;
    }

    public int find( int v ) {
        return (parent[v]==v) ? v : (parent[v] = find(parent[v]));
    }

    public boolean isConnected( int v, int w ) {
        return find(v) == find(w);
    }

    public void union( int v, int w ) {
        parent[find(v)] = find(w);
    }
}
