package ru.nikidzawa.golink.services.GUI.TrayIcon;

import io.github.gleidson28.GNAvatarView;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import lombok.Setter;
import ru.nikidzawa.networkAPI.network.TCPConnection;
import ru.nikidzawa.networkAPI.store.entities.UserEntity;

import java.io.ByteArrayInputStream;

public class TrayIconController {

    @FXML
    private BorderPane openGoLink;

    @FXML
    private GNAvatarView avatar;

    @FXML
    private BorderPane closeGoLink;

    @FXML
    private TextField name;

    @FXML
    private BorderPane notifications;

    @FXML
    private Text notificationText;

    @Setter
    private UserEntity userEntity;

    @Setter
    private TCPConnection tcpConnection;

    @Setter
    private GoLinkTrayIcon goLinkTrayIcon;

    private boolean notificationStatus = true;

    @FXML
    void initialize() {
        Platform.runLater(() -> {
            setBIO();
            openGoLink.setOnMouseClicked(_ -> openGoLink());
            notifications.setOnMouseClicked(_ -> setNotificationStatus());
            closeGoLink.setOnMouseClicked(_ -> closeGoLink());

            setGUI();
        });
    }

    private void setBIO () {
        avatar.setImage(new Image(new ByteArrayInputStream(userEntity.getAvatar())));
        name.setText(userEntity.getName());
    }

    private void openGoLink () {
        goLinkTrayIcon.showStage();
    }

    private void setNotificationStatus () {
        notificationStatus = !notificationStatus;
        notificationText.setText((notificationStatus ? "Выключить" : "Включить") + " уведомления");
    }

    private void closeGoLink () {
        goLinkTrayIcon.hide();
        tcpConnection.disconnect();
        Platform.exit();
    }

    private void setGUI () {
        openGoLink.setOnMouseEntered(_ -> openGoLink.setStyle("-fx-background-color: #18314D"));
        openGoLink.setOnMouseExited(_ -> openGoLink.setStyle("-fx-background-color: #001933"));

        notifications.setOnMouseEntered(_ -> notifications.setStyle("-fx-background-color: #18314D"));
        notifications.setOnMouseExited(_ -> notifications.setStyle("-fx-background-color: #001933"));

        closeGoLink.setOnMouseEntered(_ -> closeGoLink.setStyle("-fx-background-color: #18314D"));
        closeGoLink.setOnMouseExited(_ -> closeGoLink.setStyle("-fx-background-color: #001933"));
    }

}
