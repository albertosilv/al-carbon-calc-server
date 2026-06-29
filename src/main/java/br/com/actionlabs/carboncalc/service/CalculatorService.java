package br.com.actionlabs.carboncalc.service;

import br.com.actionlabs.carboncalc.dto.TransportationDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CalculatorService {

    public double calculateEnergyEmission(double energyConsumption, double emissionFactor) {
        return energyConsumption * emissionFactor;
    }

    public double calculateTransportationEmission(List<TransportationDTO> transportationList,
            EmissionFactorService emissionFactorService) {
        if (transportationList == null || transportationList.isEmpty()) {
            return 0.0;
        }

        double totalEmission = 0.0;
        for (TransportationDTO transport : transportationList) {
            double factor = emissionFactorService.getTransportationEmissionFactor(transport.getType());
            totalEmission += transport.getMonthlyDistance() * factor;
        }
        return totalEmission;
    }

    public double calculateSolidWasteEmission(double solidWasteTotal,
            double recyclableFactor,
            double nonRecyclableFactor,
            double recyclePercentage) {
        if (recyclePercentage < 0 || recyclePercentage > 1.0) {
            throw new IllegalArgumentException("Recycle percentage must be between 0 and 1.0");
        }

        double recyclableAmount = solidWasteTotal * recyclePercentage;
        double nonRecyclableAmount = solidWasteTotal * (1 - recyclePercentage);

        return (recyclableAmount * recyclableFactor) + (nonRecyclableAmount * nonRecyclableFactor);
    }
}