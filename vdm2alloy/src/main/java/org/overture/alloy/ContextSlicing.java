package org.overture.alloy;

import org.overture.ast.node.INode;
import org.overture.ast.node.NodeList;

import java.util.List;

/**
 * Created by macbookpro on 24/04/15.
 */
public class ContextSlicing {

    private List<String> nodes;

    public List<String> getNodes() {
        return nodes;
    }

    @Override
    public String toString() {
        return "ContextSlicing{" +
                "nodes=" + nodes +
                '}';
    }

    public ContextSlicing(List<String> nodes) {
        this.nodes = nodes;
    }
}
