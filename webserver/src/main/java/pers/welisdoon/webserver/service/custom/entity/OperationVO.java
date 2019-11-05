package pers.welisdoon.webserver.service.custom.entity;

import java.sql.Timestamp;

public class OperationVO {
    private Integer operationId;
    private Integer orderId;
    private Integer tacheId;
    private Timestamp startTime;
    private Timestamp finishTime;
    private Boolean active;
    private String oprMan;
    private String info;

    public Integer getOperationId() {
        return operationId;
    }

    public OperationVO setOperationId(Integer operationId) {
        this.operationId = operationId;
        return this;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public OperationVO setOrderId(Integer orderId) {
        this.orderId = orderId;
        return this;
    }

    public Integer getTacheId() {
        return tacheId;
    }

    public OperationVO setTacheId(Integer tacheId) {
        this.tacheId = tacheId;
        return this;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public OperationVO setStartTime(Timestamp startTime) {
        this.startTime = startTime;
        return this;
    }

    public Timestamp getFinishTime() {
        return finishTime;
    }

    public OperationVO setFinishTime(Timestamp finishTime) {
        this.finishTime = finishTime;
        return this;
    }

    public Boolean getActive() {
        return active;
    }

    public OperationVO setActive(Boolean active) {
        this.active = active;
        return this;
    }

    public String getOprMan() {
        return oprMan;
    }

    public OperationVO setOprMan(String oprMan) {
        this.oprMan = oprMan;
        return this;
    }

    public String getInfo() {
        return info;
    }

    public OperationVO setInfo(String info) {
        this.info = info;
        return this;
    }
}
