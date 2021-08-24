package com.mayurkakade.beingvaidya.data.models;

public class SubscriptionModel {
    long price;
    String numberOfPatients;
    String subscriptionId;
    String subscriptionPeriod;

    public SubscriptionModel(long price, String numberOfPatients, String subscriptionId, String subscriptionPeriod) {
        this.price = price;
        this.numberOfPatients = numberOfPatients;
        this.subscriptionId = subscriptionId;
        this.subscriptionPeriod = subscriptionPeriod;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    public String getNumberOfPatients() {
        return numberOfPatients;
    }

    public void setNumberOfPatients(String numberOfPatients) {
        this.numberOfPatients = numberOfPatients;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public String getSubscriptionPeriod() {
        return subscriptionPeriod;
    }

    public void setSubscriptionPeriod(String subscriptionPeriod) {
        this.subscriptionPeriod = subscriptionPeriod;
    }
}
