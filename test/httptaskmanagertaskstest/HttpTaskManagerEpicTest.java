package httptaskmanagertaskstest;

import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import taskmanager.*;
import adapter.DurationAdapter;
import adapter.LocalDateTimeAdapter;
import handler.*;
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

public class HttpTaskManagerEpicTest {
    TaskManager manager = Managers.getDefault();
    HttpTaskServer taskServer = new HttpTaskServer(manager);

    Gson gson = HttpTaskServer.getGson();

    public HttpTaskManagerEpicTest() throws IOException {
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
    public void shouldGetEpics() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Epic description");
        manager.createEpic(epic);

        HttpClient client = HttpClient.newHttpClient();

        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
            .uri(url)
            .GET()
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Epic> epicsFromManager = manager.printEpics();

        assertNotNull(epicsFromManager, "Задачи не возвращаются");
        assertEquals(1, epicsFromManager.size(), "Некорректное количество задач");
        assertEquals("Epic", epicsFromManager.get(0).getName(), "Некорректное имя задачи");
    }

    @Test
    public void shouldGetEpicById() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Epic description");
        manager.createEpic(epic);

        int id = epic.getId();

        HttpClient client = HttpClient.newHttpClient();

        URI url = URI.create("http://localhost:8080/epics/" + id);
        HttpRequest request = HttpRequest.newBuilder()
            .uri(url)
            .GET()
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Epic epicFromResponse = gson.fromJson(response.body(), Epic.class);
        assertEquals("Epic", epicFromResponse.getName());
        assertEquals(id, epicFromResponse.getId());
        assertEquals(TaskStatus.NEW, epicFromResponse.getTaskStatus());
    }

    @Test
    public void shouldGetSubTasksEpicById() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Epic description");
        manager.createEpic(epic);

        SubTask subTask = new SubTask("Test 2", "Testing task 2",
            epic.getId());
        manager.createSubTask(subTask);

        int id = epic.getId();

        HttpClient client = HttpClient.newHttpClient();

        URI url = URI.create("http://localhost:8080/epics/" + id + "/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
            .uri(url)
            .GET()
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<SubTask> subTaskByEpicFromResponse = manager.printSubtask();
        assertEquals(1, subTaskByEpicFromResponse.size(), "Некорректное количество задач");
        assertEquals("Test 2", subTaskByEpicFromResponse.get(0).getName(), "Некорректное имя задачи");
    }

    @Test
    public void shouldAddEpic() throws IOException, InterruptedException {

        Epic epic = new Epic("Epic", "Epic description");

        String taskJson = gson.toJson(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
            .uri(url)
            .POST(HttpRequest.BodyPublishers.ofString(taskJson))
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        List<Epic> epicsFromManager = manager.printEpics();

        assertNotNull(epicsFromManager, "Задачи не возвращаются");
        assertEquals(1, epicsFromManager.size(), "Некорректное количество задач");
        assertEquals("Epic", epicsFromManager.get(0).getName(), "Некорректное имя задачи");
    }

    @Test
    public void shouldDeleteEpicById() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Epic description");
        Epic save = manager.createEpic(epic);

        assertNotNull(save.getId(), "ID не присвоено");
        int id = save.getId();

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/" + id);
        HttpRequest request = HttpRequest.newBuilder()
            .uri(url)
            .DELETE()
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertEquals("Задача удалена.", response.body().trim());

        List<Epic> epicsFromManager = manager.printEpics();
        assertEquals(0, epicsFromManager.size(), "Некорректное количество задач");
        List<Task> prioritized = manager.getPrioritizedTasks();
        assertFalse(prioritized.contains(save), "Задача осталась в приоритетной очереди");
    }

}