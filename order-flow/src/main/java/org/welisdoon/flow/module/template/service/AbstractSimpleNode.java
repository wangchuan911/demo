package org.welisdoon.flow.module.template.service;

import org.welisdoon.flow.module.flow.entity.Stream;
import org.welisdoon.flow.module.flow.entity.StreamStatus;
import org.welisdoon.web.common.ApplicationContextProvider;

/**
 * @Classname AbstractSimpleNode
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/20 16:32
 */
public abstract class AbstractSimpleNode extends AbstractNode {

    @Override
    protected void undo(Stream stream, boolean propagation) {
        StreamStatus status = StreamStatus.getInstance(stream.getStatusId());
        if (status == StreamStatus.FUTURE) return;
        if (!propagation) {
            if (isVirtual(stream)) {
                ApplicationContextProvider.getBean(VirtualNode.class).rollback(stream);
                return;
            }
        }

        this.setStreamStatus(stream, StreamStatus.FUTURE);
        if (propagation) {
            Stream superStream = this.getSuperStream(stream);
            getInstance(superStream.getNodeId()).undo(superStream, true);
        }

    }

    @Override
    public void undo(Stream stream) {
        if (isVirtual(stream)) {
            ApplicationContextProvider.getBean(VirtualNode.class).rollback(stream);
            return;
        }
        this.undo(stream, true);
    }
}
