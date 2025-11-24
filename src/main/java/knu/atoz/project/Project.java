package knu.atoz.project;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Project {
    private Long id;
    private String title;
    private String description;
    private Integer currentCount;
    private Integer maxCount;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    public Project(Long id, String title, String description, Integer currentCount, Integer maxCount, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.currentCount = currentCount;
        this.maxCount = maxCount;
        this.createdAt = createdAt;
        this.modifiedAt = LocalDateTime.now();
    }

    public Project(String title, String description, Integer maxCount) {
        this.id = null;
        this.title = title;
        this.description = description;
        this.currentCount = 1;
        this.maxCount = maxCount;
        this.createdAt = LocalDateTime.now();
        this.modifiedAt = LocalDateTime.now();
    }
}
