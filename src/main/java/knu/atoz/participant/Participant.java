package knu.atoz.participant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Participant {
    private Long memberId;
    private Long projectId;
    private String role;
}
