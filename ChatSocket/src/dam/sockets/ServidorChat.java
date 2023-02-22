package dam.sockets;

import java.io.*;
import java.net.*;
import java.util.*;

public class ServidorChat {
    private static final int PORT = 1234;
    private static List<BufferedWriter> writers = new ArrayList<>();
    private static List<String> usernames = new ArrayList<>();
    public static ServerSocket server;
    public static Socket socket;

    public static void main(String[] args) throws IOException {
        server = new ServerSocket(PORT);
        System.out.println("Servidor iniciado en el puerto " + PORT);

        while (true) {
            socket = server.accept();
            System.out.println("Nuevo cliente conectado");

            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String nickname = reader.readLine();
            System.out.println("Nuevo cliente: " + nickname);

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            writers.add(writer);
            usernames.add(nickname);

            // Enviamos los nombres de usuario a todos los clientes conectados
            enviarUsuariosConectados();

            new Thread(new HiloServidorChat(reader, nickname)).start();
        }

    }

    public static void enviarMensaje(String message) {
        System.out.println(message);
        for (BufferedWriter writer : writers) {
            try {
                writer.write(message + "\n");
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void enviarUsuariosConectados() {
        StringBuilder messageBuilder = new StringBuilder();
        messageBuilder.append("Usuarios conectados: ");
        for (String username : usernames) {
            messageBuilder.append(username).append(", ");
        }
        String message = messageBuilder.toString();
        message = message.substring(0, message.length() - 2); // Eliminamos la última coma y espacio
        enviarMensaje(message);
    }

    private static class HiloServidorChat implements Runnable {
        private BufferedReader reader;
        private String nickname;

        public HiloServidorChat(BufferedReader reader, String nickname) {
            this.reader = reader;
            this.nickname = nickname;
        }

        @Override
        public void run() {
            enviarMensaje(nickname + " se ha conectado.");
            try {
                while (true) {
                    String message = reader.readLine();
                    if (message == null) {
                        break;
                    }
                    enviarMensaje(nickname + ": " + message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                writers.removeIf(writer -> writer.equals(reader));
                usernames.remove(nickname);
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                enviarUsuariosConectados();
                enviarMensaje(nickname + " ha abandonado el chat.");
            }
        }
    }
}
