package org.welisdoon.web.entity.wechat.custom;

import java.util.LinkedList;
import java.util.List;

public class CustomNewsMessage extends CustomMessage {
    News news;

    public CustomNewsMessage() {
        this.msgtype = MsgType.NEWS.value;
    }

    public static class News {
        public static class New {
            String title, description, url, picurl;

            public String getTitle() {
                return title;
            }

            public void setTitle(String title) {
                this.title = title;
            }

            public String getDescription() {
                return description;
            }

            public void setDescription(String description) {
                this.description = description;
            }

            public String getUrl() {
                return url;
            }

            public void setUrl(String url) {
                this.url = url;
            }

            public String getPicurl() {
                return picurl;
            }

            public void setPicurl(String picurl) {
                this.picurl = picurl;
            }
        }

        List<New> articles;

        public List<New> getArticles() {
            return articles;
        }

        public void setArticles(List<New> articles) {
            this.articles = articles;
        }
    }

    public News getNews() {
        return news;
    }

    public CustomNewsMessage setNews(News news) {
        this.news = news;
        return this;
    }

    public CustomNewsMessage addNew(String title, String description, String url, String picurl) {
        if (this.news == null) {
            this.news = new News();
            if (this.news.articles == null) {
                this.news.articles = new LinkedList<>();
            }
        }
        News.New n = new News.New();
        n.title = title;
        n.description = description;
        n.picurl = picurl;
        n.url = url;
        this.news.articles.add(n);
        return this;
    }
}
