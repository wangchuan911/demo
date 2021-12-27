package org.welisdoon.web.entity.wechat.payment.response;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @Classname TranferToPersionRequsetMesseage
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/12/27 18:59
 */
@XmlRootElement(name = "xml")
@XmlAccessorType(XmlAccessType.FIELD)
public class MarketTransferResponseMesseage {
    /**
     * 字段名	变量名	必填	示例值	类型	描述
     * 返回状态码	return_code	是	SUCCESS	String(16)	SUCCESS/FAIL
     * 此字段是通信标识，非交易标识，交易是否成功需要查看result_code来判断
     * 返回信息	return_msg	否	签名失败	String(128)	返回信息，如非空，为错误原因
     * 签名失败
     * 参数格式校验错误
     * 以下字段在return_code为SUCCESS的时候有返回
     * <p>
     * 字段名	变量名	必填	示例值	类型	描述
     * 商户appid	mch_appid	是	wx8888888888888888	String(128)	申请商户号的appid或商户号绑定的appid（号corpid即为此appId）
     * 商户号	mchid	是	1900000109	String(32)	微信支付分配的商户号
     * 设备号	device_info	否	013467007045764	String(32)	微信支付分配的终端设备号，
     * 随机字符串	nonce_str	是	5K8264ILTKCH16CQ2502SI8ZNMTM67VS	String(32)	随机字符串，不长于32位
     * 业务结果	result_code	是	SUCCESS	String(16)	SUCCESS/FAIL，注意：当状态为FAIL时，存在业务结果未明确的情况。如果状态为FAIL，请务必关注错误代码（err_code字段），通过查询接口确认此次付款的结果。
     * 错误代码	err_code	否	SYSTEMERROR	String(32)	错误码信息，注意：出现未明确的错误码时（SYSTEMERROR等），请务必用原商户订单号重试，或通过查询接口确认此次付款的结果。
     * 错误代码描述	err_code_des	否	系统错误	String(128)	结果信息描述
     * 以下字段在return_code 和result_code都为SUCCESS的时候有返回
     * <p>
     * 字段名	变量名	必填	示例值	类型	描述
     * 商户订单号	partner_trade_no	是	1217752501201407033233368018	String(32)	商户订单号，需保持历史全局唯一性(只能是字母或者数字，不能包含有其它字符)
     * 微信付款单号	payment_no	是	1007752501201407033233368018	String(64)	付款成功，返回的微信付款单号
     * 付款成功时间	payment_time	是	2015-05-19 15：26：59	String(32)	付款成功时间
     */
    @XmlElement(name = "return_code")
    String returnCode;
    @XmlElement(name = "return_msg")
    String returnMsg;
    @XmlElement(name = "mch_appid")
    String mchAppid;
    @XmlElement(name = "mchid")
    String mchid;
    @XmlElement(name = "device_info")
    String deviceInfo;
    @XmlElement(name = "nonce_str")
    String nonceStr;

    @XmlElement(name = "result_code")
    String resultCode;
    @XmlElement(name = "err_code")
    String errCode;
    @XmlElement(name = "err_code_des")
    String errCodeDes;
    @XmlElement(name = "partner_trade_no")
    String partnerTradeNo;
    @XmlElement(name = "payment_no")
    String paymentNo;
    @XmlElement(name = "payment_time")
    String paymentTime;

    public String getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(String returnCode) {
        this.returnCode = returnCode;
    }

    public String getReturnMsg() {
        return returnMsg;
    }

    public void setReturnMsg(String returnMsg) {
        this.returnMsg = returnMsg;
    }

    public String getMchAppid() {
        return mchAppid;
    }

    public void setMchAppid(String mchAppid) {
        this.mchAppid = mchAppid;
    }

    public String getMchid() {
        return mchid;
    }

    public void setMchid(String mchid) {
        this.mchid = mchid;
    }

    public String getDeviceInfo() {
        return deviceInfo;
    }

    public void setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
    }

    public String getNonceStr() {
        return nonceStr;
    }

    public void setNonceStr(String nonceStr) {
        this.nonceStr = nonceStr;
    }

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public String getErrCode() {
        return errCode;
    }

    public void setErrCode(String errCode) {
        this.errCode = errCode;
    }

    public String getErrCodeDes() {
        return errCodeDes;
    }

    public void setErrCodeDes(String errCodeDes) {
        this.errCodeDes = errCodeDes;
    }

    public String getPartnerTradeNo() {
        return partnerTradeNo;
    }

    public void setPartnerTradeNo(String partnerTradeNo) {
        this.partnerTradeNo = partnerTradeNo;
    }

    public String getPaymentNo() {
        return paymentNo;
    }

    public void setPaymentNo(String paymentNo) {
        this.paymentNo = paymentNo;
    }

    public String getPaymentTime() {
        return paymentTime;
    }

    public void setPaymentTime(String paymentTime) {
        this.paymentTime = paymentTime;
    }
}
