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
					// ʹ��HttpClient�����get��ʽ�ύ
					// 1.��ȡHttpClient����
					HttpClient hc = new DefaultHttpClient();
					// 2.����httpget���󣬹��췽���Ĳ���������ַ
					HttpGet hg = new HttpGet(path);
					// 3.ʹ�ÿͻ��˶��󣬰�get���󷢳�ȥ
					try {
						HttpResponse hr = hc.execute(hg);
						// �õ���Ӧͷ�е�״̬��
						StatusLine sl = hr.getStatusLine();
						if (sl.getStatusCode() == 200) {
							HttpEntity he = hr.getEntity();
							// �õ�ʵ���е����ݣ���ʵ���Ƿ��������ص�������
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
					// 1.�����ͻ��˶���
					HttpClient hc = new DefaultHttpClient();
					// 2.����POST����
					HttpPost hp = new HttpPost(path);
					// ����post��������ʵ�壬��ʵ���ǰ�Ҫ�ύ�����ݷ�װ��post������������

					// NameValuePair nvp = new NameValuePair();//�ӿڣ������������ʵ����
					// ��װForm���ύ������
					BasicNameValuePair bnvp_name = new BasicNameValuePair("name", name);
					BasicNameValuePair bnvp_password = new BasicNameValuePair(
							"password", password);
					List<NameValuePair> parameters = new ArrayList<NameValuePair>();
					// ��BasicNameValuePair���뼯��
					parameters.add(bnvp_name);
					parameters.add(bnvp_password);
					try {
						// Ҫ�ύ�����ݶ��Ѿ��ڼ������ˣ��Ѽ��ϴ���ʵ�����
						UrlEncodedFormEntity entity = new UrlEncodedFormEntity(
								parameters, "utf-8");
						hp.setEntity(entity);

						// 3.ʹ�ÿͻ��˷���post����

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
