package com.rbdip.bookstore.order;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class PricingCalculator {

    public record LineItem(BigDecimal price, int quantity) {
    }

    private static final int BULK_QUANTITY_THRESHOLD = 10;
    private static final BigDecimal BULK_DISCOUNT = BigDecimal.valueOf(0.95);
    private static final BigDecimal VIP_DISCOUNT = BigDecimal.valueOf(0.9);
    private static final BigDecimal WHOLESALE_DISCOUNT = BigDecimal.valueOf(0.85);
    private static final BigDecimal SAVE10_AMOUNT = BigDecimal.TEN;
    private static final BigDecimal SAVE20PERCENT_DISCOUNT = BigDecimal.valueOf(0.8);
    private static final BigDecimal LARGE_ORDER_THRESHOLD = BigDecimal.valueOf(1000);
    private static final BigDecimal LARGE_ORDER_DISCOUNT = BigDecimal.valueOf(0.98);
    private static final String VIP_CUSTOMER = "vip";
    private static final String WHOLESALE_CUSTOMER = "wholesale";
    private static final String SAVE10_COUPON = "SAVE10";
    private static final String SAVE20PERCENT_COUPON = "SAVE20PERCENT";

    public BigDecimal calculateOrderTotal(List<LineItem> items, String customerType, String couponCode) {
        BigDecimal total = sumLinePrices(items);
        total = applyCustomerDiscount(total, customerType);
        total = applyCoupon(total, couponCode);
        total = clampNegative(total);
        total = applyLargeOrderDiscount(total);
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal sumLinePrices(List<LineItem> items) {
        BigDecimal total = BigDecimal.ZERO;
        for (LineItem item : items) {
            BigDecimal linePrice = item.price().multiply(BigDecimal.valueOf(item.quantity()));
            if (item.quantity() > BULK_QUANTITY_THRESHOLD) {
                linePrice = linePrice.multiply(BULK_DISCOUNT);
            }
            total = total.add(linePrice);
        }
        return total;
    }

    private static BigDecimal applyCustomerDiscount(BigDecimal total, String customerType) {
        if (VIP_CUSTOMER.equals(customerType)) {
            return total.multiply(VIP_DISCOUNT);
        }
        if (WHOLESALE_CUSTOMER.equals(customerType)) {
            return total.multiply(WHOLESALE_DISCOUNT);
        }
        return total;
    }

    private static BigDecimal applyCoupon(BigDecimal total, String couponCode) {
        if (SAVE10_COUPON.equals(couponCode)) {
            return total.subtract(SAVE10_AMOUNT);
        }
        if (SAVE20PERCENT_COUPON.equals(couponCode)) {
            return total.multiply(SAVE20PERCENT_DISCOUNT);
        }
        return total;
    }

    private static BigDecimal clampNegative(BigDecimal total) {
        if (total.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO;
        }
        return total;
    }

    private static BigDecimal applyLargeOrderDiscount(BigDecimal total) {
        if (total.compareTo(LARGE_ORDER_THRESHOLD) > 0) {
            return total.multiply(LARGE_ORDER_DISCOUNT);
        }
        return total;
    }
}
