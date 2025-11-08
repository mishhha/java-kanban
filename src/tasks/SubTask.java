package tasks;

import taskmanager.TaskStatus;
import taskmanager.TaskType;

import java.time.Duration;
import java.time.LocalDateTime;

public class SubTask extends Task {

    private Integer epicId;

    public Integer getEpicId() {
        return epicId;
    }

    public void setEpicId(Integer epicId) {
        this.epicId = epicId;
    }

    public SubTask(String name, String description, Integer epicId) {
        super(name, description, TaskStatus.NEW);
        this.epicId = epicId;
    }

    public SubTask(String name, String description, Integer epicId, LocalDateTime startTime, Duration duration) {
        super(name, description, TaskStatus.NEW);
        this.epicId = epicId;
        setStartTime(startTime);
        setDuration(duration);
    }

    @Override
    public TaskType getType() {
        return TaskType.SUBTASK;
    }
}
