package org.welisdoon.webserver.service.custom.entity;

public class CarVO {
    private String userId;
    private String brand;
    private String lisence;
    private String color;
    private String modal;
    private byte[] picture;
    private Integer defaultSelected;

    public String getUserId() {
        return userId;
    }

    public CarVO setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public String getBrand() {
        return brand;
    }

    public CarVO setBrand(String brand) {
        this.brand = brand;
        return this;
    }

    public String getLisence() {
        return lisence;
    }

    public CarVO setLisence(String lisence) {
        this.lisence = lisence;
        return this;
    }

    public String getColor() {
        return color;
    }

    public CarVO setColor(String color) {
        this.color = color;
        return this;
    }

    public String getModal() {
        return modal;
    }

    public CarVO setModal(String modal) {
        this.modal = modal;
        return this;
    }

    public byte[] getPicture() {
        return picture;
    }

    public CarVO setPicture(byte[] picture) {
        this.picture = picture;
        return this;
    }

    public Integer getDefaultSelected() {
        return defaultSelected;
    }

    public CarVO setDefaultSelected(Integer defaultSelected) {
        this.defaultSelected = defaultSelected;
        return this;
    }
}
