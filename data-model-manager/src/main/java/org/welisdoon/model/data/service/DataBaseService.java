package org.welisdoon.model.data.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.welisdoon.model.data.dao.ColumnDao;
import org.welisdoon.model.data.dao.DataObjectDao;
import org.welisdoon.model.data.dao.FieldDao;
import org.welisdoon.model.data.dao.TableDao;
import org.welisdoon.model.data.entity.database.ColumnEntity;
import org.welisdoon.model.data.entity.database.TableEntity;
import org.welisdoon.model.data.entity.object.DataObjectEntity;
import org.welisdoon.model.data.entity.object.FieldEntity;

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
public class DataBaseService {

    TableDao tableDao;
    ColumnDao columnDao;
    FieldDao fieldDao;
    DataObjectDao dataObjectDao;

    @Autowired
    public void setColumnDao(ColumnDao columnDao) {
        this.columnDao = columnDao;
    }

    @Autowired
    public void setDataObjectDao(DataObjectDao dataObjectDao) {
        this.dataObjectDao = dataObjectDao;
    }

    @Autowired
    public void setFieldDao(FieldDao fieldDao) {
        this.fieldDao = fieldDao;
    }

    @Autowired
    public void setTableDao(TableDao tableDao) {
        this.tableDao = tableDao;
    }

    /*@PostConstruct
    void init() {
        Object o;
        o = getTable(1L);
        o = getColumn(1L);
        o = getField(1L);
        o = getDataObject(1L);
    }*/

    public TableEntity getTable(Long id) {
        TableEntity entity = tableDao.get(id);
        for (ColumnEntity column : entity.getColumns()) {
            column.setTable(entity);
            if (Objects.equals(column.getId(), entity.getPrimaryId())) {
                entity.setPrimary(column);
            }
        }
        return entity;
    }

    public ColumnEntity getColumn(Long id) {
        ColumnEntity entity = columnDao.get(id);
        return Arrays.stream(getTable(entity.getTableId()).getColumns()).filter(columnEntity -> columnEntity.equals(entity)).findFirst().get();
    }

    public DataObjectEntity getDataObject(Long id) {
        DataObjectEntity entity = dataObjectDao.get(id);
        entity.setTable(getTable(entity.getTableId()));
        Map<Long, ColumnEntity> map = Arrays.stream(entity.getTable().getColumns()).collect(Collectors.toMap(ColumnEntity::getId, columnEntity -> columnEntity));
        for (FieldEntity field : entity.getFields()) {
            field.setColumns(Arrays.stream(field.getColumnIds()).map(aLong -> map.get(aLong)).toArray(ColumnEntity[]::new));
        }
        return entity;
    }

    public FieldEntity getField(Long id) {
        FieldEntity entity = fieldDao.get(id);
        DataObjectEntity dataObjectEntity = getDataObject(entity.getObjectId());
        return Arrays.stream(dataObjectEntity.getFields()).filter(fieldEntity -> Objects.equals(entity, fieldEntity)).findFirst().get();
    }
}
