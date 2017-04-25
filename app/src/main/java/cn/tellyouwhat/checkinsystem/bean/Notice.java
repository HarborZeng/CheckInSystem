package cn.tellyouwhat.checkinsystem.bean;

import java.io.Serializable;

/**
 * Created by Harbor-Laptop on 2017/4/19.
 */

public class Notice implements Serializable {
	private String title;
	private String content;
	private String time;
	private String author;

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
}
