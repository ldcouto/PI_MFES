package org.overturetool.alloy.test.unit;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Created by macbookpro on 19/05/15.
 */
public  class TypeList {

    public int total;


        private HashMap<String,ArrayList<String>> listTypes = new HashMap<String, ArrayList<String>>() {{
            put("slicingTest1.vdmsl",newArray("Cg","t"));
            put("slicingTest2.vdmsl",newArray("A","t"));
            put("slicingTest3.vdmsl",newArray("x","t"));
            put("slicingTest4.vdmsl",newArray("record","t"));
            put("slicingTest5.vdmsl",newArray("xx","t"));
            put("slicingTest6.vdmsl",newArray("TransactionType","t"));
            put("slicingTest7.vdmsl",newArray("D","t"));
            put("slicingTest8.vdmsl",newArray("record","t"));
            put("slicingTest9.vdmsl",newArray("yyy","t"));
            put("slicingTest91.vdmsl",newArray("H","t"));
            put("slicingTest92.vdmsl",newArray("Len","f"));
            put("slicingTest93.vdmsl",newArray("aux","f"));
            put("slicingTest94.vdmsl",newArray("sum","f"));
            put("slicingTest95.vdmsl",newArray("balanceOf","f"));
            put("slicingTest96.vdmsl",newArray("StepLength","v"));
            put("slicingTest97.vdmsl",newArray("letters","v"));
            put("slicingTest98.vdmsl",newArray("Exchange","st"));
            put("slicingTest99.vdmsl",newArray("Connect","op"));
            put("slicingTest20.vdmsl",newArray("colMap","fi"));
            
    }};

    public ArrayList<String> newArray(String s1,String s2){
        ArrayList<String> arrayList = new ArrayList<String>();
        arrayList.add(s1);
        arrayList.add(s2);
        return arrayList;
    }

    public TypeList() {
        this.total=this.listTypes.size();
    }

    public HashMap<String, ArrayList<String>> getListTypes() {
        return listTypes;
    }

    public int getTotal() {

        return total;
    }





    public ArrayList<String> getPair(String s){
       return this.listTypes.get(s);
    }


    public void p(String string){
        System.out.println(string);
    }
}
