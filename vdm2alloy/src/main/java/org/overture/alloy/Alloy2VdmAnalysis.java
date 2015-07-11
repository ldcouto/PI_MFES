/*
 * Copyright (C) 2012 Kenneth Lausdahl
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 *
 */
package org.overture.alloy;

import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;


import org.overture.alloy.ast.*;
import org.overture.alloy.ast.Sig.FieldType;
import org.overture.alloy.ast.Sig.FieldType.Prefix;
import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.analysis.DepthFirstAnalysisAdaptorQuestionAnswer;
import org.overture.ast.definitions.AExplicitFunctionDefinition;
import org.overture.ast.definitions.AImplicitFunctionDefinition;
import org.overture.ast.definitions.AImplicitOperationDefinition;
import org.overture.ast.definitions.AStateDefinition;
import org.overture.ast.definitions.ATypeDefinition;
import org.overture.ast.definitions.AValueDefinition;
import org.overture.ast.definitions.PDefinition;
import org.overture.ast.expressions.*;
import org.overture.ast.intf.lex.ILexNameToken;
import org.overture.ast.lex.VDMToken;
import org.overture.ast.modules.AModuleModules;
import org.overture.ast.node.INode;
import org.overture.ast.node.NodeList;
import org.overture.ast.patterns.*;
import org.overture.ast.statements.AExternalClause;
import org.overture.ast.types.*;

public class Alloy2VdmAnalysis
        extends
        DepthFirstAnalysisAdaptorQuestionAnswer<Context, Alloy2VdmAnalysis.AlloyPart>
{
    NotAllowedTypes notAllowedTypes=new NotAllowedTypes(new HashMap<String, ArrayList<Integer>>(){{
        put("bool",null);
        put("real",null);
        put("map",null);
    }},1);

    public HashMap<String,String> parametersListFunction ;

    public boolean isPtype=false;
    public boolean isPtype() {
        return isPtype;
    }

    public HashMap<String,String> setTypes = new HashMap<String,String>();

    public boolean checkIsValid(List<PType> lType){//return 1 if "type" has any Product Type

        for(int i=0;i<lType.size();i++) {
            if (setTypes.containsKey(lType.get(i).toString())) {
                if (setTypes.get(lType.get(i).toString()).contains("*")) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean checkIsValidSing(String s){
        if(setTypes.containsKey(s)){
            if (setTypes.get(s).contains("*")) {
                return true;
            }
        }
        return false;
    }


    private boolean isPo=false;
    private boolean invariantMK=false;

    public void setPo(boolean isPo) {
        this.isPo = isPo;
    }

    private static final long serialVersionUID = 1L;
    final public List<Part> components = new Vector<Part>();
    final public List<Part> componentsPO = new Vector<Part>();
    boolean nat= false;
    AuxiliarMethods aux=new AuxiliarMethods();
    Comment cm;

    public void p(String s){
        System.out.println(s);
    }

    public HashMap getNotAllowedTypes(){

        return notAllowedTypes.getTypes();
    }

    public class AlloyPart
    {
        public String exp = "";
        public Queue<AlloyExp> predicates = new LinkedList<AlloyExp>();
        public Queue<AlloyExp> topLevel = new LinkedList<AlloyExp>();
        public Queue<AlloyTypeBind> typeBindings = new LinkedList<AlloyTypeBind>();

        public AlloyPart(String exp)
        {
            this.exp = exp;
        }
        public String getPredicatesSp(){
            String sp="";
            for (AlloyExp predicates : this.predicates)
            {
                sp += " " + predicates;
            }
            p("predicates: "+predicates.toString());
            return sp;
        }
        public AlloyPart()
        {
            this.exp = "";
        }

        public void merge(AlloyPart p)
        {
            mergeReturns(this, p);
        }

        @Override
        public String toString()
        {
            return exp;
        }

        public void appendPredicates()
        {
            for (AlloyExp predicates : this.predicates)
            {
                this.exp += " " + predicates;
            }
            this.predicates.clear();
        }

        public String toPartBody()
        {
            String tmp = exp;
            for (AlloyExp expression : topLevel)
            {
                tmp += "\n\t" + expression;
            }
            return tmp;
        }
    }

    // public List<String> result = new Vector<String>();
    Set<INode> trnaslated = new HashSet<INode>();


    // String expression = "";
    private String moduleName;

    public Alloy2VdmAnalysis(String name,boolean isPo)
    {
        this.moduleName = name;
        if(isPo){setPo(true);}

    }

    @Override
    public AlloyPart caseAModuleModules(AModuleModules node, Context question)
            throws AnalysisException
    {


        // result.add("module " + moduleName + "\n");
        // result.add("open util/relation\n");
        this.components.add(new ModuleHeader(moduleName, "util/relation", "util/boolean","vdmutil"));

        BasicTokenSearch basicTokens = new BasicTokenSearch();
        node.apply(basicTokens);
//        p(node.getDefs().getFirst().getClass().getSimpleName());
        for (Entry<String, INode> entry : basicTokens.mkbasicToken.entrySet())
        {
            Sig s = new Sig(entry.getKey());
            s.isOne = true;
            this.components.add(s);
        }

        return super.caseAModuleModules(node, question);
    }



    @Override
    public AlloyPart caseATypeDefinition(ATypeDefinition node, Context ctxt)
            throws AnalysisException
    {	if (trnaslated.contains(node))
    {
        return null;
    }


        trnaslated.add(node);

        ctxt.merge(createType(node.getType(), ctxt));

        return null;
    }

    public List<Part> getComponentsPO() {
        return componentsPO;
    }

    public String splitProductType (String type){
        String st="";
        st = type.replace("*", "");
        st = st.replace(" ", "");
        st = st.replace("(", "");
        st = st.replace(")", "");
        return st;
    }

    private void createTypeInvariant(ATypeDefinition def, Sig sig, Context ctxt,PType type) // add param to know sig type.... type of sig  = type
            throws AnalysisException
    {       String nType ;
            String exp = "";
        if(type instanceof ASetType){
            ASetType seq = (ASetType)type;
            setTypes.put(sig.name,seq.getSetof().toString());
            if(def.getInvdef() != null){
                AlloyPart pattern = def.getInvPattern().apply(this, ctxt);
                if(pattern.exp.equals("x")){exp+="xx";}
                String body="";
                if(seq.getSetof() instanceof  AProductType) {
                     body = sig.name + " = { " + "x : setOf" + splitProductType(seq.getSetof().toString()) + " | let " + pattern.exp + " = x.contents" + splitProductType(seq.getSetof().toString()) + " | ";
                }else{
                     body = sig.name + " = { " + "x : setOf" + seq.getSetof() + " | let " + pattern.exp + " = x.contents" + seq.getSetof() + " | ";
                }
                Context invCtxt = new Context(ctxt);
                invCtxt.addVariable(pattern.exp, def.getType());
                body += def.getInvExpression().apply(this, invCtxt).exp + " }";
                Fact f = new Fact(sig.name + "Inv", body);
                this.components.add(f);
            }else{
                String body="";
                if(seq.getSetof() instanceof  AProductType) {
                    body = sig.name + " = { " + "x : setOf" + splitProductType(seq.getSetof().toString()) + " }";
                }else {
                    body = sig.name + " = { " + "x : setOf" + seq.getSetof() + " }";
                }
                    Fact f = new Fact(sig.name + "Inv", body);

                    this.components.add(f);

            }

        }else {

            if (type instanceof AUnionType) {
                nType = type.toString();
                nType = nType.replace("(", "");
                nType = nType.replace(")", "");
                List<String> items = Arrays.asList(nType.split("\\|"));

                List <String> lst =  new Vector<String>();
                for(int i=0;i<items.size();i++){
                    if (items.get(i).replaceAll("\\s+","").equals("bool")){lst.add("Bool");}
                    else{lst.add((String) items.get(i));}
                }p(lst.toString());
                nType = toList(lst, "+");

            } else {
                nType = type.toString();
            }
            nType = removeSignals(nType);
            //p(nType);
            if (def.getInvdef() != null) {
                if(nType.equals("bool")){nType="Bool";}
                AlloyPart pattern = def.getInvPattern().apply(this, ctxt);
                String body = sig.name + " = { " + pattern.exp + " : " + nType + " | ";
                Context invCtxt = new Context(ctxt);
                invCtxt.addVariable(pattern.exp, def.getType());p("CLASS: " + def.getInvExpression().getClass().getSimpleName());
                body += def.getInvExpression().apply(this, invCtxt).exp + " }";
                Fact f = new Fact(sig.name + "Inv", body);
                this.components.add(f);
            }
        }
    }

    public String removeSignals(String s){
        String st= "";
        s=s.replace("<", "");
        st = s.replace(">", "");
        return st;

    }

    private void createInvariantTypes(Sig sig,String type) // method to create fact in types
            throws AnalysisException
    {

        if(type==null){
            List <String> lst =  new Vector<String>();
            for(int i=0;i<sig.getQuotes().size();i++){
                if(sig.getQuotes().get(i).equals("bool")){lst.add("Bool");}
                else{lst.add((String) sig.getQuotes().get(i));}
            }
            Fact f = new Fact(sig.name + "Type", toList(lst, "+"), sig.name);
            this.components.add(f);
        }
        else{
            if(type.equals("bool")){type="Bool";}
            Fact f = new Fact(sig.name + "Type", type, sig.name);
            this.components.add(f);
        }

    }

    private  void createInvariantRecordType(String var,String type,String body){

        Fact f = new Fact(true,var,type,body);
        this.components.add(f);
    }


    public boolean isNat() {
        return nat;
    }

    private Context createType(PType type, Context outer)
            throws AnalysisException {


        Context ctxt = new Context();
        if (outer.getSig(getTypeName(type)) != null)
        {
            return ctxt;
        }

        if (type instanceof SInvariantType)
        {
            SInvariantType invType = (SInvariantType) type;
            if (invType instanceof ANamedInvariantType)
            {
                ctxt.merge(createNamedType((ANamedInvariantType) invType, outer));;
                return ctxt;
            } else if (invType instanceof ARecordInvariantType)
            {
                //comments
                cm=new Comment(invType.toString());
                this.components.add(cm);

                String simpleName = type.getClass().getSimpleName();
                if (this.notAllowedTypes.getTypes().containsKey(simpleName)) {//if type isn't allowed
                    notAllowedTypes.addType(simpleName, type.getLocation().getStartLine());
                    //notAllowedTypes.types.put(simpleName,namedType.getLocation().getStartLine());
                }

                ARecordInvariantType recordType = (ARecordInvariantType) type;
                Sig s = new Sig(recordType.getName().getName());

                for (AFieldField f : recordType.getFields())
                {
                    //p(f.getType().toString());
                    ctxt.merge(createType(f.getType(), outer));
                    s.addField(f.getTag(), getFieldType(f.getType()));
                    s.constraints.addAll(getFieldConstraints(f, s.name));

                }

                Context invCtxt = new Context(ctxt);
                if (recordType.getInvDef() != null)
                {
                    //add this to fix 'ex'gt[ex.quali,0];
                    //AlloyPart invPart;
                    // if(recordType.getInvDef().getParamPatternList().get(0).get(0).getClass().getSimpleName().equals("AIdentifierPattern")) {
                    if(recordType.getInvDef().getParamPatternList().get(0).get(0).toString().startsWith("mk_")){//2 types inv's in record types
                        this.invariantMK=true;
                        AlloyPart  invPart = recordType.getInvDef().getParamPatternList().get(0).get(0).apply(this, invCtxt);
                        //}else{

                        //}
                        //  p(recordType.getInvDef().getParamPatternList().get(0).get(0).getClass().getSimpleName());
                        boolean hasLet = !invPart.exp.isEmpty();
                        invPart.merge(recordType.getInvDef().getBody().apply(this, invCtxt));
                        //p(recordType.getInvDef().getParamPatternList().get(0).get(0).getClass().getSimpleName());
                        if (hasLet)
                        {
                            invPart.exp = "( " + invPart.exp + ")";
                        }
                        s.constraints.add(invPart.exp);
                        this.components.add(s);

                    }else{
                        s.setRecordType(true);
                        AlloyPart  invPart = recordType.getInvDef().getParamPatternList().get(0).get(0).apply(this, invCtxt);
                        String var=invPart.toString();
                        boolean hasLet = !invPart.exp.isEmpty();
                        String body = recordType.getInvDef().getBody().apply(this, invCtxt).toString();
                        invPart.merge(recordType.getInvDef().getBody().apply(this, invCtxt));
                        s.constraints.add(invPart.exp);
                        this.components.add(s);
                        createInvariantRecordType(var,recordType.getName().getName(),body);
                    }
                }else{
                    //s.constraints.add(invPart.exp);
                    this.components.add(s);
                    //createInvariantRecordType(var,recordType.getName().getName(),body);
                }
                ctxt.addType(recordType, s);
                return ctxt;

            }
        } else if (type instanceof AQuoteType)
        {
            AQuoteType qt = (AQuoteType) type;

            String name = qt.getValue().getValue().toUpperCase();
            Sig s = new Sig(name);
            s.isOne = true;
            ctxt.addType(qt, s);
            this.components.add(s);
            return ctxt;
        }
        else if (type instanceof SBasicType)
        {
            if(type instanceof ARealNumericBasicType){

                String simpleName = type.getClass().getSimpleName();
                if (this.notAllowedTypes.getTypes().containsKey(simpleName)) {//if type isn't allowed
                    notAllowedTypes.addType(simpleName, type.getLocation().getStartLine());
                    //notAllowedTypes.types.put(simpleName,namedType.getLocation().getStartLine());
                }

            }
            else if (type instanceof ABooleanBasicType)
            {
                String simpleName = type.getClass().getSimpleName();
                if (this.notAllowedTypes.getTypes().containsKey(simpleName)) {//if type isn't allowed
                    notAllowedTypes.addType(simpleName, type.getLocation().getStartLine());
                    //notAllowedTypes.types.put(simpleName,namedType.getLocation().getStartLine());
                }

            } else if (type instanceof ATokenBasicType || type instanceof ACharBasicType)
            {
                Sig s = new Sig(getTypeName(type));
                ctxt.addType(type, s);
                this.components.add(s);
            } //else if (type instanceof SNumericBasicType)
            //{

            //}
            else if (type instanceof ANatNumericBasicType)
            {
                if(!nat) {
                    aux.createNats(getTypeName(type),type,ctxt,this.components);
                    nat=true;
                }
            }
            return ctxt;
        } else if (type instanceof SSeqType)
        {
              //SSeqType stype = (SSeqType) type;
            // result.add("sig "+getTypeName(type))
            String simpleName = ((SSeqType) type).getSeqof().getClass().getSimpleName();
            if (this.notAllowedTypes.getTypes().containsKey(simpleName)) {//if type isn't allowed
                notAllowedTypes.addType(simpleName, type.getLocation().getStartLine());
                //notAllowedTypes.types.put(simpleName,namedType.getLocation().getStartLine());
            }

            return ctxt;
        }else if(type instanceof ASetType){
            //ASetType stype = (ASetType) type;
            //p(type.toString());

            String simpleName = ((ASetType) type).getSetof().getClass().getSimpleName();
            if (this.notAllowedTypes.getTypes().containsKey(simpleName)) {//if type isn't allowed
                notAllowedTypes.addType(simpleName, type.getLocation().getStartLine());
                //notAllowedTypes.types.put(simpleName,namedType.getLocation().getStartLine());
            }
            return ctxt;
        }
        else if (type instanceof AProductType)
        {
            Sig s = new Sig(getTypeName(type));
            Sig.FieldType ftype = null;
            for (Iterator<PType> itr = ((AProductType) type).getTypes().descendingIterator(); itr.hasNext();)
            {
                String fname = getTypeName(itr.next());
                if (ftype == null)
                {
                    ftype = new Sig.FieldType(fname, Prefix.undefined);
                } else
                {
                    ftype = new Sig.MapFieldType(fname, Prefix.undefined, ftype);
                }
            }
            s.addField("x", ftype);
            s.isWrapper = true;
            ctxt.addType(type, s);
            this.components.add(s);
            return ctxt;
        }

        // switch (type.kindPType())
        // {
        // case INVARIANT:
        // {
        // SInvariantType invType = (SInvariantType) type;
        // switch (invType.kindSInvariantType())
        // {
        // case NAMED:
        // {
        // ctxt.merge(createNamedType((ANamedInvariantType) invType, outer));
        // return ctxt;
        // }
        // case RECORD:
        // {
        // ARecordInvariantType recordType = (ARecordInvariantType) type;
        // Sig s = new Sig(recordType.getName().name);
        //
        // for (AFieldField f : recordType.getFields())
        // {
        // ctxt.merge(createType(f.getType(), outer));
        // s.addField(f.getTag(), getFieldType(f.getType()));
        // s.constraints.addAll(getFieldConstraints(f, s.name));
        // }
        // Context invCtxt = new Context(ctxt);
        // if (recordType.getInvDef() != null)
        // {
        // AlloyPart invPart = recordType.getInvDef().getParamPatternList().get(0).get(0).apply(this, invCtxt);
        // boolean hasLet = !invPart.exp.isEmpty();
        // invPart.merge(recordType.getInvDef().getBody().apply(this, invCtxt));
        // if (hasLet)
        // {
        // invPart.exp = "( " + invPart.exp + ")";
        // }
        // s.constraints.add(invPart.exp);
        // }
        // ctxt.addType(recordType, s);
        // this.components.add(s);
        // return ctxt;
        // }
        //
        // }
        // }
        // case QUOTE:
        // {
        // AQuoteType qt = (AQuoteType) type;
        // String name = qt.getValue().value.toUpperCase();
        // Sig s = new Sig(name);
        // s.isOne = true;
        // ctxt.addType(qt, s);
        // this.components.add(s);
        // return ctxt;
        // }
        // case BASIC:
        // {
        // switch (((SBasicType) type).kindSBasicType())
        // {
        // case BOOLEAN:
        // break;
        // case TOKEN:
        // case CHAR:
        // {
        // Sig s = new Sig(getTypeName(type));
        // ctxt.addType(type, s);
        // this.components.add(s);
        // }
        // case NUMERIC:
        // break;
        //
        // }
        // return ctxt;
        // }
        //
        // case SEQ:
        // {
        // // SSeqType stype = (SSeqType) type;
        // // result.add("sig "+getTypeName(type))
        // return ctxt;
        // }
        //
        // case PRODUCT:
        // {
        // Sig s = new Sig(getTypeName(type));
        // Sig.FieldType ftype = null;
        // for (Iterator<PType> itr = ((AProductType) type).getTypes().descendingIterator(); itr.hasNext();)
        // {
        // String fname = getTypeName(itr.next());
        // if (ftype == null)
        // {
        // ftype = new Sig.FieldType(fname, Prefix.undefined);
        // } else
        // {
        // ftype = new Sig.MapFieldType(fname, Prefix.undefined, ftype);
        // }
        // }
        // s.addField("x", ftype);
        // s.isWrapper = true;
        // ctxt.addType(type, s);
        // this.components.add(s);
        // return ctxt;
        // }
        // }
        return ctxt;
    }

    private Context createNamedType(ANamedInvariantType namedType, Context ctxt)
            throws AnalysisException
    {

        if(namedType.getType() instanceof AMapMapType){
            String simpleName =namedType.getType().getClass().getSimpleName();
            if(this.notAllowedTypes.getTypes().containsKey(simpleName)){//if type isn't allowed
                notAllowedTypes.addType(simpleName,namedType.getLocation().getStartLine());
                //notAllowedTypes.types.put(simpleName,namedType.getLocation().getStartLine());
            }
        }

        //comments
        cm=new Comment(namedType.toString());
        this.components.add(cm);

        if (namedType.getType() instanceof SBasicType)
        {
            if(namedType.getType() instanceof ANatNumericBasicType || namedType.getType() instanceof ABooleanBasicType) {
                ATypeDefinition d = (ATypeDefinition) namedType.parent();
                if (d.getInvPattern()!=null){
                    Sig s = new Sig(namedType.getName().getName(), true);
                    ctxt.merge(createType(namedType.getType(), ctxt));
                    //s.supers.add(ctxt.getSig(namedType.getType()));

                    ctxt.addType(namedType, s);
                }
                else{
                    Sig s = new Sig(namedType.getName().getName(), true);
                    ctxt.merge(createType(namedType.getType(), ctxt));
                    //s.supers.add(ctxt.getSig(namedType.getType()));
                    ctxt.addType(namedType, s);
                    this.components.add(s);
                    createInvariantTypes(s, namedType.getType().toString());
                }
            }
            else {
                Sig s = new Sig(namedType.getName().getName(), true);
                ctxt.merge(createType(namedType.getType(), ctxt));
                //s.supers.add(ctxt.getSig(namedType.getType()));
                ctxt.addType(namedType, s);
                this.components.add(s);
                createInvariantTypes(s, namedType.getType().toString());
            }



            // break;
        }

        // case BASIC:
        // {
        // SBasicType bt = (SBasicType) t.getType();
        // switch (bt.kindSBasicType())
        // {
        // case TOKEN:
        // // result.add("sig " + t.getName().name + "{}");
        // Sig s = new Sig(node.getName().name);
        // ctxt.addType(bt, s);
        // this.components.add(s);
        // break;
        //
        // }
        // }
        // break;

        // case QUOTE:
        //
        // break;

        if(namedType.getType() instanceof  AProductType){

            AProductType aProductType = (AProductType) namedType.getType();
            String name =aProductType.getTypes().get(0).toString()+aProductType.getTypes().get(1).toString()+"Product";
            Sig s = new Sig(name);



            FieldType a  =  new FieldType(aProductType.getTypes().get(0).toString(),Prefix.one);
            FieldType aa  =  new FieldType(aProductType.getTypes().get(1).toString(),Prefix.one);

            ctxt.merge(createType(aProductType.getTypes().get(0), ctxt));
            ctxt.merge(createType(aProductType.getTypes().get(1), ctxt));
            s.addField("fst", a);
            s.addField("snd", aa);
            Fact pt =  new Fact(aProductType.getTypes().get(0).toString()+aProductType.getTypes().get(1).toString()+"ProductF",
                    "all x1,x2 : "+name+" | (x1.fst = x2.fst and x1.snd = x2.snd) implies x1=x2");
            this.components.add(s);
            this.components.add(pt);
            ctxt.addType(aProductType,s);

        }


        if (namedType.getType() instanceof AQuoteType) // new method to single quote types
        {
            ATypeDefinition d = (ATypeDefinition) namedType.parent();
            List<String> quotes = new Vector<String>();
            AQuoteType qt = (AQuoteType) namedType.getType();
            quotes.add(qt.getValue().getValue().toUpperCase());
            if(d.getInvPattern()==null) {
                createType(qt, ctxt);
                Sig s = new Sig(namedType.getName().getName(), true); // create sig in univ{}
                ctxt.addType(qt, s);
                s.setInTypes(quotes);
                this.components.add(s);
                createInvariantTypes(s, null);//fact
            }else{
                createType(qt, ctxt);
                Sig s = new Sig(namedType.getName().getName(), true); // create sig in univ{}
                ctxt.addType(qt, s);
                quotes.add(qt.getValue().getValue().toUpperCase());
                s.setInTypes(quotes);
            }

        }

        // case UNION:
        if (namedType.getType() instanceof AUnionType)
        {
            AUnionType ut = (AUnionType) namedType.getType();
            List<String> quotes = new Vector<String>();
            List<String> qts = new Vector<String>();
            for (PType ute : ut.getTypes())
            {//p(ute.toString());
                //check allowed types
                String simpleName =ute.getClass().getSimpleName();
                if(this.notAllowedTypes.getTypes().containsKey(simpleName)){//if type isn't allowed
                    notAllowedTypes.addType(simpleName,namedType.getLocation().getStartLine());
                    //notAllowedTypes.types.put(simpleName,namedType.getLocation().getStartLine());
                }


                if(!nat && ute.toString().equals("nat")){aux.createNats(ute.toString(),ute,ctxt,this.components);nat=true;}
                if (ute instanceof AQuoteType)
                {
                    AQuoteType qt = (AQuoteType) ute;
                    String name = qt.getValue().getValue().toUpperCase();
                    quotes.add(name);
                    createType(ute, ctxt);
                } else if (ute instanceof ANamedInvariantType)
                {
                    ANamedInvariantType nit = (ANamedInvariantType) ute;
                    quotes.add(nit.getName().getName());

                }else{quotes.add(ute.toString());}
            }

             //Sig s = new Sig(namedType.getName().getName(),true);
             //s.setInTypes(quotes);
            //ctxt.addType(ut, s);
            //this.components.add(s);




            if(namedType.parent() instanceof ATypeDefinition){
                ATypeDefinition def = (ATypeDefinition) namedType.parent();
                if(def.getInvPattern()==null){
                    Sig sUniv = new Sig(namedType.getName().getName(),true); // create sig in univ{}
                    sUniv.setInTypes(quotes);
                    this.components.add(sUniv);
                    ctxt.addType(ut, sUniv);
                    createInvariantTypes(sUniv,null);//fact
                }else{
                    //p(quotes.toString());
                    Sig s = new Sig(namedType.getName().getName(),true);
                    s.setInTypes(quotes);
                    ctxt.addType(ut, s);

                }
            }


            //p("ptype: "+ptype.toString()+"VARS: "+s.getFieldNames().toString());

        }
        // break;

        // case SEQ:
        if (namedType.getType() instanceof SSeqType)
        {
            SSeqType stype = (SSeqType) namedType.getType();
            ctxt.merge(createType(stype.getSeqof(), ctxt));
            Sig s = new Sig(namedType.getName().getName());
            s.addField("x", getFieldType(stype));
            s.isWrapper = true;
            ctxt.addType(stype, s);
            this.components.add(s);
            this.components.add(new Fact(namedType.getName().getName() + "Set", "all c1,c2 : "
                    + namedType.getName().getName()
                    + " | c1.x = c2.x implies c1=c2"));
            // break;
        }

        // case SET:
        int hasPtype = 0;
        String name2 = "";//!= ""  : x = set of (a * a)
        if (namedType.getType() instanceof ASetType)
        {
                Sig s = null;
                isPtype=true;
                ASetType stype = (ASetType) namedType.getType();
                String sSetString="";
                if (stype.getSetof() instanceof AProductType)
                {
                    AProductType aProductType = (AProductType) stype.getSetof();
                    FieldType a  =  new FieldType(aProductType.getTypes().get(0).toString(),Prefix.one);
                    FieldType aa  =  new FieldType(aProductType.getTypes().get(1).toString(),Prefix.one);
                    String name =aProductType.getTypes().get(0).toString()+aProductType.getTypes().get(1).toString()+"Product";

                    s =  new Sig(name);
                    ctxt.merge(createType(aProductType.getTypes().get(0), ctxt));
                    ctxt.merge(createType(aProductType.getTypes().get(1), ctxt));
                    s.addField("fst", a);
                    s.addField("snd", aa);
                    Fact pt =  new Fact(aProductType.getTypes().get(0).toString()+aProductType.getTypes().get(1).toString()+"ProductF",
                            "all x1,x2 : "+name+" | (x1.fst = x2.fst and x1.snd = x2.snd) implies x1=x2");
                    this.components.add(s);
                    this.components.add(pt);


                    ctxt.addType(aProductType,s);
                    name2 = aProductType.getTypes().get(0).toString()+aProductType.getTypes().get(1).toString();
                    sSetString = "setOf"+ name2;
                    Sig sSet = new Sig(sSetString);
                    FieldType fSet = new FieldType(name,Prefix.set);
                    sSet.addField("contents"+name2,fSet);
                    ctxt.addType(stype,sSet);
                    hasPtype=1;
                    this.components.add(sSet);
                    this.components.add(new Fact(name2+"Set","all c1,c2 : "+sSetString+" | c1.contents"+name2+" = c2.contents"+name2+" implies c1 = c2"));

                }else{
                    sSetString = "setOf"+stype.getSetof() ;
                    Sig sSet = new Sig(sSetString);
                    FieldType fSet = new FieldType(stype.getSetof().toString(),Prefix.set);
                    sSet.addField("contents"+stype.getSetof(),fSet);
                    ctxt.addType(stype,sSet);
                    this.components.add(sSet);

                    this.components.add(new Fact(stype.getSetof()+"Set","all c1,c2 : "+sSetString+" | c1.contents"+stype.getSetof()+" = c2.contents"+stype.getSetof()+" implies c1 = c2"));
                    ctxt.merge(createType(stype.getSetof(), ctxt));
                }

               /* Sig sSet = new Sig(sSetString);
                FieldType fSet = new FieldType(stype.getSetof().toString(),Prefix.set);
                sSet.addField("contents"+stype.getSetof(),fSet);
                ctxt.addType(stype,sSet);
                this.components.add(sSet);

                this.components.add(new Fact(stype.getSetof()+"Set","all c1,c2 : "+sSetString+" | c1.contents"+stype.getSetof()+" = c2.contents"+stype.getSetof()+" implies c1 = c2"));
            */

                Sig s1 = new Sig(namedType.getName().getName(),true); // Type in univ{}
               if(hasPtype==1){s1.supers.add(s);
                   s1.isWrapper = s.isWrapper;
                   /* p(s.toString());
                   p(s1.toString());
                   p(s1.getFieldNames().toString());*/
               }
                //p("cena nova: "+s1.getFieldNames().toString()+"\t"+s1.toString());
            //
           /* if (stype.getSetof() instanceof AProductType)
            {
                Sig superSig = ctxt.getSig(stype.getSetof());
                s.supers.add(superSig);
                s.isWrapper = superSig.isWrapper;
            } else
            {*/
                //s.addField("x", getFieldType(stype));
               // s.isWrapper = true;

                //this.components.add(new Fact(namedType.getName().getName()
                  //      + "Set", "all c1,c2 : " + namedType.getName().getName()
                    //    + " | c1.x = c2.x implies c1=c2"));

            //}
            ctxt.addType(stype, s1);
           // p(ctxt.toString());
            //this.components.add(s);
            // createTypeInvariant(node, s, ctxt);
            // break;
        }
        // }

        if (namedType.parent() instanceof ATypeDefinition)
        {
            ATypeDefinition def = (ATypeDefinition) namedType.parent();
                if (ctxt.getSig(namedType) != null) {
                    Sig sUniv = new Sig(namedType.getName().getName(), true); // create sig in univ{}
                    //sUniv.getFieldNames() =ctxt.getSig(namedType).getFieldNames();
                    if(hasPtype==1){sUniv=ctxt.getSig(namedType);}
                    //sUniv=ctxt.getSig(namedType);
                    if (def.getInvPattern() == null && ctxt.getSig(namedType.getName().getName()) == null) { //A = A'
                            ctxt.addType(def.getType(), sUniv);
                            this.components.add(sUniv);
                        if(namedType.getType() instanceof AProductType){
                            AProductType a = (AProductType)namedType.getType();
                            createInvariantTypes(sUniv,a.getTypes().get(0).toString()+a.getTypes().get(1)+"Product");
                        }else {
                            createInvariantTypes(sUniv, namedType.getType().toString());
                        }
                    } else {
                        if (def.getInvPattern() != null) {// A = A'  \n  inv x = E <> ...
                            if (ctxt.getSig(namedType).toString() != null && namedType.getType().toString().equals("nat")) {
                                this.components.add(ctxt.getSig(namedType));
                                createTypeInvariant(def, ctxt.getSig(namedType), ctxt, namedType.getType());
                            } else {

                                ctxt.addType(def.getType(), sUniv);
                                this.components.add(sUniv);
                                createTypeInvariant(def, sUniv, ctxt, namedType.getType());
                            }
                        }else{
                            if(namedType.getType() instanceof ASetType){
                              /*  if(name2.equals("")) {
                                    ctxt.addType(def.getType(), sUniv);
                                    this.components.add(sUniv);
                                    createTypeInvariant(def, sUniv, ctxt, namedType.getType());
                                }else{*/
                                    ctxt.addType(def.getType(), sUniv);
                                this.components.add(sUniv);
                                    createTypeInvariant(def, sUniv, ctxt, namedType.getType());
                                //}
                            }
                        }
                    }

                }
            }


        return ctxt;
    }

    String getTypeName(PType type)
    {
        // switch (type.kindPType())
        // {
        // case SEQ:
        if (type instanceof SSeqType)
        {
            SSeqType stype = (SSeqType) type;
            return "seq " + getTypeName(stype.getSeqof());
        }

        // case SET:
        if (type instanceof ASetType)
        {
            ASetType stype = (ASetType) type;
            return "set " + getTypeName(stype.getSetof());
        }

        // case INVARIANT:
        if (type instanceof SInvariantType)
        {
            SInvariantType itype = (SInvariantType) type;
            // switch (itype.kindSInvariantType())
            // {
            // case NAMED:
            if (itype instanceof ANamedInvariantType)
                return ((ANamedInvariantType) itype).getName().getName();

            // case RECORD:
            if (itype instanceof ARecordInvariantType)
                return ((ARecordInvariantType) itype).getName().getName();
            // }
        }

        // case BASIC:
        if (type instanceof SBasicType)
        {
            return ((SBasicType) type)+"";
        }

        // case PRODUCT:
        if (type instanceof AProductType)
        {
            AProductType pType = (AProductType) type;
            String name = "";
            for (PType t : pType.getTypes())
            {
                name += getTypeName(t);
            }
            return name;
        }

        // case MAP:
        if (type instanceof SMapType)
        {
            FieldType s = getFieldType(type);
            return s.toString();
        }
        // SMapType mType = (SMapType) type;
        // return getTypeName(mType.getFrom())+getTypeName(mType.getTo());

        // }
        return "unknownTypeName";
    }

    private FieldType getFieldType(PType t)
    {
        // switch (t.kindPType())
        // {
        // case MAP:
        if (t instanceof SMapType)
        {
            SMapType ftype = (SMapType) t;

            return new Sig.MapFieldType(ftype.getFrom().toString(), (ftype instanceof SMapType ? FieldType.Prefix.undefined
                    : FieldType.Prefix.lone), new Sig.FieldType(ftype.getTo().toString(), FieldType.Prefix.lone));
        }
        // case SET:
        if (t instanceof ASetType)
        {
            ASetType stype = (ASetType) t;
            return new Sig.FieldType(getTypeName(stype.getSetof()), Sig.FieldType.Prefix.set);
        }

        // case SEQ:
        if (t instanceof SSeqType)
        {
            SSeqType stype = (SSeqType) t;
            return new Sig.FieldType(getTypeName(stype.getSeqof()), Sig.FieldType.Prefix.seq);
        }

        // case INVARIANT:
        if (t instanceof SInvariantType)
        {
            SInvariantType invType = (SInvariantType) t;
            // switch (invType.kindSInvariantType())
            // {
            // case NAMED:
            if (invType instanceof ANamedInvariantType)
                return new Sig.FieldType(((ANamedInvariantType) invType).getName().getName());
            // case RECORD:
            if (invType instanceof ARecordInvariantType)
                return new Sig.FieldType(((ARecordInvariantType) invType).getName().getName());

            // }
            // }
        }
        if(t instanceof ANatNumericBasicType){
            ANatNumericBasicType nt = (ANatNumericBasicType) t;
            return new Sig.FieldType(((ANatNumericBasicType) nt).toString());
        }
        if(t instanceof ABooleanBasicType){
            ABooleanBasicType nt  = (ABooleanBasicType)t;
            return new Sig.FieldType(((ABooleanBasicType) nt).toString());
        }

        return null;
    }

    public List<String> getFieldConstraints(AFieldField field, String sig)
    {
        final List<String> constraints = new Vector<String>();
        if (field.getType() instanceof SMapType)
        {
            SMapType ftype = (SMapType) field.getType();
            // switch (ftype.kindSMapType())
            // {
            // case INMAP:
            String simpleName =ftype.getTo().getClass().getSimpleName();
            if(this.notAllowedTypes.getTypes().containsKey(simpleName)){//if type isn't allowed
                notAllowedTypes.addType(simpleName,ftype.getLocation().getStartLine());
                //notAllowedTypes.types.put(simpleName,namedType.getLocation().getStartLine());
            }
            simpleName =ftype.getFrom().getClass().getSimpleName();
            if(this.notAllowedTypes.getTypes().containsKey(simpleName)){//if type isn't allowed
                notAllowedTypes.addType(simpleName,ftype.getLocation().getStartLine());
                //notAllowedTypes.types.put(simpleName,namedType.getLocation().getStartLine());
            }
            if (ftype instanceof AInMapMapType) {

                constraints.add(" /*" + sig + "." + field.getTag()
                        + " is an INMAP */ " + "injective[" + field.getTag()
                        + "," + sig + "] and functional[" + field.getTag()
                        + "," + sig + "]");
            }
            // break;
            // case MAP:
            if (ftype instanceof AMapMapType) {

                constraints.add(" /*" + sig + "." + field.getTag()
                        + " is a MAP   */ " + "functional[" + field.getTag()
                        + "," + sig + "]");
            }
            // break;

            // }
        }
        return constraints;
    }

    @Override
    public AlloyPart caseAValueDefinition(AValueDefinition node, Context ctxt)
            throws AnalysisException
    {
        // switch (node.getType().kindPType())
        // {
        //p(node.toString());
        if (node.getType() instanceof SBasicType
                || node.getType() instanceof SInvariantType)
        {
            // case BASIC:
            // case INVARIANT:
            // {
            String name = node.getPattern().toString();// todo
            Sig s = new Sig(name,true);p("name "+name+"\ncena "+node.getType().toString());
            Fact f = new Fact(name+"Type",node.getType().toString(),name); //public Fact(String name ,String body, String atrb)

            ctxt.merge(createType(node.getType(), ctxt));
            // System.out.println("Type is: "+ node.getType()+" Found sig: "+ctxt.getSig(node.getType()).name);
            s.supers.add(ctxt.getSig(node.getType()));
            s.isOne = true;
            ctxt.addVariable(name, node.getType());
            this.components.add(s);
            this.components.add(f);
            // break;
            // return;
        } else
        // default:
        {
            System.out.println("Skipping value: \""
                    + node.getPattern().toString()
                    + "\" it should be generated as a function");
        }

        // }

        return new AlloyPart();
    }

    @Override
    public AlloyPart caseAStateDefinition(AStateDefinition node,
                                          Context question) throws AnalysisException
    {
        if (trnaslated.contains(node))
        {
            return null;
        }
        trnaslated.add(node);
        String name = node.getName().getName();
        Sig s = new Sig(name);

        for (Iterator<AFieldField> itr = node.getFields().iterator(); itr.hasNext();)
        {
            AFieldField f = itr.next();

            if (f.getType() instanceof SMapType)
            {
                SMapType ftype = (SMapType) f.getType();
                s.addField(f.getTag(), getFieldType(ftype)); /*
															 * new Sig.MapFieldType(ftype.getFrom().toString(),
															 * (ftype.kindSMapType() == EMapType.MAP ?
															 * FieldType.Prefix.undefined : FieldType.Prefix.lone), new
															 * Sig.FieldType(ftype.getTo().toString(),
															 * FieldType.Prefix.lone)));
															 */
                // switch (ftype.kindSMapType())
                // {
                // case INMAP:
                // s.constraints.add("/*" + name + "." + f.getTag()
                // + " is an INMAP */ " + "injective["
                // + f.getTag() + "," + s.name
                // + "] and functional[" + f.getTag() + ","
                // + s.name + "]");
                // break;
                // case MAP:
                // s.constraints.add("/*" + name + "." + f.getTag()
                // + " is a MAP   */ " + "functional["
                // + f.getTag() + "," + s.name + "]");
                // break;
                //
                // }
                s.constraints.addAll(getFieldConstraints(f, s.name));
            }
        }
        question.clearState();
        for (AFieldField f : node.getFields())
        {
            question.addState(f.getTag(), f.getTag());
        }
        if (node.getInvExpression() != null)
        {
            s.constraints.add(node.getInvExpression().apply(this, question).toPartBody());
        }
        this.components.add(s);
        return null;
    }

    public static String toList(List<String> quotes, String seperator)
    {
        StringBuilder sb = new StringBuilder();
        for (Iterator<String> itr = quotes.iterator(); itr.hasNext();)
        {
            sb.append(itr.next());
            if (itr.hasNext())
            {
                sb.append(" " + seperator + " ");
            }
        }
        return sb.toString();
    }

    @Override
    public AlloyPart caseAImplicitOperationDefinition(
            AImplicitOperationDefinition node, Context question)
            throws AnalysisException
    {
        String stateId = node.getState().getName().getName().substring(0, 1).toLowerCase();
        String stateSigName = node.getState().getName().getName();
        String preStateId = stateId;
        String postStateId = preStateId + "'";
        String arguments = preStateId + " : " + stateSigName + ", "
                + postStateId + " : " + stateSigName;

        List<String> stateFields = new Vector<String>();
        Context ctxt = new Context(question);

        for (Iterator<AFieldField> itr = node.getState().getFields().iterator(); itr.hasNext();)
        {
            AFieldField f = itr.next();
            stateFields.add(f.getTag());

        }

        boolean isfirst = true;
        for (Iterator<APatternListTypePair> itr = node.getParameterPatterns().iterator(); itr.hasNext();)
        {
            if (isfirst)
            {
                arguments += ", ";
                isfirst = false;
            }

            APatternListTypePair pl = itr.next();

            arguments += pl.getPatterns().get(0) + ": " + pl.getType();
            ctxt.addVariable(pl.getPatterns().get(0).toString(), pl.getType());
            if (itr.hasNext())
            {
                arguments += ", ";
            }
        }

        // frame conditions
        List<String> readOnlyState = new Vector<String>();
        readOnlyState.addAll(stateFields);
        for (AExternalClause framecondition : node.getExternals())
        {
            if (framecondition.getMode().getType() == VDMToken.WRITE)
            {
                for (ILexNameToken id : framecondition.getIdentifiers())
                {
                    readOnlyState.remove(id.getName());
                }

            }
        }

        StringBuilder sb = new StringBuilder();

        if (!readOnlyState.isEmpty())
        {
            sb.append("\n\t /* Frame conditions */");
            for (String id : readOnlyState)
            {
                sb.append("\n\t" + postStateId + "." + id + " = " + preStateId
                        + "." + id);
            }
        }

        // expression = "";
        question.clearState();
        for (String f : stateFields)
        {
            question.addState(f, preStateId + "." + f);
        }

        if (node.getPrecondition() != null)
        {
            sb.append("\n\t /* Pre conditions */");
            sb.append("\n\t"
                    + node.getPrecondition().apply(this, ctxt).toPartBody());
        }
        // expression = "";
        question.clearState();
        for (String f : stateFields)
        {
            question.addState(f, postStateId + "." + f);
            question.addState(f + "~", preStateId + "." + f);
        }

        sb.append("\n\t /* Post conditions */");
        sb.append("\n\t"
                + node.getPostcondition().apply(this, ctxt).toPartBody());

        this.components.add(new Pred(node.getName().getName(), arguments, sb.toString()));
        this.components.add(new Run(node.getName().getName()));
        return null;
    }

    @Override
    public AlloyPart caseAImplicitFunctionDefinition(
            AImplicitFunctionDefinition node, Context question)
            throws AnalysisException
    {
        Context ctxt = new Context(question);
        String arguments = "";
        int i = 0;
        for (Iterator<APatternListTypePair> itr = node.getParamPatterns().iterator(); itr.hasNext();)
        {
            APatternListTypePair p = itr.next();
            arguments += p.getPatterns().get(0) + ": "
                    + getTypeName(p.getType());
            ctxt.addVariable(p.getPatterns().get(0).toString(), node.getType().getParameters().get(i));
            i++;
        }

        if (!(node.getType().getResult() instanceof ABooleanBasicType))
        {
            String returnName = node.getResult().getPattern().toString();
            PType returnType = node.getResult().getType();
            ctxt.addVariable(returnName, returnType);
            if (arguments.isEmpty())
            {
                arguments = returnName + " : " + returnType;
            } else
            {
                arguments += ", " + returnName + " : " + returnType;
            }
        }

        question.clearState();
        StringBuilder sb = new StringBuilder();

        if (node.getPrecondition() != null)
        {
            sb.append("\n\t /* Pre conditions */");
            sb.append("\n\t"
                    + node.getPrecondition().apply(this, ctxt).toPartBody());
        }
        question.clearState();

        sb.append("\n\t /* Post conditions */");
        sb.append("\n\t"
                + node.getPostcondition().apply(this, ctxt).toPartBody());

        this.components.add(new Pred(node.getName().getName(), arguments, sb.toString()));
        this.components.add(new Run(node.getName().getName()));
        return null;
    }

    @Override
    public AlloyPart caseAExplicitFunctionDefinition(
            AExplicitFunctionDefinition node, Context question)
            throws AnalysisException
    {   String ltSet = "";


        if (node.getIsTypeInvariant())
        {
            return null;
        }
        Context ctxt = new Context(question);
        String arguments = "";


        List<String> lets = new Vector<String>();

        List<PType> lType = node.getType().getParameters(); //check if there is any argument with Product type
        if(checkIsValid(lType)){isPtype=true;}
        else{isPtype=false;}

        parametersListFunction = new HashMap<String,String>();
        for (int i = 0; i < node.getType().getParameters().size(); i++)
        {



            PPattern p = node.getParamPatternList().get(0).get(i);
            parametersListFunction.put(p.toString(),node.getType().getParameters().get(i).toString());
            String argumentName = null;
            if (p instanceof ARecordPattern)
            {
                argumentName = getNewName();
                StringBuffer letArgumentWrapper = new StringBuffer();
                // letArgumentWrapper.append("\n\t( let ");
                ARecordPattern rp = (ARecordPattern) p;
                for (int j = 0; j < rp.getPlist().size(); j++)
                {
                    ARecordInvariantType t = (ARecordInvariantType) node.getType().getParameters().get(i);
                    letArgumentWrapper.append(rp.getPlist().get(j) + " = "
                            + argumentName + "."
                            + t.getFields().get(j).getTag());
                    ctxt.addVariable(rp.getPlist().get(j).toString(), t.getFields().get(j).getType());

                    if (j < rp.getPlist().size() - 1)
                    {
                        letArgumentWrapper.append(", ");

                    }
                }
                //p(lets.toString());
                // letArgumentWrapper.append(" | \n\t");
                lets.add(letArgumentWrapper.toString());
            } else
            {
                argumentName = p.toString();
            }
            String pt = getTypeName(node.getType().getParameters().get(i));
            //p(setTypes.toString());
            if(setTypes.containsKey(pt)) {

                arguments += argumentName + ": " + pt;
                ltSet+="let "+ argumentName+" = "+argumentName+".contents"+splitProductType(setTypes.get(pt))+" | ";
            }else{
                arguments += argumentName + ": " + pt;
            }
            ctxt.addVariable(argumentName, node.getType().getParameters().get(i));
            if (i < node.getType().getParameters().size() - 1)
            {
                arguments += ", ";
            }
        }

        if (node.getType().getResult() instanceof ARecordInvariantType)// node.apply(new CheckMkAnalysis()))
        {
            ctxt = new PredicatContext(ctxt, getNewName(), node.getType().getResult());
        }

        question.clearState();
        StringBuilder sb = new StringBuilder();

        if (node.getBody() != null)
        {
            if(node.getBody() instanceof AEqualsBinaryExp){
                sb.append("\n\t" + node.getBody().apply(this, ctxt).toPartBody());
            }else {
                sb.append("\n\t" + ltSet + "/* Body */");
                sb.append("\n\t" + node.getBody().apply(this, ctxt).toPartBody());
            }
        }

        if (node.getPrecondition() != null)
        {
            question.clearState();
            sb.append(" and ");
            sb.append("\n\t /* Pre conditions */");
            sb.append("\n\t"
                    + node.getPrecondition().apply(this, ctxt).toPartBody());
            // sb.append(")");
        }
        if (node.getPostcondition() != null)
        {
            question.clearState();

            sb.append("\n\t /* Post conditions */");
            sb.append("\n\t"
                    + node.getPostcondition().apply(this, ctxt).toPartBody());
        }

        String body = sb.toString();
        sb = new StringBuilder();
        for (String let : lets)
        {
            sb.append("( let " + let + " | ");
        }
        sb.append(body);
        for (int j = 0; j < lets.size(); j++)
        {
            sb.append(")");
        }

        if (node.getType().getResult() instanceof ABooleanBasicType)
        {
            this.components.add(new Pred(node.getName().getName(), arguments, sb.toString()));

        } else if (node.getType().getResult() instanceof ARecordInvariantType)
        {
            PredicatContext pCtxt = (PredicatContext) ctxt;
            arguments += ", " + pCtxt.getReturnName() + ": "
                    + pCtxt.getSig(pCtxt.getReturnType()).name;
            this.components.add(new Pred(node.getName().getName(), arguments, sb.toString()));
        } else
        {
            this.components.add(new Fun(node.getName().getName(), arguments, sb.toString(), getTypeName(node.getType().getResult())));
        }
        this.components.add(new Run(node.getName().getName()));
        return null;
    }

    int nextNameId = 0;

    private String getNewName()
    {
        return "var" + nextNameId++;
    }

    public AlloyPart caseATupleExp(org.overture.ast.expressions.ATupleExp node,
                                   Context question) throws AnalysisException
    {
        if (node.getAncestor(AInSetBinaryExp.class) != null)
        {
            AlloyPart p = new AlloyPart();

            Sig s = question.getSig(node.getType());//p("entraaa "+s.getFieldNames().toString()+"\n");
            if (!s.getFieldNames().isEmpty()
            && s.getField(s.getFieldNames().get(0)).size() == node.getArgs().size())
            {
                List<String> fields = new Vector<String>();
                for (int i = 0; i < node.getArgs().size(); i++)
                {
                    PExp a = node.getArgs().get(i);//p(a.toString());
                    AlloyPart ap = a.apply(this, question);
                    if (question.containsVariable(ap.exp))
                    {
                        fields.add(ap.exp);
                    }

                }
                p.exp += toList(fields, "->") + " ";

            }if(!s.getFieldNames().isEmpty() && isPtype){
                List<String> fields = new Vector<String>();
                for (int i = 0; i < node.getArgs().size(); i++)
                {
                    PExp a = node.getArgs().get(i);
                    AlloyPart ap = a.apply(this, question);
                    if (question.containsVariable(ap.exp))
                    {
                        fields.add(ap.exp);
                    }

                }
            question.setFieldsProduct(fields);
           // p.exp+= toList(fields, " ");
            //p.exp += toList(fields, " ") + "  ";
            }
            return p;
        }

        return defaultInPExp(node, question);
    };

    @Override
    public AlloyPart caseALetBeStExp(ALetBeStExp node, Context question)
            throws AnalysisException
    {
        AlloyPart p = new AlloyPart("some ");
        Context ctxt = new Context(question);
        p.merge(node.getBind().apply(this, ctxt));
        p.exp += " | ";
        p.merge(node.getSuchThat().apply(this, ctxt));
        p.exp += " and ";
        p.merge(node.getValue().apply(this, ctxt));
        return p;
    }

    @Override
    public AlloyPart caseATuplePattern(ATuplePattern node, Context question)
            throws AnalysisException
    {
        ASetMultipleBind smb = null;
        if ((smb = node.getAncestor(ASetMultipleBind.class)) != null
                && smb.getSet().getType() instanceof ANamedInvariantType)
        {
            AlloyPart p = new AlloyPart();
            Map<String, PType> variables = new HashMap<String, PType>();

            ANamedInvariantType aNamedInvariantType = (ANamedInvariantType) smb.getSet().getType();
            Sig s = question.getSig(aNamedInvariantType.getName().getName());

            AProductType ptype = (AProductType) ((ASetType) aNamedInvariantType.getType()).getSetof();
            if (s.getFieldNames().size() == 1 || isPtype==true)
            {
                for (int i = 0; i < node.getPlist().size(); i++)
                { if(isPtype) {
                    PPattern a = node.getPlist().get(i);
                    AlloyPart ap = a.apply(this, question);
                    PType apType = ptype.getTypes().get(i);

                    variables.put(ap.exp, apType);
                    p.exp += ap.exp;
                    //p(variables.toString());
                    //if (i < node.getPlist().size() - 1) {
                      //  p.exp += "->";
                    //}


                    if (!question.containsVariable(ap.exp)) {
                        p.typeBindings.add(new AlloyTypeBind(ap.exp, question.getSig(apType)));
                       // p(p.getPredicatesSp().toString());
                        question.addVariable(ap.exp, apType);
                    }
                }else{
                    PPattern a = node.getPlist().get(i);
                    AlloyPart ap = a.apply(this, question);
                    PType apType = ptype.getTypes().get(i);
                    variables.put(ap.exp, apType);
                    p.exp += ap.exp;
                    if (i < node.getPlist().size() - 1) {
                        p.exp += "->";
                    }


                    if (!question.containsVariable(ap.exp)) {
                        p.typeBindings.add(new AlloyTypeBind(ap.exp, question.getSig(apType)));
                        question.addVariable(ap.exp, apType);
                    }
                }
                }

            }//p("final: "+p.toString());
            return p;
        }
        return super.caseATuplePattern(node, question);
    }

    @Override
    public AlloyPart caseAMkTypeExp(AMkTypeExp node, Context question)
            throws AnalysisException
    {

        if (question instanceof PredicatContext)
        {
            PredicatContext ctxt = (PredicatContext) question;
            AlloyPart p = new AlloyPart("(");
            ARecordInvariantType type = (ARecordInvariantType) ctxt.getReturnType();

            for (int i = 0; i < type.getFields().size(); i++)
            {
                p.exp += ctxt.getReturnName() + "."
                        + type.getFields().get(i).getTag() + "= ";
                p.merge(node.getArgs().get(i).apply(this, question));

                if (i < type.getFields().size() - 1)
                {
                    p.exp += " and ";
                }
            }
            p.exp += ")";
            return p;
        }
        return super.caseAMkTypeExp(node, question);
    }

    @Override
    public AlloyPart caseAMkBasicExp(AMkBasicExp node, Context question)
            throws AnalysisException
    {
        String name = BasicTokenSearch.getName(node);
        for (Part p : this.components)
        {
            if (p instanceof Sig && ((Sig) p).name.equals(name))
            {
                return new AlloyPart(name);
            }
        }
        return super.caseAMkBasicExp(node, question);
    }

    @Override
    public AlloyPart caseARecordPattern(ARecordPattern node, Context question)
            throws AnalysisException
    {
        question.setNameType(node.getTypename().toString());
        List<String> fieldNames = new Vector<String>();
        for (PPattern p : node.getPlist())
        {
            fieldNames.add(p.toString());
        }

        List<String> tfieldNames = new Vector<String>();
        for (AFieldField f : ((ARecordInvariantType) node.getType()).getFields())
        {
            tfieldNames.add(f.getTag());
        }

        if (tfieldNames.equals(fieldNames))
        {
            for (AFieldField f : ((ARecordInvariantType) node.getType()).getFields())
            {
                question.addVariable(f.getTag(), f.getType());
            }
            return new AlloyPart();
        } else
        {
            boolean parentIsDef = node.parent() instanceof PDefinition;
            String varName = null;
            AlloyPart p = new AlloyPart(" let ");
            if (!parentIsDef)
            {
                varName = getNewName();
                // p.typeBindings.add(new AlloyTypeBind(varName, question.getSig(node.getType())));
                p = new AlloyPart(varName);
                String let = "( let ";
                if(isPo){let=" let ";}
                Map<String, PType> variables = new HashMap<String, PType>();
                int  countSpace=question.variablesWithSpace(fieldNames);
                for (int i = 0; i < fieldNames.size(); i++)
                {

                    if(!isPo) {
                        let += fieldNames.get(i) + " = "
                                + (!parentIsDef ? varName + "." : "")
                                + tfieldNames.get(i);
                    }else{
                        if(!fieldNames.get(i).equals("-")) {
                            let += fieldNames.get(i) + " = "
                                    + (!parentIsDef ? "t" + "." : "")
                                    + tfieldNames.get(i);
                        }
                    }
                    PType type = ((ARecordInvariantType) node.getType()).getFields().get(i).getType();
                    question.addVariable(fieldNames.get(i), type);

                    variables.put(fieldNames.get(i), type);

                    if (i < (fieldNames.size() - countSpace))
                    {
                        let += ", ";
                    }

                }
                let += " | ";
                p.predicates.add(new AlloyLetExp(let, variables));

                return p;
            }
            int r = 0;
            for (int i = 0; i < fieldNames.size(); i++)
            {
                //LETSSSSS

                if(!fieldNames.get(i).equals("-")) {//ACRESCENTEI ISTO
                    p.exp += fieldNames.get(i) + " = "
                            + (!parentIsDef ? varName + "." : "")
                            + tfieldNames.get(i);
                    question.addVariable(fieldNames.get(i), ((ARecordInvariantType) node.getType()).getFields().get(i).getType());

                        if (i<(fieldNames.size()-1) && !fieldNames.get(i+1).equals("-") ){//i < (fieldNames.size() + r + 1)) {
                            p(i+"\t"+r+"\t");
                            p.exp += ", ";
                        }


                }/*else {
                    if (i < (fieldNames.size() - 1) && !fieldNames.get(i + 1).equals("-")) {//i < (fieldNames.size() + r + 1)) {
                        p.exp += ", ";p("entraaaaaa");
                    }
                }*/
            }
            p.exp += " | ";
            return p;
        }

    }

    @Override
    public AlloyPart caseAVariableExp(AVariableExp node, Context question)
            throws AnalysisException
    {

        AlloyPart p = new AlloyPart();
        String name = node.getName().getName()
                + (node.getName().isOld() ? "~" : "");
        String exp = "";
        if (question.containsState(name))
        {
            exp += question.getState(name);
        } else
        {
            if (!question.containsVariable(name)
                    && !(node.getType() instanceof AFunctionType))
            {
                Sig sig = question.getSig(node.getType());
                if (sig != null)
                {
                    if (sig.getFieldNames().size() == 1)
                    {
                        exp += name + "." + sig.getFieldNames().get(0);
                    }
                }
            } else
            {
                exp += name;
            }
        }
        if (exp.isEmpty())
        {
            if(this.isPo && !this.invariantMK){exp+=name;}
            else {
                System.err.println("no name for: " + node + " found "
                        + node.getLocation());
            }
        }
        p.exp += exp;

        return p;
    }

    @Override
    public AlloyPart caseAFieldExp(AFieldExp node, Context question)
            throws AnalysisException
    { //p(node.getField()+"\t\t"+node.getObject());
        // p(node.getType().toString());

        AlloyPart p = new AlloyPart(node.getObject() + "." + node.getField());
        //add this to fix exgt[ex.qua,o];
        //AlloyPart p = new AlloyPart(node.getField().getName());
        return p;
    }

    @Override
    public AlloyPart caseAMapDomainUnaryExp(AMapDomainUnaryExp node,
                                            Context question) throws AnalysisException
    {
        // AlloyPart p = new AlloyPart("(");
        // p.merge(node.getExp().apply(this, question));
        // p.exp += ").univ";

        AlloyPart p = new AlloyPart("dom[");
        p.merge(node.getExp().apply(this, question));
        p.exp += "]";
        return p;
    }

    public AlloyPart caseAMapRangeUnaryExp(
            org.overture.ast.expressions.AMapRangeUnaryExp node,
            Context question) throws AnalysisException
    {
        AlloyPart p = new AlloyPart("ran[");
        p.merge(node.getExp().apply(this, question));
        p.exp += "]";
        return p;
    };


    public AlloyPart caseANotUnaryExp(
            org.overture.ast.expressions.ANotUnaryExp node, Context question)
            throws AnalysisException
    {
        AlloyPart p = new AlloyPart("not ");
        p.merge(node.getExp().apply(this, question));
        return p;
    };

    @Override
    public AlloyPart caseADistUnionUnaryExp(ADistUnionUnaryExp node,
                                            Context question) throws AnalysisException
    {

        if (node.getExp() instanceof ASetCompSetExp)
        {
            AlloyPart p = new AlloyPart("toSet[ ");
            AlloyPart setcomprehension = node.getExp().apply(this, question);
            p.merge(setcomprehension);
            p.exp += "]";
            return p;
        } else
        {   AlloyPart p = new AlloyPart("(");
            p.merge(node.getExp().apply(this, question));
            Sig eType = question.getSig(node.getExp().getType());
            //p(setTypes.toString());
            if(setTypes.containsKey(eType.name)){
                if(setTypes.containsKey(setTypes.get(eType.name))){
                    p.exp += ".contents" + setTypes.get(setTypes.get(eType.name)) + ")";
                }else {
                    p.exp += ".contents" + setTypes.get(eType.name) + ")";
                }
            }
            if (eType.isWrapper)
            {
                Sig nestedT = question.getSig(eType.getField(eType.getFieldNames().get(0)).sigTypeName);
                p.exp += "." + eType.getFieldNames().get(0) + ")."
                        + nestedT.getFieldNames().get(0);
            }

            return p;
        }
    }

    @Override
    public AlloyPart caseASetEnumSetExp(ASetEnumSetExp node, Context question)
            throws AnalysisException
    {
        AlloyPart p = new AlloyPart("(");
        for (Iterator<PExp> itr = node.getMembers().iterator(); itr.hasNext();)
        {
            p.merge(itr.next().apply(this, question));
            if (itr.hasNext())
            {
                p.exp += " + ";
            }

        }
        p.exp += ")";
        return p;
    }

    @Override
    public AlloyPart caseASetCompSetExp(ASetCompSetExp node, Context question)
            throws AnalysisException
    {
        // first visit bind to build context
        Context setCompCtxt = new Context(question);
        AlloyPart pbind = new AlloyPart();
        for (Iterator<PMultipleBind> itr = node.getBindings().iterator(); itr.hasNext();)
        {
            AlloyPart bindPart = itr.next().apply(this, setCompCtxt);
            pbind.merge(bindPart);
            if (itr.hasNext())
            {
                pbind.exp += " , ";
            }
        }

        AlloyPart p = new AlloyPart("{");

        if (node.getFirst() instanceof ASetEnumSetExp)
        {
            ASetEnumSetExp setEnum = (ASetEnumSetExp) node.getFirst();

            for (Iterator<PExp> itr = setEnum.getMembers().iterator(); itr.hasNext();)
            {
                PExp exp = itr.next();

                AlloyPart ep = exp.apply(this, setCompCtxt);
                for (AlloyTypeBind bind : pbind.typeBindings)
                {
                    if (bind.var.equals(ep.exp))
                    {
                        p.exp += bind.exp;
                        if (itr.hasNext())
                        {
                            p.exp += ", ";
                        }
                        break;
                    }
                }

            }
            pbind.typeBindings.clear();
        } else
        {
            p.merge(node.getFirst().apply(this, setCompCtxt));
        }

        p.exp += " | ";

        p.merge(pbind);
        p.exp += "}";
        return p;
    }

    @Override
    public AlloyPart caseAQuoteLiteralExp(AQuoteLiteralExp node,
                                          Context question) throws AnalysisException
    {
        return new AlloyPart(node.getValue().getValue().toUpperCase());
    }

    @Override
    public AlloyPart defaultInINode(INode node, Context question)
            throws AnalysisException
    {
        if (node instanceof PExp)
        {
            return new AlloyPart(" /* NOT Translated("
                    + node.getClass().getSimpleName() + ")*/");
        }
        return null;
    }

    public AlloyPart defaultSBinaryExp(SBinaryExp node, Context question)
            throws AnalysisException
    {
        AlloyPart p = new AlloyPart("(");
        p.merge(node.getLeft().apply(this, question));

        if (node instanceof SBinaryExp)

        // switch (node.kindSBinaryExp())
        {
            if (node instanceof SBooleanBinaryExp)
            // case BOOLEAN:
            {
                SBooleanBinaryExp exp = (SBooleanBinaryExp) node;
                // switch (exp.kindSBooleanBinaryExp())
                // {
                // case AND:
                if (node instanceof AAndBooleanBinaryExp)
                {
                    p.exp += " and ";
                }
                // case EQUIVALENT:
                else if (node instanceof AEquivalentBooleanBinaryExp)
                {
                    p.exp += " = ";
                }
                // case IMPLIES:
                else if (node instanceof AImpliesBooleanBinaryExp)
                {
                    p.exp += " implies ";
                }
                // case OR:
                else if (node instanceof AOrBooleanBinaryExp)
                {
                    p.exp += " or ";

                }

                // }
            } else if (node instanceof ACompBinaryExp)
            {

            }
            // break;
            // case COMP:
            // break;
            // case DOMAINRESBY:
            // break;
            else if (node instanceof ADomainResByBinaryExp)
            {
            } else if (node instanceof ADomainResToBinaryExp)
            {
            }
            // case DOMAINRESTO:
            // break;
            else if (node instanceof AEqualsBinaryExp)
            // case EQUALS:
            {
                p.exp += " = ";
            } else if (node instanceof AInSetBinaryExp)
            {
                // case INSET:
                // p.exp += " in ";
                throw new AnalysisException("should not go here");
            } else if (node instanceof AMapUnionBinaryExp)
            {
                // case MAPUNION:
                // break;
            }
            // case NOTEQUAL:
            else if (node instanceof ANotEqualBinaryExp)
            {
                p.exp += " != ";
            } else if (node instanceof ANotInSetBinaryExp)
            {
                // case NOTINSET:
                // break;
            } else if (node instanceof SNumericBinaryExp)
            {
                // case NUMERIC:
                // break;
            } else if (node instanceof APlusPlusBinaryExp)
            {
                // case PLUSPLUS:
                p.exp += " ++ ";
                // break;
            }
            // case PROPERSUBSET:
            else if (node instanceof AProperSubsetBinaryExp)
            {
                // break;
            }
            // case RANGERESBY:
            // break;
            else if (node instanceof ARangeResByBinaryExp)
            {

            } else if (node instanceof ARangeResToBinaryExp)
            {
                // case RANGERESTO:
                p.exp += " :> ";
                // break;
            }
            // case SEQCONCAT:
            // break;
            else if (node instanceof ASeqConcatBinaryExp)
            {

            } else if (node instanceof ASetDifferenceBinaryExp)
            {

            }
            // case SETDIFFERENCE:
            // break;
            else if (node instanceof ASetIntersectBinaryExp)
            {
                // case SETINTERSECT:
                p.exp += " & ";
                // break;
            } else if (node instanceof ASetUnionBinaryExp)
            {
                // case SETUNION:
                p.exp += " + ";
                // break;
            } else if (node instanceof AStarStarBinaryExp)
            {
                // case STARSTAR:
                // break;
            } else if (node instanceof ASubsetBinaryExp)
            {
                // case SUBSET:
                // break;

            }
        }

        p.merge(node.getRight().apply(this, question));
        p.exp += ")";
        return p;
    };

    @Override
    public AlloyPart caseAMapEnumMapExp(AMapEnumMapExp node, Context question)
            throws AnalysisException
    {
        AlloyPart p = new AlloyPart("(");

        for (Iterator<AMapletExp> itr = node.getMembers().iterator(); itr.hasNext();)
        {
            AMapletExp maplet = itr.next();

            p.merge(maplet.getLeft().apply(this, question));
            p.exp += " -> ";
            p.merge(maplet.getRight().apply(this, question));
            if (itr.hasNext())
            {
                p.exp += " + ";
            }
        }

        p.exp += ")";
        return p;
    }

    @Override
    public AlloyPart caseADomainResByBinaryExp(ADomainResByBinaryExp node,
                                               Context question) throws AnalysisException
    {
        AlloyPart p = new AlloyPart("(");
        p.exp += "univ -";
        p.merge(node.getLeft().apply(this, question));
        p.exp += ")";
        p.exp += " <: ";
        p.merge(node.getRight().apply(this, question));
        return p;
    }

    @Override
    public AlloyPart caseAApplyExp(AApplyExp node, Context question)
            throws AnalysisException
    {
        AlloyPart p = new AlloyPart();
        if (node.getRoot().getType() instanceof SMapType
                && node.getAncestor(AStateDefinition.class) == null)
        {
            AlloyPart p1 = new AlloyPart();
            p1.merge(node.getArgs().get(0).apply(this, question));
            p1.exp += " in dom[";
            p1.merge(node.getRoot().apply(this, question));
            p1.exp += "]";
            p.topLevel.add(new AlloyExp(" and /*Map domain pre condition */ \n\t"
                    + p1.exp));
        }

        p.merge(node.getRoot().apply(this, question));
        p.exp += "[";
        for (Iterator<PExp> itr = node.getArgs().iterator(); itr.hasNext();)
        {
            p.merge(itr.next().apply(this, question));
            if (itr.hasNext())
            {
                p.exp += ", ";
            }
        }
        p.exp += "] ";
        return p;
    }

    @Override
    public AlloyPart caseAMapInverseUnaryExp(AMapInverseUnaryExp node,
                                             Context question) throws AnalysisException
    {
        // Only supported if directly before an apply
        AlloyPart p = new AlloyPart("~(");
        mergeReturns(p, node.getExp().apply(this, question));
        p.exp += ")";
        return p;
    }

    @Override
    public AlloyPart caseAForAllExp(AForAllExp node, Context question)
            throws AnalysisException
    {
        AlloyPart p = new AlloyPart("( all ");
        Context ctxt = new Context(question);//p(ctxt.toString());
        AlloyPart bindPart = (node.getBindList().get(0).apply(this, ctxt));// TODO
        //p(node.toString());
        boolean hasTypeBindings = !bindPart.typeBindings.isEmpty();
        if (bindPart.typeBindings.isEmpty())
        {
            p.merge(bindPart);
            p.exp += " | ";
        } else
        {
            for (Iterator<AlloyTypeBind> itr = bindPart.typeBindings.iterator(); itr.hasNext();)
            {
                p.exp += itr.next().exp;
                if (itr.hasNext())
                {
                    p.exp += ", ";
                }
            }
            p.exp += " | (";//p("Bind: "+bindPart.toString());
            bindPart.typeBindings.clear();
            p.merge(bindPart);
            p.exp += " implies ";
        }
        boolean hasLet = false;
        if (!p.predicates.isEmpty())
        {

            for (AlloyExp exp : p.predicates)
            {
                if (exp instanceof AlloyLetExp)
                {
                    hasLet = true;
                    break;
                }
            }

            p.appendPredicates();
            if (!hasLet)
            {
                p.exp += " and ";
            }
        }

        p.merge(node.getPredicate().apply(this, ctxt));
        if (hasLet)
        {
            p.exp += ")";
        }
        p.exp += ")";
        if (hasTypeBindings)
        {
            p.exp += ")";
        }
        return p;
    }

    public AlloyPart caseAExistsExp(
            org.overture.ast.expressions.AExistsExp node, Context question)
            throws AnalysisException
    {
        AlloyPart p = new AlloyPart("some ");
        mergeReturns(p, node.getBindList().get(0).apply(this, question));// TODO
        //p(p.predicates.toString() + "\t\t" + p.topLevel.toString());
       // p("node " + node.getPredicate().getClass().getSimpleName());
        p.exp += " | ";
        mergeReturns(p, node.getPredicate().apply(this, question));
        if(isPo){
            AlloyPart x = node.getPredicate().apply(this, question);
            if(invariantMK) {
                this.componentsPO.add(new Pred(question.getNameType(), "","some  t : "+question.getNameType()+" | "+p.getPredicatesSp() + x.toPartBody(), true));
                this.componentsPO.add(new Check("proof" + question.getNameType()));
            }else{
                ATypeMultipleBind a = (ATypeMultipleBind)node.getBindList().getFirst();
                //p(a.getType().toString());
                this.componentsPO.add(new Pred(a.getType().toString(), "","some "+ node.getBindList().getFirst().toString()+" | " + x.toPartBody(), true));
                this.componentsPO.add(new Check("proof" + a.getType().toString()));
            }

        }else{

        }
        return p;

    }

   /* @Override
    public AlloyPart caseATypeMultipleBind(ATypeMultipleBind node, Context question) throws AnalysisException {
        p("type bind");
        p(node.getPlist().getFirst().getClass().getSimpleName());
        //p(node.getPlist().getFirst().getClass().getSimpleName().toString());
        //p(node.getPlist().toString());
        //p(node.getPlist().getFirst().apply(this, question).toString());
        return super.caseATypeMultipleBind(node, question);
    }*/

    @Override
    public AlloyPart caseASetMultipleBind(ASetMultipleBind node,
                                          Context question) throws AnalysisException
    {

        AlloyPart p = createNewReturnValue(node, question);

        for (Iterator<PPattern> iterator = node.getPlist().iterator(); iterator.hasNext();)
        {
            PPattern e = iterator.next();
            parametersListFunction.put(e.toString(),"");
            if (!_visitedNodes.contains(e))
            {
                AlloyPart sp = e.apply(this, question);
                p.merge(sp);
                question.addVariable(sp.exp, getBoundType(node.getSet().getType()));
                for (AlloyExp sub : sp.predicates)
                {
                    if (sub instanceof AlloyLetExp)
                    {
                        question.addVariables(((AlloyLetExp) sub).variables);
                    }
                }
                if (iterator.hasNext())
                {
                    p.merge(new AlloyPart(", "));
                }
            }

        }
        boolean isTupeBind = false;
        for (PPattern pattern : node.getPlist())
        {
            if (pattern instanceof ATuplePattern)
            {
                isTupeBind = true;
                break;
            }
        }

        if (isTupeBind)
        {
            if(!isPtype) {
            }else {
                p.exp += " in ";

            }

        } else
        {
            p.exp += " : ";
        }
        if (node.getSet() != null && !_visitedNodes.contains(node.getSet()))
        {

            if(isPtype && checkIsValidSing(node.getSet().getType().toString())){
                List l = (List)p.typeBindings ;
                p.exp="";
                //p(l.toString());
               // p("cena final :"+l.toString());
                AlloyTypeBind c1 = (AlloyTypeBind)l.get(0);
                AlloyTypeBind c2 = (AlloyTypeBind)l.get(1);
                p.exp+=c1.var+" in "+getBind(node.getSet(), question)+".fst and ";//-> c1
                p.exp+=c2.var+" in "+getBind(node.getSet(), question)+".snd";

                //p.merge(getBind(node.getSet(), question));// node.getSet().apply(this, question));
            }else {
                String aux="";
                p.merge(getBind(node.getSet(), question));// node.getSet().apply(this, question));| let col = col. contentsCountry|
                if(setTypes.containsKey(node.getSet().getType().toString())){
                    if(setTypes.containsKey(setTypes.get(node.getSet().getType().toString()))){
                        aux=setTypes.get(setTypes.get(node.getSet().getType().toString()));
                                    //p.exp+=".contents"+aux;
                    }
                }

                //p("tipo "+node.getSet().getType()+"\n"+setTypes.toString()+"\t"+aux);
                for(String  e : parametersListFunction.keySet()){
                    if(parametersListFunction.get(e).equals("")){
                        parametersListFunction.put(e,getBind(node.getSet(), question).toString());
                    }
                }

            }
        }

        return p;
    }

    AlloyPart getBind(PExp bindto, Context ctxt) throws AnalysisException
    {
        AlloyPart p = new AlloyPart();
        p.merge(bindto.apply(this, ctxt));


        Sig s = ctxt.getSig(bindto.getType());
        if (s != null && bindto instanceof AVariableExp && s.isWrapper)
        {
            p.exp += "." + s.getFieldNames().get(0);
        }
        return p;
    }

    public PType getBoundType(PType type)
    {
        if (type instanceof ASetType)
        {
            return ((ASetType) type).getSetof();
        } else if (type instanceof SSeqType)
        {
            return ((SSeqType) type).getSeqof();
        }
        return type;
    }

    @Override
    public AlloyPart caseAIdentifierPattern(AIdentifierPattern node,
                                            Context question) throws AnalysisException
    {

        return new AlloyPart(node.getName().getName());

    }

    @Override
    public AlloyPart mergeReturns(AlloyPart original, AlloyPart new_)
    {
        if (original == null || new_ == null)
        {
            return null;
        }
        original.exp += new_.exp;
        original.predicates.addAll(new_.predicates);
        original.topLevel.addAll(new_.topLevel);
        original.typeBindings.addAll(new_.typeBindings);

        return original;
    }

    @Override
    public AlloyPart createNewReturnValue(INode node, Context question)
    {
        return new AlloyPart();
    }

    @Override
    public AlloyPart createNewReturnValue(Object node, Context question)
    {
        return new AlloyPart();
    }

    /**/
    public AlloyPart caseAPlusPlusBinaryExp(
            org.overture.ast.expressions.APlusPlusBinaryExp node,
            Context question) throws AnalysisException
    {
        return defaultSBinaryExp(node, question);
    };

    public AlloyPart caseAEqualsBinaryExp(
            org.overture.ast.expressions.AEqualsBinaryExp node, Context question)
            throws AnalysisException
    {
        if (node.getRight() instanceof ASetEnumSetExp
                && ((ASetEnumSetExp) node.getRight()).getMembers().isEmpty())
        {
            AlloyPart p = new AlloyPart("no ");
            p.merge(node.getLeft().apply(this, question));
            return p;
        } else if (node.getLeft() instanceof ASetEnumSetExp
                && ((ASetEnumSetExp) node.getLeft()).getMembers().isEmpty())
        {
            AlloyPart p = new AlloyPart("no ");
            p.merge(node.getRight().apply(this, question));
            return p;
        }
        return defaultSBinaryExp(node, question);
    };

    public AlloyPart caseARangeResToBinaryExp(
            org.overture.ast.expressions.ARangeResToBinaryExp node,
            Context question) throws AnalysisException
    {
        return defaultSBinaryExp(node, question);
    };

    public AlloyPart caseAImpliesBooleanBinaryExp(
            org.overture.ast.expressions.AImpliesBooleanBinaryExp node,
            Context question) throws AnalysisException
    {
        return defaultSBinaryExp(node, question);
    };

    public AlloyPart caseAInSetBinaryExp(
            org.overture.ast.expressions.AInSetBinaryExp node, Context question)
            throws AnalysisException
    {

        AlloyPart p = new AlloyPart("(");
        String bind = "";

        bind = " in ";

        p.merge(node.getLeft().apply(this, question));
        //p("CENA: " + node.getLeft().getClass().toString() + "\t\t" + p.toString() + "\t\t" + question.getFieldsProduct().toString());
        if(isPtype){
            p.exp+=question.getFieldsProduct().get(0)+" in "+getBind(node.getRight(), question)+
                    ".fst and "+question.getFieldsProduct().get(1)+" in "+getBind(node.getRight(), question)+".snd";
        }else {
            p.exp += bind;
            p.merge(getBind(node.getRight(), question));
            String n = getBind(node.getRight(), question).toString();p(n.toString());
            if(parametersListFunction.containsKey(n)){p("entra"+setTypes.toString()+"\t"+parametersListFunction.toString());
                if(parametersListFunction.containsKey(parametersListFunction.get(n))){
                    String x = parametersListFunction.get(parametersListFunction.get(n));
                    if(setTypes.containsKey(x) && setTypes.containsKey(setTypes.get(x))){
                        p.exp+=".contents"+setTypes.get(setTypes.get(x));
                    }
                }
            }

        }
        if (!p.predicates.isEmpty())
        {
            p.exp += " |";
        }

        p.appendPredicates();
        p.exp += ")";

        return p;
    };

    public AlloyPart caseAAndBooleanBinaryExp(
            org.overture.ast.expressions.AAndBooleanBinaryExp node,
            Context question) throws AnalysisException
    {p(node.getLeft().getClass().getSimpleName());
        return defaultSBinaryExp(node, question);
    };

    public AlloyPart caseAOrBooleanBinaryExp(AOrBooleanBinaryExp node,
                                             Context question) throws AnalysisException
    {
        return defaultSBinaryExp(node, question);
    };

    public AlloyPart caseANotEqualBinaryExp(
            org.overture.ast.expressions.ANotEqualBinaryExp node,
            Context question) throws AnalysisException
    {
        if(node.getRight() instanceof  ASetEnumSetExp){
            AlloyPart p = new AlloyPart("( some ");
            p.exp+=node.getLeft().toString();
            p.exp += " )";
            return p;
        }
        return defaultSBinaryExp(node, question);
    };

    @Override
    public AlloyPart caseABooleanConstExp(ABooleanConstExp node, Context question) throws AnalysisException {
        AlloyPart p = new AlloyPart("");
      if(node.toString().equals("true")){
            p.exp="True";
        }
        else{
            p.exp="False";
        }

        return p;
    }

    public AlloyPart caseASetIntersectBinaryExp(
            org.overture.ast.expressions.ASetIntersectBinaryExp node,
            Context question) throws AnalysisException
    {
        return defaultSBinaryExp(node, question);
    };

    @Override
    public AlloyPart caseASetUnionBinaryExp(ASetUnionBinaryExp node,
                                            Context question) throws AnalysisException
    {// TODO: not checked
        return defaultSBinaryExp(node, question);
    }


    public AlloyPart caseAGreaterNumericBinaryExp(AGreaterNumericBinaryExp node,
                                                  Context question) throws AnalysisException
    {
        AlloyPart p = new AlloyPart("gt[");

        p.merge(node.getLeft().apply(this, question));
        p.exp += ',';
        p.merge(getBind(node.getRight(), question));
        p.exp += ']';

        return p;
    };

    public AlloyPart caseALessNumericBinaryExp(ALessNumericBinaryExp node,
                                               Context question) throws AnalysisException
    {
        AlloyPart p = new AlloyPart("lt[");

        p.merge(node.getLeft().apply(this, question));
        p.exp += ',';
        p.merge(getBind(node.getRight(), question));
        p.exp += ']';
        return p;
    };

    public AlloyPart caseAGreaterEqualNumericBinaryExp(AGreaterEqualNumericBinaryExp node,
                                                       Context question) throws AnalysisException
    {
        AlloyPart p = new AlloyPart("gte[");

        p.merge(node.getLeft().apply(this, question));
        p.exp += ',';
        p.merge(getBind(node.getRight(), question));
        p.exp += ']';
        return p;
    };

    public AlloyPart caseALessEqualNumericBinaryExp(ALessEqualNumericBinaryExp node,
                                                    Context question) throws AnalysisException
    {
        AlloyPart p = new AlloyPart("lte[");

        p.merge(node.getLeft().apply(this, question));
        p.exp += ',';
        p.merge(getBind(node.getRight(), question));
        p.exp += ']';
        return p;
    };

    public AlloyPart caseALenUnaryExp (ALenUnaryExp node, Context question) throws AnalysisException {
        AlloyPart p = new AlloyPart("#(");

        p.merge(node.getExp().apply(this, question));

        p.exp += ')';
        return p;
    }


    @Override
    public AlloyPart caseAIfExp(AIfExp node, Context question) throws AnalysisException {
        AlloyPart alloyPart = new AlloyPart("");
        alloyPart.merge(node.getTest().apply(this,question));
        alloyPart.exp+=" implies ";
        alloyPart.merge(node.getThen().apply(this, question));
        alloyPart.exp+=" else ";
        alloyPart.merge(node.getElse().apply(this, question));
        return alloyPart;
    }

    public AlloyPart caseAIntLiteralExp (AIntLiteralExp node, Context question) throws AnalysisException {
        return new AlloyPart(node.getValue().toString());
    }
}

