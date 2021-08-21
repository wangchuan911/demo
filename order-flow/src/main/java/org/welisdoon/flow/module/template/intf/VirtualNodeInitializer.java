package org.welisdoon.flow.module.template.intf;

import org.welisdoon.flow.module.flow.entity.Stream;
import org.welisdoon.flow.module.template.entity.Link;

import java.util.List;

/**
 * @Classname VirtualNodeProcessor
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/21 00:58
 */
public interface VirtualNodeInitializer {
    List<Stream> createStream(Stream templateStream);
}
