package pers.welisdoon.webserver.service.custom.entity;

public class CarVO {
    private String userId;
    private String brand;
    private Integer lisence;
    private String color;
    private String modal;

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

    public Integer getLisence() {
        return lisence;
    }

    public CarVO setLisence(Integer lisence) {
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
}
