package br.com.actionlabs.carboncalc.service;

import br.com.actionlabs.carboncalc.dto.CarbonCalculationResultDTO;
import br.com.actionlabs.carboncalc.dto.StartCalcRequestDTO;
import br.com.actionlabs.carboncalc.dto.UpdateCalcInfoRequestDTO;
import br.com.actionlabs.carboncalc.dto.TransportationDTO;
import br.com.actionlabs.carboncalc.model.Calculation;
import br.com.actionlabs.carboncalc.repository.CalculationRepository;
import br.com.actionlabs.carboncalc.exception.CalculationNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CalculationService {

    private final CalculationRepository calculationRepository;
    private final EmissionFactorService emissionFactorService;
    private final CalculatorService calculatorService;

    public CalculationService(CalculationRepository calculationRepository,
                              EmissionFactorService emissionFactorService,
                              CalculatorService calculatorService) {
        this.calculationRepository = calculationRepository;
        this.emissionFactorService = emissionFactorService;
        this.calculatorService = calculatorService;
    }

    @Transactional
    public String startCalculation(StartCalcRequestDTO request) {
        // Validar campos obrigatórios
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Name is mandatory");
        }
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new IllegalArgumentException("Email is mandatory");
        }
        if (request.getPhoneNumber() == null || request.getPhoneNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number is mandatory");
        }
        if (request.getUf() == null || request.getUf().trim().isEmpty()) {
            throw new IllegalArgumentException("UF is mandatory");
        }

        Calculation calculation = new Calculation(
            request.getName(),
            request.getEmail(),
            request.getPhoneNumber(),
            request.getUf().toUpperCase()
        );

        Calculation saved = calculationRepository.save(calculation);
        return saved.getId();
    }

    @Transactional
    public void updateCalculationInfo(UpdateCalcInfoRequestDTO request) {
        if (request.getId() == null || request.getId().trim().isEmpty()) {
            throw new IllegalArgumentException("Calculation ID is mandatory");
        }

        Calculation calculation = calculationRepository.findById(request.getId())
            .orElseThrow(() -> new CalculationNotFoundException(request.getId()));

        calculation.setEnergyConsumption((double) request.getEnergyConsumption());
        calculation.setSolidWasteProduction((double) request.getSolidWasteTotal());
        calculation.setRecyclePercentage(request.getRecyclePercentage());
        calculation.setUpdatedAt(LocalDateTime.now());

        if (request.getTransportation() != null && !request.getTransportation().isEmpty()) {
            TransportationDTO firstTransport = request.getTransportation().get(0);
            calculation.setTransportationType(firstTransport.getType());
            calculation.setTransportationDistance((double) firstTransport.getMonthlyDistance());
        } else {
            calculation.setTransportationType(null);
            calculation.setTransportationDistance(null);
        }

        calculateAndSaveTotal(calculation);
        calculationRepository.save(calculation);
    }

    public CarbonCalculationResultDTO getResult(String id) {
        Calculation calculation = calculationRepository.findById(id)
            .orElseThrow(() -> new CalculationNotFoundException(id));

        double energyEmission = calculateEnergyEmission(calculation);
        double transportationEmission = calculateTransportationEmission(calculation);
        double solidWasteEmission = calculateSolidWasteEmission(calculation);
        double total = energyEmission + transportationEmission + solidWasteEmission;

        CarbonCalculationResultDTO result = new CarbonCalculationResultDTO();
        result.setEnergy(energyEmission);
        result.setTransportation(transportationEmission);
        result.setSolidWaste(solidWasteEmission);
        result.setTotal(total);
        
        return result;
    }

    private void calculateAndSaveTotal(Calculation calculation) {
        double energyEmission = calculateEnergyEmission(calculation);
        double transportationEmission = calculateTransportationEmission(calculation);
        double solidWasteEmission = calculateSolidWasteEmission(calculation);
        double total = energyEmission + transportationEmission + solidWasteEmission;
        
        calculation.setTotalCarbonFootprint(total);
    }

    private double calculateEnergyEmission(Calculation calculation) {
        if (calculation.getEnergyConsumption() == null || calculation.getUf() == null) {
            return 0.0;
        }
        double factor = emissionFactorService.getEnergyEmissionFactor(calculation.getUf());
        return calculatorService.calculateEnergyEmission(calculation.getEnergyConsumption(), factor);
    }

    private double calculateTransportationEmission(Calculation calculation) {
        if (calculation.getTransportationDistance() == null || calculation.getTransportationType() == null) {
            return 0.0;
        }
        double factor = emissionFactorService.getTransportationEmissionFactor(calculation.getTransportationType());
        return calculation.getTransportationDistance() * factor;
    }

    private double calculateSolidWasteEmission(Calculation calculation) {
        if (calculation.getSolidWasteProduction() == null || calculation.getUf() == null) {
            return 0.0;
        }
        
        double recyclableFactor = emissionFactorService.getRecyclableSolidWasteFactor(calculation.getUf());
        double nonRecyclableFactor = emissionFactorService.getNonRecyclableSolidWasteFactor(calculation.getUf());
        double recyclePct = calculation.getRecyclePercentage() != null ? calculation.getRecyclePercentage() : 0.0;
        
        return calculatorService.calculateSolidWasteEmission(
            calculation.getSolidWasteProduction(),
            recyclableFactor,
            nonRecyclableFactor,
            recyclePct
        );
    }
}