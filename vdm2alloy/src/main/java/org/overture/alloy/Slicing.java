package org.overture.alloy;

import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.analysis.QuestionAnswerAdaptor;
import org.overture.ast.definitions.PDefinition;
import org.overture.ast.modules.AModuleModules;
import org.overture.ast.node.INode;
import org.overture.ast.node.NodeList;
import org.overture.ast.types.*;

import javax.xml.soap.Node;
import java.util.ArrayList;
import java.util.List;


public class Slicing  extends QuestionAnswerAdaptor<ContextSlicing,NodeList> {


    List<INode> listFinalInode = new ArrayList<INode>();
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
            p("TYPE: " + p.getType().toString());
            nodeList.addAll(p.getType().apply(this, question));
        }
        p("\n\nFINAL:  " + question.getInodes().toString() + "\n\n");
        return nodeList;
    }

    @Override
    public NodeList caseANamedInvariantType(ANamedInvariantType node, ContextSlicing question) throws AnalysisException {

        question.getNodes().add(node.toString());

        question.addType(node.toString(), node.getType().toString());
        if(question.invAddTypes(node.getType().toString())!= 1){
           // p("entra"+node+" TIPO\t"+node.getType());

                nodeList.add(node);
                nodeList.addAll(node.getType().apply(this, question));
            }

        p(question.getInodes().toString()+"\n--------------");
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

        return nodeList;
    }

    @Override
    public NodeList caseABooleanBasicType(ABooleanBasicType node, ContextSlicing question) throws AnalysisException {
        question.addContext(node);
        nodeList.remove(node.parent());
        return nodeList;
    }

    @Override
    public NodeList caseAUnionType(AUnionType node, ContextSlicing question) throws AnalysisException {
        question.initContext();
        question.addContext(node.parent());
        for(PType pt : node.getTypes()){
            nodeList.addAll(pt.apply(this,question));
        }

        if(!question.intersetionTypes()){nodeList.removeAll(question.getContext());}


        return nodeList;
    }

    @Override
    public NodeList caseAQuoteType(AQuoteType node, ContextSlicing question) throws AnalysisException {
        question.addContext(node);
        nodeList.add(node);
        return nodeList;
    }

    @Override
    public NodeList caseARecordInvariantType(ARecordInvariantType node, ContextSlicing question) throws AnalysisException {
        //question.initContext();
        //p("NODE: " + node.parent().parent().toString());
        //question.addContext(node.parent().parent());
        for(AFieldField ff : node.getFields()){
            //p(ff.toString());
            nodeList.add(ff.getType());
            //nodeList.addAll(ff.getType().apply(this,question));
        }

        /*if(!question.intersetionTypes())
            nodeList.removeAll(question.getContext());*/

        return nodeList;
    }

    public void p(String string){
        System.out.println(string);
    }
}
