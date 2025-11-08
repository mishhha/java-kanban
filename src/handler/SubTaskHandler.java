package handler;

import adapter.DurationAdapter;
import adapter.LocalDateTimeAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import taskmanager.*;
import tasks.SubTask;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

public class SubTaskHandler extends BaseHttpHandler implements HttpHandler {

    TaskManager manager;

    public SubTaskHandler(TaskManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Gson gson = HttpTaskServer.getGson();

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
