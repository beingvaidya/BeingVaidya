package com.mayurkakade.beingvaidya.data.models;

public class LocalStoreModel {
    StoreModel storeModel;
    boolean isWishlisted;

    public LocalStoreModel() {
    }

    public LocalStoreModel(StoreModel storeModel, boolean isWishlisted) {
        this.storeModel = storeModel;
        this.isWishlisted = isWishlisted;
    }

    public StoreModel getStoreModel() {
        return storeModel;
    }

    public void setStoreModel(StoreModel storeModel) {
        this.storeModel = storeModel;
    }

    public boolean isWishlisted() {
        return isWishlisted;
    }

    public void setWishlisted(boolean wishlisted) {
        isWishlisted = wishlisted;
    }
}
