package org.welisdoon.flow.module.template.service;

import org.welisdoon.flow.module.flow.entity.FlowCondition;
import org.welisdoon.flow.module.flow.entity.Stream;
import org.welisdoon.flow.module.flow.entity.StreamStatus;

import java.util.List;

/**
 * @Classname ComplexNodeService
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/20 16:34
 */
public abstract class AbstractComplexNodeService extends AbstractNodeService {
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

    public void skip(Stream stream) {
        this.setStreamStatus(stream, StreamStatus.SKIP);
        for (Stream subStream : this.getSubStreams(stream)) {
            switch (StreamStatus.getInstance(subStream.getStatusId())) {
                case FUTURE:
                case READY:
                    this.skip(subStream);
                    break;
            }
        }
    }

    public void skip(List<Stream> streams) {
        for (Stream subStream : streams) {
            switch (StreamStatus.getInstance(subStream.getStatusId())) {
                case FUTURE:
                case READY:
                    this.skip(subStream);
                    break;
            }
        }
    }

}
