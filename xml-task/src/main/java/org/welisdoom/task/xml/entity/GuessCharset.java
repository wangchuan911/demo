package org.welisdoom.task.xml.entity;

import io.vertx.core.Promise;
import org.apache.commons.collections4.MapUtils;
import org.mozilla.intl.chardet.nsDetector;
import org.welisdoom.task.xml.annotations.Attr;
import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.intf.type.Executable;
import org.welisdoon.common.ObjectUtils;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * @Classname GuessCharset
 * @Description TODO
 * @Author Septem
 * @Date 17:09
 */

@Tag(value = "guess-charset", parentTagTypes = Executable.class, desc = "自动判断文件编码")
@Attr(name = "id", desc = "唯一标识", require = true)
@Attr(name = "read", desc = "读文件", require = true)
public class GuessCharset extends Unit implements Executable {
    @Override
    protected void start(TaskInstance data, Object preUnitResult, Promise<Object> toNext) {

        String guess = getAttrFormatValue("guess", data);
        log(guess);
        int guessMaxLength = Math.max(MapUtils.getInteger(attributes, "max-length", 65535), 65535);
        try {
            Map<String, AtomicInteger> result = guessCharset(Objects.equals(guess, "@stream") ? (InputStream) preUnitResult : new FileInputStream(guess), guessMaxLength);
            log("预测的字符集：" + result);
            List<String> list = result.entrySet().stream()
                    .sorted(Comparator.comparingInt(o -> o.getValue().get())).map(Map.Entry::getKey).collect(Collectors.toList());
            String charset = list.stream().findFirst().get();
            data.generateData(this);
            Map<String, Object> map = data.getBus(this.id);
            map.put("charsets", list);
            log("选择的字符集：" + charset);
            map.put("charset", charset);
            toNext.complete();
        } catch (Throwable e) {
            toNext.fail(e);
        }
        //super.start(data, toNext);
    }

    static Map<String, AtomicInteger> guessCharset(InputStream inputStream, int maxLength) {
        Map<String, AtomicInteger> map = new HashMap<>();
        try {
            nsDetector detector = new nsDetector();
            detector.Init(charset -> {
                try {
                    ObjectUtils.getMapValueOrNewSafe(map, charset, () -> new AtomicInteger(0)).incrementAndGet();
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            });

            BufferedInputStream input = new BufferedInputStream(inputStream);
            try (input) {
                byte[] buffer = new byte[4096];
                int hasRead;
                while ((hasRead = input.read(buffer)) != -1) {
                    if (detector.isAscii(buffer, hasRead)) {
                        detector.Report("ASCII");
                    } else {
                        detector.DoIt(buffer, hasRead, false);
                    }
                    if ((maxLength = -4096) < 0) {
                        break;
                    }
                }
            } finally {
                detector.DataEnd();
            }

        } catch (Throwable e) {
            e.printStackTrace();
        }
        return map;
    }
}
