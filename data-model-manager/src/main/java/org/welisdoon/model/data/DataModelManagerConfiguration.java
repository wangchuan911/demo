package org.welisdoon.model.data;

import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Configuration;
import org.welisdoon.common.data.IData;
import org.welisdoon.model.data.annotations.Input;
import org.welisdoon.model.data.annotations.Model;
import org.welisdoon.web.config.VertxInSpringConfiguration;


@Configuration
@AutoConfigureAfter(VertxInSpringConfiguration.class)
public class DataModelManagerConfiguration {


    @Autowired
    public void setReflections(Reflections reflections) {
        reflections.getTypesAnnotatedWith(Model.class).stream().forEach(aClass -> {
            System.out.println(aClass.getAnnotation(Model.class));
            if (IData.class.isAssignableFrom(aClass))
                IData.add(aClass.getAnnotation(Model.class).value(), (Class<? extends IData>) aClass);
        });
        reflections.getTypesAnnotatedWith(Input.class).stream().forEach(aClass -> {
            if (IData.class.isAssignableFrom(aClass))
                IData.add(aClass.getAnnotation(Input.class).value(), (Class<? extends IData>) aClass);
        });
    }

}
