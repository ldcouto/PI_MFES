package org.overture.alloy;

import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.analysis.QuestionAnswerAdaptor;
import org.overture.ast.definitions.AExplicitFunctionDefinition;
import org.overture.ast.definitions.PDefinition;
import org.overture.ast.modules.AModuleModules;
import org.overture.ast.node.INode;
import org.overture.ast.node.NodeList;
import org.overture.ast.types.*;

/**
 * Created by macbookpro on 26/04/15.
 */
public class Slicing extends QuestionAnswerAdaptor<ContextSlicing,NodeList> {

    NodeList<INode> nodeList =  new NodeList(null);
   ;

    public Slicing(String module) {
        this.module = module;
    }

    public String module;

    public NodeList getNodeList() {
        return nodeList;
    }

    @Override
    public NodeList createNewReturnValue(INode iNode, ContextSlicing newContextSlicing) throws AnalysisException {
        return null;
    }

    @Override
    public NodeList createNewReturnValue(Object o, ContextSlicing newContextSlicing) throws AnalysisException {
        return null;
    }



    @Override
    public NodeList caseAModuleModules(AModuleModules node, ContextSlicing question) throws AnalysisException {

        for (PDefinition p : node.getDefs())
        {
            question.init();
            p.getType().apply(this, question);
            if(question.isNotAllowed()){nodeList.add(p.getType());}
        }
        return nodeList;
    }

    @Override
    public NodeList caseANamedInvariantType(ANamedInvariantType node, ContextSlicing question) throws AnalysisException {
        node.getType().apply(this, question);
        return this.nodeList;
    }

    @Override
    public NodeList caseARecordInvariantType(ARecordInvariantType node, ContextSlicing question) throws AnalysisException {
        question.setRecord(true);
        for(AFieldField ff : node.getFields()){
            ff.getType().apply(this,question);

        }
        return this.nodeList;
    }

    @Override
    public NodeList caseAUnionType(AUnionType node, ContextSlicing question) throws AnalysisException {
        for(PType pt : node.getTypes()){
            pt.apply(this, question);
        }

        return this.nodeList;
    }

    @Override
    public NodeList caseARealNumericBasicType(ARealNumericBasicType node, ContextSlicing question) throws AnalysisException {
        question.setNotAllowed(false);
        return this.nodeList;
    }

    @Override
    public NodeList caseABooleanBasicType(ABooleanBasicType node, ContextSlicing question) throws AnalysisException {
        question.setNotAllowed(false);
        return this.nodeList;
    }

    @Override
    public NodeList caseAMapMapType(AMapMapType node, ContextSlicing question) throws AnalysisException {
        if(!question.isRecord()){question.setNotAllowed(false);}
        else{
            node.getTo().apply(this, question);
            node.getFrom().apply(this,question);
        }
        return this.nodeList;
    }

    @Override
    public NodeList caseAExplicitFunctionDefinition(AExplicitFunctionDefinition node, ContextSlicing question) throws AnalysisException {
        return this.nodeList;
    }


    @Override
    public NodeList caseASeqSeqType(ASeqSeqType node, ContextSlicing question) throws AnalysisException {
        node.getSeqof().apply(this, question);
        return nodeList;
    }

    @Override
    public NodeList caseASetType(ASetType node, ContextSlicing question) throws AnalysisException {
        node.getSetof().apply(this,question);
        return nodeList;
    }








    public void p(String string){
        System.out.println(string);
    }
}
