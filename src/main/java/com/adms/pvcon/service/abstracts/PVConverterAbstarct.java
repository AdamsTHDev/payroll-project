package com.adms.pvcon.service.abstracts;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.adms.imex.excelformat.DataHolder;
import com.adms.imex.excelformat.ExcelFormat;
import com.adms.pvcon.enums.EFileFormat;
import com.adms.pvcon.object.VendorInfo;
import com.adms.pvcon.service.PvConverterService;
import com.adms.pvcon.util.GetResourceUtil;

public abstract class PVConverterAbstarct implements PvConverterService {
	
	private final List<String> TITLES = Arrays.asList(new String[]{"Mr.", "Ms.", "Mrs."});
	
	protected final String SEPARATE_VAL = "@";
	protected final String NEWLINE = "\r\n";

	protected Map<String, String> paramValueMap;
	
	protected Map<String, String[]> companyAccMap;
	protected Map<String, DataHolder> staffInfoMap;
	protected Map<String, VendorInfo> supplierInfoMap;
	protected Map<String, DataHolder> orderingPartyMap;
	protected Map<String, DataHolder> bankMap;
	
	protected void initParamValue() throws Exception {
		paramValueMap = new HashMap<>();
		
		InputStream formatStream = GetResourceUtil.getInstance().getContextResourceAsStream(EFileFormat.PARAM_VALUE_FORMAT.getValue());
		InputStream xlsStream = new FileInputStream(GetResourceUtil.getInstance().getConfigValue("cfg.file.param.value"));
		
		ExcelFormat ef = new ExcelFormat(formatStream);
		DataHolder wbHolder = ef.readExcel(xlsStream);
		
		DataHolder sheetHolder = wbHolder.get(wbHolder.getKeyList().get(0));
		List<DataHolder> list = sheetHolder.getDataList("dataList");
		
		for(DataHolder data : list) {
			paramValueMap.put(data.get("param").getStringValue(), data.get("value").getStringValue());
		}

		xlsStream.close();
		formatStream.close();
	}
	
	protected void initCompanyStaffInfo() throws Exception {
		InputStream formatStream = GetResourceUtil.getInstance().getContextResourceAsStream(EFileFormat.COMPANY_AND_STAFF_ACCT_FORMAT.getValue());
		ExcelFormat ef = new ExcelFormat(formatStream);
		InputStream xlsStream = new FileInputStream(GetResourceUtil.getInstance().getConfigValue("cfg.file.company.staff.acct"));
		DataHolder wbCompanyStaff = ef.readExcel(xlsStream);
		
//		Company Account Sheet
		DataHolder sheetHolder = wbCompanyStaff.get(wbCompanyStaff.getSheetNameByIndex(0));
		List<DataHolder> list = sheetHolder.getDataList("dataList");
		companyAccMap = new HashMap<>();
		for(DataHolder data : list) {
			String[] array = new String[2];
			array[0] = data.get("accNo").getStringValue();
			array[1] = data.get("initialCode").getStringValue();
			companyAccMap.put(data.get("compAccName").getStringValue(), array);
		}
		
//		Staff Info Sheet
		sheetHolder = wbCompanyStaff.get(wbCompanyStaff.getSheetNameByIndex(1));
		list = sheetHolder.getDataList("dataList");
		staffInfoMap = new HashMap<>();
		for(DataHolder data : list) {
			staffInfoMap.put(removeTitle(data.get("name").getStringValue(), data.get("surname").getStringValue()), data);
		}
		
//		Supplier Info Sheet
		sheetHolder = wbCompanyStaff.get(wbCompanyStaff.getSheetNameByIndex(2));
		list = sheetHolder.getDataList("dataList");
		supplierInfoMap = new HashMap<>();
		for(DataHolder data : list) {
			
			VendorInfo vendorInfo = new VendorInfo();
			vendorInfo.setWhtName(data.get("whtName").getStringValue());
			vendorInfo.setWhtAddress1(data.get("whtAddress1").getStringValue());
			vendorInfo.setWhtAddress2(data.get("whtAddress2").getStringValue());
			vendorInfo.setWhtAddress3(data.get("whtAddress3").getStringValue());
			vendorInfo.setWhtAddress4(data.get("whtAddress4").getStringValue());
			vendorInfo.setWhtTaxId(data.get("whtTaxId").getStringValue());
			vendorInfo.setPnd(data.get("pnd").getStringValue());
			supplierInfoMap.put(data.get("whtName").getStringValue(), vendorInfo);
		}
		
//		Ordering Party Sheet
		sheetHolder = wbCompanyStaff.get(wbCompanyStaff.getSheetNameByIndex(3));
		list = sheetHolder.getDataList("dataList");
		orderingPartyMap = new HashMap<>();
		for(DataHolder data : list) {
			orderingPartyMap.put(data.get("type").getStringValue(), data);
		}
		
//		Bank Code, Branch Code Sheet
		sheetHolder = wbCompanyStaff.get(wbCompanyStaff.getSheetNameByIndex(5));
		list = sheetHolder.getDataList("dataList");
		bankMap = new HashMap<>();
		for(DataHolder data : list) {
			bankMap.put(data.get("initialCode").getStringValue(), data);
		}
		
		xlsStream.close();
		formatStream.close();
	}
	
	protected String removeTitle(String name, String surName) {
		for(String t : TITLES) {
			if(name.startsWith(t)) {
				name = name.replace(t, "").trim();
			}
		}
		return new String(name + " " + surName.trim()).trim();
	}

}
