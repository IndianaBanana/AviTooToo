package org.banana.entity.rating;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.banana.entity.User;
import org.hibernate.proxy.HibernateProxy;

import java.util.Objects;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
public class Rating {

    @EmbeddedId
    private RatingId id;

    @NotNull
    @Min(1)
    @Max(5)
    private Short ratingValue;
//    // todo посмотреть надо ли нам оставлять эти объекты.
//    @Transient
//    @ManyToOne(optional = false, fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_id", insertable = false, updatable = false)
//    @ToString.Exclude
//    private User user;
//    @Transient
//    @ManyToOne(optional = false, fetch = FetchType.LAZY)
//    @JoinColumn(name = "rater_id", insertable = false, updatable = false)
//    @ToString.Exclude
//    private User rater;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Rating rating = (Rating) o;
        return getId() != null && Objects.equals(getId(), rating.getId());
    }

    @Override
    public final int hashCode() {
        return Objects.hash(id);
    }
}

