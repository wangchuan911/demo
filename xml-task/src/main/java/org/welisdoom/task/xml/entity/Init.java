package org.welisdoom.task.xml.entity;

import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Classname Init
 * @Description TODO
 * @Author Septem
 * @Date 18:24
 */
@Tag(value = "init", parentTag = "task")
public class Init extends Unit {
    @Override
    public void nodeEndOnChild(Unit unit) {
        Task task = (Task) this.parent;
        if (unit instanceof Instances) {
            task.instances.putAll(((Instances) unit).units.stream().collect(Collectors.toMap(Unit::getName, unit1 -> unit1)));
        } else if (unit instanceof Values) {
            task.values.putAll(((Values) unit).values.stream().filter(value -> Objects.nonNull(value.name) && Objects.nonNull(value.value)).collect(Collectors.toMap(Value::getName, Value::getValue)));
        }
    }

    @Override
    public void nodeEnd() {
        this.parent = null;
    }
}
