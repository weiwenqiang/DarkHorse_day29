package com.example.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Utils {
	public static String getTextFromStream(InputStream is){
		byte[] b = new byte[1024];
		int len = 0;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try {
			while((len= is.read(b))!=-1){
				bos.write(b, 0, len);
			}
			String text = new String(bos.toByteArray(),"utf-8");
			bos.close();
			return text;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
