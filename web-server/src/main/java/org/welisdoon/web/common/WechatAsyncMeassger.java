package org.welisdoon.web.common;

import com.alibaba.fastjson.JSONObject;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import org.apache.commons.collections4.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;


public class WechatAsyncMeassger {

    private static final Logger logger = LoggerFactory.getLogger(WechatAsyncMeassger.class);
    WebClient webClient;
    Map url;
    String accessToken;
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

    public WechatAsyncMeassger setAccessToken(String accessToken) {
        this.accessToken = accessToken;
        return this;
    }

    public String getAccessToken() {
        return accessToken;
    }


    public Future<HttpResponse<Buffer>> post(String key, Object o) {
        final String url = setUrlParamValue(key, null);
        logger.info(String.format("url:%s[POST]", url));
        String param = JSONObject.toJSONString(o);
        logger.info(String.format("param:%s", param));
        return webClient.postAbs(url)
                /*.putHeader(HttpHeaders.CONTENT_TYPE.toString(), "application/json")*/
                .sendBuffer(Buffer.buffer(param))
                .compose(bufferHttpResponse -> {
                    logger.debug(String.format("response:%s", bufferHttpResponse.body()));
                    return Future.succeededFuture(bufferHttpResponse);
                }, throwable -> {
                    logger.error(throwable.getMessage(), throwable);
                    return Future.failedFuture(throwable);
                });
    }


    public Future<HttpResponse<Buffer>> get(String key, Map o) {
        final String url = setUrlParamValue(key, o);
        logger.debug(String.format("url:%s[GET]", url));
        return webClient.getAbs(url).send()
                .compose(bufferHttpResponse -> {
                    logger.debug(String.format("response:%s", bufferHttpResponse.body()));
                    return Future.succeededFuture(bufferHttpResponse);
                }, throwable -> {
                    logger.error(throwable.getMessage(), throwable);
                    return Future.failedFuture(throwable);
                });
    }

    String setUrlParamValue(String key, Map map) {
        StringBuilder url = new StringBuilder(this.url.get(key).toString());
        if (MapUtils.isNotEmpty(map)) {
            Set<Map.Entry> s = map.entrySet();
            s.stream().filter(entry -> entry.getValue() != null).forEach(o -> {
                replace(url, String.format("{{%s}}", o.getKey().toString()), o.getValue().toString());
            });
        }
        replace(url, "{{ACCESS_TOKEN}}", this.accessToken);
        return url.toString();
    }

    void replace(StringBuilder url, String colmn, String value) {
        int idx;
        if ((idx = url.indexOf(colmn)) > 0) {
            url.replace(idx, idx + colmn.length(), value);
        }
    }

    public WebClient getWebClient() {
        return webClient;
    }
}
