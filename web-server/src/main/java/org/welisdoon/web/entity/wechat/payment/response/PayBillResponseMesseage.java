package org.welisdoon.web.entity.wechat.payment.response;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "xml")
@XmlAccessorType(XmlAccessType.FIELD)
public class PayBillResponseMesseage {
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
}
