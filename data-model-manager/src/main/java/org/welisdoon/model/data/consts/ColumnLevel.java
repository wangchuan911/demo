package org.welisdoon.model.data.consts;

public enum ColumnLevel {
    ID(0), TYPE(1), CODE(2), NAME(3), OTHER(4);
    int level;

    ColumnLevel(int level) {
        this.level = level;
    }

    public int id() {
        return this.level;
    }

}