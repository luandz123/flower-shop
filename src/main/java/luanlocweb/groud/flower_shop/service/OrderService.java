package luanlocweb.groud.flower_shop.service;

import luanlocweb.groud.flower_shop.model.Order;
import luanlocweb.groud.flower_shop.model.OrderItem;
import luanlocweb.groud.flower_shop.model.Product;
import luanlocweb.groud.flower_shop.model.User;
import luanlocweb.groud.flower_shop.repository.OrderRepository;
import luanlocweb.groud.flower_shop.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private ProductRepository productRepository;
    
    @Autowired
    private ProductService productService;

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Order not found with id: " + id));
    }

    public List<Order> getOrdersByStatus(String status) {
        return orderRepository.findByStatus(status);
    }

    public List<Order> getOrdersByUser(Long userId) {
        return orderRepository.findByUserIdOrderByOrderDateDesc(userId);
    }

    public Order updateOrderStatus(Long id, String status) {
        Order order = getOrderById(id);
        order.setStatus(status);
        return orderRepository.save(order);
    }
    
    // Thêm phương thức tạo đơn hàng mới từ giỏ hàng
    @Transactional
    public Order createOrder(User user, Map<Long, Integer> cart, String shippingAddress, 
                             String paymentMethod, String notes) {
        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setShippingAddress(shippingAddress);
        order.setPaymentMethod(paymentMethod);
        order.setStatus("PENDING");
        order.setNotes(notes);
        
        // Tạo các mục đơn hàng từ giỏ hàng
        List<OrderItem> orderItems = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : cart.entrySet()) {
            Product product = productService.getProductById(entry.getKey());
            
            // Kiểm tra số lượng trong kho
            if (product.getStockQuantity() < entry.getValue()) {
                throw new RuntimeException("Not enough stock for product: " + product.getName());
            }
            
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(entry.getValue());
            orderItem.setPrice(product.getPrice());
            orderItems.add(orderItem);
            
            // Cập nhật số lượng trong kho
            product.setStockQuantity(product.getStockQuantity() - entry.getValue());
            productRepository.save(product);
        }
        
        order.setOrderItems(orderItems);
        
        // Tính tổng tiền đơn hàng
        order.calculateTotal();
        
        // Lưu đơn hàng vào cơ sở dữ liệu
        return orderRepository.save(order);
    }

    public Map<String, Object> getSalesData() {
        Map<String, Object> salesData = new HashMap<>();
        
        // Lấy tất cả đơn hàng
        List<Order> allOrders = orderRepository.findAll();
        
        // Tổng doanh thu - sử dụng getTotal() mới
        BigDecimal totalRevenue = allOrders.stream()
            .filter(order -> "COMPLETED".equals(order.getStatus()) || "DELIVERED".equals(order.getStatus()))
            .map(Order::getTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Tổng số đơn hàng
        long totalOrders = allOrders.size();
        
        // Đơn hàng theo trạng thái
        Map<String, Long> ordersByStatus = new HashMap<>();
        ordersByStatus.put("PENDING", allOrders.stream()
            .filter(order -> "PENDING".equals(order.getStatus())).count());
        ordersByStatus.put("PROCESSING", allOrders.stream()
            .filter(order -> "PROCESSING".equals(order.getStatus())).count());
        ordersByStatus.put("SHIPPED", allOrders.stream()
            .filter(order -> "SHIPPED".equals(order.getStatus())).count());
        ordersByStatus.put("DELIVERED", allOrders.stream()
            .filter(order -> "DELIVERED".equals(order.getStatus())).count());
        ordersByStatus.put("CANCELLED", allOrders.stream()
            .filter(order -> "CANCELLED".equals(order.getStatus())).count());
        
        // Doanh thu theo tháng (6 tháng gần đây)
        Map<String, BigDecimal> revenueByMonth = new LinkedHashMap<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-yyyy");
        LocalDateTime now = LocalDateTime.now();
        
        for (int i = 5; i >= 0; i--) {
            LocalDateTime monthStart = now.minusMonths(i).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime monthEnd;
            if (i == 0) {
                monthEnd = now;
            } else {
                monthEnd = monthStart.plusMonths(1).minusNanos(1);
            }
            
            final LocalDateTime finalMonthStart = monthStart;
            final LocalDateTime finalMonthEnd = monthEnd;
            
            BigDecimal monthlyRevenue = allOrders.stream()
                .filter(order -> "COMPLETED".equals(order.getStatus()) || "DELIVERED".equals(order.getStatus()))
                .filter(order -> {
                    LocalDateTime orderDate = order.getOrderDate();
                    return orderDate != null && orderDate.isAfter(finalMonthStart) && orderDate.isBefore(finalMonthEnd);
                })
                .map(Order::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            revenueByMonth.put(monthStart.format(formatter), monthlyRevenue);
        }
        
        salesData.put("totalRevenue", totalRevenue);
        salesData.put("totalOrders", totalOrders);
        salesData.put("ordersByStatus", ordersByStatus);
        salesData.put("revenueByMonth", revenueByMonth);
        
        return salesData;
    }
}