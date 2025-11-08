package handler;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import taskmanager.*;
import tasks.Task;

import java.io.IOException;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;

public class TaskHandler extends BaseHttpHandler implements HttpHandler {

    TaskManager manager;

    public TaskHandler(TaskManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Gson gson = HttpTaskServer.getGson();

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
