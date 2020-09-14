package com.example.demo.controllers;

import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.UserOrder;
import com.example.demo.model.persistence.repositories.OrderRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import java.util.List;
import org.apache.logging.log4j.*;
import com.splunk.logging.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepository orderRepository;

    private final Logger splunkLogger = org.apache.logging.log4j.core.LoggerContext.getContext().getLogger(UserController.class.getSimpleName());

    @PostMapping("/submit/{username}")
    public ResponseEntity<UserOrder> submitOrderForUser(@PathVariable String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            splunkLogger.info("No user with username {} exists", username);
            return ResponseEntity.notFound().build();
        }
        UserOrder order = UserOrder.createFromCart(user.getCart());
        orderRepository.save(order);
        splunkLogger.info("Submit order successful for {} with total {}", username, order.getTotal());
        return ResponseEntity.ok(order);
    }

    @GetMapping("/history/{username}")
    public ResponseEntity<List<UserOrder>> getOrdersForUser(@PathVariable String username) {
        User user = userRepository.findByUsername(username);
        if (user == null) {
            splunkLogger.warn("Get orders failed for {}", username);
            return ResponseEntity.notFound().build();
        }
        splunkLogger.info("Got orders for {}!", username);
        return ResponseEntity.ok(orderRepository.findByUser(user));
    }
}
