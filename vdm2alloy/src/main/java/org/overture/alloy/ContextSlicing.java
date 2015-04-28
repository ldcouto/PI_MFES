package org.overture.alloy;

/**
 * Created by macbookpro on 27/04/15.
 */
public class ContextSlicing {

    private boolean isNotAllowed;//true if is allowed type, else false

    private boolean isRecord; // through this boolean ,we know if the map is a record or atrib.




    public boolean isAllowed() {
        return isNotAllowed;
    }

    public void init() {
        this.isNotAllowed=true;
        this.isRecord=false;
    }



    public boolean isRecord() {
        return isRecord;
    }

    public void setRecord(boolean isRecord) {
        this.isRecord = isRecord;
    }

    public void setNotAllowed(boolean isNotAllowed) {

        this.isNotAllowed = isNotAllowed;
    }




}

