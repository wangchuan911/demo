package org.welisdoon.flow.module.flow.service;

import org.springframework.stereotype.Component;
import org.welisdoon.flow.module.flow.entity.Stream;
import org.welisdoon.flow.module.template.intf.VirtualNodeInitializer;

import java.util.List;

/**
 * @Classname Temp
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/21 13:33
 */
@Component
public class Temp implements VirtualNodeInitializer {
    @Override
    public List<Stream> createStream(Stream templateStream) {
        templateStream.setNodeId(6L);
        templateStream.setFunctionId(null);
        return List.of(templateStream);
    }
}
