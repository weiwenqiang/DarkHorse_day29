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
	// ȷ�����ص�ַ
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
				//�ѱ����ĳ�long
				tv.setText((long)pb.getProgress() * 100 / pb.getMax()+ "%");
				break;
			case 2:
				//Ϊ�˽��������Ϻ��ı����ȼ�¼����100��ǿ�ư���д��100
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

				// ����get�������������ַ����Դ
				try {
					URL url = new URL(path);
					HttpURLConnection conn = (HttpURLConnection) url
							.openConnection();
					conn.setRequestMethod("GET");
					conn.setConnectTimeout(5000);
					conn.setReadTimeout(5000);

					if (conn.getResponseCode() == 200) {
						// �õ���������Դ�ļ��ĳ���
						int length = conn.getContentLength();

						//���ý����������ֵ����
						pb.setMax(length);
						
						File file = new File(Environment.getExternalStorageDirectory(),fileName);
						// ���API��Ϊ������дÿ���̵߳Ŀ�ʼ����λ�ã����Բ���
						// FileOutputStream fos = new FileOutputStream(file);
						// ����洢�ļ���API�����ɵ�����ÿ���̵߳Ŀ�ʼ�����ڵ㶼��һ��
						// ������ʱ�ļ�
						RandomAccessFile raf = new RandomAccessFile(file, "rwd");
						// ������ʱ�ļ��Ĵ�С
						raf.setLength(length);
						raf.close();
						// ����ÿ���߳�Ӧ�����ض����ֽ�
						int size = length / ThreadCount;
						// �����̵߳Ŀ�ʼλ�úͽ���λ��
						for (int i = 0; i < ThreadCount; i++) {
							int startIndex = i * size;
							int endIndex = (i + 1) * size - 1;
							if (i == ThreadCount - 1) {
								endIndex = length - 1;
							}
							// System.out.println("�߳�"+i+"���������䣺"+startIndex+"-----"+endIndex);
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
				// �жϽ�����ʱ�ļ��Ƿ����
				if (progressFile.exists()) {
					FileInputStream fis = new FileInputStream(progressFile);
					BufferedReader br = new BufferedReader(
							new InputStreamReader(fis));
					// ��ȡ��ʱ�ļ��ϴ����ص��ܽ��ȣ�Ȼ����ԭ���Ŀ�ʼλ����ӣ��õ��µĿ�ʼλ��
					int lastProgress =Integer.parseInt(br.readLine());
					startIndex += lastProgress;
					
					//���ϴ����صĽ�����ʾ��������
					currentProgress += lastProgress;
					pb.setProgress(currentProgress);
					//������Ϣ�������߳�ˢ���ı�����
					handler.sendEmptyMessage(1);
					
					// ����
					fis.close();
				}
				System.out.println("�߳�" + thireadId + "���������䣺" + startIndex
						+ "-----" + endIndex);

				URL url = new URL(path);
				HttpURLConnection conn = (HttpURLConnection) url
						.openConnection();
				conn.setRequestMethod("GET");
				conn.setConnectTimeout(5000);
				conn.setReadTimeout(5000);
				// ���ñ���http�������������
				conn.setRequestProperty("Range", "bytes=" + startIndex + "-"
						+ endIndex);
				// ���󲿷����ݣ���Ӧ����206
				if (conn.getResponseCode() == 206) {
					// �����ʱֻ��1/3��Դ�ļ�����
					InputStream is = conn.getInputStream();
					byte[] b = new byte[1024];
					int len = 0;
					int total = 0;
					// �õ���ʱ�ļ������������
					File file = new File(Environment.getExternalStorageDirectory(),fileName);
					RandomAccessFile raf = new RandomAccessFile(file, "rwd");
					// ���ļ�д���λ���ƶ���startIndex
					raf.seek(startIndex);
					while ((len = is.read(b)) != -1) {
						// ÿ�ζ�ȡ���������֮��ͬ������д����ʱ�ļ�
						raf.write(b, 0, len);

						total += len;
						System.out.println("�߳�"+thireadId+"������"+total);
						
						//���������������߳�ˢ��UI
						//ÿ�ζ�ȡ���������֮�󣬰ѱ��ζ�ȡ�����ݳ�����ʾ��������
						currentProgress +=len;
						pb.setProgress(currentProgress);
						//������Ϣ�������߳�ˢ���ı�����
						handler.sendEmptyMessage(1);
						// ����һ��ר��������¼���ؽ��ȵ���ʱ�ļ�

						RandomAccessFile progressRaf = new RandomAccessFile(
								progressFile, "rwd");
						// ÿ�ζ�ȡ�������ݣ�ͬ���ѵ�ǰ�߳����ص��ܽ��ȣ�д����ʱ�ļ���
						progressRaf.write((total + "").getBytes());
						progressRaf.close();
					}
					System.out.println("�߳�" + thireadId
							+ "-------------------�������-------------------");
					raf.close();
					// ɾ���ļ�������3���̶߳�Ҫ������ϲ���ɾ���ļ�
					finishedThread++;
					synchronized (path) {
						if (finishedThread == ThreadCount) {
							for (int i = 0; i < ThreadCount; i++) {
								File f = new File(Environment.getExternalStorageDirectory(), i + ".txt");
								f.delete();
								//Ϊ�˽��������Ϻ��ı����ȼ�¼����100��ǿ�ư���д��100
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
