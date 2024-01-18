package ru.nikidzawa.golink.FXControllers.Configurations;

import javafx.animation.PauseTransition;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import ru.nikidzawa.golink.FXControllers.GoLink;
import ru.nikidzawa.golink.FXControllers.cash.ContactCash;
import ru.nikidzawa.networkAPI.store.entities.ChatEntity;
import ru.nikidzawa.networkAPI.store.entities.PersonalChatEntity;
import ru.nikidzawa.networkAPI.store.entities.UserEntity;

import java.util.List;
import java.util.Objects;

public class SearchConfig {
    private final GoLink goLink;
    private final VBox contactsField;
    private final TextField searchPanel;

    public SearchConfig(GoLink goLink, VBox contactsField, TextField searchPanel) {
        this.searchPanel = searchPanel;
        this.contactsField = contactsField;
        this.goLink = goLink;
        searchPanel.textProperty().addListener((observable, oldValue, newValue) -> setSearchConfig(newValue, new PauseTransition(Duration.millis(1000))));
    }

    private void setSearchConfig(String newValue, PauseTransition pause) {
        pause.stop();
        pause.playFromStart();
        pause.setOnFinished(event -> {
            contactsField.getChildren().clear();
            goLink.userRepository.findFirstByNickname(newValue).ifPresent(interlocutor -> {
                BorderPane contact = goLink.GUIPatterns.newChatBuilder(interlocutor);
                contactsField.getChildren().add(contact);
                contact.setOnMouseClicked(mouseEvent -> {
                    searchPanel.clear();
                    goLink.contacts.values().stream()
                            .filter(contactCash1 -> Objects.equals(contactCash1.getInterlocutor().getId(), interlocutor.getId()))
                            .findFirst().ifPresentOrElse(existingContactCash -> {
                                goLink.openChat(existingContactCash.getChat(), existingContactCash.getPersonalChatEntity(), existingContactCash.getInterlocutor(), existingContactCash);
                                loadContactsFromCash();
                            }, () -> createNewChatRoom(interlocutor));
                });
            });
            if (searchPanel.getText().isEmpty()) loadContactsFromCash();
        });
    }

    private void createNewChatRoom(UserEntity interlocutor) {
        ChatEntity newChat = ChatEntity.builder().build();
        goLink.chatRepository.saveAndFlush(newChat);
        PersonalChatEntity myPersonalChatEntity = PersonalChatEntity.builder()
                .chat(newChat)
                .owner(goLink.userEntity)
                .interlocutor(interlocutor)
                .build();

        PersonalChatEntity participantPersonalChatEntity = PersonalChatEntity.builder()
                .chat(newChat)
                .owner(interlocutor)
                .interlocutor(goLink.userEntity)
                .build();

        goLink.personalChatRepository.saveAndFlush(myPersonalChatEntity);
        goLink.personalChatRepository.saveAndFlush(participantPersonalChatEntity);

        newChat.setPersonalChatEntities(List.of(myPersonalChatEntity, participantPersonalChatEntity));
        goLink.chatRepository.saveAndFlush(newChat);
        loadContactsFromCash();
        ContactCash newContactCash = goLink.createContact(interlocutor, newChat, myPersonalChatEntity);
        goLink.openChat(newChat, myPersonalChatEntity, interlocutor, newContactCash);
        goLink.TCPConnection.CREATE_NEW_CHAT_ROOM(interlocutor.getId(), participantPersonalChatEntity.getId());
        goLink.userEntity.getUserChats().add(myPersonalChatEntity);
    }

    private void loadContactsFromCash() {
        contactsField.getChildren().clear();
        goLink.contacts.values().forEach(contactCash -> contactsField.getChildren().add(contactCash.getGUI()));
    }
}
