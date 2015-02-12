package com.adms.pvcon.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class FileUtil {

	private static FileUtil instance;
	
	public static FileUtil getInstance() {
		if(instance == null) {
			instance = new FileUtil();
		}
		return instance;
	}
	
	public String writeout(File fileOut, StringBuffer contents, String encodeType) throws Exception {
		try {
			if(contents != null && contents.length() > 0) {
				if(!fileOut.exists()) {
					fileOut.createNewFile();
				}
				FileOutputStream fos = new FileOutputStream(fileOut);
				Writer writer = new BufferedWriter(new OutputStreamWriter(fos, encodeType));
				writer.write(contents.toString());
				
				writer.flush();
				writer.close();
				fos.close();
				
				return fileOut.getAbsolutePath();
			}
		} catch(Exception e) {
			throw e;
		}
		return null;
	}
	
}
