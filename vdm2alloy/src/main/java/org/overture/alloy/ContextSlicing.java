package org.overture.alloy;

import org.overture.ast.node.INode;
import org.overture.ast.node.NodeList;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;


/**
 * Created by macbookpro on 24/04/15.
 */
public class ContextSlicing {
    NotAllowedTypes aux= new NotAllowedTypes();
        private List<String> nodes;
        private HashMap<String,String> inodes =  new HashMap<String,String>();
        private List<INode> context = new Vector<INode>();
        private boolean isModelValide = false;

    public void setModelValide(boolean isModelValide) {
        this.isModelValide = isModelValide;
    }

    public boolean isModelValide() {

        return isModelValide;
    }

    public boolean intersetionTypes(){
        for(INode s : context){
            if(this.nodes.contains(aux.translation(s.toString()))){return false;}
        }
        return true;
    }

    public List<INode> getContext() {
        return context;
    }
    public boolean hasType(INode s){
        if(this.context.contains(s.toString())){return  true;}
        else{return false;}
    }

    public void initContext(){
        this.context.clear();
        this.setModelValide(false);
        this.context=new Vector<INode>();
    }

    public void addContext(INode s){
        this.context.add(s);
    }

    public int  invAddTypes(String type){
            int f=0;
            if(this.hasTypeKey(type)) {//if type node.getType already has value

               // if (this.nodes.contains(aux.translation(this.inodes.get(type)))) {
                if(this.invAddTypesAux(type)==0){
                    return 1;//return 1 if node.getType exist in hash Keys and that type is invalid.
                } else{return 3;}// return 3 if node.getType exist but his type is valid.

            }else{
                return 2;//return 2 if node.getType don't exist in hashkeys
            }
    }

    public int invAddTypesAux(String type){//
        if( this.hasTypeKey(type)) {
            if (this.nodes.contains(aux.translation(this.inodes.get(type)))) {
                return 0;
            } else {
                if (this.hasTypeKey(type)) {
                    return invAddTypesAux(this.inodes.get(type));
                } else {
                    return 1;
                }
            }
        }
        return 1;
    }



    public List<String> getNodes() {
        return nodes;
    }

    public HashMap<String, String> getInodes() {
        return inodes;
    }

    public boolean hasTypeKey(String s){
        if(this.inodes.containsKey(s)){return true;}
        else{return false;}
    }

    public boolean hasTypeValue(String s){
        if(this.inodes.containsValue(s)){return true;}
        else{return false;}
    }

    public void setValueNull(String s){//
        this.inodes.put(s,null);
    }

    public String getValue(String s){
        return this.inodes.get(s);
    }

    public void addType (String s1,String s2){
        this.inodes.put(s1,s2);
    }

    public void addtypeList(String s){
        this.nodes.add(s);
    }

    public boolean hasTypeNodeName(String n){
        if(this.nodes.contains(aux.translation(n))){
            return true;
        }else{return false;}
    }

    @Override
    public String toString() {
        return "ContextSlicing{" +
                "nodes=" + nodes +
                '}';
    }

    public ContextSlicing(List<String> nodes) {
        List <String> newL = new Vector<String>();
        for(String l :nodes ){
            newL.add(aux.translation(l));
        }
        this.nodes = newL;
    }

    public void p(String string){
        System.out.println(string);
    }
}
