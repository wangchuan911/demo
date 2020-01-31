package org.welisdoon.webserver.entity.wechat.payment.requset;

import com.sun.istack.NotNull;
import org.apache.commons.codec.digest.Md5Crypt;
import org.reflections.ReflectionUtils;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@XmlRootElement(name = "xml")
@XmlAccessorType(XmlAccessType.FIELD)
public class PayBillRequsetMesseage {
    final static String DATE_FORMAT = "yyyyMMddHHmmss";
    /**
     * 字段名	变量名	必填	类型	示例值	描述
     * 小程序ID	appid	是	String(32)	wxd678efh567hg6787	微信分配的小程序ID
     */
    @XmlElement(name = "appid")
    String appId;
    /**
     * 字段名	变量名	必填	类型	示例值	描述
     * 商户号	mch_id	是	String(32)	1230000109	微信支付分配的商户号
     */
    @XmlElement(name = "mch_id")
    String mchId;
    /**
     * 字段名	变量名	必填	类型	示例值	描述
     * 设备号	device_info	否	String(32)	013467007045764	自定义参数，可以为终端设备号(门店号或收银设备ID)，PC网页或公众号内支付可以传"WEB"
     */
    @XmlElement(name = "device_info")
    String deviceInfo;
    /**
     * 字段名	变量名	必填	类型	示例值	描述
     * 随机字符串	nonce_str	是	String(32)	5K8264ILTKCH16CQ2502SI8ZNMTM67VS	随机字符串，长度要求在32位以内。推荐随机数生成算法
     */
    @XmlElement(name = "nonce_str")
    String nonceStr;
    /**
     * 字段名	变量名	必填	类型	示例值	描述
     * 签名	sign	是	String(64)	C380BEC2BFD727A4B6845133519F3AD6	通过签名算法计算得出的签名值，详见签名生成算法
     */
    @XmlElement(name = "sign")
    String sign;
    /**
     * 字段名	变量名	必填	类型	示例值	描述
     * 签名类型	sign_type	否	String(32)	MD5	签名类型，默认为MD5，支持HMAC-SHA256和MD5。
     */
    @XmlElement(name = "sign_type")
    String signType = "MD5";
    /**
     * 字段名	变量名	必填	类型	示例值	描述
     * 业务结果	result_code	是	String(16)	SUCCESS	SUCCESS/FAIL
     */
    @XmlElement(name = "result_code")
    String resultCode;
    /**
     * 字段名	变量名	必填	类型	示例值	描述
     * 错误代码	err_code	否	String(32)	SYSTEMERROR	错误返回的信息描述
     */
    @XmlElement(name = "err_code")
    String errCode;
    /**
     * 字段名	变量名	必填	类型	示例值	描述
     * 错误代码描述	err_code_des	否	String(128)	系统错误	错误返回的信息描述
     */
    @XmlElement(name = "err_code_des")
    String errCodeDes;
    /**
     * 字段名	变量名	必填	类型	示例值	描述
     * 用户标识	openid	是	String(128)	wxd930ea5d5a258f4f	用户在商户appid下的唯一标识
     */
    @XmlElement(name = "openid")
    String openId;
    /**
     * 字段名	变量名	必填	类型	示例值	描述
     * 是否关注公众账号	is_subscribe	是	String(1)	Y	用户是否关注公众账号，Y-关注，N-未关注
     */
    @XmlElement(name = "is_subscribe")
    String isSubscribe;
    /**
     * 字段名	变量名	必填	类型	示例值	描述
     * 交易类型	trade_type	是	String(16)	JSAPI	JSAPI、NATIVE、APP
     */
    @XmlElement(name = "trade_type")
    String tradeType;
    /**
     * 字段名	变量名	必填	类型	示例值	描述
     * 付款银行	bank_type	是	String(32)	CMC	银行类型，采用字符串类型的银行标识，银行类型见银行列表
     */
    @XmlElement(name = "bank_type")
    String bankType;
    /**
     * 字段名	变量名	必填	类型	示例值	描述
     * 订单金额	total_fee	是	Int	100	订单总金额，单位为分
     */
    @XmlElement(name = "total_fee")
    Integer totalFee;
    /**
     * 字段名	变量名	必填	类型	示例值	描述
     * 应结订单金额	settlement_total_fee	否	Int	100	应结订单金额=订单金额-非充值代金券金额，应结订单金额<=订单金额。
     */
    @XmlElement(name = "settlement_total_fee")
    String settlementTotalFee;
    /**
     * 字段名	变量名	必填	类型	示例值	描述
     * 货币种类	fee_type	否	String(8)	CNY	货币类型，符合ISO4217标准的三位字母代码，默认人民币：CNY，其他值列表详见货币类型
     */
    @XmlElement(name = "fee_type")
    String feeType;
    /**
     * 字段名	变量名	必填	类型	示例值	描述
     * 现金支付金额	cash_fee	是	Int	100	现金支付金额订单现金支付金额，详见支付金额
     */
    @XmlElement(name = "cash_fee")
    Integer cashFee;
    /**
     * 字段名	变量名	必填	类型	示例值	描述
     * 现金支付货币类型	cash_fee_type	否	String(16)	CNY	货币类型，符合ISO4217标准的三位字母代码，默认人民币：CNY，其他值列表详见货币类型
     */
    @XmlElement(name = "cash_fee_type")
    String cashFeeType = "CNY";
    /**
     * 字段名	变量名	必填	类型	示例值	描述
     * 总代金券金额	coupon_fee	否	Int	10	代金券金额<=订单金额，订单金额-代金券金额=现金支付金额，详见支付金额
     */
    @XmlElement(name = "coupon_fee")
    Integer couponFee;
    /**
     * 字段名	变量名	必填	类型	示例值	描述
     * 代金券使用数量	coupon_count	否	Int	1	代金券使用数量
     */
    @XmlElement(name = "coupon_count")
    Integer couponCount;


    /**
     * 字段名	变量名	必填	类型	示例值	描述
     * 代金券类型	coupon_type_$n	否	String	CASH
     * CASH--充值代金券
     * NO_CASH---非充值代金券
     * <p>
     * 并且订单使用了免充值券后有返回（取值：CASH、NO_CASH）。$n为下标,从0开始编号，举例：coupon_type_0
     * <p>
     * 注意：只有下单时订单使用了优惠，回调通知才会返回券信息。
     */
    @XmlElement(name = "coupon_type_0")
    String couponType0;
    @XmlElement(name = "coupon_type_1")
    String couponType1;

    /**
     * 字段名	变量名	必填	类型	示例值	描述
     * 代金券ID	coupon_id_$n	否	String(20)	10000	代金券ID,$n为下标，从0开始编号
     * 注意：只有下单时订单使用了优惠，回调通知才会返回券信息。
     */
    @XmlElement(name = "coupon_id_0")
    String couponId0;
    @XmlElement(name = "coupon_id_1")
    String couponId1;
    /**
     * 字段名	变量名	必填	类型	示例值	描述
     * 单个代金券支付金额	coupon_fee_$n	否	Int	100	单个代金券支付金额,$n为下标，从0开始编号
     */
    @XmlElement(name = "coupon_fee_0")
    String couponFee0;
    @XmlElement(name = "coupon_fee_1")
    String couponFee1;

    /**
     * 字段名	变量名	必填	类型	示例值	描述
     * 微信支付订单号	transaction_id	是	String(32)	1217752501201407033233368018	微信支付订单号
     */
    @XmlElement(name = "transaction_id")
    String transactionId;
    /**
     * 字段名	变量名	必填	类型	示例值	描述
     * 商户订单号	out_trade_no	是	String(32)	1212321211201407033568112322	商户系统内部订单号，要求32个字符内，只能是数字、大小写字母_-|*@ ，且在同一个商户号下唯一。
     */
    @XmlElement(name = "out_trade_no")
    String outTradeNo;
    /**
     * 字段名	变量名	必填	类型	示例值	描述
     * 商家数据包	attach	否	String(128)	123456	商家数据包，原样返回
     */
    @XmlElement(name = "attach")
    String attach;
    /**
     * 字段名	变量名	必填	类型	示例值	描述
     * 支付完成时间	time_end	是	String(14)	20141030133525	支付完成时间，格式为yyyyMMddHHmmss，如2009年12月25日9点10分10秒表示为20091225091010。其他详见时间规则
     */
    @XmlElement(name = "time_end")
    String timeEnd;

    public static String getDateFormat() {
        return DATE_FORMAT;
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

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public String getSignType() {
        return signType;
    }

    public void setSignType(String signType) {
        this.signType = signType;
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

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }

    public String getIsSubscribe() {
        return isSubscribe;
    }

    public void setIsSubscribe(String isSubscribe) {
        this.isSubscribe = isSubscribe;
    }

    public String getTradeType() {
        return tradeType;
    }

    public void setTradeType(String tradeType) {
        this.tradeType = tradeType;
    }

    public String getBankType() {
        return bankType;
    }

    public void setBankType(String bankType) {
        this.bankType = bankType;
    }

    public Integer getTotalFee() {
        return totalFee;
    }

    public void setTotalFee(Integer totalFee) {
        this.totalFee = totalFee;
    }

    public String getSettlementTotalFee() {
        return settlementTotalFee;
    }

    public void setSettlementTotalFee(String settlementTotalFee) {
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

    public Integer getCouponFee() {
        return couponFee;
    }

    public void setCouponFee(Integer couponFee) {
        this.couponFee = couponFee;
    }

    public Integer getCouponCount() {
        return couponCount;
    }

    public void setCouponCount(Integer couponCount) {
        this.couponCount = couponCount;
    }

    public String getCouponType0() {
        return couponType0;
    }

    public void setCouponType0(String couponType0) {
        this.couponType0 = couponType0;
    }

    public String getCouponType1() {
        return couponType1;
    }

    public void setCouponType1(String couponType1) {
        this.couponType1 = couponType1;
    }

    public String getCouponId0() {
        return couponId0;
    }

    public void setCouponId0(String couponId0) {
        this.couponId0 = couponId0;
    }

    public String getCouponId1() {
        return couponId1;
    }

    public void setCouponId1(String couponId1) {
        this.couponId1 = couponId1;
    }

    public String getCouponFee0() {
        return couponFee0;
    }

    public void setCouponFee0(String couponFee0) {
        this.couponFee0 = couponFee0;
    }

    public String getCouponFee1() {
        return couponFee1;
    }

    public void setCouponFee1(String couponFee1) {
        this.couponFee1 = couponFee1;
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

    public String getAttach() {
        return attach;
    }

    public void setAttach(String attach) {
        this.attach = attach;
    }

    public String getTimeEnd() {
        return timeEnd;
    }

    public void setTimeEnd(String timeEnd) {
        this.timeEnd = timeEnd;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("PayBillRequsetMesseage{");
        sb.append("appId='").append(appId).append('\'');
        sb.append(", mchId='").append(mchId).append('\'');
        sb.append(", deviceInfo='").append(deviceInfo).append('\'');
        sb.append(", nonceStr='").append(nonceStr).append('\'');
        sb.append(", sign='").append(sign).append('\'');
        sb.append(", signType='").append(signType).append('\'');
        sb.append(", resultCode='").append(resultCode).append('\'');
        sb.append(", errCode='").append(errCode).append('\'');
        sb.append(", errCodeDes='").append(errCodeDes).append('\'');
        sb.append(", openId='").append(openId).append('\'');
        sb.append(", isSubscribe='").append(isSubscribe).append('\'');
        sb.append(", tradeType='").append(tradeType).append('\'');
        sb.append(", bankType='").append(bankType).append('\'');
        sb.append(", totalFee=").append(totalFee);
        sb.append(", settlementTotalFee='").append(settlementTotalFee).append('\'');
        sb.append(", feeType='").append(feeType).append('\'');
        sb.append(", cashFee=").append(cashFee);
        sb.append(", cashFeeType='").append(cashFeeType).append('\'');
        sb.append(", couponFee=").append(couponFee);
        sb.append(", couponCount=").append(couponCount);
        sb.append(", couponType0='").append(couponType0).append('\'');
        sb.append(", couponType1='").append(couponType1).append('\'');
        sb.append(", couponId0='").append(couponId0).append('\'');
        sb.append(", couponId1='").append(couponId1).append('\'');
        sb.append(", couponFee0='").append(couponFee0).append('\'');
        sb.append(", couponFee1='").append(couponFee1).append('\'');
        sb.append(", transactionId='").append(transactionId).append('\'');
        sb.append(", outTradeNo='").append(outTradeNo).append('\'');
        sb.append(", attach='").append(attach).append('\'');
        sb.append(", timeEnd='").append(timeEnd).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
