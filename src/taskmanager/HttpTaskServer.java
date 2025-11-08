package taskmanager;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.sun.net.httpserver.*;

import java.net.InetSocketAddress;

import java.io.IOException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import tasks.Epic;
import tasks.SubTask;
import tasks.Task;

import static java.nio.charset.StandardCharsets.UTF_8;

public class HttpTaskServer {
    TaskManager manager;
    HttpServer httpServer;

    public HttpTaskServer(TaskManager manager) {
        this.manager = manager;
    }

    private static final Gson gson  = new GsonBuilder()
        .registerTypeAdapter(Duration.class, new DurationAdapter())
        .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
        .create();

    public void startHttpServer() throws IOException {
        httpServer = HttpServer.create();
        httpServer.bind(new InetSocketAddress(8080), 0);
        httpServer.start();
        System.out.println("Сервер запущен на порту: 8080");


        httpServer.createContext("/tasks", new TaskHandler());
        httpServer.createContext("/subtasks", new SubTaskHandler());
        httpServer.createContext("/epics", new EpicHandler());
        httpServer.createContext("/history", new HistoryHandler());
        httpServer.createContext("/prioritized", new PrioritizedHandler());
    }

    public void stopHttpServer() {
        httpServer.stop(5);
    }

    public static Gson getGson() {
        return gson;
    }

    class TaskHandler extends BaseHttpHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {

            String method = exchange.getRequestMethod();
            String[] path = exchange.getRequestURI().getPath().split("/", -1);

            switch (method) {
                case "GET":
                    if (path[1].equals("tasks") && path.length == 2) {
                        List<Task> tasks = manager.printTasks();
                        String jsonList = gson.toJson(tasks);
                        sendText(exchange, jsonList);
                    } else if (path.length == 3 && path[1].equals("tasks") && !path[2].trim().isEmpty()) {
                        try {
                            int id = Integer.parseInt(path[2].trim());
                            Task task = manager.getByIdTask(id);
                            String taskToJson = gson.toJson(task);
                            sendText(exchange, taskToJson);
                        } catch (NumberFormatException e) {
                            badRequest(exchange);
                        } catch (NotFoundException e) {
                            sendNotFound(exchange);
                        }
                    } else {
                        badRequest(exchange);
                    }
                    break;

                case "POST":
                    try {
                        String response = new String(exchange.getRequestBody().readAllBytes(), UTF_8);
                        Task task = gson.fromJson(response, Task.class);
                        if (task.getId() == null) {
                            Task makeTask = manager.createTask(task);
                            sendResponse201(exchange);
                        } else {
                            Task newTask = manager.updateTask(task);
                            sendResponse201(exchange);
                        }
                    } catch (JsonSyntaxException e) {
                        syntaxException(exchange);
                    } catch (NotFoundException | CrossingException e) {
                        sendHasOverlaps(exchange);
                    } catch (Exception e) {
                        badRequest(exchange);
                    }
                    break;

                case "DELETE":
                        try {
                            if (path.length == 3 && path[1].equals("tasks")) {
                                int idTask = Integer.parseInt(path[2]);
                                Task task = manager.deleteTask(idTask);
                                sendText(exchange, "Задача удалена.");
                            } else {
                                badRequest(exchange);
                            }
                        } catch (NumberFormatException e) {
                            badRequest(exchange);
                        } catch (NotFoundException e) {
                            sendNotFound(exchange);
                        }
                    break;
            }

        }
    }

        public class SubTaskHandler extends BaseHttpHandler implements HttpHandler {

            @Override
            public void handle(HttpExchange exchange) throws IOException {

                String method = exchange.getRequestMethod();
                String[] path = exchange.getRequestURI().getPath().split("/", -1);

                switch (method) {
                    case "GET":
                        if (path[1].equals("subtasks") && path.length == 2) {
                            List<SubTask> subTasks = manager.printSubtask();
                            String jsonList = gson.toJson(subTasks);
                            sendText(exchange, jsonList);
                        } else if (path.length == 3 && path[1].equals("subtasks") && !path[2].trim().isEmpty()) {
                            try {
                                int id = Integer.parseInt(path[2].trim());
                                SubTask subTask = manager.getByIdSubtask(id);
                                String taskToJson = gson.toJson(subTask);
                                sendText(exchange, taskToJson);
                            } catch (NumberFormatException e) {
                                badRequest(exchange);
                            } catch (NotFoundException e) {
                                sendNotFound(exchange);
                            }
                        } else {
                            badRequest(exchange);
                        }
                        break;

                    case "POST":
                        try {
                            String response = new String(exchange.getRequestBody().readAllBytes(), UTF_8);
                            SubTask subTask = gson.fromJson(response, SubTask.class);
                            if (subTask.getId() == null) {
                                SubTask makeTask = manager.createSubTask(subTask);
                                sendResponse201(exchange);
                            } else {
                                SubTask newSubTask = manager.updateSubtask(subTask);
                                sendResponse201(exchange);
                            }
                        } catch (JsonSyntaxException e) {
                            syntaxException(exchange);
                        } catch (NotFoundException | CrossingException e) {
                            sendHasOverlaps(exchange);
                        } catch (Exception e) {
                            badRequest(exchange);
                        }
                        break;

                    case "DELETE":
                        try {
                            if (path.length == 3 && path[1].equals("subtasks")) {
                                int idSubTask = Integer.parseInt(path[2]);
                                SubTask subTask = manager.deleteSubtaskById(idSubTask);
                                sendText(exchange, "Задача удалена.");
                            } else {
                                badRequest(exchange);
                            }
                        } catch (NumberFormatException e) {
                            badRequest(exchange);
                        } catch (NotFoundException e) {
                            sendNotFound(exchange);
                        }
                        break;
                }

            }
        }

        public class EpicHandler extends BaseHttpHandler implements HttpHandler {

            @Override
            public void handle(HttpExchange exchange) throws IOException {

                String method = exchange.getRequestMethod();
                String[] path = exchange.getRequestURI().getPath().split("/", -1);

                switch (method) {
                    case "GET":
                        if (path[1].equals("epics") && path.length == 2) {
                            List<Epic> epics = manager.printEpics();
                            String jsonList = gson.toJson(epics);
                            sendText(exchange, jsonList);
                        } else if (path.length == 3 && path[1].equals("epics") && !path[2].trim().isEmpty()) {
                            try {
                                int id = Integer.parseInt(path[2].trim());
                                Epic epic = manager.getByIdEpic(id);
                                String taskToJson = gson.toJson(epic);
                                sendText(exchange, taskToJson);
                            } catch (NumberFormatException e) {
                                badRequest(exchange);
                            } catch (NotFoundException e) {
                                sendNotFound(exchange);
                            }
                        } else if (path.length == 4 && path[1].equals("epics") && !path[2].trim().isEmpty()
                        && path[3].equals("subtasks")) {
                            try {
                            int id = Integer.parseInt(path[2].trim());
                            List<SubTask> subTasks = manager.getSubTasksByEpic(id);
                            String taskToJson = gson.toJson(subTasks);
                            sendText(exchange, taskToJson);
                            } catch (NumberFormatException e) {
                                badRequest(exchange);
                            } catch (NotFoundException e) {
                                sendNotFound(exchange);
                            }
                        } else {
                            badRequest(exchange);
                        }
                        break;

                    case "POST":
                        try {
                            String response = new String(exchange.getRequestBody().readAllBytes(), UTF_8);
                            Epic epic = gson.fromJson(response, Epic.class);
                            if (epic.getId() == null) {
                                Epic makeEpic = manager.createEpic(epic);
                                sendResponse201(exchange);
                            }
                        } catch (JsonSyntaxException e) {
                            syntaxException(exchange);
                        } catch (NotFoundException | CrossingException e) {
                            sendHasOverlaps(exchange);
                        } catch (Exception e) {
                            badRequest(exchange);
                        }
                        break;

                    case "DELETE":
                        try {
                            if (path.length == 3 && path[1].equals("epics")) {
                                int idEpic = Integer.parseInt(path[2]);
                                Epic epic = manager.deleteEpic(idEpic);
                                sendText(exchange, "Задача удалена.");
                            } else {
                                badRequest(exchange);
                            }
                        } catch (NumberFormatException e) {
                            badRequest(exchange);
                        } catch (NotFoundException e) {
                            sendNotFound(exchange);
                        }
                        break;
                }

            }

        }

    public class HistoryHandler extends BaseHttpHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {

            String[] path = exchange.getRequestURI().getPath().split("/", -1);

            if (path[1].equals("history") && path.length == 2) {
                List<Task> history = manager.getHistory();
                String jsonList = gson.toJson(history);
                sendText(exchange, jsonList);
            } else {
                badRequest(exchange);
            }
        }
    }

        public class PrioritizedHandler extends BaseHttpHandler implements HttpHandler {

            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String[] path = exchange.getRequestURI().getPath().split("/", -1);

                if (path[1].equals("prioritized") && path.length == 2) {
                    List<Task> history = manager.getPrioritizedTasks();
                    String jsonList = gson.toJson(history);
                    sendText(exchange, jsonList);
                } else {
                    badRequest(exchange);
                }
            }

        }

    static class DurationAdapter extends TypeAdapter<Duration> {

        @Override
        public void write(JsonWriter jsonWriter, Duration duration) throws IOException {
            if (duration == null) {
                jsonWriter.nullValue();
            } else {
                jsonWriter.value(duration.toString());
            }
        }

        @Override
        public Duration read(JsonReader jsonReader) throws IOException {
            String readDuration = jsonReader.nextString();
            Duration duration = Duration.parse(readDuration);
            return duration;
        }

    }

    static class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {

        private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

        @Override
        public void write(JsonWriter out, LocalDateTime value) throws IOException {
            if (value == null) {
                out.nullValue();
            } else {
                out.value(FORMATTER.format(value));
            }
        }

        @Override
        public LocalDateTime read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            String dateTimeStr = in.nextString();
            return LocalDateTime.parse(dateTimeStr, FORMATTER);
        }
    }


    }

