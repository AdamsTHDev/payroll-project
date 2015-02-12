package com.adms.pvcon.service.impl;

import java.io.File;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import com.adms.pvcon.enums.ETaxType;
import com.adms.pvcon.object.VendorInfo;
import com.adms.pvcon.service.abstracts.PVConverterAbstarct;
import com.adms.pvcon.util.FileUtil;

/**
 * This service is for Supplier (pay by check)
 * @author patawee.cha
 *
 */
public class SupplierServiceImpl extends PVConverterAbstarct {
	
	private final char CHAR_ZERO = '0';
	private final int PAY_TO_ROW = 6;
	private final int PAY_TO_COL_VAL = 2;
	private final int START_ROW = 16;
	
	private Map<String, PaymentWHT> paymentMap;
	
	@Override
	public StringBuffer doConvert(Sheet sheet) throws Exception {
		initParamValue();
		initCompanyStaffInfo();
		
		Date valueDate = sheet.getRow(8).getCell(9, Row.CREATE_NULL_AS_BLANK).getDateCellValue();
//		String 
		String compname = sheet.getRow(0).getCell(2, Row.CREATE_NULL_AS_BLANK).getStringCellValue();
		String[] debtAcc = companyAccMap.get(compname);
		
		if(debtAcc == null) throw new Exception("!!!! NULL POINTER !!!!! ==> Cannot find account no. for " + compname);
		
		String transactionRefNo = sheet.getRow(6).getCell(9, Row.CREATE_NULL_AS_BLANK).getStringCellValue();
		
		paymentDataList(sheet);
		return processContents(debtAcc, valueDate, transactionRefNo);
	}

	@Override
	public String writeout(File file, StringBuffer contents, String encodeType) throws Exception {
		return FileUtil.getInstance().writeout(file, contents, encodeType);
	}
	
	private void invoiceValidation(BigDecimal paymentAmt, BigDecimal invoiceAmt, BigDecimal whtAmt) throws Exception {
		if(paymentAmt.equals(invoiceAmt.subtract(whtAmt))) return; else throw new Exception("ERROR ==> Payment Amount not equal to Invoice Amount minus WHT Amount: " + paymentAmt + " != " + invoiceAmt + " - " + whtAmt);
	}
	
	private StringBuffer processContents(String[] debtAcc, Date valueDate, String transactionRefNo) throws Exception {
		final int lineLenght = 226;
		final String inv = "INV@";
		final String wht = "WHT";
		final Double vat7 = 0.07D;
		
		SimpleDateFormat yyyyMMddFormat = new SimpleDateFormat("yyyyMMdd");
		SimpleDateFormat normalFormat = new SimpleDateFormat("dd/MM/yyyy");
		
		StringBuffer contents = new StringBuffer();
		for(String name : paymentMap.keySet()) {
			PaymentWHT payment = paymentMap.get(name);
			
			Double invoiceVal = payment.sumAllAmt + (payment.sumForCalVat * vat7);
			Double whtTaxVal = 0D;
			for(Double tax : payment.sumForWhtTaxMap.keySet()) {
				whtTaxVal += payment.sumForWhtTaxMap.get(tax) * tax;
			}
			Double paymentVal = invoiceVal - whtTaxVal;
			
			BigDecimal invoiceAmt = convertTo2Digits(invoiceVal);
			BigDecimal paymentAmt = convertTo2Digits(paymentVal);
			BigDecimal whtAmt = convertTo2Digits(whtTaxVal);
			
			invoiceValidation(paymentAmt, invoiceAmt, whtAmt);
			
			VendorInfo vendorInfo = supplierInfoMap.get(name);
			for(int i = 0; i < lineLenght; i++) {
				switch(i) {
				case 0	: contents.append(paramValueMap.get("PRODUCT_CODE_PLC")); break; // hardcode
				case 2	: contents.append(paramValueMap.get("COUNTRY_CODE_TH")); break; // hardcode
				case 4	: contents.append(debtAcc[0]); break; // company acc
				case 6	: contents.append(paramValueMap.get("PAYMENT_CCY_THB")); break; // hardcode
				case 8	: contents.append(paymentAmt); break; // pay amt
				case 12	: contents.append(yyyyMMddFormat.format(valueDate)); break;
//				case 24	: contents.append("Ordering Party  ID"); break;  /* Ordering Party ID */
				case 26 : contents.append(orderingPartyMap.get("CHECK_" + debtAcc[1]).get("orderingPartyName").getStringValue()); break; /* Ordering party name */
				case 38	: contents.append(name); break; // bene name
				case 48	: contents.append(StringUtils.isNoneBlank(vendorInfo.getAcctNo()) ? vendorInfo.getAcctNo() : ""); break; // bank account no.
//				case 62	: contents.append(StringUtils.rightPad(vendorInfo.getBankCode(), 7, CHAR_ZERO)); break; // bank code ex. 0020000
				case 70	: contents.append(transactionRefNo.toUpperCase()); break; // ex. ADAMSTH-1501C51
				case 114 : contents.append("OUR"); break; // indicator
//				case 156 : contents.append(paramValueMap.get("TRANSACTION_TYPE_VENDOR_GIRO")); break; // PLC not need this field
				case 180 : contents.append("RET"); /* MAIL - mailed to beneficiary by Citibank, RET - returned to ordering party, C/R, C/OR */ break;
				case 182 : contents.append(paramValueMap.get("PAYABLE_LOCATION_THA")); break;
				case 192 : contents.append(invoiceAmt); break; // invoice amount
//				case 194 : contents.append(0); break; // hardcode follow on CITI
//				case 196 : contents.append(0); break; // hardcode follow on CITI
				case 198 : contents.append(whtAmt); break; // total WHT Amount
				default	: if(i % 2 != 0) contents.append(SEPARATE_VAL); break;
				}
			}
			contents.append(NEWLINE);
			
//			for skip WHT data
			if(StringUtils.isBlank(vendorInfo.getPnd())) continue;
			
//			Withholder data
			final int firstSectionRow = 18;
			final int maxRow = 33;
			int currentRow = 0;
			String pndCode = null;
			
			for(ETaxType t : ETaxType.values()) {
				if(t.getCode().equals(vendorInfo.getPnd())) {
					pndCode = t.getValue();
					break;
				}
			}
			
			for(int i = 0; i < firstSectionRow; i++) {
				switch(i) {
				case 1 : contents.append(inv + "FAX").append(NEWLINE); break;/* Fix value: FAX */
				case 3 : contents.append(inv + wht).append(pndCode) /* WHT Tax Form*/
						.append(StringUtils.rightPad("", 30)) /* blank space */
						.append(NEWLINE);
					break;
				case 4 : contents.append(inv + wht).append(StringUtils.rightPad("", 10)).append(vendorInfo.getWhtTaxId()).append(NEWLINE);  /* Bene Tax ID */break;
				case 6 : contents.append(inv + wht).append(vendorInfo.getWhtName()).append(NEWLINE); /* Bene Name or WHT Cert */ break;
				case 9 : contents.append(inv + wht).append(StringUtils.isNoneBlank(vendorInfo.getWhtAddress1()) ? vendorInfo.getWhtAddress1() : "").append(NEWLINE); /* Bene Address or WHT Cert */ break;
				case 10 : contents.append(inv + wht).append(StringUtils.isNoneBlank(vendorInfo.getWhtAddress2()) ? vendorInfo.getWhtAddress2() : "").append(NEWLINE); /* Bene Address or WHT Cert */ break;
				case 11 : contents.append(inv + wht).append(StringUtils.isNoneBlank(vendorInfo.getWhtAddress3()) ? vendorInfo.getWhtAddress3() : "").append(NEWLINE); /* Bene Address or WHT Cert */ break;
				case 12 : contents.append(inv + wht).append(StringUtils.isNoneBlank(vendorInfo.getWhtAddress4()) ? vendorInfo.getWhtAddress4() : "").append(NEWLINE); /* Bene Address or WHT Cert */ break;
//				case 13 : contents.append(inv + wht).append(vendorInfo.getWhtAddress()).append(newLine); /* Bene Address or WHT Cert (same as row 9) optional! */ break;
				default: if(i >= 0 && i <= 2) contents.append(inv).append(NEWLINE); else contents.append(inv + wht).append(NEWLINE); break; /* others blank data*/
				}
				currentRow++;
			}
			
//			Adding WHT
			for(Double tax : payment.sumForWhtTaxMap.keySet()) {
				contents.append(inv + wht).append(StringUtils.rightPad("WHT " + (tax * 100) + "%", 60)).append("99000000").append(NEWLINE); /* Description */
				currentRow++;
				contents.append(inv + wht).append(NEWLINE); /* not required */
				currentRow++;
				contents.append(inv + wht).append(NEWLINE); /* not required */
				currentRow++;
				
				String taxRate = String.valueOf(new BigDecimal(tax * 100).setScale(0));
				
				String[] ins = String.valueOf(payment.sumForWhtTaxMap.get(tax).doubleValue()).split("\\.");
				String invoiceAmtPerTax = StringUtils.leftPad(ins[0], 17, CHAR_ZERO)
						.concat(StringUtils.rightPad(Integer.valueOf(ins[1]) > 0 ? ins[1] : "", 2, CHAR_ZERO));
				
				Double whtAmtD = payment.sumForWhtTaxMap.get(tax) * tax;
				String[] whts = String.valueOf(whtAmtD.doubleValue()).split("\\.");
				String whtAmtPerTax = StringUtils.leftPad(whts[0], 15, CHAR_ZERO)
						.concat(StringUtils.rightPad(Integer.valueOf(whts[1]) > 0 ? whts[1] : "", 2, CHAR_ZERO));
				
				contents.append(inv + wht).append(StringUtils.leftPad(taxRate, 4, CHAR_ZERO)) /* WHT Percentage */
						.append(invoiceAmtPerTax) /* Invoice Amount */
						.append(whtAmtPerTax) /* WHT Amount */
						.append(normalFormat.format(valueDate)) /* value date */
						.append(NEWLINE);
				currentRow++;
				contents.append(inv + wht).append("1").append(NEWLINE);/* tax payer code*/
				currentRow++;
			}
			
//			for add blank data
			for(int i = currentRow; i < maxRow; i++) {
				contents.append(inv + wht).append(NEWLINE);
			}
			
		}
		
		return contents;
	}
	
	private void paymentDataList(Sheet sheet) throws Exception {
		if(sheet == null) return;
		
		final String check = "AMT (THB)";
		
		final int vatCol = 21;
		final int whtTaxCol = 22;
		final int amtCol = 10;
		
//		final String invoiceAmt = "Total Amount (THB)";
//		final String withholdTax = "Withholding Tax";
//		final String paymentAmt = "Grand Total Amount (THB)";
		
//		int desAmtCol = 4;
//		int amountCol = 10;
		this.paymentMap = new HashMap<>();
		
		String nameToPay = removeTitle(sheet.getRow(PAY_TO_ROW).getCell(PAY_TO_COL_VAL, Row.CREATE_NULL_AS_BLANK).getStringCellValue(), "");
		
		for(int r = START_ROW; r < sheet.getLastRowNum(); r++) {
			String str = sheet.getRow(r).getCell(0, Row.CREATE_NULL_AS_BLANK).getStringCellValue();
			if(str.equalsIgnoreCase(check)) {
				break;
			}
			
			Double amount = sheet.getRow(r).getCell(amtCol, Row.CREATE_NULL_AS_BLANK).getNumericCellValue();
			String isVat = sheet.getRow(r).getCell(vatCol, Row.CREATE_NULL_AS_BLANK).getStringCellValue();
			Double whtTax = sheet.getRow(r).getCell(whtTaxCol, Row.CREATE_NULL_AS_BLANK).getNumericCellValue();
			
			if(amount != null && amount > 0D) {
				if(supplierInfoMap.containsKey(nameToPay)) {
					PaymentWHT payment = paymentMap.get(nameToPay);
					if(payment == null) {
						payment = new PaymentWHT();
						paymentMap.put(nameToPay, payment);
					}
					payment.sumAllAmt += amount;
					
					if(StringUtils.isNoneBlank(isVat) && isVat.equalsIgnoreCase("Y")) {
						payment.sumForCalVat += amount;
					}
					
					if(whtTax != null && whtTax > 0D) {
						Double val = payment.sumForWhtTaxMap.get(whtTax);
						if(val == null) {
							payment.sumForWhtTaxMap.put(whtTax, amount);
						} else {
							val += amount;
							payment.sumForWhtTaxMap.replace(whtTax, val);
						}
					}
				}
			}
			
		}
//		PaymentWHT pay = paymentMap.get(nameToPay);
//		System.out.println("Pay to: " + nameToPay + " | All Amount: " + pay.sumAllAmt + " | TAX: " + pay.sumForWhtTaxMap + " | Sum Vat: " + pay.sumForCalVat);
	}
	
	private BigDecimal convertTo2Digits(Double val) {
		return BigDecimal.valueOf(val).setScale(2, BigDecimal.ROUND_HALF_UP);
	}
	
	private class PaymentWHT {
		private Double sumForCalVat = 0D;
		private Map<Double, Double> sumForWhtTaxMap = new HashMap<>();
		private Double sumAllAmt = 0D;
		
	}
	
}
