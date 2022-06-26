package com.ermetic.dosserver.controllers;

import com.ermetic.dosserver.services.dos_protection.IDosProtectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("DosProtection")
public class DosProtectionController {

    @Autowired
    IDosProtectionService dosProtectionService;

    @RequestMapping("ClientRequest")
    public void handleClientRequest(@RequestParam int clientId, HttpServletResponse response) {
        if (dosProtectionService.isClientReachedMaxRequest(clientId)) {
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        } else {
            response.setStatus(HttpServletResponse.SC_OK);
        }
    }
}
