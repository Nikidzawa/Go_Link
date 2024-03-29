package ru.nikidzawa.networkAPI.store.entities;

import jakarta.persistence.*;
import lombok.*;
import ru.nikidzawa.networkAPI.store.MessageType;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String text;

    private byte[] metadata;

    private MessageType messageType;

    private LocalDateTime date;

    private boolean hasBeenChanged = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private UserEntity sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", nullable = false)
    private ChatEntity chat;
}