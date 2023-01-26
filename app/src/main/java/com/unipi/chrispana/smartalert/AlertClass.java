package com.unipi.chrispana.smartalert;

public class AlertClass {
    String id, event, comments, location, timestamp, photo;

    public AlertClass(){}
    public AlertClass(String event, String comments, String location, String timestamp, String photo) {
        this.event = event;
        this.comments = comments;
        this.location = location;
        this.timestamp = timestamp;
        this.photo = photo;
    }


    public void setEvent(String event) {
        this.event = event;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public String getEvent() {
        return event;
    }

    public String getComments() {
        return comments;
    }

    public String getLocation() {
        return location;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getPhoto() {
        return photo;
    }
}
