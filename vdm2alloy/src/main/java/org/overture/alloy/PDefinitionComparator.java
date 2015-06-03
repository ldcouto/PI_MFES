package org.overture.alloy;

import org.overture.ast.definitions.PDefinition;

import java.util.Comparator;

/**
 * Created by macbookpro on 03/06/15.
 */
public class PDefinitionComparator implements Comparator<PDefinition> {
    public int compare(PDefinition c1, PDefinition c2)
    {
        return ((Integer)c1.getLocation().getStartLine()).compareTo(c2.getLocation().getStartLine());
    }
}
