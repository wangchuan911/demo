package org.welisdoon.common.object.wrapper.dao.annotation;

import org.welisdoon.common.object.wrapper.dao.base.IEntity;

import java.lang.annotation.*;

/**
 * @Classname Data
 * @Description 注解 定义在 interface上，由ant 动态生成实例对象
 * @Author Septem
 * @Date 15:11
 */
public interface Database {
    // 主表
    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface Main {
        //主键
        String id();

        //特征
        Feature feature();

        //组成
        Part[] parts();
    }

    //子类 扩展表
    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface Extra {
        //特征
        Feature feature();

        //组成
        Part[] parts();


    }

    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface Part {

        //表名
        String name();

        Attr[] attrs();

        boolean required() default true;

    }

    //特征
    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface Feature {
        //对象名
        String alias();

        Express[] express();
    }

    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface Express {
        //属性名
        String attr();

        String[] value();

        Operation operation() default Operation.Equal;

    }

    enum Operation {
        Equal
    }

    //字段
    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface Attr {
        //名称
        String name();

        //字段描述
        String desc();

        //转成对象名称
        String alias();

        //与其他表的关联
        String relAttr() default "";

        //字段映射java
        Class classType() default String.class;

        //字段权重
        int level() default Integer.MIN_VALUE;
    }

    @interface ExtraAttrs {
        Attr[] value();
    }

    @interface AttrToAttr {
        String self();

        String target();
    }

    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Repeatable(Relations.class)
    @interface Relation {
        String alias();

        Class<? extends IEntity> target();

        AttrToAttr[] rel();

        boolean one2More() default false;
    }

    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface Relations {
        Relation[] value();
    }

   /* @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Repeatable(Relations2.class)
    @interface Relation2 {
        String alias();

        String selfAttr();

        AttrToAttr2[] rel();

        Class<? extends IEntity> baseType() default IEntity.class;
    }

    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @interface Relations2 {
        Relation2[] value();
    }

    @interface AttrToAttr2 {
        Class<? extends IEntity> targetType();

        String targetAttr();
    }*/
}
