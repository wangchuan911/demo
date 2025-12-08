package org.welisdoon.common.object.wrapper;

import org.welisdoon.common.data.BaseCondition;

import java.util.HashMap;
import java.util.List;

/**
 * @Classname DemoDaoEntity
 * @Description TODO
 * @Author Septem
 * @Date 15:38
 */

public class DemoTypeAEntity implements IDemoTypeAEntity {
    @Override
    public <T> T loadRemote(String key) {
        return null;
    }

    @Override
    public <T> List<T> loadsRemote(String key) {
        return null;
    }

    @Override
    public <T> T loadLocal(String key) {
        return null;
    }

    @Override
    public <T> List<T> loadsLocal(String key) {
        return null;
    }

    public static void main(String[] args) {
//        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
//        configurationBuilder.setUrls(ClasspathHelper.forPackage(DemoDaoEntity.class.getPackageName()));
//        configurationBuilder.setInputsFilter(s -> s.toLowerCase().endsWith(".class") || s.toLowerCase().endsWith(".java"));
//        IDataAccessObject.initialization(new Reflections(configurationBuilder));
        IDataAccessObject.page(DemoTypeAEntity.class, new HashMap<>(), new BaseCondition.Page(1, 1));
    }
}
