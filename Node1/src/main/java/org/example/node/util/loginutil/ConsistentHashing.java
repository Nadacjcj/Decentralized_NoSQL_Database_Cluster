package org.example.node.util.loginutil;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.SortedMap;
import java.util.TreeMap;


public class ConsistentHashing {
    private final SortedMap<Integer, String> hashRing = new TreeMap<>();

    public ConsistentHashing(String[] nodes, int virtualNodes) {
        for (String node : nodes) {
            for (int i = 0; i < virtualNodes; i++) {
                int hash = hash(node + "#" + i);
                hashRing.put(hash, node);
            }
        }
    }

    private int hash(String key) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(key.getBytes(StandardCharsets.UTF_8));
            // Take first 4 bytes for int
            return ((digest[0] & 0xFF) << 24) |
                    ((digest[1] & 0xFF) << 16) |
                    ((digest[2] & 0xFF) << 8)  |
                    (digest[3] & 0xFF);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String getNode(String userId) {
        int hash = hash(userId);
        SortedMap<Integer, String> tailMap = hashRing.tailMap(hash);
        int nodeHash = tailMap.isEmpty() ? hashRing.firstKey() : tailMap.firstKey();
        return hashRing.get(nodeHash);
    }

}

