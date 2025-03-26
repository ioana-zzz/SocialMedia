package org.example.UI;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.Text;
import javafx.stage.Popup;
import javafx.stage.Stage;
import org.example.domain.Friendship;
import org.example.domain.Message;
import org.example.domain.Utilizator;
import org.example.paging.Page;
import org.example.paging.Pageable;
import org.example.service.Service;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MainController {
    private final Service service;

    public MainController(Service service) {
        this.service = service;
    }

    private final int pageSize = 3;
    private int friendRequestsPage = 0;
    private int friendsPage = 0;
    ObservableList<String> friendRequests = FXCollections.observableArrayList();
    ObservableList<String> friends = FXCollections.observableArrayList();

    @FXML
    private ImageView profileImageView;

    @FXML
    private Text profileName;

    @FXML
    private Text friendCountText;

    @FXML
    private Text postCountText;

    @FXML
    private ImageView postImageView1;

    @FXML
    private ImageView postImageView2;

    @FXML
    private ListView<String> friendsListView;

    @FXML
    private ListView<String> friendRequestsListView;

    @FXML
    private TextField addFriendField;

    @FXML
    private Button addButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button acceptButton;

    @FXML
    private Button rejectButton;

    @FXML
    private Button prevfr;

    @FXML
    private Button nextfr;

    @FXML
    private Button prevf;

    @FXML
    private Button nextf;

    @FXML
    private Button logout;

    @FXML
    public void initialize() {
        loadProfileInfo();
        loadFriendsAndRequests();
    }

    private void loadProfileInfo() {
        profileName.setText(service.getCrtUser().getFirstName() + " " + service.getCrtUser().getLastName());
        friendCountText.setText(service.getFriends().size()+"");
        postCountText.setText("2");
        // Load profile picture and posts (example placeholders)
        profileImageView.setImage(new Image(new ByteArrayInputStream(service.getCrtUser().getImage())));
        postImageView1.setImage(new Image("post-image1.jpg"));
        postImageView2.setImage(new Image("post-image2.jpg"));
        sendMessageButton.setOnAction(e -> handleSendMessage());
        loadUsersWithConversations();
        //loadFriendsAndRequests();
        rejectButton.setOnAction(e -> handleRejectRequest());
        acceptButton.setOnAction(e -> handleAcceptRequest());
        deleteButton.setOnAction(e -> handleDeleteFriend());
        addButton.setOnAction(e -> handleAddFriend());
        nextfr.setOnAction(e -> loadFriendRequestsPage(1));
        prevfr.setOnAction(e -> loadFriendRequestsPage(-1));
        nextf.setOnAction(e -> loadFriendsPage(1));
        prevf.setOnAction(e -> loadFriendsPage(-1));
        logout.setOnAction(e -> handleLogout((Stage) logout.getScene().getWindow()));

    }

    private void loadFriendsAndRequests() {
        Page<Friendship> friendReqPage = service.findAllFriendRequestsOnPage(new Pageable(0, pageSize));
        friendRequests.setAll(friendReqPage.getElementsOnPage().stream().map(fr -> service.friendRequestToString(fr)).toList());
        Page<Friendship> friendsPage = service.findAllFriendsOnPage(new Pageable(0, pageSize));
        friends.setAll(friendsPage.getElementsOnPage().stream().map(fr -> service.friendshipToString(fr)).toList());
        friendRequestsListView.setItems(friendRequests);
        friendsListView.setItems(friends);
    }

    private void handleAddFriend() {
        try {
            String searchQuery = addFriendField.getText().trim();
            Optional<Utilizator> user = service.searchUserByName(searchQuery);
            if (user.isPresent()) {
                service.addFriend(user.get().getId());
                showErrorPopup( "Friend request sent to " + user.get().getFirstName() + " " + user.get().getLastName());
            } else {
                showErrorPopup( "No user found with the name " + searchQuery);
            }
        }catch (Exception e) {
            showErrorPopup( e.getMessage());
        }
    }

    @FXML
    private void handleDeleteFriend() {
        try {
            String selectedFriend = friendsListView.getSelectionModel().getSelectedItem().split("-")[0].trim();
            if (selectedFriend != null) {
                service.deleteFriend(service.searchUserByName(selectedFriend).get().getId());
                loadFriendsAndRequests();
            }
        } catch (Exception e) {
        showErrorPopup(e.getMessage());
        }
    }

    @FXML
    private void handleAcceptRequest() {
        try {
            String selectedRequest = friendRequestsListView.getSelectionModel().getSelectedItem().split("-")[0].trim();
            if (selectedRequest != null) {
                service.acceptFriend(service.searchUserByName(selectedRequest).get().getId());
                loadFriendsAndRequests();
            }
        } catch (Exception e) {
        showErrorPopup(e.getMessage());
        }
    }

    @FXML
    private void handleRejectRequest() {
        try {
            String selectedRequest = friendRequestsListView.getSelectionModel().getSelectedItem().split("-")[0].trim();
            if (selectedRequest != null) {
                service.deleteFriend(service.searchUserByName(selectedRequest).get().getId());
                loadFriendsAndRequests();
            }
        } catch (Exception e) {
           showErrorPopup(e.getMessage());
        }
    }

    private Utilizator selectedUser;

    @FXML
    private ListView<String> chatUserListView;

    @FXML
    private ListView<String> chatMessagesListView;

    @FXML
    private TextField chatMessageField;

    @FXML
    private Button sendMessageButton;


    private void loadUsersWithConversations() {
        chatUserListView.getItems().clear();
        service.getAllConversations(service.getCrtUser().getId())
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
        List<Message> messages = service.getConversation(service.getCrtUser().getId(), selectedUser.getId());
        chatMessagesListView.getItems().clear();
        messages.stream()
                .map(msg -> "[" + msg.getDate() + "] " + msg.getFrom().getFirstName() + ": " + msg.getMessage())
                .forEach(chatMessagesListView.getItems()::add);
    }

    @FXML
    private void handleSendMessage() {
        String messageText = chatMessageField.getText();
        if (!messageText.isEmpty() && selectedUser != null) {
            service.sendMessage(service.getCrtUser().getId(), List.of(selectedUser.getId()), messageText);
            chatMessagesListView.getItems().add("[" + LocalDateTime.now() + "] " + service.getCrtUser().getFirstName() + ": " + messageText);
            chatMessageField.clear();
        }
    }


    public void showErrorPopup(String errorMessage) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(errorMessage);
        alert.showAndWait();
    }


    private void loadFriendRequestsPage(int direction) {
        int newPage = friendRequestsPage + direction;
        if (newPage < 0) return;

        Pageable pageable = new Pageable(newPage, pageSize);

        var page = service.findAllFriendRequestsOnPage(pageable);
        if (!page.getElementsOnPage().iterator().hasNext() && direction > 0) return;
        if (page.getElementsOnPage().isEmpty() && direction == 0 && newPage > 0){
            loadFriendRequestsPage(-1);
            return;}

        friendRequestsPage = newPage;
        friendRequests.setAll(page.getElementsOnPage().stream().map( fr->service.friendshipToString(fr)).toList());
    }

    private void loadFriendsPage(int direction) {
        int newPage = friendsPage + direction;
        if (newPage < 0) return;

        Pageable pageable = new Pageable(newPage, pageSize);

        var page = service.findAllFriendsOnPage(pageable);
        if (!page.getElementsOnPage().iterator().hasNext() && direction > 0) return;
        if (page.getElementsOnPage().isEmpty() && direction == 0 && newPage > 0){
            loadFriendsPage(-1);
            return;}

        friendsPage = newPage;
        friends.setAll(page.getElementsOnPage().stream().map( fr->service.friendshipToString(fr)).toList());
    }

    private void handleLogout(Stage primaryStage) {
        primaryStage.close();
        service.setLastSeen();
        new IntroWindow(service).start(new Stage());
    }

}
