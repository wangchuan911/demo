package pers.welisdoon.webserver.service.custom.entity;

import java.sql.Timestamp;

public class OrderVO {
    private Integer orderId;
    private String orderCode;
    private String carLicenseNumber;
    private String carAddress;
    private Timestamp createDate;
    private Timestamp finishDate;
    private Integer orderState;
    private String orderControlPerson;
    private String orderAppointPerson;
    private Timestamp orderArrangeDate;
    private String custId;
    private String serviceId;
    private String orderNote;
    private Integer tacheId;
    private Double posX;
    private Double posY;

    public Integer getOrderId() {
        return orderId;
    }

    public OrderVO setOrderId(Integer orderId) {
        this.orderId = orderId;
        return this;
    }

    public String getOrderCode() {
        return orderCode;
    }

    public OrderVO setOrderCode(String orderCode) {
        this.orderCode = orderCode;
        return this;
    }

    public String getCarLicenseNumber() {
        return carLicenseNumber;
    }

    public OrderVO setCarLicenseNumber(String carLicenseNumber) {
        this.carLicenseNumber = carLicenseNumber;
        return this;
    }

    public String getCarAddress() {
        return carAddress;
    }

    public OrderVO setCarAddress(String carAddress) {
        this.carAddress = carAddress;
        return this;
    }

    public Timestamp getCreateDate() {
        return createDate;
    }

    public OrderVO setCreateDate(Timestamp createDate) {
        this.createDate = createDate;
        return this;
    }

    public Timestamp getFinishDate() {
        return finishDate;
    }

    public OrderVO setFinishDate(Timestamp finishDate) {
        this.finishDate = finishDate;
        return this;
    }

    public Integer getOrderState() {
        return orderState;
    }

    public OrderVO setOrderState(Integer orderState) {
        this.orderState = orderState;
        return this;
    }

    public String getOrderControlPerson() {
        return orderControlPerson;
    }

    public OrderVO setOrderControlPerson(String orderControlPerson) {
        this.orderControlPerson = orderControlPerson;
        return this;
    }

    public String getOrderAppointPerson() {
        return orderAppointPerson;
    }

    public OrderVO setOrderAppointPerson(String orderAppointPerson) {
        this.orderAppointPerson = orderAppointPerson;
        return this;
    }

    public Timestamp getOrderArrangeDate() {
        return orderArrangeDate;
    }

    public OrderVO setOrderArrangeDate(Timestamp orderArrangeDate) {
        this.orderArrangeDate = orderArrangeDate;
        return this;
    }

    public String getCustId() {
        return custId;
    }

    public OrderVO setCustId(String custId) {
        this.custId = custId;
        return this;
    }

    public String getServiceId() {
        return serviceId;
    }

    public OrderVO setServiceId(String serviceId) {
        this.serviceId = serviceId;
        return this;
    }

    public Double getPosX() {
        return posX;
    }

    public OrderVO setPosX(Double posX) {
        this.posX = posX;
        return this;
    }

    public Double getPosY() {
        return posY;
    }

    public OrderVO setPosY(Double posY) {
        this.posY = posY;
        return this;
    }

    public String getOrderNote() {
        return orderNote;
    }

    public OrderVO setOrderNote(String orderNote) {
        this.orderNote = orderNote;
        return this;
    }

    public Integer getTacheId() {
        return tacheId;
    }

    public OrderVO setTacheId(Integer tacheId) {
        this.tacheId = tacheId;
        return this;
    }
}

