package org.banana.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
//@Table(name = "comment")
public class Comment {

    @Id
    @UuidGenerator
    @Column(name = "comment_id", updatable = false, nullable = false)
    private UUID id;

    @NotNull
    @ToString.Exclude
    private UUID advertisementId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commenter_id")
    @ToString.Exclude
    private User commenter;

    private UUID rootCommentId;

    private UUID parentCommentId;

    @NotBlank
    private String commentText;

    @NotNull
    private LocalDateTime commentDate;

    public Comment(UUID advertisementId, UUID rootCommentId, UUID parentCommentId, String commentText, User commenter, LocalDateTime commentDate) {
        this.advertisementId = advertisementId;
        this.rootCommentId = rootCommentId;
        this.parentCommentId = parentCommentId;
        this.commentText = commentText;
        this.commenter = commenter;
        this.commentDate = commentDate;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Comment comment = (Comment) o;
        return getId() != null && Objects.equals(getId(), comment.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
