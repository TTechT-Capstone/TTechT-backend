package com.example.TTECHT.entity.token;

import com.example.TTECHT.entity.user.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Data
@Builder
@Getter
@Setter
@Table(name = "password_reset_tokens")
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PasswordResetToken {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    Long id;

    @Column(name = "token", unique = true)
    String token;

    // TODO: should add the nullable = false
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    User user;

    @Column(name = "expiry_date")
    LocalDateTime expiryDate;

    @CreationTimestamp
    LocalDateTime createdDate;

    @Column(name = "used")
    boolean used = false;

    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDateTime.now());
    }
}
