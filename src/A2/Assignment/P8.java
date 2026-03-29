package A2;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

public class P8 {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        int n = input.nextInt();
        for (int i = 0; i < n; i++) {
            int lockNum = input.nextInt();
            int keyNum = input.nextInt();
            Lock[] locks = new Lock[lockNum];

            for (int j = 0; j < lockNum; j++) {
                int lockType = input.nextInt();
                locks[j] = new Lock(lockType);
            }

            Queue<Key> keyQueue = new LinkedList<>();
            for (int j = 0; j < keyNum; j++) {
                int unlockCount = input.nextInt();
                int[] unlocks = new int[unlockCount];
                for (int k = 0; k < unlockCount; k++) {
                    unlocks[k] = input.nextInt();
                }
                Key key = new Key(unlocks);
                keyQueue.add(key);
            }

            for (int k = 0; k < lockNum; k++) {
                int queueSize = keyQueue.size();
                boolean unlocked = false;

                for (int j = 0; j < queueSize; j++) {
                    Key currentKey = keyQueue.poll();
                    if (currentKey != null && currentKey.unlock(locks[k].getType())) {
                        locks[k].unLock();
                        unlocked = true;
                        break;
                    } else {
                        keyQueue.add(currentKey);
                    }
                }
                if (!unlocked) {
                    break;
                }
            }

            if (Lock.allSolved(locks)) {
                System.out.print("NULL");
            } else {
                for (int k = 0; k < lockNum; k++) {
                    if (locks[k].isLocked()) {
                        System.out.print(locks[k].getType());
                        if (k != lockNum - 1) System.out.print(" ");
                    }
                }
            }
            System.out.println();
        }
        input.close();
    }
}

class Key {
    private final int[] locks;

    public Key(int[] locks) {
        Arrays.sort(locks);
        this.locks = locks;
    }

    public boolean unlock(int lock) {
        return Arrays.binarySearch(locks, lock) >= 0;
    }
}

class Lock {
    private boolean locked;
    private final int type;

    public Lock(int type) {
        this.locked = true;
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public boolean isLocked() {
        return locked;
    }

    public void unLock() {
        this.locked = false;
    }

    public static boolean allSolved(Lock[] locks) {
        for (Lock lock : locks) {
            if (lock.isLocked()) return false;
        }
        return true;
    }
}