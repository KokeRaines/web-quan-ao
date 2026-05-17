package com.poly.asm.controller;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.poly.asm.daos.OrderRepository;
import com.poly.asm.entitys.Order;
import com.poly.asm.services.OrderService;
import com.poly.asm.services.PdfService;
import com.poly.asm.services.ExcelService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/staff")
public class StaffController {

	@Autowired
    private OrderRepository orderRepository;
	
	@Autowired
    private OrderService orderService;
	
	@Autowired
	private ExcelService excelService;
	
    @GetMapping("/index")
    public String index(Model model) {
        model.addAttribute("message", "Welcome to Staff Dashboard");
        return "staff/index";
    }
    
 // ----------- QUẢN LÝ ĐƠN HÀNG -----------

    // Danh sách đơn hàng
    @GetMapping("/orders")
    public String listOrders(Model model) {
        model.addAttribute("orders", orderRepository.findAll());
        return "staff/orders";
    }

    // Xem chi tiết đơn hàng
    @GetMapping("/orders/{id}")
    public String orderDetails(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Order order = orderRepository.findByIdWithDetails(id);
        if (order == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn hàng!");
            return "redirect:/staff/orders";
        }
        model.addAttribute("order", order);
        return "staff/order-detail";
    }

    // Cập nhật trạng thái đơn hàng
    @PostMapping("/orders/{id}/status")
    public String updateOrderStatus(@PathVariable Long id, @RequestParam("status") String status,
                                   RedirectAttributes redirectAttributes) {
        try {
            orderService.updateOrderStatus(id, status);
            redirectAttributes.addFlashAttribute("success", "Trạng thái đơn hàng đã được cập nhật!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/staff/orders";
    }

    // Xóa đơn hàng
    @GetMapping("/orders/delete/{id}")
    public String deleteOrder(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Order order = orderRepository.findByIdWithDetails(id);
        if (order == null || !"PENDING".equalsIgnoreCase(order.getStatus())) {
            redirectAttributes.addFlashAttribute("error", "Chỉ có thể xóa đơn hàng ở trạng thái Chờ xử lý");
            return "redirect:/staff/orders";
        }
        try {
            orderService.updateOrderStatus(id, "CANCELED"); // Hoàn kho trước khi xóa
            orderRepository.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Đơn hàng đã được xóa!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/staff/orders";
    }
    @GetMapping("/statistics")
    public String statistics(Model model) {
        model.addAttribute("totalRevenue", orderService.getTotalRevenue());
        model.addAttribute("revenueData", orderService.getRevenueData());
        return "staff/statistics";
    }
    @GetMapping("/statistics/export-all")
    public ResponseEntity<InputStreamResource> exportAllExcel() throws IOException {
        List<Object[]> data = orderRepository.getAllTimeProductRevenue();
        ByteArrayInputStream in = excelService.exportAllTimeRevenue(data);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=tong-hop-doanh-thu.xlsx")
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }
    
    @Autowired PdfService pdfService;

    @GetMapping("/orders/print/{id}")
    //public ResponseEntity<InputStreamResource> printInvoice(@PathVariable("id") Long id) throws IOException
    public ResponseEntity<InputStreamResource> printInvoice(@PathVariable() Long id) throws IOException {
     Order order = orderRepository.findById(id).orElse(null);
     if (order == null) return ResponseEntity.notFound().build();

     ByteArrayInputStream bis = pdfService.exportInvoice(order);

     HttpHeaders headers = new HttpHeaders();
     headers.add("Content-Disposition", "inline; filename=hoa-don-" + id + ".pdf");

     return ResponseEntity.ok()
             .headers(headers)
             .contentType(MediaType.APPLICATION_PDF)
             .body(new InputStreamResource(bis));
    }
}