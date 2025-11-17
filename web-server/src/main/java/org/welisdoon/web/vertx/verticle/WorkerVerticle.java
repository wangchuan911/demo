package org.welisdoon.web.vertx.verticle;

import io.vertx.core.ThreadingModel;
import org.springframework.stereotype.Component;
import org.welisdoon.web.vertx.annotation.Verticle;

@Component("workerVerticle")
@Verticle(mode = ThreadingModel.WORKER)
public class WorkerVerticle extends AbstractMyVerticle {

}
