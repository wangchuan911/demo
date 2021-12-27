package org.welisdoon.web.service.wechat.intf;

import org.welisdoon.web.entity.wechat.WeChatMarketTransferOrder;
import org.welisdoon.web.entity.wechat.WeChatPayOrder;
import org.welisdoon.web.entity.wechat.WeChatRefundOrder;
import org.welisdoon.web.entity.wechat.payment.requset.*;
import org.welisdoon.web.entity.wechat.payment.response.PayBillResponseMesseage;
import org.welisdoon.web.entity.wechat.payment.response.RefundReplyMesseage;

public interface IWechatPayHandler {
	PayBillResponseMesseage payCallBack(PayBillRequsetMesseage payBillRequsetMesseage);

	PrePayRequsetMesseage payRequset(WeChatPayOrder weChatPayOrder);

	RefundReplyMesseage refundCallBack(RefundResultMesseage refundResultMesseage);

	RefundRequestMesseage refundRequset(WeChatRefundOrder weChatPayOrder);

	MarketTransferRequsetMesseage marketTransferRequset(WeChatMarketTransferOrder weChatPayOrder);

}
