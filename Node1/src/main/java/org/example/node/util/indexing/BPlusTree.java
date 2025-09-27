package org.example.node.util.indexing;

import org.example.node.dto.queries.BPlusNode;
import java.util.ArrayList;
import java.util.List;

public class BPlusTree {

    private BPlusNode root;
    private int maxKeys;
    private boolean unique;
    private String fieldType;

    public BPlusTree(int maxKeys, boolean unique, String fieldType) {
        this.root = new BPlusNode();
        this.root.isLeaf = true;
        this.maxKeys = maxKeys;
        this.unique = unique;
        this.fieldType = fieldType.toLowerCase();
    }

    public void insert(String key, String docId) {
        if (unique) insertUnique(root, key, docId);
        else insertNonUnique(root, key, docId);
    }

    public List<String> search(String key) {
        BPlusNode node = root;
        while (!node.isLeaf) {
            int i = 0;
            while (i < node.keys.size() && compareKeys(key, node.keys.get(i)) >= 0) i++;
            node = node.children.get(i);
        }

        if (unique) {
            int idx = node.keys.indexOf(key);
            if (idx != -1) return new ArrayList<>(List.of(node.docIds.get(idx)));
        } else {
            int idx = node.keys.indexOf(key);
            if (idx != -1) return new ArrayList<>(node.docIdsList.get(idx));
        }
        return new ArrayList<>();
    }

    private void insertUnique(BPlusNode leaf, String key, String docId) {
        int i = 0;
        while (i < leaf.keys.size() && compareKeys(key, leaf.keys.get(i)) > 0) i++;
        leaf.keys.add(i, key);
        leaf.docIds.add(i, docId);
    }

    private void insertNonUnique(BPlusNode leaf, String key, String docId) {
        int i = leaf.keys.indexOf(key);
        if (i != -1) {
            leaf.docIdsList.get(i).add(docId);
        } else {
            i = 0;
            while (i < leaf.keys.size() && compareKeys(key, leaf.keys.get(i)) > 0) i++;
            leaf.keys.add(i, key);
            List<String> list = new ArrayList<>();
            list.add(docId);
            leaf.docIdsList.add(i, list);
        }
    }

    public List<String> rangeQuery(String startKey, boolean startInclusive, String endKey, boolean endInclusive) {
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

                if (afterStart && beforeEnd) {
                    if (unique) result.add(node.docIds.get(i));
                    else result.addAll(node.docIdsList.get(i));
                }
            }
            node = node.next;
        }

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
            throw e;
        }
    }
}
