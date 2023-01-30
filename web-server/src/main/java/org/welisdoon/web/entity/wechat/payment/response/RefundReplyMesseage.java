package org.welisdoon.web.entity.wechat.payment.response;

import org.springframework.util.StringUtils;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @Classname RefundResponseMesseage
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/11/8 21:26
 */
@XmlRootElement(name = "xml")
@XmlAccessorType(XmlAccessType.FIELD)
public class RefundReplyMesseage {

    //返回状态码	return_code	是	String(16)	SUCCESS
    //SUCCESS/FAIL
    //
    //此字段是通信标识，非交易标识，退款是否成功需要解密后查看refund_status来判断
    //
    @XmlElement(name = "return_code")
    String returnCode;
    //返回信息	return_msg	是	String(128)	OK
    //当return_code为FAIL时返回信息为错误原因 ，例如
    //
    //签名失败
    //
    //参数格式校验错误
    @XmlElement(name = "return_msg")
    String returnMsg;

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

    public void ok() {
        this.setReturnCode("SUCCESS");
        this.setReturnMsg("OK");
    }

    public void fail(String msg) {
        this.setReturnCode("FAIL");
        this.setReturnMsg(StringUtils.isEmpty(msg) ? "FAIL" : msg);
    }
}
