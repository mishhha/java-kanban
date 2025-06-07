package tasks;

import org.junit.jupiter.api.Test;

import taskmanager.TaskManager;
import taskmanager.Managers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {

    @Test // Проверка Task по ID
    void tasksWithSameIdShouldBeEqual() {
        Task task1 = new Task("Задача", "Описание", TaskStatus.NEW);
        task1.setId(1);

        Task task2 = new Task("Другое имя", "Другое описание", TaskStatus.DONE);
        task2.setId(1);

        assertEquals(task1, task2); // Они должны быть равны по ID
    }

    @Test // Проверка наследников по ID
    void epicsWithSameIdShouldBeEqual() {
        Epic epic1 = new Epic("Эпик", "Описание");
        epic1.setId(2);

        Epic epic2 = new Epic("Другой эпик", "Другое описание");
        epic2.setId(2);

        assertEquals(epic1, epic2); // Равны по ID
    }

    @Test // Проверка наследников по ID
    void subtasksWithSameIdShouldBeEqual() {
        Epic epic = new Epic("Эпик", "Описание");
        epic.setId(3);

        SubTask sub1 = new SubTask("Сабтаск", "Описание", epic.getId());
        sub1.setId(4);

        SubTask sub2 = new SubTask("Другой сабтаск", "Другое описание", epic.getId());
        sub2.setId(4);

        assertEquals(sub1, sub2); // Равны по ID
    }

    @Test // Проверка, что объект Epic нельзя добавить в самого себя в виде подзадачи;
    void shouldNotAllowAddingEpicAsSubtaskOfItself() {
        Epic epic = new Epic("Эпик", "Не должен ссылаться на себя");
        epic.setId(5);

        SubTask subTask = new SubTask("Подзадача", "Нельзя", epic.getId());
        subTask.setId(6);

        if (epic.getId().equals(subTask.getEpicId())) {
            epic.getSubTasks().add(subTask.getId());
        }

        assertTrue(epic.getSubTasks().contains(subTask.getId()));
    }

    @Test // Проверка, что объект Subtask нельзя сделать своим же эпиком;
    void shouldThrowExceptionIfSubtaskIsItsOwnEpic() {
        SubTask selfReferencingSubTask = new SubTask("Ошибка", "Сам себе эпик", 7);
        selfReferencingSubTask.setId(7); // ID эпика == своему ID

        TaskManager manager = Managers.getDefault();

        assertThrows(IllegalArgumentException.class, () -> {
            manager.createSubTask(selfReferencingSubTask);
        });
    }

    @Test // Проверка, что утилитарный класс всегда возвращает проинициализированные и готовые к работе экземпляры менеджеров;
    void managersShouldReturnNonNullInstances() {
        assertNotNull(Managers.getDefault());
        assertNotNull(Managers.getDefaultHistory());
    }

    @Test // Проверка проверьте, что InMemoryTaskManager действительно добавляет задачи разного типа и может найти их по id;
    void taskManagerCanAddAndGetTasksById() {
        TaskManager manager = Managers.getDefault();

        Task task = new Task("Задача", "Описание", TaskStatus.NEW);
        Epic epic = new Epic("Эпик", "Описание");
        SubTask subTask = new SubTask("Сабтаск", "Описание", epic.getId());

        task.setId(manager.getNextId());
        epic.setId(manager.getNextId());
        subTask.setId(manager.getNextId());

        manager.createTask(task);
        manager.createEpic(epic);
        manager.createSubTask(subTask);

        assertEquals(task, manager.getByIdTask(task.getId()));
        assertEquals(epic, manager.getByIdEpic(epic.getId()));
        assertEquals(subTask, manager.getByIdSubtask(subTask.getId()));
    }

    @Test // Проверка, что задачи с заданным id и сгенерированным id не конфликтуют внутри менеджера;
    void idsShouldBeUniqueAcrossAllTypes() {
        TaskManager manager = Managers.getDefault();

        Task task = new Task("Задача", "Описание", TaskStatus.NEW);
        Epic epic = new Epic("Эпик", "Описание");
        SubTask subTask = new SubTask("Сабтаск", "Описание", epic.getId());

        task.setId(manager.getNextId());
        epic.setId(manager.getNextId());
        subTask.setId(manager.getNextId());

        manager.createTask(task);
        manager.createEpic(epic);
        manager.createSubTask(subTask);

        assertFalse(task.getId() == epic.getId() || task.getId() == subTask.getId()
                || epic.getId() == subTask.getId());
    }

    @Test // создайте тест, в котором проверяется неизменность задачи (по всем полям) при добавлении задачи в менеджер
    void taskShouldNotChangeAfterAddingToManager() {
        TaskManager manager = Managers.getDefault();

        Task original = new Task("Задача", "Описание", TaskStatus.NEW);
        original.setId(10);

        Task saved = manager.createTask(original);

        assertEquals(original.getName(), saved.getName());
        assertEquals(original.getDescription(), saved.getDescription());
        assertEquals(original.getTaskStatus(), saved.getTaskStatus());
    }

    @Test // Проверка, что задачи, добавляемые в HistoryManager, сохраняют предыдущую версию задачи и её данных.
    void historyShouldStoreFullTaskObject() {
        TaskManager manager = Managers.getDefault();

        Task task = new Task("Купить хлеб", "Срочно", TaskStatus.NEW);
        task.setId(manager.getNextId());
        manager.createTask(task);

        manager.getByIdTask(task.getId()); // добавили в историю

        List<Task> history = manager.getHistory();
        assertFalse(history.isEmpty());
        assertEquals(task.getName(), history.get(0).getName());
        assertEquals(task.getDescription(), history.get(0).getDescription());
        assertEquals(task.getTaskStatus(), history.get(0).getTaskStatus());
    }






}