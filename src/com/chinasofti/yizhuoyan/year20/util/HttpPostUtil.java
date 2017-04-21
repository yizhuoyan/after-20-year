package com.chinasofti.yizhuoyan.year20.util;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.incors.plaf.alloy.ca;

public class HttpPostUtil {
	public abstract class Callback {

		protected abstract void on200(byte[] data) throws Exception;

		protected void onOther(Exception e) {
			e.printStackTrace();
		}

		protected final String getContentType() {
			return http.getContentType();
		}

		protected final String getContentEncoding() {
			String encoding = http.getContentEncoding();
			encoding = encoding == null ? "UTF-8" : encoding;
			return encoding;
		}
	}

	private static final String ENCTYPE_MULTIPARTFROMDATA = "multipart/form-data",
			ENCTYPE_FORMURLENCODED = "application/x-www-form-urlencoded";
	private static final String PREFIX = "--", CRLF = "\r\n";
	/**
	 * url
	 */
	private String url;
	/** 连接超时 */
	private int timeout = 30000;
	/**
	 * 请求参数字符编码
	 */
	private String requestCharEncoding = "utf-8";
	/**
	 * 核心对象,http连接对象
	 */
	private HttpURLConnection http;
	/**
	 * 请求参数
	 */
	private Map<String, List<Object>> paramsMap;
	/**
	 * 请求头
	 */
	private Map<String, List<Object>> headerMap;
	/**
	 * 请求文件
	 */
	private Map<String, File> filesMap;
	/**
	 * BOUNDARY
	 */
	private String BOUNDARY;

	private static String randomBoundary() {
		return "----" + java.util.UUID.randomUUID().toString();
	}

	public HttpPostUtil(String url) {
		this.url = url;
	}

	public HttpPostUtil setRequestCharEncoding(String requestCharEncoding) {
		this.requestCharEncoding = requestCharEncoding;
		return this;
	}

	public HttpPostUtil setUrl(String url) {
		this.url = url;
		return this;
	}

	public HttpPostUtil setTimeout(int timeout) {
		this.timeout = timeout;
		return this;
	}

	/**
	 * 添加请求头
	 *
	 * @param key
	 * @param value
	 * @return
	 */
	public HttpPostUtil addHeader(String key, String value) {
		Map<String, List<Object>> map = this.headerMap;
		if (map == null) {
			this.headerMap = map = new HashMap<>();
		}
		List<Object> values = map.get(key);
		if (values == null) {
			map.put(key, (values = new LinkedList<Object>()));
		}
		values.add(value);
		return this;
	}

	/**
	 * 添加请求参数
	 *
	 * @param key
	 *            参数名
	 * @param value
	 *            参数值,可以是原生类型,Collection,数组
	 * @return
	 */
	public HttpPostUtil addParameter(String key, Object value) {
		Map<String, List<Object>> map = this.paramsMap;
		if (map == null) {
			this.paramsMap = map = new HashMap<>();
		}
		List<Object> values = map.get(key);
		if (values == null) {
			map.put(key, (values = new LinkedList<Object>()));
		}
		if (value.getClass().isArray()) {
			int len = Array.getLength(value);
			for (int i = 0; i < len; i++) {
				values.add(Array.get(value, i));
			}
		} else if (value instanceof Collection) {
			values.addAll((Collection) value);
		} else {
			values.add(value);
		}
		return this;
	}

	/**
	 * 添加请求文件
	 *
	 * @param key
	 *            文件名
	 * @return 文件路径
	 */
	public HttpPostUtil addFile(String key, File file) {
		Map<String, File> map = this.filesMap;
		if (map == null) {
			this.filesMap = map = new HashMap<>();
		}
		map.put(key, file);
		return this;
	}

	private void setRequestHeader() {
		// 默认无缓存,可被替换
		http.setRequestProperty("Cache-Control", "no-cache");
		http.setRequestProperty("Pragma", "no-cache");
		http.setRequestProperty(
				"user-agent",
				"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36");
		if (this.headerMap != null) {
			Set<Map.Entry<String, List<Object>>> entrySet = this.headerMap
					.entrySet();
			String key = null;
			for (Map.Entry<String, List<Object>> entry : entrySet) {
				key = entry.getKey();
				for (Object value : entry.getValue()) {
					if (value != null) {
						http.addRequestProperty(key, String.valueOf(value));
					}
				}
			}
		}
		// 设置默认的
		http.setRequestProperty("Connection", "Keep-Alive");
		// 是否有文件上传
		if (this.filesMap == null) {
			http.setRequestProperty("Content-Type", ENCTYPE_FORMURLENCODED);
		} else {
			BOUNDARY = randomBoundary();
			http.setRequestProperty("Content-Type", ENCTYPE_MULTIPARTFROMDATA
					+ "; boundary=" + BOUNDARY);
		}
	}

	/**
	 * 设置请求参数
	 * 
	 * @param out
	 * @throws IOException
	 */
	private void setRequestParameters(DataOutputStream out) throws IOException {
		if (this.paramsMap == null) {
			return;
		}
		Set<Map.Entry<String, List<Object>>> entrySet = this.paramsMap
				.entrySet();
		String key = null;
		for (Map.Entry<String, List<Object>> entry : entrySet) {
			key = entry.getKey();
			for (Object value : entry.getValue()) {
				if (value != null) {
					this.setRequestParameter(key, value.toString(), out);
				}
			}
		}
	}

	private void setRequestParameter(String key, String value,
			DataOutputStream out) throws IOException {
		if (this.filesMap == null) {// 无文件上传,使用ENCTYPE_FORMURLENCODED编码
			key = URLEncoder.encode(key, "utf-8");
			out.writeBytes(key);
			out.write('=');
			value = URLEncoder.encode(value, "utf-8");
			out.writeBytes(value);
			out.write('&');
		} else {
			out.writeBytes(PREFIX);
			out.writeBytes(BOUNDARY);
			out.writeBytes(CRLF);
			out.writeBytes("Content-Disposition: form-data; name=");
			out.write('"');
			out.write(key.getBytes(requestCharEncoding));
			out.write('"');
			out.writeBytes(CRLF);
			out.writeBytes(CRLF);
			out.write(value.getBytes(requestCharEncoding));
			out.writeBytes(CRLF);
		}
	}

	private void setUploadFiles(DataOutputStream out) throws IOException {
		if (this.filesMap == null) {
			return;
		}
		Set<Map.Entry<String, File>> entrySet = this.filesMap.entrySet();
		String key = null;
		File file = null;
		for (Map.Entry<String, File> entry : entrySet) {
			key = entry.getKey();
			file = entry.getValue();
			this.setUploadFile(key, file, out);
		}
		// 输出结尾
		out.writeBytes(PREFIX + BOUNDARY + PREFIX);
		out.flush();

	}

	private void setUploadFile(String name, File file, DataOutputStream out)
			throws IOException {
		out.writeBytes(PREFIX);
		out.writeBytes(BOUNDARY);
		out.writeBytes(CRLF);
		String fileName = file.getName();
		String contentType = Files.probeContentType(Paths.get(file.toURI()));
		if (contentType == null) {
			contentType = "application/octet-stream";
		}
		out.writeBytes("Content-Disposition:form-data; name=\"" + name
				+ "\"; filename=\"");
		out.write(fileName.getBytes("utf-8"));
		out.writeByte('"');
		out.writeBytes(CRLF);
		out.writeBytes("Content-Type: " + contentType + CRLF);
		out.writeBytes(CRLF);
		byte[] buffer = new byte[10240];
		int len = 0;
		FileInputStream is = new FileInputStream(file);
		while ((len = is.read(buffer)) != -1) {
			out.write(buffer, 0, len);
			out.flush();
		}
		out.writeBytes(CRLF);
		is.close();
	}

	private byte[] post() throws Exception {
		String url = this.url;
		http = (HttpURLConnection) new URL(url).openConnection();
		http.setConnectTimeout(timeout);
		http.setRequestMethod("POST");
		http.setUseCaches(false);
		http.setDoOutput(true);
		// 设置请求头
		this.setRequestHeader();

		DataOutputStream out = new DataOutputStream(http.getOutputStream());
		// 设置post参数
		this.setRequestParameters(out);
		// 设置上传文件
		this.setUploadFiles(out);
		// 连接
		http.connect();
		// 获取应答
		int responseCode = http.getResponseCode();
		if (HttpURLConnection.HTTP_OK == responseCode) {
			return getResponseData();
		} else {
			throw new RuntimeException(responseCode + ":"
					+ http.getResponseMessage());
		}
	}

	/**
	 * 读取应答字符串
	 * 
	 * @return
	 * @throws java.io.IOException
	 */
	private byte[] getResponseData() throws IOException {
		int contentLength = http.getContentLength();
		if (contentLength <= 0) {
			contentLength = 32;
		}
		try (InputStream is = http.getInputStream();
				ByteArrayOutputStream bao = new ByteArrayOutputStream(
						contentLength);) {
			byte[] bs = new byte[1024];
			int length = 0;
			while ((length = is.read(bs)) != -1) {
				bao.write(bs, 0, length);
			}
			return bao.toByteArray();
		}
	}

	/**
	 * 开始任务
	 *
	 * @throws Exception
	 */

	public void submit(Callback callback) {
		try {
			byte[] data = post();
			callback.on200(data);
		} catch (Exception e) {
			callback.onOther(e);
		}
	}
}
