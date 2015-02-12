package com.adms.pvcon.app;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JDialog;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import com.adms.pvcon.service.PvConverterFactory;
import com.adms.pvcon.service.PvConverterService;
import com.adms.pvcon.service.impl.PayrollServiceImpl;
import com.adms.pvcon.service.impl.SupplierServiceImpl;
import com.adms.pvcon.util.GetResourceUtil;
import com.adms.utils.Logger;

public class PVConverterApp extends JDialog {
	
	private static final long serialVersionUID = 6797768910633132882L;

	private static final Logger log;
	
	static {
		log = Logger.getLogger();
		OutputStream os = null;
		try {
			os = new FileOutputStream(GetResourceUtil.getInstance().getConfigValue("logger.out.path"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		log.setOutputStream(os);
	}
	
	public static void main(String[] args) {
		InputStream is = null;
		Workbook wb = null;
		
		try {
			log.info("Start Converting ==> " + new Date());
			String fixFile = GetResourceUtil.getInstance().getConfigValue("input.pv.file.name");
			String pathStr = GetResourceUtil.getInstance().getConfigValue("input.pv.file.path");
			
			if(StringUtils.isBlank(pathStr)) return;
			
			if(StringUtils.isBlank(fixFile)) {
				File path = new File(pathStr);
				for(File file : path.listFiles()) {
					is = new FileInputStream(file);
					wb = WorkbookFactory.create(is);
					process(wb, file.getName());
				}
			} else {
				is = new FileInputStream(pathStr.concat("/").concat(fixFile));
				wb = WorkbookFactory.create(is);
				process(wb, fixFile);
			}
			
		} catch (Exception e) {
			log.error("ERROR!!! => ", e);
		} finally {
			try {
				is.close();
			} catch (IOException e) {}
		}
		log.info("End Converting ==> " + new Date());
	}
	
	private static void process(Workbook wb, String fileName) {
		StringBuffer strbuffPTP = new StringBuffer();
		StringBuffer strbuffPLC = new StringBuffer();
		
		for(int i = 0; i < wb.getNumberOfSheets(); i++) {
			Sheet sheet = wb.getSheetAt(i);
			String sheetName = sheet.getSheetName().trim();
			
			if(!StringUtils.isNumeric(sheetName)) continue;
			log.info("PROCESS.... Sheet:" + sheetName);
//			System.out.println("PROCESS.... Sheet:" + sheetName);
			
			Row row = sheet.getRow(6);
			Cell cell = row.getCell(2, Row.CREATE_NULL_AS_BLANK);
			String payto = cell.getStringCellValue();
			
			if(payto.toLowerCase().contains("payroll")) continue; //skip if it is payroll
			
			PvConverterService converter = PvConverterFactory.getService(payto);
			try {
				if(converter != null) {
					StringBuffer contents = converter.doConvert(sheet);
					if(StringUtils.isBlank(contents)) continue;
					
					if(converter instanceof PayrollServiceImpl) {
						strbuffPLC.append(contents);
					} else if(converter instanceof SupplierServiceImpl) {
						strbuffPTP.append(contents);
					}
				}
				
			} catch (Exception e) {
				log.error("ERROR!!! => ", e);
			}
		}
		
		try {
			write(new PayrollServiceImpl(), strbuffPLC, fileName);
		} catch(Exception e) {
			log.error("Error while writing file", e);
		}
		
		try {
			write(new SupplierServiceImpl(), strbuffPTP, fileName);
		} catch(Exception e) {
			log.error("Error while writing file", e);
		}
		
		try {
			wb.close();
		} catch (IOException e) {}
		
	}
	
	private static void write(PvConverterService service, StringBuffer contents, String xlsName) throws Exception {
//		write file
		GetResourceUtil resource = GetResourceUtil.getInstance();
		String strDateFormat = resource.getConfigValue("output.txt.date.format");
		SimpleDateFormat sdf = new SimpleDateFormat(strDateFormat);
		String encodingType = resource.getConfigValue("encoding.type");
		String txtEx = resource.getConfigValue("file.type.text");
		
		String outPath = resource.getConfigValue("output.txt.file.path");
		String fileName = "";
		
		if(service instanceof PayrollServiceImpl) {
			fileName = resource.getConfigValue("output.txt.file.name.ptp");
		} else if(service instanceof SupplierServiceImpl) {
			fileName = resource.getConfigValue("output.txt.file.name.plc");
		}
//		replace yyyyMMdd to date
		fileName = fileName.replace(strDateFormat, sdf.format(new Date()));
		fileName = fileName.substring(0, fileName.indexOf(txtEx) - 1) 
				+ "_" + xlsName.replace(".xls", "") + "_" 
				+ fileName.substring(fileName.indexOf(txtEx), fileName.length());
		
		
		String fileOutPath = service.writeout(new File(outPath + "/" + fileName), contents, encodingType);
		log.info("Write file finished ==> " + fileOutPath);
	}

}
