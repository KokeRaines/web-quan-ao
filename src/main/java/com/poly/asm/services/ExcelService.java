package com.poly.asm.services;

import org.springframework.stereotype.Service;
import org.apache.poi.*;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import jakarta.servlet.http.HttpServletRequest;
import java.util.*;
import java.io.*;

@Service
public class ExcelService {
	public ByteArrayInputStream exportAllTimeRevenue(List<Object[]> data) throws IOException {
	    String[] columns = {"STT", "Mã danh mục", "Tên danh mục", "Mã sản phẩm", "Tên sản phẩm", "Số lượng đã bán", "Thành tiền"};
	    
	    try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
	        Sheet sheet = workbook.createSheet("Doanh thu tong hop");
	        int rowIdx = 0;
	        String currentMonth = "";
	        int stt = 1;
	        int monthQty = 0;
	        double monthRev = 0;

	        for (int i = 0; i < data.size(); i++) {
	            Object[] rowData = data.get(i);
	            String rowMonth = rowData[0].toString();

	            // Nếu bắt đầu một tháng mới
	            if (!rowMonth.equals(currentMonth)) {
	                // In dòng Tổng cho tháng trước đó (nếu không phải tháng đầu tiên)
	                if (!currentMonth.equals("")) {
	                    Row footerRow = sheet.createRow(rowIdx++);
	                    footerRow.createCell(0).setCellValue("Tổng " + currentMonth);
	                    footerRow.createCell(5).setCellValue(monthQty);
	                    footerRow.createCell(6).setCellValue(monthRev);
	                    rowIdx++; // Dòng trống ngăn cách
	                }

	                // Vẽ Header cho tháng mới
	                currentMonth = rowMonth;
	                stt = 1; monthQty = 0; monthRev = 0;

	                sheet.createRow(rowIdx++).createCell(0).setCellValue("Cửa hàng thời trang VIHO");
	                sheet.createRow(rowIdx++).createCell(0).setCellValue("Chi tiết sản phẩm đã bán");
	                sheet.createRow(rowIdx++).createCell(0).setCellValue("Tháng " + currentMonth);
	                
	                Row headerRow = sheet.createRow(rowIdx++);
	                for (int col = 0; col < columns.length; col++) {
	                    headerRow.createCell(col).setCellValue(columns[col]);
	                }
	            }

	            // Ghi dữ liệu sản phẩm
	            Row row = sheet.createRow(rowIdx++);
	            row.createCell(0).setCellValue(stt++);
	            row.createCell(1).setCellValue(rowData[1].toString()); // Mã DM
	            row.createCell(2).setCellValue(rowData[2].toString()); // Tên DM
	            row.createCell(3).setCellValue(rowData[3].toString()); // Mã SP
	            row.createCell(4).setCellValue(rowData[4].toString()); // Tên SP
	            
	            int qty = Integer.parseInt(rowData[5].toString());
	            double amount = Double.parseDouble(rowData[6].toString());
	            row.createCell(5).setCellValue(qty);
	            row.createCell(6).setCellValue(amount);

	            monthQty += qty;
	            monthRev += amount;

	            // Nếu là dòng cuối cùng của toàn bộ dữ liệu, in dòng Tổng cho tháng cuối
	            if (i == data.size() - 1) {
	                Row footerRow = sheet.createRow(rowIdx++);
	                footerRow.createCell(0).setCellValue("Tổng " + currentMonth);
	                footerRow.createCell(5).setCellValue(monthQty);
	                footerRow.createCell(6).setCellValue(monthRev);
	            }
	        }

	        for (int i = 0; i < columns.length; i++) sheet.autoSizeColumn(i);
	        workbook.write(out);
	        return new ByteArrayInputStream(out.toByteArray());
	    }
	}
}