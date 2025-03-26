package org.example.domain;

import org.example.repository.database.UtilizatorDBRepository;

import java.time.LocalDateTime;
import java.util.Objects;

public class Friendship extends Entity<String>{
    private String id1;
    private String id2;
    private String status;
    private LocalDateTime friendsFrom;
    
    public Friendship(String id1, String id2, String status, LocalDateTime friendsFrom) {
        super.setId(id1+"+"+id2);
        this.id1 = id1;
        this.id2 = id2;
        this.status = status;
        this.friendsFrom = friendsFrom;
    }

    public String getId1() {
        return id1;
    }

    public void setId1(String id1) {
        this.id1 = id1;
    }

    public String getId2() {
        return id2;
    }

    public void setId2(String id2) {
        this.id2 = id2;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getStatus() {
        return status;
    }

    public LocalDateTime getFriendsFrom() {
        return friendsFrom;
    }

    public void setFriendsFrom(LocalDateTime friendsFrom) {
        this.friendsFrom = friendsFrom;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Friendship that = (Friendship) o;
        return (Objects.equals(id1, that.id1) && Objects.equals(id2, that.id2)) || (Objects.equals(id1, that.id2) && Objects.equals(id2, that.id1)) ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id1, id2);
    }



}
