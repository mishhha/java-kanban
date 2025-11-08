package handler;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import taskmanager.*;
import tasks.Epic;
import tasks.SubTask;

import java.io.IOException;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

public class EpicHandler extends BaseHttpHandler {

    TaskManager manager;

    public EpicHandler(TaskManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

        Gson gson = HttpTaskServer.getGson();

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
