package httptaskmanagertaskstest;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import taskmanager.HttpTaskServer;
import taskmanager.InMemoryTaskManager;
import taskmanager.TaskManager;
import taskmanager.TaskStatus;
import tasks.Epic;
import tasks.SubTask;
import tasks.Task;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskManagerSubTasksTest {

    TaskManager manager = new InMemoryTaskManager();
    HttpTaskServer taskServer = new HttpTaskServer(manager);
    Gson gson = HttpTaskServer.getGson();

    @BeforeEach
    public void setUp() throws IOException {
        manager.removeAllTasks();
        manager.removeAllSubTasks();
        manager.removeAllEpics();
        taskServer.startHttpServer();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stopHttpServer();
    }

    @Test
    public void shouldGetSubTasks() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Epic description");
        manager.createEpic(epic);

        SubTask subTask = new SubTask("Test 2", "Testing task 2",
            epic.getId());
        manager.createSubTask(subTask);

        HttpClient client = HttpClient.newHttpClient();

        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
            .uri(url)
            .GET()
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<SubTask> subTasksFromManager = manager.printSubtask();

        assertNotNull(subTasksFromManager, "Задачи не возвращаются");
        assertEquals(1, subTasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Test 2", subTasksFromManager.get(0).getName(), "Некорректное имя задачи");
    }

    @Test
    public void shouldGetSubTaskById() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Epic description");
        manager.createEpic(epic);

        SubTask subTask = new SubTask("Test 2", "Testing task 2",
            epic.getId());
        manager.createSubTask(subTask);

        int id = subTask.getId();

        HttpClient client = HttpClient.newHttpClient();

        URI url = URI.create("http://localhost:8080/subtasks/" + id);
        HttpRequest request = HttpRequest.newBuilder()
            .uri(url)
            .GET()
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        SubTask subTasksFromResponse = gson.fromJson(response.body(), SubTask.class);
        assertEquals("Test 2", subTasksFromResponse.getName());
        assertEquals(id, subTasksFromResponse.getId());
        assertEquals(TaskStatus.NEW, subTasksFromResponse.getTaskStatus());
    }

    @Test
    public void shouldAddSubTask() throws IOException, InterruptedException {

        Epic epic = new Epic("Epic", "Epic description");
        manager.createEpic(epic);

        SubTask subTask = new SubTask("Test 2", "Testing task 2",
            epic.getId());

        String taskJson = gson.toJson(subTask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
            .uri(url)
            .POST(HttpRequest.BodyPublishers.ofString(taskJson))
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        List<SubTask> tasksFromManager = manager.printSubtask();

        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Test 2", tasksFromManager.get(0).getName(), "Некорректное имя задачи");
    }

    @Test
    public void shouldDeleteSubTaskById() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Epic description");
        manager.createEpic(epic);

        SubTask subTask = new SubTask("Test 2", "Testing task 2",
            epic.getId());
        SubTask save = manager.createSubTask(subTask);

        assertNotNull(save.getId(), "ID не присвоено");
        int id = save.getId();

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/" + id);
        HttpRequest request = HttpRequest.newBuilder()
            .uri(url)
            .DELETE()
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertEquals("Задача удалена.", response.body().trim());

        List<SubTask> tasksFromManager = manager.printSubtask();
        assertEquals(0, tasksFromManager.size(), "Некорректное количество задач");
        List<Task> prioritized = manager.getPrioritizedTasks();
        assertFalse(prioritized.contains(save), "Задача осталась в приоритетной очереди");
    }

}