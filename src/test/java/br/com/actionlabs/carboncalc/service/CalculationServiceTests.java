package br.com.actionlabs.carboncalc.service;

import br.com.actionlabs.carboncalc.dto.CarbonCalculationResultDTO;
import br.com.actionlabs.carboncalc.dto.StartCalcRequestDTO;
import br.com.actionlabs.carboncalc.dto.TransportationDTO;
import br.com.actionlabs.carboncalc.dto.UpdateCalcInfoRequestDTO;
import br.com.actionlabs.carboncalc.exception.CalculationNotFoundException;
import br.com.actionlabs.carboncalc.model.Calculation;
import br.com.actionlabs.carboncalc.enums.TransportationType;
import br.com.actionlabs.carboncalc.repository.CalculationRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CalculationService")
class CalculationServiceTest {

    @Mock
    private CalculationRepository calculationRepository;

    @Mock
    private EmissionFactorService emissionFactorService;

    @Mock
    private CalculatorService calculatorService;

    private CalculationService calculationService;

    @BeforeEach
    void setUp() {
        calculationService = new CalculationService(
            calculationRepository,
            emissionFactorService,
            calculatorService
        );
    }

    // ---------------------------------------------------------------
    // startCalculation
    // ---------------------------------------------------------------
    @Nested
    @DisplayName("startCalculation")
    class StartCalculation {

        private StartCalcRequestDTO buildValidRequest() {
            StartCalcRequestDTO request = new StartCalcRequestDTO();
            request.setName("João da Silva");
            request.setEmail("joao@example.com");
            request.setPhoneNumber("83999998888");
            request.setUf("pb");
            return request;
        }

        @Test
        @DisplayName("deve criar e salvar um novo cálculo com UF em maiúsculas")
        void shouldCreateAndSaveCalculation() {
            StartCalcRequestDTO request = buildValidRequest();

            Calculation saved = new Calculation(
                request.getName(),
                request.getEmail(),
                request.getPhoneNumber(),
                "PB"
            );
            saved.setId("generated-id-123");

            when(calculationRepository.save(any(Calculation.class))).thenReturn(saved);

            String id = calculationService.startCalculation(request);

            assertEquals("generated-id-123", id);

            ArgumentCaptor<Calculation> captor = ArgumentCaptor.forClass(Calculation.class);
            verify(calculationRepository, times(1)).save(captor.capture());

            Calculation toSave = captor.getValue();
            assertEquals("João da Silva", toSave.getName());
            assertEquals("joao@example.com", toSave.getEmail());
            assertEquals("83999998888", toSave.getPhoneNumber());
            assertEquals("PB", toSave.getUf());
        }

        @Test
        @DisplayName("deve lançar IllegalArgumentException quando o nome estiver nulo")
        void shouldThrowWhenNameIsNull() {
            StartCalcRequestDTO request = buildValidRequest();
            request.setName(null);

            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> calculationService.startCalculation(request)
            );
            assertEquals("Name is mandatory", ex.getMessage());
            verifyNoInteractions(calculationRepository);
        }

        @Test
        @DisplayName("deve lançar IllegalArgumentException quando o nome estiver em branco")
        void shouldThrowWhenNameIsBlank() {
            StartCalcRequestDTO request = buildValidRequest();
            request.setName("   ");

            assertThrows(IllegalArgumentException.class,
                () -> calculationService.startCalculation(request));
            verifyNoInteractions(calculationRepository);
        }

        @Test
        @DisplayName("deve lançar IllegalArgumentException quando o email estiver nulo")
        void shouldThrowWhenEmailIsNull() {
            StartCalcRequestDTO request = buildValidRequest();
            request.setEmail(null);

            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> calculationService.startCalculation(request)
            );
            assertEquals("Email is mandatory", ex.getMessage());
        }

        @Test
        @DisplayName("deve lançar IllegalArgumentException quando o telefone estiver nulo")
        void shouldThrowWhenPhoneIsNull() {
            StartCalcRequestDTO request = buildValidRequest();
            request.setPhoneNumber(null);

            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> calculationService.startCalculation(request)
            );
            assertEquals("Phone number is mandatory", ex.getMessage());
        }

        @Test
        @DisplayName("deve lançar IllegalArgumentException quando a UF estiver nula")
        void shouldThrowWhenUfIsNull() {
            StartCalcRequestDTO request = buildValidRequest();
            request.setUf(null);

            IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> calculationService.startCalculation(request)
            );
            assertEquals("UF is mandatory", ex.getMessage());
        }

        @Test
        @DisplayName("deve lançar IllegalArgumentException quando a UF estiver em branco")
        void shouldThrowWhenUfIsBlank() {
            StartCalcRequestDTO request = buildValidRequest();
            request.setUf("  ");

            assertThrows(IllegalArgumentException.class,
                () -> calculationService.startCalculation(request));
        }
    }

    // ---------------------------------------------------------------
    // updateCalculationInfo
    // ---------------------------------------------------------------
    @Nested
    @DisplayName("updateCalculationInfo")
    class UpdateCalculationInfo {

        private Calculation existingCalculation;

        @BeforeEach
        void setUpCalculation() {
            existingCalculation = new Calculation("Maria", "maria@example.com", "83988887777", "PB");
            existingCalculation.setId("calc-1");
        }

        @Test
        @DisplayName("deve lançar IllegalArgumentException quando o id estiver nulo")
        void shouldThrowWhenIdIsNull() {
            UpdateCalcInfoRequestDTO request = new UpdateCalcInfoRequestDTO();
            request.setId(null);

            assertThrows(IllegalArgumentException.class,
                () -> calculationService.updateCalculationInfo(request));
            verifyNoInteractions(calculationRepository);
        }

        @Test
        @DisplayName("deve lançar IllegalArgumentException quando o id estiver em branco")
        void shouldThrowWhenIdIsBlank() {
            UpdateCalcInfoRequestDTO request = new UpdateCalcInfoRequestDTO();
            request.setId("   ");

            assertThrows(IllegalArgumentException.class,
                () -> calculationService.updateCalculationInfo(request));
            verifyNoInteractions(calculationRepository);
        }

        @Test
        @DisplayName("deve lançar CalculationNotFoundException quando o cálculo não existir")
        void shouldThrowWhenCalculationNotFound() {
            UpdateCalcInfoRequestDTO request = new UpdateCalcInfoRequestDTO();
            request.setId("inexistente");

            when(calculationRepository.findById("inexistente")).thenReturn(Optional.empty());

            assertThrows(CalculationNotFoundException.class,
                () -> calculationService.updateCalculationInfo(request));

            verify(calculationRepository, never()).save(any());
        }

        @Test
        @DisplayName("deve atualizar dados, calcular o total e salvar quando houver transporte informado")
        void shouldUpdateAndSaveWithTransportation() {
            UpdateCalcInfoRequestDTO request = new UpdateCalcInfoRequestDTO();
            request.setId("calc-1");
            request.setEnergyConsumption(150);
            request.setSolidWasteTotal(20);
            request.setRecyclePercentage(0.5);

            TransportationDTO transport = new TransportationDTO();
            transport.setType(TransportationType.CAR);
            transport.setMonthlyDistance(300);
            request.setTransportation(List.of(transport));

            when(calculationRepository.findById("calc-1")).thenReturn(Optional.of(existingCalculation));

            when(emissionFactorService.getEnergyEmissionFactor("PB")).thenReturn(0.51);
            when(calculatorService.calculateEnergyEmission(150.0, 0.51)).thenReturn(76.5);

            when(emissionFactorService.getTransportationEmissionFactor(TransportationType.CAR)).thenReturn(0.19);

            when(emissionFactorService.getRecyclableSolidWasteFactor("PB")).thenReturn(0.43);
            when(emissionFactorService.getNonRecyclableSolidWasteFactor("PB")).thenReturn(0.96);
            when(calculatorService.calculateSolidWasteEmission(20.0, 0.43, 0.96, 0.5)).thenReturn(13.9);

            calculationService.updateCalculationInfo(request);

            assertEquals(150.0, existingCalculation.getEnergyConsumption());
            assertEquals(20.0, existingCalculation.getSolidWasteProduction());
            assertEquals(0.5, existingCalculation.getRecyclePercentage());
            assertEquals(TransportationType.CAR, existingCalculation.getTransportationType());
            assertEquals(300.0, existingCalculation.getTransportationDistance());
            assertNotNull(existingCalculation.getUpdatedAt());

            // total = energy(76.5) + transportation(300 * 0.19 = 57.0) + solidWaste(13.9) = 147.4
            assertEquals(147.4, existingCalculation.getTotalCarbonFootprint(), 0.0001);

            verify(calculationRepository, times(1)).save(existingCalculation);
        }

        @Test
        @DisplayName("deve limpar dados de transporte quando a lista vier vazia")
        void shouldClearTransportationWhenListIsEmpty() {
            UpdateCalcInfoRequestDTO request = new UpdateCalcInfoRequestDTO();
            request.setId("calc-1");
            request.setEnergyConsumption(0);
            request.setSolidWasteTotal(0);
            request.setRecyclePercentage(0.0);
            request.setTransportation(Collections.emptyList());

            when(calculationRepository.findById("calc-1")).thenReturn(Optional.of(existingCalculation));

            calculationService.updateCalculationInfo(request);

            assertNull(existingCalculation.getTransportationType());
            assertNull(existingCalculation.getTransportationDistance());
            verify(calculationRepository).save(existingCalculation);
        }

        @Test
        @DisplayName("deve limpar dados de transporte quando a lista vier nula")
        void shouldClearTransportationWhenListIsNull() {
            UpdateCalcInfoRequestDTO request = new UpdateCalcInfoRequestDTO();
            request.setId("calc-1");
            request.setEnergyConsumption(0);
            request.setSolidWasteTotal(0);
            request.setRecyclePercentage(0.0);
            request.setTransportation(null);

            when(calculationRepository.findById("calc-1")).thenReturn(Optional.of(existingCalculation));

            calculationService.updateCalculationInfo(request);

            assertNull(existingCalculation.getTransportationType());
            assertNull(existingCalculation.getTransportationDistance());
            verify(calculationRepository).save(existingCalculation);
        }

        @Test
        @DisplayName("deve usar apenas o primeiro item de transporte quando houver mais de um")
        void shouldUseOnlyFirstTransportationItem() {
            UpdateCalcInfoRequestDTO request = new UpdateCalcInfoRequestDTO();
            request.setId("calc-1");
            request.setEnergyConsumption(0);
            request.setSolidWasteTotal(0);
            request.setRecyclePercentage(0.0);

            TransportationDTO first = new TransportationDTO();
            first.setType(TransportationType.MOTORCYCLE);
            first.setMonthlyDistance(100);

            TransportationDTO second = new TransportationDTO();
            second.setType(TransportationType.CAR);
            second.setMonthlyDistance(500);

            request.setTransportation(List.of(first, second));

            when(calculationRepository.findById("calc-1")).thenReturn(Optional.of(existingCalculation));
            when(emissionFactorService.getTransportationEmissionFactor(TransportationType.MOTORCYCLE)).thenReturn(0.09);

            calculationService.updateCalculationInfo(request);

            assertEquals(TransportationType.MOTORCYCLE, existingCalculation.getTransportationType());
            assertEquals(100.0, existingCalculation.getTransportationDistance());

            verify(emissionFactorService, never()).getTransportationEmissionFactor(TransportationType.CAR);
        }
    }

    // ---------------------------------------------------------------
    // getResult
    // ---------------------------------------------------------------
    @Nested
    @DisplayName("getResult")
    class GetResult {

        @Test
        @DisplayName("deve lançar CalculationNotFoundException quando o cálculo não existir")
        void shouldThrowWhenNotFound() {
            when(calculationRepository.findById("inexistente")).thenReturn(Optional.empty());

            assertThrows(CalculationNotFoundException.class,
                () -> calculationService.getResult("inexistente"));
        }

        @Test
        @DisplayName("deve retornar zero em todos os campos quando não houver dados suficientes")
        void shouldReturnZeroWhenNoDataAvailable() {
            Calculation calculation = new Calculation("Ana", "ana@example.com", "83977776666", "SP");
            calculation.setId("calc-2");
            // energyConsumption, transportationDistance/type e solidWasteProduction permanecem nulos

            when(calculationRepository.findById("calc-2")).thenReturn(Optional.of(calculation));

            CarbonCalculationResultDTO result = calculationService.getResult("calc-2");

            assertEquals(0.0, result.getEnergy());
            assertEquals(0.0, result.getTransportation());
            assertEquals(0.0, result.getSolidWaste());
            assertEquals(0.0, result.getTotal());

            verifyNoInteractions(emissionFactorService);
            verifyNoInteractions(calculatorService);
        }

        @Test
        @DisplayName("deve calcular corretamente energia, transporte e resíduos sólidos e somar o total")
        void shouldCalculateFullResult() {
            Calculation calculation = new Calculation("Carlos", "carlos@example.com", "83955554444", "RJ");
            calculation.setId("calc-3");
            calculation.setEnergyConsumption(200.0);
            calculation.setSolidWasteProduction(30.0);
            calculation.setRecyclePercentage(0.6);
            calculation.setTransportationType(TransportationType.PUBLIC_TRANSPORT);
            calculation.setTransportationDistance(400.0);

            when(calculationRepository.findById("calc-3")).thenReturn(Optional.of(calculation));

            when(emissionFactorService.getEnergyEmissionFactor("RJ")).thenReturn(0.57);
            when(calculatorService.calculateEnergyEmission(200.0, 0.57)).thenReturn(114.0);

            when(emissionFactorService.getTransportationEmissionFactor(TransportationType.PUBLIC_TRANSPORT)).thenReturn(0.04);

            when(emissionFactorService.getRecyclableSolidWasteFactor("RJ")).thenReturn(0.46);
            when(emissionFactorService.getNonRecyclableSolidWasteFactor("RJ")).thenReturn(0.98);
            when(calculatorService.calculateSolidWasteEmission(30.0, 0.46, 0.98, 0.6)).thenReturn(17.8);

            CarbonCalculationResultDTO result = calculationService.getResult("calc-3");

            assertEquals(114.0, result.getEnergy());
            assertEquals(16.0, result.getTransportation(), 0.0001); // 400 * 0.04
            assertEquals(17.8, result.getSolidWaste());
            assertEquals(147.8, result.getTotal(), 0.0001);
        }

        @Test
        @DisplayName("deve usar zero como percentual de reciclagem quando ele for nulo")
        void shouldDefaultRecyclePercentageToZeroWhenNull() {
            Calculation calculation = new Calculation("Bia", "bia@example.com", "83933332222", "MG");
            calculation.setId("calc-4");
            calculation.setSolidWasteProduction(10.0);
            calculation.setRecyclePercentage(null);

            when(calculationRepository.findById("calc-4")).thenReturn(Optional.of(calculation));

            when(emissionFactorService.getRecyclableSolidWasteFactor("MG")).thenReturn(0.45);
            when(emissionFactorService.getNonRecyclableSolidWasteFactor("MG")).thenReturn(0.95);
            when(calculatorService.calculateSolidWasteEmission(10.0, 0.45, 0.95, 0.0)).thenReturn(9.5);

            CarbonCalculationResultDTO result = calculationService.getResult("calc-4");

            assertEquals(9.5, result.getSolidWaste());
            verify(calculatorService).calculateSolidWasteEmission(10.0, 0.45, 0.95, 0.0);
        }
    }
}