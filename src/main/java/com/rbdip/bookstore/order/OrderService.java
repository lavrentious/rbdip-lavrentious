package com.rbdip.bookstore.order;

import com.rbdip.bookstore.product.Product;
import com.rbdip.bookstore.product.ProductRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    private static final String DEFAULT_CUSTOMER_TYPE = "regular";

    private final OrderValidator orderValidator;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderNotificationService notificationService;
    private final PricingCalculator pricingCalculator;

    public OrderService(
            OrderValidator orderValidator,
            ProductRepository productRepository,
            CustomerRepository customerRepository,
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            OrderNotificationService notificationService,
            PricingCalculator pricingCalculator) {
        this.orderValidator = orderValidator;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.notificationService = notificationService;
        this.pricingCalculator = pricingCalculator;
    }

    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        orderValidator.validate(request);

        List<OrderLine> lines = resolveLines(request.items());
        BigDecimal total = pricingCalculator.calculateOrderTotal(
                pricingLines(lines),
                request.customerType() == null ? DEFAULT_CUSTOMER_TYPE : request.customerType(),
                request.couponCode());

        Order order = orderRepository.save(new Order(resolveCustomer(request), Order.STATUS_NEW));

        for (OrderLine line : lines) {
            orderItemRepository.save(new OrderItem(order.getId(), line.product(), line.quantity()));
        }

        notificationService.sendConfirmationEmail(request.customerFullName(), order.getId(), total);

        return order;
    }

    private Customer resolveCustomer(CreateOrderRequest request) {
        return customerRepository.findByFullName(request.customerFullName())
                .orElseGet(() -> customerRepository.save(new Customer(
                        request.customerFullName(), request.customerAddress(), request.customerPhone())));
    }

    private List<OrderLine> resolveLines(List<CreateOrderRequest.Item> items) {
        List<OrderLine> lines = new ArrayList<>();
        for (CreateOrderRequest.Item item : items) {
            Product product = productRepository.findById(item.productId())
                    .orElseThrow(() -> new IllegalArgumentException("product " + item.productId() + " not found"));
            int quantity = item.quantity() == null ? 1 : item.quantity();
            lines.add(new OrderLine(product, quantity));
        }
        return lines;
    }

    private static List<PricingCalculator.LineItem> pricingLines(List<OrderLine> lines) {
        return lines.stream()
                .map(line -> new PricingCalculator.LineItem(line.product().getPrice(), line.quantity()))
                .toList();
    }
}
