package org.welisdoom.task.xml.entity;

import io.vertx.core.Future;
import net.sf.sevenzipjbinding.*;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import org.springframework.util.StringUtils;
import org.welisdoom.task.xml.annotations.Attr;
import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.intf.type.Executable;

import java.io.File;
import java.io.*;
import java.util.Locale;

/**
 * @Classname Break
 * @Description TODO
 * @Author Septem
 * @Date 16:39
 */
@Tag(value = "uncompress", parentTagTypes = Executable.class, desc = "解缩")
@Attr(name = "file", desc = "文件")
public class Uncompress extends Unit implements Executable {
    @Override
    protected Future<Object> start(TaskInstance data, Object preUnitResult) {
        try {
            String file = getAttrFormatValue("file", data);
            String toPath = attributes.containsKey("to") ?
                    getAttrFormatValue("to", data) :
                    file.substring(0, file.lastIndexOf("."));
            String password = getAttrFormatValue("pw", data);
            switch (file.substring(file.lastIndexOf(".") + 1).toLowerCase(Locale.ROOT)) {
                case "RAR":
                    try {
                        // 第一个参数是需要解压的压缩包路径，第二个参数参考JdkAPI文档的RandomAccessFile
                        //r代表以只读的方式打开文本，也就意味着不能用write来操作文件
                        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "r");

                        IInArchive archive = StringUtils.isEmpty(password) ?
                                SevenZip.openInArchive(null, new RandomAccessFileInStream(randomAccessFile))
                                : SevenZip.openInArchive(null, new RandomAccessFileInStream(randomAccessFile), password);

                        int[] in = new int[archive.getNumberOfItems()];
                        for (int i = 0; i < in.length; i++) {
                            in[i] = i;
                        }
                        //拼接输出目录文件路径
                        String dest = toPath + File.separator;

                        archive.extract(in, false, new IArchiveExtractCallback() {


                            @Override
                            public void setCompleted(long arg0) {
                            }

                            @Override
                            public void setTotal(long arg0) {
                            }

                            @Override
                            public ISequentialOutStream getStream(int index, ExtractAskMode extractAskMode) throws SevenZipException {
                                final String path = (String) archive.getProperty(index, PropID.PATH);
                                final boolean isFolder = (boolean) archive.getProperty(index, PropID.IS_FOLDER);
                                return data -> {
                                    try {
                                        if (!isFolder) {
                                            File file = new File(dest + path);
                                            saveFile(file, data);
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    return data.length;
                                };
                            }

                            @Override
                            public void prepareOperation(ExtractAskMode arg0) {
                            }

                            @Override
                            public void setOperationResult(ExtractOperationResult extractOperationResult) {

                            }

                            public void saveFile(File file, byte[] msg) {
                                OutputStream fos = null;
                                try {
                                    File parent = file.getParentFile();
                                    if ((!parent.exists()) && (!parent.mkdirs())) {
                                        return;
                                    }
                                    fos = new FileOutputStream(file, true);
                                    fos.write(msg);
                                    fos.flush();
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } finally {
                                    try {
                                        fos.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });
                        archive.close();
                        randomAccessFile.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

            }
            return Future.succeededFuture(toPath);
        } catch (Throwable e) {
            return Future.failedFuture(e);
        }
    }

}
