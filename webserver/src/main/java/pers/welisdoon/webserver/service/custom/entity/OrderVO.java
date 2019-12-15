package pers.welisdoon.webserver.service.custom.entity;

import java.sql.Timestamp;
import java.util.List;

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
    private String orderAppointPersonName;
    private String orderAppointPhone;
    private Timestamp orderArrangeDate;
    private String custId;
    private String custName;
    private String custPhone;
    private String passTache;
    private String orderNote;
    private Integer tacheId;
    private Double posX;
    private Double posY;
    private List<PictureVO> pictureVOS;
    private CarVO carVO;

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

    public String getPassTache() {
        return passTache;
    }

    public OrderVO setPassTache(String passTache) {
        this.passTache = passTache;
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

    public List<PictureVO> getPictureVOS() {
        return pictureVOS;
    }

    public OrderVO setPictureVOS(List<PictureVO> pictureVOS) {
        this.pictureVOS = pictureVOS;
        return this;
    }

    public CarVO getCarVO() {
        return carVO;
    }

    public OrderVO setCarVO(CarVO carVO) {
        this.carVO = carVO;
        return this;
    }

    public String getOrderAppointPersonName() {
        return orderAppointPersonName;
    }

    public OrderVO setOrderAppointPersonName(String orderAppointPersonName) {
        this.orderAppointPersonName = orderAppointPersonName;
        return this;
    }

    public String getCustName() {
        return custName;
    }

    public OrderVO setCustName(String custName) {
        this.custName = custName;
        return this;
    }

    public String getCustPhone() {
        return custPhone;
    }

    public OrderVO setCustPhone(String custPhone) {
        this.custPhone = custPhone;
        return this;
    }

    public String getOrderAppointPhone() {
        return orderAppointPhone;
    }

    public OrderVO setOrderAppointPhone(String orderAppointPhone) {
        this.orderAppointPhone = orderAppointPhone;
        return this;
    }
}

