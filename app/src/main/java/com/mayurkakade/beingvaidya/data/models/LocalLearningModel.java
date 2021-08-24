package com.mayurkakade.beingvaidya.data.models;

public class LocalLearningModel {
    LearningModel learningModel;
    boolean isStarred;

    public LocalLearningModel(LearningModel learningModel, boolean isStarred) {
        this.learningModel = learningModel;
        this.isStarred = isStarred;
    }

    public LocalLearningModel() {
    }

    public LearningModel getLearningModel() {
        return learningModel;
    }

    public void setLearningModel(LearningModel learningModel) {
        this.learningModel = learningModel;
    }

    public boolean isStarred() {
        return isStarred;
    }

    public void setStarred(boolean starred) {
        isStarred = starred;
    }
}