package org.welisdoon.flow.module.template.service;

import org.welisdoon.flow.module.flow.entity.Stream;
import org.welisdoon.flow.module.flow.entity.StreamStatus;
import org.welisdoon.web.common.ApplicationContextProvider;

import java.util.List;

/**
 * @Classname ComplexNodeService
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/20 16:34
 */
public abstract class AbstractComplexNode extends AbstractNode {
    public SubStreamStatusCount countStreamStatus(List<Stream> stream) {
        int WAIT = 0, READY = 0, SKIP = 0, FUTURE = 0, COMPLETE = 0;
        for (Stream subStream : stream) {
            switch (StreamStatus.getInstance(subStream.getStatusId())) {
                case READY:
                    READY++;
                    break;
                case SKIP:
                    SKIP++;
                    break;
                case WAIT:
                    WAIT++;
                    break;
                case FUTURE:
                    FUTURE++;
                    break;
                case COMPLETE:
                    COMPLETE++;
                    break;
            }
        }
        return new SubStreamStatusCount(WAIT, READY, SKIP, FUTURE, COMPLETE);
    }

    @Override
    public boolean skip(Stream stream) {
        if (super.skip(stream)){
            for (Stream subStream : this.getSubStreams(stream)) {
                AbstractNode.getInstance(subStream.getNodeId()).skip(stream);
            }
            return true;
        }
        return false;
    }




    @Override
    public void dismiss(Stream stream) {
        List<Stream> subStreams = this.getSubStreams(stream);
        for (Stream subStream : subStreams) {
            getInstance(subStream.getNodeId()).dismiss(subStream);
        }
        super.dismiss(stream);

    }

    @Override
    public void undo(Stream stream) {
        StreamStatus status = StreamStatus.getInstance(stream.getStatusId());
        switch (status) {
            case COMPLETE:
            case SKIP:
                List<Stream> subStreams = this.getSubStreams(stream);
                if (isVirtual(stream)) {
                    ApplicationContextProvider.getBean(VirtualNode.class).rollback(stream);
                } else {
                    for (Stream subStream : subStreams) {
                        getInstance(subStream.getNodeId()).undo(subStream, false);
                    }
                    this.setStreamStatus(stream, StreamStatus.FUTURE);
                }
                Stream superStream = getSuperStream(stream);
                if (superStream != null)
                    getInstance(superStream.getNodeId()).undo(superStream, true);
            default:
        }
    }
}
