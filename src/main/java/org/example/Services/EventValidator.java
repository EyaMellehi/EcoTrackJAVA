package org.example.Services;

import org.example.Entities.Event;

/**
 * Validation metier pour Event.
 */
public class EventValidator {

    /**
     * Valide un evenement selon les regles metier.
     *
     * @param event evenement a valider
     * @return true si valide
     * @throws IllegalArgumentException si une regle est violee
     */
    public boolean validate(Event event) {
        if (event == null) {
            throw new IllegalArgumentException("Evenement invalide");
        }

        String titre = event.getTitre();
        if (titre == null || titre.trim().isEmpty()) {
            throw new IllegalArgumentException("Le titre est obligatoire");
        }
        if (titre.trim().length() < 3) {
            throw new IllegalArgumentException("Le titre doit contenir au moins 3 caracteres");
        }

        String lieu = event.getLieu();
        if (lieu == null || lieu.trim().isEmpty()) {
            throw new IllegalArgumentException("Le lieu est obligatoire");
        }

        if (event.getDateDeb() == null) {
            throw new IllegalArgumentException("La date de debut est obligatoire");
        }
        if (event.getDateFin() == null) {
            throw new IllegalArgumentException("La date de fin est obligatoire");
        }
        if (!event.getDateFin().isAfter(event.getDateDeb())) {
            throw new IllegalArgumentException("La date de fin doit etre posterieure a la date de debut");
        }

        if (event.getPointGain() < 0) {
            throw new IllegalArgumentException("Le point gagne ne peut pas etre negatif");
        }

        if (event.getCapaciteMax() < 0) {
            throw new IllegalArgumentException("La capacite maximale ne peut pas etre negative");
        }
        if (event.getCapaciteMax() == 0) {
            throw new IllegalArgumentException("La capacite maximale doit etre superieure a zero");
        }

        return true;
    }
}

