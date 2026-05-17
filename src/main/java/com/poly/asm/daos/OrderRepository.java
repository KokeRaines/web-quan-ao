package com.poly.asm.daos;

import com.poly.asm.entitys.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    // Lấy đơn hàng có trạng thái là "PENDING"
    List<Order> findByStatus(String status);

    // Thay đổi query để truy vấn theo user và status
    List<Order> findByUser_IdAndStatus(Integer userId, String status);

    List<Order> findByUser_Id(Integer userId);

    // Lấy đơn hàng kèm chi tiết
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderDetails od LEFT JOIN FETCH od.variant v LEFT JOIN FETCH v.product p WHERE o.id = ?1")
    Order findByIdWithDetails(Long id);

    List<Order> findByUser_Id(Long userId);
    
    // Thống kê tổng doanh thu
    @Query("SELECT SUM(o.totalPrice) FROM Order o WHERE o.status = 'DELIVERED'")
    Double getTotalRevenue();

    // Thống kê doanh thu theo tháng
    @Query(value = "SELECT MONTH(order_date) AS month, SUM(total_price) AS revenue " +
                   "FROM Orders WHERE YEAR(order_date) = YEAR(GETDATE()) AND status = 'DELIVERED' " +
                   "GROUP BY MONTH(order_date) " +
                   "ORDER BY MONTH(order_date) ASC", nativeQuery = true)
    List<Object[]> getRevenueByMonth();
    
    // Lấy danh sách tháng/năm có đơn hàng thành công (MM/yyyy)
    @Query(value = "SELECT DISTINCT FORMAT(order_date, 'MM/yyyy') FROM Orders " +
                   "WHERE status = 'DELIVERED' ORDER BY FORMAT(order_date, 'MM/yyyy') DESC", nativeQuery = true)
    List<String> getMonthsWithOrders();

 // Truy vấn toàn bộ doanh thu sản phẩm, nhóm theo Tháng/Năm và Sản phẩm
    @Query(value = "SELECT FORMAT(o.order_date, 'MM/yyyy') AS monthYear, " +
                   "c.id, c.name, p.id, p.name, SUM(od.quantity), SUM(od.quantity * od.price), " +
                   "YEAR(o.order_date) AS y, MONTH(o.order_date) AS m " +
                   "FROM Orders o " +
                   "JOIN OrderDetails od ON o.id = od.order_id " +
                   "JOIN ProductVariants pv ON od.variant_id = pv.id " +
                   "JOIN Products p ON pv.product_id = p.id " +
                   "JOIN Categories c ON p.category_id = c.id " +
                   "WHERE o.status = 'DELIVERED' " +
                   "GROUP BY FORMAT(o.order_date, 'MM/yyyy'), c.id, c.name, p.id, p.name, YEAR(o.order_date), MONTH(o.order_date) " +
                   "ORDER BY y DESC, m DESC, c.id ASC", nativeQuery = true)
    List<Object[]> getAllTimeProductRevenue();
}