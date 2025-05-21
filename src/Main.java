import tasks.Epic;
import tasks.SubTask;
import tasks.Task;
import tasks.TaskStatus;

public class Main {
    public static void main(String[] args) {
        TaskManager manager = new TaskManager();

    // Обычные задачи - Проверка

        // Создать задачу
        System.out.println("Создать задачу");
        Task taskForCreate = new Task("Name", "Descrip", TaskStatus.NEW);
        manager.createTask(taskForCreate);
        System.out.println(manager.getTasks());
        System.out.println();

        // Обновление
        System.out.println("Обновление задачи по ID");
        Task taskForUpdate = new Task(taskForCreate.getId(), "New Name",
                taskForCreate.getDescription(), TaskStatus.IN_PROGRESS);
        manager.updateTask(taskForUpdate);
        System.out.println(manager.getTasks());
        System.out.println();

        // Получение по ID
        System.out.println("Получение по ID");
        System.out.println(manager.getTask(taskForCreate.getId()));
        System.out.println();

        // Удаление по ID
        System.out.println("Удаление по ID");
        manager.deleteTask(taskForUpdate.getId());
        System.out.println(manager.getTasks());
        System.out.println();

        // Получение всех задач
        System.out.println("Получение всех задач");
        System.out.println(manager.getTasks());
        System.out.println();

        // Удаление всех задач
        System.out.println("Удаление всех задач");
        manager.clearTask();
        System.out.println(manager.getTasks());
        System.out.println();

    // Эпики - Проверка

        // Создать задачу Epic
        System.out.println("Создать задачу Epic");
        Epic epicForCreate = new Epic("Name", "Descrip");
        manager.createEpic(epicForCreate);
        System.out.println(manager.getTasks());
        System.out.println();

        // Получение всех задач для Epic
        System.out.println("Получение всех задач для Epic");
        System.out.println(manager.getAllEpics());
        System.out.println();

        // Создать Subtask для Epic и привязываем по ID
        System.out.println("Создать подзадачу для Epic и привязываем по ID");
        SubTask subTask = new SubTask("NameSubtask", "DescripSubtask", epicForCreate.getId());
        manager.createSubTaskForEpic(subTask);
        System.out.println(manager.getTasks());
        System.out.println();

        // Получаем Epic и его подзадач по ID
        System.out.println("Получаем EPIC и его подзадачи");
        Epic epic = manager.getEpic(epicForCreate.getId());
        if (epic != null) {
            System.out.println("Эпик и подзадачи:");
            for (SubTask sub : epic.getSubTasks()) {
                System.out.println(" - " + sub.getName());
                System.out.println(" - " + sub.getDescription());
            }
        }
        System.out.println();

        // Обновление Epic по ID
        Epic storedEpic = manager.getEpic(epicForCreate.getId());
        if (storedEpic != null) {
            storedEpic.setName("New Name");
            storedEpic.setDescription("Новое описание");
            storedEpic.setStatus(TaskStatus.IN_PROGRESS);
            manager.updateEpic(storedEpic);
        }
        System.out.println(manager.getTasks());
        System.out.println();

        // Удаляем Subtask у Epica
        System.out.println("Удаляем Subtask у Epica ");
        manager.deleteAllSubTasksOfEpic(epicForCreate.getId());
        System.out.println(manager.getTasks());
        System.out.println();

        // Удаляем Epic и его Subtask по ID
        System.out.println("Удаляем Epic и его Subtask по ID");
        System.out.println(manager.deleteEpicAndItsSubTasks(epicForCreate.getId()));
        System.out.println(manager.getTasks());
        System.out.println();
    }
}