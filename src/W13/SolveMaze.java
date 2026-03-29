package W13;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class SolveMaze {

    private static Graph buildGraph( char[][] maze ) {
        int height = maze.length;
        int width = maze[0].length;
        Graph graph = new GraphAdjList(height*width);
        int[] dh = new int[] { -1, 1,  0, 0 };
        int[] dw = new int[] {  0, 0, -1, 1 };

        for( int h = 0; h < height; h ++ )
            for( int w = 0; w < width; w ++ ) {
                if( maze[h][w] == 'W' )
                    continue;
                for( int i = 0; i < 4; i ++ ) {
                    int h2 = h+dh[i];
                    int w2 = w+dw[i];
                    if( h2 < 0 || h2 >= height || w2 < 0 || w2 >= width || maze[h2][w2] == 'W' )
                        continue;
                    graph.addEdge(h*width+w, h2*width+w2);
                }
            }
        return graph;
    }

    private static int findIndex( char[][] maze, char find ) {
        int height = maze.length;
        int width = maze[0].length;

        for( int h = 0; h < height; h ++ )
            for( int w = 0; w < width; w ++ )
                if( maze[h][w] == find )
                    return h*width+w;
        return -1;
    }

    private static boolean bfs( Graph graph, int start, int end ) {
        boolean[] visited = new boolean[graph.size()];
        Queue<Integer> queue = new LinkedList<>();
        visited[start] = true;
        queue.add(start);
        while( !queue.isEmpty() ) {
            int current = queue.poll();
            if( current == end )
                return true;
            for( int adj : graph.adjacency(current) ) {
                if( visited[adj] )
                    continue;
                visited[adj] = true;
                queue.add(adj);
            }
        }
        return false;
    }

    private static int [] heuristic ( int width , int height , int end ) {
        int[] heu = new int[width*height];
        int eh = end / width;
        int ew = end % width;
        for( int w = 0; w < width; w ++ )
            for( int h = 0; h < height; h ++ )
                heu[h*width+w] = Math.abs(w-ew)+Math.abs(h-eh);
        return heu;
    }

    public static void main( String[] args ) throws FileNotFoundException {
        char[][] maze;
        try( Scanner input = new Scanner(new File("C:\\Users\\hp\\Desktop\\CS203B\\lab13\\astar\\data\\maze.txt")) ) {
            LinkedList<char[]> list = new LinkedList<>();
            while( input.hasNextLine() ) {
                String line = input.nextLine().trim();
                if(line.isEmpty())
                    break;
                list.add(line.toCharArray());
            }
            maze = list.toArray(new char[0][]);
        }
        System.out.println("Maze:");
        for (char[] chars : maze) System.out.println(chars);

        int start = findIndex(maze, 'S');
        int end   = findIndex(maze, 'E');
        System.out.printf("%nStart: %d%nEnd:%d%n%n", start, end);

        Graph graph = buildGraph(maze);
        System.out.printf("Graph:%n%s%n", graph);
        boolean result = bfs(graph, start, end);
        System.out.printf("end is %sreachable from the start%n", (result) ? "" : "not " );

        System.out.printf("astar: %s%n", Arrays.toString(Objects.requireNonNull(AStar.astar(graph, start, end, heuristic(maze[0].length, maze.length, end))).toArray(new Integer[0])));
    }
}
