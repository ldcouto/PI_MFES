package org.overture.alloy;

import org.overture.ast.node.INode;
import org.overture.ast.node.NodeList;

import java.util.List;
import java.util.Vector;


/**
 * Created by macbookpro on 24/04/15.
 */
public class ContextSlicing {
    NotAllowedTypes aux= new NotAllowedTypes();
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
        List <String> newL = new Vector<String>();
        for(String l :nodes ){
            newL.add(aux.translation(l));
        }
        this.nodes = newL;
    }
}
