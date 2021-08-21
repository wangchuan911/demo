package org.welisdoon.flow.module.template.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.welisdoon.flow.module.flow.entity.Stream;
import org.welisdoon.flow.module.flow.entity.StreamStatus;
import org.welisdoon.flow.module.template.annotation.NodeType;
import org.welisdoon.flow.module.template.dao.LinkFunctionDao;
import org.welisdoon.flow.module.template.entity.LinkFunction;
import org.welisdoon.web.common.ApplicationContextProvider;

import java.util.List;
import java.util.function.Predicate;

/**
 * @Classname ConditionNodeService
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/20 15:46
 */
@NodeType(10004)
@Service
public class ConditionAnyNodeService extends ParallelAnyNode {
    LinkFunctionDao linkFunctionDao;

    @Autowired
    public void setLinkFunctionDao(LinkFunctionDao linkFunctionDao) {
        this.linkFunctionDao = linkFunctionDao;
    }

    @Override
    public void start(Stream stream) {
        List<Stream> subStreams = this.getSubStreams(stream);
        stream.setStatusId(StreamStatus.WAIT.statusId());
        this.getStreamDao().put(stream);

        LinkFunction function = linkFunctionDao.get(stream.getFunctionId());
        StreamStatus status;
        boolean hasReady = false;
        Predicate<Stream> predicate;
        for (Stream subStream : subStreams) {
            predicate = ApplicationContextProvider.getBean(function.targetClass());
            status = predicate.test(subStream) ? StreamStatus.READY : StreamStatus.SKIP;
            switch (status) {
                case SKIP:
                    subStream.setStatusId(status.statusId());
                    this.getStreamDao().put(subStream);
                case READY:
                    hasReady = true;
                    AbstractNodeService abstractNodeService = getInstance(subStream.getNodeId());
                    abstractNodeService.start(subStream);

            }

        }

    }

}
