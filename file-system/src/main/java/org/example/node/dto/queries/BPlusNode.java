package org.example.node.dto.queries;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class BPlusNode {
    public boolean isLeaf;
    public List<String> keys;
    public List<BPlusNode> children;
    public List<List<String>> docIdsList;
    public List<String> docIds;
    public BPlusNode next;

    public BPlusNode() {
        this.isLeaf = false;
        this.keys = new ArrayList<>();
        this.children = new ArrayList<>();
        this.docIdsList = new ArrayList<>();
        this.docIds = new ArrayList<>();
        this.next = null;
    }
}
