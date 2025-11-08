package taskmanager;

import adapter.*;
import handler.*;
import com.google.gson.*;
import com.sun.net.httpserver.*;

import java.net.InetSocketAddress;
import java.io.IOException;

import java.time.Duration;
import java.time.LocalDateTime;

public class HttpTaskServer {
    private final TaskManager taskManager;
    private final HttpServer httpServer;

    public HttpTaskServer(TaskManager taskManager) throws IOException {
        this.taskManager = taskManager;
        this.httpServer = HttpServer.create(new InetSocketAddress(8080), 0);
    }

    private static final Gson gson  = new GsonBuilder()
        .registerTypeAdapter(Duration.class, new DurationAdapter())
        .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
        .create();

    public void startHttpServer() {
        httpServer.createContext("/tasks", new TaskHandler(taskManager));
        httpServer.createContext("/subtasks", new SubTaskHandler(taskManager));
        httpServer.createContext("/epics", new EpicHandler(taskManager));
        httpServer.createContext("/history", new HistoryHandler(taskManager));
        httpServer.createContext("/prioritized", new PrioritizedHandler(taskManager));

        httpServer.start();
        System.out.println("Сервер запущен на порту: 8080");
    }

    public void stopHttpServer() {
        httpServer.stop(3);
    }

    public static Gson getGson() {
        return gson;
    }

}

