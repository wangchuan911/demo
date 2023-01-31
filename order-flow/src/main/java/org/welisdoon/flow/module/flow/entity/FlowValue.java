package org.welisdoon.flow.module.flow.entity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.util.TypeUtils;
import com.sun.istack.NotNull;
import org.springframework.util.StringUtils;
import org.welisdoon.flow.module.template.entity.LinkValue;

import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/**
 * @Classname FlowValue
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/9/14 10:03
 */
public class FlowValue extends LinkValue {
    Long flowId;
    FlowJSONValue jsonValue;

    public Long getFlowId() {
        return flowId;
    }

    public FlowValue setFlowId(Long flowId) {
        this.flowId = flowId;
        return this;
    }

    public FlowValue() {
    }

    public FlowValue(Long flowId) {
        this.flowId = flowId;
    }

    @Override
    public String getValue() {
        if (jsonValue != null && jsonValue.change) {
            setValue(jsonValue.current.toJSONString());
            jsonValue.change = false;
        }
        return super.getValue();
    }

    @Override
    public void setValue(String value) {
        if (Objects.equals(getValue(), value)) {
            return;
        }
        super.setValue(value);
        if (jsonValue != null) {
            jsonValue.change = false;
            jsonValue.current.clear();
            jsonValue.current.putAll(JSON.parseObject(value));
        }

    }

    public FlowJSONValue jsonValue() {
        return this.jsonValue == null ? (this.jsonValue = new FlowJSONValue(getValue())) : this.jsonValue;
    }

    public static FlowJSONValue emptyFlowJSONValue() {
        return new FlowJSONValue();
    }

    public static class FlowJSONValue {
        JSONObject current;
        boolean change = false;

        FlowJSONValue() {
            current = new JSONObject();
        }

        FlowJSONValue(JSONObject object) {
            current = object != null ? object : new JSONObject();
        }

        FlowJSONValue(String jsonString) {
            current = StringUtils.isEmpty(jsonString) ? new JSONObject() : JSON.parseObject(jsonString);
        }

        public <T> T getObject(String key, Class<T> clazz) {
            return current.getObject(key, clazz);
        }

        public <T> T getObject(String key, Type type) {
            return current.getObject(key, type);
        }

        public <T> T getObject(String key, TypeReference typeReference) {
            return current.getObject(key, typeReference);
        }

        public Boolean getBoolean(String key) {
            return current.getBoolean(key);
        }

        public byte[] getBytes(String key) {
            return current.getBytes(key);
        }

        public boolean getBooleanValue(String key) {
            return current.getBooleanValue(key);
        }

        public Byte getByte(String key) {
            return current.getByte(key);
        }

        public byte getByteValue(String key) {
            return current.getByteValue(key);
        }

        public Short getShort(String key) {
            return current.getShort(key);
        }

        public short getShortValue(String key) {
            return current.getShortValue(key);
        }

        public Integer getInteger(String key) {
            return current.getInteger(key);
        }

        public int getIntValue(String key) {
            return current.getIntValue(key);
        }

        public Long getLong(String key) {
            return current.getLong(key);
        }

        public long getLongValue(String key) {
            return current.getLongValue(key);
        }

        public Float getFloat(String key) {
            return current.getFloat(key);
        }

        public float getFloatValue(String key) {
            return current.getFloatValue(key);
        }

        public Double getDouble(String key) {
            return current.getDouble(key);
        }

        public double getDoubleValue(String key) {
            return current.getDoubleValue(key);
        }

        public BigDecimal getBigDecimal(String key) {
            return current.getBigDecimal(key);
        }

        public BigInteger getBigInteger(String key) {
            return current.getBigInteger(key);
        }

        public String getString(String key) {
            return current.getString(key);
        }

        public Date getDate(String key) {
            return current.getDate(key);
        }

        public Object getSqlDate(String key) {
            return current.getSqlDate(key);
        }

        public Object getTimestamp(String key) {
            return current.getTimestamp(key);
        }

        public Object put(String key, Object value) {
            this.change = true;
            return current.put(key, value);
        }

        public Object get(Object key) {
            return this.current.get(key);
        }

        public boolean isEmpty() {
            return this.current.isEmpty();
        }

        public boolean containsKey(Object key) {
            return this.current.containsKey(key);
        }

        public boolean containsValue(Object value) {
            return this.current.containsValue(value);
        }

        public Object getOrDefault(Object key, Object defaultValue) {
            return this.current.getOrDefault(key, defaultValue);
        }

        public JSONObject getJSONObject(String key) {
            return this.current.getJSONObject(key);
        }

        public JSONArray getJSONArray(String key) {
            return this.current.getJSONArray(key);
        }
    }
}
