package org.welisdoon.common.object.wrapper.demo;

import org.welisdoon.common.data.BaseCondition;
import org.welisdoon.common.object.wrapper.AbstractDataAccessObject;
import org.welisdoon.common.object.wrapper.IDataAccessObject;
import org.welisdoon.common.object.wrapper.Prepare;

import java.util.List;
import java.util.Map;

/**
 * @Classname DemoDaoEntity
 * @Description TODO
 * @Author Septem
 * @Date 15:38
 */

public class DemoTypeAEntity extends AbstractDataAccessObject implements IDemoTypeAEntity {

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

        IDataAccessObject.page(DemoTypeAEntity.class, Map.of("t1column1", 1), new BaseCondition.Page(1, 1));
        IDataAccessObject.page(DemoTypeBEntity.class, Map.of("t5_3column2", 1, "t7_2column1", 1), new BaseCondition.Page(1, 1));


        IDataAccessObject.get(DemoTypeAEntity.class, 1);
        IDataAccessObject.get(DemoTypeBEntity.class, 1);


    }

    @Override
    public String getValueA() {
        return null;
    }
}
