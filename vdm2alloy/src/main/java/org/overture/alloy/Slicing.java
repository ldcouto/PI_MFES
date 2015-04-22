package org.overture.alloy;

import org.overture.alloy.ast.Sig;
import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.analysis.QuestionAnswerAdaptor;
import org.overture.ast.analysis.intf.IAnalysis;
import org.overture.ast.definitions.*;
import org.overture.ast.definitions.traces.*;
import org.overture.ast.expressions.*;
import org.overture.ast.intf.lex.*;
import org.overture.ast.modules.*;
import org.overture.ast.node.INode;
import org.overture.ast.node.IToken;
import org.overture.ast.node.tokens.TAsync;
import org.overture.ast.node.tokens.TStatic;
import org.overture.ast.patterns.*;
import org.overture.ast.statements.*;
import org.overture.ast.typechecker.ClassDefinitionSettings;
import org.overture.ast.typechecker.NameScope;
import org.overture.ast.typechecker.Pass;
import org.overture.ast.types.*;
import org.overture.ast.util.ClonableFile;
import org.overture.ast.util.ClonableString;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class Slicing  extends QuestionAnswerAdaptor<Context,Slicing.ListNode> {

    private String module;

    public List<INode> iNodeListGlobal = new ArrayList<INode>();

    @Override
    public ListNode createNewReturnValue(INode iNode, Context context) throws AnalysisException {
        return null;
    }

    @Override
    public ListNode createNewReturnValue(Object o, Context context) throws AnalysisException {
        return null;
    }

    public List<INode> getiNodeListGlobal() {
        return iNodeListGlobal;
    }

    public Slicing(String module) {

        this.module = module;
    }

    @Override
    public ListNode caseAModuleModules(AModuleModules node, Context question) throws AnalysisException {
        ListNode listNode = new ListNode();
        for(PDefinition p : node.getDefs()){
            iNodeListGlobal.add(p);
            question.addInode(p);
            listNode.addInode(p);
            p.getType().apply(this,question);
        }
        return listNode;
    }


    @Override
    public ListNode caseANamedInvariantType(ANamedInvariantType node, Context question) throws AnalysisException {
   // p(node.getType().getClass().toString());
        if(node.getType() instanceof  ARealNumericBasicType){

        }else if(node.getType() instanceof  ABooleanBasicType){

        }
        else if(node.getType() instanceof  AUnionType){

            AUnionType ut = (AUnionType) node.getType();
            for(PType pt : ut.getTypes()) {
                if(pt instanceof  ARealNumericBasicType) {

                }else if (pt instanceof  ABooleanBasicType){

                }
            }
        }else if (node.getType() instanceof SSeqType){

        }else if(node.getType() instanceof ASetType){

        }
        return super.caseANamedInvariantType(node, question);
    }

    @Override
    public ListNode caseARealLiteralExp(ARealLiteralExp node, Context question) throws AnalysisException {

        return super.caseARealLiteralExp(node, question);
    }

    @Override
    public ListNode caseATokenBasicType(ATokenBasicType node, Context question) throws AnalysisException {

        return super.caseATokenBasicType(node, question);
    }

    @Override
    public ListNode defaultPExp(PExp node, Context question) throws AnalysisException {
        return node.apply(this,question);
    }


    public class ListNode{

        public List<INode> iNodeList = new Vector<INode>();


        public void addInode(INode iNode){
            this.iNodeList.add(iNode);
        }

        public List<INode> getiNodeList() {
            return iNodeList;
        }

        @Override
        public String toString() {
            String str = "";
            for(INode i : iNodeList){
                str +=i+"\n";
            }
            return str;
        }

        public void addInodeList(List<INode> inode) {
            this.iNodeList.addAll(inode);
        }


    }

    public void p(String string){
       System.out.println(string);
    }


}
