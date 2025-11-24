package knu.atoz.techspec.project;

import jakarta.servlet.http.HttpSession;
import knu.atoz.member.Member;
import knu.atoz.project.Project;
import knu.atoz.project.ProjectService;
import knu.atoz.techspec.Techspec;
import knu.atoz.techspec.exception.TechspecException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
@RequestMapping("/projects/{projectId}/techspecs")
@RequiredArgsConstructor
public class ProjectTechspecController {

    private final ProjectTechspecService projectTechspecService;
    private final ProjectService projectService; 

    
    @GetMapping
    public String showManagePage(@PathVariable Long projectId,
                                 HttpSession session,
                                 Model model) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/members/login";

        try {
            
            Project project = projectService.getMyProjectById(loginMember.getId(), projectId);
            model.addAttribute("project", project);

            
            List<Techspec> techspecs = projectTechspecService.getProjectTechspecs(projectId);
            model.addAttribute("techspecs", techspecs);

            return "project/techspec"; 

        } catch (Exception e) {
            return "redirect:/projects/my?error=" + encode(e.getMessage());
        }
    }

    
    @PostMapping("/add")
    public String addTechspec(@PathVariable Long projectId,
                              @RequestParam String techName,
                              HttpSession session) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/members/login";

        try {
            
            projectTechspecService.addTechspecToProject(projectId, techName.trim());
            return "redirect:/projects/" + projectId + "/techspecs";

        } catch (TechspecException e) {
            return "redirect:/projects/" + projectId + "/techspecs?error=" + encode(e.getMessage());
        }
    }

    
    @PostMapping("/{techspecId}/delete")
    public String removeTechspec(@PathVariable Long projectId,
                                 @PathVariable Long techspecId,
                                 HttpSession session) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/members/login";

        try {
            projectTechspecService.removeTechspecFromProject(projectId, techspecId);
            return "redirect:/projects/" + projectId + "/techspecs";

        } catch (Exception e) {
            return "redirect:/projects/" + projectId + "/techspecs?error=" + encode(e.getMessage());
        }
    }

    
    private String encode(String text) {
        return URLEncoder.encode(text, StandardCharsets.UTF_8);
    }
}