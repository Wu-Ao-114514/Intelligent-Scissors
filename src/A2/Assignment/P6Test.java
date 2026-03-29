package A2;

import java.util.Arrays;
import java.util.Random;
import static A2.P6.minCost;

public class P6Test {
    public static void main(String[] args) {
        int[] ints = generateData();
        int[] copy = Arrays.stream(ints).toArray();
        long cost = minCost(ints);
        System.out.printf("Cost: %s, %b",cost,cost==minCostToSort(copy));
    }
    public static int[] generateData() {
        Random random = new Random();
        long n = random.nextLong(1,100000);
        int[] ints = new int[Math.toIntExact(n)];
        System.out.printf("n=%d\n",n);
        for (int i = 0; i < n; i++) {
            ints[i] = random.nextInt(1000000000);
        }
        return ints;
    }
    public static long minCostToSort(int[] a){
        long cost = 0;

        for (int i = 0; i < a.length; i++) {
            int minPosition = findMinPosition(a,i);
            int min = a[minPosition];
            for (int j = minPosition; j > i; j--) {
                exchange(a,j,j-1);
                cost += min;
            }
        }
        return cost;
    }
    private static void exchange (int[] ints,int i, int j) {
        int k = ints[i];
        ints[i] = ints[j];
        ints[j] = k;
    }
    private static int findMinPosition(int[] ints,int i) {
        int min = ints[i];
        int position = i;
        for (int j = i; j < ints.length; j++) {
            if (ints[j] < min) {
                min = ints[j];
                position = j;
            }
        }
        return position;
    }
}
