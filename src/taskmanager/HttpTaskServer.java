package taskmanager;


import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;

import java.net.InetSocketAddress;

import java.io.IOException;

import com.google.gson.Gson;
import tasks.Task;

public class HttpTaskServer {

    public static void main(String[] args) throws IOException {

        HttpServer httpServer = HttpServer.create(); // Создали Http сервер
        httpServer.bind(new InetSocketAddress(8080),0); // Слушаем порт
        httpServer.start();

        TaskManager manager = Managers.getDefault();

        httpServer.createContext("/tasks", new TaskHandler(manager));
        httpServer.createContext("/subtasks", new SubTaskHandler(manager));
        httpServer.createContext("/epics", new EpicHandler(manager));
        httpServer.createContext("/history", new HistoryHandler(manager));
        httpServer.createContext("/prioritized", new PrioritizedHandler(manager));

    }

    static class TaskHandler implements HttpHandler {
        TaskManager taskManager;

        public TaskHandler(TaskManager taskManager) {
            this.taskManager = taskManager;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {

        }

    }

    static class SubTaskHandler implements HttpHandler {
        TaskManager taskManager;

        public SubTaskHandler(TaskManager taskManager) {
            this.taskManager = taskManager;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {

        }

    }

    static class EpicHandler implements HttpHandler {
        TaskManager taskManager;

        public EpicHandler(TaskManager taskManager) {
            this.taskManager = taskManager;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {

        }

    }

    static class HistoryHandler implements HttpHandler {
        TaskManager taskManager;

        public HistoryHandler(TaskManager taskManager) {
            this.taskManager = taskManager;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {

        }

    }

    static class PrioritizedHandler implements HttpHandler {
        TaskManager taskManager;

        public PrioritizedHandler(TaskManager taskManager) {
            this.taskManager = taskManager;
        }
        @Override
        public void handle(HttpExchange exchange) throws IOException {

        }

    }

}