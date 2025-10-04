package taskmanager;

import tasks.Epic;
import tasks.SubTask;
import tasks.Task;

import java.time.Duration;
import java.time.LocalDateTime;

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
        line.append(",");

        if (task.getStartTime() != null) {
            line.append(task.getStartTime().toString());
        }
        line.append(",");

        if (task.getDuration() != null) {
            line.append(task.getDuration().toMinutes());
        }
        line.append(",");

        return line.toString();
    }

    public static Task parseFromString(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }

        String[] massiveTransform = line.split(",", -1);

        if (massiveTransform.length < 8) {
            return null;
        }

        int id = Integer.parseInt(massiveTransform[0].trim());
        String type = massiveTransform[1].trim();
        String name = massiveTransform[2].trim();
        String status = massiveTransform[3].trim();
        String description = massiveTransform[4].trim();

        int epicIdSub = -1;
        LocalDateTime startTime = null;
        Duration duration = null;

        if (!massiveTransform[5].trim().isEmpty()) {
            epicIdSub = Integer.parseInt(massiveTransform[5].trim());
        }

        if (!massiveTransform[6].trim().isEmpty()) {
            startTime = LocalDateTime.parse(massiveTransform[6].trim());
        }

        if (!massiveTransform[7].trim().isEmpty()) {
            long minutes = Long.parseLong(massiveTransform[7].trim());
            duration = Duration.ofMinutes(minutes);
        }

        switch (type) {
            case "TASK" :
                Task task = new Task(name, description, TaskStatus.NEW);
                task.setId(id);
                task.setStartTime(startTime);
                task.setDuration(duration);
                if ("IN_PROGRESS".equals(status)) {
                    task.setTaskStatus(TaskStatus.IN_PROGRESS);
                } else if ("DONE".equals(status)) {
                    task.setTaskStatus(TaskStatus.DONE);
                }
                return task;

            case "EPIC" :
                Epic epic = new Epic(name, description);
                epic.setId(id);
                epic.setStartTime(startTime);
                epic.setDuration(duration);
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
                SubTask subTask = new SubTask(name, description, epicIdSub, startTime, duration);
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
