package taskmanager;


import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;

import java.net.InetSocketAddress;

import java.io.IOException;

import com.google.gson.Gson;

public class HttpTaskServer {
    public static void main(String[] args) throws IOException {
        HttpServer httpServer = HttpServer.create(); // Создали Http сервер
        httpServer.bind(new InetSocketAddress(8080),0); // Слушаем порт

        httpServer.createContext("/tasks", new TaskHandler());
        httpServer.createContext("/subtasks", new SubTaskHandler());
        httpServer.createContext("/epics", new EpicHandler());
        httpServer.createContext("/history", new HistoryHandler());
        httpServer.createContext("/prioritized", new PrioritizedHandler());


    }

    static class TaskHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {

        }

    }

    static class SubTaskHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {

        }

    }

    static class EpicHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {

        }

    }

    static class HistoryHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {

        }

    }

    static class PrioritizedHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {

        }

    }


}