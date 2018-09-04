package net.bither.net;

import android.os.Handler;
import android.os.Looper;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

import net.bither.util.LogUtil;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by xusong on 2016/5/20.
 */
public class OkHttpHelper {

	/*OKHttp对象*/
	private OkHttpClient okHttpClient;
	/*辅助对象*/
	private static OkHttpHelper okHttpHelper;
	/*json解析对象*/
	private Gson gson;
	/*主线程跟新操作*/
	private Handler handler;
	public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

	public OkHttpHelper() {

		okHttpClient = new OkHttpClient.Builder()
				.connectTimeout(30, TimeUnit.SECONDS)
				.writeTimeout(10, TimeUnit.SECONDS)
				.readTimeout(30, TimeUnit.SECONDS)
				.build();
		gson = new Gson();
		handler = new Handler(Looper.getMainLooper());
	}

	/*单例对象*/
	public static OkHttpHelper getInstance() {
		if (okHttpHelper == null) {
			synchronized (OkHttpHelper.class) {

				if (okHttpHelper == null) {
					okHttpHelper = new OkHttpHelper();
				}
			}
		}
		return okHttpHelper;
	}

	/*get请求*/
	public void get(String url, BaseCallback baseCallback) {

		Request getRequest = buildRequest(url, null, HttpMethodType.GET);
		sendRequest(getRequest, baseCallback, true);
		LogUtil.i("HTTP", "URL:" + url);

	}

	/*postForm请求*/
	public void postToForm(String url, Map<String, String> params, boolean isShow, BaseCallback baseCallback) {

		Request postRequest = buildRequest(url, params, HttpMethodType.POST);
		sendRequest(postRequest, baseCallback, isShow);

		LogUtil.i("HTTP", "URL:" + url);
		LogUtil.i("HTTP", "params:" + params.toString());
	}

	/*postTOjSON请求*/
	public void postToJson(String url, String json, boolean isShow, BaseCallback baseCallback) {

		Request postRequest = buildRequestForJson(url, json);
		sendRequest(postRequest, baseCallback, isShow);

		LogUtil.i("HTTP", "URL:" + url);
		LogUtil.i("HTTP", "JSON:" + json.toString());
	}


	/*发送请求*/
	private void sendRequest(final Request request, final BaseCallback baseCallback, boolean isShow) {

		//发送请求前
		baseCallback.onRequestBefore(request, isShow);

		okHttpClient.newCall(request).enqueue(new Callback() {
			@Override
			public void onFailure(Call call, IOException e) {
				baseCallback.onFailure(call.request(), e);
				LogUtil.i("HTTP", "failure" + e.toString());
			}

			@Override
			public void onResponse(Call call, Response response) throws IOException {
				//主要是隐藏dialog操作
				baseCallback.onResponse(response);
				if (response.isSuccessful()) {
					String resultStr = response.body().string();
					LogUtil.i("HTTP", "Result:" + resultStr);
					if (baseCallback.mType == String.class) {
						//如果需要字符串类型则不需要解析
//                        baseCallback.onSuccess(response, resultStr);//非UI线程，不能直接刷新UI界面
						//通过handler操作UI界面
						callbackSuccess(baseCallback, response, resultStr);
					} else {
						//返回其它类型用Gson解析
						try {
							Object object = gson.fromJson(resultStr, baseCallback.mType);
							callbackSuccess(baseCallback, response, object);
						} catch (JsonParseException e) {
//                            baseCallback.onError(response, response.code(), e);//非UI线程，不能直接刷新UI界面
							LogUtil.i("HTTP", "解析转换异常:" + e.toString());
							callbackError(baseCallback, response, e);
						}


					}


				} else {

					callbackError(baseCallback, response, null);
				}
			}
		});

	}

	/*创建request*/
	private Request buildRequest(String url, Map<String, String> params, HttpMethodType methodType) {

		//创建Builder(request通过Builder创建)
		Request.Builder builder = new Request.Builder();
		//如果是get请求类型
		if (methodType == HttpMethodType.GET) {
			builder.get();
		} else if (methodType == HttpMethodType.POST) {
			//需要传递RequestBody
			RequestBody requestBody = buildFormData(params);
			builder.post(requestBody);
		}
		builder.url(url);
		return builder.build();
	}


	/*创建RequestBody*/
	private RequestBody buildFormData(Map<String, String> params) {
		//FormBody 为RequestBody的子类
		FormBody.Builder fBuilder = new FormBody.Builder();
		if (params != null) {
			for (Map.Entry<String, String> entry : params.entrySet()) {
				fBuilder.add(entry.getKey(), entry.getValue());
			}
		}

		return fBuilder.build();
	}

	/*创建Jsonrequest*/
	private Request buildRequestForJson(String url, String json) {

		//创建Builder(request通过Builder创建)
		Request.Builder builder = new Request.Builder();
		//需要传递RequestBody
		RequestBody requestBody = RequestBody.create(JSON, json);
		builder.post(requestBody);

		builder.url(url);
		return builder.build();
	}


	/*请求成功刷新界面UI*/
	private void callbackSuccess(final BaseCallback baseCallback, final Response response, final Object object) {

		handler.post(new Runnable() {
			@Override
			public void run() {

				baseCallback.onSuccess(response, object);

			}
		});

	}

	/*请求失败刷新界面UI*/
	private void callbackError(final BaseCallback baseCallback, final Response response, final JsonParseException e) {

		handler.post(new Runnable() {
			@Override
			public void run() {
				baseCallback.onError(response, response.code(), e);
			}
		});

	}

	/*请求方法分类  get or post*/
	enum HttpMethodType {
		GET,
		POST
	}


}
