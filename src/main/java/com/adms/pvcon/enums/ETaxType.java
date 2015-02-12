package com.adms.pvcon.enums;

public enum ETaxType {

	TYPE_1("1", "010100"),
	TYPE_1_GOR("1¡", "010101"),
	TYPE_2("2", "020000"),
	TYPE_2_GOR("2¡", "020100"),
	TYPE_3("3", "030000"),
	TYPE_3_GOR("3¡", "030100"),
	TYPE_53("53", "530000");
	
	private String code;
	private String value;
	
	private ETaxType(String code, String value) {
		this.code = code;
		this.value = value;
	}	
	
	public String getCode() {
		return code;
	}
	public String getValue() {
		return value;
	}
	
	
}
