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

    // 1. MBTI 수정 화면 보여주기
    @GetMapping
    public String showMbtiForm(HttpSession session, Model model) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/members/login";
        }

        // (1) MBTI 차원 정보 가져오기 (E/I, S/N 등 질문지 생성용)
        List<MbtiDimension> dimensions = memberMbtiService.getMbtiDimensions();
        model.addAttribute("dimensions", dimensions);

        // (2) 현재 회원의 MBTI 정보 가져오기 (기존 값 체크용)
        Map<Long, String> currentMbti = memberMbtiService.getMbtiMapByMemberId(loginMember.getId());

        // (3) DTO에 담아서 전달
        MemberMbtiUpdateDto dto = new MemberMbtiUpdateDto();
        dto.setMbtiMap(currentMbti);
        model.addAttribute("mbtiDto", dto);

        return "member/mbti"; // mbti.html
    }

    // 2. MBTI 저장 처리
    @PostMapping
    public String saveMbti(@ModelAttribute MemberMbtiUpdateDto dto,
                           HttpSession session) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/members/login";
        }

        try {
            // 서비스 호출하여 저장
            memberMbtiService.saveMyMbti(loginMember.getId(), dto.getMbtiMap());
            return "redirect:/members/mypage"; // 저장 후 마이페이지로 이동

        } catch (Exception e) {
            // 에러 발생 시 다시 폼으로 (여기선 편의상 리다이렉트 처리)
            return "redirect:/members/mbti?error=" + e.getMessage();
        }
    }
}