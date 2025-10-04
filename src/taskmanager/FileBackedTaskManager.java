package taskmanager;
import tasks.Epic;
import tasks.SubTask;
import tasks.Task;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import java.util.ArrayList;

public class FileBackedTaskManager extends InMemoryTaskManager {

    Path path;

    public FileBackedTaskManager(Path path) {
        this.path = path;
    }

    @Override
    public ArrayList<Task> printTasks() { // Печать всех задач Task
        return new ArrayList<>(super.printTasks()); // Так как поле private в IMTM, получаем через метод.
    }

    @Override
    public ArrayList<Epic> printEpics() { // Печать всех задач Epic
        return new ArrayList<>(super.printEpics());
    }

    @Override
    public ArrayList<SubTask> printSubtask() { // Печать всех задач SubTask
        return new ArrayList<>(super.printSubtask());
    }

    @Override
    public Task getByIdTask(Integer id) { // Получить Task по Id
        Task getTask = super.getByIdTask(id); // Пришел null или задача.
            if (getTask != null) { // Если не null сохраняем.
                save();
            }
        return getTask; // Возвращаем или null или задачу.
    }

    @Override
    public Epic getByIdEpic(Integer id) { // Получить Epic по Id
        Epic getEpic = super.getByIdEpic(id);
        if (getEpic != null) {
            save();
        }
        return getEpic;
    }

    @Override
    public SubTask getByIdSubtask(Integer id) { // Получить Subtask по Id
        SubTask subTask = super.getByIdSubtask(id);
        if (subTask != null) {
            save();
        }
        return subTask;
    }

    @Override
    public void removeAllTasks() { // Удаление всех задач Task
        super.removeAllTasks(); // Обратились через родительский метод.
        save();
    }

    @Override
    public void removeAllEpics() { // Удаление всех задач Epic
        super.removeAllEpics();
        save();
    }

    @Override
    public void removeAllSubTasks() { // Удаление всех задач SubTask
        super.removeAllSubTasks();
        save();
    }

    @Override
    public Task deleteTask(Integer id) { // Удаляем TASK задачу по ID
        Task removed = super.deleteTask(id); // Обратились через родительский метод.
        if (removed != null) {
            save();
        }
        return removed;
    }

    @Override
    public Epic deleteEpic(Integer id) { // Удаляем EPIC задачу по ID
        Epic removed = super.deleteEpic(id);
        if (removed != null) {
            save();
        }
        return removed;
    }

    @Override
    public SubTask deleteSubtaskById(Integer id) { // Удаляем SubTask задачу по ID
        SubTask removed = super.deleteSubtaskById(id);
        if (removed != null) {
            save();
        }
        return removed;
    }

    @Override
    public Task createTask(Task task) { // Создание Task
        Task result = super.createTask(task); // Обратились через родительский метод.
        if (result != null) {
            save();
        }
        return result;
    }

    @Override
    public Epic createEpic(Epic epic) { // Создание Epic
        Epic result = super.createEpic(epic);
        if (result != null) {
            save();
        }
        return result;
    }

    @Override
    public SubTask createSubTask(SubTask subTask) { // Создание Subtask
        SubTask result = super.createSubTask(subTask);
        if (result != null) {
            save();
        }
        return result;
    }

    @Override
    public Task updateTask(Task task) { // Получаем задачу, записываем ее по ID в Map и возвращаем обновленную.
        Task update = super.updateTask(task);
        if (update != null) {
            save();
        }
        return update;
    }

    @Override
    public Epic updateEpic(Epic epic) {
        Epic update = super.updateEpic(epic);
        if (update != null) {
            save();
        }
        return update;
    }

    @Override
    public SubTask updateSubtask(SubTask subTask) {
        SubTask update = super.updateSubtask(subTask);
        if (update != null) {
            save();
        }
        return update;
    }

// Новая функциональность Спринта №7

    public void save() {
        ArrayList<Task> allTasks = new ArrayList<>(printTasks());
        allTasks.addAll(printEpics());
        allTasks.addAll(printSubtask());
        try (
             BufferedWriter bw = new BufferedWriter(
                 new OutputStreamWriter(
                     new FileOutputStream(path.toFile()),
                     StandardCharsets.UTF_8));
        ) {
            bw.write("id,type,name,status,description,epic");
            bw.newLine();

            allTasks.stream()
                .forEach( task -> {
                    try {
                        bw.write(CsvTaskHelper.parseToString(task));
                        bw.newLine();
                    } catch (IOException e) {
                        throw new RuntimeException("Ошибка записи в файл", e);
                    }
                });
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
                Task task = CsvTaskHelper.parseFromString(line);
                if (task != null) {
                    if (task instanceof SubTask subTask) {
                        manager.createSubTask(subTask);
                    } else if (task instanceof Epic epic) {
                        manager.createEpic(epic);
                    } else {
                        manager.createTask(task);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Файла по данному пути, не существует." + e.getMessage());
        }
        return manager;
    }


}