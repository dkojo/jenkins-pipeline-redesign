package com.example.orders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class OrdersTest {
    @Test
    void totalItemsAddsUpEveryOrder() {
        assertEquals(3, Orders.totalItems());
    }

    @Test
    void jsonListsOrdersAndTheTotal() {
        assertTrue(Orders.toJson().endsWith("\"totalItems\":3}"));
    }
}
