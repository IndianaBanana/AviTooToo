package org.banana.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "advertisement")
public class Advertisement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "advertisement_id", nullable = false)
    private UUID id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;
    @Column(name = "title", nullable = false)
    @Size(max = 255, message = "title max size must be 255 characters or less")
    private String title;
    @Column(name = "description", nullable = false)
    private String description;
    @Column(name = "price", nullable = false)
    @PositiveOrZero
    private BigDecimal price;  // цена за единицу товара
    @Column(name = "quantity", nullable = false)
    @Positive
    private Integer quantity; // сколько продукта продает пользователь
    @Column(name = "is_paid", nullable = false)
    private Boolean isPaid; // todo: подумать сделать ли тут как то default или просто в сервисе мутить уже
    @Column(name = "create_date", nullable = false)
    private LocalDateTime createDate;
    @Column(name = "close_date")
    private LocalDateTime closeDate;
}