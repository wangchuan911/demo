package org.welisdoon.webserver.common.web;

import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractBaseService<T> {

	final public Logger logger = LoggerFactory.getLogger(this.getClass());

	public abstract Object handle(int exeCode, Map map);

	public Object handle(int exeCode, T map) {
		throw new RuntimeException();
	}

	@PostConstruct
	public void init() throws Throwable {

	}

	public static <K> K mapToObject(Map params, Class<K> type) {
		return JsonObject.mapFrom(params).mapTo(type);
	}

	public static <T> void removeMapKeys(Map<T, ?> map, T... keys) {
		Arrays.stream(keys).forEach(t -> map.remove(t));
	}

	public static <T> Map<T, ?> mapSpliter(Map<T, ?> map, Class<? extends Map> targetMapClass, T... keys) {
		int len;
		if ((len = keys.length) == 0) return null;
		if (targetMapClass == Map.class) {
			Map.Entry<T, ?>[] entries = Arrays.stream(keys)
					.filter(key -> map.get(key) != null)
					.map(key -> Map.entry(key, map.remove(key)))
					.toArray(Map.Entry[]::new);
			return Map.ofEntries(entries);
		} else {
			T key;
			Map<T, Object> hashMap = new HashMap<>(len + 1, 1.0f);
			for (int i = 0; i < len; i++) {
				key = keys[i];
				hashMap.put(key, map.remove(key));
			}
			return hashMap;
		}
	}

	public static Logger logger(Class<?> type) {
		return LoggerFactory.getLogger(type);
	}

}
