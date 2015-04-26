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
public class NewSlicing extends QuestionAnswerAdaptor<NewContextSlicing,NodeList> {

    NodeList<INode> nodeList =  new NodeList(null);

    public NewSlicing(String module) {
        this.module = module;
    }

    public String module;

    public NodeList getNodeList() {
        return nodeList;
    }

    @Override
    public NodeList createNewReturnValue(INode iNode, NewContextSlicing newContextSlicing) throws AnalysisException {
        return null;
    }

    @Override
    public NodeList createNewReturnValue(Object o, NewContextSlicing newContextSlicing) throws AnalysisException {
        return null;
    }

    @Override
    public NodeList caseAModuleModules(AModuleModules node, NewContextSlicing question) throws AnalysisException {

        for (PDefinition p : node.getDefs())
        {
            nodeList.addAll(p.getType().apply(this, question));
        }
        return nodeList;
    }

    @Override
    public NodeList caseANamedInvariantType(ANamedInvariantType node, NewContextSlicing question) throws AnalysisException {
        question.init();
        question.addType(node.toString(),node.getType().toString());
        question.setRecord(false);


        node.getType().apply(this,question);

        if(question.isNotAllowed()){
            this.nodeList.add(node);
        }else {
            if (!question.invAllowed(node.toString()) )  {

                this.nodeList.add(node);
            }
        }
        return this.nodeList;
    }

    @Override
    public NodeList caseARecordInvariantType(ARecordInvariantType node, NewContextSlicing question) throws AnalysisException {
        question.init();
        question.setRecord(true);
        int flag=1;
        for(AFieldField ff : node.getFields()){
            if(question.invAllowed(ff.getType().toString())){flag=0;}
                ff.getType().apply(this,question);

        }

        if(question.isNotAllowed() && flag==1){this.nodeList.add(node);}
        return this.nodeList;
    }

    @Override
    public NodeList caseAUnionType(AUnionType node, NewContextSlicing question) throws AnalysisException {
        for(PType pt : node.getTypes()){
           pt.apply(this, question);
        }

        return this.nodeList;
    }

    @Override
    public NodeList caseARealNumericBasicType(ARealNumericBasicType node, NewContextSlicing question) throws AnalysisException {
        question.setNotAllowed(false);
        return this.nodeList;
    }

    @Override
    public NodeList caseABooleanBasicType(ABooleanBasicType node, NewContextSlicing question) throws AnalysisException {
        question.setNotAllowed(false);
        return this.nodeList;
    }

    @Override
    public NodeList caseAMapMapType(AMapMapType node, NewContextSlicing question) throws AnalysisException {
        if(!question.isRecord()){question.setNotAllowed(false);}
        return this.nodeList;
    }

    @Override
    public NodeList caseAExplicitFunctionDefinition(AExplicitFunctionDefinition node, NewContextSlicing question) throws AnalysisException {
        return this.nodeList;
    }

    public void p(String string){
        System.out.println(string);
    }
}
