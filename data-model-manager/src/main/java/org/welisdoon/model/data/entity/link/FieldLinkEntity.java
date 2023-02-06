package org.welisdoon.model.data.entity.link;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ql.util.express.DefaultContext;
import com.ql.util.express.ExpressRunner;
import org.apache.commons.logging.LogFactory;
import org.welisdoon.model.data.entity.database.FieldEntity;

import java.util.Map;

/**
 * @Classname InputLinkEntity
 * @Description TODO
 * @Author Septem
 * @Date 15:09
 */
public class FieldLinkEntity  {
    Long id,superId;
    int order;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public void setSuperId(Long superId) {
        this.superId = superId;
    }

    public Long getSuperId() {
        return superId;
    }

    @JsonIgnore(value = true)
    @JSONField(deserialize = false, serialize = false)
    String ableExpress;
    FieldEntity linkEntity;

    public void setLinkEntity(FieldEntity linkEntity) {
        this.linkEntity = linkEntity;
    }

    public FieldEntity getLinkEntity() {
        return linkEntity;
    }

    public FieldLinkEntity[] subLinks;

    public void setSubLinks(FieldLinkEntity[] subLinks) {
        this.subLinks = subLinks;
    }

    public FieldLinkEntity[] getSubLinks() {
        return subLinks;
    }

    public FieldLinkEntity setAbleExpress(String ableExpress) {
        this.ableExpress = ableExpress;
        return this;
    }

    public String getAbleExpress() {
        return ableExpress;
    }

    public boolean isAble(ExpressRunner runner, Map<String, Object> params) throws Exception {
        DefaultContext<String, Object> context = new DefaultContext<>();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            context.put(entry.getKey(), entry.getValue());
        }
        return Boolean.valueOf(runner.execute(this.ableExpress, context, null, false, false, LogFactory.getLog(this.getClass())).toString());
    }

    /*public static void main(String[] args) {
        try {
            System.out.println(new InputLinkEntity().setEableExpress("if(a>b){ return false ;} else if(b>c) { return true;} else return false;")
                    .isEable(new ExpressRunner(), Map.of("a", "1", "b", "2", "c", 3)));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }*/
}
