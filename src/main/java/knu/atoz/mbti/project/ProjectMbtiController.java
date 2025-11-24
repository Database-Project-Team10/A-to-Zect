package knu.atoz.mbti.project;

import jakarta.servlet.http.HttpSession;
import knu.atoz.mbti.MbtiDimension;
import knu.atoz.mbti.project.dto.ProjectMbtiUpdateDto;
import knu.atoz.member.Member;
import knu.atoz.project.Project;
import knu.atoz.project.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/projects/{projectId}/mbti")
@RequiredArgsConstructor
public class ProjectMbtiController {

    private final ProjectMbtiService projectMbtiService;
    private final ProjectService projectService;

    
    @GetMapping
    public String showMbtiForm(@PathVariable Long projectId,
                               HttpSession session,
                               Model model) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/members/login";

        try {
            
            Project project = projectService.getMyProjectById(loginMember.getId(), projectId);
            model.addAttribute("project", project);

            
            List<MbtiDimension> dimensions = projectMbtiService.getMbtiDimensions();
            model.addAttribute("dimensions", dimensions);

            
            Map<Long, String> currentMbti = projectMbtiService.getMbtiMapByProjectId(projectId);

            
            ProjectMbtiUpdateDto dto = new ProjectMbtiUpdateDto();
            dto.setMbtiMap(currentMbti);
            model.addAttribute("mbtiDto", dto);

            return "project/mbti";

        } catch (Exception e) {
            return "redirect:/projects/my?error=" + encode(e.getMessage());
        }
    }

    
    @PostMapping
    public String saveMbti(@PathVariable Long projectId,
                           @ModelAttribute ProjectMbtiUpdateDto dto,
                           HttpSession session) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/members/login";

        try {
            
            projectMbtiService.saveProjectMbti(projectId, dto.getMbtiMap());
            return "redirect:/projects/" + projectId; 

        } catch (Exception e) {
            return "redirect:/projects/" + projectId + "/mbti?error=" + encode(e.getMessage());
        }
    }

    private String encode(String text) {
        return URLEncoder.encode(text, StandardCharsets.UTF_8);
    }
}