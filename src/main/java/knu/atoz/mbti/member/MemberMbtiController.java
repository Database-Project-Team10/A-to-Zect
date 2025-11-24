package knu.atoz.mbti.member;

import jakarta.servlet.http.HttpSession;
import knu.atoz.mbti.MbtiDimension;
import knu.atoz.mbti.member.dto.MemberMbtiUpdateDto;
import knu.atoz.member.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/members/mbti")
@RequiredArgsConstructor
public class MemberMbtiController {

    private final MemberMbtiService memberMbtiService;

    @GetMapping
    public String showMbtiForm(HttpSession session, Model model) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/members/login";
        }

        List<MbtiDimension> dimensions = memberMbtiService.getMbtiDimensions();
        model.addAttribute("dimensions", dimensions);

        Map<Long, String> currentMbti = memberMbtiService.getMbtiMapByMemberId(loginMember.getId());

        MemberMbtiUpdateDto dto = new MemberMbtiUpdateDto();
        dto.setMbtiMap(currentMbti);
        model.addAttribute("mbtiDto", dto);

        return "member/mbti";
    }

    @PostMapping
    public String saveMbti(@ModelAttribute MemberMbtiUpdateDto dto,
                           HttpSession session) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/members/login";
        }

        try {
            memberMbtiService.saveMyMbti(loginMember.getId(), dto.getMbtiMap());
            return "redirect:/members/mypage";

        } catch (Exception e) {
            return "redirect:/members/mbti?error=" + e.getMessage();
        }
    }
}