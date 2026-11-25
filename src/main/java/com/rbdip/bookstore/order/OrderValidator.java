package com.rbdip.bookstore.order;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class OrderValidator {

    public void validate(CreateOrderRequest request) {
        validateCustomerFields(request);
        validateItems(request.items());
    }

    private static void validateCustomerFields(CreateOrderRequest request) {
        if (request.customerFullName() == null || request.customerFullName().isBlank()) {
            throw new IllegalArgumentException("customerFullName is required");
        }
        if (request.customerAddress() == null || request.customerAddress().isBlank()) {
            throw new IllegalArgumentException("customerAddress is required");
        }
    }

    private static void validateItems(List<CreateOrderRequest.Item> items) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("order must contain at least one item");
        }
        for (CreateOrderRequest.Item item : items) {
            if (item.quantity() != null && item.quantity() <= 0) {
                throw new IllegalArgumentException("quantity must be positive");
            }
        }
    }
}
