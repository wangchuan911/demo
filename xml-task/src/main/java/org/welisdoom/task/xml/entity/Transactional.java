package org.welisdoom.task.xml.entity;


import org.welisdoom.task.xml.intf.type.Executable;
import org.welisdoom.task.xml.intf.type.Initialize;

import java.util.Map;

/**
 * @Classname Transactional
 * @Description TODO
 * @Author Septem
 * @Date 17:59
 */
@Tag(value = "transactional", parentTagTypes = Executable.class)
public class Transactional extends Unit implements Executable {
    @Override
    protected void execute(Map<String, Object> data) {
        try {
            super.execute(data);
        } finally {
        }
    }
}
