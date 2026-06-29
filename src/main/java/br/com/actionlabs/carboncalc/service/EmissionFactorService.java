package br.com.actionlabs.carboncalc.service;

import br.com.actionlabs.carboncalc.enums.TransportationType;
import br.com.actionlabs.carboncalc.exception.EmissionFactorNotFoundException;
import br.com.actionlabs.carboncalc.model.EnergyEmissionFactor;
import br.com.actionlabs.carboncalc.model.SolidWasteEmissionFactor;
import br.com.actionlabs.carboncalc.model.TransportationEmissionFactor;
import br.com.actionlabs.carboncalc.repository.EnergyEmissionFactorRepository;
import br.com.actionlabs.carboncalc.repository.SolidWasteEmissionFactorRepository;
import br.com.actionlabs.carboncalc.repository.TransportationEmissionFactorRepository;
import org.springframework.stereotype.Service;

@Service
public class EmissionFactorService {

    private final EnergyEmissionFactorRepository energyRepository;
    private final TransportationEmissionFactorRepository transportationRepository;
    private final SolidWasteEmissionFactorRepository solidWasteRepository;

    public EmissionFactorService(EnergyEmissionFactorRepository energyRepository,
            TransportationEmissionFactorRepository transportationRepository,
            SolidWasteEmissionFactorRepository solidWasteRepository) {
        this.energyRepository = energyRepository;
        this.transportationRepository = transportationRepository;
        this.solidWasteRepository = solidWasteRepository;
    }

    public double getEnergyEmissionFactor(String uf) {
        return energyRepository.findByUf(uf)
                .map(EnergyEmissionFactor::getFactor)
                .orElseThrow(() -> {
                    return new EmissionFactorNotFoundException(
                            "Energy emission factor not found for UF: " + uf);
                });
    }

    public double getTransportationEmissionFactor(TransportationType type) {
        return transportationRepository.findByType(type)
                .map(TransportationEmissionFactor::getFactor)
                .orElseThrow(() -> {
                    return new EmissionFactorNotFoundException(
                            "Transportation emission factor not found for type: " + type);
                });
    }

    public SolidWasteEmissionFactor getSolidWasteEmissionFactor(String uf) {
        return solidWasteRepository.findByUf(uf.toUpperCase().trim())
                .orElseThrow(() -> new EmissionFactorNotFoundException(
                        "Solid waste emission factor not found for UF: " + uf));
    }

    public double getRecyclableSolidWasteFactor(String uf) {
        return getSolidWasteEmissionFactor(uf).getRecyclableFactor();
    }

    public double getNonRecyclableSolidWasteFactor(String uf) {
        return getSolidWasteEmissionFactor(uf).getNonRecyclableFactor();
    }
}