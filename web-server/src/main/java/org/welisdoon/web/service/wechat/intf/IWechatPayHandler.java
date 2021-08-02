package org.welisdoon.web.service.wechat.intf;

import org.welisdoon.web.entity.wechat.WeChatPayOrder;
import org.welisdoon.web.entity.wechat.payment.requset.PayBillRequsetMesseage;
import org.welisdoon.web.entity.wechat.payment.requset.PrePayRequsetMesseage;
import org.welisdoon.web.entity.wechat.payment.response.PayBillResponseMesseage;

public interface IWechatPayHandler {
	PayBillResponseMesseage payBillCallBack(PayBillRequsetMesseage payBillRequsetMesseage);

	PrePayRequsetMesseage prePayRequset(WeChatPayOrder weChatPayOrder);

}
