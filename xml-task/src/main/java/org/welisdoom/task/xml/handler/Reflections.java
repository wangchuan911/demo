package org.welisdoom.task.xml.handler;

import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.welisdoom.task.xml.XmlTaskApplication;

import java.net.URL;
import java.util.Collection;
import java.util.HashSet;

/**
 * @Classname Reflections
 * @Description TODO
 * @Author Septem
 * @Date 17:41
 */
public class Reflections {
    static org.reflections.Reflections instance;

    protected static void addPathToReflections(Collection<URL> CollectUrl, Class clz) {
        CollectUrl.addAll(ClasspathHelper.forPackage(clz.getPackageName()));
//        ParserConfig.getGlobalInstance().addAccept(String.format("%s.", clz.getPackageName()));
    }

    static {
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
        {
            final Collection<URL> CollectUrl = new HashSet<>();
            addPathToReflections(CollectUrl, XmlTaskApplication.class);
//            logger.warn(CollectUrl.toString());
            configurationBuilder.setUrls(CollectUrl);
            configurationBuilder.setInputsFilter(s -> s.toLowerCase().endsWith(".class") || s.toLowerCase().endsWith(".java"));
        }
        instance = new org.reflections.Reflections(configurationBuilder);
    }

    public static org.reflections.Reflections getInstance() {
        return instance;
    }
}
