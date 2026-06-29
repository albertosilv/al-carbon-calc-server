package br.com.actionlabs.carboncalc.repository;

import br.com.actionlabs.carboncalc.model.EnergyEmissionFactor;
import br.com.actionlabs.carboncalc.model.SolidWasteEmissionFactor;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SolidWasteEmissionFactorRepository
        extends MongoRepository<SolidWasteEmissionFactor, String> {

    Optional<SolidWasteEmissionFactor> findByUf(String upperCase);
}
