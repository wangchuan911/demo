package org.welisdoon.flow.module.flow.demo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.welisdoon.flow.module.flow.entity.Stream;
import org.welisdoon.flow.module.template.service.VirtualNode;

import java.util.List;

/**
 * @Classname DemoVirualInstantiated
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2022/2/18 23:01
 */

@Component
@ConditionalOnProperty(prefix = "order-flow.demo", name = "router")
public class DemoVirualInstantiated implements VirtualNode.OnInstantiated {
    @Override
    public List<Stream> apply(Stream templateStream) {
        templateStream.setNodeId(6L);
        templateStream.setFunctionId(null);
        Stream s1 = JSONObject.toJavaObject((JSON) JSONObject.toJSON(templateStream), Stream.class),
                s2 = JSONObject.toJavaObject((JSON) JSONObject.toJSON(templateStream), Stream.class);
        s1.setName(templateStream.getName() + "(简单)1");
        s1.setSeq(1);
        s2.setName(templateStream.getName() + "(简单)2");
        s1.setSeq(2);
        return List.of(s1, s2);
    }
}
