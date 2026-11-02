package com.example.banking.service;

import com.example.banking.domain.GovernmentProof;
import com.example.banking.dto.request.SaveGovernmentProofRequest;
import com.example.banking.dto.response.GovernmentProofResponse;
import com.example.banking.repository.GovernmentProofRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.banking.dto.response.BulkLoadGovernmentProofResponse;

import java.util.ArrayList;
import java.util.List;

@Service
public class GovernmentProofService {
    private final GovernmentProofRepository repository;

    public GovernmentProofService(GovernmentProofRepository repository) {
        this.repository = repository;
    }


    @Transactional
    public BulkLoadGovernmentProofResponse bulkLoad(List<SaveGovernmentProofRequest> requests) {
        int inserted = 0;
        List<String> duplicates = new ArrayList<>();
        for (SaveGovernmentProofRequest request : requests) {
            String normalizedNumber = request.getGovernmentIdNumber().trim().toUpperCase();
            boolean exists = repository.findByGovernmentIdTypeAndGovernmentIdNumber(
                    request.getGovernmentIdType(), normalizedNumber).isPresent();
            if (exists) {
                duplicates.add(request.getGovernmentIdType() + ":" + mask(normalizedNumber));
                continue;
            }
            GovernmentProof proof = new GovernmentProof(request.getGovernmentIdType(), normalizedNumber,
                    request.getFirstName(), request.getMiddleName(), request.getLastName(), request.getDateOfBirth(), request.getRegisteredEmail());
            repository.save(proof);
            inserted++;
        }
        return new BulkLoadGovernmentProofResponse(requests.size(), inserted, duplicates);
    }

    private String mask(String number) {
        return number.length() <= 4 ? "****" : "*".repeat(number.length() - 4) + number.substring(number.length() - 4);
    }
}
