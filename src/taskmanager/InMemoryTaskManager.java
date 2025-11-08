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
        prioritizedTasks.removeIf(task -> task.getClass() == Task.class);
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

        prioritizedTasks.removeIf(task -> task.getClass() == SubTask.class);
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

        prioritizedTasks.removeIf(task -> task.getClass() == SubTask.class);
    }

    @Override
    public Task getByIdTask(Integer id) throws NotFoundException { // Получить Task по Id
        Task task = tasks.get(id);
        if (task == null) {
            throw new NotFoundException("Такой задачи нет.");
        }
        historyManager.add(task);
        return task;
    }

    @Override
    public Epic getByIdEpic(Integer id) { // Получить Epic по Id
        Epic epic = epics.get(id);
        if (epic == null) {
            throw new NotFoundException("Такой задачи нет.");
        }
        historyManager.add(epic);
        return epic;
    }

    @Override
    public SubTask getByIdSubtask(Integer id) { // Получить Subtask по Id
        SubTask subtask = subtasks.get(id);
        if (subtask == null) {
            throw new NotFoundException("Такой задачи нет.");
        }
        historyManager.add(subtask);
        return subtask;
    }

    @Override
    public Task createTask(Task task) throws NotFoundException { // К Task задаче добавили ID и добавили ее по ID в Map, вернули задачу.
        if (task == null) {
            throw new NotFoundException("Передан пустой объект.");
        }
        if (tasks.containsValue(task)) {
            throw new NotFoundException("Такая задача уже существует.");
        }
        if (checkCrossing(task)) {
            throw new CrossingException("Задача пересекается с другой задачей.");
        }
        task.setId(getNextId());
        tasks.put(task.getId(), task);
        prioritizedTasks.add(task);
        return task;
    }

    @Override
    public Epic createEpic(Epic epic) {
        if (epic == null) {
            throw new NotFoundException("Передан пустой объект.");
        }
        if (epics.containsValue(epic)) {
            throw new NotFoundException("Такая задача уже существует.");
        }
        epic.setId(getNextId());
        epics.put(epic.getId(), epic);
        return epic;
    }

    @Override
    public SubTask createSubTask(SubTask subTask) {
        if (subTask == null) {
            throw new NotFoundException("Передан пустой объект.");
        }
        Integer epicId = subTask.getEpicId(); // Получили epicID подзадачи
        if (!epics.containsKey(epicId)) { // если мапа не содержит такой ключ с таким id, null!
            throw new NotFoundException("Такого эпика не существует.");
        }
        if (checkCrossing(subTask)) {
            throw new CrossingException("СабТаск пересекается с другой задачей");
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
            throw new NotFoundException("Задача не найдена");
        }
        prioritizedTasks.remove(oldVerisonTask);

        if (checkCrossing(newTask)) {
            prioritizedTasks.add(oldVerisonTask);
            throw new CrossingException("Не удалось обновить задачу: время пересекается с другой задачей");
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
            throw new NotFoundException("Задача не найдена");
        }

        updateVerison.setName(newEpic.getName());
        updateVerison.setDescription(newEpic.getDescription());

        updateEpicStatus(updateVerison);
        updateTimes(updateVerison);

        return updateVerison;
    }

    @Override
    public SubTask updateSubtask(SubTask newSubTask) {
        Integer id = newSubTask.getId();

        SubTask updateVersion = subtasks.get(id);
        if (updateVersion == null) {
            throw new NotFoundException("Задача не найдена");
        }
        prioritizedTasks.remove(updateVersion);
        if (checkCrossing(newSubTask)) {
            prioritizedTasks.add(updateVersion);
            throw new ManagerSaveException("Не удалось обновить subTask: время пересекается с другой задачей");
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
        if (removed == null) {
            throw new NotFoundException("Задача не найдена.");
        }
        historyManager.remove(id); // Удаление из истории
        prioritizedTasks.remove(removed);
        return removed;
    }

    @Override
    public Epic deleteEpic(Integer id) {
        Epic epic = epics.get(id); // Получаем задачу из мап по Id
        if (epic == null) {
            throw new NotFoundException("Задача не найдена");
        }

        printSubtask().stream()
            .filter(subtask -> id.equals(subtask.getEpicId()))
            .forEach(subtask -> {
                    subtasks.remove(subtask.getId());
                    historyManager.remove(subtask.getId());
                    prioritizedTasks.remove(subtask);
            });

        epics.remove(id); // Удалили эпик задачу
        historyManager.remove(id); // Удаление эпика из истории

        return epic;
    }

    @Override
    public SubTask deleteSubtaskById(Integer id) {
        SubTask removed = subtasks.remove(id);
        if (removed == null) {
            throw new NotFoundException("Задача не найдена");
        }
        historyManager.remove(id); // Удаление подзадачи из истории
        prioritizedTasks.remove(removed);

        Epic epic = epics.get(removed.getEpicId());
            if (epic != null) {
                updateEpicStatus(epic);
                updateTimes(epic);
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
