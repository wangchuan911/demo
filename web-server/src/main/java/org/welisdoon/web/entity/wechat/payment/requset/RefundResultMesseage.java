package org.welisdoon.web.entity.wechat.payment.requset;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.reflections.ReflectionUtils;
import org.springframework.util.Assert;
import org.springframework.util.Base64Utils;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.welisdoon.web.common.encrypt.WXBizMsgCrypt;
import org.welisdoon.web.entity.wechat.utils.SignUtils;
import org.xml.sax.InputSource;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.security.*;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidParameterSpecException;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * @Classname RefundRequestMesseage
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/10/26 20:50
 */
@XmlRootElement(name = "xml")
@XmlAccessorType(XmlAccessType.FIELD)
public class RefundResultMesseage {

    //    公众账号ID	appid	是	String(32)	wx8888888888888888	微信分配的公众账号ID（企业号corpid即为此appid）
    String appId;
    //    商户号	mch_id	是	String(32)	1900000109	微信支付分配的商户号
    String mchId;
    //    随机字符串	nonce_str	是	String(32)	5K8264ILTKCH16CQ2502SI8ZNMTM67VS	随机字符串，不长于32位。推荐随机数生成算法
    String nonceStr;
    //    签名	sign	是	String(32)	C380BEC2BFD727A4B6845133519F3AD6	签名，详见签名生成算法
    String sign;
    //    签名类型	sign_type	否	String(32)	HMAC-SHA256	签名类型，目前支持HMAC-SHA256和MD5，默认为MD5
    String signType;
    //    微信支付订单号	transaction_id	二选一	String(32)	1217752501201407033233368018	微信生成的订单号，在支付通知中有返回
//    商户订单号	out_trade_no	String(32)	1217752501201407033233368018	商户系统内部订单号，要求32个字符内，只能是数字、大小写字母_-|*@ ，且在同一个商户号下唯一。
//    transaction_id、out_trade_no二选一，如果同时存在优先级：transaction_id> out_trade_no
//
    @XmlElement(name = "transaction_id")
    String transactionId;
    @XmlElement(name = "out_trade_no")
    String outTradeNo;
//    商户退款单号	out_refund_no	是	String(64)	1217752501201407033233368018	商户系统内部的退款单号，商户系统内部唯一，只能是数字、大小写字母_-|*@ ，同一退款单号多次请求只退一笔。

    @XmlElement(name = "out_refund_no")
    String outRefundNo;
    //    订单金额	total_fee	是	int	100	订单总金额，单位为分，只能为整数，详见支付金额
    @XmlElement(name = "total_fee")
    Integer totalFee;
    //    退款金额	refund_fee	是	int	100	退款总金额，订单总金额，单位为分，只能为整数，详见支付金额
    @XmlElement(name = "refund_fee")
    Integer refundFee;
    //    退款货币种类	refund_fee_type	否	String(8)	CNY	退款货币类型，需与支付一致，或者不填。符合ISO 4217标准的三位字母代码，默认人民币：CNY，其他值列表详见货币类型
    @XmlElement(name = "refund_fee_type")
    String refundFeeType;
    //    退款原因	refund_desc	否	String(80)	商品已售完
//    若商户传入，会在下发给用户的退款消息中体现退款原因
    @XmlElement(name = "refund_desc")
    String refundDesc;
//
//    注意：若订单退款金额≤1元，且属于部分退款，则不会在退款消息中体现退款原因
//
//    退款资金来源	refund_account	否	String(30)	REFUND_SOURCE_RECHARGE_FUNDS
//    仅针对老资金流商户使用
//
//    REFUND_SOURCE_UNSETTLED_FUNDS---未结算资金退款（默认使用未结算资金退款）
//
//    REFUND_SOURCE_RECHARGE_FUNDS---可用余额退款

    @XmlElement(name = "refund_account")
    String refundAccount;
//
//    退款结果通知url	notify_url	否	String(256)	https://weixin.qq.com/notify/
//    异步接收微信支付退款结果通知的回调地址，通知URL必须为外网可访问的url，不允许带参数
//    公网域名必须为https，如果是走专线接入，使用专线NAT IP或者私有回调域名可使用http
//
//    如果参数中传了notify_url，则商户平台上配置的回调地址将不会生效。

    @XmlElement(name = "notify_url")
    String notifyUrl;


    @XmlElement(name = "req_info")
    String reqInfo;

    public String getAppId() {
        return appId;
    }

    public RefundResultMesseage setAppId(String appId) {
        this.appId = appId;
        return this;
    }

    public String getMchId() {
        return mchId;
    }

    public RefundResultMesseage setMchId(String mchId) {
        this.mchId = mchId;
        return this;
    }

    public String getNonceStr() {
        return nonceStr;
    }

    public RefundResultMesseage setNonceStr(String nonceStr) {
        this.nonceStr = nonceStr;
        return this;
    }

    public String getSign() {
        return sign;
    }

    public RefundResultMesseage setSign(String sign) {
        /*StringBuilder tmpStr = new StringBuilder();
        Class<XmlElement> annotationClass = XmlElement.class;
        ReflectionUtils.getFields(this.getClass(), ReflectionUtils.withAnnotation(annotationClass))
                .stream()
                .filter(field -> {
                    try {
                        field.setAccessible(true);
                        return field.get(this) != null;
                    } catch (Throwable t) {
                        t.printStackTrace();
                        throw new RuntimeException(t);
                    }
                })
                .sorted((o1, o2) -> {
                    *//*char c1 = o1.getAnnotation(annotationClass).name().charAt(0);
                    char c2 = o2.getAnnotation(annotationClass).name().charAt(0);
                    return c1 - c2;*//*
                    return PrePayRequsetMesseage.ASCIISort(o1.getAnnotation(annotationClass).name(), o2.getAnnotation(annotationClass).name());
                })
                .forEachOrdered(field -> {
                    try {
                        tmpStr.append(field.getAnnotation(annotationClass).name()).append('=').append(field.get(this)).append('&');
                    } catch (Throwable t) {
                        t.printStackTrace();
                        throw new RuntimeException(t);
                    }
                });
        tmpStr.append("key=")
                .append(sign);
        this.sign = DigestUtils
                .md5Hex(tmpStr.toString())
                .toUpperCase();*/
        this.sign = SignUtils.sign(this, sign);
        return this;
    }

    public String getSignType() {
        return signType;
    }

    public RefundResultMesseage setSignType(String signType) {
        this.signType = signType;
        return this;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public RefundResultMesseage setTransactionId(String transactionId) {
        this.transactionId = transactionId;
        return this;
    }

    public String getOutRefundNo() {
        return outRefundNo;
    }

    public RefundResultMesseage setOutRefundNo(String outRefundNo) {
        this.outRefundNo = outRefundNo;
        return this;
    }

    public Integer getTotalFee() {
        return totalFee;
    }

    public RefundResultMesseage setTotalFee(Integer totalFee) {
        this.totalFee = totalFee;
        return this;
    }

    public Integer getRefundFee() {
        return refundFee;
    }

    public RefundResultMesseage setRefundFee(Integer refundFee) {
        this.refundFee = refundFee;
        return this;
    }

    public String getRefundFeeType() {
        return refundFeeType;
    }

    public RefundResultMesseage setRefundFeeType(String refundFeeType) {
        this.refundFeeType = refundFeeType;
        return this;
    }

    public String getRefundDesc() {
        return refundDesc;
    }

    public RefundResultMesseage setRefundDesc(String refundDesc) {
        this.refundDesc = refundDesc;
        return this;
    }

    public String getRefundAccount() {
        return refundAccount;
    }

    public RefundResultMesseage setRefundAccount(String refundAccount) {
        this.refundAccount = refundAccount;
        return this;
    }

    public String getNotifyUrl() {
        return notifyUrl;
    }

    public RefundResultMesseage setNotifyUrl(String notifyUrl) {
        this.notifyUrl = notifyUrl;
        return this;
    }

    public String getOutTradeNo() {
        return outTradeNo;
    }

    public void setOutTradeNo(String outTradeNo) {
        this.outTradeNo = outTradeNo;
    }

    public String getReqInfo() {
        return reqInfo;
    }

    public RefundResultMesseage setReqInfo(String reqInfo) {
        this.reqInfo = reqInfo;
        return this;
    }

    public RefundResultMesseage decrypt(String key) {
        /*
         *解密步骤如下：
            （1）对加密串A做base64解码，得到加密串B
            （2）对商户key做md5，得到32位小写key* ( key设置路径：微信商户平台(pay.weixin.qq.com)-->账户设置-->API安全-->密钥设置 )

            （3）用key*对加密串B做AES-256-ECB解密（PKCS7Padding）
        */
        /*
         PZ/oFW/q+RziEaWc3ujFJpKvgYocDhtUZEDKa/FodEqTHk3b3yOmsAcNE/d0AVClj3NRYEa9E9XcoNYNzXPKZX7naKCZ3qjkzSHLNaUe8rfUVOb0wsdHa50yNxrgBupDVn97ctSlKvEcLwBcnDVLBEF/JnJ2kyfYQjIyBiWuKsjPtPb4h8csUIMJqdhheRBt7414/NdA6Ep/FcnXTr/VnQfWdD7QT1dkuWtyXdYeOrlPLSYmrvbjEfIeJ/IOiMCO97PsRLm6fIqbafr6NErQWJoK7M87Z6kgUJfM3LvAlioOS1gDCHA+3kqvBezYGqCDdgfZAks0lDsmXiwOLTidmXOHRZc2bMfZham9YHyB8Iv1zLxJ68WVa7JD3nwiTOW6SbgTsL+vtRdBhKVKcL4u4Ya8VqWIhOP1/0s1uuqyGl7Ai9UGyvXNLyUBwDg+t8En3kOVvsb7FQADKfSViAJkgcoizLcFQsu0pCeweXamCUKvbvl9b00ULKE8m9kJBLkXCu4qVkFOejqeOn9mgHzNLEl2mEPXiiWSYWj5yyKYo7g6Fav3RSEklvSdroojsNyxF5vcvunDOUQv2RzLT8Gvi8iyhmBYKumJCjk2ULec2ARF2cuR5ZjEq94hLFHEhRouu6iuXqMe81xULTYW71pQT/fGcjay9noFNjkISaeUg5pBTb2PKRbl8HKpXmF0nOaho1/YKlyVfIAGzTICG190D8hLaumuSSH6Yh8modcqeYHD6TgguKw8RlxpwGZ1HxLSvaNYRZeIOa98TmDU1TmTDs2TT6Eu7gR99YW25TCVjJh9aE6husUpyjaHDIEdJjxd2yldvAOaCwCh7BVDR8xzEalapAASErD0XQVoRvMq5xyt6LWnx885pSiR23hdStCEd5hToB9kxYoaV1O578xtLtq81mFGwWQp1TEYYwpfuV96mFs4trhqm/UVEFCZ0y1/YKUclEMPaNSMiWMEx4xzxH9SOxS6trPBWqA6CGj1merZQA6ADMUizyU+yn1luz9GiQ/mv2wKNUyNpFfOXgP8mlHYww41QcJT2eknxWEgHAQFH9yPhXhIjEqr1rLg4MPv0RAJDUmiaCVdY3KlaMibGw==
        */
        try {

            byte[] content = WXBizMsgCrypt.base64.decode(this.reqInfo);
            // 初始化
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS7Padding", "BC");
            SecretKeySpec secretKeySpec = new SecretKeySpec(DigestUtils.md5Hex(key).toLowerCase().getBytes(), "AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            StringReader sr = new StringReader(new String(cipher.doFinal(content)));
            InputSource is = new InputSource(sr);
            Document document = db.parse(is);

            Element root = document.getDocumentElement();
            for (Field field : ReflectionUtils.getAllFields(this.getClass(), ReflectionUtils.withAnnotation(XmlElement.class))) {
                NodeList nodeList = root.getElementsByTagName(field.getAnnotation(XmlElement.class).name());
                if (nodeList == null || nodeList.getLength() == 0) continue;
                String xmlValue = nodeList.item(0).getTextContent();
                if (xmlValue == null || xmlValue.length() == 0) continue;
                Object value;
                try {
                    if (Integer.class.equals(field.getType())) {
                        value = Integer.parseInt(xmlValue);
                    } else if (String.class.equals(field.getType())) {
                        value = xmlValue;
                    } else {
                        continue;
                    }
                    field.set(RefundResultMesseage.this, value);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return this;
    }

    public static void main(String[] args) {
        new RefundResultMesseage().setReqInfo("PZ/oFW/q+RziEaWc3ujFJpKvgYocDhtUZEDKa/FodEqTHk3b3yOmsAcNE/d0AVClj3NRYEa9E9XcoNYNzXPKZX7naKCZ3qjkzSHLNaUe8rfUVOb0wsdHa50yNxrgBupDVn97ctSlKvEcLwBcnDVLBEF/JnJ2kyfYQjIyBiWuKsjPtPb4h8csUIMJqdhheRBt7414/NdA6Ep/FcnXTr/VnQfWdD7QT1dkuWtyXdYeOrlPLSYmrvbjEfIeJ/IOiMCO97PsRLm6fIqbafr6NErQWJoK7M87Z6kgUJfM3LvAlioOS1gDCHA+3kqvBezYGqCDdgfZAks0lDsmXiwOLTidmXOHRZc2bMfZham9YHyB8Iv1zLxJ68WVa7JD3nwiTOW6SbgTsL+vtRdBhKVKcL4u4Ya8VqWIhOP1/0s1uuqyGl7Ai9UGyvXNLyUBwDg+t8En3kOVvsb7FQADKfSViAJkgcoizLcFQsu0pCeweXamCUKvbvl9b00ULKE8m9kJBLkXCu4qVkFOejqeOn9mgHzNLEl2mEPXiiWSYWj5yyKYo7g6Fav3RSEklvSdroojsNyxF5vcvunDOUQv2RzLT8Gvi8iyhmBYKumJCjk2ULec2ARF2cuR5ZjEq94hLFHEhRouu6iuXqMe81xULTYW71pQT/fGcjay9noFNjkISaeUg5pBTb2PKRbl8HKpXmF0nOaho1/YKlyVfIAGzTICG190D8hLaumuSSH6Yh8modcqeYHD6TgguKw8RlxpwGZ1HxLSvaNYRZeIOa98TmDU1TmTDs2TT6Eu7gR99YW25TCVjJh9aE6husUpyjaHDIEdJjxd2yldvAOaCwCh7BVDR8xzEalapAASErD0XQVoRvMq5xyt6LWnx885pSiR23hdStCEd5hToB9kxYoaV1O578xtLtq81mFGwWQp1TEYYwpfuV96mFs4trhqm/UVEFCZ0y1/YKUclEMPaNSMiWMEx4xzxH9SOxS6trPBWqA6CGj1merZQA6ADMUizyU+yn1luz9GiQ/mv2wKNUyNpFfOXgP8mlHYww41QcJT2eknxWEgHAQFH9yPhXhIjEqr1rLg4MPv0RAJDUmiaCVdY3KlaMibGw==")
                .decrypt("20191230dashanhuaquanfuhubida888");
    }
}
