package ru.nikidzawa.golink.FXControllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
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
import ru.nikidzawa.networkAPI.store.repositories.UserRepository;

import java.util.Objects;

@Controller
public class VerifyNumber {
    @Setter
    private ConfigurableApplicationContext context;

    @Setter
    private Long phone;

    @Setter
    private String password;

    @Setter
    private String code;

    @FXML
    private HBox area;

    @FXML
    private Button closeButton;

    @FXML
    private Text goBack;

    @FXML
    private Button minimizeButton;

    @FXML
    private Button scaleButton;

    @FXML
    private Pane titleBar;

    @Autowired
    GUIPatterns GUIPatterns;

    @Autowired
    UserRepository userRepository;

    @FXML
    void initialize() {
        Platform.runLater(() -> GUIPatterns.setBaseWindowTitleCommands(titleBar, minimizeButton, scaleButton, closeButton, context));

        goBack.setOnMouseClicked(_ -> goBack());
        final int maxLength = 1;

        for (int i = 0; i < 6; i++) {
            TextField textField = new TextField();
            textField.setPrefWidth(90);
            textField.setStyle("-fx-border-width:0 0 2 0; -fx-text-fill: white; -fx-background-color:  #001933; -fx-border-color: blue;");
            textField.setPrefColumnCount(1);
            textField.setAlignment(Pos.CENTER);
            textField.setFont(Font.font(20));
            int finalI = i;
            textField.textProperty().addListener((_, _, newValue) -> {
                if (newValue.length() > maxLength) {
                    textField.setText(newValue.substring(newValue.length() - 1));
                }
            });
            textField.addEventHandler(KeyEvent.KEY_RELEASED, event -> handleInput(textField, finalI, event));
            textField.addEventHandler(KeyEvent.KEY_PRESSED, event -> handleBackspace(textField, finalI, event));
            textField.addEventFilter(KeyEvent.KEY_TYPED, event -> {
                if (!event.getCharacter().matches("[0-9]")) {
                    event.consume();
                }
            });
            area.getChildren().add(textField);
        }
        Platform.runLater(() -> area.getChildren().getFirst().requestFocus());
    }

    private void handleInput(TextField currentTextField, int index, KeyEvent event) {
        String input = event.getText();
        if (input.length() == 1) {
            int nextIndex = index + 1;
            if (nextIndex < ((HBox) currentTextField.getParent()).getChildren().size()) {
                TextField nextTextField = (TextField) ((HBox) currentTextField.getParent()).getChildren().get(nextIndex);
                nextTextField.requestFocus();
            } else {
                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 0; i < 6; i++) {
                    TextField txt = (TextField) area.getChildren().get(i);
                    stringBuilder.append(txt.getText());
                }
                if (code.contentEquals(stringBuilder)) {
                    fxAvatar();
                } else {
                    exception();
                }
            }
        }
    }

    private void exception() {
        Notifications.create()
                .owner(area.getScene().getWindow())
                .position(Pos.TOP_RIGHT)
                .title("Ошибка")
                .text("Неверный код")
                .showError();
    }

    @SneakyThrows
    private void fxAvatar() {
        area.getScene().getWindow().hide();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("avatar.fxml"));
        loader.setControllerFactory(context::getBean);
        loader.load();
        SelectAvatar selectAvatar = loader.getController();
        selectAvatar.setPhone(phone);
        selectAvatar.setPassword(password);
        selectAvatar.setContext(context);
        Parent root = loader.getRoot();
        Stage stage = new Stage();
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/logo.png"))));
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setScene(new Scene(root));
        stage.show();
    }

    private void handleBackspace(TextField currentTextField, int index, KeyEvent event) {
        if (event.getCode() == KeyCode.BACK_SPACE && currentTextField.getCaretPosition() == 0) {
            int prevIndex = index - 1;
            if (prevIndex >= 0) {
                TextField prevTextField = (TextField) ((HBox) currentTextField.getParent()).getChildren().get(prevIndex);
                prevTextField.setText("");
                prevTextField.requestFocus();
            }
        }
    }

    @SneakyThrows
    private void goBack() {
        goBack.getScene().getWindow().hide();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ru/nikidzawa/goLink/register.fxml"));
        loader.setControllerFactory(context::getBean);
        Parent root = loader.load();

        Register register = loader.getController();
        register.setContext(context);

        Stage stage = new Stage();
        stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/logo.png"))));
        stage.initStyle(StageStyle.UNDECORATED);
        stage.setTitle("GoLink");
        stage.setScene(new Scene(root));
        stage.show();
    }
}
