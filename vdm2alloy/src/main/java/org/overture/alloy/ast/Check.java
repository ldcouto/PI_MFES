package org.overture.alloy.ast;

/**
 * Created by macbookpro on 23/05/15.
 */
public class Check extends Part{
    private String def;
    public Check(String name) {
        this.def=name;
    }

    public String toString(){
        String st="";
        st+="check { \n " + this.def+"\n}";
        return st;
    }
}
