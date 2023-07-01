package org.welisdoon.model.data.web;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.welisdoon.model.data.condition.TableCondition;
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

import java.util.Map;

/**
 * @Classname TableManagerRouter
 * @Description TODO
 * @Author Septem
 * @Date 16:02
 */

@Component
@VertxConfiguration
@VertxRoutePath(prefix = "/dm/database/table", requestBodyEnable = true)
public class DataManagerTableRouter {
    DataTableService baseService;
    TableDao tableDao;

    @Autowired
    public void setTableDao(TableDao tableDao) {
        this.tableDao = tableDao;
    }

    @Autowired
    void setValue(DataTableService baseService) {
        this.baseService = baseService;
    }

    @VertxRouter(path = "\\/(?<id>\\d+)(\\/(?<tid>\\d+))?",
            method = "GET",
            mode = VertxRouteType.PathRegex)
    public void table(RoutingContextChain chain) {
        chain.handler(routingContext -> {
            TableEntity entity = this.baseService.getTable(Long.valueOf(routingContext.pathParam("id")));
            if (StringUtils.isNotEmpty(routingContext.pathParam("tid"))) {
                IColumnDataFormat.setFormatValue(Long.valueOf(routingContext.pathParam("tid")), entity);
            }
            routingContext.end(JSONObject.toJSONString(entity));
        });
    }

    @VertxRouter(path = "/query",
            method = "POST")
    public void queryTable(RoutingContextChain chain) {
        chain.handler(routingContext -> {
            TableCondition condition = JSONObject.parseObject(routingContext.getBodyAsString(), TableCondition.class);
            PageHelper.startPage(condition.getPage().getPage(), condition.getPage().getPageSize());
            routingContext.end(JSONObject.toJSONString(PageInfo.of(tableDao.page(condition))));
        });
    }

    @VertxRouter(path = "\\/(?<id>\\d+)",
            method = "GET", mode = VertxRouteType.PathRegex)
    public void getTable(RoutingContextChain chain) {
        chain.handler(routingContext -> {
            TableCondition condition = JSONObject.parseObject(routingContext.getBodyAsString(), TableCondition.class);
            PageHelper.startPage(condition.getPage().getPage(), condition.getPage().getPageSize());
            routingContext.end(JSONObject.toJSONString(baseService.getTable(Long.valueOf(routingContext.pathParam("id")))));
        });
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
