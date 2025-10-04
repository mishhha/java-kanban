package tasks;

import taskmanager.TaskStatus;
import taskmanager.TaskType;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Objects;

public class Task {
    private String name;
    private String description;
    private Integer id;
    private TaskStatus taskStatus;
    private TaskType taskType;
    private Duration duration;
    private LocalDateTime startTime;

    public static final Comparator<Task> BY_START_TIME = (o1, o2) -> {
        // 1. Если startTime null
        if (o1.getStartTime() == null && o2.getStartTime() == null) {
            return 0;
        }
        if (o1.getStartTime() == null) {
            return 1;
        }
        if (o2.getStartTime() == null) {
            return -1;
        }
        // 2. Если время есть
        LocalDateTime start1 = o1.getStartTime();
        LocalDateTime start2 = o2.getStartTime();

        if (start1.isBefore(start2)) {
            return -1;
        }
        if (start1.isAfter(start2)) {
            return 1;
        }
        // 3. Если время одинаково, проверим по Id
        if (o1.getId() < o2.getId()) {
            return -1;
        }
        if (o1.getId() > o2.getId()) {
            return 1;
        }
        return 0;
    };

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        if (startTime == null || duration == null) {
            return null;
        }
        return startTime.plus(duration);
    }

    public TaskType getType() {
        return TaskType.TASK;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskStatus getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(TaskStatus taskStatus) {
        this.taskStatus = taskStatus;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Task(String name, String description, TaskStatus taskStatus) {
        this.name = name;
        this.description = description;
        this.taskStatus = taskStatus;
    }

    public Task(Integer id, String name, String description, TaskStatus taskStatus) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.taskStatus = taskStatus;
    }

    public Task(String name, String description, TaskStatus taskStatus, LocalDateTime startTime, Duration duration) {
        this.name = name;
        this.description = description;
        this.taskStatus = taskStatus;
        this.startTime = startTime;
        this.duration = duration;
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return Objects.equals(id, task.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Task{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", id=" + id +
                ", taskStatus=" + taskStatus +
                '}';
    }

}
