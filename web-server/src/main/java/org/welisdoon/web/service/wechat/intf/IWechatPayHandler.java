package org.welisdoon.web.service.wechat.intf;

import org.welisdoon.web.entity.wechat.WeChatPayOrder;
import org.welisdoon.web.entity.wechat.WeChatRefundOrder;
import org.welisdoon.web.entity.wechat.payment.requset.PayBillRequsetMesseage;
import org.welisdoon.web.entity.wechat.payment.requset.PrePayRequsetMesseage;
import org.welisdoon.web.entity.wechat.payment.requset.RefundRequestMesseage;
import org.welisdoon.web.entity.wechat.payment.requset.RefundResultMesseage;
import org.welisdoon.web.entity.wechat.payment.response.PayBillResponseMesseage;
import org.welisdoon.web.entity.wechat.payment.response.RefundReplyMesseage;
import org.welisdoon.web.entity.wechat.payment.response.RefundResponseMesseage;

public interface IWechatPayHandler {
	PayBillResponseMesseage payCallBack(PayBillRequsetMesseage payBillRequsetMesseage);

	PrePayRequsetMesseage payRequset(WeChatPayOrder weChatPayOrder);

	RefundReplyMesseage refundCallBack(RefundResultMesseage refundResultMesseage);

	RefundRequestMesseage refundRequset(WeChatRefundOrder weChatPayOrder);

}
