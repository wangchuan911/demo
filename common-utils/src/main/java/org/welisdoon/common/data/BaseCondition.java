package org.welisdoon.common.data;

import java.util.Map;

/**
 * @Classname ICondition
 * @Description TODO
 * @Author Septem
 * @Date 16:59
 */
public abstract class BaseCondition<ID, D extends IData<ID, ?>> {
    ID id;
    D data;
    Map<String, Object> condition;
    String query;
    Page page;

    public void setPage(Page page) {
        this.page = page;
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

        protected void compute() {
            start = (this.page - 1) * this.pageSize + startIndex;
            end = this.page * this.pageSize;
        }

        public int getPage() {
            return page;
        }

        public void setPage(int page) {
            this.page = Math.max(page, 1);
            compute();
        }

        public int getPageSize() {
            return pageSize;
        }

        public void setPageSize(int pageSize) {
            this.pageSize = Math.max(pageSize, 1);
            compute();
        }

        public long getStart() {
            return start;
        }

        public long getEnd() {
            return end;
        }


        public void setStartIndex(int startIndex) {
            this.startIndex = startIndex;
            compute();
        }
    }

    public ID getId() {
        return id;
    }

    public D getData() {
        return data;
    }

    public void setId(ID id) {
        this.id = id;
    }

    public void setData(D data) {
        this.data = data;
    }

    public void setCondition(Map<String, Object> condition) {
        this.condition = condition;
    }

    public Map<String, Object> getCondition() {
        return condition;
    }
}