package com.example.day29.httpclient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import com.example.day29.R;
import com.example.util.Utils;

import android.app.Activity;
import android.content.res.Resources.Theme;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

public class TestHttpClient extends Activity implements OnClickListener {
	EditText et_name, et_password;
	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 1:
				Toast.makeText(TestHttpClient.this, (String) msg.obj, 0).show();
				break;
			default:
				break;
			}
		}
	};

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
		case R.id.b1_get: {
			final String name = et_name.getText().toString();
			final String password = et_password.getText().toString();
			Thread t = new Thread() {
				@Override
				public void run() {
					super.run();
					String path = "http://10.0.2.2:8080/AndroidRequest/servlet/LoginRequest?name="
							+ URLEncoder.encode(name) + "&password=" + password;
					// 使用HttpClient框架做get方式提交
					// 1.获取HttpClient对象
					HttpClient hc = new DefaultHttpClient();
					// 2.创建httpget对象，构造方法的参数就是网址
					HttpGet hg = new HttpGet(path);
					// 3.使用客户端对象，把get请求发出去
					try {
						HttpResponse hr = hc.execute(hg);
						// 拿到响应头中的状态行
						StatusLine sl = hr.getStatusLine();
						if (sl.getStatusCode() == 200) {
							HttpEntity he = hr.getEntity();
							// 拿到实体中的内容，其实就是服务器返回的输入流
							InputStream is = he.getContent();
							String text = Utils.getTextFromStream(is);

							Message msg = handler.obtainMessage();
							msg.obj = text;
							msg.what = 1;
							handler.sendMessage(msg);
						}
					} catch (ClientProtocolException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			};
			t.start();
			break;
		}
		case R.id.b1_post: {
			final String name = et_name.getText().toString();
			final String password = et_password.getText().toString();
			final String path = "http://10.0.2.2:8080/AndroidRequest/servlet/LoginRequest";
			
			Thread t = new Thread(){

				@Override
				public void run() {
					super.run();
					// 1.创建客户端对象
					HttpClient hc = new DefaultHttpClient();
					// 2.创建POST请求
					HttpPost hp = new HttpPost(path);
					// 设置post请求对象的实体，其实就是把要提交的数据封装至post请求的输出流中

					// NameValuePair nvp = new NameValuePair();//接口，所以用下面的实现类
					// 封装Form表单提交的数据
					BasicNameValuePair bnvp_name = new BasicNameValuePair("name", name);
					BasicNameValuePair bnvp_password = new BasicNameValuePair(
							"password", password);
					List<NameValuePair> parameters = new ArrayList<NameValuePair>();
					// 把BasicNameValuePair放入集合
					parameters.add(bnvp_name);
					parameters.add(bnvp_password);
					try {
						// 要提交的数据都已经在集合中了，把集合传给实体对象
						UrlEncodedFormEntity entity = new UrlEncodedFormEntity(
								parameters, "utf-8");
						hp.setEntity(entity);

						// 3.使用客户端发送post请求

						HttpResponse hr = hc.execute(hp);
						if (hr.getStatusLine().getStatusCode() == 200) {
							InputStream is = hr.getEntity().getContent();
							String text = Utils.getTextFromStream(is);
							Message msg = handler.obtainMessage();
							msg.obj = text;
							msg.what = 1;
							handler.sendMessage(msg);
						}
					} catch (ClientProtocolException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			};
			t.start();
			
			break;
		}
		default:
			break;
		}
	}
}
