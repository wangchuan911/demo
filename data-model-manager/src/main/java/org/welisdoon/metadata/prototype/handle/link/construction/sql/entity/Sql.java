package org.welisdoon.metadata.prototype.handle.link.construction.sql.entity;

import org.welisdoon.metadata.prototype.define.MetaLink;

import java.util.LinkedList;
import java.util.List;

/**
 * @Classname Sql
 * @Description TODO
 * @Author Septem
 * @Date 11:12
 */
public abstract class Sql extends MetaLink {

    String getPrefix() {
        MetaLink metaLink = getParent();
        if (metaLink instanceof Sql)
            return ((Sql) metaLink).getPrefix();
        return "";
    }

    protected void readonly() {
        this.setState(LifeState.Readonly);
    }

    abstract protected void build();

    abstract protected String format(FormatContent content);

    public static class FormatContent {
        List<Part> parts = new LinkedList<>();
        int level = 0;

        public void deep() {
            level++;
        }

        public void shallow() {
            level--;
        }

        public int getTableCount() {
            return parts.size();
        }

        public void addPart(Part part) {
            parts.add(part);
        }

        public static class Part {
            boolean weak;
            SqlAlias sqlAlias;
            List<String> condition = new LinkedList<>();
            int level;

            public Part(FormatContent content) {
                this.level = content.level;
            }

            public Part setCondition(List<String> condition) {
                this.condition = condition;
                return this;
            }

            public Part setSqlAlias(SqlAlias sqlAlias) {
                this.sqlAlias = sqlAlias;
                return this;
            }

            public Part setWeak(boolean weak) {
                this.weak = weak;
                return this;
            }
        }
    }
}
