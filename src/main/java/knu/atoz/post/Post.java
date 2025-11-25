package knu.atoz.post;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class Post {
    private final Long id;
    private final Long projectId;
    private final Long memberId;

    private final String title;
    private final String content;
    private final LocalDateTime createdAt;
    private final LocalDateTime modifiedAt;

    public Post(Long projectId, Long memberId, String title, String content) {
        this.id = null;
        this.projectId = projectId;
        this.memberId = memberId;
        this.title = title;
        this.content = content;
        this.createdAt = LocalDateTime.now();
        this.modifiedAt = LocalDateTime.now();
    }

}
