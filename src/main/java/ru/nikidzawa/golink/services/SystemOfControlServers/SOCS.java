package ru.nikidzawa.golink.services.SystemOfControlServers;

import ru.nikidzawa.golink.network.Server;
import ru.nikidzawa.golink.services.GoMessage.MessageBroker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class SOCS {
    public HashMap<Integer, Server> servers = new HashMap<>();

    public static void main(String[] args) {
        new Thread(MessageBroker::new).start();
        new Thread(SOCS::new).start();
    }

    public SOCS() {
        System.out.println("SOCS IS ACTIVE\n--------------");
        int PORT = 8080;
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(() -> {
                    try {
                        handleClient(clientSocket);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }).start();
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void handleClient(Socket clientSocket) throws IOException {
        BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        String[] strings = in.readLine().split(":");
        String command = strings[0];
        String value;
        String userId;
        try {
            value = strings[1];
        }catch (ArrayIndexOutOfBoundsException ex) {
            value = null;
        }
        try {
            userId = strings[2];
        }catch (ArrayIndexOutOfBoundsException ex) {
            userId = null;
        }
        switch (command) {
            case "CREATE_SERVER" :
                Server server = new Server(0);
                int port = server.getPORT();
                System.out.println("CREATE NEW SERVER ON PORT: " + port);
                servers.put(port, server);
                new Thread(server::start).start();
                out.println(port);
                break;
            case "GET_SERVERS_PORTS":
                for (Integer key : servers.keySet()) {
                    System.out.println(key);
                }
                break;
            case "RELEASE_PORT":
                int serverPort = Integer.parseInt(value);
                servers.remove(serverPort);
                System.out.println("Порт " + value + " освобождён");
                break;
            case "CHECK_USER":
                out.println(servers.get(Integer.parseInt(value)).connections.containsKey(userId));
                break;
        }
        clientSocket.close();
    }
}