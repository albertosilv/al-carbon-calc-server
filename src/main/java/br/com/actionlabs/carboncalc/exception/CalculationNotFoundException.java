package br.com.actionlabs.carboncalc.exception;

public class CalculationNotFoundException extends RuntimeException {
    public CalculationNotFoundException(String message) {
        super(message);
    }
}
