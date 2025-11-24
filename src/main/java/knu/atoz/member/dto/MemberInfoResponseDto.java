package knu.atoz.member.dto;

import knu.atoz.project.Project;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MemberInfoResponseDto {

    private Long id;
    private String name;
    private String email;
    private LocalDate birthDate;
    private String mbti;
    private String techspecs;

    private List<Project> projects;
}