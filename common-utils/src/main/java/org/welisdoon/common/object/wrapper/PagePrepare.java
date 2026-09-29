package org.welisdoon.common.object.wrapper;

import org.welisdoon.common.CodeUtils;
import org.welisdoon.common.data.BaseCondition;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @Classname PagePrepare
 * @Description TODO
 * @Author Septem
 * @Date 17:46
 */
public class PagePrepare extends Prepare {
    final BaseCondition.Page page;
    final PageFormat format;

    public PagePrepare(Prepare prepare, PageFormat format) {
        this(prepare, format, new BaseCondition.Page(1, 100));
    }

    public PagePrepare(Prepare prepare, PageFormat format, BaseCondition.Page page) {
        super(format.pageBody(prepare.sql), CodeUtils.creator(() -> {
            Map.Entry<Integer, Integer>[] entries = format.getPageParam(prepare.params.size(), page);
            List<Object> params = new LinkedList<>(prepare.params);
            for (Map.Entry<Integer, Integer> entry : entries) {
                if (entry.getKey() + 1 >= params.size()) {
                    params.add(entry.getKey(), entries);
                } else {
                    params.add(entry.getValue());
                }
            }
            return params;
        }));
        this.page = page;
        this.format = format;
    }

    protected List<Object> createPageParams(Prepare prepare, PageFormat format, BaseCondition.Page page) {
        return new LinkedList<>(prepare.params);
    }

    public PagePrepare nextPage() {
        return nextPage(1);
    }

    public PagePrepare nextPage(int nextPage) {
        page.nextPage(nextPage);
        toPage(nextPage + page.getPage(), page.getPageSize());
        return this;
    }

    public PagePrepare setPage(BaseCondition.Page page) {
        return toPage(page.getPage(), page.getPageSize());
    }

    public PagePrepare toPage(int page, int size) {
        this.page.setPage(page);
        this.page.setPageSize(size);
        setParams(format.getPageParam(this.params.size(), this.page));
        return this;
    }

    public PagePrepare nextPage(PreparedStatement preparedStatement) throws SQLException {
        nextPage();
        setParams(preparedStatement, format.getPageParam(this.params.size(), this.page));
        return this;
    }

    public void setParams(Map.Entry<Integer, Integer>[] entries) {
        for (Map.Entry<Integer, Integer> entry : entries) {
            params.set(entry.getKey(), entry.getValue());
        }
    }

    public void setParams(PreparedStatement preparedStatement, Map.Entry<Integer, Integer>[] entries) throws SQLException {
        for (Map.Entry<Integer, Integer> entry : entries) {
            preparedStatement.setInt(entry.getKey() + 1, entry.getValue());
        }
    }


    public interface PageFormat {
        String pageBody(String s);

        Map.Entry<Integer, Integer>[] getPageParam(int size, BaseCondition.Page page);

    }

    public enum DefaultPageFormat implements PagePrepare.PageFormat {
        mysql((s) -> s + " limit ? offset ?", (length, page1) -> {
            return new Map.Entry[]{Map.entry(length - 2, page1.getStart()), Map.entry(length - 1, page1.getPageSize())};
        }),

        oracle((s) -> "select * from (select rownum as rn,* from ( " + s + " ) where rownum<? ) and rn >?", (length, page1) -> {
            return new Map.Entry[]{Map.entry(length - 2, page1.getEnd()), Map.entry(length - 1, page1.getStart())};
        }),

        postgresql((s) -> s + " limit ? offset ? ", (length, page1) -> {
            return new Map.Entry[]{Map.entry(length - 2, page1.getStart()), Map.entry(length - 1, page1.getPageSize())};
        });

        DefaultPageFormat(Function<String, String> body, BiFunction<Integer, BaseCondition.Page, Map.Entry<Integer, Integer>[]> pageSetter) {
            this.bodyChanger = body;
            this.pageSetter = pageSetter;
        }

        final Function<String, String> bodyChanger;
        final BiFunction<Integer, BaseCondition.Page, Map.Entry<Integer, Integer>[]> pageSetter;


        @Override
        public String pageBody(String s) {
            return bodyChanger.apply(s);
        }

        @Override
        public Map.Entry<Integer, Integer>[] getPageParam(int size, BaseCondition.Page page) {
            return pageSetter.apply(size, page);
        }


        public static PagePrepare.PageFormat getFormat(String name) {
            return getFormat(SqlMapper.getDataSource(name));
        }

        static Field druidField;

        public static PagePrepare.PageFormat getFormat(DataSource dataSource) {
            if (dataSource != null)
                try {
                    String className = dataSource.getClass().getName();
                    if (className.startsWith("com.alibaba.druid")) {
                        if (druidField == null) {
                            druidField = dataSource.getClass().getDeclaredField("driverClass");
                            druidField.setAccessible(true);
                        }
                        className = (String) druidField.get(dataSource);
                    }
                    for (DefaultPageFormat value : values()) {
                        if (className.contains(value.name())) {
                            return value;
                        }
                    }
                } catch (Throwable throwables) {
                    throwables.printStackTrace();
                }
            return mysql;
        }


    }

}
