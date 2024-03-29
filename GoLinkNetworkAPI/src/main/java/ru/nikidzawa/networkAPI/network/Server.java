package ru.nikidzawa.networkAPI.network;

import jakarta.annotation.PostConstruct;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.nikidzawa.networkAPI.network.helpers.ResponseStatus;
import ru.nikidzawa.networkAPI.store.entities.PersonalChatEntity;
import ru.nikidzawa.networkAPI.store.repositories.PersonalChatRepository;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

@Service
public class Server implements ServerListener {
    private final HashMap<Integer, TCPConnection> connections = new HashMap<>();
    public ServerSocket serverSocket;

    @Autowired
    PersonalChatRepository personalChatRepository;

    @PostConstruct
    public void init() {
        new Thread(this::startServer).start();
        System.out.println("Сервер запущен");
    }

    @SneakyThrows
    private void startServer() {
        serverSocket = new ServerSocket(8081);
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                DataInputStream in = new DataInputStream(socket.getInputStream());
                String userId = in.readUTF();

                new TCPConnection(socket, this, userId, in);
            } catch (IOException ex) {
                throw new RuntimeException("Завершение работы GoLink Network API...");
            }
        }
    }

    @Override
    public synchronized void onConnectionReady(TCPConnection tcpConnection) {
        connections.put(Integer.parseInt(tcpConnection.getUserId()), tcpConnection);
    }

    @Override
    public synchronized void onReceiveMessage(TCPConnection tcpConnection, String string) {
        String[] strings = string.split(":", 3);
        String command = strings[0];
        int userId = Integer.parseInt(strings[1]);
        try {
            switch (command) {
                case "DELETE", "CREATE_NEW_CHAT_ROOM", "WRITING_STATUS" -> connections.get(userId).sendMessage(command + ":" + strings[2]);
                case "CHANGE_USER_STATUS" -> tcpConnection.sendMessage(command + ":" + connections.containsKey(userId));
            }
        } catch (NullPointerException ex) {
            System.out.println("Получатель не в сети");
        }
    }

    @Override
    public synchronized void onReceiveFile(TCPConnection tcpConnection, String protocol, byte[] content) {
        String[] strings = protocol.split(":", 3);
        String command = strings[0];
        int userId = Integer.parseInt(strings[1]);
        try {
            switch (command) {
                case "POST" -> {
                    String[] strings2 = strings[2].split(":", 2);
                    try {
                        connections.get(userId).sendFile(command + ":" + strings2[1], content);
                    } catch (NullPointerException ex) {
                        sendNotificationAsync(strings2[0]).thenAccept(responseStatus -> {
                            if (responseStatus.isStatus()) System.out.println ("Пользователь не в сети, уведомление успешно отправлено");
                            else throw new RuntimeException ("Пользователь не в сети, ошибка при отправке уведомления " + responseStatus.getException());
                        });
                    }
                }
                case "EDIT" -> connections.get(userId).sendFile(command + ":" + strings[2], content);
            }
        } catch (NullPointerException ex) {System.out.println("Получатель не в сети");}
    }

    @Async
    public synchronized CompletableFuture<ResponseStatus> sendNotificationAsync (String string) {
        try {
            PersonalChatEntity personalChatEntity = personalChatRepository.findById(Long.valueOf(string)).orElseThrow();
            personalChatEntity.setNewMessagesCount(personalChatEntity.getNewMessagesCount() + 1);
            personalChatRepository.saveAndFlush(personalChatEntity);
            return CompletableFuture.completedFuture(ResponseStatus.builder().status(true).build());
        } catch (Exception exception) {
            return CompletableFuture.completedFuture(ResponseStatus.builder().status(false).exception(exception).build());
        }
    }

    @Override
    public synchronized void onDisconnect(TCPConnection tcpConnection) {
        connections.remove(Integer.parseInt(tcpConnection.getUserId()));
    }
}