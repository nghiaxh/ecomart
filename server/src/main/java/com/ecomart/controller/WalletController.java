package com.ecomart.controller;

import com.ecomart.dto.response.EcoWalletResponse;
import com.ecomart.service.EcoWalletService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    private final EcoWalletService walletService;

    public WalletController(EcoWalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping
    public EcoWalletResponse getMyWallet() {
        return walletService.getMyWallet();
    }
}
