package com.adms.pvcon.service;

import java.io.File;

import org.apache.poi.ss.usermodel.Sheet;

public interface PvConverterService {

	public StringBuffer doConvert(Sheet sheet) throws Exception;
	public String writeout(File file, StringBuffer contents, String encodeType) throws Exception;
}
