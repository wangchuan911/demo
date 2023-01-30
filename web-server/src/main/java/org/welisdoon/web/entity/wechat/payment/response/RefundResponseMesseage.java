package org.welisdoon.web.entity.wechat.payment.response;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.StringJoiner;

/**
 * @Classname RefundResponseMesseage
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/11/8 21:26
 */

@XmlRootElement(name = "xml")
@XmlAccessorType(XmlAccessType.FIELD)
public class RefundResponseMesseage {
    //返回状态码	return_code	是	String(16)	SUCCESS
    //SUCCESS/FAIL
    //
    @XmlElement(name = "return_code")
    String returnCode;
    //此字段是通信标识，表示接口层的请求结果，并非退款状态。
    //
    //返回信息	return_msg	是	String(128)	OK
    //当return_code为FAIL时返回信息为错误原因 ，例如
    //
    //签名失败
    //
    //参数格式校验错误
    @XmlElement(name = "return_msg")
    String returnMsg;

    //业务结果	result_code	是	String(16)	SUCCESS
    //SUCCESS/FAIL
    //
    //SUCCESS退款申请接收成功，结果通过退款查询接口查询
    //
    //FAIL 提交业务失败
    //
    //错误代码	err_code	否	String(32)	SYSTEMERROR	列表详见错误码列表
    @XmlElement(name = "err_code")
    String errCode;
    //错误代码描述	err_code_des	否	String(128)	系统超时	结果信息描述
    @XmlElement(name = "err_code_des")
    String errCodeDes;
    //公众账号ID	appid	是	String(32)	wx8888888888888888	微信分配的公众账号ID
    @XmlElement(name = "appid")
    String appId;
    //商户号	mch_id	是	String(32)	1900000109	微信支付分配的商户号
    @XmlElement(name = "mch_id")
    String mchId;
    //随机字符串	nonce_str	是	String(32)	5K8264ILTKCH16CQ2502SI8ZNMTM67VS	随机字符串，不长于32位
    @XmlElement(name = "nonce_str")
    String nonceStr;
    //签名	sign	是	String(32)	5K8264ILTKCH16CQ2502SI8ZNMTM67VS	签名，详见签名算法
    @XmlElement(name = "sign")
    String sign;
    //微信支付订单号	transaction_id	是	String(32)	4007752501201407033233368018	微信订单号
    @XmlElement(name = "transaction_id")
    String transactionId;
    //商户订单号	out_trade_no	是	String(32)	33368018	商户系统内部订单号，要求32个字符内（最少6个字符），只能是数字、大小写字母_-|*且在同一个商户号下唯一。详见商户订单号

    @XmlElement(name = "outTradeNo")
    String outTradeNo;
    //商户退款单号	out_refund_no	是	String(64)	121775250	商户系统内部的退款单号，商户系统内部唯一，只能是数字、大小写字母_-|*@ ，同一退款单号多次请求只退一笔。

    @XmlElement(name = "out_refund_no")
    String outRefundNo;
    //微信退款单号	refund_id	是	String(32)	2007752501201407033233368018	微信退款单号

    @XmlElement(name = "refund_id")
    String refundId;
    //退款金额	refund_fee	是	int	100	退款总金额,单位为分,可以做部分退款

    @XmlElement(name = "refund_fee")
    Integer refundFee;
    //应结退款金额	settlement_refund_fee	否	int	100	去掉非充值代金券退款金额后的退款金额，退款金额=申请退款金额-非充值代金券退款金额，退款金额<=申请退款金额

    @XmlElement(name = "settlement_refund_fee")
    String settlementRefundFee;
    //标价金额	total_fee	是	int	100	订单总金额，单位为分，只能为整数，详见支付金额

    @XmlElement(name = "total_fee")
    Integer totalFee;
    //应结订单金额	settlement_total_fee	否	int	100	去掉非充值代金券金额后的订单总金额，应结订单金额=订单金额-非充值代金券金额，应结订单金额<=订单金额。

    @XmlElement(name = "settlement_total_fee")
    Integer settlementTotalFee;
    //标价币种	fee_type	否	String(8)	CNY	订单金额货币类型，符合ISO 4217标准的三位字母代码，默认人民币：CNY，其他值列表详见货币类型

    @XmlElement(name = "fee_type")
    String feeType;
    //现金支付金额	cash_fee	是	int	100	现金支付金额，单位为分，只能为整数，详见支付金额

    @XmlElement(name = "cash_fee")
    Integer cashFee;
    //现金支付币种	cash_fee_type	否	String(16)	CNY	货币类型，符合ISO 4217标准的三位字母代码，默认人民币：CNY，其他值列表详见货币类型

    @XmlElement(name = "cash_fee_type")
    String cashFeeType;
    //现金退款金额	cash_refund_fee	否	int	100	现金退款金额，单位为分，只能为整数，详见支付金额

    @XmlElement(name = "cash_refund_fee")
    Integer cashRefundFee;
    //代金券类型	coupon_type_$n	否	String(8)	CASH
    //CASH--充值代金券
    //NO_CASH---非充值代金券
    //
    //订单使用代金券时有返回（取值：CASH、NO_CASH）。$n为下标,从0开始编号，举例：coupon_type_0
    //
    //代金券退款总金额	coupon_refund_fee	否	int	100	代金券退款金额<=退款金额，退款金额-代金券或立减优惠退款金额为现金，说明详见代金券或立减优惠

    @XmlElement(name = "coupon_refund_fee")
    Integer couponRefundFee;
    //单个代金券退款金额	coupon_refund_fee_$n	否	int	100	代金券退款金额<=退款金额，退款金额-代金券或立减优惠退款金额为现金，说明详见代金券或立减优惠
    //退款代金券使用数量	coupon_refund_count	否	int	1	退款代金券使用数量

    @XmlElement(name = "coupon_refund_count")
    Integer couponRefundCount;
    //退款代金券ID	coupon_refund_id_$n	否	String(20)	10000 	退款代金券ID, $n为下标，从0开始编号


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

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getMchId() {
        return mchId;
    }

    public void setMchId(String mchId) {
        this.mchId = mchId;
    }

    public String getNonceStr() {
        return nonceStr;
    }

    public void setNonceStr(String nonceStr) {
        this.nonceStr = nonceStr;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getOutTradeNo() {
        return outTradeNo;
    }

    public void setOutTradeNo(String outTradeNo) {
        this.outTradeNo = outTradeNo;
    }

    public String getOutRefundNo() {
        return outRefundNo;
    }

    public void setOutRefundNo(String outRefundNo) {
        this.outRefundNo = outRefundNo;
    }

    public String getRefundId() {
        return refundId;
    }

    public void setRefundId(String refundId) {
        this.refundId = refundId;
    }

    public Integer getRefundFee() {
        return refundFee;
    }

    public void setRefundFee(Integer refundFee) {
        this.refundFee = refundFee;
    }

    public String getSettlementRefundFee() {
        return settlementRefundFee;
    }

    public void setSettlementRefundFee(String settlementRefundFee) {
        this.settlementRefundFee = settlementRefundFee;
    }

    public Integer getTotalFee() {
        return totalFee;
    }

    public void setTotalFee(Integer totalFee) {
        this.totalFee = totalFee;
    }

    public Integer getSettlementTotalFee() {
        return settlementTotalFee;
    }

    public void setSettlementTotalFee(Integer settlementTotalFee) {
        this.settlementTotalFee = settlementTotalFee;
    }

    public String getFeeType() {
        return feeType;
    }

    public void setFeeType(String feeType) {
        this.feeType = feeType;
    }

    public Integer getCashFee() {
        return cashFee;
    }

    public void setCashFee(Integer cashFee) {
        this.cashFee = cashFee;
    }

    public String getCashFeeType() {
        return cashFeeType;
    }

    public void setCashFeeType(String cashFeeType) {
        this.cashFeeType = cashFeeType;
    }

    public Integer getCashRefundFee() {
        return cashRefundFee;
    }

    public void setCashRefundFee(Integer cashRefundFee) {
        this.cashRefundFee = cashRefundFee;
    }

    public Integer getCouponRefundFee() {
        return couponRefundFee;
    }

    public void setCouponRefundFee(Integer couponRefundFee) {
        this.couponRefundFee = couponRefundFee;
    }

    public Integer getCouponRefundCount() {
        return couponRefundCount;
    }

    public void setCouponRefundCount(Integer couponRefundCount) {
        this.couponRefundCount = couponRefundCount;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", RefundResponseMesseage.class.getSimpleName() + "[", "]")
                .add("returnCode='" + returnCode + "'")
                .add("returnMsg='" + returnMsg + "'")
                .add("errCode='" + errCode + "'")
                .add("errCodeDes='" + errCodeDes + "'")
                .add("appId='" + appId + "'")
                .add("mchId='" + mchId + "'")
                .add("nonceStr='" + nonceStr + "'")
                .add("sign='" + sign + "'")
                .add("transactionId='" + transactionId + "'")
                .add("outTradeNo='" + outTradeNo + "'")
                .add("outRefundNo='" + outRefundNo + "'")
                .add("refundId='" + refundId + "'")
                .add("refundFee=" + refundFee)
                .add("settlementRefundFee='" + settlementRefundFee + "'")
                .add("totalFee=" + totalFee)
                .add("settlementTotalFee=" + settlementTotalFee)
                .add("feeType='" + feeType + "'")
                .add("cashFee=" + cashFee)
                .add("cashFeeType='" + cashFeeType + "'")
                .add("cashRefundFee=" + cashRefundFee)
                .add("couponRefundFee=" + couponRefundFee)
                .add("couponRefundCount=" + couponRefundCount)
                .toString();
    }
}
