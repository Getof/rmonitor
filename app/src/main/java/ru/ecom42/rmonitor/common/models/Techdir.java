package ru.ecom42.rmonitor.common.models;

import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Techdir {

    public long id;
    @Expose
    @SerializedName("mobile_number")
    public long mobileNumber;

    public String status;

    public static Techdir fromJson(String json) {
        return (new GsonBuilder()).create().fromJson(json, Techdir.class);
    }

    public static String toJson(Techdir rider) {
        return (new GsonBuilder().excludeFieldsWithoutExposeAnnotation()).create().toJson(rider);
    }




}
