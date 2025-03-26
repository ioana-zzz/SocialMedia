package org.example.repository.database;

import org.example.domain.Utilizator;
import org.example.domain.validators.Validator;

import java.sql.*;
import java.time.LocalDateTime;

public class UtilizatorDBRepository extends AbstractDatabaseRepository<String, Utilizator> {

    public UtilizatorDBRepository(Validator<Utilizator> validator, String url, String user, String password) {
        super(validator, url, user, password);
    }

    @Override
    protected Utilizator resultSetToEntity(ResultSet rs) throws SQLException {
        return new Utilizator(
                rs.getString("id"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("password"),
                rs.getTimestamp("last_seen").toLocalDateTime(),
                rs.getBytes("image")
        );
    }

    @Override
    protected void entityToPreparedStatement(Utilizator entity, PreparedStatement pstmt) throws SQLException {

        int count = pstmt.getParameterMetaData().getParameterCount();
        switch (count) {
            case 5 -> {
                pstmt.setString(5, entity.getId());
                pstmt.setString(1, entity.getFirstName());
                pstmt.setString(2, entity.getLastName());
                pstmt.setString(3, entity.getPassword());
                pstmt.setTimestamp(4, Timestamp.valueOf(entity.getLastSeen()));

            }

            case 1 -> {
                pstmt.setString(1, entity.getId());
            }
        }

    }

    @Override
    protected String getTableName() {
        return "users";
    }

    @Override
    protected String getInsertQuery() {
        return "INSERT INTO users (first_name, last_name, password,last_seen,id) VALUES (?, ?, ?, ?, ?)";
    }

    @Override
    protected String getUpdateQuery() {
        return "UPDATE users SET first_name = ?, last_name = ?, password = ?, last_seen = ? WHERE id = ?";
    }

    @Override
    protected String getDeleteQuery() {
        return "DELETE FROM users WHERE id = ?";
    }

    @Override
    protected String getSelectByIdQuery() {
        return "SELECT * FROM users WHERE id = ?";
    }

    public void setLastSeen(LocalDateTime lastSeen, Utilizator user) {
          String query = "UPDATE users SET last_seen = ? WHERE id = ?";
            try (Connection connection = getConnection();
                 PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setTimestamp(1, Timestamp.valueOf(lastSeen));
                preparedStatement.setString(2,user.getId() );
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
    }
}
