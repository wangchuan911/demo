package org.welisdoon.webserver.entity.wechat.payment.requset;

import com.sun.istack.NotNull;
import org.apache.commons.codec.digest.Md5Crypt;
import org.reflections.ReflectionUtils;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.lang.annotation.Annotation;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@XmlRootElement(name = "xml")
@XmlAccessorType(XmlAccessType.FIELD)
public class PrePayRequsetMesseage {
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
     * 商品描述	body	是	String(128)	腾讯充值中心-QQ会员充值 商品简单描述，该字段请按照规范传递，具体请见参数规定
     */
    @NotNull
    @XmlElement(name = "body")
    String body;

    /**
     * 字段名	变量名	必填	类型	示例值	描述
     * 商品详情	detail	否	String(6000)	 	商品详细描述，对于使用单品优惠的商户，该字段必须按照规范上传，详见“单品优惠参数说明”
     */
    @XmlElement(name = "detail")
    String detail;

    /**
     * 字段名	变量名	必填	类型	示例值	描述
     * 附加数据	attach	否	String(127)	深圳分店	附加数据，在查询API和支付通知中原样返回，可作为自定义参数使用。
     */
    @XmlElement(name = "attach")
    String attach;
    /**
     * 商户订单号	out_trade_no	是	String(32)	20150806125346	商户系统内部订单号，要求32个字符内，只能是数字、大小写字母_-|*且在同一个商户号下唯一。详见商户订单号
     */
    @XmlElement(name = "out_trade_no")
    String outTradeNo;
    /**
     * 字段名	变量名	必填	类型	示例值	描述
     * 标价币种	fee_type	否	String(16)	CNY	符合ISO 4217标准的三位字母代码，默认人民币：CNY，详细列表请参见货币类型
     */
    @XmlElement(name = "fee_type")
    String feeType = "CNY";
    /**
     * 字段名	变量名	必填	类型	示例值	描述
     * 标价金额	total_fee	是	Int	88	订单总金额，单位为分，详见支付金额
     */
    @XmlElement(name = "total_fee")
    Integer totalFee;
    /**
     * 字段名	变量名	必填	类型	示例值	描述
     * 终端IP	spbill_create_ip	是	String(64)	123.12.12.123	支持IPV4和IPV6两种格式的IP地址。调用微信支付API的机器IP
     */
    @XmlElement(name = "spbill_create_ip")
    String spbillCreateIp;
    /**
     * 字段名	变量名	必填	类型	示例值	描述
     * 交易起始时间	time_start	否	String(14)	20091225091010	订单生成时间，格式为yyyyMMddHHmmss，如2009年12月25日9点10分10秒表示为20091225091010。其他详见时间规则
     */
    @XmlElement(name = "time_start")
    String timeStart;
    /**
     * 字段名	变量名	必填	类型	示例值	描述
     * 交易结束时间	time_expire	否	String(14)	20091227091010  订单失效时间，格式为yyyyMMddHHmmss，如2009年12月27日9点10分10秒表示为20091227091010。订单失效时间是针对订单号而言的，由于在请求支付的时候有一个必传参数prepay_id只有两小时的有效期，所以在重入时间超过2小时的时候需要重新请求下单接口获取新的prepay_id。其他详见时间规则
     * <p>
     * 建议：最短失效时间间隔大于1分钟
     */
    @XmlElement(name = "time_expire")
    String timeExpire;
    /**
     * 字段名	变量名	必填	类型	示例值	描述
     * 订单优惠标记	goods_tag	否	String(32)	WXG	订单优惠标记，使用代金券或立减优惠功能时需要的参数，说明详见代金券或立减优惠
     */
    @XmlElement(name = "goods_tag")
    String goodsTag;
    /**
     * 字段名	变量名	必填	类型	示例值	描述
     * 通知地址	notify_url	是	String(256)	http://www.weixin.qq.com/wxpay/pay.php	异步接收微信支付结果通知的回调地址，通知url必须为外网可访问的url，不能携带参数。
     */
    @XmlElement(name = "notify_url")
    String notifyUrl;
    /**
     * 字段名	变量名	必填	类型	示例值	描述
     * 交易类型	trade_type	是	String(16)	JSAPI	小程序取值如下：JSAPI，详细说明见参数规定
     */
    @XmlElement(name = "trade_type")
    String tradeType;
    /**
     * 字段名	变量名	必填	类型	示例值	描述
     * 商品ID	product_id	否	String(32)	12235413214070356458058	trade_type=NATIVE时，此参数必传。此参数为二维码中包含的商品ID，商户自行定义。
     */
    @XmlElement(name = "product_id")
    String productId;
    /**
     * 字段名	变量名	必填	类型	示例值	描述
     * 指定支付方式	limit_pay	否	String(32)	no_credit	上传此参数no_credit--可限制用户不能使用信用卡支付
     */
    @XmlElement(name = "limit_pay")
    String limitPay;
    /**
     * 字段名	变量名	必填	类型	示例值	描述
     * 用户标识	openid	否	String(128)	oUpF8uMuAJO_M2pxb1Q9zNjWeS6o	trade_type=JSAPI，此参数必传，用户在商户appid下的唯一标识。openid如何获取，可参考【获取openid】。
     */
    @XmlElement(name = "openid")
    String openid;
    /**
     * 字段名	变量名	必填	类型	示例值	描述
     * 电子发票入口开放标识	receipt	否	String(8)	Y	Y，传入Y时，支付成功消息和支付详情页将出现开票入口。需要在微信支付商户平台或微信公众平台开通电子发票功能，传此字段才可生效
     */
    @XmlElement(name = "receipt")
    String receipt;
    /**
     * 字段名	变量名	必填	类型	示例值	描述
     * +场景信息	scene_info	否	String(256)
     * {"store_info" : {
     * "id": "SZTX001",
     * "name": "腾大餐厅",
     * "area_code": "440305",
     * "address": "科技园中一路腾讯大厦" }}
     * <p>
     * 该字段常用于线下活动时的场景信息上报，支持上报实际门店信息，商户也可以按需求自己上报相关信息。该字段为JSON对象数据，对象格式为{"store_info":{"id": "门店ID","name": "名称","area_code": "编码","address": "地址" }} ，字段详细说明请点击行前的+展开
     */
    @XmlElement(name = "scene_info")
    String sceneInfo;


    public String getAppId() {
        return appId;
    }

    public PrePayRequsetMesseage setAppId(String appId) {
        this.appId = appId;
        return this;
    }

    public String getMchId() {
        return mchId;
    }

    public PrePayRequsetMesseage setMchId(String mchId) {
        this.mchId = mchId;
        return this;
    }

    public String getDeviceInfo() {
        return deviceInfo;
    }

    public PrePayRequsetMesseage setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
        return this;
    }

    public String getNonceStr() {
        return nonceStr;
    }

    public PrePayRequsetMesseage setNonceStr(String nonceStr) {
        this.nonceStr = nonceStr;
        return this;
    }

    public String getSign() {
        return sign;
    }

    public PrePayRequsetMesseage setSign(String sign) {
        StringBuilder tmpStr = new StringBuilder();
        Class<XmlElement> annotationClass = XmlElement.class;
        ReflectionUtils.getFields(this.getClass(), ReflectionUtils.withAnnotation(annotationClass))
                .stream()
                .filter(field -> {
                    try {
                        field.setAccessible(true);
                        return field.get(this) != null;
                    } catch (Throwable t) {
                        t.printStackTrace();
                        return false;
                    }
                })
                .sorted((o1, o2) -> {
                    char c1 = o1.getAnnotation(annotationClass).name().charAt(0);
                    char c2 = o2.getAnnotation(annotationClass).name().charAt(0);
                    return c1 - c2;
                })
                .forEachOrdered(field -> {
                    try {
                        tmpStr.append(field.getAnnotation(annotationClass).name()).append('=').append(field.get(this)).append('&');
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                });
        this.sign = Md5Crypt
                .md5Crypt(tmpStr.append("key=")
                        .append(sign)
                        .toString()
                        .getBytes())
                .toUpperCase();
        return this;
    }

    public String getSignType() {
        return signType;
    }

    public PrePayRequsetMesseage setSignType(String signType) {
        this.signType = signType;
        return this;
    }

    public String getBody() {
        return body;
    }

    public PrePayRequsetMesseage setBody(String body) {
        this.body = body;
        return this;
    }

    public String getDetail() {
        return detail;
    }

    public PrePayRequsetMesseage setDetail(String detail) {
        this.detail = detail;
        return this;
    }

    public String getAttach() {
        return attach;
    }

    public PrePayRequsetMesseage setAttach(String attach) {
        this.attach = attach;
        return this;
    }

    public String getOutTradeNo() {
        return outTradeNo;
    }

    public PrePayRequsetMesseage setOutTradeNo(String outTradeNo) {
        this.outTradeNo = outTradeNo;
        return this;
    }

    public String getFeeType() {
        return feeType;
    }

    public PrePayRequsetMesseage setFeeType(String feeType) {
        this.feeType = feeType;
        return this;
    }

    public Integer getTotalFee() {
        return totalFee;
    }

    public PrePayRequsetMesseage setTotalFee(Integer totalFee) {
        this.totalFee = totalFee;
        return this;
    }

    public String getSpbillCreateIp() {
        return spbillCreateIp;
    }

    public PrePayRequsetMesseage setSpbillCreateIp(String spbillCreateIp) {
        this.spbillCreateIp = spbillCreateIp;
        return this;
    }

    public Date getTimeStart() {
        try {
            return new SimpleDateFormat(DATE_FORMAT).parse(this.timeStart);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public PrePayRequsetMesseage setTimeStart(Date timeStart) {
        this.timeStart = new SimpleDateFormat(DATE_FORMAT).format(timeStart);
        return this;
    }

    public Date getTimeExpire() {
        try {
            return new SimpleDateFormat(DATE_FORMAT).parse(this.timeExpire);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public PrePayRequsetMesseage setTimeExpire(Date timeExpire) {
        this.timeExpire = new SimpleDateFormat(DATE_FORMAT).format(timeExpire);
        return this;
    }

    public String getGoodsTag() {
        return goodsTag;
    }

    public PrePayRequsetMesseage setGoodsTag(String goodsTag) {
        this.goodsTag = goodsTag;
        return this;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public PrePayRequsetMesseage setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
        return this;
    }

    public String getTradeType() {
        return tradeType;
    }

    public PrePayRequsetMesseage setTradeType(String tradeType) {
        this.tradeType = tradeType;
        return this;
    }

    public String getProductId() {
        return productId;
    }

    public PrePayRequsetMesseage setProductId(String productId) {
        this.productId = productId;
        return this;
    }

    public String getLimitPay() {
        return limitPay;
    }

    public PrePayRequsetMesseage setLimitPay(String limitPay) {
        this.limitPay = limitPay;
        return this;
    }

    public String getOpenid() {
        return openid;
    }

    public PrePayRequsetMesseage setOpenid(String openid) {
        this.openid = openid;
        return this;
    }

    public String getReceipt() {
        return receipt;
    }

    public PrePayRequsetMesseage setReceipt(String receipt) {
        this.receipt = receipt;
        return this;
    }

    public String getSceneInfo() {
        return sceneInfo;
    }

    public PrePayRequsetMesseage setSceneInfo(String sceneInfo) {
        this.sceneInfo = sceneInfo;
        return this;
    }
}
