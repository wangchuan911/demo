package org.welisdoon.webserver.service.wechat.intf;

import org.welisdoon.webserver.entity.wechat.WeChatPayOrder;
import org.welisdoon.webserver.entity.wechat.payment.requset.PayBillRequsetMesseage;
import org.welisdoon.webserver.entity.wechat.payment.requset.PrePayRequsetMesseage;
import org.welisdoon.webserver.entity.wechat.payment.response.PayBillResponseMesseage;

public interface IWechatPayHandler {
	PayBillResponseMesseage payBillCallBack(PayBillRequsetMesseage payBillRequsetMesseage);

	PrePayRequsetMesseage prePayRequset(WeChatPayOrder weChatPayOrder);

}
