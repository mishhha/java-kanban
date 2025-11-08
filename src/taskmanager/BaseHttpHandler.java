package taskmanager;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import static java.nio.charset.StandardCharsets.UTF_8;

public class BaseHttpHandler {

    protected void sendText(HttpExchange h, String text) throws IOException {
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(200, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }

    public void sendNotFound(HttpExchange h) throws IOException {
        String text = "Задача не найдена.";
        byte[] resp = text.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "text/plain;charset=utf-8");
        h.sendResponseHeaders(404, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }

    public void sendHasOverlaps(HttpExchange h) throws IOException {
        String error = "Задача пересекается с другой задачей";
        byte[] errorBytes = error.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "text/plain;charset=utf-8");
        h.sendResponseHeaders(406, errorBytes.length);
        try (OutputStream os = h.getResponseBody()) {
            os.write(errorBytes);
        } finally {
            h.close();
        }
    }

    public void badRequest(HttpExchange h) throws IOException {
        String error = "Некорректный формат запроса";
        byte[] errorBytes = error.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "text/plain;charset=utf-8");
        h.sendResponseHeaders(400, errorBytes.length);
        try (OutputStream os = h.getResponseBody()) {
            os.write(errorBytes);
        } finally {
            h.close();
        }
    }

    public void syntaxException(HttpExchange h) throws IOException {
        String error = "Некорректный формат передачи JSON";
        byte[] errorBytes = error.getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "text/plain;charset=utf-8");
        h.sendResponseHeaders(400, errorBytes.length);
        try (OutputStream os = h.getResponseBody()) {
            os.write(errorBytes);
        } finally {
            h.close();
        }
    }

    public void sendResponse201(HttpExchange h) throws IOException {
        byte[] done = "Задача успешно создана".getBytes(UTF_8);
        h.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        h.sendResponseHeaders(201, done.length);
        try (OutputStream os = h.getResponseBody()) {
            os.write(done);
        } finally {
            h.close();
        }
    }


}
