package cn.tellyouwhat.checkinsystem.domain;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class NetTask implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;


	private String url;
	private int method;// 0:post 1: get
	private Map<String, String> headers = new HashMap<String, String>();
	private Map<String, String> parameters = new HashMap<String, String>();

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getMethod() {
		return method;
	}

	public void setMethod(int method) {
		this.method = method;
	}

	public Map<String, String> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}

	public Map<String, String> getParameters() {
		return parameters;
	}

	public void setParameters(Map<String, String> parameters) {
		this.parameters = parameters;
	}

}
