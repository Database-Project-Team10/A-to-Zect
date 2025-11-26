package knu.atoz.participant;

import jakarta.servlet.http.HttpSession;
import knu.atoz.member.Member;
import knu.atoz.participant.dto.ParticipantResponseDto;
import knu.atoz.project.Project;
import knu.atoz.project.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
@RequestMapping("/projects/{projectId}/participants")
@RequiredArgsConstructor
public class ParticipantController {

    private final ParticipantService participantService;
    private final ProjectService projectService;

    
    @GetMapping("")
    public String showTeamMembers(@PathVariable Long projectId, HttpSession session, Model model) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/members/login";

        
        String myRole = participantService.getMyRole(projectId, loginMember.getId());
        if (myRole == null || "PENDING".equals(myRole)) {
            return "redirect:/projects/" + projectId + "?error=" + encode("접근 권한이 없습니다.");
        }

        Project project = projectService.getProject(projectId);
        List<ParticipantResponseDto> members = participantService.getTeamMembers(projectId);

        model.addAttribute("project", project);
        model.addAttribute("members", members);
        model.addAttribute("myRole", myRole); 

        return "participant/team";
    }

    
    @GetMapping("/applicants")
    public String showApplicants(@PathVariable Long projectId, HttpSession session, Model model) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/members/login";

        
        String myRole = participantService.getMyRole(projectId, loginMember.getId());
        if (!"LEADER".equals(myRole)) {
            return "redirect:/projects/" + projectId + "?error=" + encode("리더만 접근 가능합니다.");
        }

        Project project = projectService.getProject(projectId);
        List<Member> applicants = participantService.getPendingMembers(projectId);

        model.addAttribute("project", project);
        model.addAttribute("applicants", applicants);

        return "participant/manage";
    }

    @PostMapping("/apply")
    public String applyProject(@PathVariable Long projectId, HttpSession session) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/members/login";

        try {
            participantService.applyProject(projectId, loginMember.getId());
            return "redirect:/projects/" + projectId;
        } catch (Exception e) {
            return "redirect:/projects/" + projectId + "?error=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
        }
    }

    @PostMapping("/leave")
    public String leaveProject(@PathVariable Long projectId, HttpSession session) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/members/login";
        }

        try {
            // 서비스 호출
            participantService.leaveProject(projectId, loginMember.getId());
        } catch (Exception e) {
            // 에러 발생 시에도 일단 목록으로 리다이렉트 (필요 시 에러 파라미터 추가)
            System.err.println("나가기 실패: " + e.getMessage());
        }

        return "redirect:/projects/my";
    }

    @PostMapping("/cancel")
    public String cancelApplication(@PathVariable Long projectId, HttpSession session) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/members/login";
        }

        try {
            participantService.cancelApplication(projectId, loginMember.getId());
        } catch (Exception e) {
            System.err.println("신청 취소 실패: " + e.getMessage());
        }

        return "redirect:/projects/my";
    }
    
    @PostMapping("/{memberId}/kick")
    public String kickMember(@PathVariable Long projectId, @PathVariable Long memberId) {
        try {
            participantService.kickMember(projectId, memberId);
            return "redirect:/projects/" + projectId + "/participants";
        } catch (Exception e) {
            return "redirect:/projects/" + projectId + "/participants?error=" + encode(e.getMessage());
        }
    }

    
    @PostMapping("/{memberId}/accept")
    public String acceptMember(@PathVariable Long projectId,
                               @PathVariable Long memberId,
                               HttpSession session) {
        try {
            participantService.acceptMember(projectId, memberId);
            return "redirect:/projects/" + projectId + "/participants/applicants";
        } catch (Exception e) {
            return "redirect:/projects/" + projectId + "/participants/applicants?error=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
        }
    }

    
    @PostMapping("/{memberId}/reject")
    public String rejectMember(@PathVariable Long projectId,
                               @PathVariable Long memberId) {
        try {
            participantService.rejectMember(projectId, memberId);
            return "redirect:/projects/" + projectId + "/participants/applicants";
        } catch (Exception e) {
            return "redirect:/projects/" + projectId + "/participants/applicants?error=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
        }
    }

    private String encode(String text) {
        return URLEncoder.encode(text, StandardCharsets.UTF_8);
    }
}