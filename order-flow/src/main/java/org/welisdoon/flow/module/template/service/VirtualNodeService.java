package org.welisdoon.flow.module.template.service;

import org.springframework.stereotype.Service;
import org.welisdoon.flow.module.flow.entity.Stream;
import org.welisdoon.flow.module.template.annotation.NodeType;

import java.util.List;

/**
 * @Classname VirtualNodeService
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/20 16:22
 */

@NodeType(10005)
@Service
public class VirtualNodeService extends SimpleNodeService {

    @Override
    public void start(Stream stream) {
        throw new RuntimeException("stream not allow link VirtualNode!");
    }

    @Override
    public void finish(Stream stream) {
        throw new RuntimeException("stream not allow link VirtualNode!");
    }

    @Override
    public void createStream(Stream stream) {
        super.createStream(stream);
    }
}
