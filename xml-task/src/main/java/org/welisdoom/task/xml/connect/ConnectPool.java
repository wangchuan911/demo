package org.welisdoom.task.xml.connect;

import io.vertx.core.Future;
import org.apache.commons.net.ftp.FTPClient;

/**
 * @Classname ConnectPool
 * @Description TODO
 * @Author Septem
 * @Date 13:53
 */
@Deprecated
public interface ConnectPool<T> {

    @Deprecated
    interface IToken {

    }

    @Deprecated
    Future<T> getConnect(String name, IToken token);
}
