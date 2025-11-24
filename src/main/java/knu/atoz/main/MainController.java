package knu.atoz.main;

import knu.atoz.project.Project;
import knu.atoz.project.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;


@Controller
@RequiredArgsConstructor
public class MainController {

    private final ProjectService projectService;

    @GetMapping("/")
    public String index(Model model) {
        List<Project> projectList = projectService.getProjectList(10);

        // "projects"라는 이름으로 HTML에 전달
        model.addAttribute("projects", projectList);

        return "index";
    }

}