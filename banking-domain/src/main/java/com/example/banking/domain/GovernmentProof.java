package com.example.banking.domain;
import com.example.banking.domain.enums.GovernmentIdType;
import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
@Entity
@Table(name = "government_proofs", uniqueConstraints = @UniqueConstraint(name = "uk_gov_proof_type_number", columnNames = {"government_id_type", "government_id_number"}))
public class GovernmentProof  {
    @Id @Column(length = 36) private String id;
    @Enumerated(EnumType.STRING) @Column(name = "government_id_type", nullable = false, length = 30) private GovernmentIdType governmentIdType;
    @Column(name = "government_id_number", nullable = false, length = 30) private String governmentIdNumber;
    @Column(name = "first_name", nullable = false, length = 80) private String firstName;
    @Column(name = "middle_name", length = 80) private String middleName;
    @Column(name = "last_name", nullable = false, length = 80) private String lastName;
    @Column(name = "date_of_birth", nullable = false) private LocalDate dateOfBirth;
    @Column(name = "registered_email", length = 254) private String registeredEmail;
    @Column(nullable = false, length = 30) private String validationStatus;
    @Column(nullable = false) private Instant createdAt;
    protected GovernmentProof()  {
    }
    public GovernmentProof(GovernmentIdType type, String number, String first, String middle, String last, LocalDate dob, String registeredEmail)  {
        this.id = UUID.randomUUID().toString();
        this.governmentIdType = type;
        this.governmentIdNumber = number.trim().toUpperCase();
        this.firstName = first.trim();
        this.middleName = middle == null ? null : middle.trim();
        this.lastName = last.trim();
        this.dateOfBirth = dob;
        this.registeredEmail = registeredEmail == null ? null : registeredEmail.trim().toLowerCase();
        this.validationStatus = "VERIFIED";
        this.createdAt = Instant.now();
    }
    public String getId()  {
        return id;
    }
    public GovernmentIdType getGovernmentIdType()  {
        return governmentIdType;
    }
    public String getGovernmentIdNumber()  {
        return governmentIdNumber;
    }
    public String getFirstName()  {
        return firstName;
    }
    public String getMiddleName()  {
        return middleName;
    }
    public String getLastName()  {
        return lastName;
    }
    public LocalDate getDateOfBirth()  {
        return dateOfBirth;
    }
    public String getRegisteredEmail()  {
        return registeredEmail;
    }
    public String getValidationStatus()  {
        return validationStatus;
    }
    public Instant getCreatedAt()  {
        return createdAt;
    }
}
