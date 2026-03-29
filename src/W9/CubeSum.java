package W9;

import edu.princeton.cs.algs4.StdOut;

import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.Set;

public class CubeSum {

    private static class CubePair implements Comparable<CubePair> {
        int sum;  // a^3 + b^3
        int a;    // a 的值
        int b;    // b 的值

        // 构造函数
        public CubePair(int sum, int a, int b) {
            this.sum = sum;
            this.a = a;
            this.b = b;
        }

        // 实现比较功能，以便在优先队列中按 sum 排序
        @Override
        public int compareTo(CubePair other) {
            return Integer.compare(this.sum, other.sum);
        }
    }

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        // 从命令行读取 N 的值
        StdOut.print("Input N:");
        int N = input.nextInt();
        // 创建优先队列
        PriorityQueue<CubePair> queue = new PriorityQueue<>();

        // 初始化队列
        for (int i = 0; i <= N; i++) {
            queue.offer(new CubePair(i * i * i, i, 0)); // a^3, a, b=0
        }

        // 使用集合来避免重复值
        Set<Integer> printed = new HashSet<>();

        // 处理优先队列
        while (!queue.isEmpty()) {
            CubePair pair = queue.poll(); // 取出当前最小的元素
            int sum = pair.sum;
            int a = pair.a;
            int b = pair.b;

            // 如果该和尚未打印过，打印它
            if (!printed.contains(sum)) {
                System.out.println(sum);
                printed.add(sum);
            }

            // 如果 j < N，插入 (a^3 + (b+1)^3, a, b+1)
            if (b < N) {
                int newSum = a * a * a + (b + 1) * (b + 1) * (b + 1);
                queue.offer(new CubePair(newSum, a, b + 1));
            }
        }
    }
}
