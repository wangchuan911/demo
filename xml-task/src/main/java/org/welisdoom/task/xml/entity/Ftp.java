package org.welisdoom.task.xml.entity;

import org.w3c.dom.Element;
import org.welisdoom.task.xml.intf.type.Executable;
import org.welisdoom.task.xml.intf.type.Stream;

/**
 * @Classname Ftp
 * @Description TODO
 * @Author Septem
 * @Date 17:23
 */
@Tag(value = "ftp", parentTagTypes = Executable.class)
public class Ftp extends Unit implements Stream {
}
