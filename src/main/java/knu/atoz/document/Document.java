package knu.atoz.document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Document {
    Long id;
    Long projectId;
    String title;
    String location;

    public Document(Long projectId, String title, String location) {
        this.projectId = projectId;
        this.title = title;
        this.location = location;
    }
}
