package com.mayurkakade.beingvaidya.data.models;

public class DoctorModel {
    String name,degree,phone_no,email,qualification,university,pincode,profile_url,bio,awards_and_honors,awards_and_honors_link,publication,publication_link,presentation,presentation_link,course,course_link;

    int current_plan_patients = 5, total_patients = 5;
    public DoctorModel() {
    }

    public DoctorModel(String name, String degree, String phone_no, String email, String qualification, String university, String pincode, String profile_url ,int  current_plan_patients , int total_patients) {
        this.name = name;
        this.degree = degree;
        this.phone_no = phone_no;
        this.email = email;
        this.qualification = qualification;
        this.university = university;
        this.pincode = pincode;
        this.profile_url = profile_url;
        this.current_plan_patients = current_plan_patients;
        this.total_patients = total_patients;
    }

    public DoctorModel(String name, String degree, String phone_no, String email, String qualification, String university, String pincode, String profile_url, String bio, String awards_and_honors, String awards_and_honors_link, String publication, String publication_link, String presentation, String presentation_link, String course, String course_link,int  current_plan_patients , int total_patients) {
        this.name = name;
        this.degree = degree;
        this.phone_no = phone_no;
        this.email = email;
        this.qualification = qualification;
        this.university = university;
        this.pincode = pincode;
        this.profile_url = profile_url;
        this.bio = bio;
        this.awards_and_honors = awards_and_honors;
        this.awards_and_honors_link = awards_and_honors_link;
        this.publication = publication;
        this.publication_link = publication_link;
        this.presentation = presentation;
        this.presentation_link = presentation_link;
        this.course = course;
        this.course_link = course_link;
        this.current_plan_patients = current_plan_patients;
        this.total_patients = total_patients;
    }

    public int getCurrent_plan_patients() {
        return current_plan_patients;
    }

    public void setCurrent_plan_patients(int current_plan_patients) {
        this.current_plan_patients = current_plan_patients;
    }

    public int getTotal_patients() {
        return total_patients;
    }

    public void setTotal_patients(int total_patients) {
        this.total_patients = total_patients;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public String getAwards_and_honors() {
        return awards_and_honors;
    }

    public void setAwards_and_honors(String awards_and_honors) {
        this.awards_and_honors = awards_and_honors;
    }

    public String getAwards_and_honors_link() {
        return awards_and_honors_link;
    }

    public void setAwards_and_honors_link(String awards_and_honors_link) {
        this.awards_and_honors_link = awards_and_honors_link;
    }

    public String getPublication() {
        return publication;
    }

    public void setPublication(String publication) {
        this.publication = publication;
    }

    public String getPublication_link() {
        return publication_link;
    }

    public void setPublication_link(String publication_link) {
        this.publication_link = publication_link;
    }

    public String getPresentation() {
        return presentation;
    }

    public void setPresentation(String presentation) {
        this.presentation = presentation;
    }

    public String getPresentation_link() {
        return presentation_link;
    }

    public void setPresentation_link(String presentation_link) {
        this.presentation_link = presentation_link;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getCourse_link() {
        return course_link;
    }

    public void setCourse_link(String course_link) {
        this.course_link = course_link;
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

    public String getDegree() {
        return degree;
    }

    public void setDegree(String degree) {
        this.degree = degree;
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

    public String getQualification() {
        return qualification;
    }

    public void setQualification(String qualification) {
        this.qualification = qualification;
    }

    public String getUniversity() {
        return university;
    }

    public void setUniversity(String university) {
        this.university = university;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }
}
