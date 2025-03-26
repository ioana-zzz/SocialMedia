package org.example.repository.database;

import org.example.domain.Friendship;
import org.example.domain.validators.Validator;
import org.example.paging.Page;
import org.example.paging.Pageable;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FriendshipDBRepository extends AbstractDatabaseRepository<String, Friendship> {

    public FriendshipDBRepository(Validator<Friendship> validator, String url, String user, String password) {
        super(validator, url, user, password);
    }

    @Override
    protected Friendship resultSetToEntity(ResultSet rs) throws SQLException {
        return new Friendship(
                rs.getString("id1"),
                rs.getString("id2"),
                rs.getString("status"),
                rs.getTimestamp("friendsFrom").toLocalDateTime()
        );
    }

    @Override
    protected void entityToPreparedStatement(Friendship entity, PreparedStatement pstmt) throws SQLException {
        int count = pstmt.getParameterMetaData().getParameterCount();
        switch (count) {
            case 5 -> {
                pstmt.setString(1, entity.getId());
                pstmt.setString(2, entity.getId1());
                pstmt.setString(3, entity.getId2());
                pstmt.setString(4, entity.getStatus());
                pstmt.setTimestamp(5, Timestamp.valueOf(entity.getFriendsFrom()));
            }
            case 2 -> {
                pstmt.setString(1, entity.getId1());
                pstmt.setString(2, entity.getId2());
            }

            case 4 -> {
                pstmt.setString(3, entity.getId1());
                pstmt.setString(4, entity.getId2());
                pstmt.setString(1, entity.getStatus());
                pstmt.setTimestamp(2, Timestamp.valueOf(entity.getFriendsFrom()));
            }

            case 1 -> {
                pstmt.setString(1, entity.getId());
            }
        }
    }

    @Override
    protected String getTableName() {
        return "friendships";
    }

    @Override
    protected String getInsertQuery() {
        return "INSERT INTO friendships (id, id1, id2, status, friendsFrom) VALUES (? ,?, ?, ?, ?)";
    }

    @Override
    protected String getUpdateQuery() {
        return "UPDATE friendships SET status = ?, friendsFrom = ? WHERE id1 = ? AND id2 = ?";
    }

    @Override
    protected String getDeleteQuery() {
        return "DELETE FROM friendships WHERE id1 = ? AND id2 = ?";
    }


    @Override
    protected String getSelectByIdQuery() {
        return "SELECT * FROM friendships WHERE id = ?";
    }


    public Page<Friendship> findAllFriendshipsOnPage(String userId, Pageable pageable) {
        List<Friendship> friendships = new ArrayList<>();
        String query = "SELECT * FROM friendships WHERE status = 'accepted' AND (id1 = ? OR id2 = ?)  LIMIT ? OFFSET ?";
        String countQuery = "SELECT COUNT(*) FROM friendships WHERE status = 'accepted' AND (id1 = ? OR id2 = ?)";
        int totalElements;

        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement countStmt = connection.prepareStatement(countQuery);
             PreparedStatement stmt = connection.prepareStatement(query)) {



            countStmt.setString(1, userId);
            countStmt.setString(2, userId);

            try (ResultSet rs = countStmt.executeQuery()) {
                if (rs.next()) {
                    totalElements = rs.getInt(1);
                } else {
                    totalElements = 0;
                }
            }



            stmt.setString(1, userId);
            stmt.setString(2, userId);
            stmt.setInt(3, pageable.getPageSize());
            stmt.setInt(4, pageable.getPageNumber() * pageable.getPageSize());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    friendships.add(resultSetToEntity(rs));
                }
            }


            return new Page<>(friendships, totalElements);
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving friendships by status", e);
        }
    }


    public Page<Friendship> findAllFriendRequestsOnPage(String userId, Pageable pageable) {
        List<Friendship> friendships = new ArrayList<>();
        String query = "SELECT * FROM friendships WHERE status = 'pending' AND id2 = ?  LIMIT ? OFFSET ?";
        String countQuery = "SELECT COUNT(*) FROM friendships WHERE status = 'pending' AND id2 = ?";
        int totalElements;

        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement countStmt = connection.prepareStatement(countQuery);
             PreparedStatement stmt = connection.prepareStatement(query)) {



            countStmt.setString(1, userId);


            try (ResultSet rs = countStmt.executeQuery()) {
                if (rs.next()) {
                    totalElements = rs.getInt(1);
                } else {
                    totalElements = 0;
                }
            }



            stmt.setString(1, userId);

            stmt.setInt(2, pageable.getPageSize());
            stmt.setInt(3, pageable.getPageNumber() * pageable.getPageSize());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    friendships.add(resultSetToEntity(rs));
                }
            }


            return new Page<>(friendships, totalElements);
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving friend requests by status", e);
        }
    }


}


