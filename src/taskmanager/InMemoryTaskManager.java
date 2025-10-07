package taskmanager;
import tasks.Epic;
import tasks.SubTask;
import tasks.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {

    private final HashMap<Integer, Task> tasks = new HashMap<>(); // Хранение Task задач.
    private final HashMap<Integer, Epic> epics = new HashMap<>(); // Хранение Epic задач.
    private final HashMap<Integer, SubTask> subtasks = new HashMap<>(); // Хранение Subtask задач.

    // Функциональность 8 спринт

    private final TreeSet<Task> prioritizedTasks = new TreeSet<>(Task.BY_START_TIME);

    // Новая функциональность для ФЗ Спринта 5

    private final HistoryManager historyManager;

    private Integer generatorId = 1; // Объявляем переменную для хранения ID


    public InMemoryTaskManager() {
        this.historyManager = Managers.getDefaultHistory();
    }

    private boolean checkCrossingTasks(Task t1, Task t2) {
        if (t1 == null || t2 == null ||
                t1.getStartTime() == null || t2.getStartTime() == null ||
                t1.getDuration() == null || t2.getDuration() == null) {
            return false;
        }
        LocalDateTime start1 = t1.getStartTime();
        LocalDateTime end1 = t1.getEndTime();
        LocalDateTime start2 = t2.getStartTime();
        LocalDateTime end2 = t2.getEndTime();

        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    private boolean checkCrossing(Task newTask) {
        return getPrioritizedTasks().stream()
            .filter(task -> task.getId() != newTask.getId())
            .anyMatch(task -> checkCrossingTasks(task, newTask));
    }

    private void updateTimes(Epic epic) { // Реализация метода поиска и суммирования времени подзадач.
        List<SubTask> epicSubTasks = getSubTasksByEpic(epic.getId());

        LocalDateTime firstTimeSubTask = epicSubTasks.stream()
                .map(subTask -> subTask.getStartTime())
                .filter(subtask -> subtask != null)
                .min((date1, date2) -> date1.compareTo(date2))
                .orElse(null);

        LocalDateTime lastTimeSubTask = epicSubTasks.stream()
                .map(subTask -> subTask.getEndTime())
                .filter(subtask -> subtask != null)
                .max((date1, date2) -> date1.compareTo(date2))
                .orElse(null);

        Duration totalDuration = epicSubTasks.stream()
                .map(subTask -> subTask.getDuration())
                .filter(duration -> duration != null)
                .reduce(Duration.ofMinutes(0), (a, b) -> a.plus(b));

        epic.setDuration(totalDuration);
        epic.setStartTime(firstTimeSubTask);
        epic.setEndTime(lastTimeSubTask);
    }

    @Override
    public List<Task> getPrioritizedTasks() { // Получить список приоритетных задач.
        return new ArrayList<>(prioritizedTasks);
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    @Override
    public Integer getNextId() { // Метод для генерации след. ID
        return generatorId++;
    }

    @Override
    public ArrayList<Task> printTasks() { // Печать всех задач Task
        return new ArrayList<>(tasks.values());
    }

    @Override
    public ArrayList<Epic> printEpics() { // Печать всех задач Epic
        return new ArrayList<>(epics.values());
    }

    @Override
    public ArrayList<SubTask> printSubtask() { // Печать всех задач SubTask
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public void removeAllTasks() { // Удаление всех задач Task
        tasks.values().stream()
                .forEach(task -> historyManager.remove(task.getId()));

        tasks.clear();
        // Получаем стрим из объектов prioritizedTasks
        Set<Task> removeTasks = prioritizedTasks.stream()
            .filter(task -> task.getClass() == Task.class) // Проверили, что это Task
            .collect(Collectors.toSet());
        // Теперь удалили из prioritizedTasks
        prioritizedTasks.removeAll(removeTasks);
    }

    @Override
    public void removeAllEpics() { // Удаление всех задач Epic
        epics.values().stream()
            .forEach(epic ->
                historyManager.remove(epic.getId())
            );

        subtasks.values().stream()
            .forEach(subtask ->
                historyManager.remove(subtask.getId())
            );

        epics.clear();
        subtasks.clear();

        Set<Task> epicsRemove = prioritizedTasks.stream() // Получаем стрим из объектов prioritizedTasks
            .filter(task -> // Фильтруем Эпики и СабТаски
                task.getClass() == Epic.class ||
                task.getClass() == SubTask.class
            )
            .collect(Collectors.toSet());

        prioritizedTasks.removeAll(epicsRemove); // Теперь удалили из prioritizedTasks
    }

    @Override
    public void removeAllSubTasks() { // Удаление всех задач SubTask
        subtasks.values().stream()
            .forEach(subTask ->
                historyManager.remove(subTask.getId())
            );

        subtasks.clear();

        epics.values().stream()
            .forEach(epic -> {
                epic.getSubTasks().clear();
                updateEpicStatus(epic);
                updateTimes(epic);
            });

        // Получаем стрим из объектов prioritizedTasks
        Set<Task> subTasksRemove = prioritizedTasks.stream()
            .filter(task -> task.getClass() == SubTask.class) // Фильтруем Сабтаски
            .collect(Collectors.toSet());
        // Теперь удалили из prioritizedTasks
        prioritizedTasks.removeAll(subTasksRemove);
    }

    @Override
    public Task getByIdTask(Integer id) { // Получить Task по Id
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.add(task);
        }
        return task;
    }

    @Override
    public Epic getByIdEpic(Integer id) { // Получить Epic по Id
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.add(epic);
        }
        return epic;
    }

    @Override
    public SubTask getByIdSubtask(Integer id) { // Получить Subtask по Id
        SubTask subtask = subtasks.get(id);
        if (subtask != null) {
            historyManager.add(subtask);
        }
        return subtask;
    }

    @Override
    public Task createTask(Task task) { // К Task задаче добавили ID и добавили ее по ID в Map, вернули задачу.
        if (task == null) {
            System.out.println("Пустой объект");
            return null;
        }
        if (tasks.containsValue(task)) {
            System.out.println("Такая задача уже существует");
            return null;
        }
        if (checkCrossing(task)) {
            System.out.println("Задача пересекается с другой задачей");
            return null;
        }
        task.setId(getNextId());
        tasks.put(task.getId(), task);
        prioritizedTasks.add(task);
        return task;
    }

    @Override
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
        prioritizedTasks.add(epic);
        return epic;
    }

    @Override
    public SubTask createSubTask(SubTask subTask) {
        if (subTask == null) {
            System.out.println("Пустой объект");
            return null;
        }
        Integer epicId = subTask.getEpicId(); // Получили epicID подзадачи
        if (!epics.containsKey(epicId)) { // если мапа не содержит такой ключ с таким id, null!
            return null;
        }
        if (checkCrossing(subTask)) {
            System.out.println("СабТаск пересекается с другой задачей");
            return null;
        }
        int newSubTaskId = getNextId();
        if (newSubTaskId == epicId) { // Проверка на самоссылку
            return null;
        }
        Epic epic = epics.get(subTask.getEpicId()); // Получили Эпик задачу из мапы

        subTask.setId(newSubTaskId); // Установили ID, сгенерированный
        epic.getSubTasks().add(subTask.getId()); // Добавили ID в список сабтаскID Epica
        subtasks.put(subTask.getId(), subTask); // Добавили в мапу
        updateEpicStatus(epic); // Обновили статус Эпика
        updateTimes(epic);
        prioritizedTasks.add(subTask);

        return subTask;
    }

    @Override
    public Task updateTask(Task newTask) { // Получаем задачу, записываем ее по ID в Map и возвращаем обновленную.
        Integer id = newTask.getId();

        Task oldVerisonTask = tasks.get(id);
        if (oldVerisonTask == null) {
            return null;
        }
        prioritizedTasks.remove(oldVerisonTask);

        if (checkCrossing(newTask)) {
            prioritizedTasks.add(oldVerisonTask);
            return null;
        }

        tasks.put(id, newTask);
        prioritizedTasks.add(newTask);

        return newTask;
    }

    @Override
    public Epic updateEpic(Epic newEpic) {
        Integer id = newEpic.getId();

        Epic updateVerison = epics.get(id);
        if (updateVerison == null) {
            return null;
        }

        updateVerison.setName(newEpic.getName());
        updateVerison.setDescription(newEpic.getDescription());

        updateEpicStatus(updateVerison);
        updateTimes(updateVerison);

        prioritizedTasks.add(updateVerison);

        return updateVerison;
    }

    @Override
    public SubTask updateSubtask(SubTask newSubTask) {
        Integer id = newSubTask.getId();

        SubTask updateVersion = subtasks.get(id);
        if (updateVersion == null) {
            return null;
        }
        prioritizedTasks.remove(updateVersion);
        if (checkCrossing(newSubTask)) {
            prioritizedTasks.add(updateVersion);
            System.out.println("СабТаск пересекается с другой задачей.");
            return null;
        }
        subtasks.put(id, newSubTask);
        prioritizedTasks.add(newSubTask);
        Epic epic = epics.get(newSubTask.getEpicId());
        if (epic != null) {
            updateEpicStatus(epic);
            updateTimes(epic);
        }
        return newSubTask;
    }

    @Override
    public Task deleteTask(Integer id) { // Принимаем ID объекта, удаляем и возвращаем удаленный объект.
        Task removed = tasks.remove(id);
        if (removed != null) {
            historyManager.remove(id); // Удаление из истории
            prioritizedTasks.remove(removed);
        }
        return removed;
    }

    @Override
    public Epic deleteEpic(Integer id) {
        Epic epic = epics.get(id); // Получаем задачу из мап по Id
        if (epic == null) {
            return null;
        }

        printSubtask().stream()
            .filter(subtask -> id.equals(subtask.getEpicId()))
            .forEach(subtask -> {
                    subtasks.remove(subtask.getId());
                    historyManager.remove(subtask.getId());
                    prioritizedTasks.remove(subtask);
            });

        epics.remove(id); // Удалили эпик задачу
        prioritizedTasks.remove(epic);
        historyManager.remove(id); // Удаление эпика из истории

        return epic;
    }

    @Override
    public SubTask deleteSubtaskById(Integer id) {
        SubTask removed = subtasks.remove(id);
        if (removed != null) {
            historyManager.remove(id); // Удаление подзадачи из истории
            prioritizedTasks.remove(removed);

        Epic epic = epics.get(removed.getEpicId());
            if (epic != null) {
                updateEpicStatus(epic);
                updateTimes(epic);
            }
        }
        return removed;
    }

    @Override
    public ArrayList<SubTask> getSubTasksByEpic(Integer epicId) { // Получение списка всех подзадач определённого эпика.
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return new ArrayList<>();
        }

        ArrayList<SubTask> result = new ArrayList<>(epic.getSubTasks().stream()
            .map(subtask -> subtasks.get(subtask))
            .filter(subtask -> subtask != null)
            .collect(Collectors.toList()));

        return result;
    }

    @Override
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
    }
}
