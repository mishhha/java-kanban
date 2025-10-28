package taskmanager;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;

import java.io.IOException;

public class HttpTaskServer {
    public static void main(String[] args) throws IOException {
        HttpServer httpServer = HttpServer.create(); // Создали Http сервер
        httpServer.bind(new InetSocketAddress(8080),0); // Слушаем порт

    }
}