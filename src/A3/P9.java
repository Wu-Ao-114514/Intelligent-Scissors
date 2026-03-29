package A3;

import java.util.*;

public class P9 {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);

        int z = input.nextInt();
        int a = input.nextInt();

        String[] NFTs = new String[z];

        for (int i = 0; i < z; i++) {
            NFTs[i] = input.next();
        }

        List<List<String>> zoneAccessors = new ArrayList<>();
        for (int i = 0; i < z; i++) {
            zoneAccessors.add(new ArrayList<>());
        }

        for (int i = 0; i < a; i++) {
            String owner = input.next();
            int num = input.nextInt();
            Set<String> ownedNFTs = new HashSet<>();

            for (int j = 0; j < num; j++) {
                String nftName = input.next();
                ownedNFTs.add(nftName);
            }

            for (int j = 0; j < z; j++) {
                if (ownedNFTs.contains(NFTs[j])) {
                    zoneAccessors.get(j).add(owner);
                }
            }
        }

        for (List<String> accessList : zoneAccessors) {
            if (accessList.isEmpty()) {
                System.out.println();
            } else {
                System.out.println(String.join(" ", accessList));
            }
        }

        input.close();
    }
}
