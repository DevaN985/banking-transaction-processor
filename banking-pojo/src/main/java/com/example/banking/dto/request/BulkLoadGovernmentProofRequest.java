package com.example.banking.dto.request;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;
public class BulkLoadGovernmentProofRequest  {
    @NotEmpty @Size(max = 1000) private List<@Valid SaveGovernmentProofRequest> customers;
    public List<SaveGovernmentProofRequest> getCustomers()  {
        return customers;
    }
    public void setCustomers(List<SaveGovernmentProofRequest> customers)  {
        this.customers = customers;
    }
}
