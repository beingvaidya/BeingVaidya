package com.mayurkakade.beingvaidya.data;

import com.mayurkakade.beingvaidya.data.models.PatientsCommunityImageModel;

public interface OnQueryDataListener {
    void onSuccess();
    void onSuccessPatientsCommunity(PatientsCommunityImageModel patientsCommunityImageModel);
    void onStart();
    void onFailure(String exception);
}
