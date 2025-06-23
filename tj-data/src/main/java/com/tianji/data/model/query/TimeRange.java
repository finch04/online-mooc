package com.tianji.data.model.query;

public class TimeRange {
    private String begin;
    private String end;

    public TimeRange(String begin, String end) {
        this.begin = begin;
        this.end = end;
    }

    // getters and setters
    public String getBegin() {
        return begin;
    }

    public void setBegin(String begin) {
        this.begin = begin;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }
}