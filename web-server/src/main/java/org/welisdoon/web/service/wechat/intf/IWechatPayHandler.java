package org.welisdoon.web.service.wechat.intf;

import io.vertx.core.Future;
import org.welisdoon.web.entity.wechat.WeChatMarketTransferOrder;
import org.welisdoon.web.entity.wechat.WeChatPayOrder;
import org.welisdoon.web.entity.wechat.WeChatRefundOrder;
import org.welisdoon.web.entity.wechat.payment.requset.*;
import org.welisdoon.web.entity.wechat.payment.response.PayBillResponseMesseage;
import org.welisdoon.web.entity.wechat.payment.response.RefundReplyMesseage;

public interface IWechatPayHandler {
	default Future<PayBillResponseMesseage> payCallBack(PayBillRequsetMesseage payBillRequsetMesseage){
		return Future.failedFuture("不支持的操作");
	}

	default Future<PrePayRequsetMesseage> payRequset(WeChatPayOrder weChatPayOrder){
		return Future.failedFuture("不支持的操作");
	}

	default Future<RefundReplyMesseage> refundCallBack(RefundResultMesseage refundResultMesseage){
		return Future.failedFuture("不支持的操作");
	}

	default Future<RefundRequestMesseage> refundRequset(WeChatRefundOrder weChatPayOrder){
		return Future.failedFuture("不支持的操作");
	}

	default Future<MarketTransferRequsetMesseage> marketTransferRequset(WeChatMarketTransferOrder weChatPayOrder) {
		return Future.failedFuture("不支持的操作");
	}

}
