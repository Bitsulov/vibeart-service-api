package ru.vibeart.api.dtos.chat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

@Schema(description = "Данные для создания чата")
public class CreateChatRequest {
    private List<UUID> memberUuids;

    @Schema(description = "UUID участников чата, без учёта создателя")
    @NotEmpty(message = "Members list cannot be empty")
    public List<UUID> getMemberUuids() {
        return memberUuids;
    }
    public void setMemberUuids(List<UUID> memberUuids) {
        this.memberUuids = memberUuids;
    }
}
