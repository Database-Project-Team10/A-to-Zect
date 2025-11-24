package knu.atoz.project.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Map;
import java.util.Set;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class ProjectCreateRequestDto {
    private Long memberId;
    private String title;
    private String description;
    private Set<String> techSpecs;
    private Map<Long, String> mbtiMap;
}
