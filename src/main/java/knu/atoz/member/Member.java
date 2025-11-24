package knu.atoz.member;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class Member {

    private Long id;
    private String email;
    private String password;
    private String name;
    private LocalDate birthDate;
    private LocalDateTime createdAt;

    public Member(String email, String password, String name, LocalDate birthDate,  LocalDateTime createdAt) {
        this.id = null;
        this.email = email;
        this.password = password;
        this.name = name;
        this.birthDate = birthDate;
        this.createdAt = createdAt;
    }
}
