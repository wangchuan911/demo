package org.welisdoon.webserver.common.web.intf;


import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.*;
import org.springframework.util.StringUtils;
import org.welisdoon.webserver.common.web.AsyncProxyUtils;
import org.welisdoon.webserver.common.web.Requset;
import org.welisdoon.webserver.common.web.Response;

@ProxyGen
@VertxGen
public interface ICommonAsynService {

    void callService(Requset requset, Handler<AsyncResult<Response>> outputBodyHandler);

}
