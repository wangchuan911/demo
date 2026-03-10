package org.welisdoon.common.object.wrapper;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * @Classname PagePrepare
 * @Description TODO
 * @Author Septem
 * @Date 17:46
 */
public class PagePrepare extends Prepare {
    int page = 1;
    int size = 100;
    PageFormat format;

    public PagePrepare(Prepare prepare, PageFormat format) {
        super(format.pageBody(prepare.sql), new LinkedList<>(prepare.params));
        if (format.insertFirst()) {
            this.params.add(0, format.getPageArg2(page, size));
            this.params.add(0, format.getPageArg1(page, size));
        } else {
            this.params.add(format.getPageArg1(page, size));
            this.params.add(format.getPageArg2(page, size));
        }
    }

    public PagePrepare nextPage() {
        return nextPage(1);
    }

    public PagePrepare nextPage(int nextPage) {
        page += nextPage;
        if (format.insertFirst()) {
            this.params.set(0, format.getPageArg1(page, size));
            this.params.set(1, format.getPageArg2(page, size));
        } else {
            this.params.set(this.params.size() - 2, format.getPageArg1(page, size));
            this.params.set(this.params.size() - 1, format.getPageArg2(page, size));
        }
        return this;
    }

    public PagePrepare nextPage(PreparedStatement preparedStatement) throws SQLException {
        nextPage();
        if (format.insertFirst()) {
            preparedStatement.setInt(1, (int) this.params.get(0));
            preparedStatement.setInt(2, (int) this.params.get(1));
        } else {
            final int offset = params.size() - 2;
            preparedStatement.setInt(offset, (int) this.params.get(0));
            preparedStatement.setInt(offset + 1, (int) this.params.get(1));
        }
        return this;
    }


    public interface PageFormat {
        String pageBody(String s);

        int getPageArg1(int page, int size);

        int getPageArg2(int page, int size);

        default boolean insertFirst() {
            return false;
        }
    }

    public enum DefaultPageFormat implements PagePrepare.PageFormat {
        mysql((s) -> s + " limit ? offset ?", (page, size) -> (page - 1) * size, (page, size) -> size),

        oracle((s) -> "select * from (select rownum as rn,* from ( " + s + " ) where rownum<? ) and rn >?", (page, size) -> ((page - 1) * size) + 1, (page, size) -> page * size),

        postgresql((s) -> s + " limit ? offset ? ", (page, size) -> (page - 1) * size, (page, size) -> size);

        DefaultPageFormat(Function<String, String> body, BiFunction<Integer, Integer, Integer> arg0, BiFunction<Integer, Integer, Integer> arg1) {
            this.body = body;
            this.arg0 = arg0;
            this.arg1 = arg1;
        }

        final Function<String, String> body;
        final BiFunction<Integer, Integer, Integer> arg0, arg1;


        @Override
        public String pageBody(String s) {
            return body.apply(s);
        }

        @Override
        public int getPageArg1(int page, int size) {
            return arg0.apply(page, size);
        }

        @Override
        public int getPageArg2(int page, int size) {
            return arg1.apply(page, size);
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
