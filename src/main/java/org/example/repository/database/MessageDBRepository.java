package org.example.repository.database;
import org.example.domain.Friendship;
import org.example.domain.Message;
import org.example.domain.Utilizator;
import org.example.domain.validators.Validator;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class MessageDBRepository extends AbstractDatabaseRepository<Integer, Message> {
private final UtilizatorDBRepository userRepository;


    public MessageDBRepository(Validator<Message> validator,UtilizatorDBRepository userRepository, String url, String user, String password) {
        super(validator, url, user, password);
        this.userRepository = userRepository;
    }

    @Override
    protected Message resultSetToEntity(ResultSet resultSet) throws SQLException {
        int id = resultSet.getInt("id");
        String from_id = resultSet.getString("from_user_id");
        String to_id = resultSet.getString("to_user_id");
        LocalDateTime date = resultSet.getTimestamp("date").toLocalDateTime();
        String message = resultSet.getString("message");
        int reply_id = resultSet.getInt("reply_to");


        Utilizator from = userRepository.findOne(from_id).orElse(null);
        List<Utilizator> to = Collections.singletonList(userRepository.findOne(to_id).orElse(null));
        Message msg = new Message(from, to, message, date);
        msg.setId(id);

        if (reply_id != 0) {
            Message replyMessage = findOneNoReply(reply_id).orElse(null);
            msg.setReply(replyMessage);
        }
        return msg;
    }

    @Override
    protected void entityToPreparedStatement(Message entity, PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.setString(1, entity.getFrom().getId());
        preparedStatement.setString(2, entity.getTo().get(0).getId());
        preparedStatement.setTimestamp(3, Timestamp.valueOf(entity.getDate()));
        preparedStatement.setString(4, entity.getMessage());
        if (entity.getReply() == null) {
            preparedStatement.setNull(5, Types.NULL);
        } else {
            preparedStatement.setLong(5, entity.getReply().getId());
        }
    }

    @Override
    protected String getInsertQuery() {
        return "INSERT INTO messages(from_user_id, to_user_id, date, message, reply_to) VALUES (?,?,?,?,?)";
    }

    @Override
    protected String getUpdateQuery() {
        return "UPDATE messages SET from_user_id = ?, to_user_id = ?, date = ?, message = ?, reply_to = ? WHERE id = ?";
    }

    @Override
    protected String getDeleteQuery() {
        return "DELETE FROM messages WHERE id = ?";
    }

    @Override
    protected String getSelectByIdQuery() {
        return "SELECT * FROM messages WHERE id = ?";
    }

    @Override
    protected String getTableName() {
        return "messages";
    }

    public Optional<Message> findOneNoReply(int aLong) {
        String query = "SELECT * FROM messages WHERE id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, aLong);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return Optional.of(resultSetToEntity(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.empty();
    }

   
}
