package org.welisdoom.task.xml.entity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.vertx.core.Future;
import org.apache.commons.collections4.MapUtils;
import org.springframework.util.StringUtils;
import org.welisdoom.task.xml.annotations.Attr;
import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.consts.MagicKey;
import org.welisdoom.task.xml.intf.type.Initialize;
import org.welisdoom.task.xml.intf.type.UnitType;
import org.welisdoon.common.ObjectUtils;
import org.xml.sax.Attributes;

import java.util.HashMap;

/**
 * @Classname Values
 * @Description TODO
 * @Author Septem
 * @Date 19:24
 */
@Tag(value = "value", parentTagTypes = Initialize.class, desc = "初始化值")
@Attr(name = "id", desc = "唯一标识", require = true)
@Attr(name = "value", desc = "值", require = true)
public class Value extends Unit implements Initialize {

    @Override
    public Unit attr(Attributes attributes) {
        return super.attr(attributes);
    }

    public String getValue() {
        return MapUtils.getString(attributes, "value", getChild(Content.class).stream().findFirst().orElse(new Content().setContent("")).getContent());
    }

    /*public static String getValue(Task task, String name) {
        return Initialization.getInstance(task)
                .getChildren(Value.class)
                .stream()
                .filter(value -> Objects.equals(name, value.getId()))
                .findFirst().orElse(new Value()).getValue();

    }*/

    @Override
    protected Future start(TaskInstance data, Object preUnitResult) {
        try {
            HashMap map = (HashMap) ObjectUtils.getMapValueOrNewSafe(data.getBus(), MagicKey.VALUES, HashMap::new);
            String value = UnitType.textFormat(data, getValue());
            log("value:" + value);
            map.put(this.id, value);
            return Future.succeededFuture();
        } catch (Throwable throwable) {
            return Future.failedFuture(throwable);
        }
    }


    public static Object get(Object val, String longKey) {
        if (val == null) return null;
        if (val instanceof JSON) {
            for (String s : longKey.split("[\\.\\[\\]]+")) {
                if (val == null) return null;
                if (StringUtils.isEmpty(s)) continue;
                if (val instanceof JSONObject) {
                    val = ((JSONObject) val).get(s);
                } else if (val instanceof JSONArray) {
                    val = ((JSONArray) val).get(Integer.valueOf(s));
                } else {
                    val = null;
                }
            }
            return val;
        }
        if (val instanceof CharSequence) {
            return get(JSON.parse(val.toString()), longKey);
        }
        return get(JSON.toJSON(val), longKey);
    }

    /*public static void main(String[] args) {
        System.out.println(get("{\"A\":{\"b\":[{\"v\":1}]}}", "A.b[0].v").getClass());
    }*/
}
