package com.hpe.hackathon.runner;

public class OpinionResponse {
    private String text;
    private Integer size;
    public String getText() {
        return text;
    }
    public void setText(String text) {
        this.text = text;
    }
    public Integer getSize() {
        return size;
    }
    public void setSize(Integer size) {
        this.size = size;
    }
}