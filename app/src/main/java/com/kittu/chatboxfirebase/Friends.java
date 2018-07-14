package com.kittu.chatboxfirebase;

public class Friends {
 public String date;
 public String uid;
 public String name;
 public String image;
public String online;

    public Friends(String date, String uid, String name, String image, String online) {
        this.date = date;
        this.uid = uid;
        this.name = name;
        this.image = image;
        this.online = online;
    }

    public String getOnline() {
        return online;
    }

    public void setOnline(String online) {
        this.online = online;
    }

    public Friends(String date, String uid, String name, String image) {
        this.date = date;
        this.uid = uid;
        this.name = name;
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }




    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Friends(String date,String uid) {
        this.date = date;
        this.uid = uid;
    }
    public Friends(){

    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
