package A2;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;


public class Test {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        int n = input.nextInt();

        for (int i = 0; i < n; i++) {
            int lockNum = input.nextInt();
            int keyNum = input.nextInt();
            int[] locks = new int[lockNum];

            for (int j = 0; j < lockNum; j++) {
                locks[j] = input.nextInt();
            }

            Queue<int[]> keyQueue = new LinkedList<>();
            for (int j = 0; j < keyNum; j++) {
                int unlockNum = input.nextInt();
                int[] unlocks = new int[unlockNum];
                for (int k = 0; k < unlockNum; k++) {
                    unlocks[k] = input.nextInt();
                }
                keyQueue.add(unlocks);
            }

            int currentLockIndex = 0;
            int attempts = 0;
            boolean[] unlocked = new boolean[lockNum];

            while (currentLockIndex < lockNum && attempts < keyNum) {
                int[] currentKey = keyQueue.poll();
                if (currentKey == null) break;

                boolean found = false;
                for (int type : currentKey) {
                    if (type == locks[currentLockIndex]) {
                        unlocked[currentLockIndex] = true;
                        currentLockIndex++;
                        attempts = 0;
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    keyQueue.add(currentKey);
                    attempts++;
                }
            }

            boolean allUnlocked = true;
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < lockNum; j++) {
                if (!unlocked[j]) {
                    allUnlocked = false;
                    sb.append(locks[j]).append(" ");
                }
            }

            System.out.println(allUnlocked ? "NULL" : sb.toString().trim());
        }
        input.close();
    }
}
