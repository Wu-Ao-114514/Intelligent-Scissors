package A2;

import java.util.Scanner;

public class P6 {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        int n = input.nextInt();
        int[] nums = new int[n];
        for (int i = 0; i < n; i++) {
            nums[i] = input.nextInt();
        }
        System.out.println(minCost(nums));
    }

    private static long mergeSort(int[] nums, int[] temp, int left, int right) {
        long cost = 0;
        if (left < right) {
            int mid = (left + right) / 2;

            cost += mergeSort(nums, temp, left, mid);
            cost += mergeSort(nums, temp, mid + 1, right);
            cost += merge(nums, temp, left, mid, right);
        }
        return cost;
    }

    private static long merge(int[] nums, int[] temp, int left, int mid, int right) {
        long cost = 0;

        if (right + 1 - left >= 0) System.arraycopy(nums, left, temp, left, right + 1 - left);

        int i = left;
        int j = mid + 1;
        int k = left;

        while (i <= mid && j <= right) {
            if (temp[i] <= temp[j]) {
                nums[k++] = temp[i++];
            } else {
                cost += (long) (mid - i + 1) * temp[j];
                nums[k++] = temp[j++];
            }
        }

        while (i <= mid) {
            nums[k++] = temp[i++];
        }
        while (j <= right) {
            nums[k++] = temp[j++];
        }
        return cost;
    }
    public static long minCost(int[] nums) {
        int[] temp = new int[nums.length];
        return mergeSort(nums, temp, 0, nums.length - 1);
    }
}
