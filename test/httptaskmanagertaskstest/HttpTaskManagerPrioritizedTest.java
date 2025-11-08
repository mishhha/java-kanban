package httptaskmanagertaskstest;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class HttpTaskManagerPrioritizedTest {

    TaskManager manager = new InMemoryTaskManager();
    HttpTaskServer taskServer = new HttpTaskServer(manager);
    Gson gson = HttpTaskServer.getGson();

    public HttpTaskManagerPrioritizedTest() throws IOException {
    }

    static class TaskHistoryTypeToken extends TypeToken<List<Task>> {
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
    public void shouldGetPrioritizedList() throws IOException, InterruptedException {
        Task task1 = new Task("Test1", "Testing task1",
            TaskStatus.NEW, LocalDateTime.now(), Duration.ofMinutes(5));
        Task save = manager.createTask(task1);

        int id = save.getId();

        HttpClient client = HttpClient.newHttpClient();

        URI url = URI.create("http://localhost:8080/prioritized");
        HttpRequest request = HttpRequest.newBuilder()
            .uri(url)
            .GET()
            .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        List<Task> historyFromResponse = gson.fromJson(response.body(), new TaskHistoryTypeToken().getType());

        assertNotNull(historyFromResponse, "Задачи не возвращаются");
        assertEquals(id, historyFromResponse.get(0).getId(), "Некорректный ID задачи");
        assertEquals(1, historyFromResponse.size(), "Некорректное количество задач");
        assertEquals("Test1", historyFromResponse.get(0).getName(), "Некорректное имя задачи");
    }
}
