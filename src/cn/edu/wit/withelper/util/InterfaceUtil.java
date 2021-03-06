package cn.edu.wit.withelper.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import cn.edu.wit.withelper.bean.Task;
import cn.edu.wit.withelper.services.MainService;

import android.os.Message;
import android.util.Log;

public class InterfaceUtil {

	private static final String SALT = "withelper_itjesse";//约定的一个key
	private static final String TAG = "InterfaceUtil";

	@SuppressWarnings("finally")
	public static JSONObject getJSONObject(
			String url, // 请求的URL
			Map<String, String> params // 请求的参数序列
	) {

		long timestamp = new Date().getTime();
		//时间戳
		params.put("timestamp", "" + timestamp);
		//sign
		params.put("sign", MD5Util.getMD5String(SALT + timestamp));//通过时间戳与约定key的MD5值校验请求的安全性
		
		String result = postRequestToServer(url, params);//请求数据
		
		if (null == result) {
			Log.i(TAG, "result = null");
			return null;
			
		}else {
			Log.i(TAG, "result = " + result);
			JSONTokener jsonTokener = new JSONTokener(result);
			JSONObject json = null;
			try {
				//解析出json对象
				json = (JSONObject) jsonTokener.nextValue();
				
			} catch (JSONException e) {
				e.printStackTrace();
			} finally {
				return json;
			}
		}//end of else

	}


	@SuppressWarnings("finally")
	public static String postRequestToServer(String url, // 请求的URL
			Map<String, String> params // 请求的参数序列
	) {
		Log.i(TAG, "访问网络");
		HttpEntityEnclosingRequestBase httpRequest = new HttpPost(url);
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(params.size());

		for (Map.Entry<String, String> entry : params.entrySet()) {// 构建表单字段内容
			nameValuePairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
		}
		String strResult = "";
		try {

			httpRequest.setEntity(new UrlEncodedFormEntity(nameValuePairs,"UTF-8"));

			HttpClient client = new DefaultHttpClient();
			
			HttpParams httpparams = client.getParams();

			//请求时长
			HttpConnectionParams.setConnectionTimeout(httpparams,7000);
			HttpConnectionParams.setSoTimeout(httpparams,7000);
			
			// 执行请求
			HttpResponse httpResponse = client.execute(httpRequest);

			// 判断返回结果，200则说明正确返回
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				// 从返回的结果中获取内容
				strResult = EntityUtils.toString(httpResponse.getEntity(),"UTF-8");
			} else {
				strResult = null;
			}

		} catch (Exception e) {
			e.printStackTrace();
			//异常处理
			Message msg = new Message();
			msg.what = Task.ERROR_NETEXCEPTION ;
			msg.obj = e;
			MainService.handler.sendMessage(msg);
			
			strResult = null;
		} finally {
			return strResult;
		}
	}

}
