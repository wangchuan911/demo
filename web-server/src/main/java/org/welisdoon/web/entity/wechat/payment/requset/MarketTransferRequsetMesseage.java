package org.welisdoon.web.entity.wechat.payment.requset;

import org.welisdoon.web.entity.wechat.utils.SignUtils;

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
public class MarketTransferRequsetMesseage {
    /**
     * 商户账号appid	mch_appid	是	wx8888888888888888	String(128)	申请商户号的appid或商户号绑定的appid
     * 商户号	mchid	是	1900000109	String(32)	微信支付分配的商户号
     * 设备号	device_info	否	013467007045764	String(32)	微信支付分配的终端设备号
     * 随机字符串	nonce_str	是	5K8264ILTKCH16CQ2502SI8ZNMTM67VS	String(32)	随机字符串，不长于32位
     * 签名	sign	是	C380BEC2BFD727A4B6845133519F3AD6	String(32)	签名，详见签名算法
     * 商户订单号	partner_trade_no	是	10000098201411111234567890	String(32)	商户订单号，需保持唯一性
     * (只能是字母或者数字，不能包含有其它字符)
     * 用户openid	openid	是	oxTWIuGaIt6gTKsQRLau2M0yL16E	String(64)	openid是微信用户在公众号appid下的唯一用户标识（appid不同，则获取到的openid就不同），可用于永久标记一个用户。
     * 获取openid的链接
     * 校验用户姓名选项	check_name	是	FORCE_CHECK	String(16)	NO_CHECK：不校验真实姓名
     * FORCE_CHECK：强校验真实姓名
     * 收款用户姓名	re_user_name	否	王小王	String(64)	收款用户真实姓名。
     * 如果check_name设置为FORCE_CHECK，则必填用户真实姓名
     * 如需电子回单，需要传入收款用户姓名
     * 金额	amount	是	10099	int	付款金额，单位为分
     * 付款备注	desc	是	理赔	String(100)	付款备注，必填。注意：备注中的敏感词会被转成字符*
     * Ip地址	spbill_create_ip	否	192.168.0.1	String(32)	该IP同在商户平台设置的IP白名单中的IP没有关联，该IP可传用户端或者服务端的IP。
     * 付款场景	scene	否	BRAND_REDPACKET	String(64)	BRAND_REDPACKET：品牌红包，
     * 其他值或不传则默认为普通付款到零钱
     * （品牌红包能力暂未全量开放，若有意愿参与内测请填写问卷https://wj.qq.com/s2/9229085/29f4/）
     * 品牌ID	brand_id	否	1234	int	品牌在微信支付的唯一标识。仅在付款场景为品牌红包时必填。
     * 消息模板ID	finder_template_id	否	1243100000000000	String(128)	品牌所配置的消息模板的唯一标识。仅在付款场景为品牌红包时必填。
     */
    @XmlElement(name = "mch_appid")
    String mchAppid;
    @XmlElement(name = "mchid")
    String mchId;
    @XmlElement(name = "device_info")
    String deviceInfo;
    @XmlElement(name = "nonce_str")
    String nonceStr;
    @XmlElement(name = "sign")
    String sign;
    @XmlElement(name = "partner_trade_no")
    String partnerTradeNo;
    @XmlElement(name = "openid")
    String openid;
    @XmlElement(name = "check_name")
    String checkName;
    @XmlElement(name = "re_user_name")
    String reUserName;
    @XmlElement(name = "amount")
    Integer amount;
    @XmlElement(name = "desc")
    String desc;
    @XmlElement(name = "spbill_create_ip")
    String spbillCreateIp;
    @XmlElement(name = "scene")
    String scene;
    @XmlElement(name = "brand_id")
    String brandId;
    @XmlElement(name = "finder_template_id")
    String finderTemplateId;

    public String getMchAppid() {
        return mchAppid;
    }

    public MarketTransferRequsetMesseage setMchAppid(String mchAppid) {
        this.mchAppid = mchAppid;
        return this;
    }

    public String getMchId() {
        return mchId;
    }

    public MarketTransferRequsetMesseage setMchId(String mchId) {
        this.mchId = mchId;
        return this;
    }

    public String getDeviceInfo() {
        return deviceInfo;
    }

    public MarketTransferRequsetMesseage setDeviceInfo(String deviceInfo) {
        this.deviceInfo = deviceInfo;
        return this;
    }

    public String getNonceStr() {
        return nonceStr;
    }

    public MarketTransferRequsetMesseage setNonceStr(String nonceStr) {
        this.nonceStr = nonceStr;
        return this;
    }

    public String getSign() {
        return sign;
    }

    public MarketTransferRequsetMesseage setSign(String sign) {
        this.sign = SignUtils.sign(this, sign);
        return this;
    }

    public String getPartnerTradeNo() {
        return partnerTradeNo;
    }

    public MarketTransferRequsetMesseage setPartnerTradeNo(String partnerTradeNo) {
        this.partnerTradeNo = partnerTradeNo;
        return this;
    }

    public String getOpenid() {
        return openid;
    }

    public MarketTransferRequsetMesseage setOpenid(String openid) {
        this.openid = openid;
        return this;
    }

    public String getCheckName() {
        return checkName;
    }

    public MarketTransferRequsetMesseage setCheckName(String checkName) {
        this.checkName = checkName;
        return this;
    }

    public String getReUserName() {
        return reUserName;
    }

    public MarketTransferRequsetMesseage setReUserName(String reUserName) {
        this.reUserName = reUserName;
        return this;
    }

    public Integer getAmount() {
        return amount;
    }

    public MarketTransferRequsetMesseage setAmount(Integer amount) {
        this.amount = amount;
        return this;
    }

    public String getDesc() {
        return desc;
    }

    public MarketTransferRequsetMesseage setDesc(String desc) {
        this.desc = desc;
        return this;
    }

    public String getSpbillCreateIp() {
        return spbillCreateIp;
    }

    public MarketTransferRequsetMesseage setSpbillCreateIp(String spbillCreateIp) {
        this.spbillCreateIp = spbillCreateIp;
        return this;
    }

    public String getScene() {
        return scene;
    }

    public MarketTransferRequsetMesseage setScene(String scene) {
        this.scene = scene;
        return this;
    }

    public String getBrandId() {
        return brandId;
    }

    public MarketTransferRequsetMesseage setBrandId(String brandId) {
        this.brandId = brandId;
        return this;
    }

    public String getFinderTemplateId() {
        return finderTemplateId;
    }

    public MarketTransferRequsetMesseage setFinderTemplateId(String finderTemplateId) {
        this.finderTemplateId = finderTemplateId;
        return this;
    }
}
