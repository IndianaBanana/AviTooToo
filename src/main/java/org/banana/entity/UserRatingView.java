package org.banana.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Immutable;
import org.hibernate.proxy.HibernateProxy;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Entity
@Immutable
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor

public class UserRatingView {

//    @Id
//    @MapsId
//    @OneToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
//    private User userId;
    @Id
    private UUID userId;
    private BigDecimal averageRating;
    private int ratingCount;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        UserRatingView that = (UserRatingView) o;
        return getUserId() != null && Objects.equals(getUserId(), that.getUserId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
