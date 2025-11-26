package knu.atoz.link;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import knu.atoz.link.dto.LinkRequestDto;
import knu.atoz.member.Member;
import knu.atoz.participant.ParticipantService;
import knu.atoz.project.Project;
import knu.atoz.project.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
@RequestMapping("/projects/{projectId}/links")
@RequiredArgsConstructor
public class LinkController {

    private final LinkService linkService;
    private final ProjectService projectService;
    private final ParticipantService participantService;

    // 링크 목록 조회
    @GetMapping
    public String listLinks(@PathVariable Long projectId,
                            HttpSession session,
                            Model model) {
        Member loginMember = getLoginMember(session);
        if (loginMember == null) return "redirect:/members/login";

        if (!isTeamMember(projectId, loginMember.getId())) {
            return "redirect:/projects/" + projectId + "?error=" + encode("접근 권한이 없습니다.");
        }

        try {
            Project project = projectService.getProject(projectId);
            List<Link> links = linkService.getLinksByProject(projectId);

            model.addAttribute("project", project);
            model.addAttribute("links", links);

            return "link/list";

        } catch (Exception e) {
            return "redirect:/projects/" + projectId + "?error=" + encode(e.getMessage());
        }
    }

    // 링크 생성 폼
    @GetMapping("/new")
    public String showCreateForm(@PathVariable Long projectId,
                                 HttpSession session,
                                 Model model) {
        Member loginMember = getLoginMember(session);
        if (loginMember == null) return "redirect:/members/login";

        if (!isTeamMember(projectId, loginMember.getId())) {
            return "redirect:/projects/" + projectId + "/links?error=" + encode("링크 등록 권한이 없습니다.");
        }

        Project project = projectService.getProject(projectId);

        model.addAttribute("project", project);
        model.addAttribute("linkDto", new LinkRequestDto());
        model.addAttribute("isNew", true);

        return "link/form";
    }

    @PostMapping("/new")
    public String createLink(@PathVariable Long projectId,
                             @Valid @ModelAttribute("linkDto") LinkRequestDto dto,
                             BindingResult bindingResult,
                             HttpSession session,
                             Model model) {
        Member loginMember = getLoginMember(session);
        if (loginMember == null) return "redirect:/members/login";

        if (bindingResult.hasErrors()) {
            model.addAttribute("project", projectService.getProject(projectId));
            model.addAttribute("isNew", true);
            return "link/form";
        }

        try {
            linkService.createLink(projectId, dto);
            return "redirect:/projects/" + projectId + "/links";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("project", projectService.getProject(projectId));
            model.addAttribute("isNew", true);
            return "link/form";
        }
    }

    // 링크 수정 폼
    @GetMapping("/{linkId}/edit")
    public String showEditForm(@PathVariable Long projectId,
                               @PathVariable Long linkId,
                               HttpSession session,
                               Model model) {
        Member loginMember = getLoginMember(session);
        if (loginMember == null) return "redirect:/members/login";

        if (!isTeamMember(projectId, loginMember.getId())) {
            return "redirect:/projects/" + projectId + "/links?error=" + encode("링크 수정 권한이 없습니다.");
        }

        try {
            Project project = projectService.getProject(projectId);
            Link link = linkService.getLink(linkId);

            LinkRequestDto dto = new LinkRequestDto(link.getTitle(), link.getUrl());

            model.addAttribute("project", project);
            model.addAttribute("link", link);
            model.addAttribute("linkDto", dto);
            model.addAttribute("isNew", false);

            return "link/form";

        } catch (Exception e) {
            return "redirect:/projects/" + projectId + "/links?error=" + encode(e.getMessage());
        }
    }

    @PostMapping("/{linkId}/edit")
    public String updateLink(@PathVariable Long projectId,
                             @PathVariable Long linkId,
                             @Valid @ModelAttribute("linkDto") LinkRequestDto dto,
                             BindingResult bindingResult,
                             HttpSession session,
                             Model model) {
        Member loginMember = getLoginMember(session);
        if (loginMember == null) return "redirect:/members/login";

        if (bindingResult.hasErrors()) {
            model.addAttribute("project", projectService.getProject(projectId));
            model.addAttribute("isNew", false);
            try {
                model.addAttribute("link", linkService.getLink(linkId));
            } catch (Exception ignored) {}

            return "link/form";
        }

        try {
            linkService.updateLink(linkId, projectId, dto);
            return "redirect:/projects/" + projectId + "/links";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("project", projectService.getProject(projectId));
            model.addAttribute("isNew", false);

            try {
                model.addAttribute("link", linkService.getLink(linkId));
            } catch (Exception ignored) {}

            return "link/form";
        }
    }

    @PostMapping("/{linkId}/delete")
    public String deleteLink(@PathVariable Long projectId,
                             @PathVariable Long linkId,
                             HttpSession session) {
        Member loginMember = getLoginMember(session);
        if (loginMember == null) return "redirect:/members/login";

        if (!isTeamMember(projectId, loginMember.getId())) {
            return "redirect:/projects/" + projectId + "/links?error=" + encode("링크 삭제 권한이 없습니다.");
        }

        try {
            linkService.deleteLink(linkId, projectId);
        } catch (Exception e) {
            return "redirect:/projects/" + projectId + "/links?error=" + encode(e.getMessage());
        }
        return "redirect:/projects/" + projectId + "/links";
    }

    // 유틸리티 메서드
    private Member getLoginMember(HttpSession session) {
        return (Member) session.getAttribute("loginMember");
    }

    private boolean isTeamMember(Long projectId, Long memberId) {
        String role = participantService.getMyRole(projectId, memberId);
        return "LEADER".equals(role) || "MEMBER".equals(role);
    }

    private String encode(String text) {
        return URLEncoder.encode(text, StandardCharsets.UTF_8);
    }
}