import com.sun.net.httpserver.*;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.*;
import java.util.*;

public class SimpleServer {
    static final String FILE_PATH = "messages.txt";

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);

        server.createContext("/message", new MessageHandler());
        server.createContext("/messages", new MessagesHandler());

        server.setExecutor(null);
        server.start();
        System.out.println("Server running at http://localhost:8000/");
    }

    // POST: зберігає нове повідомлення
    static class MessageHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                sendCORS(exchange);
                return;
            }

            if ("POST".equals(exchange.getRequestMethod())) {
                InputStream is = exchange.getRequestBody();
                String msg = new String(is.readAllBytes());

                // Додаємо повідомлення у файл
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
                    writer.write(msg);
                    writer.newLine();
                }

                exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                exchange.sendResponseHeaders(200, 0);
                exchange.getResponseBody().close();
            }
        }
    }

    // GET: повертає список повідомлень
    static class MessagesHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                sendCORS(exchange);
                return;
            }

            List<String> messages = new ArrayList<>();
            if (Files.exists(Paths.get(FILE_PATH))) {
                messages = Files.readAllLines(Paths.get(FILE_PATH));
            }

            String json = new StringBuilder("[\"")
                    .append(String.join("\",\"", messages))
                    .append("\"]")
                    .toString();

            byte[] response = json.getBytes();
            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.getResponseBody().close();
        }
    }

    // Обробка preflight (CORS) запитів
    static void sendCORS(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "*");
        exchange.sendResponseHeaders(204, -1); // No Content
    }
}
