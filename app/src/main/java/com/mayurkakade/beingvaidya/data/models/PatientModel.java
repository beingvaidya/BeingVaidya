package com.mayurkakade.beingvaidya.data.models;

public class PatientModel {
    String name = "",phone_no,email,address,doctor_unique_id,profile_url;
    int age;
    boolean isLocked = false;

    public PatientModel() {
    }

    public PatientModel(String name, String phone_no, String email, String address, String doctor_unique_id, int age) {
        this.name = name;
        this.phone_no = phone_no;
        this.email = email;
        this.address = address;
        this.doctor_unique_id = doctor_unique_id;
        this.age = age;
    }

    public PatientModel(String name, String phone_no, String email, String address, String doctor_unique_id, String profile_url, int age) {
        this.name = name;
        this.phone_no = phone_no;
        this.email = email;
        this.address = address;
        this.doctor_unique_id = doctor_unique_id;
        this.profile_url = profile_url;
        this.age = age;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }

    public String getProfile_url() {
        return profile_url;
    }

    public void setProfile_url(String profile_url) {
        this.profile_url = profile_url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone_no() {
        return phone_no;
    }

    public void setPhone_no(String phone_no) {
        this.phone_no = phone_no;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDoctor_unique_id() {
        return doctor_unique_id;
    }

    public void setDoctor_unique_id(String doctor_unique_id) {
        this.doctor_unique_id = doctor_unique_id;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

}
