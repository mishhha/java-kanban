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

    public void printTasks () { // Печать всех задач Task
        for (Task taskValues :tasks.values()) {
            System.out.println(taskValues);
        }
    }

    public void printEpics () { // Печать всех задач Epic
        for (Epic epicValues :epics.values()) {
            System.out.println(epicValues);
        }
    }

    public void printSubtask () { // Печать всех задач SubTask
        for (SubTask subValues :subtasks.values()) {
            System.out.println(subValues);
        }
    }

    public void removeTasks () { // Удаление всех задач Task
        tasks.clear();
    }

    public void removeEpic () { // Удаление всех задач Epic
        epics.clear();
    }

    public void removeSubTask () { // Удаление всех задач SubTask
        subtasks.clear();
        for (Epic epic : epics.values()) {
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
        task.setId(getNextId());
        tasks.put(task.getId(), task);
        return task;
    }

    public Epic createEpic(Epic epic) {
        epic.setId(getNextId());
        epics.put(epic.getId(), epic);
        return epic;
    }

    public SubTask createSubTask(SubTask subTask) {
        subTask.setId(getNextId());
        subtasks.put(subTask.getId(), subTask);
        Epic epic = epics.get(subTask.getEpicId());
        if (epic != null) {
            updateEpicStatus(epic);
        }
        return subTask;
    }

    public Task updateTask(Task task) { // Получаем задачу, записываем ее по ID в Map и возвращаем обновленную.
        tasks.put(task.getId(), task);
        return task;
    }

    public Epic updateEpic(Epic epic) {
        epics.put(epic.getId(), epic);
        return epic;
    }

    public SubTask updateSubtask(SubTask subTask) {
        subtasks.put(subTask.getId(), subTask);
        Epic epic = epics.get(subTask.getEpicId());
        if (epic != null) {
            updateEpicStatus(epic);
        }
        return subTask;
    }

    public Task deleteTask(Integer id){ // Принимаем ID объекта, удаляем и возвращаем удаленный объект.
        return tasks.remove(id);
    }

    public Epic deleteEpic(Integer id){
        return epics.remove(id);
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
        ArrayList<SubTask> result = new ArrayList<>();
        for (SubTask subTask : subtasks.values()) {
            if (subTask.getEpicId() != null && subTask.getEpicId().equals(epicId)) {
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
