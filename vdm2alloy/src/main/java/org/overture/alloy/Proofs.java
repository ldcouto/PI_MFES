package org.overture.alloy;


import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.definitions.PDefinition;
import org.overture.ast.modules.AModuleModules;
import org.overture.ast.node.INode;
import org.overture.pof.AVdmPoTree;
import org.overture.pog.obligation.ProofObligation;
import org.overture.pog.pub.*;

import java.util.List;
import java.util.Vector;


/**
 * Created by macbookpro on 22/05/15.
 */
public class Proofs {
    AModuleModules md;
    public INode node;

    public Proofs(INode node) throws AnalysisException {
        this.node=this.getINodeProofObligation(node);
    }

    public INode getINodeProofObligation(INode node) throws AnalysisException {
        ProofObligation po;
        po = (ProofObligation) ProofObligationGenerator.generateProofObligations(node).get(0);
        return po.stitch;
    }

    public INode getNode() {
        return node;
    }





    public void p(String string){
        System.out.println(string);
    }
}
