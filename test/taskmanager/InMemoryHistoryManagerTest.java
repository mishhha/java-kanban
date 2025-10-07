package taskmanager;

import org.junit.jupiter.api.Test;
import tasks.Task;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryHistoryManagerTest {

    TaskManager managers = Managers.getDefault();

    @Test // Пустая история на старте
    void shouldHaveEmptyHistoryAtStart() {
        List<Task> history = managers.getHistory();
        assertTrue(history.isEmpty());
    }

    @Test // История меняется и становится пустой после применения метода удаления задачи.
    void historyShouldBeIsEmpty() {
        Task task1 = new Task("Задача 1", "Описание 1", TaskStatus.NEW);
        managers.createTask(task1);
        managers.getByIdTask(task1.getId());

        List<Task> history = managers.getHistory();
        assertEquals(1, history.size());

        managers.deleteTask(task1.getId());

        history = managers.getHistory();
        assertEquals(0, history.size());
    }

    @Test // Тест проверки на содержание дубликатов
    void historyNoShouldHaveDuplicate() {
        Task task1 = new Task("Задача 1", "Описание 1", TaskStatus.NEW);
        managers.createTask(task1);

        managers.getByIdTask(task1.getId());
        managers.getByIdTask(task1.getId());
        managers.getByIdTask(task1.getId());

        List<Task> history = managers.getHistory();
        assertEquals(1, history.size());
    }

    @Test // Тест на проверку удаления из начала истории
    void shouldDeleteFromBeginOfHistory() {
        Task task1 = new Task("Задача 1", "Описание 1", TaskStatus.NEW);
        Task task2 = new Task("Задача 2", "Описание 2", TaskStatus.NEW);
        Task task3 = new Task("Задача 3", "Описание 3", TaskStatus.NEW);

        managers.createTask(task1);
        managers.createTask(task2);
        managers.createTask(task3);

        managers.getByIdTask(task1.getId());
        managers.getByIdTask(task2.getId());
        managers.getByIdTask(task3.getId());

        List<Task> history = managers.getHistory();
        assertEquals(3, history.size());

        managers.deleteTask(task1.getId());
        history = managers.getHistory();

        Task task = history.getFirst();
        assertEquals(task2, task);
    }

    @Test // Тест на проверку удаления из конца истории
    void shouldDeleteFromEndOfHistory() {
        Task task1 = new Task("Задача 1", "Описание 1", TaskStatus.NEW);
        Task task2 = new Task("Задача 2", "Описание 2", TaskStatus.NEW);
        Task task3 = new Task("Задача 3", "Описание 3", TaskStatus.NEW);

        managers.createTask(task1);
        managers.createTask(task2);
        managers.createTask(task3);

        managers.getByIdTask(task1.getId());
        managers.getByIdTask(task2.getId());
        managers.getByIdTask(task3.getId());

        List<Task> history = managers.getHistory();
        assertEquals(3, history.size());

        managers.deleteTask(task3.getId());
        history = managers.getHistory();

        Task task = history.getLast();
        assertEquals(task2, task);
    }

    @Test // Тест на проверку удаления из конца истории
    void shouldDeleteFromMiddleOfHistory() {
        Task task1 = new Task("Задача 1", "Описание 1", TaskStatus.NEW);
        Task task2 = new Task("Задача 2", "Описание 2", TaskStatus.NEW);
        Task task3 = new Task("Задача 3", "Описание 3", TaskStatus.NEW);

        managers.createTask(task1);
        managers.createTask(task2);
        managers.createTask(task3);

        managers.getByIdTask(task1.getId());
        managers.getByIdTask(task2.getId());
        managers.getByIdTask(task3.getId());

        List<Task> history = managers.getHistory();
        assertEquals(3, history.size());

        managers.deleteTask(task2.getId());
        history = managers.getHistory();

        Task task = history.getLast();
        assertEquals(task3, task);
    }

}
