package org.welisdoon.model.data.service;

import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.welisdoon.model.data.components.foreign.IForeignKeyOperator;
import org.welisdoon.model.data.dao.ColumnDao;
import org.welisdoon.model.data.dao.DataObjectDao;
import org.welisdoon.model.data.dao.FieldDao;
import org.welisdoon.model.data.dao.TableDao;
import org.welisdoon.model.data.entity.database.ColumnEntity;
import org.welisdoon.model.data.entity.database.ForeignEntity;
import org.welisdoon.model.data.entity.database.TableEntity;
import org.welisdoon.model.data.entity.object.DataObjectEntity;
import org.welisdoon.model.data.entity.object.FieldEntity;
import org.welisdoon.web.common.ApplicationContextProvider;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @Classname DataBaseModelService
 * @Description TODO
 * @Author Septem
 * @Date 17:18
 */
@Service
public class DataObjectService {

    FieldDao fieldDao;
    DataObjectDao dataObjectDao;
    DataTableService dataTableService;
    Reflections reflections;


    @Autowired
    public void setDataTableService(DataTableService dataTableService) {
        this.dataTableService = dataTableService;
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
    public void setReflections(Reflections reflections) {
        this.reflections = reflections;
    }


    public DataObjectEntity getDataObject(Long id) {
        DataObjectEntity entity = dataObjectDao.get(id);
        entity.setTable(dataTableService.getTable(entity.getTableId()));
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
