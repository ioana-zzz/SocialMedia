package org.example.UI;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.example.service.Service;

import java.io.IOException;
import java.net.URL;

public class IntroWindow extends Application {

    private final Service service;
    private TextField usernameField;
    private PasswordField passwordField;
    private Button loginButton, signupButton;
    private TextField firstNameField, lastNameField;

    public IntroWindow(Service service) {
        this.service = service;
    }


    public IntroWindow(){
        this.service = Service.getInstance();
    }

    @Override
    public void start(Stage primaryStage) {



        usernameField = new TextField();
        passwordField = new PasswordField();
        firstNameField = new TextField();
        lastNameField = new TextField();
        loginButton = new Button("Log In");
        signupButton = new Button("Sign Up");


        VBox usernameBox = createLabeledField("Username:", usernameField);
        VBox passwordBox = createLabeledField("Password:", passwordField);


        VBox firstNameBox = createLabeledField("First Name:", firstNameField);
        VBox lastNameBox = createLabeledField("Last Name:", lastNameField);


        HBox buttonBox = new HBox(10, loginButton, signupButton);
        buttonBox.setAlignment(Pos.CENTER);


        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);
        layout.getChildren().addAll(usernameBox, passwordBox, firstNameBox, lastNameBox, buttonBox);


        loginButton.setOnAction(e -> login(primaryStage));
        signupButton.setOnAction(e -> signUp());

        Scene scene = new Scene(layout, 350, 400);
        primaryStage.setTitle("Login / Sign Up");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private VBox createLabeledField(String labelText, TextField field) {
        Label label = new Label(labelText);
        VBox box = new VBox(5, label, field);
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    private void login(Stage primaryStage) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        try {
            service.login(username, password);
            openMainWindow(primaryStage);
        } catch (Exception e) {
            showAlert("Login Failed", e.getMessage());
        }
    }

    private void signUp() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();

        try {
            service.createAccount(username, firstName, lastName, password);
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Sign up successful! You can now log in with your credentials.", ButtonType.OK);
            alert.setTitle("Account Created");
            alert.showAndWait();
        } catch (Exception e) {
            showAlert("Sign Up Failed", e.getMessage());
        }
    }

    private void openMainWindow(Stage primaryStage) {
         try {
            URL fxmlLocation = getClass().getResource("/MainUI.fxml");
            if (fxmlLocation == null) {
                System.out.println("FXML file not found!");
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainUI.fxml"));
            loader.setController(new MainController(service));
            Parent root = loader.load();

            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Main Window");
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setTitle(title);
        alert.showAndWait();
    }

    public static void main(String[] args) {

        launch(args);
    }
}
