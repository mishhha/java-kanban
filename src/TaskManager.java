import tasks.Epic;
import tasks.SubTask;
import tasks.Task;

import java.util.ArrayList;

public interface TaskManager {

    Integer getNextId(); // Метод для генерации след. Id

    Task createTask(Task task); // Создать Task

    Epic createEpic(Epic epic); // Создать Epic

    SubTask createSubTask(SubTask subTask); // Создать SubTask

    Task getByIdTask(Integer id); // Получить Task по Id

    Epic getByIdEpic(Integer id); // Получить Epic по Id

    SubTask getByIdSubtask(Integer id); // Получить SubTask по Id

    Task updateTask(Task task); // Обновить задачу Task;

    Epic updateEpic(Epic epic); // Обновить задачу Epic;

    ArrayList<SubTask> getSubTasksByEpic(Integer epicId); // Получить SubTask Epic по Id

    SubTask updateSubtask(SubTask subTask); // Обновить задачу SubTask;

    void updateEpicStatus(Epic epic); // Обновить статус Epic

    Task deleteTask(Integer id); // Удалить Task

    Epic deleteEpic(Integer id); // Удалить Epic

    SubTask deleteSubtasks(Integer id); // Удалить Subtasks

    void removeTasks(); // Удалить Task.

    void removeEpic(); // Удалить Epic.

    void removeSubTask(); // Удалить SubTask.

    // Новая функциональность ФЗ - Спринт 5

    ArrayList<Task> getHistory(); // Сохраняем историю вызова объектов.

}