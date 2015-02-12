package com.adms.pvcon.enums;

public enum EFileFormat {

	PARAM_VALUE_FORMAT("PARAM_VALUE_FORMAT", "com/adms/pvcon/format/param_value_format.xml")
	, COMPANY_AND_STAFF_ACCT_FORMAT("NAME_BANK_ACCT_FORMAT", "com/adms/pvcon/format/comp_staff_acct_format.xml");
	
	private String param;
	private String value;

	private EFileFormat(String param, String value) {
		this.param = param;
		this.value = value;
	}

	public String getParam() {
		return param;
	}

	public String getValue() {
		return value;
	}
	
}
