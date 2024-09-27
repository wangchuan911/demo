package org.welisdoon.metadata.prototype.condition;

import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.welisdoon.common.data.BaseCondition;

import java.lang.reflect.Type;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Page extends BaseCondition.Page implements ObjectDeserializer {
    @Override
    protected BaseCondition.Page compute() {
        try {
            return super.compute();
        } finally {
            PageHelper.startPage(this.getPage(), this.getPageSize());
        }
    }

    public Page(BaseCondition.Page page) {
        super(page.getPage(), page.getPageSize());
    }

    public Page() {
        super();
    }

    public Page(int page, int pageSize) {
        super(page, pageSize);
    }

    @Override
    public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
        return parser.parseObject((Type) Page.class);
    }

    @Override
    public int getFastMatchToken() {
        return 0;
    }


    public static <T> PageInfo<T> pageInfo(List<T> list) {
        return PageInfo.of(list);
    }

    public static <T, V> PageInfo<T> pageInfo(List<V> list, Function<V, T> converter) {
        PageInfo pageInfo = PageInfo.of(list);
        pageInfo.setList((List) pageInfo.getList().stream().map(o -> converter.apply((V) o)).collect(Collectors.toList()));
        return pageInfo;
    }
}
