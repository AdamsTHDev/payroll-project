package com.adms.pvcon.service;

import com.adms.pvcon.service.impl.PayrollServiceImpl;
import com.adms.pvcon.service.impl.SupplierServiceImpl;


public class PvConverterFactory {

	public static PvConverterService getService(String param) {
		if(param.toUpperCase().contains("STAFF CLAIMS")) {
//			System.out.println("PAYROLL");
			return new PayrollServiceImpl();
		} else {
//			System.out.println("SUPPLIER");
			return new SupplierServiceImpl();
		}
	}
}
