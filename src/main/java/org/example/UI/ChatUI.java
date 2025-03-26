package org.example.UI;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import org.example.domain.Message;
import org.example.domain.Utilizator;
import org.example.service.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class ChatUI {
    private final Service service;
    private Utilizator currentUser;
    private Utilizator selectedUser;

    @FXML
    private ListView<String> chatUserListView;

    @FXML
    private ListView<String> chatMessagesListView;

    @FXML
    private TextField chatMessageField;

    @FXML
    private Button sendMessageButton;

    public ChatUI(Service service) {
        this.service = service;
    }

    @FXML
    public void initialize() {
        currentUser = service.getCrtUser();
        loadUsersWithConversations();
    }

    private void loadUsersWithConversations() {
        chatUserListView.getItems().clear();
        service.getAllConversations(currentUser.getId())
                .keySet()
                .stream()
                .map(id -> service.findOne(id).map(u -> u.getFirstName() + " " + u.getLastName()).orElse("Unknown"))
                .forEach(chatUserListView.getItems()::add);

        chatUserListView.setOnMouseClicked(event -> handleUserSelection());
    }

    private void handleUserSelection() {
        String selectedUserName = chatUserListView.getSelectionModel().getSelectedItem();
        if (selectedUserName != null) {
            Optional<Utilizator> user = service.searchUserByName(selectedUserName);
            user.ifPresent(u -> {
                selectedUser = u;
                loadConversation();
            });
        }
    }

    private void loadConversation() {
        List<Message> messages = service.getConversation(currentUser.getId(), selectedUser.getId());
        chatMessagesListView.getItems().clear();
        messages.stream()
                .map(msg -> "[" + msg.getDate() + "] " + msg.getFrom().getFirstName() + ": " + msg.getMessage())
                .forEach(chatMessagesListView.getItems()::add);
    }

    @FXML
    private void handleSendMessage() {
        String messageText = chatMessageField.getText();
        if (!messageText.isEmpty() && selectedUser != null) {
            service.sendMessage(currentUser.getId(), List.of(selectedUser.getId()), messageText);
            chatMessagesListView.getItems().add("[" + LocalDateTime.now() + "] " + currentUser.getFirstName() + ": " + messageText);
            chatMessageField.clear();
        }
    }
}
