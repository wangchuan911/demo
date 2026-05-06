package org.welisdoon.common.data;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @Classname ICondition
 * @Description TODO
 * @Author Septem
 * @Date 16:59
 */
public abstract class BaseCondition<ID, D> {
    ID id;
    D data;
    Map<String, Object> condition;
    String query;
    Page page;
    int total;

    public static Page newPage() {
        return new Page(1, 100);
    }

    public <T extends BaseCondition> T setPage(Page page) {
        this.page = page;
        return (T) this;
    }

    public Page getPage() {
        return page;
    }

    public static class Page {
        int page = 1, pageSize = 10, startIndex = 1;
        long start, end;

        public Page() {
            compute();
        }


        public Page(int page, int pageSize) {
            setPageSize(pageSize);
            setPage(page);
        }

        protected Page compute() {
            start = (this.page - 1) * this.pageSize + startIndex;
            end = this.page * this.pageSize;
            return this;
        }

        public int getPage() {
            return page;
        }

        public Page setPage(int page) {
            this.page = Math.max(page, 1);
            compute();
            return this;
        }

        public Page nextPage() {
            this.page++;
            compute();
            return this;
        }

        public int getPageSize() {
            return pageSize;
        }

        public Page setPageSize(int pageSize) {
            this.pageSize = Math.max(pageSize, 1);
            compute();
            return this;
        }

        public long getStart() {
            return start;
        }

        public long getEnd() {
            return end;
        }


        public Page setStartIndex(int startIndex) {
            this.startIndex = startIndex;
            compute();
            return this;
        }
    }

    public ID getId() {
        return id;
    }

    public D getData() {
        return data;
    }

    public <T extends BaseCondition> T setId(ID id) {
        this.id = id;
        return (T) this;
    }

    public <T extends BaseCondition> T setData(D data) {
        this.data = data;
        return (T) this;
    }

    public <T extends BaseCondition> T setCondition(Map<String, Object> condition) {
        this.condition = condition;
        return (T) this;
    }

    public Map<String, Object> getCondition() {
        return condition;
    }

    public <T extends BaseCondition> T setQuery(String query) {
        this.query = query;
        return (T) this;
    }

    public String getQuery() {
        return query;
    }

    public int getTotal() {
        return total;
    }
}
