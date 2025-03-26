package org.example.domain.validators;


import org.example.domain.Utilizator;

public class UtilizatorValidator implements Validator<Utilizator> {
    @Override
    public void validate(Utilizator entity) throws ValidationException {
        if(entity.getFirstName().isEmpty() || entity.getLastName().isEmpty() || entity.getPassword().isEmpty())
            throw new ValidationException("Utilizatorul nu este valid");
    }
}
