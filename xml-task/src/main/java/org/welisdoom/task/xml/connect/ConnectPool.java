package org.welisdoom.task.xml.connect;

import io.vertx.core.Future;
import org.apache.commons.net.ftp.FTPClient;

/**
 * @Classname ConnectPool
 * @Description TODO
 * @Author Septem
 * @Date 13:53
 */
public interface ConnectPool<T> {
    interface IToken {

    }

    Future<T> getConnect(String name, IToken token);
}
