package com.example.day29.multithreaddownload;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

import com.example.day29.R;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class TestDownloadNeat extends Activity implements OnClickListener {
	Button download;
	static ProgressBar progressBar;
	static TextView progressText;
	static String fileName = "npp.6.8.8.Installer.exe";
	static String path = "http://10.0.2.2:8080/test/" + fileName;
	static int ThreadCount = 3;
	static int finishedThread = 0;
	int currentProgress;
	static Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 1:
				progressText.setText((long)progressBar.getProgress() * 100 / progressBar.getMax() +"%");
				break;
			case 2:
				progressText.setText("100%");
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
		setContentView(R.layout.b3_multithreaddownload);
		progressBar = (ProgressBar) findViewById(R.id.b3_progressbar);
		progressText = (TextView) findViewById(R.id.b3_text);
		download =(Button) findViewById(R.id.b3_download);
		download.setText("多线程断点续传干净版");
		download.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.b3_download:
			multiThreadDownload();
			break;

		default:
			break;
		}
	}
	
	public void multiThreadDownload(){
		Thread t = new Thread(){

			@Override
			public void run() {
				super.run();
				try {
					URL url = new URL(path);
					HttpURLConnection conn = (HttpURLConnection) url
							.openConnection();
					conn.setRequestMethod("GET");
					conn.setConnectTimeout(5000);
					conn.setReadTimeout(5000);
					if (conn.getResponseCode() == 200) {
						int length = conn.getContentLength();
						progressBar.setMax(length);
						File file = new File(Environment.getExternalStorageDirectory(),fileName);
						RandomAccessFile raf = new RandomAccessFile(file, "rwd");
						raf.setLength(length);
						raf.close();
						int size = length / ThreadCount;
						for (int i = 0; i < ThreadCount; i++) {
							int startIndex = i * size;
							int endIndex = (i + 1) * size - 1;
							if (i == ThreadCount - 1) {
								endIndex = length - 1;
							}
							new DownloadThread(startIndex, endIndex, i).start();
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
		};
		t.start();
	}

	class DownloadThread extends Thread {
		
		int startIndex;
		int endIndex;
		int threadId;

		public DownloadThread(int startIndex, int endIndex, int threadId) {
			super();
			this.startIndex = startIndex;
			this.endIndex = endIndex;
			this.threadId = threadId;
		}

		@Override
		public void run() {
			try {
				File progressFile = new File(Environment.getExternalStorageDirectory(), threadId + ".txt");
				if (progressFile.exists()) {
					FileInputStream fis = new FileInputStream(progressFile);
					BufferedReader br = new BufferedReader(
							new InputStreamReader(fis));
					startIndex += Integer.parseInt(br.readLine());
					br.close();
					fis.close();
				}
				System.out.println("线程" + threadId + "的下载区间：" + startIndex
						+ "-" + endIndex);
				URL url = new URL(TestDownloadNeat.path);
				HttpURLConnection conn = (HttpURLConnection) url
						.openConnection();
				conn.setRequestMethod("GET");
				conn.setConnectTimeout(5000);
				conn.setReadTimeout(5000);
				conn.setRequestProperty("Range", "bytes=" + startIndex + "-"
						+ endIndex);
				if (conn.getResponseCode() == 206) {
					InputStream is = conn.getInputStream();
					byte[] b = new byte[1024];
					int len = 0;
					int total = 0;
					File file = new File(Environment.getExternalStorageDirectory(), TestDownloadNeat.fileName);
					RandomAccessFile raf = new RandomAccessFile(file, "rwd");
					raf.seek(startIndex);
					while ((len = is.read(b)) != -1) {
						raf.write(b, 0, len);
						total += len;
						currentProgress += len;
						progressBar.setProgress(currentProgress);
						handler.sendEmptyMessage(1);
						RandomAccessFile progressRaf = new RandomAccessFile(
								progressFile, "rwd");
						progressRaf.write((total + "").getBytes());
						progressRaf.close();
					}
					raf.close();
					TestDownloadNeat.finishedThread++;
					synchronized (TestDownloadNeat.path) {
						if (TestDownloadNeat.finishedThread == TestDownloadNeat.ThreadCount) {
							for (int i = 0; i < TestDownloadNeat.ThreadCount; i++) {
								File f = new File(Environment.getExternalStorageDirectory(), i + ".txt");
								f.delete();
								handler.sendEmptyMessage(2);
							}
							TestDownloadNeat.finishedThread = 0;
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
