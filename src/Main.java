import taskmanager.FileBackedTaskManager;
import taskmanager.TaskManager;
import taskmanager.Managers;
import taskmanager.TaskStatus;

import tasks.Task;
import tasks.SubTask;
import tasks.Epic;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;


public class Main {
    public static void main(String[] args) throws IOException {
        TaskManager manager = Managers.getDefault();
        File fileToBackup = File.createTempFile("backup-", ".csv");
        FileBackedTaskManager fileBackedTaskManager = Managers.getDefaultBacked(fileToBackup.toPath());

        // Проверяем функциональность Спринта №7

        System.out.println("--------".repeat(10));
        System.out.println("Проверяем функциональность Спринта №7 \n");

        System.out.println("Создаем 3 задачи: Task, Epic, Subtask \n");

        Task task = new Task("Задача", "Описание", TaskStatus.NEW);
        fileBackedTaskManager.createTask(task);
        Epic epic  = new Epic("Задача Эпик", "Описание Эпик");
        fileBackedTaskManager.createEpic(epic);
        SubTask subTask = new SubTask("Задача Сабтаск", "Описание Сабтаск", epic.getId());
        fileBackedTaskManager.createSubTask(subTask);

        System.out.println("Проверяем, создались ли они ?");

        System.out.println(fileBackedTaskManager.printTasks());
        System.out.println(fileBackedTaskManager.printEpics());
        System.out.println(fileBackedTaskManager.printSubtask());

        System.out.println("\nПроверяем получение задач по id");

        System.out.println(fileBackedTaskManager.getByIdTask(task.getId()));
        System.out.println(fileBackedTaskManager.getByIdEpic(epic.getId()));
        System.out.println(fileBackedTaskManager.getByIdSubtask(subTask.getId()));

        System.out.println("\nЗагружаем Задачи, проверяем загрузку из файла.");
        FileBackedTaskManager loadBeckUpFile = FileBackedTaskManager.loadFromFile(fileToBackup);

        System.out.println(loadBeckUpFile.printTasks());
        System.out.println(loadBeckUpFile.printEpics());
        System.out.println(loadBeckUpFile.printSubtask());

    /*
    // Тесты ФЗ - Спринт 4

        // 1. Создаем две обычные задачи
        System.out.println("Создаём задачи про уборку:");
        Task task1 = new Task("Вымыть посуду", "После завтрака", TaskStatus.NEW);
        Task task2 = new Task("Вынести мусор", "Вывести пакет", TaskStatus.NEW);

        manager.createTask(task1);
        manager.createTask(task2);


        System.out.println("Все обычные задачи:");
        manager.printTasks();
        System.out.println();

        // 2. Создаём эпик Уборка кухни с двумя подзадачами
        System.out.println("Создаём эпик 'Уборка кухни' с двумя подзадачами:");
        Epic kitchenEpic = new Epic("Уборка кухни", "Навести порядок на кухне");
        manager.createEpic(kitchenEpic);

        SubTask sub1 = new SubTask("Протереть стол", "Очистить от крошек", kitchenEpic.getId());
        SubTask sub2 = new SubTask("Мыть пол", "Помыть после еды", kitchenEpic.getId());

        manager.createSubTask(sub1);
        manager.createSubTask(sub2);

        System.out.println("Подзадачи у эпика 'Уборка кухни':");
        for (SubTask sub : manager.getSubTasksByEpic(kitchenEpic.getId())) {
            System.out.println(" - " + sub.getName() + " - Статус: " + sub.getTaskStatus());
        }
        System.out.println();

        // 3. Создаём второй эпик "Уборка в комнате"
        System.out.println("Создаём эпик 'Уборка в комнате' с одной подзадачей:");
        Epic roomEpic = new Epic("Уборка в комнате", "Убрать вещи и протереть пыль");
        manager.createEpic(roomEpic);

        SubTask sub3 = new SubTask("Сделать генералку", "Перед уборкой", roomEpic.getId());
        manager.createSubTask(sub3);

        System.out.println("Подзадачи у эпика 'Уборка в комнате':");
        for (SubTask sub : manager.getSubTasksByEpic(roomEpic.getId())) {
            System.out.println(" - " + sub.getName() + " - Статус: " + sub.getTaskStatus());
        }
        System.out.println();

        // 4. Выводим всё, что добавили
        System.out.println("Список всех задач:");
        manager.printTasks();
        System.out.println();

        System.out.println("Список всех эпиков:");
        manager.printEpics();
        System.out.println();

        System.out.println("Список всех подзадач:");
        manager.printSubtask();
        System.out.println();

        // 5. Меняем статусы подзадач у "Уборки кухни"
        System.out.println("Обновляем статусы подзадач у эпика 'Уборка кухни':");

        sub1.setTaskStatus(TaskStatus.DONE); // Протереть стол - сделано
        manager.updateSubtask(sub1);
        System.out.println("Подзадача 1 - DONE");

        sub2.setTaskStatus(TaskStatus.DONE); // Мыть пол - тоже сделано
        manager.updateSubtask(sub2);
        System.out.println("Подзадача 2 - DONE");

        // Проверяем статус эпика
        Epic updatedKitchenEpic = manager.getByIdEpic(kitchenEpic.getId());
        System.out.println("Статус эпика 'Уборка кухни': " + updatedKitchenEpic.getTaskStatus());
        System.out.println();

        // 6. Удаляем одну подзадачу и один эпик
        System.out.println("Удаляем подзадачу и эпик:");

        manager.deleteSubtaskById(sub3.getId());
        System.out.println("Подзадача удалена");

        manager.deleteEpic(roomEpic.getId());
        System.out.println("Эпик 'Уборка в комнате' удален");
        System.out.println();

        // 7. Проверяем оставшиеся задачи
        System.out.println("Что осталось:");

        System.out.println("Обычные задачи:");
        manager.printTasks();

        System.out.println("\nЭпики:");
        manager.printEpics();

        System.out.println("\nПодзадачи:");
        manager.printSubtask();
        System.out.println();

    // Тесты ФЗ - Спринт 5

        System.out.println("// Тесты ФЗ - Спринт 5");
        System.out.println();

        System.out.println("Задачи:");
        System.out.println();
        for (Task task : manager.printTasks()) {
            System.out.println(task);
        }

        System.out.println();

        System.out.println("Эпики:");
        System.out.println();
        for (Task epic : manager.printEpics()) {
            System.out.println(epic);

            for (Task task : manager.getSubTasksByEpic(epic.getId())) {
                System.out.println("--> " + task);
            }
        }
        System.out.println();

        System.out.println("Подзадачи:");
        System.out.println();
        for (Task subtask : manager.printSubtask()) {
            System.out.println(subtask);
        }
        System.out.println();

        System.out.println("История:");
        System.out.println();
        for (Task task : manager.getHistory()) {
            System.out.println(task);
        }

        manager.removeAllTasks();
        manager.removeAllEpics();
        manager.removeAllSubTasks();

        System.out.println(manager.printTasks());
        System.out.println(manager.printEpics());
        System.out.println(manager.printSubtask() + "\n");
        System.out.println("Задач нет, пусто. \n");
*/









    }
}