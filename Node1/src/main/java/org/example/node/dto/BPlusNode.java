package org.example.node.dto;

import java.util.ArrayList;
import java.util.List;

public class BPlusNode {
    public boolean isLeaf;
    public List<String> keys;
    public List<BPlusNode> children;
    public List<List<String>> docIdsList;
    public List<String> docIds;
    public BPlusNode next;

    public BPlusNode() {
        keys = new ArrayList<>();
        children = new ArrayList<>();
        docIdsList = new ArrayList<>();
        docIds = new ArrayList<>();
        next = null;
    }
}
