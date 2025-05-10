package org.banana.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.proxy.HibernateProxy;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
//@Table(name = "message")
public class Message {

    @Id
    @UuidGenerator
    @Column(name = "message_id", updatable = false, nullable = false)
    private UUID id;

    private UUID advertisementId;

    @NotNull
    private UUID senderId;

    @NotNull
    private UUID recipientId;

    @NotBlank
    private String messageText;

    @NotNull
    private LocalDateTime messageDateTime;

    @NotNull
    private Boolean isRead = false;

    public Message(UUID advertisementId, UUID senderId, UUID recipientId, String messageText, LocalDateTime messageDateTime) {
        this.advertisementId = advertisementId;
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.messageText = messageText;
        this.messageDateTime = messageDateTime;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Message message = (Message) o;
        return getId() != null && Objects.equals(getId(), message.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
