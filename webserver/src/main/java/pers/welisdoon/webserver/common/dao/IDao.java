package pers.welisdoon.webserver.common.dao;

import java.util.List;

public interface IDao<T> {
    <T> T get(T t);

    List<T> list(T t);

    int add(T t);

    int addAll(List<T> ts);

    int set(T t);

    int setAll(List<T> ts);

    int del(T t);

    int delAll(List<T> ts);

    int num(T t);

    int count();
}
