package knu.atoz.member;

import jakarta.servlet.http.HttpSession;
import knu.atoz.member.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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

    @GetMapping("/mypage")
    public String showMyInfo(HttpSession session, Model model) {

        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/members/login";
        }

        MemberInfoResponseDto infoDto = memberService.getAllInfo(loginMember.getId());

        model.addAttribute("info", infoDto);

        return "member/mypage";
    }

    // 1. 수정 페이지 이동
    @GetMapping("/edit")
    public String showEditForm(HttpSession session, Model model) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/members/login";
        }

        // 현재 정보를 DTO에 담아서 화면으로 보냄 (기존 값을 input에 채워놓기 위함)
        // 비밀번호는 비워둠
        MemberUpdateRequestDto dto = new MemberUpdateRequestDto(
                loginMember.getEmail(),
                loginMember.getName(),
                loginMember.getBirthDate()
        );

        model.addAttribute("updateDto", dto);
        return "member/edit";
    }

    // 2. 수정 처리
    @PostMapping("/edit")
    public String updateMember(@ModelAttribute MemberUpdateRequestDto dto,
                               HttpSession session,
                               Model model) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/members/login";
        }

        try {
            // 서비스 호출 및 업데이트된 회원 정보 받기
            Member updatedMember = memberService.updateMember(loginMember.getId(), dto);

            // ★ 세션 정보 갱신 (이걸 안 하면 로그아웃 했다 들어와야 바뀐게 보임)
            session.setAttribute("loginMember", updatedMember);

            return "redirect:/members/mypage"; // 마이페이지로 이동

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("updateDto", dto); // 입력했던 값 유지
            return "member/edit";
        }
    }

    // 1. 비밀번호 변경 페이지 보여주기 (GET)
    @GetMapping("/password")
    public String showPasswordChangeForm(HttpSession session, Model model) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/members/login";
        }

        model.addAttribute("passwordDto", new PasswordUpdateRequestDto());
        return "member/edit-password"; // templates/member/password.html
    }

    // 2. 비밀번호 변경 처리 (POST)
    @PostMapping("/password")
    public String processPasswordChange(@ModelAttribute PasswordUpdateRequestDto dto,
                                        HttpSession session,
                                        Model model) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/members/login";
        }

        try {
            // 서비스 호출 (세션에 있는 ID 사용)
            memberService.editPassword(loginMember.getId(), dto);

            // 변경 성공 시 마이페이지로 이동 (또는 로그아웃 시킬 수도 있음)
            return "redirect:/members/mypage";

        } catch (Exception e) {
            // 비밀번호 불일치 등의 에러 발생 시 다시 폼으로 이동
            model.addAttribute("error", "비밀번호 변경 실패: " + e.getMessage());
            model.addAttribute("passwordDto", dto);
            return "member/edit-password";
        }
    }
}