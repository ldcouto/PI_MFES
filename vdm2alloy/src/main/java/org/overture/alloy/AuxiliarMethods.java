package org.overture.alloy;

import org.overture.alloy.ast.Part;
import org.overture.alloy.ast.Sig;

import java.util.List;
import java.util.Vector;

/**
 * Created by macbookpro on 25/03/15.
 */
public class AuxiliarMethods {

    public List<Part> insertSuperQuotesFromList(List<String> sup,List<Part> components,Sig s) {
        List<Part> cloneComponents = new Vector<Part>();
        int j = 0;
        for (Part x : components) {
            if (x instanceof Sig) {
                for (String l : sup) {
                    if (((Sig) x).name.equals(l)) {
                        if (j == 0) {
                            cloneComponents.add(s);
                            j = 1;
                        }
                        ((Sig) x).supers.add(s);
                    }
                }
            }
            cloneComponents.add(x);
        }
        return cloneComponents;
    }

    public List<Part> insertSuperQuotes(String sup,List<Part> components,Sig s) {
        List<Part> cloneComponents = new Vector<Part>();
        int j = 0;
        for (Part x : components) {
            if (x instanceof Sig) {
                    if (((Sig) x).name.equals(sup)) {
                        if (j == 0) {
                            cloneComponents.add(s);
                            j = 1;
                        }
                        ((Sig) x).supers.add(s);
                    }
            }
            cloneComponents.add(x);
        }
        return cloneComponents;
    }
}
