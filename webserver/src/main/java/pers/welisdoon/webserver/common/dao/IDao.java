package pers.welisdoon.webserver.common.dao;

import java.util.List;

import com.github.pagehelper.Page;


public interface IDao<T, K> {
    /*select one by key*/
    <T> T one(K key);

    /*select one by objct of Type T*/
    <T> T get(T t);

    /*select list  by objct of Type T (object T have list,map... field)*/
    List<T> listAll(T t);

    /*select one  by key (object T have list,map... field)*/
    <T> T oneAll(K key);

    /*select list  by objct of Type T */
    List<T> list(T t);

    /*add one  object*/
    int add(T t);

    /*add many object*/
    int addAll(List<T> ts);

    /*update one object*/
    int set(T t);

    /*update many object*/
    int setAll(List<T> ts);

    /*delete one object*/
    int del(T t);

    /*delete many object*/
    int delAll(List<T> ts);

    /*count by condition*/
    int num(T t);

    /*count all*/
    int count();

    /*select list  by objct of Type T (object T have list,map... field)*/
    Page<T> pageAll(T t);

    /*select list  by objct of Type T */
    Page<T> page(T t);
}
