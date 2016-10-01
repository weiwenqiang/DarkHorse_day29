package com.example.day29.multithreaddownload;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
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
import android.widget.ProgressBar;
import android.widget.TextView;

public class TestMultiThreadDownload extends Activity implements
		OnClickListener {
	static int ThreadCount = 3;
	static int finishedThread = 0;
	String fileName = "YoudaoDict.exe";
	// 确定下载地址
	String path = "http://10.0.2.2:8080/test/"+fileName;
	
	int currentProgress;
	ProgressBar pb;
	TextView tv;
	Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 1:
				//把变量改成long
				tv.setText((long)pb.getProgress() * 100 / pb.getMax()+ "%");
				break;
			case 2:
				//为了解决下载完毕后，文本进度记录不到100，强制把他写成100
				tv.setText("100%");
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
		findViewById(R.id.b3_download).setOnClickListener(this);
		pb = (ProgressBar) findViewById(R.id.b3_progressbar);
		tv = (TextView) findViewById(R.id.b3_text);
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

	public void multiThreadDownload() {

		Thread t = new Thread() {

			@Override
			public void run() {
				super.run();

				// 发送get请求，请求这个地址的资源
				try {
					URL url = new URL(path);
					HttpURLConnection conn = (HttpURLConnection) url
							.openConnection();
					conn.setRequestMethod("GET");
					conn.setConnectTimeout(5000);
					conn.setReadTimeout(5000);

					if (conn.getResponseCode() == 200) {
						// 拿到所请求资源文件的长度
						int length = conn.getContentLength();

						//设置进度条的最大值长度
						pb.setMax(length);
						
						File file = new File(Environment.getExternalStorageDirectory(),fileName);
						// 这个API因为不容易写每个线程的开始结束位置，所以不用
						// FileOutputStream fos = new FileOutputStream(file);
						// 随机存储文件这API很轻松的做到每个线程的开始结束节点都不一样
						// 生成临时文件
						RandomAccessFile raf = new RandomAccessFile(file, "rwd");
						// 设置临时文件的大小
						raf.setLength(length);
						raf.close();
						// 计算每个线程应该下载多少字节
						int size = length / ThreadCount;
						// 计算线程的开始位置和结束位置
						for (int i = 0; i < ThreadCount; i++) {
							int startIndex = i * size;
							int endIndex = (i + 1) * size - 1;
							if (i == ThreadCount - 1) {
								endIndex = length - 1;
							}
							// System.out.println("线程"+i+"的下载区间："+startIndex+"-----"+endIndex);
							new DownloadThread(startIndex, endIndex, i).start();
						}
					}

				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		};
		t.start();
	}

	class DownloadThread extends Thread {
		int startIndex;
		int endIndex;
		int thireadId;

		public DownloadThread(int startIndex, int endIndex, int thireadId) {
			super();
			this.startIndex = startIndex;
			this.endIndex = endIndex;
			this.thireadId = thireadId;
		}

		@Override
		public void run() {
			super.run();
			try {

				File progressFile = new File(Environment.getExternalStorageDirectory(),thireadId + ".txt");
				// 判断进度临时文件是否存在
				if (progressFile.exists()) {
					FileInputStream fis = new FileInputStream(progressFile);
					BufferedReader br = new BufferedReader(
							new InputStreamReader(fis));
					// 读取临时文件上次下载的总进度，然后与原本的开始位置相加，得到新的开始位置
					int lastProgress =Integer.parseInt(br.readLine());
					startIndex += lastProgress;
					
					//把上次下载的进度显示至进度条
					currentProgress += lastProgress;
					pb.setProgress(currentProgress);
					//发送消息，让主线程刷新文本进度
					handler.sendEmptyMessage(1);
					
					// 关流
					fis.close();
				}
				System.out.println("线程" + thireadId + "的下载区间：" + startIndex
						+ "-----" + endIndex);

				URL url = new URL(path);
				HttpURLConnection conn = (HttpURLConnection) url
						.openConnection();
				conn.setRequestMethod("GET");
				conn.setConnectTimeout(5000);
				conn.setReadTimeout(5000);
				// 设置本次http请求的数据区间
				conn.setRequestProperty("Range", "bytes=" + startIndex + "-"
						+ endIndex);
				// 请求部分数据，响应码是206
				if (conn.getResponseCode() == 206) {
					// 流里此时只有1/3的源文件数据
					InputStream is = conn.getInputStream();
					byte[] b = new byte[1024];
					int len = 0;
					int total = 0;
					// 拿到临时文件输出流的引用
					File file = new File(Environment.getExternalStorageDirectory(),fileName);
					RandomAccessFile raf = new RandomAccessFile(file, "rwd");
					// 把文件写入的位置移动至startIndex
					raf.seek(startIndex);
					while ((len = is.read(b)) != -1) {
						// 每次读取流里的数据之后，同步数据写入临时文件
						raf.write(b, 0, len);

						total += len;
						System.out.println("线程"+thireadId+"下载了"+total);
						
						//进度条可以在主线程刷新UI
						//每次读取流里的数据之后，把本次读取的数据长度显示至进度条
						currentProgress +=len;
						pb.setProgress(currentProgress);
						//发送消息，让主线程刷新文本进度
						handler.sendEmptyMessage(1);
						// 生成一个专门用来记录下载进度的临时文件

						RandomAccessFile progressRaf = new RandomAccessFile(
								progressFile, "rwd");
						// 每次读取流的数据，同步把当前线程下载的总进度，写入临时文件中
						progressRaf.write((total + "").getBytes());
						progressRaf.close();
					}
					System.out.println("线程" + thireadId
							+ "-------------------下载完毕-------------------");
					raf.close();
					// 删除文件，必须3个线程都要下载完毕才能删除文件
					finishedThread++;
					synchronized (path) {
						if (finishedThread == ThreadCount) {
							for (int i = 0; i < ThreadCount; i++) {
								File f = new File(Environment.getExternalStorageDirectory(), i + ".txt");
								f.delete();
								//为了解决下载完毕后，文本进度记录不到100，强制把他写成100
								handler.sendEmptyMessage(2);
							}
							finishedThread = 0;
						}
					}
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (ProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
