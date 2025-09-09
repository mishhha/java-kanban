package taskmanager;

import tasks.Epic;
import tasks.SubTask;
import tasks.Task;

public class CsvTaskHelper {

    public static String parseToString(Task task) {
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

    public static Task parseFromString(String line) {
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
                return task;

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
                return epic;

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
                return subTask;


            default:
                System.out.println("Неизвестный тип задачи: " + type);
                return null;
        }
    }

}
