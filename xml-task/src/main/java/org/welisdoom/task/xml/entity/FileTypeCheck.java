package org.welisdoom.task.xml.entity;

import com.alibaba.fastjson.util.IOUtils;
import io.vertx.core.Future;
import javassist.NotFoundException;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.intf.ApplicationContextProvider;
import org.welisdoom.task.xml.intf.type.Executable;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @Classname Check
 * @Description TODO
 * @Author Septem
 * @Date 12:41
 */
@Tag(value = "file-type-check", parentTagTypes = Executable.class, desc = "文件类型校验")
public class FileTypeCheck extends Unit implements Executable {

    @ConfigurationProperties("xml-task.file.check")
    @Configuration
    public static class FileCheckTypeConfiguration {
        Map<String, Object> magic;

        public void setMagic(Map<String, Object> magic) {
            this.magic = magic;
        }

        public Map<String, Object> getMagic() {
            return magic;
        }

        public void matched(String eigenvalue) throws NotFoundException {
            try {
                if (!magic.entrySet().stream().anyMatch(entry -> {
                    if (entry.getValue() instanceof Collection) {
                        for (Object o : ((Collection) entry.getValue())) {
                            return eigenvalue.startsWith(o.toString());
                        }
                        return false;
                    } else if (entry.getValue().getClass().isArray()) {
                        for (Object o : ((Object[]) entry.getValue())) {
                            return eigenvalue.startsWith(o.toString());
                        }
                        return false;
                    } else {
                        return eigenvalue.startsWith(entry.getValue().toString());
                    }
                })) {
                    throw new NotFoundException("没有匹配的特征值！");
                }
            } catch (NotFoundException e) {
                throw e;
            } catch (Exception e) {
                throw new NotFoundException(e.getMessage(), e);
            } catch (Throwable e) {
                throw new NotFoundException(e.getMessage(), new Exception(e.getMessage(), e));
            }
        }

        public void matched(String fileType, String eigenvalue) throws NotFoundException {
            try {
                if (magic.containsKey(fileType)
                        && Stream
                        .of(magic.get(fileType))
                        .anyMatch(value -> {
                            if (value instanceof Collection) {
                                for (Object o : ((Collection) value)) {
                                    return eigenvalue.startsWith(o.toString());
                                }
                                return false;
                            } else if (value.getClass().isArray()) {
                                for (Object o : ((Object[]) value)) {
                                    return eigenvalue.startsWith(o.toString());
                                }
                                return false;
                            } else {
                                return eigenvalue.startsWith(value.toString());
                            }
                        })) {
                    return;
                }
                throw new NotFoundException(fileType + ":没有匹配的特征值！");
            } catch (NotFoundException e) {
                throw e;
            } catch (Exception e) {
                throw new NotFoundException(e.getMessage(), e);
            } catch (Throwable e) {
                throw new NotFoundException(e.getMessage(), new Exception(e.getMessage(), e));
            }
        }
    }

    @Override
    protected Future<Object> start(TaskInstance data, Object preUnitResult) {
        String fileName = getAttrFormatValue("name", data);

        java.io.File file = new java.io.File(fileName);
        if (!file.exists()) {
            log("文件[" + fileName + "]不存在");
            return Future.succeededFuture(false);
        }
        String fileType = fileName.substring(fileName.lastIndexOf(".") + 1);
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
            byte[] b = new byte[200];
            fileInputStream.read(b);
            StringBuilder bd = new StringBuilder("");
            for (byte b1 : b) {
                bd.append(Integer.toHexString(b1 + 128)).append(",");
            }
            String eigenvalue = bd.toString();
            FileCheckTypeConfiguration fileCheckTypeConfiguration = ApplicationContextProvider.getApplicationContext().getBean(FileCheckTypeConfiguration.class);
            if (StringUtils.isEmpty(fileType)) {
                fileCheckTypeConfiguration.matched(eigenvalue);
            } else {
                fileCheckTypeConfiguration.matched(fileType, eigenvalue);
            }
            return Future.succeededFuture(true);
        } catch (Throwable e) {
            e.printStackTrace();
            return Future.succeededFuture(false);
        } finally {
            IOUtils.close(fileInputStream);
        }
    }

    public static String check(String path1, String path2, String... paths) {
        System.out.println("文件:" + path1.substring(path1.lastIndexOf(".") + 1));
        byte[][] strings = Stream
                .of(new String[]{path1, path2}, paths)
                .flatMap(Arrays::stream)
                .map(path -> {
                    FileInputStream fileInputStream = null;
                    try {
                        fileInputStream = new FileInputStream(path);
                        byte[] b = new byte[200];
                        fileInputStream.read(b);
                        StringBuilder bd = new StringBuilder("原始:");
                        for (byte b1 : b) {
                            bd.append(toHexString(b1)).append(",");
                        }
                        System.out.println(bd.toString());
                        return b;
                    } catch (IOException e) {
                        e.printStackTrace();
                        throw new IllegalStateException(e.getMessage(), e);
                    } finally {
                        IOUtils.close(fileInputStream);
                    }
                }).toArray(byte[][]::new);

        /*boolean flag = true;
        for (int i = 0; i < 200; i++) {
            Byte b = null;
            for (byte[] string : strings) {
                b = Optional.ofNullable(b).orElse(string[i]);
                flag = flag && (b == string[i]);
            }
            if (!flag)
                break;
            builder.append(Integer.toHexString(b + 128)).append(",");
        }*/
        String builder = check(strings);
        System.out.println("共同:" + builder);
        System.out.println();
        /*String s = String.valueOf(b);
        System.out.println(s);
        s = s.substring(0, 10);
        System.out.println(s);*/
        Assert.isTrue(!StringUtils.isEmpty(builder), "存在有问题或不是同一类型的文件");
        return builder;
    }

    public static String check(byte[]... bytes) {
        StringBuilder builder = new StringBuilder();
        boolean flag = true;
        for (int i = 0; i < 200; i++) {
            Byte b = null;
            for (byte[] bytes1 : bytes) {
                b = Optional.ofNullable(b).orElse(bytes1[i]);
                flag = flag && (b == bytes1[i]);
            }
            if (!flag)
                break;
            builder.append(toHexString(b)).append(",");
        }
        return builder.substring(0, Math.max(builder.length() - 1, 0));
    }

    public static String toHexString(byte b) {
        return Integer.toHexString(Byte.toUnsignedInt(b));
    }

    public static void checkGroup(String... magicCode) {
        String s = check(Arrays.stream(magicCode).map(String::getBytes).toArray(byte[][]::new));
        Assert.isTrue(StringUtils.isEmpty(s), "存在类似");
    }

    public static void main(String[] args) {
        checkGroup(
                check("d:\\Users\\Septem\\Downloads\\教育部学历证书电子注册备案表_王芝栋.pdf"
                        , "d:\\Users\\Septem\\Downloads\\中国高等教育学位在线验证报告_王芝栋.pdf"
                        , "d:\\Users\\Septem\\Downloads\\住房调查证明模板.pdf"
                        , "d:\\Users\\Septem\\Downloads\\(15-N-GC-00225-F-001)-2015年中国电信广西LTE（4.1期）工程建设玉林本地网光缆项目兴业交通局、新公安局、六联基站光缆线路单位工程光缆线路单位工程（图纸）-2.pdf"
//                        , "d:\\Users\\Septem\\Downloads\\T236号18GX002284006（ZR2018-GZ-092）第二批工程容县松山镇新光村大坡脚队、松山村枇杷队、罗江镇岑冲村井田队、黎村镇珊萃村山心队、杨村镇平贯村半岭队等FTTH光缆线路单位工2 (1).pdf"
                ),
                check(
                        "d:\\Users\\Septem\\Downloads\\重庆电信PF与资源系统接口规范v8.5(20231120)  (2).doc"
                        , "d:\\Users\\Septem\\Downloads\\自测报告 2.doc"
                ),
                check(
                        "d:\\Users\\Septem\\Downloads\\沉淀期规则查询_20230529114526.xls"
                        , "d:\\Users\\Septem\\Downloads\\资源系统接口总表O3 1228 222.xls"
                ),
                check("d:\\Users\\Septem\\Downloads\\2023121200329 关于云专网旧值的处理需求.docx"
                        , "d:\\Users\\Septem\\Downloads\\广西电信资源中心版本发布操作手册-V1.0.docx"
                ),
                check("d:\\Users\\Septem\\Downloads\\政企业务模型设计【重庆电信】 - 0104.xlsx"
                        , "d:\\Users\\Septem\\Downloads\\广西电信新一代系统【上线】问题记录处理跟踪表.xlsx"
                ));
        if (true) {
            return;
        }
    }
}
