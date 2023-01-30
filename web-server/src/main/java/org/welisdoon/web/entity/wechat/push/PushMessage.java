package org.welisdoon.web.entity.wechat.push;

import com.alibaba.fastjson.annotation.JSONField;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;

public abstract class PushMessage {

    /*
    touser	string		是	接收者（用户）的 openid
    template_id	string		是	所需下发的订阅模板id
    data	Object		是	模板内容，格式形如 { "key1": { "value": any }, "key2": { "value": any } }

    * */
    String touser;
    String templateId;
    Map<String, Entry> data;

    public static class Entry {
        Object value;

        public Object getValue() {
            return value;
        }

        public Entry setValue(Object value) {
            this.value = value;
            return this;
        }
    }

    public String getTouser() {
        return touser;
    }

    public PushMessage setTouser(String touser) {
        this.touser = touser;
        return this;
    }

    @JSONField(name = "template_id")
    public String getTemplateId() {
        return templateId;
    }

    public PushMessage setTemplateId(String templateId) {
        this.templateId = templateId;
        return this;
    }

    public Map<String, Entry> getData() {
        return data;
    }

    public PushMessage setData(Map<String, Entry> data) {
        this.data = data;
        return this;
    }

    public PushMessage addDatas(Map.Entry<String, Object>... entrys) {
        if (entrys != null && entrys.length > 0) {
            Map.Entry<String, Entry>[] entries = Arrays
                    .stream(entrys)
                    .map(stringObjectEntry ->
                            Map.entry(stringObjectEntry.getKey(),
                                    new Entry().setValue(stringObjectEntry.getValue())))
                    .toArray(Map.Entry[]::new);
            if (this.data == null) {
                /*this.data = Map
                        .ofEntries(Arrays
                                .stream(entrys)
                                .map(stringObjectEntry ->
                                        Map.entry(stringObjectEntry.getKey(),
                                                new Entry().setValue(stringObjectEntry.getValue())))
                                .toArray(Map.Entry[]::new));*/
                this.data = Map
                        .ofEntries(entries);
            } else {
                /*this.data = Map
                        .ofEntries(Arrays.stream(
                                new Map.Entry[][]{
                                        this.data.entrySet().stream().toArray(Map.Entry[]::new),
                                        entrys})
                                .flatMap(entries ->
                                        Arrays.stream(entries)
                                ).toArray(Map.Entry[]::new));*/

                Stream<Map.Entry<String, Entry>>[] entryStreams = new Stream[]{
                        this.data.entrySet().stream(),
                        Arrays.stream(entries)
                };
                this.data = Map
                        .ofEntries(Arrays.stream(entryStreams)
                                .flatMap(entries1 -> entries1)
                                .toArray(Map.Entry[]::new));
            }
        }
        return this;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PushMessage{");
        sb.append("touser='").append(touser).append('\'');
        sb.append(", template_id='").append(templateId).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
