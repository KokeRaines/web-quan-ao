package com.poly.asm.services;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.poly.asm.entitys.Order;
import com.poly.asm.entitys.OrderDetail;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
@Service
public class PdfService {

    public ByteArrayInputStream exportInvoice(Order order) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(out);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        // Khởi tạo Font tiếng Việt (Cần file font .ttf trong thư mục resources/static/fonts)
        // Nếu chưa có font, bạn có thể tải font "Arial" hoặc "Times New Roman" ttf về dự án
        try {
            // 1. Nạp file font từ thư mục resources
            ClassPathResource fontResource = new ClassPathResource("static/fonts/times.ttf");
            byte[] fontBytes = fontResource.getInputStream().readAllBytes();

            // 2. Khởi tạo PdfFont với bảng mã Identity-H để hiển thị tiếng Việt
            PdfFont timesFont = PdfFontFactory.createFont(fontBytes, PdfEncodings.IDENTITY_H);

            // 3. Áp dụng font cho toàn bộ Document
            document.setFont(timesFont);
            
        } catch (Exception e) {
            // Log lỗi nếu không tìm thấy file hoặc lỗi nạp font
            System.err.println("Lỗi nạp font Times New Roman: " + e.getMessage());
        }
        //String fontPath = "src/main/resources/static/fonts/Arial.ttf"; 
        //PdfFont font = PdfFontFactory.createFont(fontPath, com.itextpdf.kernel.pdf.PdfName.IdentityH.getValue());
        //document.setFont(font);

        // 1. Tên công ty & Tiêu đề
        document.add(new Paragraph("CỬA HÀNG THỜI TRANG VIHO")
                .setTextAlignment(TextAlignment.CENTER).setBold().setFontSize(14));
        document.add(new Paragraph("HÓA ĐƠN BÁN HÀNG")
                .setTextAlignment(TextAlignment.CENTER).setBold().setFontSize(18));

        // 2. Thông tin khách hàng
        document.add(new Paragraph("Họ tên người đặt: " + order.getFullname()));
        document.add(new Paragraph("Điện thoại: " + order.getPhone()));
        document.add(new Paragraph("Địa chỉ: " + order.getAddress()));
        document.add(new Paragraph("Phương thức thanh toán: " + order.getPaymentMethod()));
        document.add(new Paragraph("Ngày đặt: " + order.getOrderDate()));
        document.add(new Paragraph("\n"));

        // 3. Bảng danh sách sản phẩm (Giống image_840cbd.png)
        float[] columnWidths = {3, 1, 1, 1, 2, 2};
        Table table = new Table(UnitValue.createPercentArray(columnWidths)).useAllAvailableWidth();

        String[] headers = {"Tên sản phẩm", "Size", "Màu", "Số lượng", "Đơn giá", "Tổng"};
        for (String h : headers) {
            table.addHeaderCell(new Cell().add(new Paragraph(h).setBold()));
        }

        for (OrderDetail detail : order.getOrderDetails()) {
            table.addCell(new Cell().add(new Paragraph(detail.getVariant().getProduct().getName())));
            table.addCell(new Cell().add(new Paragraph(detail.getVariant().getSize())));
            table.addCell(new Cell().add(new Paragraph(detail.getVariant().getColor())));
            table.addCell(new Cell().add(new Paragraph(String.valueOf(detail.getQuantity()))));
            table.addCell(new Cell().add(new Paragraph(String.format("%,.2f VNĐ", detail.getPrice()))));
            table.addCell(new Cell().add(new Paragraph(String.format("%,.2f VNĐ", detail.getPrice().multiply(new java.math.BigDecimal(detail.getQuantity()))))));
        }
        document.add(table);

        document.add(new Paragraph("\nTổng cộng: " + String.format("%,.2f VNĐ", order.getTotalPrice()))
                .setTextAlignment(TextAlignment.RIGHT).setBold());

        // 4. Phần ký tên
        Table signatureTable = new Table(UnitValue.createPercentArray(new float[]{1, 1})).useAllAvailableWidth();
        signatureTable.addCell(new Cell().add(new Paragraph("Nhân viên bán hàng\n(Ký và ghi rõ họ tên)")).setBorder(com.itextpdf.layout.borders.Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER));
        signatureTable.addCell(new Cell().add(new Paragraph("Khách hàng\n(Ký và ghi rõ họ tên)")).setBorder(com.itextpdf.layout.borders.Border.NO_BORDER).setTextAlignment(TextAlignment.CENTER));
        document.add(new Paragraph("\n\n"));
        document.add(signatureTable);

        document.close();
        return new ByteArrayInputStream(out.toByteArray());
    }
}