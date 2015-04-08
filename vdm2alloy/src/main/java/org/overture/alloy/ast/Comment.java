package org.overture.alloy.ast;

import java.util.List;

/**
 * Created by macbookpro on 08/04/15.
 */
public class Comment extends Part {
    String content;

    public Comment(String ct) {
        this.content=ct;
    }

    @Override
    public String toString()
    {
        return "/************************   "+content+"   ************************/";
    }
}
