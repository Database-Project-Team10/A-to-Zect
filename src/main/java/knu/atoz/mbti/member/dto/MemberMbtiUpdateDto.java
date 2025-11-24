package knu.atoz.mbti.member.dto;

import lombok.*;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MemberMbtiUpdateDto {
    private Map<Long, String> mbtiMap = new HashMap<>();
}