package org.welisdoon.model.data.service;

import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Service;
import org.welisdoon.common.ObjectUtils;
import org.welisdoon.model.data.components.foreign.IForeignKeyOperator;
import org.welisdoon.model.data.condition.ColumnCondition;
import org.welisdoon.model.data.dao.ColumnDao;
import org.welisdoon.model.data.dao.DataObjectDao;
import org.welisdoon.model.data.dao.FieldDao;
import org.welisdoon.model.data.dao.TableDao;
import org.welisdoon.model.data.entity.database.ColumnEntity;
import org.welisdoon.model.data.entity.database.ForeignEntity;
import org.welisdoon.model.data.entity.database.IForeignTarget;
import org.welisdoon.model.data.entity.database.TableEntity;
import org.welisdoon.model.data.entity.object.DataObjectEntity;
import org.welisdoon.model.data.entity.object.FieldEntity;
import org.welisdoon.web.common.ApplicationContextProvider;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Classname DataBaseModelService
 * @Description TODO
 * @Author Septem
 * @Date 17:18
 */
@Service
public class DataTableService {

    TableDao tableDao;
    ColumnDao columnDao;
    Reflections reflections;

    @Autowired
    public void setColumnDao(ColumnDao columnDao) {
        this.columnDao = columnDao;
    }

    @Autowired
    public void setTableDao(TableDao tableDao) {
        this.tableDao = tableDao;
    }

    @Autowired
    public void setReflections(Reflections reflections) {
        this.reflections = reflections;
    }


    Map<Long, IForeignKeyOperator> longIForeignKeyOperatorMap = new HashMap<>();
    ;

    @PostConstruct
    void init() {
        /*Object o;
        o = getTable(1L);
        o = getColumn(1L);
        o = getField(1L);
        o = getDataObject(1L);*/

        reflections.getTypesAnnotatedWith(IForeignKeyOperator.ForeignKey.class)
                .stream()
                .map(aClass -> aClass.getAnnotation(IForeignKeyOperator.ForeignKey.class)).forEach(foreignKey -> {
            longIForeignKeyOperatorMap.put(foreignKey.typeId(), ApplicationContextProvider.getBean(foreignKey.operator()));
        });
    }

    public TableEntity getTable(Long id) {
        TableEntity entity = tableDao.get(id);
        entity.setColumns(columnDao.list(new ColumnCondition().setEntity(new ColumnEntity().setTableId(entity.getId()))).stream().toArray(ColumnEntity[]::new));
        for (ColumnEntity column : entity.getColumns()) {
            column.setTable(entity);
            if (Objects.equals(column.getId(), entity.getPrimaryId())) {
                entity.setPrimary(column);
            }
            if (column.isForeign()) {
                column.getForeign().setColumn(column);
                setForeign(column.getForeign());
            }
        }
        return entity;
    }

    public IForeignKeyOperator getForeignKeyOperator(long targetTypeId) {
        return longIForeignKeyOperatorMap.get(targetTypeId);
    }


    protected void setForeign(ForeignEntity foreign) {
        foreign.setTarget(longIForeignKeyOperatorMap.get(foreign.getTargetTypeId()).getTarget(foreign));
    }

    public ColumnEntity getColumn(Long id) {
        ColumnEntity entity = columnDao.get(id);
        return Arrays.stream(getTable(entity.getTableId()).getColumns()).filter(columnEntity -> columnEntity.equals(entity)).findFirst().get();
    }


}
