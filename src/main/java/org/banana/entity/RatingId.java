package org.banana.entity;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Embeddable
public class RatingId implements Serializable {

    private String userId;
    private String raterId;
}
