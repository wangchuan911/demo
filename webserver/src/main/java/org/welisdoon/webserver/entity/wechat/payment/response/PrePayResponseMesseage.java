package org.welisdoon.webserver.entity.wechat.payment.response;

import com.sun.istack.NotNull;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "xml")
@XmlAccessorType(XmlAccessType.FIELD)
public class PrePayResponseMesseage {
    /**
     * 返回状态码	return_code	是	String(16)	SUCCESS
     * <p>
     * SUCCESS/FAIL
     * <p>
     * 此字段是通信标识，非交易标识，交易是否成功需要查看result_code来判断
     */
    @XmlElement(name = "return_code")
    String returnCode;

    /**
     * 字段名	变量名	必填	类型	示例值	描述
     * 返回信息	return_msg	否	String(128)	签名失败
     * 返回信息，如非空，为错误原因
     * <p>
     * 签名失败
     * <p>
     * 参数格式校验错误
     */
    @XmlElement(name = "return_msg")
    String returnMsg;
    /**
     * 以下字段在return_code为SUCCESS的时候有返回
     * */

    /**
     * 字段名	变量名	必填	类型	示例值	描述
     * 小程序ID	appid	是	String(32)	wx8888888888888888	调用接口提交的小程序ID
     */
    @XmlElement(name = "appid")
    String appId;
    /**
     * 字段名	变量名	必填	类型	示例值	描述
     * 商户号	mch_id	是	String(32)	1900000109	调用接口提交的商户号
     */
    @XmlElement(name = "mch_id")
    String mchId;
    /**
     * 字段名	变量名	必填	类型	示例值	描述
     * 设备号	device_info	否	String(32)	013467007045764	自定义参数，可以为请求支付的终端设备号等
     */
    @XmlElement(name = "device_info")
    String deviceInfo;
    /**
     * 字段名	变量名	必填	类型	示例值	描述
     * 随机字符串	nonce_str	是	String(32)	5K8264ILTKCH16CQ2502SI8ZNMTM67VS	微信返回的随机字符串
     */
    @XmlElement(name = "nonce_str")
    String nonceStr;
    /**
     * 字段名	变量名	必填	类型	示例值	描述
     * 签名	sign	是	String(64)	C380BEC2BFD727A4B6845133519F3AD6	微信返回的签名值，详见签名算法
     */
    @XmlElement(name = "sign")
    String sign;
    /**
     * 字段名	变量名	必填	类型	示例值	描述
     * 业务结果	result_code	是	String(16)	SUCCESS	SUCCESS/FAIL
     */
    @XmlElement(name = "result_code")
    String resultCode;
    /**
     * 字段名	变量名	必填	类型	示例值	描述
     * 错误代码	err_code	否	String(32)	SYSTEMERROR	详细参见下文错误列表
     */
    @XmlElement(name = "err_code")
    String errCode;
    /**
     * 字段名	变量名	必填	类型	示例值	描述
     * 错误代码描述	err_code_des	否	String(128)	系统错误	错误信息描述
     */
    @XmlElement(name = "err_code_des")
    String errCodeDes;
    /**
     * 以下字段在return_code 和result_code都为SUCCESS的时候有返回
     */

    /**
     * 字段名	变量名	必填	类型	示例值	描述
     * 二维码链接	code_url	否	String(64)	weixin://wxpay/bizpayurl/up?pr=NwY5Mz9&groupid=00
     * trade_type=NATIVE时有返回，此url用于生成支付二维码，然后提供给用户进行扫码支付。
     * <p>
     * 注意：code_url的值并非固定，使用时按照URL格式转成二维码即可
     */
    @XmlElement(name = "code_url")
    String codeUrl;
    /**
     * 字段名	变量名	必填	类型	示例值	描述
     * 交易类型	trade_type	是	String(16)	JSAPI	交易类型，取值为：JSAPI，NATIVE，APP等，说明详见参数规定
     */
    @XmlElement(name = "trade_type")
    String tradeType;
    /**
     * 字段名	变量名	必填	类型	示例值	描述
     * 预支付交易会话标识	prepay_id	是	String(64)	wx201410272009395522657a690389285100	微信生成的预支付会话标识，用于后续接口调用中使用，该值有效期为2小时
     */
    @XmlElement(name = "prepay_id")
    String prepayId;

    public String getReturnCode() {
        return returnCode;
    }

    public PrePayResponseMesseage setReturnCode(String returnCode) {
        this.returnCode = returnCode;
        return this;
    }

    public String getReturnMsg() {
        return returnMsg;
    }

    public PrePayResponseMesseage setReturnMsg(String returnMsg) {
        this.returnMsg = returnMsg;
        return this;
    }

    public String getAppId() {
        return appId;
    }

    public PrePayResponseMesseage setAppId(String appId) {
        this.appId = appId;
        return this;
    }

    public String getMchId() {
        return mchId;
    }

    public PrePayResponseMesseage setMchId(String mchId) {
        this.mchId = mchId;
        return this;
    }

    public String getDeviceInfo() {
        return deviceInfo;
    }

    public PrePayResponseMesseage setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
        return this;
    }

    public String getNonceStr() {
        return nonceStr;
    }

    public PrePayResponseMesseage setNonceStr(String nonceStr) {
        this.nonceStr = nonceStr;
        return this;
    }

    public String getSign() {
        return sign;
    }

    public PrePayResponseMesseage setSign(String sign) {
        this.sign = sign;
        return this;
    }

    public String getResultCode() {
        return resultCode;
    }

    public PrePayResponseMesseage setResultCode(String resultCode) {
        this.resultCode = resultCode;
        return this;
    }

    public String getErrCode() {
        return errCode;
    }

    public PrePayResponseMesseage setErrCode(String errCode) {
        this.errCode = errCode;
        return this;
    }

    public String getErrCodeDes() {
        return errCodeDes;
    }

    public PrePayResponseMesseage setErrCodeDes(String errCodeDes) {
        this.errCodeDes = errCodeDes;
        return this;
    }

    public String getCodeUrl() {
        return codeUrl;
    }

    public PrePayResponseMesseage setCodeUrl(String codeUrl) {
        this.codeUrl = codeUrl;
        return this;
    }

    public String getTradeType() {
        return tradeType;
    }

    public PrePayResponseMesseage setTradeType(String tradeType) {
        this.tradeType = tradeType;
        return this;
    }

    public String getPrepayId() {
        return prepayId;
    }

    public PrePayResponseMesseage setPrepayId(String prepayId) {
        this.prepayId = prepayId;
        return this;
    }
}
