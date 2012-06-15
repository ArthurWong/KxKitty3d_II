package com.redwood.utils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import android.content.res.Resources;

public class FileUtil {
	//��assets�ļ����м�������
	public static String loadFileAssets(String filePath, Resources r)
	{
		String content = null;
		try{
			InputStream in = r.getAssets().open(filePath);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			int c = -1;
			while((c = in.read()) != -1)
			{
				out.write(c);
			}
			
			byte[] buf = out.toByteArray();
			content = new String(buf, "UTF-8");
			content.replaceAll("\\r\\n", "\n");
			in.close();
			out.close();
		} catch(Exception e)
		{
			e.printStackTrace();
		}
		return content;
	}
}
