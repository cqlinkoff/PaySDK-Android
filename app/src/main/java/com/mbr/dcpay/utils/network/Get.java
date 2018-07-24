package com.mbr.dcpay.utils.network;

import android.text.TextUtils;
import android.util.Log;

import com.mbr.dcpay.demo.MainActivity;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 作者 E-mail: panhang@ehoo.cn
 * @version 创建时间：2014-8-22 下午2:20:25
 */
public class Get {
	private static final String TAG = "Get";

	private static final int SECOND = 1000; 
	public static final int CONNECT_TIMEOUT_TIME = 25 * SECOND;
	public static final int GETDATA_TIMEOUT_TIME = 45 * SECOND;

	public static final int K = 1024;
	public static final int INPUTBUFFSIZE = 8 * K;

	public static NetResult get(String urlString, String savePath) {
		LinkedHashMap<String, List<String>> head = new LinkedHashMap<String, List<String>>();
		head.put("Accept", new ArrayList<String>(Arrays.asList("*/*")));
		head.put("Connection", new ArrayList<String>(Arrays.asList("Keep-Alive")));
		head.put("Accept-Language", new ArrayList<String>(Arrays.asList("zh-CN")));
		return get(head, urlString, savePath);
	}
	public static NetResult get(String urlString, String savePath, boolean ignoreInputStream) {
		LinkedHashMap<String, List<String>> head = new LinkedHashMap<String, List<String>>();
		head.put("Accept", new ArrayList<String>(Arrays.asList("*/*")));
		head.put("Connection", new ArrayList<String>(Arrays.asList("Keep-Alive")));
		head.put("Accept-Language", new ArrayList<String>(Arrays.asList("zh-CN")));
		return get(head, urlString, savePath, ignoreInputStream);
	}
	public static NetResult get(Map<String, List<String>> headMap, String urlString, String savePath) {
		return get(headMap, urlString, savePath, false);
	}
	public static NetResult get(Map<String, List<String>> headMap, String urlString, String savePath, boolean ignoreInputStream) {

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
			if (save) {
				Log.i(TAG, name + "文件 = " + savePath);
			}
		}

		NetResult netResult = new NetResult();
		String strResult = null;

		HttpURLConnection conn = null;
		InputStream is = null;
		BufferedInputStream bis = null;
		FileOutputStream fos = null;
		try {
			URL url = new URL(urlString);
			conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(CONNECT_TIMEOUT_TIME);
			conn.setReadTimeout(GETDATA_TIMEOUT_TIME);
			Post.fillRequestProperties(conn, headMap);
			//conn.setDoOutput(true);
			//conn.setDoInput(true);
			conn.setRequestMethod("GET");
			//conn.setUseCaches(false);
			//conn.setInstanceFollowRedirects(true);

			conn.connect();

			try {
				netResult.mHeaderFields = conn.getHeaderFields();
				if (MainActivity.isLogOn) {
					Log.v(TAG,
							"get(requestEntity, urlString, savePath, encryptor) headerMap = " + netResult.mHeaderFields);
				}
			}
			catch (Exception e1) {
				if (MainActivity.isLogOn) {
					Log.v(TAG,
							"get(head, urlString, savePath, encryptor) getHeaderFields Exception: " + e1);
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
						"get(requestEntity, urlString, savePath, encryptor) getContentLength = " + conLen);
			}
			
			if (responseCode == 200) {
				if (MainActivity.isLogOn) {
					Log.i(TAG, name + "responseCode = " + responseCode);
					Log.v(
							TAG,
							"get(headMap, urlString, savePath, encryptor, ignoreInputStream) ignoreInputStream = " + ignoreInputStream);
				}
				
				if (ignoreInputStream) {
					strResult = "";
				}
				else {
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
			}
			else {
				if (MainActivity.isLogOn) {
					Log.i(TAG, name + "responseCode = " + responseCode);
				}
			}
		}
		catch (Exception e) {
			if (MainActivity.isLogOn) {
				Log.e(TAG, name + "get Exception: " + e);
			}
		}
		finally {
			if (conn != null) {
				conn.disconnect();
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

	private static String convertStreamToString(InputStream input, int size)
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

}
