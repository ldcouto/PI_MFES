package org.overture.alloy;

import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.analysis.QuestionAnswerAdaptor;
import org.overture.ast.definitions.PDefinition;
import org.overture.ast.expressions.ARealLiteralExp;
import org.overture.ast.modules.AModuleModules;
import org.overture.ast.node.INode;
import org.overture.ast.node.NodeList;
import org.overture.ast.types.ABooleanBasicType;
import org.overture.ast.types.ANamedInvariantType;
import org.overture.ast.types.ARealNumericBasicType;

import javax.xml.soap.Node;


public class Slicing  extends QuestionAnswerAdaptor<ContextSlicing,NodeList> {

    public String module;

    public Slicing(String module){
        this.module=module;
    }

    @Override
    public NodeList createNewReturnValue(INode iNode, ContextSlicing contextSlicing) throws AnalysisException {
        return null;
    }

    @Override
    public NodeList createNewReturnValue(Object o, ContextSlicing contextSlicing) throws AnalysisException {
        return null;
    }


    @Override
    public NodeList caseAModuleModules(AModuleModules node, ContextSlicing question) throws AnalysisException {
        NodeList nodeList=new NodeList(node);
        //p(node.toString());
        //p(question.toString());
        for (PDefinition p : node.getDefs())
        {
            p.getType().apply(this,question);
           // p(p.getType().getClass().getSimpleName());
            //nodeList.push(new PONameContext(assistantFactory.createPDefnitionAssistant().getVariableNames(p)));
            //nodeList.addAll(p.apply(this, question));
          //  p(nodeList.toString());
            //nodeList.add(p);
            //question.pop();
            //question.clearStateContexts();
        }
       // p(nodeList.toString());
        return nodeList;
    }

    @Override
    public NodeList caseANamedInvariantType(ANamedInvariantType node, ContextSlicing question) throws AnalysisException {
        //  p(node.toString());
       // p(node.getType().getClass().getSimpleName());
        node.getType().apply(this,question);
        return super.caseANamedInvariantType(node, question);
    }

    @Override
    public NodeList caseARealNumericBasicType(ARealNumericBasicType node, ContextSlicing question) throws AnalysisException {
        p("Aqui esta o numeor"+node.toString());
        return super.caseARealNumericBasicType(node, question);
    }

    @Override
    public NodeList caseABooleanBasicType(ABooleanBasicType node, ContextSlicing question) throws AnalysisException {
        p("Aqui esta o Boolean"+node.toString());
        return super.caseABooleanBasicType(node, question);
    }

    public void p(String string){
        System.out.println(string);
    }
}
