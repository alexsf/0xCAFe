package com.hpe.hackathon.runner;

import java.util.*;

public class ProductFeatureResponse {
    private String name;
    private String feature;
    private Integer size;
    private List<OpinionResponse> data = null;
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getFeature() {
        return feature;
    }
    public void setFeature(String feature) {
        this.feature = feature;
    }
    public Integer getSize() {
        return size;
    }
    public void setSize(Integer size) {
        this.size = size;
    }
    public List<OpinionResponse> getData() {
        return data;
    }
    public void setData(List<OpinionResponse> data) {
        this.data = data;
    }
}
