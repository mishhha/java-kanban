package tasks;

import org.junit.jupiter.api.Test;

import taskmanager.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;



import static org.junit.jupiter.api.Assertions.*;

class EpicTest {

    TaskManager manager = Managers.getDefault();


    // Тесты статусов SubTask

    @Test // Все статусы NEW
    void epicShouldBeNewIfAllSubtasksAreNew() {
        Epic epic1 = new Epic("Задача 1", "Описание 1");
        Epic epic2 = new Epic("Задача 2", "Описание 2");
        manager.createEpic(epic1);
        manager.createEpic(epic2);
        SubTask subTask1 = new SubTask("Подзадача 1", "Описание подзадачи 1", epic1.getId());
        SubTask subTask2 = new SubTask("Подзадача 2", "Описание подзадачи 2", epic2.getId());
        manager.createSubTask(subTask1);
        manager.createSubTask(subTask2);


        assertEquals(TaskStatus.NEW, subTask1.getTaskStatus());
        assertEquals(TaskStatus.NEW, subTask2.getTaskStatus());
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

        assertEquals(TaskStatus.DONE, subTask1.getTaskStatus());
        assertEquals(TaskStatus.DONE, subTask2.getTaskStatus());
    }

    @Test // Статус NEW и Done
    void epicShouldBeProgressIfAllSubtasksAreNewAndDone() {
        Epic epic1 = new Epic("Задача 1", "Описание 1");

        manager.createEpic(epic1);

        SubTask subTask1 = new SubTask("Подзадача 1", "Описание подзадачи 1", epic1.getId());
        SubTask subTask2 = new SubTask("Подзадача 2", "Описание подзадачи 2", epic1.getId());

        manager.createSubTask(subTask1);
        manager.createSubTask(subTask2);

        subTask2.setTaskStatus(TaskStatus.DONE);
        manager.updateSubtask(subTask2);

        assertEquals(TaskStatus.NEW, subTask1.getTaskStatus());
        assertEquals(TaskStatus.DONE, subTask2.getTaskStatus());
        assertEquals(TaskStatus.IN_PROGRESS, epic1.getTaskStatus());
    }

    @Test // Статусы IN_PROGRESS
    void epicShouldBeProgressIfAllSubtasksAreProgress() {
        Epic epic1 = new Epic("Задача 1", "Описание 1");

        manager.createEpic(epic1);

        SubTask subTask1 = new SubTask("Подзадача 1", "Описание подзадачи 1", epic1.getId());
        SubTask subTask2 = new SubTask("Подзадача 2", "Описание подзадачи 2", epic1.getId());

        manager.createSubTask(subTask1);
        manager.createSubTask(subTask2);

        subTask1.setTaskStatus(TaskStatus.IN_PROGRESS);
        manager.updateSubtask(subTask1);

        subTask2.setTaskStatus(TaskStatus.IN_PROGRESS);
        manager.updateSubtask(subTask2);

        assertEquals(TaskStatus.IN_PROGRESS, subTask1.getTaskStatus());
        assertEquals(TaskStatus.IN_PROGRESS, subTask2.getTaskStatus());
        assertEquals(TaskStatus.IN_PROGRESS, epic1.getTaskStatus());
    }


    // Тесты для prioritizedTask

    @Test
    void shouldSortTasksByStartTime() {
        // Дано: задачи с разным временем
        Task task1 = new Task("A", "desc", TaskStatus.NEW);
        task1.setStartTime(LocalDateTime.of(2025, 1, 1, 10, 0));
        task1.setDuration(Duration.ofHours(1));

        Task task2 = new Task("B", "desc", TaskStatus.NEW);
        task2.setStartTime(LocalDateTime.of(2025, 1, 1, 9, 0));
        task2.setDuration(Duration.ofHours(1));

        Task task3 = new Task("C", "desc", TaskStatus.NEW);
        task3.setStartTime(LocalDateTime.of(2025, 1, 1, 11, 0));
        task3.setDuration(Duration.ofHours(1));

        // Когда: создаём задачи
        manager.createTask(task1);
        manager.createTask(task2);
        manager.createTask(task3);

        // Тогда: порядок должен быть по времени: B → A → C
        List<Task> prioritized = manager.getPrioritizedTasks();
        assertEquals(3, prioritized.size());
        assertEquals("B", prioritized.get(0).getName());
        assertEquals("A", prioritized.get(1).getName());
        assertEquals("C", prioritized.get(2).getName());
    }

    @Test
    void shouldNotLoseTasksWithSameStartTime() {
        Task task1 = new Task("A", "desc", TaskStatus.NEW);
        task1.setStartTime(LocalDateTime.of(2025, 1, 1, 10, 0));
        task1.setId(1);

        Task task2 = new Task("B", "desc", TaskStatus.NEW);
        task2.setStartTime(LocalDateTime.of(2025, 1, 1, 10, 0));
        task2.setId(2);

        manager.createTask(task1);
        manager.createTask(task2);

        List<Task> prioritized = manager.getPrioritizedTasks();
        assertEquals(2, prioritized.size());
        // Порядок определяется id
        assertEquals(1, prioritized.get(0).getId());
        assertEquals(2, prioritized.get(1).getId());
    }

    @Test
    void shouldPutNullStartTimeTasksAtEnd() {
        Task task1 = new Task("A", "desc", TaskStatus.NEW);
        task1.setStartTime(LocalDateTime.of(2025, 1, 1, 10, 0));

        Task task2 = new Task("B", "desc", TaskStatus.NEW); // startTime == null

        manager.createTask(task1);
        manager.createTask(task2);

        List<Task> prioritized = manager.getPrioritizedTasks();
        assertEquals("A", prioritized.get(0).getName()); // с временем — в начале
        assertEquals("B", prioritized.get(1).getName()); // без времени — в конце
    }

    @Test
    void shouldRepositionTaskAfterUpdate() {
        Task task1 = new Task("A", "desc", TaskStatus.NEW);
        task1.setStartTime(LocalDateTime.of(2025, 1, 1, 10, 0));
        manager.createTask(task1);

        Task task2 = new Task("B", "desc", TaskStatus.NEW);
        task2.setStartTime(LocalDateTime.of(2025, 1, 1, 12, 0));
        manager.createTask(task2);

        // Меняем startTime у task1 на более позднее время
        task1.setStartTime(LocalDateTime.of(2025, 1, 1, 13, 0));
        manager.updateTask(task1);

        List<Task> prioritized = manager.getPrioritizedTasks();
        assertEquals("B", prioritized.get(0).getName());
        assertEquals("A", prioritized.get(1).getName());
    }

    @Test
    void shouldRemoveTaskFromPrioritizedOnDelete() {
        Task task = new Task("A", "desc", TaskStatus.NEW);
        task.setStartTime(LocalDateTime.of(2025, 1, 1, 10, 0));
        manager.createTask(task);

        manager.deleteTask(task.getId());

        List<Task> prioritized = manager.getPrioritizedTasks();
        assertTrue(prioritized.isEmpty());
    }

    @Test
    void shouldIncludeSubTaskInPrioritized() {
        Epic epic = new Epic("Epic", "desc");
        manager.createEpic(epic);

        SubTask subTask = new SubTask("Sub", "desc", epic.getId());
        subTask.setStartTime(LocalDateTime.of(2025, 1, 1, 8, 0));
        manager.createSubTask(subTask);

        List<Task> prioritized = manager.getPrioritizedTasks();
        assertTrue(prioritized.contains(subTask));
    }

    // Тесты 8 спринт

    @Test
    void taskShouldReturnStartTime() {
        LocalDateTime testTime = LocalDateTime.of(2025, 10, 1, 14, 30);
        Duration duration = Duration.ofHours(2);

        Task task = new Task("Задача", "Описание", TaskStatus.NEW, testTime, duration);

        LocalDateTime result = task.getStartTime();

        assertNotNull(result);
        assertEquals(testTime, result);
    }

    @Test
    void taskShouldReturnDuration() {
        Duration expectedDuration = Duration.ofMinutes(90);
        Task task = new Task("Задача", "Описание", TaskStatus.NEW, LocalDateTime.now(), expectedDuration);

        Duration result = task.getDuration();

        assertNotNull(result);
        assertEquals(expectedDuration, result);
    }

    @Test
    void getEndTimeShouldBeStartTimePlusDuration() {
        LocalDateTime startTime = LocalDateTime.of(2025, 10, 1, 10, 0);
        Duration duration = Duration.ofHours(3); // 3 часа
        LocalDateTime expectedEnd = LocalDateTime.of(2025, 10, 1, 13, 0);

        Task task = new Task("Задача", "Описание", TaskStatus.NEW, startTime, duration);

        LocalDateTime actualEnd = task.getEndTime();

        assertEquals(expectedEnd, actualEnd);
    }

    @Test
    void getEndTimeShouldReturnNullIfStartTimeIsNull() {
        Task task = new Task("Задача", "Описание", TaskStatus.NEW,null, Duration.ofHours(1));

        assertNull(task.getEndTime());
    }

    @Test
    void getEndTimeShouldReturnNullIfDurationIsNull() {
        Task task = new Task("Задача", "Описание", TaskStatus.NEW, LocalDateTime.now(), null);

        assertNull(task.getEndTime());
    }

    @Test
    void shouldUpdateStartTimeAndDuration() {
        Task task = new Task("Задача", "Описание", TaskStatus.NEW, null, null);

        LocalDateTime newStart = LocalDateTime.of(2025, 10, 2, 9, 0);
        Duration newDuration = Duration.ofMinutes(45);

        task.setStartTime(newStart);
        task.setDuration(newDuration);

        assertEquals(newStart, task.getStartTime());
        assertEquals(newDuration, task.getDuration());
        assertEquals(newStart.plus(newDuration), task.getEndTime());
    }

    //

    @Test
    void tasksWithSameIdShouldBeEqual() { // Проверка Task по ID. Две задачи с 1 ID ==
        Task task1 = new Task("Задача", "Описание", TaskStatus.NEW);
        task1.setId(1);

        Task task2 = new Task("Другое имя", "Другое описание", TaskStatus.DONE);
        task2.setId(1);

        assertEquals(task1, task2); // Они должны быть равны по ID
    }

    @Test
    void epicsWithSameIdShouldBeEqual() { // Проверка Epic по ID
        Epic epic1 = new Epic("Эпик", "Описание");
        epic1.setId(2);

        Epic epic2 = new Epic("Другой эпик", "Другое описание");
        epic2.setId(2);

        assertEquals(epic1, epic2); // Равны по ID
    }

    @Test
    void subtasksWithSameIdShouldBeEqual() { // Проверка Subtask по ID
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

        Epic epic = new Epic("Эпик", "Описание");
        Epic createdEpic = manager.createEpic(epic);
        int epicId = createdEpic.getId();

        SubTask subTask = new SubTask("Подзадача", "Описание", epicId);
        SubTask result = manager.createSubTask(subTask);

        assertNotNull(result, "Подзадача должна быть создана");

        assertNotEquals(epicId, result.getId()); // Проверяем, что ID подзадачи НЕ совпадает с ID эпика
        assertEquals(epicId, result.getEpicId()); // Проверяем, что подзадача правильно ссылается на эпик
        assertTrue(createdEpic.getSubTasks().contains(result.getId()),"ID подзадачи должен быть в списке " +
                                                                                "подзадач эпика");
    }

    @Test // Проверка, что объект Subtask нельзя сделать своим же эпиком;
    void shouldThrowExceptionIfSubtaskIsItsOwnEpic() {
        // Создаем подзадачу, ссылающуюся на НЕСУЩЕСТВУЮЩИЙ эпик
        SubTask subTask = new SubTask("Подзадача", "Описание", 999999); // Нет эпика

        // Пытаемся создать подзадачу
        SubTask result = manager.createSubTask(subTask);

        // Проверяем, что подзадача НЕ создана
        assertNull(result, "Подзадача не должна создаваться для несуществующего эпика");
    }

    @Test // Проверка, что утилитарный класс всегда возвращает проинициализированные и готовые к работе +
    void managersShouldReturnNonNullInstances() { //  экземпляры менеджеров;
        assertNotNull(Managers.getDefault());
        assertNotNull(Managers.getDefaultHistory());
    }

    @Test // Проверка проверьте, что InMemoryTaskManager действительно добавляет задачи разного типа и может
    void taskManagerCanAddAndGetTasksById() { // найти их по id;

        Task task = new Task("Задача", "Описание", TaskStatus.NEW);
        Epic epic = new Epic("Эпик", "Описание");

        Epic createdEpic = manager.createEpic(epic);

        SubTask subTask = new SubTask("Сабтаск", "Описание", createdEpic.getId());

        Task createdTask = manager.createTask(task);
        SubTask createdSubTask = manager.createSubTask(subTask);

        Task task1 = manager.getByIdTask(createdTask.getId());
        Epic epic1 = manager.getByIdEpic(createdEpic.getId());
        SubTask subtask1 = manager.getByIdSubtask(createdSubTask.getId());

        assertEquals(task1, createdTask);
        assertEquals(epic1, createdEpic);
        assertEquals(subtask1, createdSubTask);
    }

    @Test // Проверка, что задачи с заданным id и сгенерированным id не конфликтуют внутри менеджера;
    void idsShouldBeUniqueAcrossAllTypes() {

        Task autoTask = new Task("Задача", "Описание", TaskStatus.NEW);
        Task manualTask = new Task(999, "Задача 2", "Описание 2", TaskStatus.NEW);

        Task createAuto = manager.createTask(autoTask);
        Task createManual = manager.createTask(manualTask);

        Task getTaskAuto = manager.getByIdTask(createAuto.getId());
        Task getTaskManual = manager.getByIdTask(createManual.getId());

        assertNotNull(getTaskAuto);
        assertNotNull(getTaskManual);

    }

    @Test // создайте тест, в котором проверяется неизменность задачи (по всем полям) при добавлении задачи в менеджер
    void taskShouldNotChangeAfterAddingToManager() {

        Task original = new Task("Задача", "Описание", TaskStatus.NEW);

        Task saved = manager.createTask(original);
        Task getTasks = manager.getByIdTask(saved.getId());

        assertEquals(saved.getId(), getTasks.getId());
        assertEquals(saved.getName(), getTasks.getName());
        assertEquals(saved.getDescription(), getTasks.getDescription());
        assertEquals(saved.getTaskStatus(), getTasks.getTaskStatus());
    }

    @Test // Проверка, что задачи, добавляемые в HistoryManager, сохраняют предыдущую версию задачи и её данных.
    void historyShouldStoreFullTaskObject() {

        Task task = new Task("Купить хлеб", "Срочно", TaskStatus.NEW);
        Task createTask = manager.createTask(task);

        Task taskFromManager = manager.getByIdTask(createTask.getId()); // добавили в историю
        createTask.setName("Купить хлебушка");

        List<Task> history = manager.getHistory();
        Task oldTask = history.get(0);

        assertEquals("Купить хлеб", oldTask.getName());
    }

    @Test
    void shouldAddTaskInHistoryInLast() { // Проверяем метод добавления в конец истории
        Task task1 = new Task("Первая задача", "Описание", TaskStatus.NEW);
        Task createTask1 = manager.createTask(task1);
        Task taskFromManager1 = manager.getByIdTask(createTask1.getId()); // добавили в историю

        Task task2 = new Task("Вторая задача", "Описание", TaskStatus.NEW);
        Task createTask2 = manager.createTask(task2);
        Task taskFromManager2 = manager.getByIdTask(createTask2.getId()); // добавили в историю

        Task task3 = new Task("Третья задача", "Описание", TaskStatus.NEW);
        Task createTask3 = manager.createTask(task3);
        Task taskFromManager3 = manager.getByIdTask(createTask3.getId()); // добавили в историю

        List<Task> history = manager.getHistory();
        Task oldTask = history.get(0);

        assertEquals("Первая задача", oldTask.getName());
    }

    @Test
    void shouldRemoveAndChangeOldVersionTask() { // Проверяем, что из истории удаляются задачи.
        Task task1 = new Task("Первая задача", "Описание", TaskStatus.NEW);
        Task createTask1 = manager.createTask(task1);
        Task taskFromManager1 = manager.getByIdTask(createTask1.getId()); // добавили в историю

        Task task2 = new Task("Вторая задача", "Описание", TaskStatus.NEW);
        Task createTask2 = manager.createTask(task2);
        Task taskFromManager2 = manager.getByIdTask(createTask2.getId()); // добавили в историю

        Task task3 = new Task("Третья задача", "Описание", TaskStatus.NEW);
        Task createTask3 = manager.createTask(task3);
        Task taskFromManager3 = manager.getByIdTask(createTask3.getId()); // добавили в историю

        List<Task> historyTasksOld = manager.getHistory();
        int historySizeOld = historyTasksOld.size();

        manager.deleteTask(taskFromManager3.getId());

        List<Task> historyTasksNew = manager.getHistory();
        int historySizeNew = historyTasksNew.size();

        assertNotEquals(historySizeOld, historySizeNew);
        assertEquals(historySizeOld - 1, historySizeNew);
    }

}