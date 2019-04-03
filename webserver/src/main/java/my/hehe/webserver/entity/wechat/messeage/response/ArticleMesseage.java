package my.hehe.webserver.entity.wechat.messeage.response;

import my.hehe.webserver.entity.wechat.messeage.MesseageTypeEnum;
import my.hehe.webserver.entity.wechat.messeage.request.RequestMesseage;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "xml")
@XmlAccessorType(XmlAccessType.FIELD)
public class ArticleMesseage extends ResponseMesseage {

	@XmlElement(name = "ArticleCount")
	private int articleCount;

	@XmlElementWrapper(name = "Articles")
	@XmlElement(name = "item")
	private final List<Article> articles = new ArrayList<Article>();

	public void setArticleInfo(String Title, String Description, String PicUrl,
			String Url) {
		Article article = new Article();
		article.setTitle(Title);
		article.setDescription(Description);
		article.setPicUrl(PicUrl);
		article.setUrl(Url);
		this.articles.add(article);
		this.articleCount = articles.size();

	}

	public ArticleMesseage() {
		super();
		// TODO Auto-generated constructor stub
	}

	public ArticleMesseage(RequestMesseage msg, Long createTime) {
		super(msg, createTime,MesseageTypeEnum.ARTICLE.getValue());

		// TODO Auto-generated constructor stub
	}

	public ArticleMesseage(RequestMesseage msg) {
		super(msg, MesseageTypeEnum.ARTICLE.getValue());
		// TODO Auto-generated constructor stub
	}

	public ArticleMesseage(String toUserName, String fromUserName,
						   Long createTime) {
		super(toUserName, fromUserName, createTime, MesseageTypeEnum.ARTICLE.getValue());
		// TODO Auto-generated constructor stub
	}

	public ArticleMesseage(String toUserName, String fromUserName) {
		super(toUserName, fromUserName, MesseageTypeEnum.ARTICLE.getValue());
		// TODO Auto-generated constructor stub
	}

	@Override
	public String toString() {
		return "ArticleMesseage{" +
				"articleCount=" + articleCount +
				", articles=" + articles +
				", toUserName='" + toUserName + '\'' +
				", fromUserName='" + fromUserName + '\'' +
				", createTime=" + createTime +
				", msgType='" + msgType + '\'' +
				'}';
	}
}

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
class Article {

	@XmlElement(name = "Title")
	private String title;
	@XmlElement(name = "Description")
	private String description;
	@XmlElement(name = "PicUrl")
	private String picUrl;
	@XmlElement(name = "Url")
	private String url;

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public String getPicUrl() {
		return picUrl;
	}

	public String getUrl() {
		return url;
	}

	public void setPicUrl(String picUrl) {
		this.picUrl = picUrl;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setDescription(String description) {
		this.description = description;
	}

}