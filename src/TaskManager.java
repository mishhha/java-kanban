import tasks.Epic;
import tasks.SubTask;
import tasks.Task;
import tasks.TaskStatus;

import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {
    private HashMap<Integer, Task> tasks = new HashMap<>();
    private Integer generatorId = 1;

    public ArrayList<Task> getTasks() { // Метод для печати списка всех задач.
        return new ArrayList<>(tasks.values());
    }

    public Task createTask(Task task) { // К пришедшей задаче добавили ID и добавили ее по ID в Map, вернули задачу.
        task.setId(getNextId());
        tasks.put(task.getId(), task);
        return task;
    }

    private Integer getNextId() { // Сгенерировали след. ID
        return generatorId++;
    }

    public Task updateTask(Task task) { // Получаем задачу, записываем ее по ID в Map и возвращаем обновленную.
        tasks.put(task.getId(), task);
        return task;
    }

    public Task deleteTask(Integer id){ // Принимаем ID объекта, удаляем и возвращаем удаленный объект.
        return tasks.remove(id);
    }

    public void clearTask () {
        tasks.clear();
    }

    public Task getTask (Integer id) { // Принимаем ID, получаем из MAP по ID задачу и возвращаем ее.
        return tasks.get(id);
    }

// Методы EPIC'a

    public ArrayList<Epic> getAllEpics() { // Вывести все Epic
        ArrayList<Epic> epics = new ArrayList<>();

        for (Task task : tasks.values()) {
            if (task.getClass() == Epic.class) {
                epics.add((Epic) task);
            }
        }

        return epics;
    }

    public Epic createEpic(Epic epic) { // Создание Эпика и назначение ему ID, добавляем в MAP
        epic.setId(getNextId());
        tasks.put(epic.getId(), epic);
        return epic;
    }

    private void updateEpicStatus(Epic epic) { // Обновляем статус задач
        ArrayList<SubTask> subTasks = epic.getSubTasks(); // Принимаем объект

        if (subTasks.isEmpty()) { // Если подзадач нет или новая - ставим статус NEW
            epic.setStatus(TaskStatus.NEW);
        } else {
            boolean allDone = true; // Проверяем все подзадачи вручную
            boolean allNew = true;
            for (SubTask subTask : subTasks) {
                TaskStatus status = subTask.getStatus();
                if (status != TaskStatus.DONE) {
                    allDone = false;
                }
                if (status != TaskStatus.NEW) {
                    allNew = false;
                }
            }
            if (allDone) {
                epic.setStatus(TaskStatus.DONE); // Все подзадачи выполнены
            } else if (allNew) {
                epic.setStatus(TaskStatus.NEW); // Все подзадачи новые
            } else {
                epic.setStatus(TaskStatus.IN_PROGRESS); // Есть разные статусы
            }
        }
        updateEpic(epic);
    }

    public Epic getEpic(Integer id) { // Взять Epic по ID
        Task task = tasks.get(id);
        if (task != null && task.getClass() == Epic.class) {
            return (Epic) task;
        } else {
            return null;
        }
    }

    public Epic updateEpic(Epic epic) { // Обновляем Epic
        if (epic == null) { // проверяем, не равен ли он нулл,
            return null;
        }
        if (tasks.containsKey(epic.getId())) {
            tasks.put(epic.getId(), epic); // Записываем.
            return epic;
        } else {
            return null;
        }

    }

    public Epic deleteEpicAndItsSubTasks(Integer epicId) {
        Epic epic = getEpic(epicId);
        if (epic == null) {
            return null;
        }
        for (SubTask subTask : epic.getSubTasks()) {
            tasks.remove(subTask.getId());
        }
        epic.getSubTasks().clear();
        tasks.remove(epicId);
        return epic;
    }

// Методы SubTask

    public SubTask createSubTaskForEpic(SubTask subTask) {
        if (subTask == null) {
            return null;
        }
        Epic epic = getEpic(subTask.getEpicId()); // Создаем подзадачу для Epic
        if (epic == null) { // Если нет эпика с таким ID, то не создаем задачу
            return null;
        }
        subTask.setId(getNextId()); // Назначаем ID
        tasks.put(subTask.getId(), subTask); // Добавляем подзадачу в HashMap и в список эпика
        epic.getSubTasks().add(subTask);
        updateEpicStatus(epic);
        return subTask;
    }

    public SubTask updateSubTaskStatus(Integer subTaskId, TaskStatus newStatus) {
        Task task = tasks.get(subTaskId);
        if (task == null || !(task instanceof SubTask)) {
            return null;
        }
        SubTask subTask = (SubTask) task;
        subTask.setStatus(newStatus);
        Epic epic = getEpic(subTask.getEpicId());
        if (epic != null) {
            updateEpicStatus(epic);
        }
        return subTask;
    }

    public void deleteAllSubTasksOfEpic(Integer epicId) { // Удаление подзадач Epica
        Epic epic = getEpic(epicId);
        if (epic == null) {
            return;
        }
        ArrayList<SubTask> subTasks = new ArrayList<>(epic.getSubTasks());
        for (SubTask subTask : subTasks) {
            tasks.remove(subTask.getId());
        }
        epic.getSubTasks().clear();
        updateEpicStatus(epic);
    }

}