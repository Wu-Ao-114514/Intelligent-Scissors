package A2;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class RandomDataGenerator {
    public static void main(String[] args) {
        int numLocks = 6; // Number of locks
        int numKeys = 5;  // Number of keys
        int lockRange = 10; // Range of lock types
        int maxUnlockTypes = 3; // Maximum types a key can unlock
        Random random = new Random();

        try (FileWriter writer = new FileWriter("random_data.txt")) {
            // Write number of cases
            writer.write("1\n");
            // Write locks and keys count
            writer.write(numLocks + " " + numKeys + "\n");

            // Generate locks
            for (int i = 0; i < numLocks; i++) {
                writer.write((random.nextInt(lockRange) + 1) + "\n"); // Lock type from 1 to lockRange
            }

            // Generate keys
            for (int i = 0; i < numKeys; i++) {
                int unlockNum = random.nextInt(maxUnlockTypes) + 1; // Random number of unlock types
                writer.write(unlockNum + " ");
                for (int j = 0; j < unlockNum; j++) {
                    writer.write((random.nextInt(lockRange) + 1) + " "); // Unlock types
                }
                writer.write("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
