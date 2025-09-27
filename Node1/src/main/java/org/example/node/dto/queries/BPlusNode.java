package org.example.node.dto.queries;

import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BPlusNode {
    public boolean isLeaf;
    public List<String> keys;
    public List<BPlusNode> children;
    public List<List<String>> docIdsList;
    public List<String> docIds;
    public BPlusNode next;

}
