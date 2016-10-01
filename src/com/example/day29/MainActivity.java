package com.example.day29;

import com.example.day29.asynchttp.TestAsyncHttp;
import com.example.day29.httpclient.TestHttpClient;
import com.example.day29.multithreaddownload.TestDownloadNeat;
import com.example.day29.multithreaddownload.TestMultiThreadDownload;
import com.example.day29.xutils.TestXUtilsDownload;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;

public class MainActivity extends Activity implements OnClickListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		findViewById(R.id.day29_b1).setOnClickListener(this);
		findViewById(R.id.day29_b2).setOnClickListener(this);
		findViewById(R.id.day29_b3).setOnClickListener(this);
		findViewById(R.id.day29_b4).setOnClickListener(this);
		findViewById(R.id.day29_b5).setOnClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.day29_b1:
			startActivity(new Intent(MainActivity.this, TestHttpClient.class));
			break;
		case R.id.day29_b2:
			startActivity(new Intent(MainActivity.this, TestAsyncHttp.class));
			break;
		case R.id.day29_b3:
			startActivity(new Intent(MainActivity.this, TestMultiThreadDownload.class));
			break;
		case R.id.day29_b4:
			startActivity(new Intent(MainActivity.this, TestDownloadNeat.class));
			break;
		case R.id.day29_b5:
			startActivity(new Intent(MainActivity.this, TestXUtilsDownload.class));
			break;
		default:
			break;
		}
	}
}
