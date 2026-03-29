package test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;

public class A8 {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        int testCaseCount = input.nextInt();

        for (int i = 0; i < testCaseCount; i++) {
            int lockNum = input.nextInt();
            int keyNum = input.nextInt();

            Queue<Lock> locks = new LinkedList<>();
            for (int j = 0; j < lockNum; j++) {
                int lockType = input.nextInt();
                locks.add(new Lock(lockType));
            }

            List<Key> keyList = new ArrayList<>();
            for (int j = 0; j < keyNum; j++) {
                int unlockNum = input.nextInt();
                Set<Integer> unlockTypes = new HashSet<>();
                for (int k = 0; k < unlockNum; k++) {
                    unlockTypes.add(input.nextInt());
                }
                keyList.add(new Key(unlockTypes));
            }

            while (!locks.isEmpty()) {
                boolean unlocked = false;
                int initialSize = keyList.size();

                for (int j = 0; j < initialSize; j++) {
                    Key currentKey = keyList.get(j);

                    if (currentKey.unlock(locks.peek().getType())) {
                        locks.poll();
                        unlocked = true;
                        break;
                    } else {
                        keyList.add(currentKey);
                    }
                }

                if (!unlocked) {
                    break;
                }

                keyList = keyList.subList(initialSize, keyList.size());
            }

            if (locks.isEmpty()) {
                System.out.println("NULL");
            } else {
                StringBuilder remainingLocks = new StringBuilder();
                for (Lock lock : locks) {
                    remainingLocks.append(lock.getType()).append(" ");
                }
                System.out.println(remainingLocks.toString().trim());
            }
        }
        input.close();
    }
}

class Key {
    private final Set<Integer> unlockableLocks;

    public Key(Set<Integer> unlockableLocks) {
        this.unlockableLocks = unlockableLocks;
    }

    public boolean unlock(int lockType) {
        return unlockableLocks.contains(lockType);
    }
}

class Lock {
    private final int type;

    public Lock(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }
}