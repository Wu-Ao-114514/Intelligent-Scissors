package A3;

import java.util.*;

public class P7 {
    static class Node {
        int value;
        Node left, right;
        Node(int value) { this.value = value; }
    }

    static int n, m;
    static List<Integer> sequence = new ArrayList<>();
    static List<Integer> LIS = new ArrayList<>();

    static void inorder(Node root) {
        if (root == null) return;
        inorder(root.left);
        sequence.add(root.value - m);
        m++;
        inorder(root.right);
    }

    static int binarySearch(List<Integer> list, int x) {
        int l = 0, r = list.size() - 1;
        while (l <= r) {
            int mid = (l + r) >> 1;
            if (list.get(mid) <= x) l = mid + 1;
            else r = mid - 1;
        }
        return l;
    }

    static Node buildTree(int[] vals, int[][] edges) {
        if (n == 0) return null;
        Node[] nodes = new Node[n + 1];

        for (int i = 1; i <= n; i++)
            nodes[i] = new Node(vals[i]);

        for (int[] edge : edges)
            if (edge[2] == 0)
                nodes[edge[0]].left = nodes[edge[1]];
            else
                nodes[edge[0]].right = nodes[edge[1]];

        return nodes[1];
    }

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);

        n = input.nextInt();

        int[] vals = new int[n + 1];
        for (int i = 1; i <= n; i++)
            vals[i] = input.nextInt();

        int[][] edges = new int[n - 1][3];
        for (int i = 0; i < n - 1; i++) {
            int x = input.nextInt(), y = input.nextInt();
            edges[i][0] = x;
            edges[i][1] = i + 2;
            edges[i][2] = y;
        }

        Node root = buildTree(vals, edges);

        m = 0;
        sequence.clear();
        LIS.clear();

        inorder(root);

        LIS.add(sequence.getFirst());
        for (int i = 1; i < sequence.size(); i++)
            if (sequence.get(i) >= LIS.getLast())
                LIS.add(sequence.get(i));
            else {
                int index = binarySearch(LIS, sequence.get(i));
                LIS.set(index, sequence.get(i));
            }

        System.out.println(n - LIS.size());
    }
}