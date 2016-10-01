package com.example.day29.asynchttp;

import java.net.URLEncoder;

import org.apache.http.Header;

import com.example.day29.R;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

public class TestAsyncHttp extends Activity implements OnClickListener {
	EditText et_name, et_password;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.b1_httpclient);
		et_name = (EditText) findViewById(R.id.b1_name);
		et_password = (EditText) findViewById(R.id.b1_password);
		findViewById(R.id.b1_get).setOnClickListener(this);
		findViewById(R.id.b1_post).setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.b1_get:
			getRequest();
			break;

		case R.id.b1_post:
			postRequest();
			break;
		default:
			break;
		}
	}

	private void getRequest() {
		final String name = et_name.getText().toString();
		final String password = et_password.getText().toString();
		String url = "http://10.0.2.2:8080/AndroidRequest/servlet/LoginRequest?name="
				+ URLEncoder.encode(name) + "&password=" + password;
		//创建异步httpclient
		AsyncHttpClient ahc = new AsyncHttpClient();
		//发送get请求提交数据
		ahc.get(url, new MyResponseHandler());
	}

	private void postRequest() {
		final String name = et_name.getText().toString();
		final String password = et_password.getText().toString();
		String url = "http://10.0.2.2:8080/AndroidRequest/servlet/LoginRequest?name="
				+ URLEncoder.encode(name) + "&password=" + password;
		//创建异步httpclient
		AsyncHttpClient ahc = new AsyncHttpClient();
		//把要提交的数据封装RequestParams
		RequestParams params = new RequestParams();
		params.put("name", name);
		params.put("password", password);
		//发送get请求提交数据
		ahc.post(url, params, new MyResponseHandler());
	}
	
	class MyResponseHandler extends AsyncHttpResponseHandler{
		//请求成功时此方法调用
		@Override
		public void onSuccess(int statusCode, Header[] headers,
				byte[] responseBody) {
			Toast.makeText(TestAsyncHttp.this, new String(responseBody), 0).show();
		}
		//请求失败时此方法调用
		@Override
		public void onFailure(int statusCode, Header[] headers,
				byte[] responseBody, Throwable error) {
			Toast.makeText(TestAsyncHttp.this, "请求失败", 0).show();
		}
	}
}
