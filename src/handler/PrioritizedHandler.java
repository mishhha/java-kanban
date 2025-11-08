package handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import taskmanager.BaseHttpHandler;
import taskmanager.HttpTaskServer;
import taskmanager.Managers;
import taskmanager.TaskManager;
import tasks.Task;

import java.io.IOException;
import java.util.List;

public class PrioritizedHandler extends BaseHttpHandler implements HttpHandler {

    TaskManager manager;

    public PrioritizedHandler(TaskManager manager) {
        this.manager = manager;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        Gson gson = HttpTaskServer.getGson();

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
