import tasks.TaskStatus;
import tasks.Task;
import tasks.SubTask;
import tasks.Epic;

import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {

    private final HashMap<Integer, Task> tasks = new HashMap<>(); // Хранение Task задач.
    private final HashMap<Integer, Epic> epics = new HashMap<>(); // Хранение Epic задач.
    private final HashMap<Integer, SubTask> subtasks = new HashMap<>(); // Хранение Subtask задач.

    private Integer generatorId = 1; // Объявляем переменную для хранения ID

    private Integer getNextId() { // Метод для генерации след. ID
        return generatorId++;
    }

    public ArrayList<Task> printTasks () { // Печать всех задач Task
        return new ArrayList<>(tasks.values());
    }

    public ArrayList<Epic> printEpics () { // Печать всех задач Epic
        return new ArrayList<>(epics.values());
    }

    public ArrayList<SubTask> printSubtask () { // Печать всех задач SubTask
        return new ArrayList<>(subtasks.values());
    }

    public void removeTasks () { // Удаление всех задач Task
        tasks.clear();
    }

    public void removeEpic () { // Удаление всех задач Epic
        epics.clear();
        subtasks.clear();
    }

    public void removeSubTask () { // Удаление всех задач SubTask
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.getSubTasks().clear();
            updateEpicStatus(epic);
        }
    }

    public Task getByIdTask (Integer id) { // Получить Task по Id
        return tasks.get(id);
    }

    public Epic getByIdEpic (Integer id) { // Получить Epic по Id
        return epics.get(id);
    }

    public SubTask getByIdSubtask (Integer id) { // Получить Subtask по Id
        return subtasks.get(id);
    }

    public Task createTask(Task task) { // К Task задаче добавили ID и добавили ее по ID в Map, вернули задачу.
        if (task == null) {
            System.out.println("Пустой объект");
            return null;
        }
        if (tasks.containsValue(task)) {
            System.out.println("Такая задача уже существует");
            return null;
        }
        task.setId(getNextId());
        tasks.put(task.getId(), task);
        return task;
    }

    public Epic createEpic(Epic epic) {
        if (epic == null) {
            System.out.println("Пустой объект");
            return null;
        }
        if (epics.containsValue(epic)) {
            System.out.println("Такой эпик уже существует");
            return null;
        }
        epic.setId(getNextId());
        epics.put(epic.getId(), epic);
        return epic;
    }

    public SubTask createSubTask(SubTask subTask) {
        Integer epicId = subTask.getEpicId(); // Получили epicID подзадачи
        if (!epics.containsKey(epicId)) { // если мапа не содержит такой ключ с таким id, null!
            return null;
        }
        Epic epic = epics.get(subTask.getEpicId()); // Получили Эпик задачу из мапы
        if (epic != null) { // Если задача из мапы не нулл, то выполняем логику
            subTask.setId(getNextId()); // Установили ID, сгенерированный
            epic.getSubTasks().add(subTask.getId()); // Добавили ID в список сабтаскID Epica
            subtasks.put(subTask.getId(), subTask); // Добавили в мапу
            updateEpicStatus(epic); // Обновили статус Эпика
        }
        return subTask;
    }

    public Task updateTask(Task task) { // Получаем задачу, записываем ее по ID в Map и возвращаем обновленную.
        Task task1 = tasks.get(task.getId());
        if (!task1.equals(task)){
            return null;
        }
        tasks.put(task.getId(), task);
        return task;
    }

    public Epic updateEpic(Epic epic) {
        epics.put(epic.getId(), epic);
        return epic;
    }

    public SubTask updateSubtask(SubTask subTask) {
        Epic epic = epics.get(subTask.getEpicId());
        if (epic != null) {
            subtasks.put(subTask.getId(), subTask);
            updateEpicStatus(epic);
        }
        return subTask;
    }

    public Task deleteTask(Integer id){ // Принимаем ID объекта, удаляем и возвращаем удаленный объект.
        return tasks.remove(id);
    }

    public Epic deleteEpic(Integer id) {
        Epic epic = epics.get(id); // Получаем задачу из мап по Id
        for (SubTask subTask : subtasks.values()) { // Перебираем подзадачи
            if (id.equals(subTask.getEpicId())) { // Если пришедший id сравним с id подзадачи
                subtasks.remove(subTask.getId()); // Удаляем
            }
        }
        epics.remove(id); // Удалили эпик задачу
        return epic;
    }

    public SubTask deleteSubtasks(Integer id){
        SubTask removed = subtasks.remove(id);
        if (removed != null) {
            Epic epic = epics.get(removed.getEpicId());
            if (epic != null) {
                updateEpicStatus(epic);
            }
        }
        return removed;
    }

    public ArrayList<SubTask> getSubTasksByEpic(Integer epicId) { // Получение списка всех подзадач определённого эпика.
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return new ArrayList<>();
        }
        ArrayList<SubTask> result = new ArrayList<>();
        for (Integer subTaskId : epic.getSubTasks()) {
            SubTask subTask = subtasks.get(subTaskId);
            if (subTask != null) {
                result.add(subTask);
            }
        }
        return result;
    }

    public void updateEpicStatus(Epic epic) {
        ArrayList<SubTask> subs = getSubTasksByEpic(epic.getId());
        if (subs.isEmpty()) {
            epic.setTaskStatus(TaskStatus.NEW);
        } else {
            boolean allNew = true;
            boolean allDone = true;
            for (SubTask sub : subs) {
                if (sub.getTaskStatus() != TaskStatus.NEW) {
                    allNew = false;
                }
                if (sub.getTaskStatus() != TaskStatus.DONE) {
                    allDone = false;
                }
            }
            if (allNew) {
                epic.setTaskStatus(TaskStatus.NEW);
            } else if (allDone) {
                epic.setTaskStatus(TaskStatus.DONE);
            } else {
                epic.setTaskStatus(TaskStatus.IN_PROGRESS);
            }
        }
        updateEpic(epic);
    }
}
