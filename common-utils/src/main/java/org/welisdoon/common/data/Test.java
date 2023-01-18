package org.welisdoon.common.data;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.fastjson.parser.ParserConfig;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.net.URL;
import java.util.Collection;
import java.util.HashSet;

public class Test {
    public static void main(String[] args) {
        final Collection<URL> collectUrl = new HashSet<>();
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
        collectUrl.addAll(ClasspathHelper.forPackage(Test.class.getPackageName()));
        ParserConfig.getGlobalInstance().addAccept(String.format("%s.", Test.class.getPackageName()));
        configurationBuilder.setUrls(collectUrl);
        IData.scan(new Reflections(configurationBuilder));
        String str = JSON.toJSONString(new ASD());
        System.out.println(str);
        System.out.println(IData.jsonToDataEntity(str).toString());

    }

    @IData.DataModelAliases(@IData.DataModelAlias("hehe"))
    public static class ASD extends IData.DataObject<String> {
    }
}
