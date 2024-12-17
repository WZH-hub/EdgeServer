package com.example.edgeserver.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
@Data
public class PoseDetectionResponse {

    private int status;
    private String error;
    private List<List<Float>> data;
    private boolean fall;

    @JsonProperty("fall")
    public boolean getFall() {
        return fall;
    }

    public void setFall(boolean fall) {
        this.fall = fall;
    }

    @JsonProperty("status")
    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @JsonProperty("error")
    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    @JsonProperty("data")
    public List<List<Float>> getData() {
        return data;
    }

    public void setData(List<List<Float>> data) {
        this.data = data;
    }
}

