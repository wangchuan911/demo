package org.welisdoom.task.xml.entity;

/**
 * @Classname Insert
 * @Description TODO
 * @Author Septem
 * @Date 18:01
 */
@Tag(value = "insert", parentTag = {Transactional.class, If.class, Iterate.class})
public class Insert extends Unit {
}
