package W13;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.PriorityQueue;

public class AStar {

    public static LinkedList<Integer>astar(Graph graph, int start, int end, int[] heuristic) {
        int N = graph.size();
        int[] g = new int[N];

        Arrays.fill(g, Integer.MAX_VALUE);

        PriorityQueue<Node> openSet = new PriorityQueue<>();
        g[start] = 0;
        openSet.add(new Node(start, 0, heuristic[start], null));

        while (!openSet.isEmpty()) {
            Node s = openSet.poll();
            if (s.id == end) {
                LinkedList<Integer> ret = new LinkedList<>();
                while( s != null ) {
                    ret.addFirst(s.id);
                    s = s.previous;
                }
                return ret;
            }
            for (Integer s1 : graph.adjacency(s.id)) {
                if (g[s1] > s.g + 1 ) {
                    g[s1] = s.g + 1;
                    openSet.add(new Node(s1, g[s1], g[s1] + heuristic[s1], s));
                }
            }
        }
        return null;
    }

    private static class Node implements Comparable<Node> {
        final int id;
        final int g;
        final int f;

        final Node previous;

        Node(int id, int g, int h, Node previous) {
            this.id = id;
            this.g = g;
            f = g + h;
            this.previous = previous;
        }

        public int compareTo(Node o) {
            return Integer.compare(f, o.f);
        }
    }
}
