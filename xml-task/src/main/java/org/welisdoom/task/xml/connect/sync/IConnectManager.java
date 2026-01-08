package org.welisdoom.task.xml.connect.sync;

import org.welisdoom.task.xml.intf.ISession;

/**
 * @Classname ConnectPool
 * @Description TODO
 * @Author Septem
 * @Date 13:53
 */
public interface IConnectManager<T> {

    T getConnect(String name, ISession session) throws Throwable;
}
