package org.welisdoon.common.data;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.net.URL;
import java.util.Collection;
import java.util.HashSet;

public class Test {
    public static void main(String[] args) {
        ASD a = new ASD();
        a.setDataMarker(ASDF.ASD);

        ParserConfig.getGlobalInstance().addAccept(String.format("%s.", ASD.class.getPackageName()));
        IData.add( a);
        String str = JSON.toJSONString(a, SerializerFeature.WriteClassName);
        System.out.println(str);
        System.out.println(IData.jsonToDataEntity(ASDF.class,str).toString());

    }

    public static class ASD extends IData.DataObject<String, ASDF> {
    }

    enum ASDF implements IData.IDataMarker {
        ASD;
    }
}
