package org.overture.alloy;

import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.analysis.QuestionAnswerAdaptor;
import org.overture.ast.definitions.*;
import org.overture.ast.definitions.traces.*;
import org.overture.ast.expressions.*;
import org.overture.ast.intf.lex.*;
import org.overture.ast.modules.*;
import org.overture.ast.node.INode;
import org.overture.ast.node.NodeList;
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
import org.overture.pog.obligation.ProofObligationList;


/**
 * Created by macbookpro on 26/04/15.
 */
public class NewSlicing extends QuestionAnswerAdaptor<ContextSlicing,NodeList> {

    NodeList<PDefinition> nodeList =  new NodeList(null);

    String name ;
    @Override
    public String toString() {
        if(moduleModules.getDefs().size()>0){return moduleModules.toString();}
        else{return "Slicing error on type : "+this.name;}
    }

    AModuleModules moduleModules = new AModuleModules();

    public AModuleModules getModuleModules() {
        return moduleModules;
    }

    public NewSlicing(String module) {
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
        int i=0,flag=0;
            //p(node.toString());

        this.name=question.getDef();
        for (PDefinition p : node.getDefs())
        {
            if(p.getClass().getSimpleName().equals("AValueDefinition") ){
               AValueDefinition a = (AValueDefinition)p;

                if (question.getDef().equals(a.getPattern().toString()) && p.getClass().getSimpleName().equals(question.getType())) {
                    nodeList.add(p.clone());
                    p.apply(this, question);
                    flag = 1;

                   // moduleModules.setName(a.getPattern());
                }

            }else {
                if (question.getDef().equals(p.getName().getName()) && p.getClass().getSimpleName().equals(question.getType())) {
                    nodeList.add(p.clone());
                    p(p.getClass().getSimpleName().toString());
                    p.apply(this, question);
                    flag = 1;
                    moduleModules.setName(p.getName());
                }
            }
        }

         while(flag!=0){
            if(question.getTypesDep().size()>i){
              if(!nodeList.contains(question.getTypesDep().get(i))) {
                  question.getTypesDep().get(i).apply(this, question);
                  nodeList.add((PDefinition)(question.getTypesDep().get(i)).clone());
              }
            }else{
                flag=0;
            }
            i++;
        }


        moduleModules.setDefs(nodeList);

        return nodeList;
    }

    @Override
    public NodeList caseATypeDefinition(ATypeDefinition node, ContextSlicing question) throws AnalysisException {
        if(!question.getTypesDep().contains(node)) {
               question.addTypesDep(node);
        }
        node.getType().apply(this, question);


        return nodeList;
    }

    @Override
    public NodeList caseANamedInvariantType(ANamedInvariantType node, ContextSlicing question) throws AnalysisException {

        if(!question.getTypesDep().contains(node.getDefinitions().get(0))){
            question.addTypesDep(node.getDefinitions().get(0));
        }

        node.getType().apply(this, question);
        if(node.getInvDef()!=null){
            node.getInvDef().getBody().apply(this, question);
        }
        return this.nodeList;
    }


    @Override
    public NodeList caseARecordInvariantType(ARecordInvariantType node, ContextSlicing question) throws AnalysisException {
        question.setRecord(true);
    //p(node.toString()+"\t\t"+node.getDefinitions().get(0));
        if(!question.getTypesDep().contains(node.getDefinitions().get(0))){
            question.addTypesDep(node.getDefinitions().get(0));
        }
        for(AFieldField ff : node.getFields()){
            ff.getType().apply(this, question);

        }
        if(node.getInvDef()!=null){
            //p("Body--->"+node.getInvDef().getBody().toString());
            node.getInvDef().getBody().apply(this, question);
           // node.getInvDef().apply(this,question);
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
            node.getTo().apply(this, question);
            node.getFrom().apply(this,question);

        return this.nodeList;
    }

    @Override
    public NodeList caseAExplicitFunctionDefinition(AExplicitFunctionDefinition node, ContextSlicing question) throws AnalysisException {
        if(!question.getTypesDep().contains(node)  /*&& !question.invIsInList(node.getType().getParameters().get(0).toString())*/){
            question.addTypesDep(node);
        }
        if(node.getMeasure()!=null){
            p(node.getMeasure().getClass().getSimpleName().toString() + "CLASS");
            node.getMeasure().apply(this,question);
        }
       // p(node.getType().toString());
        question.setFunctionName(node.getName().getName());
        node.getType().apply(this,question);
        node.getBody().apply(this, question);
        question.setFunctionName("");
        return this.nodeList;
    }

    @Override
    public NodeList caseAVariableExp(AVariableExp node, ContextSlicing question) throws AnalysisException {
        if(!question.getFunctionName().equals(node.getName().getName())) {
            node.getVardef().apply(this, question);
        }else{
            question.setFunctionName("");
        }
        return nodeList;
    }

    @Override
    public NodeList caseAImplicitFunctionDefinition(AImplicitFunctionDefinition node, ContextSlicing question) throws AnalysisException {

        return nodeList;
    }

    @Override
    public NodeList caseAValueDefinition(AValueDefinition node, ContextSlicing question) throws AnalysisException {
        p(node.getType().toString()+"\n\n"+node.getExpression().toString());
        node.getType().apply(this,question);p(node.getType().getClass().getSimpleName().toString());
        node.getExpression().apply(this,question);
        return nodeList;
    }


    @Override
    public NodeList caseAFunctionType(AFunctionType node, ContextSlicing question) throws AnalysisException {
       /// p("----->"+node.getResult().toString()+"\t\t"+node.getParameters().toString()+"\\\\\\\\\\\\\\\\"+node.getDefinitions().toString()+"´´´´´´´´´´´´´´´´");
        for(PType pt : node.getParameters()){
            pt.apply(this,question);
        }

        node.getResult().apply(this,question);
        return nodeList;
    }

    @Override
    public NodeList caseILexNameToken(ILexNameToken node, ContextSlicing question) throws AnalysisException {
       /* p("começa");
        p(node.getIdentifier().toString());

       p(node.getIdentifier().getClass().getSimpleName().toString());*/
        return nodeList;
    }


    @Override
    public NodeList caseASeqSeqType(ASeqSeqType node, ContextSlicing question) throws AnalysisException {
        node.getSeqof().apply(this, question);
        return nodeList;
    }

    @Override
    public NodeList caseASetType(ASetType node, ContextSlicing question) throws AnalysisException {
        node.getSetof().apply(this,question);
        p(node.getSetof().getClass().getSimpleName().toString());
        return nodeList;
    }



    //--------------------------------------------------------------------

    @Override
    public NodeList caseAStateDefinition(AStateDefinition node, ContextSlicing question) throws AnalysisException {
        for(AFieldField ff : node.getFields()){
            ff.getType().apply(this, question);
        }
        if(node.getInvPattern()!=null){
            node.getInvdef().apply(this,question);
        }
        if (node.getInitdef() != null) {
            node.getInitdef().apply(this,question);
        }
        return nodeList;
    }





    //-------------------------------------------------------------------



    @Override
    public NodeList caseAProductType(AProductType node, ContextSlicing question) throws AnalysisException {
        for(PType pt : node.getTypes()){
            pt.apply(this, question);
        }
        return nodeList;
    }

    @Override
    public NodeList caseILexToken(ILexToken node, ContextSlicing question) throws AnalysisException {
        return nodeList;
    }



    @Override
    public NodeList caseILexIdentifierToken(ILexIdentifierToken node, ContextSlicing question) throws AnalysisException {
        return nodeList;
    }

    @Override
    public NodeList caseILexBooleanToken(ILexBooleanToken node, ContextSlicing question) throws AnalysisException {
        return nodeList;
    }

    @Override
    public NodeList caseILexCharacterToken(ILexCharacterToken node, ContextSlicing question) throws AnalysisException {
        return nodeList;
    }

    @Override
    public NodeList caseILexIntegerToken(ILexIntegerToken node, ContextSlicing question) throws AnalysisException {
        return nodeList;
    }

    @Override
    public NodeList caseILexQuoteToken(ILexQuoteToken node, ContextSlicing question) throws AnalysisException {

        return nodeList;
    }

    @Override
    public NodeList caseILexRealToken(ILexRealToken node, ContextSlicing question) throws AnalysisException {
        return nodeList;
    }

    @Override
    public NodeList caseILexStringToken(ILexStringToken node, ContextSlicing question) throws AnalysisException {
        return nodeList;
    }

    @Override
    public NodeList caseILexLocation(ILexLocation node, ContextSlicing question) throws AnalysisException {
        return nodeList;
    }

    @Override
    public NodeList caseClonableFile(ClonableFile node, ContextSlicing question) throws AnalysisException {
        return nodeList;
    }

    @Override
    public NodeList caseClonableString(ClonableString node, ContextSlicing question) throws AnalysisException {
        return nodeList;
    }

    @Override
    public NodeList caseClassDefinitionSettings(ClassDefinitionSettings node, ContextSlicing question) throws AnalysisException {
        return nodeList;
    }

    @Override
    public NodeList caseNameScope(NameScope node, ContextSlicing question) throws AnalysisException {
        return nodeList;
    }

    @Override
    public NodeList casePass(Pass node, ContextSlicing question) throws AnalysisException {
        return nodeList;
    }

    @Override
    public NodeList caseBoolean(Boolean node, ContextSlicing question) throws AnalysisException {
        p("entra n boool1");
        return nodeList;
    }

    @Override
    public NodeList caseInteger(Integer node, ContextSlicing question) throws AnalysisException {
        p("entra n boool2");
        return nodeList;
    }

    @Override
    public NodeList caseString(String node, ContextSlicing question) throws AnalysisException {
        p("entra n boool3");
        return nodeList;
    }

    @Override
    public NodeList caseLong(Long node, ContextSlicing question) throws AnalysisException {
        p("entra n boool4");
        return nodeList;
    }

    @Override
    public NodeList caseTStatic(TStatic node, ContextSlicing question) throws AnalysisException {
        p("entra n boool5");
        return nodeList;
    }

    @Override
    public NodeList caseTAsync(TAsync node, ContextSlicing question) throws AnalysisException {
        p("entra n boool6");
        return nodeList;
    }

    @Override
    public NodeList defaultPExp(PExp node, ContextSlicing question) throws AnalysisException {
        node.getType().apply(this, question);
        return nodeList;
    }

    @Override
    public NodeList caseAApplyExp(AApplyExp node, ContextSlicing question) throws AnalysisException {
        node.getRoot().apply(this,question);p(node.getRoot().getClass().getSimpleName());
        return nodeList;
    }

    @Override
    public NodeList caseANarrowExp(ANarrowExp node, ContextSlicing question) throws AnalysisException {
        p("entra n booold");
        return nodeList;
    }

    @Override
    public NodeList defaultSUnaryExp(SUnaryExp node, ContextSlicing question) throws AnalysisException {
        //p("entra n boool7");
        node.getType().apply(this, question);
        return nodeList;
    }

    @Override
    public NodeList defaultSBinaryExp(SBinaryExp node, ContextSlicing question) throws AnalysisException {
        node.getRight().apply(this,question);
        node.getLeft().apply(this,question);
        return nodeList;
    }

    @Override
    public NodeList caseABooleanConstExp(ABooleanConstExp node, ContextSlicing question) throws AnalysisException {
        p("entra n boool9");
        return nodeList;
    }

    @Override
    public NodeList caseACasesExp(ACasesExp node, ContextSlicing question) throws AnalysisException {
        p("entra n boool0");
        return nodeList;
    }

    @Override
    public NodeList caseACharLiteralExp(ACharLiteralExp node, ContextSlicing question) throws AnalysisException {
        p("entra n boool11");
        return nodeList;
    }

    @Override
    public NodeList caseAElseIfExp(AElseIfExp node, ContextSlicing question) throws AnalysisException {
        p("if then");
        return nodeList;
    }

    @Override
    public NodeList caseAExists1Exp(AExists1Exp node, ContextSlicing question) throws AnalysisException {
        p("entra n boool33");
        return nodeList;
    }

    @Override
    public NodeList caseAExistsExp(AExistsExp node, ContextSlicing question) throws AnalysisException {
        p("entra n boool44");
        return nodeList;
    }

    @Override
    public NodeList caseAFieldExp(AFieldExp node, ContextSlicing question) throws AnalysisException {
        //p("entra n boool55");
        return nodeList;
    }

    @Override
    public NodeList caseAFieldNumberExp(AFieldNumberExp node, ContextSlicing question) throws AnalysisException {
        return super.caseAFieldNumberExp(node, question);
    }

    @Override
    public NodeList caseAForAllExp(AForAllExp node, ContextSlicing question) throws AnalysisException {
        return super.caseAForAllExp(node, question);
    }

    @Override
    public NodeList caseAFuncInstatiationExp(AFuncInstatiationExp node, ContextSlicing question) throws AnalysisException {
        return super.caseAFuncInstatiationExp(node, question);
    }

    @Override
    public NodeList caseAHistoryExp(AHistoryExp node, ContextSlicing question) throws AnalysisException {
        return super.caseAHistoryExp(node, question);
    }

    @Override
    public NodeList caseAIfExp(AIfExp node, ContextSlicing question) throws AnalysisException {
        node.getElse().apply(this,question);
        node.getTest().apply(this,question);
        node.getThen().apply(this,question);
        return nodeList;
    }

    @Override
    public NodeList caseAIntLiteralExp(AIntLiteralExp node, ContextSlicing question) throws AnalysisException {
        return super.caseAIntLiteralExp(node, question);
    }

    @Override
    public NodeList caseAIotaExp(AIotaExp node, ContextSlicing question) throws AnalysisException {
        return super.caseAIotaExp(node, question);
    }

    @Override
    public NodeList caseAIsExp(AIsExp node, ContextSlicing question) throws AnalysisException {
        return super.caseAIsExp(node, question);
    }

    @Override
    public NodeList caseAIsOfBaseClassExp(AIsOfBaseClassExp node, ContextSlicing question) throws AnalysisException {
        return super.caseAIsOfBaseClassExp(node, question);
    }

    @Override
    public NodeList caseAIsOfClassExp(AIsOfClassExp node, ContextSlicing question) throws AnalysisException {
        return super.caseAIsOfClassExp(node, question);
    }

    @Override
    public NodeList caseALambdaExp(ALambdaExp node, ContextSlicing question) throws AnalysisException {
        return super.caseALambdaExp(node, question);
    }

    @Override
    public NodeList caseALetBeStExp(ALetBeStExp node, ContextSlicing question) throws AnalysisException {

        return nodeList;
    }

    @Override
    public NodeList caseALetDefExp(ALetDefExp node, ContextSlicing question) throws AnalysisException {
        for (PDefinition p : node.getLocalDefs()){
            p.apply(this,question);
        }
        node.getExpression().apply(this,question);
        return nodeList;
    }

    @Override
    public NodeList caseADefExp(ADefExp node, ContextSlicing question) throws AnalysisException {
        return super.caseADefExp(node, question);
    }

    @Override
    public NodeList defaultSMapExp(SMapExp node, ContextSlicing question) throws AnalysisException {
        return super.defaultSMapExp(node, question);
    }

    @Override
    public NodeList caseAMapletExp(AMapletExp node, ContextSlicing question) throws AnalysisException {
        return super.caseAMapletExp(node, question);
    }

    @Override
    public NodeList caseAMkBasicExp(AMkBasicExp node, ContextSlicing question) throws AnalysisException {
        return super.caseAMkBasicExp(node, question);
    }

    @Override
    public NodeList caseAMkTypeExp(AMkTypeExp node, ContextSlicing question) throws AnalysisException {
        return super.caseAMkTypeExp(node, question);
    }

    @Override
    public NodeList caseAMuExp(AMuExp node, ContextSlicing question) throws AnalysisException {
        return super.caseAMuExp(node, question);
    }

    @Override
    public NodeList caseANewExp(ANewExp node, ContextSlicing question) throws AnalysisException {
        return super.caseANewExp(node, question);
    }

    @Override
    public NodeList caseANilExp(ANilExp node, ContextSlicing question) throws AnalysisException {
        return super.caseANilExp(node, question);
    }

    @Override
    public NodeList caseANotYetSpecifiedExp(ANotYetSpecifiedExp node, ContextSlicing question) throws AnalysisException {
        return super.caseANotYetSpecifiedExp(node, question);
    }

    @Override
    public NodeList caseAPostOpExp(APostOpExp node, ContextSlicing question) throws AnalysisException {
        return super.caseAPostOpExp(node, question);
    }

    @Override
    public NodeList caseAPreExp(APreExp node, ContextSlicing question) throws AnalysisException {
        return super.caseAPreExp(node, question);
    }

    @Override
    public NodeList caseAPreOpExp(APreOpExp node, ContextSlicing question) throws AnalysisException {
        return super.caseAPreOpExp(node, question);
    }

    @Override
    public NodeList caseAQuoteLiteralExp(AQuoteLiteralExp node, ContextSlicing question) throws AnalysisException {
        // p("quote");
        return nodeList;
    }

    @Override
    public NodeList caseARealLiteralExp(ARealLiteralExp node, ContextSlicing question) throws AnalysisException {
        return super.caseARealLiteralExp(node, question);
    }

    @Override
    public NodeList caseASameBaseClassExp(ASameBaseClassExp node, ContextSlicing question) throws AnalysisException {
        return super.caseASameBaseClassExp(node, question);
    }

    @Override
    public NodeList caseASameClassExp(ASameClassExp node, ContextSlicing question) throws AnalysisException {
        return super.caseASameClassExp(node, question);
    }

    @Override
    public NodeList caseASelfExp(ASelfExp node, ContextSlicing question) throws AnalysisException {
        return super.caseASelfExp(node, question);
    }

    @Override
    public NodeList defaultSSeqExp(SSeqExp node, ContextSlicing question) throws AnalysisException {
        return super.defaultSSeqExp(node, question);
    }

    @Override
    public NodeList defaultSSetExp(SSetExp node, ContextSlicing question) throws AnalysisException {
        return super.defaultSSetExp(node, question);
    }

    @Override
    public NodeList caseAStateInitExp(AStateInitExp node, ContextSlicing question) throws AnalysisException {
        return super.caseAStateInitExp(node, question);
    }

    @Override
    public NodeList caseAStringLiteralExp(AStringLiteralExp node, ContextSlicing question) throws AnalysisException {
        return super.caseAStringLiteralExp(node, question);
    }

    @Override
    public NodeList caseASubclassResponsibilityExp(ASubclassResponsibilityExp node, ContextSlicing question) throws AnalysisException {
        return super.caseASubclassResponsibilityExp(node, question);
    }

    @Override
    public NodeList caseASubseqExp(ASubseqExp node, ContextSlicing question) throws AnalysisException {
        return super.caseASubseqExp(node, question);
    }

    @Override
    public NodeList caseAThreadIdExp(AThreadIdExp node, ContextSlicing question) throws AnalysisException {
        return super.caseAThreadIdExp(node, question);
    }

    @Override
    public NodeList caseATimeExp(ATimeExp node, ContextSlicing question) throws AnalysisException {
        return super.caseATimeExp(node, question);
    }

    @Override
    public NodeList caseATupleExp(ATupleExp node, ContextSlicing question) throws AnalysisException {
        return super.caseATupleExp(node, question);
    }

    @Override
    public NodeList caseAUndefinedExp(AUndefinedExp node, ContextSlicing question) throws AnalysisException {
        return super.caseAUndefinedExp(node, question);
    }



    @Override
    public NodeList caseAAbsoluteUnaryExp(AAbsoluteUnaryExp node, ContextSlicing question) throws AnalysisException {
        return super.caseAAbsoluteUnaryExp(node, question);
    }

    @Override
    public NodeList caseACardinalityUnaryExp(ACardinalityUnaryExp node, ContextSlicing question) throws AnalysisException {
        return super.caseACardinalityUnaryExp(node, question);
    }

    @Override
    public NodeList caseADistConcatUnaryExp(ADistConcatUnaryExp node, ContextSlicing question) throws AnalysisException {
        return super.caseADistConcatUnaryExp(node, question);
    }

    @Override
    public NodeList caseADistIntersectUnaryExp(ADistIntersectUnaryExp node, ContextSlicing question) throws AnalysisException {
        return super.caseADistIntersectUnaryExp(node, question);
    }

    @Override
    public NodeList caseADistMergeUnaryExp(ADistMergeUnaryExp node, ContextSlicing question) throws AnalysisException {
        return super.caseADistMergeUnaryExp(node, question);
    }

    @Override
    public NodeList caseADistUnionUnaryExp(ADistUnionUnaryExp node, ContextSlicing question) throws AnalysisException {
        return super.caseADistUnionUnaryExp(node, question);
    }

    @Override
    public NodeList caseAElementsUnaryExp(AElementsUnaryExp node, ContextSlicing question) throws AnalysisException {
        return super.caseAElementsUnaryExp(node, question);
    }

    @Override
    public NodeList caseAFloorUnaryExp(AFloorUnaryExp node, ContextSlicing question) throws AnalysisException {
        return super.caseAFloorUnaryExp(node, question);
    }

    @Override
    public NodeList caseAHeadUnaryExp(AHeadUnaryExp node, ContextSlicing question) throws AnalysisException {
        return super.caseAHeadUnaryExp(node, question);
    }

    @Override
    public NodeList caseAIndicesUnaryExp(AIndicesUnaryExp node, ContextSlicing question) throws AnalysisException {
        return super.caseAIndicesUnaryExp(node, question);
    }

    @Override
    public NodeList caseALenUnaryExp(ALenUnaryExp node, ContextSlicing question) throws AnalysisException {
        p(node.toString());
        return nodeList;
    }

    @Override
    public NodeList caseAMapDomainUnaryExp(AMapDomainUnaryExp node, ContextSlicing question) throws AnalysisException {
        return super.caseAMapDomainUnaryExp(node, question);
    }

    @Override
    public NodeList caseAMapInverseUnaryExp(AMapInverseUnaryExp node, ContextSlicing question) throws AnalysisException {
        return super.caseAMapInverseUnaryExp(node, question);
    }

    @Override
    public NodeList caseAMapRangeUnaryExp(AMapRangeUnaryExp node, ContextSlicing question) throws AnalysisException {
        return super.caseAMapRangeUnaryExp(node, question);
    }

    @Override
    public NodeList caseANotUnaryExp(ANotUnaryExp node, ContextSlicing question) throws AnalysisException {
        return super.caseANotUnaryExp(node, question);
    }

    @Override
    public NodeList caseAPowerSetUnaryExp(APowerSetUnaryExp node, ContextSlicing question) throws AnalysisException {
        return super.caseAPowerSetUnaryExp(node, question);
    }

    @Override
    public NodeList caseAReverseUnaryExp(AReverseUnaryExp node, ContextSlicing question) throws AnalysisException {
        return super.caseAReverseUnaryExp(node, question);
    }

    @Override
    public NodeList caseATailUnaryExp(ATailUnaryExp node, ContextSlicing question) throws AnalysisException {
        return super.caseATailUnaryExp(node, question);
    }

    @Override
    public NodeList caseAUnaryMinusUnaryExp(AUnaryMinusUnaryExp node, ContextSlicing question) throws AnalysisException {
        return super.caseAUnaryMinusUnaryExp(node, question);
    }

    @Override
    public NodeList caseAUnaryPlusUnaryExp(AUnaryPlusUnaryExp node, ContextSlicing question) throws AnalysisException {
        return super.caseAUnaryPlusUnaryExp(node, question);
    }

    @Override
    public NodeList defaultSBooleanBinaryExp(SBooleanBinaryExp node, ContextSlicing question) throws AnalysisException {
        return super.defaultSBooleanBinaryExp(node, question);
    }

    @Override
    public NodeList caseACompBinaryExp(ACompBinaryExp node, ContextSlicing question) throws AnalysisException {
        return super.caseACompBinaryExp(node, question);
    }

    @Override
    public NodeList caseADomainResByBinaryExp(ADomainResByBinaryExp node, ContextSlicing question) throws AnalysisException {
        return super.caseADomainResByBinaryExp(node, question);
    }

    @Override
    public NodeList caseADomainResToBinaryExp(ADomainResToBinaryExp node, ContextSlicing question) throws AnalysisException {
        return super.caseADomainResToBinaryExp(node, question);
    }

    @Override
    public NodeList caseAEqualsBinaryExp(AEqualsBinaryExp node, ContextSlicing question) throws AnalysisException {
        return super.caseAEqualsBinaryExp(node, question);
    }

    @Override
    public NodeList caseAInSetBinaryExp(AInSetBinaryExp node, ContextSlicing question) throws AnalysisException {
        return super.caseAInSetBinaryExp(node, question);
    }

    @Override
    public NodeList caseAMapUnionBinaryExp(AMapUnionBinaryExp node, ContextSlicing question) throws AnalysisException {
        return super.caseAMapUnionBinaryExp(node, question);
    }

    @Override
    public NodeList caseANotEqualBinaryExp(ANotEqualBinaryExp node, ContextSlicing question) throws AnalysisException {
        return super.caseANotEqualBinaryExp(node, question);
    }

    @Override
    public NodeList caseANotInSetBinaryExp(ANotInSetBinaryExp node, ContextSlicing question) throws AnalysisException {
        return super.caseANotInSetBinaryExp(node, question);
    }

    @Override
    public NodeList defaultSNumericBinaryExp(SNumericBinaryExp node, ContextSlicing question) throws AnalysisException {
        return super.defaultSNumericBinaryExp(node, question);
    }

    @Override
    public NodeList caseAPlusPlusBinaryExp(APlusPlusBinaryExp node, ContextSlicing question) throws AnalysisException {
        return super.caseAPlusPlusBinaryExp(node, question);
    }

    @Override
    public NodeList caseAProperSubsetBinaryExp(AProperSubsetBinaryExp node, ContextSlicing question) throws AnalysisException {
        return super.caseAProperSubsetBinaryExp(node, question);
    }

    @Override
    public NodeList caseARangeResByBinaryExp(ARangeResByBinaryExp node, ContextSlicing question) throws AnalysisException {
        return super.caseARangeResByBinaryExp(node, question);
    }

    @Override
    public NodeList caseARangeResToBinaryExp(ARangeResToBinaryExp node, ContextSlicing question) throws AnalysisException {
        return super.caseARangeResToBinaryExp(node, question);
    }

    @Override
    public NodeList caseASeqConcatBinaryExp(ASeqConcatBinaryExp node, ContextSlicing question) throws AnalysisException {
        return super.caseASeqConcatBinaryExp(node, question);
    }

    @Override
    public NodeList caseASetDifferenceBinaryExp(ASetDifferenceBinaryExp node, ContextSlicing question) throws AnalysisException {
        return super.caseASetDifferenceBinaryExp(node, question);
    }

    @Override
    public NodeList caseASetIntersectBinaryExp(ASetIntersectBinaryExp node, ContextSlicing question) throws AnalysisException {
        return super.caseASetIntersectBinaryExp(node, question);
    }

    @Override
    public NodeList caseASetUnionBinaryExp(ASetUnionBinaryExp node, ContextSlicing question) throws AnalysisException {
        return super.caseASetUnionBinaryExp(node, question);
    }

    @Override
    public NodeList caseAStarStarBinaryExp(AStarStarBinaryExp node, ContextSlicing question) throws AnalysisException {
        return super.caseAStarStarBinaryExp(node, question);
    }

    @Override
    public NodeList caseASubsetBinaryExp(ASubsetBinaryExp node, ContextSlicing question) throws AnalysisException {
        return super.caseASubsetBinaryExp(node, question);
    }

    @Override
    public NodeList caseAAndBooleanBinaryExp(AAndBooleanBinaryExp node, ContextSlicing question) throws AnalysisException {
        return super.caseAAndBooleanBinaryExp(node, question);
    }

    @Override
    public NodeList caseAEquivalentBooleanBinaryExp(AEquivalentBooleanBinaryExp node, ContextSlicing question) throws AnalysisException {
        return super.caseAEquivalentBooleanBinaryExp(node, question);
    }

    @Override
    public NodeList caseAImpliesBooleanBinaryExp(AImpliesBooleanBinaryExp node, ContextSlicing question) throws AnalysisException {
        return super.caseAImpliesBooleanBinaryExp(node, question);
    }

    @Override
    public NodeList caseAOrBooleanBinaryExp(AOrBooleanBinaryExp node, ContextSlicing question) throws AnalysisException {
        return super.caseAOrBooleanBinaryExp(node, question);
    }

    @Override
    public NodeList caseADivNumericBinaryExp(ADivNumericBinaryExp node, ContextSlicing question) throws AnalysisException {
        return super.caseADivNumericBinaryExp(node, question);
    }

    @Override
    public NodeList caseADivideNumericBinaryExp(ADivideNumericBinaryExp node, ContextSlicing question) throws AnalysisException {
        return super.caseADivideNumericBinaryExp(node, question);
    }

    @Override
    public NodeList caseAGreaterEqualNumericBinaryExp(AGreaterEqualNumericBinaryExp node, ContextSlicing question) throws AnalysisException {
        return super.caseAGreaterEqualNumericBinaryExp(node, question);
    }

    @Override
    public NodeList caseAGreaterNumericBinaryExp(AGreaterNumericBinaryExp node, ContextSlicing question) throws AnalysisException {
       node.getLeft().apply(this,question);
        node.getRight().apply(this,question);
        return nodeList;
    }

    @Override
    public NodeList caseALessEqualNumericBinaryExp(ALessEqualNumericBinaryExp node, ContextSlicing question) throws AnalysisException {
        return super.caseALessEqualNumericBinaryExp(node, question);
    }

    @Override
    public NodeList caseALessNumericBinaryExp(ALessNumericBinaryExp node, ContextSlicing question) throws AnalysisException {
        return super.caseALessNumericBinaryExp(node, question);
    }

    @Override
    public NodeList caseAModNumericBinaryExp(AModNumericBinaryExp node, ContextSlicing question) throws AnalysisException {
        return super.caseAModNumericBinaryExp(node, question);
    }

    @Override
    public NodeList caseAPlusNumericBinaryExp(APlusNumericBinaryExp node, ContextSlicing question) throws AnalysisException {
        return super.caseAPlusNumericBinaryExp(node, question);
    }

    @Override
    public NodeList caseARemNumericBinaryExp(ARemNumericBinaryExp node, ContextSlicing question) throws AnalysisException {
        return super.caseARemNumericBinaryExp(node, question);
    }

    @Override
    public NodeList caseASubtractNumericBinaryExp(ASubtractNumericBinaryExp node, ContextSlicing question) throws AnalysisException {
        return super.caseASubtractNumericBinaryExp(node, question);
    }

    @Override
    public NodeList caseATimesNumericBinaryExp(ATimesNumericBinaryExp node, ContextSlicing question) throws AnalysisException {
        return super.caseATimesNumericBinaryExp(node, question);
    }

    @Override
    public NodeList caseAMapCompMapExp(AMapCompMapExp node, ContextSlicing question) throws AnalysisException {
        return super.caseAMapCompMapExp(node, question);
    }

    @Override
    public NodeList caseAMapEnumMapExp(AMapEnumMapExp node, ContextSlicing question) throws AnalysisException {
        return super.caseAMapEnumMapExp(node, question);
    }

    @Override
    public NodeList caseASeqCompSeqExp(ASeqCompSeqExp node, ContextSlicing question) throws AnalysisException {
        return super.caseASeqCompSeqExp(node, question);
    }

    @Override
    public NodeList caseASeqEnumSeqExp(ASeqEnumSeqExp node, ContextSlicing question) throws AnalysisException {
        return super.caseASeqEnumSeqExp(node, question);
    }

    @Override
    public NodeList caseASetCompSetExp(ASetCompSetExp node, ContextSlicing question) throws AnalysisException {
        return super.caseASetCompSetExp(node, question);
    }

    @Override
    public NodeList caseASetEnumSetExp(ASetEnumSetExp node, ContextSlicing question) throws AnalysisException {
        return super.caseASetEnumSetExp(node, question);
    }

    @Override
    public NodeList caseASetRangeSetExp(ASetRangeSetExp node, ContextSlicing question) throws AnalysisException {
        return super.caseASetRangeSetExp(node, question);
    }

    @Override
    public NodeList defaultPModifier(PModifier node, ContextSlicing question) throws AnalysisException {
        return super.defaultPModifier(node, question);
    }

    @Override
    public NodeList caseARecordModifier(ARecordModifier node, ContextSlicing question) throws AnalysisException {
        return super.caseARecordModifier(node, question);
    }

    @Override
    public NodeList defaultPAlternative(PAlternative node, ContextSlicing question) throws AnalysisException {
        return super.defaultPAlternative(node, question);
    }

    @Override
    public NodeList caseACaseAlternative(ACaseAlternative node, ContextSlicing question) throws AnalysisException {
        return super.caseACaseAlternative(node, question);
    }

    @Override
    public NodeList defaultPType(PType node, ContextSlicing question) throws AnalysisException {
        return super.defaultPType(node, question);
    }

    @Override
    public NodeList defaultSBasicType(SBasicType node, ContextSlicing question) throws AnalysisException {
        return super.defaultSBasicType(node, question);
    }

    @Override
    public NodeList caseABracketType(ABracketType node, ContextSlicing question) throws AnalysisException {
        return super.caseABracketType(node, question);
    }

    @Override
    public NodeList caseAClassType(AClassType node, ContextSlicing question) throws AnalysisException {
        return super.caseAClassType(node, question);
    }



    @Override
    public NodeList defaultSInvariantType(SInvariantType node, ContextSlicing question) throws AnalysisException {
        return super.defaultSInvariantType(node, question);
    }

    @Override
    public NodeList defaultSMapType(SMapType node, ContextSlicing question) throws AnalysisException {
        return super.defaultSMapType(node, question);
    }

    @Override
    public NodeList caseAOperationType(AOperationType node, ContextSlicing question) throws AnalysisException {
        return super.caseAOperationType(node, question);
    }

    @Override
    public NodeList caseAOptionalType(AOptionalType node, ContextSlicing question) throws AnalysisException {
        return super.caseAOptionalType(node, question);
    }

    @Override
    public NodeList caseAParameterType(AParameterType node, ContextSlicing question) throws AnalysisException {
        return super.caseAParameterType(node, question);
    }


    @Override
    public NodeList caseAQuoteType(AQuoteType node, ContextSlicing question) throws AnalysisException {
        // p("quote");
        return nodeList;
    }

    @Override
    public NodeList defaultSSeqType(SSeqType node, ContextSlicing question) throws AnalysisException {
        return super.defaultSSeqType(node, question);
    }

    @Override
    public NodeList caseAUndefinedType(AUndefinedType node, ContextSlicing question) throws AnalysisException {
        return super.caseAUndefinedType(node, question);
    }

    @Override
    public NodeList caseAUnknownType(AUnknownType node, ContextSlicing question) throws AnalysisException {
        return super.caseAUnknownType(node, question);
    }

    @Override
    public NodeList caseAUnresolvedType(AUnresolvedType node, ContextSlicing question) throws AnalysisException {
        return super.caseAUnresolvedType(node, question);
    }

    @Override
    public NodeList caseAVoidReturnType(AVoidReturnType node, ContextSlicing question) throws AnalysisException {
        return super.caseAVoidReturnType(node, question);
    }

    @Override
    public NodeList caseAVoidType(AVoidType node, ContextSlicing question) throws AnalysisException {
        return super.caseAVoidType(node, question);
    }

    @Override
    public NodeList caseASeq1SeqType(ASeq1SeqType node, ContextSlicing question) throws AnalysisException {
        return super.caseASeq1SeqType(node, question);
    }

    @Override
    public NodeList caseAInMapMapType(AInMapMapType node, ContextSlicing question) throws AnalysisException {
        return super.caseAInMapMapType(node, question);
    }

    @Override
    public NodeList caseACharBasicType(ACharBasicType node, ContextSlicing question) throws AnalysisException {
        return super.caseACharBasicType(node, question);
    }

    @Override
    public NodeList defaultSNumericBasicType(SNumericBasicType node, ContextSlicing question) throws AnalysisException {
        return super.defaultSNumericBasicType(node, question);
    }

    @Override
    public NodeList caseATokenBasicType(ATokenBasicType node, ContextSlicing question) throws AnalysisException {
        return super.caseATokenBasicType(node, question);
    }

    @Override
    public NodeList caseAIntNumericBasicType(AIntNumericBasicType node, ContextSlicing question) throws AnalysisException {
        return super.caseAIntNumericBasicType(node, question);
    }

    @Override
    public NodeList caseANatOneNumericBasicType(ANatOneNumericBasicType node, ContextSlicing question) throws AnalysisException {
        return super.caseANatOneNumericBasicType(node, question);
    }

    @Override
    public NodeList caseANatNumericBasicType(ANatNumericBasicType node, ContextSlicing question) throws AnalysisException {
        return super.caseANatNumericBasicType(node, question);
    }

    @Override
    public NodeList caseARationalNumericBasicType(ARationalNumericBasicType node, ContextSlicing question) throws AnalysisException {
        return super.caseARationalNumericBasicType(node, question);
    }

    @Override
    public NodeList defaultPField(PField node, ContextSlicing question) throws AnalysisException {
        return super.defaultPField(node, question);
    }

    @Override
    public NodeList caseAFieldField(AFieldField node, ContextSlicing question) throws AnalysisException {
        return super.caseAFieldField(node, question);
    }

    @Override
    public NodeList defaultPAccessSpecifier(PAccessSpecifier node, ContextSlicing question) throws AnalysisException {
        return super.defaultPAccessSpecifier(node, question);
    }

    @Override
    public NodeList caseAAccessSpecifierAccessSpecifier(AAccessSpecifierAccessSpecifier node, ContextSlicing question) throws AnalysisException {
        return super.caseAAccessSpecifierAccessSpecifier(node, question);
    }

    @Override
    public NodeList defaultPAccess(PAccess node, ContextSlicing question) throws AnalysisException {
        return super.defaultPAccess(node, question);
    }

    @Override
    public NodeList caseAPublicAccess(APublicAccess node, ContextSlicing question) throws AnalysisException {
        return super.caseAPublicAccess(node, question);
    }

    @Override
    public NodeList caseAProtectedAccess(AProtectedAccess node, ContextSlicing question) throws AnalysisException {
        return super.caseAProtectedAccess(node, question);
    }

    @Override
    public NodeList caseAPrivateAccess(APrivateAccess node, ContextSlicing question) throws AnalysisException {
        return super.caseAPrivateAccess(node, question);
    }

    @Override
    public NodeList defaultPPattern(PPattern node, ContextSlicing question) throws AnalysisException {
        return super.defaultPPattern(node, question);
    }

    @Override
    public NodeList caseABooleanPattern(ABooleanPattern node, ContextSlicing question) throws AnalysisException {
        return super.caseABooleanPattern(node, question);
    }

    @Override
    public NodeList caseACharacterPattern(ACharacterPattern node, ContextSlicing question) throws AnalysisException {
        return super.caseACharacterPattern(node, question);
    }

    @Override
    public NodeList caseAConcatenationPattern(AConcatenationPattern node, ContextSlicing question) throws AnalysisException {
        return super.caseAConcatenationPattern(node, question);
    }

    @Override
    public NodeList caseAExpressionPattern(AExpressionPattern node, ContextSlicing question) throws AnalysisException {
        return super.caseAExpressionPattern(node, question);
    }

    @Override
    public NodeList caseAIdentifierPattern(AIdentifierPattern node, ContextSlicing question) throws AnalysisException {
        return super.caseAIdentifierPattern(node, question);
    }

    @Override
    public NodeList caseAIgnorePattern(AIgnorePattern node, ContextSlicing question) throws AnalysisException {
        return super.caseAIgnorePattern(node, question);
    }

    @Override
    public NodeList caseAIntegerPattern(AIntegerPattern node, ContextSlicing question) throws AnalysisException {
        return super.caseAIntegerPattern(node, question);
    }

    @Override
    public NodeList caseANilPattern(ANilPattern node, ContextSlicing question) throws AnalysisException {
        return super.caseANilPattern(node, question);
    }

    @Override
    public NodeList caseAQuotePattern(AQuotePattern node, ContextSlicing question) throws AnalysisException {
        //p("quote");
        return nodeList;
    }

    @Override
    public NodeList caseARealPattern(ARealPattern node, ContextSlicing question) throws AnalysisException {
        return super.caseARealPattern(node, question);
    }

    @Override
    public NodeList caseARecordPattern(ARecordPattern node, ContextSlicing question) throws AnalysisException {
        return super.caseARecordPattern(node, question);
    }

    @Override
    public NodeList caseASeqPattern(ASeqPattern node, ContextSlicing question) throws AnalysisException {
        return super.caseASeqPattern(node, question);
    }

    @Override
    public NodeList caseASetPattern(ASetPattern node, ContextSlicing question) throws AnalysisException {
        return super.caseASetPattern(node, question);
    }

    @Override
    public NodeList caseAStringPattern(AStringPattern node, ContextSlicing question) throws AnalysisException {
        return super.caseAStringPattern(node, question);
    }

    @Override
    public NodeList caseATuplePattern(ATuplePattern node, ContextSlicing question) throws AnalysisException {
        return super.caseATuplePattern(node, question);
    }

    @Override
    public NodeList caseAUnionPattern(AUnionPattern node, ContextSlicing question) throws AnalysisException {
        return super.caseAUnionPattern(node, question);
    }

    @Override
    public NodeList caseAMapPattern(AMapPattern node, ContextSlicing question) throws AnalysisException {
        return super.caseAMapPattern(node, question);
    }

    @Override
    public NodeList caseAMapUnionPattern(AMapUnionPattern node, ContextSlicing question) throws AnalysisException {
        return super.caseAMapUnionPattern(node, question);
    }

    @Override
    public NodeList caseAObjectPattern(AObjectPattern node, ContextSlicing question) throws AnalysisException {
        return super.caseAObjectPattern(node, question);
    }

    @Override
    public NodeList defaultPMaplet(PMaplet node, ContextSlicing question) throws AnalysisException {
        return nodeList;
    }

    @Override
    public NodeList caseAMapletPatternMaplet(AMapletPatternMaplet node, ContextSlicing question) throws AnalysisException {
        return super.caseAMapletPatternMaplet(node, question);
    }

    @Override
    public NodeList defaultPPair(PPair node, ContextSlicing question) throws AnalysisException {
        return super.defaultPPair(node, question);
    }

    @Override
    public NodeList caseAPatternTypePair(APatternTypePair node, ContextSlicing question) throws AnalysisException {
        return super.caseAPatternTypePair(node, question);
    }

    @Override
    public NodeList caseAPatternListTypePair(APatternListTypePair node, ContextSlicing question) throws AnalysisException {
        return super.caseAPatternListTypePair(node, question);
    }

    @Override
    public NodeList caseANamePatternPair(ANamePatternPair node, ContextSlicing question) throws AnalysisException {
        return super.caseANamePatternPair(node, question);
    }

    @Override
    public NodeList defaultPBind(PBind node, ContextSlicing question) throws AnalysisException {
        return super.defaultPBind(node, question);
    }

    @Override
    public NodeList caseASetBind(ASetBind node, ContextSlicing question) throws AnalysisException {
        return super.caseASetBind(node, question);
    }

    @Override
    public NodeList caseATypeBind(ATypeBind node, ContextSlicing question) throws AnalysisException {
        return super.caseATypeBind(node, question);
    }

    @Override
    public NodeList defaultPMultipleBind(PMultipleBind node, ContextSlicing question) throws AnalysisException {
        return super.defaultPMultipleBind(node, question);
    }

    @Override
    public NodeList caseASetMultipleBind(ASetMultipleBind node, ContextSlicing question) throws AnalysisException {
        return super.caseASetMultipleBind(node, question);
    }

    @Override
    public NodeList caseATypeMultipleBind(ATypeMultipleBind node, ContextSlicing question) throws AnalysisException {
        return super.caseATypeMultipleBind(node, question);
    }

    @Override
    public NodeList defaultPPatternBind(PPatternBind node, ContextSlicing question) throws AnalysisException {
        return super.defaultPPatternBind(node, question);
    }

    @Override
    public NodeList caseADefPatternBind(ADefPatternBind node, ContextSlicing question) throws AnalysisException {
        return super.caseADefPatternBind(node, question);
    }

    @Override
    public NodeList defaultPDefinition(PDefinition node, ContextSlicing question) throws AnalysisException {
        return super.defaultPDefinition(node, question);
    }

    @Override
    public NodeList caseAAssignmentDefinition(AAssignmentDefinition node, ContextSlicing question) throws AnalysisException {
        return super.caseAAssignmentDefinition(node, question);
    }

    @Override
    public NodeList caseAInstanceVariableDefinition(AInstanceVariableDefinition node, ContextSlicing question) throws AnalysisException {
        return super.caseAInstanceVariableDefinition(node, question);
    }

    @Override
    public NodeList defaultSClassDefinition(SClassDefinition node, ContextSlicing question) throws AnalysisException {
        return super.defaultSClassDefinition(node, question);
    }

    @Override
    public NodeList caseAClassInvariantDefinition(AClassInvariantDefinition node, ContextSlicing question) throws AnalysisException {
        return super.caseAClassInvariantDefinition(node, question);
    }

    @Override
    public NodeList caseAEqualsDefinition(AEqualsDefinition node, ContextSlicing question) throws AnalysisException {
        return super.caseAEqualsDefinition(node, question);
    }

    @Override
    public NodeList defaultSFunctionDefinition(SFunctionDefinition node, ContextSlicing question) throws AnalysisException {
        p("----------SFunctionDefinition----------");
        return nodeList;
    }

    @Override
    public NodeList caseAExternalDefinition(AExternalDefinition node, ContextSlicing question) throws AnalysisException {
        return super.caseAExternalDefinition(node, question);
    }

    @Override
    public NodeList defaultSOperationDefinition(SOperationDefinition node, ContextSlicing question) throws AnalysisException {
        return super.defaultSOperationDefinition(node, question);
    }

    @Override
    public NodeList caseAImportedDefinition(AImportedDefinition node, ContextSlicing question) throws AnalysisException {
        return super.caseAImportedDefinition(node, question);
    }

    @Override
    public NodeList caseAInheritedDefinition(AInheritedDefinition node, ContextSlicing question) throws AnalysisException {
        return super.caseAInheritedDefinition(node, question);
    }

    @Override
    public NodeList caseALocalDefinition(ALocalDefinition node, ContextSlicing question) throws AnalysisException {
        return super.caseALocalDefinition(node, question);
    }

    @Override
    public NodeList caseAMultiBindListDefinition(AMultiBindListDefinition node, ContextSlicing question) throws AnalysisException {
        return super.caseAMultiBindListDefinition(node, question);
    }

    @Override
    public NodeList caseAMutexSyncDefinition(AMutexSyncDefinition node, ContextSlicing question) throws AnalysisException {
        return super.caseAMutexSyncDefinition(node, question);
    }

    @Override
    public NodeList caseANamedTraceDefinition(ANamedTraceDefinition node, ContextSlicing question) throws AnalysisException {
        return super.caseANamedTraceDefinition(node, question);
    }

    @Override
    public NodeList caseAPerSyncDefinition(APerSyncDefinition node, ContextSlicing question) throws AnalysisException {
        return super.caseAPerSyncDefinition(node, question);
    }

    @Override
    public NodeList caseARenamedDefinition(ARenamedDefinition node, ContextSlicing question) throws AnalysisException {
        return super.caseARenamedDefinition(node, question);
    }

    @Override
    public NodeList caseAThreadDefinition(AThreadDefinition node, ContextSlicing question) throws AnalysisException {
        return super.caseAThreadDefinition(node, question);
    }

    @Override
    public NodeList caseAUntypedDefinition(AUntypedDefinition node, ContextSlicing question) throws AnalysisException {
        return super.caseAUntypedDefinition(node, question);
    }



    @Override
    public NodeList caseAExplicitOperationDefinition(AExplicitOperationDefinition node, ContextSlicing question) throws AnalysisException {
        return super.caseAExplicitOperationDefinition(node, question);
    }

    @Override
    public NodeList caseAImplicitOperationDefinition(AImplicitOperationDefinition node, ContextSlicing question) throws AnalysisException {
        return super.caseAImplicitOperationDefinition(node, question);
    }

    @Override
    public NodeList defaultPTerm(PTerm node, ContextSlicing question) throws AnalysisException {
        return super.defaultPTerm(node, question);
    }

    @Override
    public NodeList caseATraceDefinitionTerm(ATraceDefinitionTerm node, ContextSlicing question) throws AnalysisException {
        return super.caseATraceDefinitionTerm(node, question);
    }

    @Override
    public NodeList defaultPTraceDefinition(PTraceDefinition node, ContextSlicing question) throws AnalysisException {
        return super.defaultPTraceDefinition(node, question);
    }

    @Override
    public NodeList caseAInstanceTraceDefinition(AInstanceTraceDefinition node, ContextSlicing question) throws AnalysisException {
        return super.caseAInstanceTraceDefinition(node, question);
    }

    @Override
    public NodeList caseALetBeStBindingTraceDefinition(ALetBeStBindingTraceDefinition node, ContextSlicing question) throws AnalysisException {
        p("...................");
        return nodeList;
    }

    @Override
    public NodeList caseALetDefBindingTraceDefinition(ALetDefBindingTraceDefinition node, ContextSlicing question) throws AnalysisException {
        p("...................");
        return nodeList;

    }

    @Override
    public NodeList caseARepeatTraceDefinition(ARepeatTraceDefinition node, ContextSlicing question) throws AnalysisException {
        return super.caseARepeatTraceDefinition(node, question);
    }

    @Override
    public NodeList defaultPTraceCoreDefinition(PTraceCoreDefinition node, ContextSlicing question) throws AnalysisException {
        return super.defaultPTraceCoreDefinition(node, question);
    }

    @Override
    public NodeList caseAApplyExpressionTraceCoreDefinition(AApplyExpressionTraceCoreDefinition node, ContextSlicing question) throws AnalysisException {
        return super.caseAApplyExpressionTraceCoreDefinition(node, question);
    }

    @Override
    public NodeList caseABracketedExpressionTraceCoreDefinition(ABracketedExpressionTraceCoreDefinition node, ContextSlicing question) throws AnalysisException {
        return super.caseABracketedExpressionTraceCoreDefinition(node, question);
    }

    @Override
    public NodeList caseAConcurrentExpressionTraceCoreDefinition(AConcurrentExpressionTraceCoreDefinition node, ContextSlicing question) throws AnalysisException {
        return super.caseAConcurrentExpressionTraceCoreDefinition(node, question);
    }

    @Override
    public NodeList caseABusClassDefinition(ABusClassDefinition node, ContextSlicing question) throws AnalysisException {
        return super.caseABusClassDefinition(node, question);
    }

    @Override
    public NodeList caseACpuClassDefinition(ACpuClassDefinition node, ContextSlicing question) throws AnalysisException {
        return super.caseACpuClassDefinition(node, question);
    }

    @Override
    public NodeList caseASystemClassDefinition(ASystemClassDefinition node, ContextSlicing question) throws AnalysisException {
        return super.caseASystemClassDefinition(node, question);
    }

    @Override
    public NodeList caseAClassClassDefinition(AClassClassDefinition node, ContextSlicing question) throws AnalysisException {
        return super.caseAClassClassDefinition(node, question);
    }

    @Override
    public NodeList defaultPModules(PModules node, ContextSlicing question) throws AnalysisException {
        return super.defaultPModules(node, question);
    }

    @Override
    public NodeList defaultPImports(PImports node, ContextSlicing question) throws AnalysisException {
        return super.defaultPImports(node, question);
    }

    @Override
    public NodeList caseAModuleImports(AModuleImports node, ContextSlicing question) throws AnalysisException {
        return super.caseAModuleImports(node, question);
    }

    @Override
    public NodeList caseAFromModuleImports(AFromModuleImports node, ContextSlicing question) throws AnalysisException {
        return super.caseAFromModuleImports(node, question);
    }

    @Override
    public NodeList defaultPImport(PImport node, ContextSlicing question) throws AnalysisException {
        return super.defaultPImport(node, question);
    }

    @Override
    public NodeList caseAAllImport(AAllImport node, ContextSlicing question) throws AnalysisException {
        return super.caseAAllImport(node, question);
    }

    @Override
    public NodeList caseATypeImport(ATypeImport node, ContextSlicing question) throws AnalysisException {
        return super.caseATypeImport(node, question);
    }

    @Override
    public NodeList defaultSValueImport(SValueImport node, ContextSlicing question) throws AnalysisException {
        return super.defaultSValueImport(node, question);
    }

    @Override
    public NodeList caseAValueValueImport(AValueValueImport node, ContextSlicing question) throws AnalysisException {
        return super.caseAValueValueImport(node, question);
    }

    @Override
    public NodeList caseAFunctionValueImport(AFunctionValueImport node, ContextSlicing question) throws AnalysisException {
        return super.caseAFunctionValueImport(node, question);
    }

    @Override
    public NodeList caseAOperationValueImport(AOperationValueImport node, ContextSlicing question) throws AnalysisException {
        return super.caseAOperationValueImport(node, question);
    }

    @Override
    public NodeList defaultPExports(PExports node, ContextSlicing question) throws AnalysisException {
        return super.defaultPExports(node, question);
    }

    @Override
    public NodeList caseAModuleExports(AModuleExports node, ContextSlicing question) throws AnalysisException {
        return super.caseAModuleExports(node, question);
    }

    @Override
    public NodeList defaultPExport(PExport node, ContextSlicing question) throws AnalysisException {
        return super.defaultPExport(node, question);
    }

    @Override
    public NodeList caseAAllExport(AAllExport node, ContextSlicing question) throws AnalysisException {
        return super.caseAAllExport(node, question);
    }

    @Override
    public NodeList caseAFunctionExport(AFunctionExport node, ContextSlicing question) throws AnalysisException {
        return super.caseAFunctionExport(node, question);
    }

    @Override
    public NodeList caseAOperationExport(AOperationExport node, ContextSlicing question) throws AnalysisException {
        return super.caseAOperationExport(node, question);
    }

    @Override
    public NodeList caseATypeExport(ATypeExport node, ContextSlicing question) throws AnalysisException {
        return super.caseATypeExport(node, question);
    }

    @Override
    public NodeList caseAValueExport(AValueExport node, ContextSlicing question) throws AnalysisException {
        return super.caseAValueExport(node, question);
    }

    @Override
    public NodeList defaultPStm(PStm node, ContextSlicing question) throws AnalysisException {
        return super.defaultPStm(node, question);
    }

    @Override
    public NodeList caseAAlwaysStm(AAlwaysStm node, ContextSlicing question) throws AnalysisException {
        return super.caseAAlwaysStm(node, question);
    }

    @Override
    public NodeList caseAAssignmentStm(AAssignmentStm node, ContextSlicing question) throws AnalysisException {
        return super.caseAAssignmentStm(node, question);
    }

    @Override
    public NodeList caseAAtomicStm(AAtomicStm node, ContextSlicing question) throws AnalysisException {
        return super.caseAAtomicStm(node, question);
    }

    @Override
    public NodeList caseACallObjectStm(ACallObjectStm node, ContextSlicing question) throws AnalysisException {
        return super.caseACallObjectStm(node, question);
    }

    @Override
    public NodeList caseACallStm(ACallStm node, ContextSlicing question) throws AnalysisException {
        return super.caseACallStm(node, question);
    }

    @Override
    public NodeList caseACasesStm(ACasesStm node, ContextSlicing question) throws AnalysisException {
        return super.caseACasesStm(node, question);
    }

    @Override
    public NodeList caseAClassInvariantStm(AClassInvariantStm node, ContextSlicing question) throws AnalysisException {
        return super.caseAClassInvariantStm(node, question);
    }

    @Override
    public NodeList caseACyclesStm(ACyclesStm node, ContextSlicing question) throws AnalysisException {
        return super.caseACyclesStm(node, question);
    }

    @Override
    public NodeList caseADurationStm(ADurationStm node, ContextSlicing question) throws AnalysisException {
        return super.caseADurationStm(node, question);
    }

    @Override
    public NodeList caseAElseIfStm(AElseIfStm node, ContextSlicing question) throws AnalysisException {

        p("caseAElseIf");
        return nodeList;
    }

    @Override
    public NodeList caseAErrorStm(AErrorStm node, ContextSlicing question) throws AnalysisException {
        return super.caseAErrorStm(node, question);
    }

    @Override
    public NodeList caseAExitStm(AExitStm node, ContextSlicing question) throws AnalysisException {
        return super.caseAExitStm(node, question);
    }

    @Override
    public NodeList caseAForAllStm(AForAllStm node, ContextSlicing question) throws AnalysisException {
        return super.caseAForAllStm(node, question);
    }

    @Override
    public NodeList caseAForIndexStm(AForIndexStm node, ContextSlicing question) throws AnalysisException {
        return super.caseAForIndexStm(node, question);
    }

    @Override
    public NodeList caseAForPatternBindStm(AForPatternBindStm node, ContextSlicing question) throws AnalysisException {
        return super.caseAForPatternBindStm(node, question);
    }

    @Override
    public NodeList caseAIfStm(AIfStm node, ContextSlicing question) throws AnalysisException {
        return super.caseAIfStm(node, question);
    }

    @Override
    public NodeList caseALetBeStStm(ALetBeStStm node, ContextSlicing question) throws AnalysisException {
        return super.caseALetBeStStm(node, question);
    }

    @Override
    public NodeList caseALetStm(ALetStm node, ContextSlicing question) throws AnalysisException {
        return super.caseALetStm(node, question);
    }

    @Override
    public NodeList caseANotYetSpecifiedStm(ANotYetSpecifiedStm node, ContextSlicing question) throws AnalysisException {
        return super.caseANotYetSpecifiedStm(node, question);
    }

    @Override
    public NodeList caseAReturnStm(AReturnStm node, ContextSlicing question) throws AnalysisException {
        return super.caseAReturnStm(node, question);
    }

    @Override
    public NodeList defaultSSimpleBlockStm(SSimpleBlockStm node, ContextSlicing question) throws AnalysisException {
        return super.defaultSSimpleBlockStm(node, question);
    }

    @Override
    public NodeList caseASkipStm(ASkipStm node, ContextSlicing question) throws AnalysisException {
        return super.caseASkipStm(node, question);
    }

    @Override
    public NodeList caseASpecificationStm(ASpecificationStm node, ContextSlicing question) throws AnalysisException {
        return super.caseASpecificationStm(node, question);
    }

    @Override
    public NodeList caseAStartStm(AStartStm node, ContextSlicing question) throws AnalysisException {
        return super.caseAStartStm(node, question);
    }

    @Override
    public NodeList caseAStopStm(AStopStm node, ContextSlicing question) throws AnalysisException {
        return super.caseAStopStm(node, question);
    }

    @Override
    public NodeList caseASubclassResponsibilityStm(ASubclassResponsibilityStm node, ContextSlicing question) throws AnalysisException {
        return super.caseASubclassResponsibilityStm(node, question);
    }

    @Override
    public NodeList caseATixeStm(ATixeStm node, ContextSlicing question) throws AnalysisException {
        return super.caseATixeStm(node, question);
    }

    @Override
    public NodeList caseATrapStm(ATrapStm node, ContextSlicing question) throws AnalysisException {
        return super.caseATrapStm(node, question);
    }

    @Override
    public NodeList caseAWhileStm(AWhileStm node, ContextSlicing question) throws AnalysisException {
        return super.caseAWhileStm(node, question);
    }

    @Override
    public NodeList caseAPeriodicStm(APeriodicStm node, ContextSlicing question) throws AnalysisException {
        return super.caseAPeriodicStm(node, question);
    }

    @Override
    public NodeList caseASporadicStm(ASporadicStm node, ContextSlicing question) throws AnalysisException {
        return super.caseASporadicStm(node, question);
    }

    @Override
    public NodeList caseABlockSimpleBlockStm(ABlockSimpleBlockStm node, ContextSlicing question) throws AnalysisException {
        return super.caseABlockSimpleBlockStm(node, question);
    }

    @Override
    public NodeList caseANonDeterministicSimpleBlockStm(ANonDeterministicSimpleBlockStm node, ContextSlicing question) throws AnalysisException {
        return super.caseANonDeterministicSimpleBlockStm(node, question);
    }

    @Override
    public NodeList defaultPStateDesignator(PStateDesignator node, ContextSlicing question) throws AnalysisException {
        return super.defaultPStateDesignator(node, question);
    }

    @Override
    public NodeList caseAFieldStateDesignator(AFieldStateDesignator node, ContextSlicing question) throws AnalysisException {
        return super.caseAFieldStateDesignator(node, question);
    }

    @Override
    public NodeList caseAIdentifierStateDesignator(AIdentifierStateDesignator node, ContextSlicing question) throws AnalysisException {
        return super.caseAIdentifierStateDesignator(node, question);
    }

    @Override
    public NodeList caseAMapSeqStateDesignator(AMapSeqStateDesignator node, ContextSlicing question) throws AnalysisException {
        return super.caseAMapSeqStateDesignator(node, question);
    }

    @Override
    public NodeList defaultPObjectDesignator(PObjectDesignator node, ContextSlicing question) throws AnalysisException {
        return super.defaultPObjectDesignator(node, question);
    }

    @Override
    public NodeList caseAApplyObjectDesignator(AApplyObjectDesignator node, ContextSlicing question) throws AnalysisException {
        return super.caseAApplyObjectDesignator(node, question);
    }

    @Override
    public NodeList caseAFieldObjectDesignator(AFieldObjectDesignator node, ContextSlicing question) throws AnalysisException {
        return super.caseAFieldObjectDesignator(node, question);
    }

    @Override
    public NodeList caseAIdentifierObjectDesignator(AIdentifierObjectDesignator node, ContextSlicing question) throws AnalysisException {
        return super.caseAIdentifierObjectDesignator(node, question);
    }

    @Override
    public NodeList caseANewObjectDesignator(ANewObjectDesignator node, ContextSlicing question) throws AnalysisException {
        return super.caseANewObjectDesignator(node, question);
    }

    @Override
    public NodeList caseASelfObjectDesignator(ASelfObjectDesignator node, ContextSlicing question) throws AnalysisException {
        return super.caseASelfObjectDesignator(node, question);
    }


    public void p(String string){
        System.out.println(string);
    }
}