package com.example.day29.xutils;

import java.io.File;

import com.example.day29.R;
import com.lidroid.xutils.HttpUtils;
import com.lidroid.xutils.exception.HttpException;
import com.lidroid.xutils.http.ResponseInfo;
import com.lidroid.xutils.http.callback.RequestCallBack;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class TestXUtilsDownload extends Activity implements OnClickListener {
	String fileName = "YoudaoDict.exe";
	String path = "http://10.0.2.2:8080/test/"+fileName;
	TextView tv_msg;
	ProgressBar progressBar;
	TextView progressText;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.b5_xutilsdownload);
		progressBar = (ProgressBar) findViewById(R.id.b5_progressbar);
		progressText = (TextView) findViewById(R.id.b5_text);
		findViewById(R.id.b5_download).setOnClickListener(this);
		tv_msg = (TextView) findViewById(R.id.b5_msg);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.b5_download:
			xUtilsDownload();
			break;

		default:
			break;
		}
	}
	public void xUtilsDownload(){
		HttpUtils utils = new HttpUtils();
		//下载地址，保存路径，服务器是否支持断点续传
		utils.download(path, Environment.getExternalStorageDirectory()+"/"+fileName, true, true, new RequestCallBack<File>(){
			//下载成功时调用
			@Override
			public void onSuccess(ResponseInfo<File> responseInfo) {
				Toast.makeText(TestXUtilsDownload.this, responseInfo.result.getPath(), 0).show();
				tv_msg.setText(responseInfo.result.getPath());
			}
			//下载失败时调用
			@Override
			public void onFailure(HttpException error, String msg) {
				tv_msg.setText(msg);
			}
			@Override
			public void onLoading(long total, long current, boolean isUploading) {
				super.onLoading(total, current, isUploading);
				progressBar.setMax((int)total);
				progressBar.setProgress((int)current);
				progressText.setText(current * 100 / total + "%");
			}
			
		});
	}
}
