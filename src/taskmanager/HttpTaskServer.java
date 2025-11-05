package taskmanager;


import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;

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

    public static void main(String[] args) throws IOException {

        HttpServer httpServer = HttpServer.create(); // Создали Http сервер
        httpServer.bind(new InetSocketAddress(8080), 0); // Слушаем порт
        httpServer.start();
        System.out.println("Сервер запущен на порту 8080");

        TaskManager manager = Managers.getDefault();
        BaseHttpHandler baseHttpHandler = new BaseHttpHandler();

        Gson gson  = new GsonBuilder()
            .registerTypeAdapter(Duration.class, new DurationAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .create();

            httpServer.createContext("/tasks", new TaskHandler(manager, baseHttpHandler, gson));
            httpServer.createContext("/subtasks", new SubTaskHandler(manager, baseHttpHandler, gson));
            httpServer.createContext("/epics", new EpicHandler(manager, baseHttpHandler, gson));
            httpServer.createContext("/history", new HistoryHandler(manager, baseHttpHandler, gson));
            httpServer.createContext("/prioritized", new PrioritizedHandler(manager, baseHttpHandler, gson));

        Task task = new Task("Задача - Эта", "Описание", TaskStatus.NEW);
        manager.createTask(task);
        Task task1 = new Task("Задача1", "Описание1", TaskStatus.NEW);
        manager.createTask(task1);

        Epic epic = new Epic("Задача Эпик", "Описание Эпик");
        manager.createEpic(epic);

        SubTask subTask = new SubTask("Задача Саб", "Описание саб", epic.getId());
        manager.createSubTask(subTask);
        SubTask subTask2 = new SubTask("Задача Саб2", "Описание саб2", epic.getId());
        manager.createSubTask(subTask2);
    }


    static class TaskHandler implements HttpHandler {
        TaskManager taskManager;
        BaseHttpHandler baseHttpHandler;
        Gson gson;

        public TaskHandler(TaskManager taskManager, BaseHttpHandler baseHttpHandler, Gson gson) {
            this.taskManager = taskManager;
            this.baseHttpHandler = baseHttpHandler;
            this.gson = gson;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {

            String method = exchange.getRequestMethod();
            String[] path = exchange.getRequestURI().getPath().split("/", -1);

            switch (method) {
                case "GET":
                    if (path[1].equals("tasks") && path.length == 2) {
                        List<Task> tasks = taskManager.printTasks();
                        String jsonList = gson.toJson(tasks);
                        baseHttpHandler.sendText(exchange, jsonList);
                    } else if (path.length == 3 && path[1].equals("tasks") && !path[2].trim().isEmpty()) {
                        try {
                            int id = Integer.parseInt(path[2].trim());
                            Task task = taskManager.getByIdTask(id);
                            if (task != null) {
                                String taskToJson = gson.toJson(task);
                                baseHttpHandler.sendText(exchange, taskToJson);
                            } else {
                                baseHttpHandler.sendNotFound(exchange);
                            }
                        } catch (NumberFormatException e) {
                            baseHttpHandler.badRequest(exchange);
                        }
                    } else {
                        baseHttpHandler.badRequest(exchange);
                    }
                    break;

                case "POST":
                    try {
                        String response = new String(exchange.getRequestBody().readAllBytes(), UTF_8);
                        Task task = gson.fromJson(response, Task.class);
                        if (task.getId() == null) {
                            Task makeTask = taskManager.createTask(task);
                            if (makeTask != null) {
                                baseHttpHandler.sendResponse201(exchange);
                            } else {
                                baseHttpHandler.sendHasOverlaps(exchange);
                            }
                        } else {
                            Task newTask = taskManager.updateTask(task);
                            if (newTask != null) {
                                baseHttpHandler.sendResponse201(exchange);
                            } else {
                                baseHttpHandler.sendNotFound(exchange);
                            }
                        }
                    } catch (JsonSyntaxException e) {
                        baseHttpHandler.syntaxException(exchange);
                    } catch (ManagerSaveException e) {
                        baseHttpHandler.sendHasOverlaps(exchange);
                    } catch (Exception e) {
                        baseHttpHandler.badRequest(exchange);
                    }

                case "DELETE":
                    if (path.length == 3 && path[1].equals("tasks")) {
                        try {
                            int idTask = Integer.parseInt(path[2]);
                            Task task = taskManager.deleteTask(idTask);
                            if (task == null) {
                                baseHttpHandler.sendNotFound(exchange);
                            }
                            baseHttpHandler.sendText(exchange, "Задача удалена.");
                        } catch (NumberFormatException e) {
                            baseHttpHandler.badRequest(exchange);
                        }
                    } else {
                        baseHttpHandler.badRequest(exchange);
                    }
            }

        }
    }

        static class SubTaskHandler implements HttpHandler {
            TaskManager taskManager;
            BaseHttpHandler baseHttpHandler;
            Gson gson;

            public SubTaskHandler(TaskManager taskManager, BaseHttpHandler baseHttpHandler, Gson gson) {
                this.taskManager = taskManager;
                this.baseHttpHandler = baseHttpHandler;
                this.gson = gson;
            }

            @Override
            public void handle(HttpExchange exchange) throws IOException {

                String method = exchange.getRequestMethod();
                String[] path = exchange.getRequestURI().getPath().split("/", -1);

                switch (method) {
                    case "GET":
                        if (path[1].equals("subtasks") && path.length == 2) {
                            List<SubTask> subTasks = taskManager.printSubtask();
                            String jsonList = gson.toJson(subTasks);
                            baseHttpHandler.sendText(exchange, jsonList);
                        } else if (path.length == 3 && path[1].equals("subtasks") && !path[2].trim().isEmpty()) {
                            try {
                                int id = Integer.parseInt(path[2].trim());
                                SubTask subTask = taskManager.getByIdSubtask(id);
                                if (subTask != null) {
                                    String taskToJson = gson.toJson(subTask);
                                    baseHttpHandler.sendText(exchange, taskToJson);
                                } else {
                                    baseHttpHandler.sendNotFound(exchange);
                                }
                            } catch (NumberFormatException e) {
                                baseHttpHandler.badRequest(exchange);
                            }
                        } else {
                            baseHttpHandler.badRequest(exchange);
                        }
                        break;

                    case "POST":
                        try {
                            String response = new String(exchange.getRequestBody().readAllBytes(), UTF_8);
                            SubTask subTask = gson.fromJson(response, SubTask.class);
                            if (subTask.getId() == null) {
                                SubTask makeSubTask = taskManager.createSubTask(subTask);
                                if (makeSubTask != null) {
                                    baseHttpHandler.sendResponse201(exchange);
                                } else {
                                    baseHttpHandler.sendHasOverlaps(exchange);
                                }
                            } else {
                                SubTask newSubTask = taskManager.updateSubtask(subTask);
                                if (newSubTask != null) {
                                    baseHttpHandler.sendResponse201(exchange);
                                } else {
                                    baseHttpHandler.sendNotFound(exchange);
                                }
                            }
                        } catch (JsonSyntaxException e) {
                            baseHttpHandler.syntaxException(exchange);
                        } catch (ManagerSaveException e) {
                            baseHttpHandler.sendHasOverlaps(exchange);
                        } catch (Exception e) {
                            baseHttpHandler.badRequest(exchange);
                        }

                    case "DELETE":
                        if (path.length == 3 && path[1].equals("subtasks")) {
                            try {
                                int idTask = Integer.parseInt(path[2]);
                                SubTask subTask = taskManager.deleteSubtaskById(idTask);
                                if (subTask == null) {
                                    baseHttpHandler.sendNotFound(exchange);
                                }
                                baseHttpHandler.sendText(exchange, "Подзадача удалена.");
                            } catch (NumberFormatException e) {
                                baseHttpHandler.badRequest(exchange);
                            }
                        } else {
                            baseHttpHandler.badRequest(exchange);
                        }
                }

            }
        }

        static class EpicHandler implements HttpHandler {
            TaskManager taskManager;
            BaseHttpHandler baseHttpHandler;
            Gson gson;

            public EpicHandler(TaskManager taskManager, BaseHttpHandler baseHttpHandler, Gson gson) {
                this.taskManager = taskManager;
                this.baseHttpHandler = baseHttpHandler;
                this.gson = gson;
            }

            @Override
            public void handle(HttpExchange exchange) throws IOException {

                String method = exchange.getRequestMethod();
                String[] path = exchange.getRequestURI().getPath().split("/", -1);

                switch (method) {
                    case "GET":
                        if (path[1].equals("epics") && path.length == 2) {
                            List<Epic> epics = taskManager.printEpics();
                            String jsonList = gson.toJson(epics);
                            baseHttpHandler.sendText(exchange, jsonList);
                        } else if (path.length == 3 && path[1].equals("epics") && !path[2].trim().isEmpty()) {
                            try {
                                int id = Integer.parseInt(path[2].trim());
                                Epic epic = taskManager.getByIdEpic(id);
                                if (epic != null) {
                                    String taskToJson = gson.toJson(epic);
                                    baseHttpHandler.sendText(exchange, taskToJson);
                                } else {
                                    baseHttpHandler.sendNotFound(exchange);
                                }
                            } catch (NumberFormatException e) {
                                baseHttpHandler.badRequest(exchange);
                            }
                        } else if (path.length == 4 && path[1].equals("epics") && !path[2].trim().isEmpty()
                            && path[3].equals("subtasks")) {
                            try {
                                int id = Integer.parseInt(path[2].trim());
                                List<SubTask> subTasks = taskManager.getSubTasksByEpic(id);
                                if (subTasks.isEmpty()) {
                                    baseHttpHandler.sendNotFound(exchange);
                                }
                                String jsonList = gson.toJson(subTasks);
                                baseHttpHandler.sendText(exchange, jsonList);
                            } catch (NumberFormatException e) {
                                baseHttpHandler.badRequest(exchange);
                            }
                        } else {
                            baseHttpHandler.badRequest(exchange);
                        }
                        break;

                    case "POST":
                        try {
                            String response = new String(exchange.getRequestBody().readAllBytes(), UTF_8);
                            Epic epic = gson.fromJson(response, Epic.class);
                            if (epic.getId() == null) {
                                Epic makeEpic = taskManager.createEpic(epic);
                                if (makeEpic != null) {
                                    baseHttpHandler.sendResponse201(exchange);
                                }
                            }
                        } catch (JsonSyntaxException e) {
                            baseHttpHandler.syntaxException(exchange);
                        } catch (ManagerSaveException e) {
                            baseHttpHandler.sendHasOverlaps(exchange);
                        } catch (Exception e) {
                            baseHttpHandler.badRequest(exchange);
                        }

                    case "DELETE":
                        try {
                            if (path.length == 3 && path[1].equals("epics") && !path[2].trim().isEmpty()) {
                                int idEpic = Integer.parseInt(path[2]);
                                Epic epic = taskManager.deleteEpic(idEpic);
                                if (epic != null) {
                                    baseHttpHandler.sendText(exchange, "Эпик и его подзадачи удалены.");
                                }
                                baseHttpHandler.sendNotFound(exchange);
                            } else {
                                baseHttpHandler.badRequest(exchange);
                            }
                        } catch (NumberFormatException e) {
                            baseHttpHandler.badRequest(exchange);
                        }
                }


            }

        }

    static class HistoryHandler implements HttpHandler {
        TaskManager taskManager;
        BaseHttpHandler baseHttpHandler;
        Gson gson;

        public HistoryHandler(TaskManager taskManager, BaseHttpHandler baseHttpHandler, Gson gson) {
            this.taskManager = taskManager;
            this.baseHttpHandler = baseHttpHandler;
            this.gson = gson;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {

            String[] path = exchange.getRequestURI().getPath().split("/", -1);

            if (path[1].equals("history") && path.length == 2) {
                List<Task> history = taskManager.getHistory();
                String jsonList = gson.toJson(history);
                baseHttpHandler.sendText(exchange, jsonList);
            } else {
                baseHttpHandler.badRequest(exchange);
            }
        }
    }

        static class PrioritizedHandler implements HttpHandler {
            TaskManager taskManager;
            BaseHttpHandler baseHttpHandler;
            Gson gson;

            public PrioritizedHandler(TaskManager taskManager, BaseHttpHandler baseHttpHandler, Gson gson) {
                this.taskManager = taskManager;
                this.baseHttpHandler = baseHttpHandler;
                this.gson = gson;
            }

            @Override
            public void handle(HttpExchange exchange) throws IOException {
                String[] path = exchange.getRequestURI().getPath().split("/", -1);

                if (path[1].equals("prioritized") && path.length == 2) {
                    List<Task> history = taskManager.getPrioritizedTasks();
                    String jsonList = gson.toJson(history);
                    baseHttpHandler.sendText(exchange, jsonList);
                } else {
                    baseHttpHandler.badRequest(exchange);
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

