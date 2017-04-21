package com.chinasofti.yizhuoyan.year20.model.impl;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.util.EntityUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.chinasofti.yizhuoyan.year20.model.AppModel;
import com.chinasofti.yizhuoyan.year20.util.HttpPostUtil;

public class HttpURLConnectionImpl extends AppModel {
	private Callback callback;

	@Override
	public void doAgeing(Callback cb) {
		this.callback = cb;
		try {
			doWork(isMale(), getAgeingYear(), getPhoto());
		} catch (Exception e) {
			e.printStackTrace();
			callback.error(e);
		}
	}

	private void doWork(boolean male, int ageingYear, File photo)
			throws Exception {
		HttpPostUtil post = new HttpPostUtil(URL_UPLOAD);
		post.addParameter("action", "upload");
		post.addParameter("gender", male ? "male" : "female");
		post.addParameter("age", String.valueOf(ageingYear));
		post.addParameter("drugs", "0");
		post.addFile("photofile", photo);
		
		post.submit(post.new Callback() {
			@Override
			protected void on200(byte[] data) throws Exception {
				String respString = new String(data, getContentEncoding());
				System.out.println(respString);
				JSONObject uploadResult = JSON.parseObject(respString);
				System.out.println("上传成功!" + uploadResult);
				checkImage(uploadResult);
			}

			@Override
			protected void onOther(Exception e) {
				callback.error(e);
			}
		});

	}

	private void checkImage(final JSONObject uploadResult) throws Exception {
		String imageId = uploadResult.getString("key");
		HttpPostUtil post = new HttpPostUtil(URL_CHECKRESULT);
		post.addParameter("action", "check");
		post.addParameter("image_id", imageId);
		post.submit(post.new Callback() {
			@Override
			protected void on200(byte[] data) throws Exception {
				String respString = new String(data, this.getContentEncoding());
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

			protected void onOther(Exception e) {
				callback.error(e);
			}
		});
	}

}
