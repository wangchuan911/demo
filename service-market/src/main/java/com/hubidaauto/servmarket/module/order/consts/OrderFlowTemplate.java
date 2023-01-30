package com.hubidaauto.servmarket.module.order.consts;

import java.util.Arrays;

/**
 * @Classname OrderFlowTemplate
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/24 00:44
 */
public enum OrderFlowTemplate {
    CREATE_ORDER(1), SIGN_IN(2), SERVICE_CHOICE(3), APPIONT_WORKER(4);

    private long templateId;

    OrderFlowTemplate(long templateId) {
        this.templateId = templateId;
    }

    public long getTemplateId() {
        return templateId;
    }

    public static OrderFlowTemplate getInstance(long templateId) {
        return Arrays.stream(OrderFlowTemplate.values()).filter(orderFlowTemplate -> orderFlowTemplate.templateId == templateId).findFirst().get();
    }
}
