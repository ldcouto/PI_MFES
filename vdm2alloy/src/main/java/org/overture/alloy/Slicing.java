package org.overture.alloy;

import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.analysis.QuestionAnswerAdaptor;
import org.overture.ast.definitions.PDefinition;
import org.overture.ast.modules.AModuleModules;
import org.overture.ast.node.INode;
import org.overture.ast.node.NodeList;
import org.overture.ast.types.ABooleanBasicType;
import org.overture.ast.types.ANamedInvariantType;
import org.overture.ast.types.ARealNumericBasicType;
import org.overture.ast.types.ATokenBasicType;

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

        //p(node.toString());
        //p(question.toString());
      /*  NodeList nodeList=new NodeList(node);
        nodeList.add(node);
        p(nodeList.toString());*/
      //  nodeList = new NodeList(node);
       // nodeList.add(node);

       // NodeList<INode> nodeList =  new NodeList(null);
        for (PDefinition p : node.getDefs())
        {
                  // p.getType().apply(this,question);
           // p(p.getType().getClass().getSimpleName());
            //nodeList.push(new PONameContext(assistantFactory.createPDefnitionAssistant().getVariableNames(p)));
           // nodeList=new NodeList(p,p.getType().apply(this, question));
            //nodeList.addAll(p.getType().apply(this, question));

            nodeList.addAll(p.getType().apply(this, question));

          //  p(nodeList.toString());
            //nodeList.add(p);
            //question.pop();
            //question.clearStateContexts();
        }
        p(nodeList.toString());
        return nodeList;
    }

    @Override
    public NodeList caseANamedInvariantType(ANamedInvariantType node, ContextSlicing question) throws AnalysisException {
       //   p(node.parent().toString());
       // p("node: "+node.toString()+"   "+node.getType().getClass().getSimpleName());
        //NodeList nodeList1 = new NodeList(node);
        //nodeList1.add(node);
       // nodeList.add()


        question.getNodes().add(node.toString());
        p("-----------------------\n" + question.toString());
        nodeList.add(node);
        nodeList.addAll(node.getType().apply(this, question));
        //node.getType().apply(this, question);

       /* nodeList.add(node);
        p(nodeList.toString());
        node.getType().apply(this, question);*/
        /*if(node.getInvDef()!=null){
            p("Invariant é:"+node.getInvDef().toString() );
        }*/
        //return super.caseANamedInvariantType(node, question);
       // nodeList=new NodeList(node);


        return nodeList;
    }


    @Override
    public NodeList caseATokenBasicType(ATokenBasicType node, ContextSlicing question) throws AnalysisException {
        nodeList.add(node);
        return nodeList;
    }

    @Override
    public NodeList caseARealNumericBasicType(ARealNumericBasicType node, ContextSlicing question) throws AnalysisException {
        //nodeList.add(node);
        question.getNodes().add(node.toString());
        p("-----------------------\n"+question.toString());
       // return super.caseARealNumericBasicType(node, question);
       // nodeList.add(node);
        //return new NodeList(node);
        return nodeList;
    }

    @Override
    public NodeList caseABooleanBasicType(ABooleanBasicType node, ContextSlicing question) throws AnalysisException {
        return super.caseABooleanBasicType(node, question);
    }

    public void p(String string){
        System.out.println(string);
    }
}
