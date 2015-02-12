package com.adms.pvcon.enums;

public enum ETransactionType {

	SALARY_COMMISSION_PENSION("SALARY_COMMISSION_PENSION", "01"),
	DIVIDEND("DIVIDEND", "02"),
	INTEREST("INTEREST", "03"),
	GOODS_AND_SERVICES("GOODS_AND_SERVIES_PAYMENT", "04"),
	STOCK_SELLING("STOCK_SELLING", "05"),
	TAX_REFUND("TAX_REFUND", "06");
	
	private ETransactionType(String code, String value) {
		this.code = code;
		this.value = value;
	}
	
	private String code;
	private String value;
	
	public String getCode() {
		return code;
	}
	public String getValue() {
		return value;
	}
	
}
