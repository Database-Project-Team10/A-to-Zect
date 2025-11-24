package knu.atoz.member;

import jakarta.servlet.http.HttpSession;
import knu.atoz.member.dto.*;
import knu.atoz.project.Project;
import knu.atoz.project.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final ProjectService projectService;

    
    @GetMapping("/signup")
    public String showSignupForm(Model model) {
        
        model.addAttribute("signupDto", new SignupRequestDto());
        return "member/signup"; 
    }

    
    @PostMapping("/signup")
    public String processSignup(@ModelAttribute SignupRequestDto dto) {
        memberService.signUp(dto);
        return "redirect:/members/login"; 
    }

    
    @GetMapping("/login")
    public String showLoginForm(Model model) { 
        
        model.addAttribute("loginDto", new LoginRequestDto());

        return "member/login";
    }

    
    @PostMapping("/login")
    public String processLogin(@ModelAttribute LoginRequestDto dto, HttpSession session, Model model) {
        try {
            Member member = memberService.login(dto.getEmail(), dto.getPassword());

            session.setAttribute("loginMember", member);
            return "redirect:/";

        } catch (Exception e) {
            model.addAttribute("loginDto", dto);

            
            model.addAttribute("error", "아이디 또는 비밀번호가 올바르지 않습니다.");
            return "member/login";
        }
    }

    
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); 
        return "redirect:/";
    }

    @GetMapping("/mypage")
    public String showMyInfo(HttpSession session, Model model) {

        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/members/login";
        }

        MemberInfoResponseDto infoDto = memberService.getAllInfo(loginMember.getId());

        List<Project> myProjects = projectService.getMyProjectList(loginMember.getId());

        infoDto.setProjects(myProjects);

        model.addAttribute("info", infoDto);

        return "member/mypage";
    }

    
    @GetMapping("/edit")
    public String showEditForm(HttpSession session, Model model) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/members/login";
        }

        
        
        MemberUpdateRequestDto dto = new MemberUpdateRequestDto(
                loginMember.getEmail(),
                loginMember.getName(),
                loginMember.getBirthDate()
        );

        model.addAttribute("updateDto", dto);
        return "member/edit";
    }

    
    @PostMapping("/edit")
    public String updateMember(@ModelAttribute MemberUpdateRequestDto dto,
                               HttpSession session,
                               Model model) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/members/login";
        }

        try {
            
            Member updatedMember = memberService.updateMember(loginMember.getId(), dto);

            
            session.setAttribute("loginMember", updatedMember);

            return "redirect:/members/mypage"; 

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("updateDto", dto); 
            return "member/edit";
        }
    }

    
    @GetMapping("/password")
    public String showPasswordChangeForm(HttpSession session, Model model) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/members/login";
        }

        model.addAttribute("passwordDto", new PasswordUpdateRequestDto());
        return "member/edit-password"; 
    }

    
    @PostMapping("/password")
    public String processPasswordChange(@ModelAttribute PasswordUpdateRequestDto dto,
                                        HttpSession session,
                                        Model model) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/members/login";
        }

        try {
            
            memberService.editPassword(loginMember.getId(), dto);

            
            return "redirect:/members/mypage";

        } catch (Exception e) {
            
            model.addAttribute("error", "비밀번호 변경 실패: " + e.getMessage());
            model.addAttribute("passwordDto", dto);
            return "member/edit-password";
        }
    }
}