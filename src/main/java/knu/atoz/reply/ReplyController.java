package knu.atoz.reply;

import jakarta.servlet.http.HttpSession;
import knu.atoz.member.Member;
import knu.atoz.reply.dto.ReplyRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/projects/{projectId}/posts/{postId}/replies")
@RequiredArgsConstructor
public class ReplyController {

    private final ReplyService replyService;

    // 1. 댓글 작성 (POST)
    @PostMapping
    public String createReply(@PathVariable Long projectId,
                              @PathVariable Long postId,
                              @ModelAttribute ReplyRequestDto dto,
                              HttpSession session) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/members/login";

        try {
            replyService.createReply(postId, loginMember.getId(), dto);
            return "redirect:/projects/" + projectId + "/posts/" + postId; // 게시글 상세로 복귀
        } catch (Exception e) {
            return "redirect:/projects/" + projectId + "/posts/" + postId + "?error=" + encode(e.getMessage());
        }
    }

    // 2. 댓글 삭제 (POST)
    @PostMapping("/{replyId}/delete")
    public String deleteReply(@PathVariable Long projectId,
                              @PathVariable Long postId,
                              @PathVariable Long replyId,
                              HttpSession session) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/members/login";

        try {
            replyService.deleteReply(replyId, loginMember.getId());
            return "redirect:/projects/" + projectId + "/posts/" + postId;
        } catch (Exception e) {
            return "redirect:/projects/" + projectId + "/posts/" + postId + "?error=" + encode(e.getMessage());
        }
    }

    private String encode(String text) {
        return URLEncoder.encode(text, StandardCharsets.UTF_8);
    }
}