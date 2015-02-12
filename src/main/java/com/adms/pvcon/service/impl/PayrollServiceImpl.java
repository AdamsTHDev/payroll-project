package com.adms.pvcon.service.impl;

import java.io.File;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import com.adms.imex.excelformat.DataHolder;
import com.adms.pvcon.service.abstracts.PVConverterAbstarct;
import com.adms.pvcon.util.FileUtil;

/**
 * This service is for Staff Reimbursements
 * @author patawee.cha
 *
 */
public class PayrollServiceImpl extends PVConverterAbstarct {
	
	private final int START_ROW = 16;
	
	@Override
	public StringBuffer doConvert(Sheet sheet) throws Exception {
		initParamValue();
		initCompanyStaffInfo();
		
		Date valueDate = sheet.getRow(8).getCell(9, Row.CREATE_NULL_AS_BLANK).getDateCellValue();
		String compname = sheet.getRow(0).getCell(2, Row.CREATE_NULL_AS_BLANK).getStringCellValue();
		String[] debtAcc = super.companyAccMap.get(compname);
		
		if(debtAcc == null) throw new Exception("!!!! NULL POINTER !!!!! ==> Cannot find account no. for " + compname);
		
		String transactionRefNo = sheet.getRow(6).getCell(9, Row.CREATE_NULL_AS_BLANK).getStringCellValue();
		return processContents(payrollDataList(sheet), debtAcc, valueDate, transactionRefNo);
	}

	@Override
	public String writeout(File file, StringBuffer contents, String encodeType) throws Exception {
		return FileUtil.getInstance().writeout(file, contents, encodeType);
	}
	
	private StringBuffer processContents(Map<String, Double> staffAmtMap, String[] debtAcc, Date valueDate, String transactionRefNo) {
		int lineLenght = 226;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		
		StringBuffer contents = new StringBuffer();
		for(String name : staffAmtMap.keySet()) {
			BigDecimal amt = BigDecimal.valueOf(staffAmtMap.get(name)).setScale(2, BigDecimal.ROUND_HALF_UP);
			DataHolder staffHolder = super.staffInfoMap.get(name);
			DataHolder bankHolder = bankMap.get(staffHolder.get("bank").getStringValue());
			
			for(int i = 0; i < lineLenght; i++) {
				switch(i) {
				case 0	: contents.append(super.paramValueMap.get("PRODUCT_CODE_PTP")); break; // hardcode
				case 2	: contents.append(super.paramValueMap.get("COUNTRY_CODE_TH")); break; // hardcode
				case 4	: contents.append(debtAcc[0]); break; // company acc
				case 6	: contents.append(super.paramValueMap.get("PAYMENT_CCY_THB")); break; // hardcode
				case 8	: contents.append(amt); break; // pay amt
				case 12	: contents.append(sdf.format(valueDate)); break;
//				case 24	: contents.append("101688"); break; /* Ordering Party ID */
				case 26 : contents.append(super.orderingPartyMap.get("PAYROLL_" + debtAcc[1]).get("orderingPartyName").getStringValue()); break; /* Ordering party name */
				case 38	: contents.append(staffHolder.get("name").getStringValue().concat(" ").concat(staffHolder.get("surname").getStringValue())); break; // bene name
				case 48	: contents.append(staffHolder.get("bankAccNo").getStringValue()); break; // bank account no.
				case 62	: contents.append(StringUtils.rightPad(bankHolder.get("bankCode").getStringValue().trim().concat(bankHolder.get("branchCode").getStringValue().trim()), 7, '0')); break; // bank code ex. 0020000
				case 70 : contents.append(transactionRefNo); break; // ex. ADAMSTH-1501C51
				case 114 : contents.append("OUR"); break; // hardcode follow on CITI
				case 156 : contents.append(super.paramValueMap.get("TRANSACTION_TYPE_PAYROLL_GIRO")); break; // GIRO
				case 192 : contents.append(amt); break; // invoice amount
//				case 194 : contents.append(0); break; // hardcode follow on CITI
//				case 196 : contents.append(0); break; // hardcode follow on CITI
//				case 198 : contents.append(0); break; // hardcode follow on CITI
				default	: if(i % 2 != 0) contents.append(SEPARATE_VAL); break;
				}
			}
			contents.append(NEWLINE);
		}
		return contents;
	}
	
	private Map<String, Double> payrollDataList(Sheet sheet) throws Exception {
		if(sheet == null) return null;
		
		int nameCol = 0;
		int amountCol = 10;
		Map<String, Double> amtByNameMap = new HashMap<>();
		List<String> nameNotFound = new ArrayList<>();
		
		for(int r = START_ROW; r < sheet.getLastRowNum(); r++) {
			String name = sheet.getRow(r).getCell(nameCol, Row.CREATE_NULL_AS_BLANK).getStringCellValue();
			Double amount = sheet.getRow(r).getCell(amountCol, Row.CREATE_NULL_AS_BLANK).getNumericCellValue();
			boolean isFound = false;
			
			if(StringUtils.isNotBlank(name) && amount > 0D) {
				for(String key : super.staffInfoMap.keySet()) {
					if(name.contains(key)) {
						if(amtByNameMap.get(key) == null) {
							amtByNameMap.put(key, amount);
						} else {
							amtByNameMap.replace(key, amtByNameMap.get(key) + amount);
						}
						isFound = true;
						break;
					}
				}
				if(!isFound) nameNotFound.add(name);
			} 
		}
		if(nameNotFound.size() > 0) throw new Exception("Name not found in Base data: " + Arrays.toString(nameNotFound.toArray()));
		return amtByNameMap;
	}
	
}
