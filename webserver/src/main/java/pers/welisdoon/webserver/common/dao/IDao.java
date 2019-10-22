package pers.welisdoon.webserver.common.dao;

import java.util.List;

public interface IDao<T> {
    <T> T get(T t);

    List<T> list(T t);

    int put(T t);

    int putAll(List<T> ts);
}
