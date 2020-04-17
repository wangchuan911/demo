package org.welisdoon.webserver.entity.wechat.push;

import java.util.Arrays;
import java.util.Map;

public abstract class PushMessage {

    /*
    touser	string		是	接收者（用户）的 openid
    template_id	string		是	所需下发的订阅模板id
    data	Object		是	模板内容，格式形如 { "key1": { "value": any }, "key2": { "value": any } }

    * */
    String touser;
    String template_id;
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

    public String getTemplate_id() {
        return template_id;
    }

    public PushMessage setTemplate_id(String template_id) {
        this.template_id = template_id;
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
            if (this.data == null) {
                this.data = Map
                        .ofEntries(Arrays
                                .stream(entrys)
                                .map(stringObjectEntry ->
                                        Map.entry(stringObjectEntry.getKey(),
                                                new Entry().setValue(stringObjectEntry.getValue())))
                                .toArray(Map.Entry[]::new));
            } else {
                this.data = Map
                        .ofEntries(Arrays.stream(
                                new Map.Entry[][]{
                                        this.data.entrySet().stream().toArray(Map.Entry[]::new),
                                        entrys})
                                .flatMap(entries ->
                                        Arrays.stream(entries)
                                ).toArray(Map.Entry[]::new));
            }
        }
        return this;
    }

}
