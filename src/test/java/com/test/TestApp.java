package com.test;

import java.util.HashMap;
import java.util.Map;



public class TestApp {

	public static void main(String[] args) {

		String val = "Metika Tiraprasart";
		
		Map<String, String> map = new HashMap<>();
		map.put("Ms. Metika Tiraprasart", "Metika");
		map.put("Mr. Wiwat Jiranukul", "Wiwat");
		map.put("Ms. Siriporn Muanggam", "Siriporn");
		
		System.out.println(map.containsKey(val));
	}
}
