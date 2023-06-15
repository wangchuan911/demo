package org.welisdoon.model.data.web;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.welisdoon.model.data.dao.ColumnDao;
import org.welisdoon.model.data.dao.DataObjectDao;
import org.welisdoon.model.data.dao.TableDao;
import org.welisdoon.model.data.entity.database.AbstractDataEntity;
import org.welisdoon.model.data.entity.database.ColumnEntity;
import org.welisdoon.model.data.entity.database.IColumnDataFormat;
import org.welisdoon.model.data.entity.database.TableEntity;
import org.welisdoon.model.data.entity.object.DataObjectEntity;
import org.welisdoon.model.data.service.DataTableService;
import org.welisdoon.model.data.utils.TableResultUtils;
import org.welisdoon.web.common.ApplicationContextProvider;
import org.welisdoon.web.vertx.annotation.VertxConfiguration;
import org.welisdoon.web.vertx.annotation.VertxRoutePath;
import org.welisdoon.web.vertx.annotation.VertxRouter;
import org.welisdoon.web.vertx.enums.VertxRouteType;
import org.welisdoon.web.vertx.utils.RoutingContextChain;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @Classname TableManagerRouter
 * @Description TODO
 * @Author Septem
 * @Date 16:02
 */

@Component
@VertxConfiguration
@VertxRoutePath(prefix = "/dm/database/object", requestBodyEnable = true)
public class DataManagerObjectRouter {
    DataTableService baseService;

    @Autowired
    void setValue(DataTableService baseService) {
        this.baseService = baseService;
    }


    @VertxRouter(path = "\\/(?<id>\\d+)(\\/(?<oid>\\d+))?",
            method = "GET",
            mode = VertxRouteType.PathRegex)
    public void object(RoutingContextChain chain) {
        chain.handler(routingContext -> {
            DataObjectEntity entity = this.baseService.getDataObject(Long.valueOf(routingContext.pathParam("id")));
            if (StringUtils.isNotEmpty(routingContext.pathParam("oid"))) {
                List<ColumnEntity> entities = new LinkedList<>();
                try {
                    /*Map<String, Object> result = TableResultUtils.queryForMap(Long.valueOf(routingContext.pathParam("oid")), entity.getTable().getColumns());
                    for (FieldEntity field : entity.getFields()) {
                        for (ColumnEntity column : field.getColumns()) {
                            if (column.getTable().equals(entity.getTable())) {
                                entities.add(column);
                            }
                        }
                    }
                    IColumnDataFormat.setFormatValue(result, entities.toArray(new ColumnEntity[0]));*/
                    TableResultUtils.query(Long.valueOf(routingContext.pathParam("oid")), entity);
                } finally {
                    entities.clear();
                }
            }
            routingContext.end(JSONObject.toJSONString(entity));
        });
    }

    protected void setValue(Long id, Map<String, Object> result, ColumnEntity entity) {

    }

    @VertxRouter(path = "\\/value\\/(?<type>\\w+)\\/(?<id>\\d+)",
            method = "GET",
            mode = VertxRouteType.PathRegex)
    public void value(RoutingContextChain chain) {
        chain.handler(routingContext -> {
            AbstractDataEntity entity;
            long id = Long.valueOf(routingContext.pathParam("id"));
            Map<String, Object> result;
            switch (routingContext.pathParam("type")) {
                case "object":
                    entity = ApplicationContextProvider.getApplicationContext().getBean(DataObjectDao.class).get(id);
                    result = TableResultUtils.queryForMap(id, ((DataObjectEntity) entity).getTable().getColumns());
                    break;
                case "column":
                    entity = ApplicationContextProvider.getApplicationContext().getBean(ColumnDao.class).get(id);
                    result = TableResultUtils.queryForMap(id, ((ColumnEntity) entity).getTable().getColumns());
                    break;
                case "table":
                    entity = ApplicationContextProvider.getApplicationContext().getBean(TableDao.class).get(id);
                    result = TableResultUtils.queryForMap(id, ((TableEntity) entity).getColumns());
                    break;
                default:
                    result = Map.of();
            }
            routingContext.end(JSONObject.toJSONString(result));
        });
    }

    @VertxRouter(path = "\\/(?<type>\\w+)\\/(?<id>\\d+)(\\/(?<tid>\\d+))?",
            method = "PUT",
            mode = VertxRouteType.PathRegex)
    public void addValue(RoutingContextChain chain) {
        chain.handler(routingContext -> {
            long id = Long.valueOf(routingContext.pathParam("id"));
            JSONObject result = JSON.parseObject(routingContext.getBodyAsString());
            switch (routingContext.pathParam("type")) {
                case "object": {
                    DataObjectEntity entity = this.baseService.getDataObject(Long.valueOf(routingContext.pathParam("id")));
                    if (StringUtils.isNotEmpty(routingContext.pathParam("tid"))) {
                        IColumnDataFormat.setValue(Long.valueOf(routingContext.pathParam("tid")), result, entity);
                    } else {
                        IColumnDataFormat.setValue(result, entity);
                    }
                    result = (JSONObject) JSON.toJSON(entity);
                }
                break;
                case "table":
                    TableEntity entity = ApplicationContextProvider.getApplicationContext().getBean(TableDao.class).get(id);
                    IColumnDataFormat.setValue(result, entity.getColumns());
                    break;
                default:
            }
            System.out.println(result.toJSONString());
            System.out.println(result.getDate("F13"));
            routingContext.end(JSONObject.toJSONString(result));
        });
    }

    @VertxRouter(path = "\\/value\\/(?<type>\\w+)\\/(?<id>\\d+)",
            method = "POST",
            mode = VertxRouteType.PathRegex)
    public void setValue(RoutingContextChain chain) {
        chain.handler(routingContext -> {
            AbstractDataEntity entity;
            JSONObject result = JSON.parseObject(routingContext.getBodyAsString());
            switch (routingContext.pathParam("type")) {
                case "object":
                    break;
                case "column":
                    break;
                case "table":
                    break;
                default:
            }
            System.out.println(result.toJSONString());
            routingContext.end(JSONObject.toJSONString(result));
        });
    }
}
