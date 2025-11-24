package knu.atoz.matching;

import jakarta.servlet.http.HttpSession;
import knu.atoz.member.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/matching")
@RequiredArgsConstructor
public class MatchingController {

    private final MatchingService matchingService;

    @GetMapping
    public String showMatchingPage(@RequestParam(required = false) String type,
                                   HttpSession session,
                                   Model model) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/members/login";
        }

        if (type != null) {
            List<MatchedProject> results = null;
            String title = "";

            switch (type) {
                case "mbti":
                    results = matchingService.getMbtiMatches(loginMember.getId());
                    title = "🧠 MBTI 성향 기반 추천";
                    break;
                case "tech":
                    results = matchingService.getTechMatches(loginMember.getId());
                    title = "💻 기술 스택 기반 추천";
                    break;
                case "combined":
                    results = matchingService.getCombinedMatches(loginMember.getId());
                    title = "✨ 종합 추천 (MBTI + Tech)";
                    break;
            }
            model.addAttribute("results", results);
            model.addAttribute("matchTitle", title);
        }

        model.addAttribute("currentType", type);

        return "matching/index";
    }
}