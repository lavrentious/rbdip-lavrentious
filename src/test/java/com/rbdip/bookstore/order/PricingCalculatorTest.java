package com.rbdip.bookstore.order;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class PricingCalculatorTest {

    private final PricingCalculator calculator = new PricingCalculator();

    @Test
    void regularCustomerPaysFullPrice() {
        BigDecimal total = calculator.calculateOrderTotal(
                List.of(new PricingCalculator.LineItem(new BigDecimal("25.00"), 2)), "regular", null);

        assertThat(total).isEqualByComparingTo("50.00");
    }

    @Test
    void missingCustomerTypeIsTreatedAsRegular() {
        BigDecimal total = calculator.calculateOrderTotal(
                List.of(new PricingCalculator.LineItem(new BigDecimal("25.00"), 2)), null, null);

        assertThat(total).isEqualByComparingTo("50.00");
    }

    @Test
    void vipCustomerGetsTenPercentOff() {
        BigDecimal total = calculator.calculateOrderTotal(
                List.of(new PricingCalculator.LineItem(new BigDecimal("100.00"), 1)), "vip", null);

        assertThat(total).isEqualByComparingTo("90.00");
    }

    @Test
    void wholesaleCustomerGetsFifteenPercentOff() {
        BigDecimal total = calculator.calculateOrderTotal(
                List.of(new PricingCalculator.LineItem(new BigDecimal("100.00"), 1)), "wholesale", null);

        assertThat(total).isEqualByComparingTo("85.00");
    }

    @Test
    void unknownCustomerTypePaysFullPrice() {
        BigDecimal total = calculator.calculateOrderTotal(
                List.of(new PricingCalculator.LineItem(new BigDecimal("100.00"), 1)), "student", null);

        assertThat(total).isEqualByComparingTo("100.00");
    }

    @Test
    void bulkDiscountAppliesAboveTenUnits() {
        BigDecimal total = calculator.calculateOrderTotal(
                List.of(new PricingCalculator.LineItem(new BigDecimal("10.00"), 11)), "regular", null);

        assertThat(total).isEqualByComparingTo("104.50");
    }

    @Test
    void bulkDiscountDoesNotApplyAtTenUnits() {
        BigDecimal total = calculator.calculateOrderTotal(
                List.of(new PricingCalculator.LineItem(new BigDecimal("10.00"), 10)), "regular", null);

        assertThat(total).isEqualByComparingTo("100.00");
    }

    @Test
    void save10CouponSubtractsFixedAmount() {
        BigDecimal total = calculator.calculateOrderTotal(
                List.of(new PricingCalculator.LineItem(new BigDecimal("20.00"), 1)), "regular", "SAVE10");

        assertThat(total).isEqualByComparingTo("10.00");
    }

    @Test
    void save20PercentCouponCutsTotalByFifth() {
        BigDecimal total = calculator.calculateOrderTotal(
                List.of(new PricingCalculator.LineItem(new BigDecimal("50.00"), 1)), "regular", "SAVE20PERCENT");

        assertThat(total).isEqualByComparingTo("40.00");
    }

    @Test
    void unknownCouponIsIgnored() {
        BigDecimal total = calculator.calculateOrderTotal(
                List.of(new PricingCalculator.LineItem(new BigDecimal("50.00"), 1)), "regular", "GIMME5");

        assertThat(total).isEqualByComparingTo("50.00");
    }

    @Test
    void negativeTotalAfterCouponIsClampedToZero() {
        BigDecimal total = calculator.calculateOrderTotal(
                List.of(new PricingCalculator.LineItem(new BigDecimal("5.00"), 1)), "regular", "SAVE10");

        assertThat(total).isEqualByComparingTo("0.00");
    }

    @Test
    void largeOrderDiscountAppliesAboveThousand() {
        BigDecimal total = calculator.calculateOrderTotal(
                List.of(new PricingCalculator.LineItem(new BigDecimal("2000.00"), 1)), "regular", null);

        assertThat(total).isEqualByComparingTo("1960.00");
    }

    @Test
    void largeOrderDiscountDoesNotApplyAtThousand() {
        BigDecimal total = calculator.calculateOrderTotal(
                List.of(new PricingCalculator.LineItem(new BigDecimal("1000.00"), 1)), "regular", null);

        assertThat(total).isEqualByComparingTo("1000.00");
    }

    @Test
    void discountsStack() {
        BigDecimal total = calculator.calculateOrderTotal(
                List.of(new PricingCalculator.LineItem(new BigDecimal("100.00"), 15)), "vip", "SAVE20PERCENT");

        assertThat(total).isEqualByComparingTo("1005.48");
    }

    @Test
    void sumsMultipleLines() {
        BigDecimal total = calculator.calculateOrderTotal(
                List.of(
                        new PricingCalculator.LineItem(new BigDecimal("10.00"), 2),
                        new PricingCalculator.LineItem(new BigDecimal("5.00"), 3)),
                "regular", null);

        assertThat(total).isEqualByComparingTo("35.00");
    }

    @Test
    void emptyOrderTotalsZero() {
        BigDecimal total = calculator.calculateOrderTotal(List.of(), "regular", null);

        assertThat(total).isEqualByComparingTo("0.00");
    }

    @Test
    void roundsHalfUpToCents() {
        BigDecimal total = calculator.calculateOrderTotal(
                List.of(new PricingCalculator.LineItem(new BigDecimal("3.335"), 1)), "regular", null);

        assertThat(total).isEqualByComparingTo("3.34");
    }
}
