package ru.vibeart.api.models.enums;

/**
 * Статус доставки сообщения.
 *
 * <h2>Значения</h2>
 * <ul>
 *   <li>{@code RECEIVED} — сообщение доставлено;</li>
 *   <li>{@code READ} — сообщение прочитано получателем.</li>
 * </ul>
 */
public enum MessageStatus {
    RECEIVED(1, "received"),
    READ(2, "read");

    private int id;
    private String title;

    MessageStatus(int id, String title) {
        this.id = id;
        this.title = title;
    }

    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
}
