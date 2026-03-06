package org.welisdoon.common.object.wrapper;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.LinkedList;

/**
 * @Classname PagePrepare
 * @Description TODO
 * @Author Septem
 * @Date 17:46
 */
public class PagePrepare extends Prepare {
    int page = 1;
    int size = 100;
    Format format;

    public PagePrepare(Prepare prepare, Format format) {
        super(MessageFormat.format("{0} {1} {2}", format.getStart(), prepare.sql, format.getEnd()), new LinkedList<>(prepare.params));
        if (format.insertFirst()) {
            this.params.add(0, format.getPageArg2(page, size));
            this.params.add(0, format.getPageArg1(page, size));
        } else {
            this.params.add(format.getPageArg1(page, size));
            this.params.add(format.getPageArg2(page, size));
        }
    }

    public PagePrepare nextPage() {
        page++;
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


    public interface Format {
        String getStart();

        String getEnd();

        int getPageArg1(int page, int size);

        int getPageArg2(int page, int size);

        default boolean insertFirst() {
            return false;
        }
    }
}
