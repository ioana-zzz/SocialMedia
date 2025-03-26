package org.example.domain.validators;
import org.example.domain.Friendship;

public class FriendshipValidator implements Validator<Friendship> {
    public void validate(Friendship entity) throws ValidationException {
        if(!"pending".equals(entity.getStatus()) && !"accepted".equals(entity.getStatus()))
            throw new ValidationException("Friendship is not valid");

        if(entity.getId1().equals(entity.getId2()))
            throw new ValidationException("You can't befriend yourself!");

        if(entity.getId1().isEmpty() || entity.getId2().isEmpty())
            throw new ValidationException("Friendship is not valid");
    }
}
