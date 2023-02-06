package org.welisdoon.model.data.entity.database;

import org.welisdoon.model.data.annotations.Model;
import org.welisdoon.model.data.consts.DataModelType;

@Model(DataModelType.DataSource)
public class DataSourceEntity extends AbstractDataEntity {
    String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
