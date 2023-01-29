package com.unipi.chrispana.smartalert;

public class AlertClass {
    private String id, event, comments, location, timestamp, photo;
    private int count = 1;

    public AlertClass(){}

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public AlertClass(String id,String event, String comments, String location, String timestamp, String photo) {
        this.id = id;
        this.event = event;
        this.comments = comments;
        this.location = location;
        this.timestamp = timestamp;
        this.photo = photo;
        this.count = 1;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
