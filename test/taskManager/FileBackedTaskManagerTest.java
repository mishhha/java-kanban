package taskManager;

import org.junit.jupiter.api.Test;
import tasks.Task;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FileBackedTaskManagerTest {

    @Test // Не должен бросать исключение при сохранении.
    void saveShouldNotThrowExceptionWhenFileIsWritable() {
        File backup = null;
        try {
            backup = File.createTempFile("backup-", ".csv");
            FileBackedTaskManager manager = Managers.getDefaultBacked(backup.toPath());

            Task task = new Task("Задача", "Описание", TaskStatus.NEW);
            manager.createTask(task);

            assertDoesNotThrow(() -> manager.save(), "Сохранение должно пройти без ошибок");

        } catch (IOException e) {
            System.out.println(e.getMessage() + "Произошла ошибка записи.");
        }
    }

    @Test // Должен бросать исключение при сохранении.
    void shouldCatchExceptionBeforeSaveInFile() {

        Path invalidPath = Paths.get("/kuda-to/suda.csv");
        FileBackedTaskManager manager = Managers.getDefaultBacked(invalidPath);

        assertThrows(ManagerSaveException.class, () -> manager.save(),
            "Сохранение должно вызывать исключение"
        );
    }

    @Test // Загрузка не должна вызывать исключение
    void loadFromFileShouldNotThrowExceptionForValidFile() throws IOException {
        File tempFile = File.createTempFile("test-load-", ".csv");
        try (BufferedWriter bw = Files.newBufferedWriter(tempFile.toPath(), StandardCharsets.UTF_8)) {
            bw.write("id,type,name,status,description,epic");
            bw.newLine();
            bw.write("1,TASK,Test,NEW,Description,");
            bw.newLine();
        }

        assertDoesNotThrow(
            () -> FileBackedTaskManager.loadFromFile(tempFile),
            "Загрузка не должна вызывать исключение"
        );
    }

}
