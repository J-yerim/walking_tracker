package com.example.b2001140_dbmap;

public class ListViewItem {
    private Integer no;
    private  String date;
    private double log;
    private double lat;
    private String time;

    public Integer getNo() {
        return no;
    }

    public String getDate(){
        return date;
    }
    public double getLog() {
        return log;
    }

    public double getLat() {
        return lat;
    }

    public String getTime() {
        return time;
    }

    public void setNo(Integer no) {
        this.no = no;

    }

    public void setDate(String date){
        this.date=date;
    }
    public void setLog(double log) {
        this.log = log;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
