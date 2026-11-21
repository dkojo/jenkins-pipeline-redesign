package com.example.orders;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/** Deployed as orders.war, so Tomcat serves it at /orders/health and /orders/list. */
@WebServlet(urlPatterns = {"/health", "/list"})
public class OrdersServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setCharacterEncoding("UTF-8");
        if ("/health".equals(req.getServletPath())) {
            resp.setContentType("text/plain");
            resp.getWriter().write("ok");
        } else {
            resp.setContentType("application/json");
            resp.getWriter().write(Orders.toJson());
        }
    }
}
