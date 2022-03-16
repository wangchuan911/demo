package org.welisdoon.common.entity;

import java.util.*;

/**
 * @Classname Line
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/2/7 16:16
 */
public class Line {
    final static Line.Section.Point END_POINT = new Line.Section.Point();

    String lineId;
    Section.Point[] points;

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Line{");
        sb.append("lineId='").append(lineId).append('\'');
        sb.append(", points=").append(Arrays.toString(points));
        sb.append('}');
        return sb.toString();
    }

    public String getLineId() {
        return lineId;
    }

    public Line setLineId(String lineId) {
        this.lineId = lineId;
        return this;
    }

    public Section.Point[] getPoints() {
        return points;
    }

    public Line setPoints(Section.Point[] points) {
        this.points = points;
        return this;
    }

    public static class Section {
        String sectionId, lineId;
        Point head;
        Point foot;

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Section{");
            sb.append(head);
            sb.append(",").append(foot);
            sb.append('}');
            return sb.toString();
        }

        public String getSectionId() {
            return sectionId;
        }

        public Section setSectionId(String sectionId) {
            this.sectionId = sectionId;
            return this;
        }

        public Point getHead() {
            return head;
        }

        public Section setHead(Point head) {
            this.head = head;
            return this;
        }

        public Point getFoot() {
            return foot;
        }

        public Section setFoot(Point foot) {
            this.foot = foot;
            return this;
        }

        public String getLineId() {
            return lineId;
        }

        public Section setLineId(String lineId) {
            this.lineId = lineId;
            return this;
        }

        public static class Point {
            String pointId, sectionId, lineId;
            int salt;

            @Override
            public String toString() {
                final StringBuilder sb = new StringBuilder("Point{");
                sb.append("pointId='").append(pointId).append('\'');
                sb.append('}');
                return sb.toString();
            }

            public String getPointId() {
                return pointId;
            }

            public Point setPointId(String pointId) {
                this.pointId = pointId;
                return this;
            }

            public int getSalt() {
                return salt;
            }

            public Point setSalt(int salt) {
                this.salt = salt;
                return this;
            }

            public String getSectionId() {
                return sectionId;
            }

            public String getLineId() {
                return lineId;
            }

            public Point setLineId(String lineId) {
                this.lineId = lineId;
                return this;
            }

            public Point setSectionId(String sectionId) {
                this.sectionId = sectionId;
                return this;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                Point point = (Point) o;
                return salt == point.salt &&
                        Objects.equals(pointId, point.pointId) &&
                        Objects.equals(lineId, point.lineId);
            }

            @Override
            public int hashCode() {
                return Objects.hash(pointId, lineId, salt);
            }
        }

        public static Line.Section[] toSection(Line.Section.Point[] points) {
            Map<String, Section> s = new HashMap<>();
            Arrays.stream(points).forEach(point -> {
                if (point.sectionId == null || point.sectionId.length() == 0) return;
                String key = String.format("%s_%s", point.lineId, point.sectionId);
                if (s.containsKey(key)) {
                    if (s.get(key).getFoot() == null)
                        s.get(key).setFoot(point);
                } else {
                    s.put(key, new Line.Section().setHead(point));
                }
            });
            return s.entrySet().stream().map(stringSectionEntry -> stringSectionEntry.getValue())
                    .filter(section -> section.foot != null && section.head != null)
                    .toArray(Line.Section[]::new);
        }
    }

        /*static void toString(Map<Line.Section.Point, Line.Section.Point> lines, Set<Line.Section.Point> lineHaed) {
            System.out.println(Arrays.toString(lineHaed.stream().map(point1 -> {
                List<Line.Section.Point> list = new LinkedList<>();
                Line.Section.Point point = point1;
                list.add(point);
                while ((point = lines.get(point)) != END_POINT && point != null) {
                    list.add(point);
                }
                return new Line().setPoints(list.toArray(Line.Section.Point[]::new)).setLineId(point1.lineId);
            }).toArray(Line[]::new)));
            ;
        }*/

    public static Line[] toLine(Line.Section[] sections) {
        Map<Line.Section.Point, Line.Section.Point> lines = new HashMap<>();
        Set<Section.Point> lineHaed = new HashSet<>();
        Arrays.stream(sections).forEach(section -> {
            if (link(section.head, section.foot, lineHaed, lines) || link(section.foot, section.head, lineHaed, lines)) {

            }
        });
        return lineHaed.stream().map(point1 -> {
            List<Line.Section.Point> list = new LinkedList<>();
            Line.Section.Point point = point1;
            list.add(point);
            while ((point = lines.remove(point)) != END_POINT && point != null) {
                list.add(point);
            }
            return new Line().setPoints(list.toArray(Line.Section.Point[]::new)).setLineId(point1.lineId);
        }).toArray(Line[]::new);
    }

    static boolean link(Line.Section.Point start, Line.Section.Point end, Set<Line.Section.Point> lineHaed, Map<Line.Section.Point, Line.Section.Point> lines) {
        boolean START_CONTAINS_HEAD = lineHaed.contains(start),
                START_LINE_CONTAINS = lines.containsKey(start),
                START_LINE_END = lines.get(start) == END_POINT,
                END_CONTAINS_HEAD = lineHaed.contains(end),
                END_LINE_CONTAINS = lines.containsKey(end),
                END_LINE_END = lines.get(end) == END_POINT;
        if (START_CONTAINS_HEAD && END_CONTAINS_HEAD) {
            //两个A端连在一起
            lineHaed.remove(start);
            Line.Section.Point p = lines.put(end, start), p2 = end, p3;
            while (p != null && p != END_POINT) {
                p3 = p;
                p = lines.put(p, p2);
                if (p == END_POINT) {
                    lineHaed.remove(end);
                    lineHaed.add(p3);
                }
                p2 = p3;
            }
        } else if (START_CONTAINS_HEAD) {
            // （1） A端 存在 更新 A端
            lineHaed.remove(start);
            lineHaed.add(end);
            lines.put(end, start);
        } else if (START_LINE_CONTAINS && START_LINE_END) {
            // （1） Z端 存在 更新 Z端
            // （2）如果z端存在其他线中，调整end的hash,作为新的节点
            while (lines.containsKey(end)) {
                end.salt++;
            }
            lines.put(end, lines.put(start, end));
        } else if (!START_CONTAINS_HEAD && START_LINE_CONTAINS && START_LINE_END) {
            // （1） 点 不在 AZ端， 则新增一个独立的线
            start.salt++;
            return link(start, end, lineHaed, lines);
        } else if (!START_CONTAINS_HEAD && !START_LINE_CONTAINS && !END_CONTAINS_HEAD && !END_LINE_CONTAINS) {
            // （1） 点 不存在， 则新增一个独立的线
            lineHaed.add(start);
            lines.put(start, end);
            lines.put(end, END_POINT);
        } else {
            return false;
        }
            /*toString(lines, lineHaed);
            System.out.println("----------------------");*/
        return true;
    }

    public static Line[] toLine(Line.Section.Point[] points) {
        return toLine(Section.toSection(points));
    }


    public static void main(String[] args) {
        List<Line.Section.Point> points = new LinkedList<>();
        String id = "233333";
        for (int i = 0, j = 0; i < 10; i++) {
            Line.Section.Point point = new Line.Section.Point().setLineId("1");
            point.setPointId((j = (i % 2 == 0 ? j : (int) (Math.random() * 1000))) + "");
            point.setSectionId(Math.floor(i / 2) + "");
            points.add(point);
            if (i % 2 == 1 || i == 0) {
                System.out.println(point.getPointId());
            }
            if (i == 3)
                id = point.pointId;
        }
        System.out.println("------------------" + id);
        for (int i = 10, j = Integer.parseInt(id); i < 16; i++) {
            Line.Section.Point point = new Line.Section.Point().setLineId("1");

            point.setPointId((j = (i % 2 == 0 ? j : (int) (Math.random() * 1000))) + "");
            if (i % 2 == 1 || i == 10) {
                System.out.println(point.getPointId());
            }
            point.setSectionId(Math.floor(i / 2) + "");
            points.add(point);
        }*/
        System.out.println(Arrays.toString(Line.toLine(points.stream().toArray(Line.Section.Point[]::new))));
    }
}
