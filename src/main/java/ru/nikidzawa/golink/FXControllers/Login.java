package ru.nikidzawa.golink.FXControllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.Setter;
import lombok.SneakyThrows;
import org.controlsfx.control.Notifications;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Controller;
import ru.nikidzawa.golink.services.GUI.GUIPatterns;
import ru.nikidzawa.networkAPI.store.entities.UserEntity;
import ru.nikidzawa.networkAPI.store.repositories.UserRepository;

import java.util.Objects;
import java.util.Optional;

@Controller
public class Login {
    @Setter
    private ConfigurableApplicationContext context;

    @FXML
    private Button closeButton;

    @FXML
    private Button enter;

    @FXML
    private Button minimizeButton;

    @FXML
    private PasswordField password;

    @FXML
    private TextField nickname;

    @FXML
    private Text register;

    @FXML
    private Button scaleButton;

    @FXML
    private Pane titleBar;

    @Autowired
    UserRepository userRepository;

    @Autowired
    GUIPatterns GUIPatterns;

    @FXML
    void initialize() {
        Platform.runLater(() -> GUIPatterns.setBaseWindowTitleCommands(titleBar, minimizeButton, scaleButton, closeButton, context));
        enter.setOnAction(_ -> enterEvent());

        password.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER) enterEvent();
        });

        nickname.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ENTER)
                if (password.getText().isEmpty()) {
                    password.requestFocus();
                } else enterEvent();
        });

        register.setOnMouseClicked(_ -> fxRegister());
    }

    private void enterEvent() {
        String finalNickname = nickname.getText().toLowerCase();
        if (userRepository.existsByPhoneAndPassword(finalNickname, password.getText())) {
            Optional<UserEntity> userEntity = userRepository.findFirstByNickname(finalNickname);
            fxMenu(userEntity.orElseThrow());
        } else exception();
    }

    @SneakyThrows
    private void fxMenu(UserEntity user) {
        enter.getScene().getWindow().hide();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("menu.fxml"));
        loader.setControllerFactory(context::getBean);
        Parent root = loader.load();
        Scene scene = new Scene(root);

        GoLink goLink = loader.getController();
        goLink.setUserEntity(user);
        goLink.setScene(scene);
        goLink.setContext(context);

        Stage stage = new Stage();
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/logo.png"))));
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setTitle("GoLink");
        stage.setScene(scene);
        stage.show();
    }

    @SneakyThrows
    private void fxRegister() {
        register.getScene().getWindow().hide();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ru/nikidzawa/goLink/register.fxml"));
        loader.setControllerFactory(context::getBean);
        Parent root = loader.load();

        Register registerController = loader.getController();
        registerController.setContext(context);

        Stage stage = new Stage();
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/logo.png"))));
        stage.setTitle("GoLink");
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setScene(new Scene(root));
        stage.show();
    }

    private void exception() {
        Notifications.create()
                .owner(enter.getScene().getWindow())
                .position(Pos.TOP_RIGHT)
                .title("Ошибка")
                .text("Неверный логин или пароль, попробуйте ещё раз")
                .showError();
    }
}
