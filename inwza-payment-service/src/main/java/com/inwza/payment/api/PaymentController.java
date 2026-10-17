package com.inwza.payment.api;

import com.inwza.payment.api.dto.PaymentDtos;
import com.inwza.payment.application.PaymentSagaService;
import com.inwza.payment.application.messaging.PaymentTransferMessage;
import com.inwza.payment.config.PlayerRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/payment_v1")
public class PaymentController {

    private final PaymentSagaService paymentSagaService;

    public PaymentController(PaymentSagaService paymentSagaService) {
        this.paymentSagaService = paymentSagaService;
    }

    @GetMapping("")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }

    @PostMapping("/payment/buy")
    public List<PaymentTransferMessage> placeOrder(
            HttpServletRequest request, @Valid @RequestBody PaymentDtos.ItemServiceRequest body) {
        String playerId = (String) request.getAttribute(PlayerRequestAttributes.PLAYER_ID);
        return paymentSagaService.executePurchase(playerId, body);
    }

    @PostMapping("/payment/sell")
    public List<PaymentTransferMessage> sellItem(
            HttpServletRequest request, @Valid @RequestBody PaymentDtos.ItemServiceRequest body) {
        String playerId = (String) request.getAttribute(PlayerRequestAttributes.PLAYER_ID);
        return paymentSagaService.sellItem(playerId, body);
    }
}
