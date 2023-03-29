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
    public void nodeEnd() {
        Task task = getParent(Task.class);
        for (Unit unit : children) {
            if (unit instanceof Instances) {
                task.instances.putAll(((Instances) unit).children.stream().collect(Collectors.toMap(Unit::getName, unit1 -> unit1)));
            } else if (unit instanceof Values) {
                task.values.putAll(((Values) unit)
                        .children.stream()
                        .filter(unit1 -> unit1 instanceof Value && Objects.nonNull(unit1.name) && Objects.nonNull(((Value) unit1).value))
                        .map(unit1 -> (Value) unit1)
                        .collect(Collectors.toMap(Value::getName, Value::getValue)));
            }
        }
    }
}
