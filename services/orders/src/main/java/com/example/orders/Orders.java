package com.example.orders;

import java.util.List;

/** The order book, kept free of servlet code so it is easy to test. */
public final class Orders {
    public record Order(int id, String sku, int quantity) {}

    public static final List<Order> ALL = List.of(new Order(1001, "KB-01", 2), new Order(1002, "MN-02", 1), new Order(1003, "HS-03", 4));

    private Orders() {}

    public static int totalItems() {
        return ALL.stream().mapToInt(Order::quantity).sum();
    }

    public static String toJson() {
        StringBuilder sb = new StringBuilder("{\"orders\":[");
        for (int i = 0; i < ALL.size(); i++) {
            Order o = ALL.get(i);
            if (i > 0) sb.append(',');
            sb.append("{\"id\":").append(o.id()).append(",\"sku\":\"").append(o.sku()).append("\",\"quantity\":").append(o.quantity()).append('}');
        }
        return sb.append("],\"totalItems\":").append(totalItems()).append('}').toString();
    }
}
