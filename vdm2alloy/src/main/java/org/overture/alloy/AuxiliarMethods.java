package org.overture.alloy;

import org.overture.alloy.ast.Comment;
import org.overture.alloy.ast.Fact;
import org.overture.alloy.ast.Part;
import org.overture.alloy.ast.Sig;
import org.overture.ast.types.PType;

import java.util.List;

/**
 * Created by macbookpro on 08/04/15.
 */
public class AuxiliarMethods {


    public List createNatFact(List<Part> l){
        Fact f = new Fact("factNat","{ nat = { i:Int | gte[i,0]}}");
         l.add(f);
        return l;
    }

    public void createNats(String name,PType type,Context ct,List<Part> l){
        Sig s = new Sig(name, true);
        ct.addType(type, s);
        l.add(s);
        createNatFact(l);

    }




}
