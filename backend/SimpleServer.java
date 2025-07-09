import com.sun.net.httpserver.*;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class SimpleServer {

    private static final int PORT = 8000;
    private static final String FILE_PATH = "messages.txt";
    private static final List<String> messages = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        loadMessagesFromFile();

        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/message", new CorsWrapper(new MessageHandler()));
        server.createContext("/messages", new CorsWrapper(new MessagesHandler()));
        server.setExecutor(null);
        server.start();

        System.out.println("✅ Server running at http://127.0.0.1:" + PORT);
    }

    static class MessageHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                InputStream is = exchange.getRequestBody();
                String msg = new String(is.readAllBytes());

                messages.add(msg);
                saveMessagesToFile();

                exchange.sendResponseHeaders(200, -1);
            }
        }
    }

    static class MessagesHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String json = messages.stream()
                    .map(s -> "\"" + s.replace("\"", "\\\"") + "\"")
                    .collect(Collectors.joining(",", "[", "]"));

            byte[] response = json.getBytes();
            exchange.sendResponseHeaders(200, response.length);
            OutputStream os = exchange.getResponseBody();
            os.write(response);
            os.close();
        }
    }

    static void loadMessagesFromFile() {
        try {
            Path path = Paths.get(FILE_PATH);
            if (Files.exists(path)) {
                messages.addAll(Files.readAllLines(path));
            } else {
                Files.createFile(path);
            }
        } catch (IOException e) {
            System.err.println("Не вдалося завантажити повідомлення: " + e.getMessage());
        }
    }

    static void saveMessagesToFile() {
        try {
            Files.write(Paths.get(FILE_PATH), messages);
        } catch (IOException e) {
            System.err.println("Не вдалося зберегти повідомлення: " + e.getMessage());
        }
    }

    // ----------------- CORS SUPPORT ----------------

    static class CorsWrapper implements HttpHandler {
        private final HttpHandler next;

        CorsWrapper(HttpHandler next) {
            this.next = next;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCORS(exchange);

            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            next.handle(exchange);
        }
    }

    static void addCORS(HttpExchange exchange) {
        Headers headers = exchange.getResponseHeaders();
        headers.set("Access-Control-Allow-Origin", "http://127.0.0.1:5500");
        headers.set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        headers.set("Access-Control-Allow-Headers", "Content-Type");
        headers.set("Access-Control-Max-Age", "86400");
    }
}
