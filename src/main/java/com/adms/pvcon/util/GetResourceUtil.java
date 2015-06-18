package com.adms.pvcon.util;

import java.io.InputStream;
import java.util.Properties;

public class GetResourceUtil {

	private static GetResourceUtil instance;
	private Properties property;

	public InputStream getContextResourceAsStream(String path) {
		return Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
	}
	
	public String getConfigValue(String param) {
		if(property == null) {
			InputStream is = null;
			try {
				property = new Properties();
				
//				File jarPath = new File(GetResourceUtil.class.getProtectionDomain().getCodeSource().getLocation().getPath());
//				System.out.println("jar location: " + jarPath.getParent());
//				String propPath = jarPath.getParentFile().getAbsolutePath();
//				is = new FileInputStream(new File(propPath + "/config.properties"));
				is = getContextResourceAsStream("config.properties");
				property.load(is);
			} catch(Exception e) {
				e.printStackTrace();
			} finally {
				try {
					is.close();
				} catch(Exception e) {}
			}
		}
		return property.getProperty(param);
	}
	
	public static GetResourceUtil getInstance() {
		if(instance == null) {
			instance = new GetResourceUtil();
		}
		return instance;
	}

}
