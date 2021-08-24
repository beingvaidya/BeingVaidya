package com.mayurkakade.beingvaidya.data.models;

import androidx.annotation.NonNull;

public class DocId {
    public String DocId;

        public <T extends DocId> T withId (@NonNull final String Id)
        {
            this.DocId = Id;
            return (T) this;
        }

}
