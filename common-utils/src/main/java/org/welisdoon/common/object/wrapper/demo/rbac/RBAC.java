package org.welisdoon.common.object.wrapper.demo.rbac;

import org.welisdoon.common.object.wrapper.AbstractDataAccessObject;

import java.util.List;

/**
 * @Classname Org
 * @Description TODO
 * @Author Septem
 * @Date 17:52
 */
public class RBAC extends AbstractDataAccessObject implements IRBAC {
    @Override
    public <T> T loadRemote(String key) {
        return null;
    }

    @Override
    public <T> List<T> loadsRemote(String key) {
        return null;
    }

    @Override
    public <T> T loadLocal(String key) {
        return null;
    }

    @Override
    public <T> List<T> loadsLocal(String key) {
        return null;
    }
}
