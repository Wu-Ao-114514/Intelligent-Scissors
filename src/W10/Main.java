package W10;

import edu.princeton.cs.algs4.BTree;
import edu.princeton.cs.algs4.StdOut;

public class Main {
    public static void main(String[] args) {
        BTree<Integer, Double> tree = new BTree<>();
        tree.put(1, 1.0);
        tree.put(2, 2.0);

        StdOut.println(tree.get(1));
        StdOut.println(tree.get(2));
        StdOut.println(tree.get(3));

    }
}
