package knu.atoz.post;

import jakarta.servlet.http.HttpSession;
import knu.atoz.member.Member;
import knu.atoz.member.MemberService;
import knu.atoz.participant.ParticipantService;
import knu.atoz.post.dto.PostRequestDto;
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
@RequestMapping("/projects/{projectId}/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final ProjectService projectService;
    private final ParticipantService participantService;
    private final MemberService memberService;

    // 1. 게시글 목록
    @GetMapping
    public String listPosts(@PathVariable Long projectId, HttpSession session, Model model) {
        Member loginMember = checkLogin(session);
        if (loginMember == null) return "redirect:/members/login";

        if (!isTeamMember(projectId, loginMember.getId())) {
            return "redirect:/projects/" + projectId + "?error=" + encode("팀원만 접근 가능합니다.");
        }

        Project project = projectService.getProject(projectId);
        List<Post> posts = postService.getPostList(projectId);

        model.addAttribute("project", project);
        model.addAttribute("posts", posts);

        return "post/list";
    }

    // 2. 게시글 상세
    @GetMapping("/{postId}")
    public String viewPost(@PathVariable Long projectId,
                           @PathVariable Long postId,
                           HttpSession session,
                           Model model) {

        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/members/login";

        // 권한 체크 (팀원인지)
        if (!isTeamMember(projectId, loginMember.getId())) {
            return "redirect:/projects/" + projectId + "?error=" + encode("접근 권한이 없습니다.");
        }

        try {
            Post post = postService.getPost(postId);
            Project project = projectService.getProject(projectId);

            model.addAttribute("post", post);
            model.addAttribute("project", project);
            model.addAttribute("loginMemberId", loginMember.getId());

            String authorName = memberService.getMemberName(post.getMemberId());
            model.addAttribute("authorName", authorName);

            return "post/detail";

        } catch (Exception e) {
            return "redirect:/projects/" + projectId + "/posts?error=" + encode(e.getMessage());
        }
    }

    // 3. 글쓰기 폼
    @GetMapping("/new")
    public String createForm(@PathVariable Long projectId, HttpSession session, Model model) {
        if (checkLogin(session) == null) return "redirect:/members/login";

        model.addAttribute("project", projectService.getProject(projectId));
        model.addAttribute("postDto", new PostRequestDto());
        model.addAttribute("isNew", true);
        return "post/form";
    }

    // 4. 글쓰기 처리
    @PostMapping("/new")
    public String createPost(@PathVariable Long projectId, @ModelAttribute PostRequestDto dto, HttpSession session) {
        Member loginMember = checkLogin(session);
        if (loginMember == null) return "redirect:/members/login";

        try {
            postService.createPost(projectId, loginMember.getId(), dto);
            return "redirect:/projects/" + projectId + "/posts";
        } catch (Exception e) {
            return "redirect:/projects/" + projectId + "/posts/new?error=" + encode(e.getMessage());
        }
    }

    // 5. 수정 폼
    @GetMapping("/{postId}/edit")
    public String editForm(@PathVariable Long projectId, @PathVariable Long postId, HttpSession session, Model model) {
        Member loginMember = checkLogin(session);
        if (loginMember == null) return "redirect:/members/login";

        Post post = postService.getPost(postId);
        if (!post.getMemberId().equals(loginMember.getId())) {
            return "redirect:/projects/" + projectId + "/posts/" + postId + "?error=" + encode("수정 권한이 없습니다.");
        }

        PostRequestDto dto = new PostRequestDto(post.getTitle(), post.getContent());
        model.addAttribute("project", projectService.getProject(projectId));
        model.addAttribute("postDto", dto);
        model.addAttribute("postId", postId); // 수정 시 ID 필요
        model.addAttribute("isNew", false);

        return "post/form";
    }

    // 6. 수정 처리
    @PostMapping("/{postId}/edit")
    public String updatePost(@PathVariable Long projectId, @PathVariable Long postId,
                             @ModelAttribute PostRequestDto dto, HttpSession session) {
        Member loginMember = checkLogin(session);
        if (loginMember == null) return "redirect:/members/login";

        try {
            postService.updatePost(postId, loginMember.getId(), dto);
            return "redirect:/projects/" + projectId + "/posts/" + postId;
        } catch (Exception e) {
            return "redirect:/projects/" + projectId + "/posts/" + postId + "/edit?error=" + encode(e.getMessage());
        }
    }

    // 7. 삭제 처리
    @PostMapping("/{postId}/delete")
    public String deletePost(@PathVariable Long projectId, @PathVariable Long postId, HttpSession session) {
        Member loginMember = checkLogin(session);
        if (loginMember == null) return "redirect:/members/login";

        try {
            postService.deletePost(postId, loginMember.getId());
            return "redirect:/projects/" + projectId + "/posts";
        } catch (Exception e) {
            return "redirect:/projects/" + projectId + "/posts/" + postId + "?error=" + encode(e.getMessage());
        }
    }

    // --- Helpers ---
    private Member checkLogin(HttpSession session) {
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