package br.com.actionlabs.carboncalc.model;

import br.com.actionlabs.carboncalc.enums.TransportationType;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document("calculations")
public class Calculation {
    @Id
    private String id;
    private String name;
    private String email;
    private String phoneNumber;
    private String uf;
    private Double energyConsumption;
    private Double transportationDistance;
    private TransportationType transportationType;
    private Double solidWasteProduction;
    private Double recyclePercentage;
    private Double totalCarbonFootprint;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Calculation() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public Calculation(String name, String email, String phoneNumber, String uf) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.uf = uf;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}