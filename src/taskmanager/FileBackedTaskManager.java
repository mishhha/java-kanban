package taskmanager;
import tasks.Epic;
import tasks.SubTask;
import tasks.Task;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import java.util.ArrayList;
import java.util.HashMap;

public class FileBackedTaskManager extends InMemoryTaskManager {

    Path path;

    private final HashMap<Integer, Task> tasks = new HashMap<>(); // Хранение Task задач.
    private final HashMap<Integer, Epic> epics = new HashMap<>(); // Хранение Epic задач.
    private final HashMap<Integer, SubTask> subtasks = new HashMap<>(); // Хранение Subtask задач.

    public FileBackedTaskManager(Path path) {
        super();
        this.path = path;
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
        task.setId(getNextId());
        tasks.put(task.getId(), task);
        save();
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
        save();
        return epic;
    }

    @Override
    public SubTask createSubTask(SubTask subTask) {
        Integer epicId = subTask.getEpicId(); // Получили epicID подзадачи
        if (!epics.containsKey(epicId)) { // если мапа не содержит такой ключ с таким id, null!
            return null;
        }
        int newSubTaskId = getNextId();
        if (newSubTaskId == epicId) { // Проверка на самоссылку
            return null;
        }
        Epic epic = epics.get(subTask.getEpicId()); // Получили Эпик задачу из мапы
        if (epic != null) { // Если задача из мапы не нулл, то выполняем логику
            subTask.setId(newSubTaskId); // Установили ID, сгенерированный
            epic.getSubTasks().add(subTask.getId()); // Добавили ID в список сабтаскID Epica
            subtasks.put(subTask.getId(), subTask); // Добавили в мапу
            updateEpicStatus(epic); // Обновили статус Эпика
        }
        save();
        return subTask;
    }

    @Override
    public Task updateTask(Task task) { // Получаем задачу, записываем ее по ID в Map и возвращаем обновленную.
        Task task1 = tasks.get(task.getId());
        if (!task1.equals(task)) {
            return null;
        }
        tasks.put(task.getId(), task);
        save();
        return task;
    }

    @Override
    public Epic updateEpic(Epic epic) {
        epics.put(epic.getId(), epic);
        save();
        return epic;
    }

    @Override
    public SubTask updateSubtask(SubTask subTask) {
        Epic epic = epics.get(subTask.getEpicId());
        if (epic != null) {
            subtasks.put(subTask.getId(), subTask);
            updateEpicStatus(epic);
        }
        save();
        return subTask;
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
        save();
        updateEpic(epic);
    }

// Новая функциональность Спринта №7

    public String toString(Task task) {
        StringBuilder line = new StringBuilder();
        line.append(task.getId());
        line.append(",");
        line.append(task.getType());
        line.append(",");
        line.append(task.getName());
        line.append(",");
        line.append(task.getTaskStatus());
        line.append(",");
        line.append(task.getDescription());
        line.append(",");

        if (task instanceof SubTask subTask) {
            line.append(subTask.getEpicId());
        }
        return line.toString();
    }

    public void save() {
        ArrayList<Task> allTasks = new ArrayList<>(tasks.values());
        allTasks.addAll(epics.values());
        allTasks.addAll(subtasks.values());
        try (
             BufferedWriter bw = new BufferedWriter(
                 new OutputStreamWriter(
                     new FileOutputStream(path.toFile()),
                     StandardCharsets.UTF_8));
        ) {
            bw.write("id,type,name,status,description,epic");
            bw.newLine();

            for (Task task : allTasks) {
                bw.write(toString(task));
                bw.newLine();
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Не удалось сохранить данные в файл." + e.getMessage());
        }
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        Path path = file.toPath();
        FileBackedTaskManager manager = new FileBackedTaskManager(path);
        try (
            FileInputStream fis = new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(isr);
        ) {
            String line;

            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("id,")) {
                    continue;
                }
                String[] massiveTransform = line.split(",");

                int id = Integer.parseInt(massiveTransform[0]);
                String type = massiveTransform[1].trim();
                String name = massiveTransform[2].trim();
                String description = massiveTransform[3].trim();
                String status = massiveTransform[4].trim();
                int epicIdSub = -1;
                if (massiveTransform.length > 5 && !massiveTransform[5].trim().isEmpty()) {
                    epicIdSub = Integer.parseInt(massiveTransform[5]);
                }

                switch (type) {
                    case "TASK" :
                        Task task = new Task(name, description, TaskStatus.NEW);
                        task.setId(id);
                        if ("IN_PROGRESS".equals(status)) {
                            task.setTaskStatus(TaskStatus.IN_PROGRESS);
                        } else if ("DONE".equals(status)) {
                            task.setTaskStatus(TaskStatus.DONE);
                        }
                        manager.createTask(task);
                        break;

                    case "EPIC" :
                        Epic epic = new Epic(name, description);
                        epic.setId(id);
                        switch (status) {
                            case "NEW":
                                epic.setTaskStatus(TaskStatus.NEW);
                                break;
                            case "IN_PROGRESS":
                                epic.setTaskStatus(TaskStatus.IN_PROGRESS);
                                break;
                            case "DONE":
                                epic.setTaskStatus(TaskStatus.DONE);
                                break;
                        }
                        manager.createEpic(epic);
                        break;

                    case "SUBTASK" :
                        SubTask subTask = new SubTask(name, description, epicIdSub);
                        subTask.setId(id);
                        subTask.setEpicId(epicIdSub);
                        subTask.setTaskStatus(TaskStatus.NEW);
                        if ("IN_PROGRESS".equals(status)) {
                            subTask.setTaskStatus(TaskStatus.IN_PROGRESS);
                        } else if ("DONE".equals(status)) {
                            subTask.setTaskStatus(TaskStatus.DONE);
                        }
                        manager.createSubTask(subTask);
                        break;

                    default:
                        System.out.println("Неизвестный тип задачи: " + type);
                        break;
                }
            }
        } catch (IOException e) {
            System.out.println("Файла по данному пути, не существует." + e.getMessage());
        }
        return manager;
    }


}