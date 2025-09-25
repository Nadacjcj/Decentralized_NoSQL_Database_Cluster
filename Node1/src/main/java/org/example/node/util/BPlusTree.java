package org.example.node.util;

import org.example.node.dto.BPlusNode;
import java.util.ArrayList;
import java.util.List;


// Remove the dubugging statements and add LOG4J

public class BPlusTree {

    private BPlusNode root;
    private int maxKeys;
    private boolean unique;
    private String fieldType; // "integer", "double", "string", "boolean"

    public BPlusTree(int maxKeys, boolean unique, String fieldType) {
        this.root = new BPlusNode();
        this.root.isLeaf = true;
        this.maxKeys = maxKeys;
        this.unique = unique;
        this.fieldType = fieldType.toLowerCase();
        System.out.println("[DEBUG] Created BPlusTree: unique=" + unique + ", type=" + fieldType);
    }

    public void insert(String key, String docId) {
        System.out.println("[DEBUG] Inserting key=" + key + ", docId=" + docId);
        if (unique) insertUnique(root, key, docId);
        else insertNonUnique(root, key, docId);
    }

    public List<String> search(String key) {
        System.out.println("[DEBUG] Searching for key=" + key);
        BPlusNode node = root;
        while (!node.isLeaf) {
            int i = 0;
            while (i < node.keys.size() && compareKeys(key, node.keys.get(i)) >= 0) i++;
            node = node.children.get(i);
        }

        if (unique) {
            int idx = node.keys.indexOf(key);
            System.out.println("[DEBUG] Unique search index=" + idx);
            if (idx != -1) return new ArrayList<>(List.of(node.docIds.get(idx)));
        } else {
            int idx = node.keys.indexOf(key);
            System.out.println("[DEBUG] Non-unique search index=" + idx);
            if (idx != -1) return new ArrayList<>(node.docIdsList.get(idx));
        }
        return new ArrayList<>();
    }

    private void insertUnique(BPlusNode leaf, String key, String docId) {
        int i = 0;
        while (i < leaf.keys.size() && compareKeys(key, leaf.keys.get(i)) > 0) i++;
        leaf.keys.add(i, key);
        leaf.docIds.add(i, docId);
        System.out.println("[DEBUG] Inserted unique key=" + key + " at position=" + i);
    }

    private void insertNonUnique(BPlusNode leaf, String key, String docId) {
        int i = leaf.keys.indexOf(key);
        if (i != -1) {
            leaf.docIdsList.get(i).add(docId);
            System.out.println("[DEBUG] Added to existing key=" + key + " docId=" + docId);
        } else {
            i = 0;
            while (i < leaf.keys.size() && compareKeys(key, leaf.keys.get(i)) > 0) i++;
            leaf.keys.add(i, key);
            List<String> list = new ArrayList<>();
            list.add(docId);
            leaf.docIdsList.add(i, list);
            System.out.println("[DEBUG] Inserted non-unique key=" + key + " at position=" + i);
        }
    }

    public List<String> rangeQuery(String startKey, boolean startInclusive, String endKey, boolean endInclusive) {
        System.out.println("[DEBUG] rangeQuery start=" + startKey + " inclusive=" + startInclusive
                + " end=" + endKey + " inclusive=" + endInclusive);
        List<String> result = new ArrayList<>();
        BPlusNode node = root;

        while (!node.isLeaf) {
            node = node.children.get(0);
        }

        while (node != null) {
            for (int i = 0; i < node.keys.size(); i++) {
                String key = node.keys.get(i);
                int startCmp = startKey == null ? 1 : compareKeys(key, startKey);
                int endCmp = endKey == null ? -1 : compareKeys(key, endKey);

                boolean afterStart = startKey == null || (startInclusive ? startCmp >= 0 : startCmp > 0);
                boolean beforeEnd = endKey == null || (endInclusive ? endCmp <= 0 : endCmp < 0);

                System.out.println("[DEBUG] Checking key=" + key + " afterStart=" + afterStart + " beforeEnd=" + beforeEnd);

                if (afterStart && beforeEnd) {
                    if (unique) result.add(node.docIds.get(i));
                    else result.addAll(node.docIdsList.get(i));
                }
            }
            node = node.next;
        }

        System.out.println("[DEBUG] rangeQuery result size=" + result.size());
        return result;
    }

    private int compareKeys(String a, String b) {
        try {
            switch (fieldType) {
                case "integer":
                    return Integer.compare(Integer.parseInt(a), Integer.parseInt(b));
                case "double":
                    return Double.compare(Double.parseDouble(a), Double.parseDouble(b));
                case "boolean":
                    return Boolean.compare(Boolean.parseBoolean(a), Boolean.parseBoolean(b));
                default:
                    return a.compareTo(b);
            }
        } catch (Exception e) {
            System.out.println("[ERROR] compareKeys failed for a=" + a + " b=" + b + " type=" + fieldType);
            throw e;
        }
    }
}
