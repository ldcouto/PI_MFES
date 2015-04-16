package org.overture.alloy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by macbookpro on 16/04/15.
 */
public class NotAllowedTypes {
    //List<String> types ;//= new ArrayList<String>() ;
    public HashMap<String,Integer> types;//=  new HashMap<String,Integer>();

    public NotAllowedTypes(HashMap<String,Integer> types){
        HashMap<String,Integer> t=  new HashMap<String,Integer>();
        for (String s : types.keySet()){
            t.put(translation(s), -1);
        }
        this.types=t;
    }

    public void addType(String type,Integer line){
        this.types.put(this.translation(type), line);
    }

    public HashMap<String,Integer> getTypes() {
        return types;
    }

    public boolean hasnoAllowedType(){//true- has type not allowed types
        for (Integer i : this.types.values()){
            if(i!=-1){
                return true;
            }
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
        return "NotAllowedTypes{" +
                "types=" + types +
                '}';
    }


}
