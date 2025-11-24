package knu.atoz.mbti.project.dto;

import lombok.*;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProjectMbtiUpdateDto {
    private Map<Long, String> mbtiMap = new HashMap<>();
}