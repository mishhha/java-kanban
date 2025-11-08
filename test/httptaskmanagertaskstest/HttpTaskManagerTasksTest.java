package httptaskmanagertaskstest;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import taskmanager.HttpTaskServer;
import taskmanager.InMemoryTaskManager;
import taskmanager.TaskManager;
import taskmanager.TaskStatus;
import tasks.Task;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskManagerTasksTest {

    TaskManager manager = new InMemoryTaskManager();
    HttpTaskServer taskServer = new HttpTaskServer(manager);
    Gson gson = HttpTaskServer.getGson();

    public HttpTaskManagerTasksTest() throws IOException {
    }

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
    public void shouldGetTasks() throws IOException, InterruptedException {
        Task task = new Task("Test 2", "Testing task 2",
            TaskStatus.NEW, LocalDateTime.now(), Duration.ofMinutes(5));

        manager.createTask(task);

        HttpClient client = HttpClient.newHttpClient();

        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
            .uri(url)
            .GET()
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Task> tasksFromManager = manager.printTasks();

        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Test 2", tasksFromManager.get(0).getName(), "Некорректное имя задачи");
    }

    @Test
    public void shouldGetTaskById() throws IOException, InterruptedException {
        Task task = new Task("Test 2", "Testing task 2",
            TaskStatus.NEW, LocalDateTime.now(), Duration.ofMinutes(5));

        Task save = manager.createTask(task);
        int id = save.getId();

        HttpClient client = HttpClient.newHttpClient();

        URI url = URI.create("http://localhost:8080/tasks/" + id);
        HttpRequest request = HttpRequest.newBuilder()
            .uri(url)
            .GET()
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Task taskFromResponse = gson.fromJson(response.body(), Task.class);
        assertEquals("Test 2", taskFromResponse.getName());
        assertEquals(id, taskFromResponse.getId());
        assertEquals(TaskStatus.NEW, taskFromResponse.getTaskStatus());
    }

    @Test
    public void shouldAddTask() throws IOException, InterruptedException {

        Task task = new Task("Test 2", "Testing task 2",
            TaskStatus.NEW, LocalDateTime.now(), Duration.ofMinutes(5));
        String taskJson = gson.toJson(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
            .uri(url)
            .POST(HttpRequest.BodyPublishers.ofString(taskJson))
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        List<Task> tasksFromManager = manager.printTasks();

        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Test 2", tasksFromManager.get(0).getName(), "Некорректное имя задачи");
    }

    @Test
    public void shouldDeleteTaskById() throws IOException, InterruptedException {
        Task task = new Task("Test 2", "Testing task 2",
            TaskStatus.NEW, LocalDateTime.now(), Duration.ofMinutes(5));
        Task save = manager.createTask(task);
        assertNotNull(save.getId(), "ID не присвоено");
        int id = save.getId();

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/" + id);
        HttpRequest request = HttpRequest.newBuilder()
            .uri(url)
            .DELETE()
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertEquals("Задача удалена.", response.body().trim());

        List<Task> tasksFromManager = manager.printTasks();
        assertEquals(0, tasksFromManager.size(), "Некорректное количество задач");
        List<Task> prioritized = manager.getPrioritizedTasks();
        assertFalse(prioritized.contains(save), "Задача осталась в приоритетной очереди");
    }







}