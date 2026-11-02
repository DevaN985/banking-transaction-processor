package com.example.banking.dto.response;
import java.util.List;
public class BulkLoadGovernmentProofResponse  {
    private final int received;
    private final int inserted;
    private final int skippedDuplicates;
    private final List<String> duplicateIds;
    public BulkLoadGovernmentProofResponse(int received, int inserted, List<String> duplicateIds)  {
        this.received = received;
        this.inserted = inserted;
        this.duplicateIds = List.copyOf(duplicateIds);
        this.skippedDuplicates = duplicateIds.size();
    }
    public int getReceived()  {
        return received;
    }
    public int getInserted()  {
        return inserted;
    }
    public int getSkippedDuplicates()  {
        return skippedDuplicates;
    }
    public List<String> getDuplicateIds()  {
        return duplicateIds;
    }
}
