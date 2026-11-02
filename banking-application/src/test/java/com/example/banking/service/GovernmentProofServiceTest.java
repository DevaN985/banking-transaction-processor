package com.example.banking.service;

import com.example.banking.domain.GovernmentProof;
import com.example.banking.domain.enums.GovernmentIdType;
import com.example.banking.dto.request.SaveGovernmentProofRequest;
import com.example.banking.dto.response.BulkLoadGovernmentProofResponse;
import com.example.banking.repository.GovernmentProofRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GovernmentProofServiceTest {

    @Mock
    private GovernmentProofRepository repository;

    @InjectMocks
    private GovernmentProofService service;

    private SaveGovernmentProofRequest request;

    @BeforeEach
    void setUp() {
        request = new SaveGovernmentProofRequest();
        request.setGovernmentIdType(GovernmentIdType.PAN);
        request.setGovernmentIdNumber(" abcde1234f ");
        request.setFirstName("Devendra");
        request.setMiddleName("Kumar");
        request.setLastName("Bandi");
        request.setDateOfBirth(LocalDate.of(1990, 1, 1));
        request.setRegisteredEmail("devendra@example.com");
    }

    @Test
    void bulkLoadWithEmptyListReturnsZeroCounts() {
        BulkLoadGovernmentProofResponse response =
                service.bulkLoad(List.of());

        assertEquals(0, response.getReceived());
        assertEquals(0, response.getInserted());
        assertEquals(0, response.getSkippedDuplicates());
        assertTrue(response.getDuplicateIds().isEmpty());

        verifyNoInteractions(repository);
    }

    @Test
    void bulkLoadSavesNewProofAndNormalizesGovernmentIdNumber() {
        when(repository.findByGovernmentIdTypeAndGovernmentIdNumber(
                GovernmentIdType.PAN, "ABCDE1234F"))
                .thenReturn(Optional.empty());

        when(repository.save(any(GovernmentProof.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BulkLoadGovernmentProofResponse response =
                service.bulkLoad(List.of(request));

        assertEquals(1, response.getReceived());
        assertEquals(1, response.getInserted());
        assertEquals(0, response.getSkippedDuplicates());
        assertTrue(response.getDuplicateIds().isEmpty());

        ArgumentCaptor<GovernmentProof> captor =
                ArgumentCaptor.forClass(GovernmentProof.class);

        verify(repository).save(captor.capture());

        GovernmentProof savedProof = captor.getValue();

        assertNotNull(savedProof.getId());
        assertEquals(GovernmentIdType.PAN, savedProof.getGovernmentIdType());
        assertEquals("ABCDE1234F", savedProof.getGovernmentIdNumber());
        assertEquals("Devendra", savedProof.getFirstName());
        assertEquals("Kumar", savedProof.getMiddleName());
        assertEquals("Bandi", savedProof.getLastName());
        assertEquals(LocalDate.of(1990, 1, 1), savedProof.getDateOfBirth());
        assertEquals("devendra@example.com", savedProof.getRegisteredEmail());
        assertEquals("VERIFIED", savedProof.getValidationStatus());
        assertNotNull(savedProof.getCreatedAt());
    }

    @Test
    void bulkLoadSkipsDuplicateAndReturnsMaskedGovernmentIdNumber() {
        when(repository.findByGovernmentIdTypeAndGovernmentIdNumber(
                GovernmentIdType.PAN, "ABCDE1234F"))
                .thenReturn(Optional.of(mock(GovernmentProof.class)));

        BulkLoadGovernmentProofResponse response =
                service.bulkLoad(List.of(request));

        assertEquals(1, response.getReceived());
        assertEquals(0, response.getInserted());
        assertEquals(1, response.getSkippedDuplicates());
        assertEquals(
                List.of("PAN:******234F"),
                response.getDuplicateIds()
        );

        verify(repository, never()).save(any(GovernmentProof.class));
    }

    @Test
    void bulkLoadHandlesNewAndDuplicateProofsTogether() {
        SaveGovernmentProofRequest newRequest =
                new SaveGovernmentProofRequest();

        newRequest.setGovernmentIdType(GovernmentIdType.AADHAAR);
        newRequest.setGovernmentIdNumber(" 123456789012 ");
        newRequest.setFirstName("Test");
        newRequest.setMiddleName(null);
        newRequest.setLastName("Customer");
        newRequest.setDateOfBirth(LocalDate.of(1992, 5, 10));
        newRequest.setRegisteredEmail("test@example.com");

        when(repository.findByGovernmentIdTypeAndGovernmentIdNumber(
                GovernmentIdType.PAN, "ABCDE1234F"))
                .thenReturn(Optional.of(mock(GovernmentProof.class)));

        when(repository.findByGovernmentIdTypeAndGovernmentIdNumber(
                GovernmentIdType.AADHAAR, "123456789012"))
                .thenReturn(Optional.empty());

        when(repository.save(any(GovernmentProof.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BulkLoadGovernmentProofResponse response =
                service.bulkLoad(List.of(request, newRequest));

        assertEquals(2, response.getReceived());
        assertEquals(1, response.getInserted());
        assertEquals(1, response.getSkippedDuplicates());
        assertEquals(
                List.of("PAN:******234F"),
                response.getDuplicateIds()
        );

        verify(repository, times(1)).save(any(GovernmentProof.class));
    }

    @Test
    void bulkLoadMasksDuplicateNumberOfFourCharactersOrLess() {
        request.setGovernmentIdNumber(" ABCD ");

        when(repository.findByGovernmentIdTypeAndGovernmentIdNumber(
                GovernmentIdType.PAN, "ABCD"))
                .thenReturn(Optional.of(mock(GovernmentProof.class)));

        BulkLoadGovernmentProofResponse response =
                service.bulkLoad(List.of(request));

        assertEquals(1, response.getSkippedDuplicates());
        assertEquals(
                List.of("PAN:****"),
                response.getDuplicateIds()
        );

        verify(repository, never()).save(any(GovernmentProof.class));
    }

    @Test
    void bulkLoadTrimsAndUppercasesNumberBeforeRepositoryLookup() {
        request.setGovernmentIdNumber("  abcd1234f  ");

        when(repository.findByGovernmentIdTypeAndGovernmentIdNumber(
                GovernmentIdType.PAN, "ABCD1234F"))
                .thenReturn(Optional.empty());

        when(repository.save(any(GovernmentProof.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        service.bulkLoad(List.of(request));

        verify(repository).findByGovernmentIdTypeAndGovernmentIdNumber(
                GovernmentIdType.PAN, "ABCD1234F"
        );
    }

    @Test
    void bulkLoadUsesGovernmentIdTypeAsPartOfDuplicateLookup() {
        request.setGovernmentIdType(GovernmentIdType.PASSPORT);
        request.setGovernmentIdNumber(" p1234567 ");

        when(repository.findByGovernmentIdTypeAndGovernmentIdNumber(
                GovernmentIdType.PASSPORT, "P1234567"))
                .thenReturn(Optional.empty());

        when(repository.save(any(GovernmentProof.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BulkLoadGovernmentProofResponse response =
                service.bulkLoad(List.of(request));

        assertEquals(1, response.getInserted());

        verify(repository).findByGovernmentIdTypeAndGovernmentIdNumber(
                GovernmentIdType.PASSPORT, "P1234567"
        );
    }
}