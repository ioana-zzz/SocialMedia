package org.example.domain;

import java.time.LocalDateTime;
import java.util.Objects;

public class Utilizator extends Entity<String>{
    private String firstName;
    private String lastName;
    private String password;
    private LocalDateTime lastSeen;
    private byte[] image;

    public Utilizator(String id, String firstName, String lastName, String password, byte[] image) {
        super.setId(id);
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
        this.image = image;
    }

    public Utilizator(String id, String firstName, String lastName, String password, LocalDateTime lastSeen, byte[] image) {
        super.setId(id);
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
        this.lastSeen = lastSeen;
        this.image = image;
    }


    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Override
    public String toString() {
        return "Utilizator{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", friends=" +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Utilizator)) return false;
        Utilizator that = (Utilizator) o;
        return getFirstName().equals(that.getFirstName()) &&
                getLastName().equals(that.getLastName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFirstName(), getLastName());
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public LocalDateTime getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(LocalDateTime lastSeen) {
        this.lastSeen = lastSeen;
    }

    public byte[] getImage() {
        return image;
    }
}