package org.overture.alloy;

import org.overture.ast.definitions.PDefinition;
import org.overture.ast.node.INode;
import org.overture.ast.node.NodeList;

import java.util.List;
import java.util.Vector;

/**
 * Created by macbookpro on 27/04/15.
 */
public class ContextSlicing {

    private  String type;

    private boolean isNotAllowed;//true if is allowed type, else false

    private boolean isRecord; // through this boolean ,we know if the map is a record or atrib.

    private String def ;

    private String functionName="";

    public String getFunctionName() {
        return functionName;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    private NodeList<PDefinition> typesDep;

    public String getType() {
        return type;
    }

    public ContextSlicing(String def,String op) {
        this.def = def;
        this.typesDep= new NodeList(null);
        this.type=translation(op);
    }

    public String translation(String s){
        if(s.equals("t")){
            return "ATypeDefinition";
        }else if(s.equals("f")){
            return "AExplicitFunctionDefinition";
        }else if(s.equals("v")){
            return "AValueDefinition";
        }
        return "";
    }

    public String inverseTranslation(String type){
        if(type.equals("ATypeDefinition")){
            return "t";
        }else if(type.equals("AExplicitFunctionDefinition")){
            return "f";
        }else if(type.equals("AValueDefinition")){
            return "v";
        }
        return "";
    }
    public ContextSlicing(){

    }

    public NodeList<PDefinition> getTypesDep() {
        return typesDep;
    }

    public void addTypesDep(PDefinition i){
        this.typesDep.add((PDefinition)i.clone());
    }

    public String getDef() {
        return def;
    }

    public void setDef(String def) {
        this.def = def;
    }

    public boolean hasType(String s){
        if(this.typesDep.contains(s)){return true;}
        else{return false;}
    }

    public boolean invIsInList(String s){
        for(PDefinition p : this.typesDep){
            if(p.getType().toString().equals(s)){return  true;}
        }
        return false;
    }



    /************************************************/
    public boolean isAllowed() {
        return isNotAllowed;
    }

    public void init() {
        this.isNotAllowed=true;
        this.isRecord=false;
    }



    public boolean isRecord() {
        return isRecord;
    }

    public void setRecord(boolean isRecord) {
        this.isRecord = isRecord;
    }

    public void setNotAllowed(boolean isNotAllowed) {

        this.isNotAllowed = isNotAllowed;
    }




}

