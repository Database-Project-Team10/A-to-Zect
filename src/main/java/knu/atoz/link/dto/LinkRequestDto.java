package knu.atoz.link.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LinkRequestDto {
    @NotBlank(message = "제목은 필수 입력값입니다.")
    private String title;

    @NotBlank(message = "URL은 필수 입력값입니다.")
    @Pattern(regexp = "^https?://.*", message = "URL은 반드시 http:// 또는 https:// 로 시작해야 합니다.")
    private String url;
}