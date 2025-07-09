import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.*;

public class SimpleServer {
    static List<String> messages = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);

        server.createContext("/message", new MessageHandler());
        server.createContext("/messages", new MessagesHandler());

        server.setExecutor(null);
        server.start();
        System.out.println("Server running at http://localhost:8000/");
    }

    static class MessageHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                InputStream is = exchange.getRequestBody();
                String msg = new String(is.readAllBytes());
                messages.add(msg);
                exchange.sendResponseHeaders(200, 0);
                exchange.getResponseBody().close();
            }
        }
    }

    static class MessagesHandler implements HttpHandler {
        public void handle(HttpExchange exchange) throws IOException {
            String json = new StringBuilder("[\"")
                    .append(String.join("\",\"", messages))
                    .append("\"]")
                    .toString();
            byte[] response = json.getBytes();
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.getResponseBody().close();
        }
    }
}
