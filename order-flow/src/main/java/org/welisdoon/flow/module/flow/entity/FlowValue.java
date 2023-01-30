package org.welisdoon.flow.module.flow.entity;

import org.welisdoon.flow.module.template.entity.LinkValue;

/**
 * @Classname FlowValue
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/9/14 10:03
 */
public class FlowValue extends LinkValue {
    Long flowId;

    public Long getFlowId() {
        return flowId;
    }

    public FlowValue setFlowId(Long flowId) {
        this.flowId = flowId;
        return this;
    }


}
