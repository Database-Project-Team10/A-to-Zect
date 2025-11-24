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
    private final ProjectService projectService; // 프로젝트 정보 확인용

    // 1. 관리 페이지 보여주기 (목록 + 추가폼)
    @GetMapping
    public String showManagePage(@PathVariable Long projectId,
                                 HttpSession session,
                                 Model model) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/members/login";

        try {
            // 본인 프로젝트인지 등 권한 체크 및 프로젝트 정보 조회
            Project project = projectService.getMyProjectById(loginMember, projectId);
            model.addAttribute("project", project);

            // 현재 등록된 기술 스택 목록 조회
            List<Techspec> techspecs = projectTechspecService.getProjectTechspecs(projectId);
            model.addAttribute("techspecs", techspecs);

            return "project/techspec-manage"; // 뷰 이름

        } catch (Exception e) {
            return "redirect:/projects/my?error=" + encode(e.getMessage());
        }
    }

    // 2. 기술 스택 추가 (POST)
    @PostMapping("/add")
    public String addTechspec(@PathVariable Long projectId,
                              @RequestParam String techName,
                              HttpSession session) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/members/login";

        try {
            // 서비스 호출
            projectTechspecService.addTechspecToProject(projectId, techName.trim());
            return "redirect:/projects/" + projectId + "/techspecs";

        } catch (TechspecException e) {
            return "redirect:/projects/" + projectId + "/techspecs?error=" + encode(e.getMessage());
        }
    }

    // 3. 기술 스택 삭제 (POST)
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

    // 한글 인코딩 헬퍼 메서드
    private String encode(String text) {
        return URLEncoder.encode(text, StandardCharsets.UTF_8);
    }
}