package knu.atoz.member;

import jakarta.servlet.http.HttpSession;
import knu.atoz.member.dto.SignupRequestDto;
import knu.atoz.member.dto.LoginRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    // 1. 회원가입 '페이지' 보여주기 (GET)
    @GetMapping("/signup")
    public String showSignupForm(Model model) {
        // 빈 객체를 보내서 폼과 바인딩 (Thymeleaf 관례)
        model.addAttribute("signupDto", new SignupRequestDto());
        return "member/signup"; // resources/templates/member/signup.html을 찾아감
    }

    // 2. 회원가입 '처리' 하기 (POST)
    @PostMapping("/signup")
    public String processSignup(@ModelAttribute SignupRequestDto dto) {
        memberService.signUp(dto);
        return "redirect:/members/login"; // 가입 성공 후 로그인 페이지로 이동 (URL 리다이렉트)
    }

    // 3. 로그인 '페이지' 보여주기
    @GetMapping("/login")
    public String showLoginForm(Model model) { // [2] 파라미터에 Model 추가
        // [3] 빈 껍데기 객체를 만들어서 "loginDto"라는 이름표를 붙여서 보냄
        model.addAttribute("loginDto", new LoginRequestDto());

        return "member/login";
    }

    // 4. 로그인 '처리' 하기
    @PostMapping("/login")
    public String processLogin(@ModelAttribute LoginRequestDto dto, HttpSession session, Model model) {
        try {
            Member member = memberService.login(dto.getEmail(), dto.getPassword());

            session.setAttribute("loginMember", member);
            return "redirect:/";

        } catch (Exception e) {
            model.addAttribute("loginDto", dto);

            // 에러 메시지 전달
            model.addAttribute("error", "아이디 또는 비밀번호가 올바르지 않습니다.");
            return "member/login";
        }
    }

    // 5. 로그아웃
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); // 세션 날리기
        return "redirect:/";
    }
}