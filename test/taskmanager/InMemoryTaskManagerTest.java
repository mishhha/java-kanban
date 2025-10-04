package taskmanager;

import org.junit.jupiter.api.Test;
import tasks.Epic;
import tasks.SubTask;
import tasks.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class InMemoryTaskManagerTest {

    TaskManager manager = Managers.getDefault();

    // Тест на проверку пересечения интервалов.

    @Test
    void intervalsTasksShouldCross(){
        Task task1 = new Task("Задача", "Описание", TaskStatus.NEW);
        LocalDateTime localDateTimeTask1 = LocalDateTime.of(2025, 9, 4, 10, 0);
        task1.setStartTime(localDateTimeTask1);
        task1.setDuration(Duration.ofMinutes(30));
        manager.createTask(task1);

        Task task2 = new Task("Задача", "Описание", TaskStatus.NEW);
        LocalDateTime localDateTimeTask2 = LocalDateTime.of(2025, 9, 4, 10, 0);
        task2.setStartTime(localDateTimeTask2);
        task2.setDuration(Duration.ofMinutes(30));
        manager.createTask(task2);

        List<Task> tasks = manager.printTasks();

        assertEquals(1, tasks.size());
    }

    @Test
    void intervalsTasksNoShouldCross(){

        Task task1 = new Task("Задача", "Описание", TaskStatus.NEW);
        LocalDateTime localDateTimeTask1 = LocalDateTime.of(2025, 8, 4, 10, 0);
        task1.setStartTime(localDateTimeTask1);
        task1.setDuration(Duration.ofMinutes(30));
        manager.createTask(task1);

        Task task2 = new Task("Задача", "Описание", TaskStatus.NEW);
        LocalDateTime localDateTimeTask2 = LocalDateTime.of(2025,9,4,0,19);
        task2.setStartTime(localDateTimeTask2);
        task2.setDuration(Duration.ofMinutes(30));
        manager.createTask(task2);

        List<Task> tasks = manager.printTasks();

        assertEquals(2, tasks.size());
    }

    // Проверяем, что СабТаск знает свой Эпик

    @Test
    void subTaskShouldBeLinkWithEpic() {

        Epic epic1 = new Epic("Задача 1", "Описание 1");
        manager.createEpic(epic1);

        SubTask subTask1 = new SubTask("Подзадача 1", "Описание подзадачи 1", epic1.getId());
        manager.createSubTask(subTask1);

        assertEquals(epic1.getId(), subTask1.getEpicId());

        assertTrue(epic1.getSubTasks().contains(subTask1.getId()),"Эпик должен содержать ID подзадачи");

        SubTask savedSubTask = manager.getByIdSubtask(subTask1.getId());

        assertNotNull(savedSubTask, "Подзадача должна быть сохранена");
        assertEquals(epic1.getId(), savedSubTask.getEpicId());
    }

    // Проверяем корректность статуса Epic, на основании статусов SubTask

    @Test
    void epicShouldBeNewIfAllSubtasksAreNew() {
        Epic epic1 = new Epic("Задача 1", "Описание 1");
        Epic epic2 = new Epic("Задача 2", "Описание 2");
        manager.createEpic(epic1);
        manager.createEpic(epic2);
        SubTask subTask1 = new SubTask("Подзадача 1", "Описание подзадачи 1", epic1.getId());
        SubTask subTask2 = new SubTask("Подзадача 2", "Описание подзадачи 2", epic2.getId());
        manager.createSubTask(subTask1);
        manager.createSubTask(subTask2);


        assertEquals(TaskStatus.NEW, epic1.getTaskStatus());
        assertEquals(TaskStatus.NEW, epic2.getTaskStatus());
    }

    @Test // Все статусы Done
    void epicShouldBeDoneIfAllSubtasksAreDone() {
        Epic epic1 = new Epic("Задача 1", "Описание 1");
        Epic epic2 = new Epic("Задача 2", "Описание 2");

        manager.createEpic(epic1);
        manager.createEpic(epic2);

        SubTask subTask1 = new SubTask("Подзадача 1", "Описание подзадачи 1", epic1.getId());
        SubTask subTask2 = new SubTask("Подзадача 2", "Описание подзадачи 2", epic2.getId());

        manager.createSubTask(subTask1);
        manager.createSubTask(subTask2);

        subTask1.setTaskStatus(TaskStatus.DONE);
        manager.updateSubtask(subTask1);

        subTask2.setTaskStatus(TaskStatus.DONE);
        manager.updateSubtask(subTask2);

        assertEquals(TaskStatus.DONE, epic1.getTaskStatus());
        assertEquals(TaskStatus.DONE, epic2.getTaskStatus());
    }

    @Test // Все статусы IN_PROGRESS
    void epicShouldBeDoneIfAllSubtasksAreProgress() {
        Epic epic1 = new Epic("Задача 1", "Описание 1");
        Epic epic2 = new Epic("Задача 2", "Описание 2");

        manager.createEpic(epic1);
        manager.createEpic(epic2);

        SubTask subTask1 = new SubTask("Подзадача 1", "Описание подзадачи 1", epic1.getId());
        SubTask subTask2 = new SubTask("Подзадача 2", "Описание подзадачи 2", epic2.getId());

        manager.createSubTask(subTask1);
        manager.createSubTask(subTask2);

        subTask1.setTaskStatus(TaskStatus.IN_PROGRESS);
        manager.updateSubtask(subTask1);

        subTask2.setTaskStatus(TaskStatus.IN_PROGRESS);
        manager.updateSubtask(subTask2);

        assertEquals(TaskStatus.IN_PROGRESS, epic1.getTaskStatus());
        assertEquals(TaskStatus.IN_PROGRESS, epic2.getTaskStatus());
    }

    @Test // Статус Эпика IN_PROGRESS
    void epicShouldBeProgress() {
        Epic epic1 = new Epic("Задача 1", "Описание 1");

        manager.createEpic(epic1);

        SubTask subTask1 = new SubTask("Подзадача 1", "Описание подзадачи 1", epic1.getId());
        SubTask subTask2 = new SubTask("Подзадача 2", "Описание подзадачи 2", epic1.getId());

        manager.createSubTask(subTask1);
        manager.createSubTask(subTask2);

        subTask1.setTaskStatus(TaskStatus.NEW);
        manager.updateSubtask(subTask1);

        subTask2.setTaskStatus(TaskStatus.DONE);
        manager.updateSubtask(subTask2);

        assertEquals(TaskStatus.IN_PROGRESS, epic1.getTaskStatus());
    }

}
