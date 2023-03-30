package org.welisdoom.task.xml.entity;

import org.welisdoom.task.xml.intf.type.Executable;
import org.welisdoom.task.xml.intf.type.Initialize;

/**
 * @Classname Transactional
 * @Description TODO
 * @Author Septem
 * @Date 17:59
 */
@Tag(value = "transactional", parentTagTypes = Executable.class)
public class Transactional extends Unit implements Executable {
}
