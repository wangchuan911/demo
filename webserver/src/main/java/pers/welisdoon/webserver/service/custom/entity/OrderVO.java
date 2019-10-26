package pers.welisdoon.webserver.service.custom.entity;

import java.sql.Timestamp;

public class OrderVO {
    private String orderId;
    private String orderCode;
    private String carLicenseNumber;
    private String carAddress;
    private Timestamp createDate;
    private Timestamp finishDate;
    private Integer orderState;
    private Integer orderControlPerson;
    private Integer orderAppointPerson;
    private Timestamp orderArrangeDate;
    private String custId;
    private String serviceId;
    private String orderNote;
    private Integer tacheId;
    private double posX;
    private double posY;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public void setOrderCode(String orderCode) {
        this.orderCode = orderCode;
    }

    public String getCarLicenseNumber() {
        return carLicenseNumber;
    }

    public void setCarLicenseNumber(String carLicenseNumber) {
        this.carLicenseNumber = carLicenseNumber;
    }

    public String getCarAddress() {
        return carAddress;
    }

    public void setCarAddress(String carAddress) {
        this.carAddress = carAddress;
    }

    public Timestamp getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Timestamp createDate) {
        this.createDate = createDate;
    }

    public Timestamp getFinishDate() {
        return finishDate;
    }

    public void setFinishDate(Timestamp finishDate) {
        this.finishDate = finishDate;
    }

    public Integer getOrderState() {
        return orderState;
    }

    public void setOrderState(Integer orderState) {
        this.orderState = orderState;
    }

    public Integer getOrderControlPerson() {
        return orderControlPerson;
    }

    public void setOrderControlPerson(Integer orderControlPerson) {
        this.orderControlPerson = orderControlPerson;
    }

    public Integer getOrderAppointPerson() {
        return orderAppointPerson;
    }

    public void setOrderAppointPerson(Integer orderAppointPerson) {
        this.orderAppointPerson = orderAppointPerson;
    }

    public Timestamp getOrderArrangeDate() {
        return orderArrangeDate;
    }

    public void setOrderArrangeDate(Timestamp orderArrangeDate) {
        this.orderArrangeDate = orderArrangeDate;
    }

    public String getCustId() {
        return custId;
    }

    public void setCustId(String custId) {
        this.custId = custId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public double getPosX() {
        return posX;
    }

    public void setPosX(double posX) {
        this.posX = posX;
    }

    public double getPosY() {
        return posY;
    }

    public void setPosY(double posY) {
        this.posY = posY;
    }

    public String getOrderNote() {
        return orderNote;
    }

    public void setOrderNote(String orderNote) {
        this.orderNote = orderNote;
    }

    public Integer getTacheId() {
        return tacheId;
    }

    public void setTacheId(Integer tacheId) {
        this.tacheId = tacheId;
    }
}

