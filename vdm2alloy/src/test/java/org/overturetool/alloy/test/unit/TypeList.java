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


    private HashMap<Integer,ArrayList<String>> listTypes = new HashMap<Integer, ArrayList<String>>() {{
        put(1,newArray("Cg","t"));
        put(2,newArray("A","t"));
        put(3,newArray("x","t"));
        put(4,newArray("record","t"));
        put(5,newArray("xx","t"));
        put(6,newArray("TransactionType","t"));
        put(7,newArray("D","t"));
        put(8,newArray("record","t"));
        put(9,newArray("yyy","t"));
        put(10,newArray("H","t"));
        put(11,newArray("Len","f"));
        put(12,newArray("aux","f"));
        put(13,newArray("sum","f"));
        put(14,newArray("balanceOf","f"));
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

    public HashMap<Integer, ArrayList<String>> getListTypes() {
        return listTypes;
    }

    public int getTotal() {

        return total;
    }

    public Integer getVarAndInc() throws FileNotFoundException {
        File file = new File("/Users/macbookpro/Desktop/Pi_MFES/PI_MFES/vdm2alloy/src/test/java/org/overturetool/alloy/test/unit/counterTest.txt");
        Scanner scanner = new Scanner(file);
        Integer x =  scanner.nextInt();

        if(this.total==x) {
            PrintWriter writer = new PrintWriter(file);
            writer.print(1);
            writer.close();
        }
        else {
            PrintWriter writer = new PrintWriter(file);
            writer.print(x+1);
            writer.close();
        }
        return x;

    }

    public ArrayList<String> getPair(int x){
       return this.listTypes.get(x);
    }


    public void p(String string){
        System.out.println(string);
    }
}
