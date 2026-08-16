package com.portfolio.exception;

/**
 * Exception métier : l'action demandée est refusée pour une raison
 * fonctionnelle (et non technique). Renvoyée à l'admin sous forme de
 * message clair avec un statut HTTP 409 (Conflict).
 */
public class BusinessRuleException extends RuntimeException {
    public BusinessRuleException(String message) {
        super(message);
    }
}
