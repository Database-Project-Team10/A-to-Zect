package knu.atoz.member.dto;

import knu.atoz.member.Member;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@RequiredArgsConstructor
public class SignupRequestDto {
    private String email;
    private String password;
    private String confirmPassword;
    private String name;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthDate;

    public Member toEntity() {
        return new Member(
                this.email,
                this.password,
                this.name,
                this.birthDate,
                LocalDateTime.now()
        );
    }
}
