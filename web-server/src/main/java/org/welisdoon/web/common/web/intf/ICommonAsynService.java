package org.welisdoon.web.common.web.intf;


import io.vertx.codegen.annotations.ProxyGen;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.*;
import org.welisdoon.web.common.web.Requset;
import org.welisdoon.web.common.web.Response;

@ProxyGen
@VertxGen
public interface ICommonAsynService {

    void callService(Requset requset, Handler<AsyncResult<Response>> outputBodyHandler);

}
