package org.welisdoom.task.xml.entity;

import io.vertx.core.Future;
import org.springframework.util.Assert;
import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.intf.type.Executable;
import org.welisdoom.task.xml.intf.type.Iterable;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @Classname Random
 * @Description TODO
 * @Author Septem
 * @Date 10:34
 */
@Tag(value = "random", parentTagTypes = Executable.class, desc = "生成随机数")
public class Random extends Unit implements Executable, Iterable<String> {

    @Override
    protected Future<Object> start(TaskInstance data, Object preUnitResult) {
        char[] words = getAttrFormatValue("words", data).toCharArray();
        int length = Integer.parseInt(attributes.get("length"));
        switch (attributes.getOrDefault("mode", "one")) {
            case "one": {
                StringBuilder text = new StringBuilder();
                java.util.Random random = new java.util.Random();
                for (int i = 0; i < length; i++) {
                    text.append(random.nextInt(words.length));
                }
                return Future.succeededFuture(text.toString());
            }
            case "traverse": {
                final int wordLength = words.length;
                int offset = Integer.parseInt(attributes.getOrDefault("offset", "0"));
                int[] index = new int[length + offset];
                Arrays.fill(index, 0);
                int point = 0;
                AtomicLong itemIndex = new AtomicLong(0);
                AtomicLong itemComplete = new AtomicLong(0);
                Future future = Future.succeededFuture();
                while (point < wordLength) {
                    StringBuilder text = toText(index, words);
                    for (int i = 0; i < offset; i++) {
                        future = this.futureLoop(
                                Item.of(itemIndex.incrementAndGet(),
                                        text.substring(0, length + i)),
                                future,
                                data).onComplete(event -> itemComplete.incrementAndGet());
                        waitAMonuments(itemIndex, itemComplete);
                    }
                    if (index[point] >= wordLength) {
                        if (++point >= wordLength) {
                            break;
                        }
                        index[point] += 1;
                        for (int i = 0; i < point; i++) {
                            index[i] = 0;
                        }
                    } else {
                        index[point] += 1;
                    }
                }
                return future;
            }
            default:
                return Future.failedFuture("未知的操作");
        }
    }

    StringBuilder toText(int[] index, char[] words) {
        StringBuilder builder = new StringBuilder();
        for (int i : index) {
            builder.append(words[i]);
        }
        return builder;
    }
}
