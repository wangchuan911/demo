package org.welisdoon.webserver.common;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.welisdoon.webserver.common.config.AbstractWechatConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class WechatAsyncMeassger {

	private static final Logger logger = LoggerFactory.getLogger(WechatAsyncMeassger.class);
	WebClient webClient;
	Map url;
	String token;
	/*final static Map<Class<?>, WechatAsyncMeassger> MAP = new HashMap(4);*/

	/*public WechatAsyncMeassger(AbstractWechatConfiguration configuration, WebClient webClient) {
		this.webClient = webClient;
		this.url = configuration.getUrls();
		Class<?> cls = configuration.getClass();
		if (MAP.containsKey(cls))
			throw new RuntimeException(String.format("%s is exists", cls.getName()));
		if (cls.getName().indexOf("EnhancerBySpringCGLIB") > 0) {
			cls = cls.getSuperclass();
		}
		MAP.put(cls, this);
	}*/

	public WechatAsyncMeassger(Map urls, WebClient webClient) {
		this.webClient = webClient;
		this.url = urls;
	}

	/*public static WechatAsyncMeassger get(Class<? extends AbstractWechatConfiguration> cls) {
		return MAP.get(cls);
	}*/

	public WechatAsyncMeassger setWebClient(WebClient webClient) {
		this.webClient = webClient;
		return this;
	}

	public void setUrl(String key, String url) {
		this.url.put(key, url);
	}

	public WechatAsyncMeassger setToken(String token) {
		this.token = token;
		return this;
	}

	public String getToken() {
		return token;
	}


	public WechatAsyncMeassger post(String key, Object o) {
		return post(key, o, null, null);
	}

	public WechatAsyncMeassger post(String key, Object o, Handler<HttpResponse<Buffer>> success) {
		return post(key, o, success, null);
	}

	public WechatAsyncMeassger post(String key, Object o, Handler<HttpResponse<Buffer>> success, Handler<Throwable> error) {
		final String url = setUrlParamValue(key, null);
		logger.info(String.format("url:%s[POST]", url));
		logger.info(String.format("param:%s", JsonObject.mapFrom(o)));
		webClient.postAbs(url).sendJson(o, this.handler(url, o, success, error));
		return this;
	}

	public WechatAsyncMeassger get(String key, Map o) {
		return get(key, o, null, null);
	}

	public WechatAsyncMeassger get(String key, Map o, Handler<HttpResponse<Buffer>> success) {
		return get(key, o, success, null);
	}

	public WechatAsyncMeassger get(String key, Map o, Handler<HttpResponse<Buffer>> success, Handler<Throwable> error) {
		final String url = setUrlParamValue(key, o);
		logger.debug(String.format("url:%s[GET]", url));
		logger.debug(String.format("param:%s", JsonObject.mapFrom(o)));
		webClient.getAbs(url).send(this.handler(url, o, success, error));
		return this;
	}

	Handler<AsyncResult<HttpResponse<Buffer>>> handler(String url, Object o, Handler<HttpResponse<Buffer>> success, Handler<Throwable> error) {
		return httpResponseAsyncResult -> {
			String response;
			if (httpResponseAsyncResult.succeeded()) {
				if (success != null) {
					success.handle(httpResponseAsyncResult.result());
				}
				response = httpResponseAsyncResult.result().bodyAsString();
			} else {
				if (error != null) {
					error.handle(httpResponseAsyncResult.cause());
				}
				response = httpResponseAsyncResult.cause().getMessage();
			}
			logger.debug(String.format("response:%s", response));
		};
	}

	String setUrlParamValue(String key, Map map) {
		StringBuilder url = new StringBuilder(this.url.get(key).toString());
		if (MapUtils.isNotEmpty(map)) {
			Set<Map.Entry> s = map.entrySet();
			s.stream().filter(entry -> entry.getValue() != null).forEach(o -> {
				replace(url, String.format("{{%s}}", o.getKey().toString()), o.getValue().toString());
			});
		}
		replace(url, "{{ACCESS_TOKEN}}", this.token);
		return url.toString();
	}

	void replace(StringBuilder url, String colmn, String value) {
		int idx;
		if ((idx = url.indexOf(colmn)) > 0) {
			url.replace(idx, idx + colmn.length(), value);
		}
	}
}
