package com.example.service;

/**
 * Levée quand un créneau chevauche une réservation existante.
 */
public class ConflitReservationException extends RuntimeException {

    public ConflitReservationException(String message) {
        super(message);
    }
}
