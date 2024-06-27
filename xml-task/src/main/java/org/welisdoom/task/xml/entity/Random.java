package org.welisdoom.task.xml.entity;

import io.vertx.core.Future;
import org.springframework.util.StringUtils;
import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.intf.type.Executable;
import org.welisdoom.task.xml.intf.type.Iterable;
import org.welisdoom.task.xml.intf.type.Script;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Classname Random
 * @Description TODO
 * @Author Septem
 * @Date 10:34
 */
@Tag(value = "random", parentTagTypes = Executable.class, desc = "生成随机数")
public class Random extends Unit implements Executable, Iterable<String> {

    protected WordRange[] getWordGroup(TaskInstance data) {
//        final String key = "--range";
//        String words = getAttrFormatValue("words", data).trim();
//        if (words.contains(key)) {
//            List<WordRange> list = new LinkedList<>();
//            for (String s : words.split(key)) {
//                if (StringUtils.isEmpty(s)) continue;
//                String[] args = s.trim().split("\\s+");
//                switch (args.length) {
//                    case 3:
//                        list.add(new WordRange(args[0].toCharArray(), Integer.parseInt(args[1]), Integer.parseInt(args[1])));
//                        break;
//                    case 2:
//                        list.add(new WordRange(args[0].toCharArray(), Integer.parseInt(args[1]), -1));
//                        break;
//                    case 1:
//                        list.add(new WordRange(args[0].toCharArray()));
//                        break;
//                    default:
//                        continue;
//                }
//            }
//            return list.toArray(WordRange[]::new);
//        }
//        return new WordRange[]{new WordRange(words.toCharArray())};
        return getChild(WordRange.class).stream().map(wordRange -> wordRange.range(data)).toArray(WordRange[]::new);
    }

    public static class AtomBigDecimal {
        public AtomBigDecimal(String s) {
            value = new BigDecimal(s);
        }

        public AtomBigDecimal(long s) {
            value = new BigDecimal(s);
        }

        BigDecimal value;

        public final BigDecimal getAndIncrement() {
            return getAndAdd(BigDecimal.ONE);
        }

        public final BigDecimal getAndDecrement() {
            BigDecimal old = value;
            value = value.subtract(BigDecimal.ONE);
            return old;
        }

        public final BigDecimal getAndAdd(BigDecimal delta) {
            BigDecimal old = value;
            value = getAndAdd(delta);
            return old;
        }

        public final BigDecimal incrementAndGet() {
            return addAndGet(BigDecimal.ONE);
        }

        public final BigDecimal decrementAndGet() {
            return value = value.subtract(BigDecimal.ONE);
        }

        public final BigDecimal addAndGet(BigDecimal delta) {
            return value.add(delta);
        }

        public BigDecimal get() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            return value.equals(o);
        }

        @Override
        public int hashCode() {
            return value.hashCode();
        }
    }

    @Override
    protected Future<Object> start(TaskInstance data, Object preUnitResult) {
        WordRange[] wordGroup = getWordGroup(data);
        char[] words = Arrays.stream(wordGroup).map(wordRange -> wordRange.words).map(String::new).collect(Collectors.joining("")).toCharArray();
        int length = Integer.parseInt(attributes.get("length"));
        StringBuilder text = new StringBuilder();
        final BigDecimal times = new BigDecimal(attributes.getOrDefault("times", "1"));
        final int wordLength = words.length;
        int offset = Integer.parseInt(attributes.getOrDefault("offset", "0"));

        if (times.equals(BigDecimal.ONE)) {
            java.util.Random random = new java.util.Random();
            List<String> list = new LinkedList<>();
            for (int j = 0; j <= offset; j++) {
                do {
                    text.setLength(0);
                    for (int i = 0; i < length + j; i++) {
                        text.append(words[random.nextInt(words.length)]);
                    }
                } while (!check(wordGroup, text));
                list.add(text.toString());
            }
            log(list.stream().collect(Collectors.joining("\n")));
            return Future.succeededFuture(list.stream().collect(Collectors.joining("\n")));
        }

        int[] index = new int[length + offset];
        Arrays.fill(index, 0);
        int point = 0;
        AtomBigDecimal itemIndex = new AtomBigDecimal(0);
        AtomBigDecimal itemComplete = new AtomBigDecimal(0);
        Future future = Future.succeededFuture();
        BigDecimal combination = new BigDecimal(words.length).pow(index.length);
        BigDecimal count = new BigDecimal("0");
        long time = System.currentTimeMillis();
        log("预计有{}种组合", combination);
        while (point < wordLength) {
            toText(text, index, words);
            for (int i = 0; i <= offset; i++) {
                if (check(wordGroup, text)) {
                    if (times.equals(BigDecimal.ONE) && offset == 0) {
                        return Future.succeededFuture(text);
                    }
                    log("{}", text.substring(0, length + i));
                    future = this.futureLoop(
                            Item.of(Math.abs(itemIndex.incrementAndGet().longValue()),
                                    text.substring(0, length + i)),
                            future,
                            data).onComplete(event -> itemComplete.incrementAndGet());
                    waitAMonuments(itemIndex.get(), itemComplete.get());
                }
            }
            if (++index[point] >= wordLength) {
                if (++point >= index.length) {
                    break;
                }
                while (++index[point] >= wordLength) {
                    if (++point >= index.length) {
                        break;
                    }
                }

                for (int i = 0; i < point; i++) {
                    index[i] = 0;
                }
                point = 0;
            }
            count.add(BigDecimal.ONE);
            if (System.currentTimeMillis() - time >= 10000) {
                time = System.currentTimeMillis();
                log("{},()", count, combination);
            }
        }
        return future;
    }

    void toText(StringBuilder builder, int[] index, char[] words) {
        builder.setLength(0);
        for (int i : index) {
            builder.append(words[i]);
        }
    }

    boolean check(WordRange[] wordGroup, StringBuilder text) {
        return Arrays.stream(wordGroup).allMatch(wordRange -> wordRange.isRange(text));
    }

    @Tag(value = "range", parentTagTypes = Random.class, desc = "范围限定")
    public static class WordRange extends Unit implements Script {
        char[] words;
        int min;
        int max;

        boolean isRange(StringBuilder text) {
            if (min == 1 && max < 0) {
                return text.chars().anyMatch(this::isRange);
            }
            long match = text.chars().filter(this::isRange).count();
            return (match >= min && (max < 0 || match <= max));
        }

        boolean isRange(int value) {
            for (char word : words) {
                if (word == value) {
                    return true;
                }
            }
            return false;
        }

        public WordRange range(TaskInstance data) {
            this.words = getAttrFormatValue("words", data).toCharArray();
            this.min = Integer.parseInt(attributes.getOrDefault("min", "1"));
            this.max = Integer.parseInt(attributes.getOrDefault("max", "-1"));
            return this;
        }
    }
}
