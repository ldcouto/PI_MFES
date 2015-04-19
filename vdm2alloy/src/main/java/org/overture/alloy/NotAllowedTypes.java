package org.overture.alloy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by macbookpro on 16/04/15.
 */
public class NotAllowedTypes {

    public HashMap<String,ArrayList<Integer>> types;//=  new HashMap<String,Integer>();

    public NotAllowedTypes(HashMap<String,ArrayList<Integer>> types,int flag){
        if(flag==1) {
            HashMap<String, ArrayList<Integer>> t = new HashMap<String, ArrayList<Integer>>();
            for (String s : types.keySet()) {
                ArrayList<Integer> x = new ArrayList<Integer>();
                t.put(translation(s), x);
            }
            this.types=t;
        }else{
            this.types=types;
        }
    }

    public NotAllowedTypes(){
      this.types =   new HashMap<String,ArrayList<Integer>>();
    }

    public void addType(String type,Integer i){
        ArrayList<Integer> x;
        if(this.types.get(type).isEmpty()){ //if is empty
            x=new ArrayList<Integer>();
            x.add(i);
            this.types.put(type,x);
        }
        else {
            x=this.types.get(type);
            x.add(i);
            this.types.put(type,x);
        }
    }

    public HashMap<String,ArrayList<Integer>> getTypes() {
        return types;
    }

    public boolean hasnoAllowedType(){//true- has type not allowed types
        for (String s : this.types.keySet()){
            if(this.types.get(s).size()>0){return true;}
        }
        return false;
    }

    public String translation(String Name){
        if(Name.equals("map")){return "AMapMapType";}
        else{
            if(Name.equals("real")){return "ARealNumericBasicType";}
            else {
                if(Name.equals("bool")){return "ABooleanBasicType";}
                else{return null;}
            }
        }


    }

    public String translationToName(String Name){
        if(Name.equals("AMapMapType")){return "map";}
        else{
            if(Name.equals("ARealNumericBasicType")){return "real";}
            else {
                if(Name.equals("ABooleanBasicType")){return "bool";}
                else{return null;}
            }
        }


    }

    @Override
    public String toString() {
        String s = "";
        for(String st : this.types.keySet()) {
            if(!this.types.get(st).isEmpty()){
                s +="Not Allowed type "+translationToName(st)+" on the line: ";
                int it=0;
                for(Integer i : this.types.get(st)){
                    if(it==0){
                        s +=i;
                    }else{
                        s +=" , "+i;
                    }
                    it++;
                }
                s+="\n";
            }

        }
        return s;

    }
}
