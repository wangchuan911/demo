package org.welisdoon.webserver.common;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JAXBUtils {
    final static Map<Class<?>, JAXBContext> JAXB_CONTEXT_MAP;

    static {
        JAXB_CONTEXT_MAP = new ConcurrentHashMap<>();
    }

    public static String toXML(Object obj) throws JAXBException {
        JAXBContext context;
        Class<?> clazz=obj.getClass();
        if(JAXB_CONTEXT_MAP.containsKey(clazz)){
            context=JAXB_CONTEXT_MAP.get(clazz);
        }else{
            context=JAXBContext.newInstance(clazz);
            JAXB_CONTEXT_MAP.put(clazz,context);
        }
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");// //编码格式
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);// 是否格式化生成的xml串
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, false);// 是否省略xm头声明信息
        StringWriter writer = new StringWriter();
        marshaller.marshal(obj, writer);
        String xml = writer.toString();
        return xml;
    }

    /**
     * @param xml       传入的XML报文
     * @param valueType 需要返回的cls
     * @return T 解析完成对象
     * @throws JAXBException 参数
     * @Description XML转化为对象
     */
    @SuppressWarnings("unchecked")
    public static <T> T fromXML(String xml, Class<T> valueType) throws JAXBException {
        JAXBContext context;
        Class<?> clazz=valueType;
        if(JAXB_CONTEXT_MAP.containsKey(clazz)){
            context=JAXB_CONTEXT_MAP.get(clazz);
        }else{
            context=JAXBContext.newInstance(clazz);
            JAXB_CONTEXT_MAP.put(clazz,context);
        }
        Unmarshaller unmarshaller = context.createUnmarshaller();
        return (T) unmarshaller.unmarshal(new StringReader(xml));
    }
/*---------------------
    作者：醉蝶依
    来源：CSDN
    原文：https://blog.csdn.net/qq_27949963/article/details/80915099
    版权声明：本文为博主原创文章，转载请附上博文链接！
*/
}
