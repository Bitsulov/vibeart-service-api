package ru.vibeart.api.dtos.common;

import ru.vibeart.api.models.enums.StompEvent;

import java.util.UUID;

public class StompResponse<T> {
    private String target;
    private StompEvent event;
    private UUID chatUuid;
    private T payload;

    public StompResponse(String target, StompEvent event, UUID chatUuid, T payload) {
        this.target = target;
        this.event = event;
        this.chatUuid = chatUuid;
        this.payload = payload;
    }

    public String getTarget() {
        return target;
    }
    public void setTarget(String target) {
        this.target = target;
    }

    public StompEvent getEvent() {
        return event;
    }
    public void setEvent(StompEvent event) {
        this.event = event;
    }

    public UUID getChatUuid() {
        return chatUuid;
    }
    public void setChatUuid(UUID chatUuid) {
        this.chatUuid = chatUuid;
    }

    public T getPayload() {
        return payload;
    }
    public void setPayload(T payload) {
        this.payload = payload;
    }
}
