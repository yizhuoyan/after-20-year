package com.chinasofti.yizhuoyan.year20.model.impl;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.chinasofti.yizhuoyan.year20.model.AppModel;
import com.chinasofti.yizhuoyan.year20.util.HttpPostUtil;

public class HttpClientImpl extends AppModel{
	private ExecutorService workExecutor = Executors.newSingleThreadExecutor();
	private HttpClient client;
	private volatile Callback callback;

	public HttpClientImpl() {
		initHttpClient();
	}

	private void initHttpClient() {
		List<Header> headers = new LinkedList<Header>();
		headers.add(new BasicHeader("Accept-Encoding", "gzip, deflate, sdch"));
		headers.add(new BasicHeader("Accept-Language",
				"en-US,en;q=0.8,zh-CN;q=0.6,zh;q=0.4"));
		headers.add(new BasicHeader("Cache-Control", "no-cache"));
		headers.add(new BasicHeader("Connection", "keep-alive"));
		headers.add(new BasicHeader("Host", "www.in20years.com"));
		headers.add(new BasicHeader("Pragma", "no-cache"));
		headers.add(new BasicHeader("Upgrade-Insecure-Requests", "1"));
		headers.add(new BasicHeader(
				"User-Agent",
				"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36"));
		headers.add(new BasicHeader("Accept",
				"text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8"));
		client = HttpClients.custom().setDefaultHeaders(headers)
				.setDefaultCookieStore(new BasicCookieStore())
				.build();
	}

	private void doWork(boolean male, int ageingYear, File photo) throws Exception {
		HttpPost req = new HttpPost(URL_UPLOAD);

		HttpEntity reqEntity = MultipartEntityBuilder.create()
				.addTextBody("action", "upload")
				.addTextBody("gender",male ? "male" : "female")
				.addTextBody("age", String.valueOf(ageingYear))
				.addTextBody("drugs", "0")
				.addBinaryBody("photofile",photo).build();
		req.setEntity(reqEntity);

		HttpResponse resp = client.execute(req);
		if (resp.getStatusLine().getStatusCode() != 200) {
			throw new RuntimeException(resp.getStatusLine().getReasonPhrase());
		}
		HttpEntity respEntity = resp.getEntity();
		String respString = EntityUtils.toString(respEntity);
		JSONObject uploadResult = JSON.parseObject(respString);
		System.out.println("上传成功!" + uploadResult);
		checkImage(uploadResult);
	}

	

	@Override
	public void doAgeing(Callback cb) {
		this.callback=cb;
		this.workExecutor.execute(new Runnable() {
			@Override
			public void run() {
				try {
					doWork(isMale(),getAgeingYear(),getPhoto());
				} catch (Exception e) {
					callback.error(e);
				}
			}
		});
	}

	private void checkImage(JSONObject uploadResult) throws Exception {
		String imageId = uploadResult.getString("key");
		HttpUriRequest req = RequestBuilder.post(URL_CHECKRESULT)
				.addParameter("action", "check")
				.addParameter("image_id", imageId).build();
		HttpResponse resp = client.execute(req);
		String respString = EntityUtils.toString(resp.getEntity());
		JSONObject checkResult = JSON.parseObject(respString);
		if (checkResult.getInteger("ok") == 1) {
			if (checkResult.getInteger("ready") == 0) {
				System.out.println("还未完毕,重试");
				Thread.sleep(CHECK_INTERVAL);
				// retry
				checkImage(uploadResult);
			} else {
				String resultUrl = checkResult.getString("result_url");
				System.out.println("处理完毕" + resultUrl);
				callback.done(resultUrl);
			}
		}
	}


}
