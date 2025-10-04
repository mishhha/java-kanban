package tasks;

import taskManager.TaskStatus;
import taskManager.TaskType;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class Epic extends Task {

    private LocalDateTime endTime;

    private final ArrayList<Integer> subTasksId;

    public ArrayList<Integer> getSubTasks() {
        return subTasksId;
    }

    public Epic(String name, String description) {
        super(name, description, TaskStatus.NEW);
        this.subTasksId = new ArrayList<>();
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    @Override
    public TaskType getType() {
        return TaskType.EPIC;
    }

}
