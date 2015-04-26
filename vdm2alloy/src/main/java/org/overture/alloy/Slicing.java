package org.overture.alloy;

import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.analysis.QuestionAnswerAdaptor;
import org.overture.ast.definitions.AExplicitFunctionDefinition;
import org.overture.ast.definitions.PDefinition;
import org.overture.ast.modules.AModuleModules;
import org.overture.ast.node.INode;
import org.overture.ast.node.NodeList;
import org.overture.ast.types.*;

import javax.xml.soap.Node;
import java.util.ArrayList;
import java.util.List;


public class Slicing  extends QuestionAnswerAdaptor<ContextSlicing,NodeList> {



    NodeList<INode> nodeList =  new NodeList(null);

    public String module;

    public Slicing(String module){
        this.module=module;
    }

    @Override
    public NodeList createNewReturnValue(INode iNode, ContextSlicing contextSlicing) throws AnalysisException {
        return null;
    }

    public NodeList getNodeList() {
        return nodeList;
    }

    @Override
    public NodeList createNewReturnValue(Object o, ContextSlicing contextSlicing) throws AnalysisException {
        return null;

    }


    @Override
    public NodeList caseAModuleModules(AModuleModules node, ContextSlicing question) throws AnalysisException {

         for (PDefinition p : node.getDefs())
        {
            nodeList.addAll(p.getType().apply(this, question));
        }

        return nodeList;
    }

    @Override
    public NodeList caseANamedInvariantType(ANamedInvariantType node, ContextSlicing question) throws AnalysisException {

        question.getNodes().add(node.toString());

        question.addType(node.toString(), node.getType().toString());
        if(question.invAddTypes(node.getType().toString())!= 1){
                nodeList.add(node);
                nodeList.addAll(node.getType().apply(this, question));
                if(node.getInvDef()!=null){
                    nodeList.addAll(node.getInvDef().apply(this, question));
                }
            }
      //  p(nodeList.toString());
         return nodeList;
    }

    @Override
    public NodeList caseAExplicitFunctionDefinition(AExplicitFunctionDefinition node, ContextSlicing question) throws AnalysisException {
        nodeList.add(node);
        return nodeList;
    }

    @Override
    public NodeList caseATokenBasicType(ATokenBasicType node, ContextSlicing question) throws AnalysisException {
        question.addContext(node);
        nodeList.add(node);
        return nodeList;
    }

    @Override
    public NodeList caseARealNumericBasicType(ARealNumericBasicType node, ContextSlicing question) throws AnalysisException {
        question.getNodes().add(node.toString());
        question.addContext(node);
        nodeList.remove(node.parent());
        question.setModelValide(true);
        return nodeList;
    }

    @Override
    public NodeList caseABooleanBasicType(ABooleanBasicType node, ContextSlicing question) throws AnalysisException {
        question.addContext(node);
        nodeList.remove(node.parent());
        question.setModelValide(true);
        //p("entra");
        return nodeList;
    }



    @Override
    public NodeList caseAUnionType(AUnionType node, ContextSlicing question) throws AnalysisException {
        question.initContext();
        question.addContext(node.parent());

        for(PType pt : node.getTypes()){
            nodeList.addAll(pt.apply(this,question));
        }
        if(!question.intersetionTypes()){
            question.getContext().remove(node.parent());
            nodeList.removeAll(question.getContext());
            nodeList.removeLast();
        }
        else{
            question.getContext().remove(node.parent());
            nodeList.removeAll(question.getContext());
            nodeList.removeLast();
            nodeList.add(node.parent());
            nodeList.add(node);
        }

        return nodeList;
    }

    @Override
    public NodeList caseAQuoteType(AQuoteType node, ContextSlicing question) throws AnalysisException {
        question.addContext(node);
        nodeList.add(node);
        return nodeList;
    }

    @Override
    public NodeList caseANatNumericBasicType(ANatNumericBasicType node, ContextSlicing question) throws AnalysisException {
        //nodeList.add(node);
        return nodeList;
    }

    @Override
    public NodeList caseARecordInvariantType(ARecordInvariantType node, ContextSlicing question) throws AnalysisException {
        question.initContext();
        for(AFieldField ff : node.getFields()){
                ff.getType().apply(this,question);
    }

        if(!question.isModelValide()){nodeList.add(node);}

        return nodeList;
    }

    @Override
    public NodeList caseAMapMapType(AMapMapType node, ContextSlicing question) throws AnalysisException {
        node.getTo().apply(this, question);
        node.getFrom().apply(this,question);
        return nodeList;
    }

    public void p(String string){
        System.out.println(string);
    }
}
