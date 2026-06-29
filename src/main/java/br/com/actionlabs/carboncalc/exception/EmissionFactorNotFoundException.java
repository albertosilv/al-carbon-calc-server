package br.com.actionlabs.carboncalc.exception;

public class EmissionFactorNotFoundException extends RuntimeException {
    public EmissionFactorNotFoundException(String message) {
        super(message);
    }
}
