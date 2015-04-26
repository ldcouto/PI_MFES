package org.overture.alloy;

import java.util.HashMap;
import java.util.List;
import java.util.Vector;

/**
 * Created by macbookpro on 26/04/15.
 */
public class NewContextSlicing {

    private boolean isNotAllowed;//true if is allowed type, else false

    private boolean isRecord; // through this boolean ,we know if the map is a record or atrib.

    private HashMap<String,String> inodes =  new HashMap<String,String>();

    public final List<String> listNotAllowed = new Vector<String>();




    public List getListNotAllowed(){
        this.listNotAllowed.add("real");
        this.listNotAllowed.add("map");
        this.listNotAllowed.add("bool");
        return listNotAllowed;
    }

    //---------------

    public NewContextSlicing() {
        this.isNotAllowed = true;
    }

    public void addType (String s1,String s2){
        this.inodes.put(s1,s2);

    }

    public String getTypeVar(String var){
        if(this.inodes.containsKey(this.inodes.get(var))){
            return getTypeVar(this.inodes.get(var));
        }else{return this.inodes.get(var);}
    }


    public boolean invAllowed(String s) {
        if(this.listNotAllowed.contains(getTypeVar(s))){return false;}
        else{return true;}
    }

    public HashMap<String, String> getInodes() {
        return inodes;
    }



    //------------------

    public void init() {
    this.isNotAllowed=true;
    }

    public boolean isRecord() {
        return isRecord;
    }

    public void setRecord(boolean isRecord) {
        this.isRecord = isRecord;
    }
    //---------------------
    public void setNotAllowed(boolean isNotAllowed) {

        this.isNotAllowed = isNotAllowed;
    }

    //--------------------

    public boolean isNotAllowed() {
        return isNotAllowed;
    }

    public void p(String string){
        System.out.println(string);
    }


}
