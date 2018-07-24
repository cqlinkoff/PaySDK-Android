package com.mbr.dcpay.utils.network;

import android.text.TextUtils;
import android.util.Log;

import com.mbr.dcpay.demo.MainActivity;
import com.mbr.dcpay.utils.json.Object2Json;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Post {
	private static final String TAG = "Post";

	public static final int CONNECT_TIMEOUT_TIME = 25 * 1000;
	public static final int GETDATA_TIMEOUT_TIME = 45 * 1000;

	public static final int K = 1024;
	public static final int INPUTBUFFSIZE = 8 * K;
	
	public static NetResult post(String requestEntity, String urlString) {
		return post("application/json;charset=UTF-8", requestEntity, urlString, null);
	}
	public static NetResult post(String contentType, String requestEntity, String urlString, String savePath) {
		LinkedHashMap<String, List<String>> head = new LinkedHashMap<String, List<String>>();
		head.put("Accept", new ArrayList<String>(Arrays.asList("*/*")));
		head.put("Connection", new ArrayList<String>(Arrays.asList("Keep-Alive")));
		head.put("Accept-Language", new ArrayList<String>(Arrays.asList("zh-CN")));
		head.put("Content-type", new ArrayList<String>(Arrays.asList(contentType)));
		return post(head, requestEntity, urlString, savePath);
	}
	public static NetResult post(Map<String, List<String>> headMap, String requestEntity, String urlString, String savePath) {
		String name = "";

		if (MainActivity.isLogOn) {
			if (!TextUtils.isEmpty(urlString)) {
				int vindex = urlString.lastIndexOf("/v");
				if (vindex == -1) {
					vindex = urlString.lastIndexOf("/");
				}
				name = urlString.substring(vindex) + " | ";
			}
		}

		boolean save = savePath != null;

		if (MainActivity.isLogOn) {
			Log.i(TAG, name + "请求：");
			Log.i(TAG, name + "url =  " + urlString);
			Log.i(TAG, name + "post = " + requestEntity);
			if (save) {
				Log.i(TAG, name + "文件 = " + savePath);
			}
		}

		NetResult netResult = new NetResult();
		String strResult = null;

		HttpURLConnection conn = null;
		DataOutputStream postStream = null;
		InputStream is = null;
		BufferedInputStream bis = null;
		FileOutputStream fos = null;
		BufferedWriter writer = null;

		try {
			URL url = new URL(urlString);
			conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(CONNECT_TIMEOUT_TIME);
			conn.setReadTimeout(GETDATA_TIMEOUT_TIME);
			fillRequestProperties(conn, headMap);
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setRequestMethod("POST");
			conn.setUseCaches(false); // post 请求不能使用缓存
			conn.setInstanceFollowRedirects(true);

			conn.connect();

			postStream = new DataOutputStream(conn.getOutputStream());
			writer = new BufferedWriter(new OutputStreamWriter(postStream,
					"UTF-8"));
			writer.write(requestEntity);
			// postStream.writeBytes(requestEntity);
			// postStream.flush();
			writer.flush();
			try {
				postStream.close();
			}
			catch (Exception e) {
				if (MainActivity.isLogOn) {
					Log.e(
							TAG,
							name + "postStream.close() error: "
									+ e);
				}
			}
			try {
				writer.close();
			}
			catch (Exception e) {
				if (MainActivity.isLogOn) {
					Log.e(TAG,
							name + "writer.close() error: " + e);
				}
			}
			
			try {
				netResult.mHeaderFields = conn.getHeaderFields();
			}
			catch (Exception e1) {
				if (MainActivity.isLogOn) {
					Log.v(
							TAG,
							"post(head, requestEntity, urlString, savePath, encryptor) getHeaderFields Exception: " + e1);
				}
			}

			int responseCode = conn.getResponseCode();
			if (MainActivity.isLogOn) {
				int conLen;
				try {
					conLen = conn.getContentLength();
				}
				catch (Exception e) {
					conLen = -9527;
				}
				Log.v(TAG,
						"post(head, requestEntity, urlString, savePath, encryptor) getContentLength = " + conLen);
			}
			if (responseCode == 200) {
				if (MainActivity.isLogOn) {
					Log.i(TAG, name + "responseCode = " + responseCode);
				}
				is = conn.getInputStream();
				bis = new BufferedInputStream(is, INPUTBUFFSIZE);
				bis.mark(INPUTBUFFSIZE);

				if (save) {
					fos = new FileOutputStream(savePath);

					byte[] buf = new byte[1024];
					int count = -1, length = 0;
					while ((count = bis.read(buf)) != -1) {
						fos.write(buf, 0, count);
						length++;
					}

					fos.close();

					if (length > INPUTBUFFSIZE) {
						strResult = convertStreamToString(new FileInputStream(
								savePath), -1);
						if (MainActivity.isLogOn) {
							Log.i(TAG, name + "大文件:");
						}
					}
					else {
						bis.reset();
						strResult = convertStreamToString(bis, length);
						if (MainActivity.isLogOn) {
							Log.i(TAG, name + "小文件:");
						}
					}
				}
				else {
					strResult = convertStreamToString(bis, -1);
					if (MainActivity.isLogOn) {
						Log.i(TAG, name + "未保存文件,");
					}
				}

			}
			else {
				if (MainActivity.isLogOn) {
					Log.i(TAG, name + "responseCode = " + responseCode);
				}
			}
		}
		catch (Exception e) {
			if (MainActivity.isLogOn) {
				Log.e(TAG, name + "post error: " + e);
			}
		}
		finally {
			if (conn != null) {
				conn.disconnect();
			}
			if (postStream != null) {
				try {
					postStream.close();
				}
				catch (Exception e) {
					if (MainActivity.isLogOn) {
						Log.e(TAG, name + "postStream.close() error: "
								+ e);
					}
				}
			}
			if (writer != null) {
				try {
					writer.close();
				}
				catch (Exception e) {
					if (MainActivity.isLogOn) {
						Log.e(TAG, name + "writer.close() error: " + e);
					}
				}
			}
			if (is != null) {
				try {
					is.close();
				}
				catch (Exception e) {
					if (MainActivity.isLogOn) {
						Log.e(TAG, name + "is.close() error: " + e);
					}
				}
			}
			if (fos != null) {
				try {
					fos.close();
				}
				catch (Exception e) {
					if (MainActivity.isLogOn) {
						Log.e(TAG, name + "fos.close() error: " + e);
					}
				}
			}
		}

		if (MainActivity.isLogOn) {
			if (TextUtils.isEmpty(strResult)) {
				Log.i(TAG, name + "返回数据大小：" + -1 + "字节");
			}
			else {
				Log.i(TAG, name + "返回数据大小："
						+ strResult.getBytes().length + "字节");
			}

			Log.i(TAG, name + "返回数据内容： " + strResult);
		}

		netResult.mData = strResult;
		return netResult;
	}

	public static String convertStreamToString(InputStream input) throws Exception {
		return convertStreamToString(input, -1);
	}
	public static String convertStreamToString(InputStream input, int size)
			throws Exception {
		BufferedReader reader = null;
		if (size > 0) {
			reader = new BufferedReader(new InputStreamReader(input), size);
		}
		else {
			reader = new BufferedReader(new InputStreamReader(input));
		}
		StringBuilder sb = new StringBuilder();
		String line = null;

		while ((line = reader.readLine()) != null) {
			sb.append(line);
		}

		input.close();
		return sb.toString();
	}

	public static void fillRequestProperties(HttpURLConnection conn, Map<String, List<String>> headMap) throws Exception {
		if (MainActivity.isLogOn) {
			Log.v(TAG, "fillRequestProperties(conn, headMap) headMap = " + Object2Json.toJson(headMap, true));
		}
		
		if (headMap == null || headMap.size() < 1) {
			return;
		}
		
		Iterator<Entry<String, List<String>>> iterator = headMap.entrySet().iterator();
		Entry<String, List<String>> entry;
		while (iterator.hasNext()) {
			entry = iterator.next();
			String key = entry.getKey();
			List<String> valueList = entry.getValue();
			StringBuilder builder = new StringBuilder();
			if (valueList != null) {
				for (int i = 0; i < valueList.size(); i++) {
					builder.append(valueList.get(i)).append(";");
				}
			}
			String value = builder.toString();
			if (value.endsWith(";")) {
				value = value.substring(0, value.length() - 1);
			}
			conn.setRequestProperty(key, value);
		}
	}
}
