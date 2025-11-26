package knu.atoz.member.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PasswordUpdateRequestDto {
    @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
    @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하로 입력해주세요.")
    private String newPassword;

    @NotBlank(message = "비밀번호 확인은 필수 입력 값입니다.")
    private String confirmNewPassword;

    @NotBlank(message = "현재 비밀번호는 필수 입력 값입니다.")
    private String currentPassword;
}
