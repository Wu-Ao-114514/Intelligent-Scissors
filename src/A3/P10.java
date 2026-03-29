package A3;

import java.util.PriorityQueue;
import java.util.Scanner;

public class P10 {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        PriorityQueue<Integer> pq = new PriorityQueue<>();

        int n = input.nextInt();
        for (int i = 0; i < n; i++) {
            pq.add(input.nextInt());
        }
        int sum;
        int total = 0;
        while (pq.size() > 1) {
            int a = pq.poll();
            int b = pq.poll();
            sum = a+b;
            total += sum;
            pq.add(sum);
        }
        System.out.println(total);
    }
}
