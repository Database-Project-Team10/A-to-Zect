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

    // 1. MBTI 수정 화면 (GET)
    @GetMapping
    public String showMbtiForm(@PathVariable Long projectId,
                               HttpSession session,
                               Model model) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/members/login";

        try {
            // (1) 권한 체크 및 프로젝트 정보
            Project project = projectService.getMyProjectById(loginMember.getId(), projectId);
            model.addAttribute("project", project);

            // (2) MBTI 차원 정보 (E/I, S/N...)
            List<MbtiDimension> dimensions = projectMbtiService.getMbtiDimensions();
            model.addAttribute("dimensions", dimensions);

            // (3) 현재 설정된 MBTI 값 가져오기
            Map<Long, String> currentMbti = projectMbtiService.getMbtiMapByProjectId(projectId);

            // (4) DTO에 담기
            ProjectMbtiUpdateDto dto = new ProjectMbtiUpdateDto();
            dto.setMbtiMap(currentMbti);
            model.addAttribute("mbtiDto", dto);

            return "project/mbti";

        } catch (Exception e) {
            return "redirect:/projects/my?error=" + encode(e.getMessage());
        }
    }

    // 2. MBTI 저장 (POST)
    @PostMapping
    public String saveMbti(@PathVariable Long projectId,
                           @ModelAttribute ProjectMbtiUpdateDto dto,
                           HttpSession session) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/members/login";

        try {
            // 저장 서비스 호출
            projectMbtiService.saveProjectMbti(projectId, dto.getMbtiMap());
            return "redirect:/projects/" + projectId; // 상세 페이지로 이동

        } catch (Exception e) {
            return "redirect:/projects/" + projectId + "/mbti?error=" + encode(e.getMessage());
        }
    }

    private String encode(String text) {
        return URLEncoder.encode(text, StandardCharsets.UTF_8);
    }
}