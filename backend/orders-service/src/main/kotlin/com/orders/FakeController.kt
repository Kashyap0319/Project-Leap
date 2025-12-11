package com.orders

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.RestController
import kotlin.random.Random

@RestController
class FakeController {

    @GetMapping("/orders/create")
    fun createOrder(): Map<String, String> {
        // Random latency between 20-800ms (some will be slow > 500ms)
        Thread.sleep(Random.nextLong(20, 800))
        return mapOf("message" to "Order created", "orderId" to Random.nextInt(1000, 9999).toString())
    }

    @GetMapping("/orders/list")
    fun listOrders(): Map<String, Any> {
        // Fast endpoint
        Thread.sleep(Random.nextLong(10, 300))
        return mapOf("message" to "Orders listed", "count" to Random.nextInt(0, 100))
    }

    @GetMapping("/orders/{id}")
    fun getOrder(): Map<String, String> {
        Thread.sleep(Random.nextLong(15, 250))
        return mapOf("message" to "Order retrieved")
    }

    @PostMapping("/orders")
    fun createOrderPost(): Map<String, String> {
        // Sometimes slow
        Thread.sleep(Random.nextLong(50, 600))
        return mapOf("message" to "Order created via POST")
    }

    @PutMapping("/orders/{id}")
    fun updateOrder(): Map<String, String> {
        Thread.sleep(Random.nextLong(30, 400))
        return mapOf("message" to "Order updated")
    }

    @DeleteMapping("/orders/{id}")
    fun deleteOrder(): Map<String, String> {
        Thread.sleep(Random.nextLong(20, 200))
        return mapOf("message" to "Order deleted")
    }

    @GetMapping("/orders/payment/process")
    fun processPayment(): Map<String, String> {
        // Sometimes fails (5xx) - simulate server error
        val shouldFail = Random.nextDouble() < 0.15 // 15% chance of error
        Thread.sleep(Random.nextLong(100, 900))
        if (shouldFail) {
            // Return 500 status via response
            throw PaymentProcessingException("Payment processing failed")
        }
        return mapOf("message" to "Payment processed")
    }
    
    class PaymentProcessingException(message: String) : RuntimeException(message)

    @GetMapping("/orders/inventory/check")
    fun checkInventory(): Map<String, Any> {
        Thread.sleep(Random.nextLong(40, 350))
        return mapOf("message" to "Inventory checked", "available" to Random.nextBoolean())
    }
}

