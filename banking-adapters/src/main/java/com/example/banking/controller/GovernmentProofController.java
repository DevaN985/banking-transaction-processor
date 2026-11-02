package com.example.banking.controller;
import com.example.banking.dto.request.BulkLoadGovernmentProofRequest;
import com.example.banking.dto.response.BulkLoadGovernmentProofResponse;
import com.example.banking.service.GovernmentProofService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/v1/government-proofs")
public class GovernmentProofController  {
    private final GovernmentProofService service;
    public GovernmentProofController(GovernmentProofService service)  {
        this.service = service;
    }
    @PostMapping("/bulk-load")
    public BulkLoadGovernmentProofResponse bulkLoad(@Valid @RequestBody BulkLoadGovernmentProofRequest request)  {
        return service.bulkLoad(request.getCustomers());
    }
}
