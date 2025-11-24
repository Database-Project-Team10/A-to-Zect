package knu.atoz.techspec.member;

import jakarta.servlet.http.HttpSession;
import knu.atoz.member.Member;
import knu.atoz.techspec.Techspec;
import knu.atoz.techspec.exception.TechspecException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
@RequestMapping("/members/techspecs")
@RequiredArgsConstructor
public class MemberTechspecController {

    private final MemberTechspecService memberTechspecService;

    
    @GetMapping
    public String showTechspecManagePage(HttpSession session, Model model) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/members/login";

        
        List<Techspec> myTechspecs = memberTechspecService.getMyTechspecs(loginMember.getId());
        model.addAttribute("techspecs", myTechspecs);

        return "member/techspec"; 
    }

    
    @PostMapping("/add")
    public String addTechspec(@RequestParam String techName, HttpSession session) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/members/login";

        try {
            memberTechspecService.addTechspec(loginMember.getId(), techName.trim());
            return "redirect:/members/techspecs";
        } catch (TechspecException e) {
            return "redirect:/members/techspecs?error=" + encode(e.getMessage());
        }
    }

    
    @PostMapping("/{techspecId}/delete")
    public String removeTechspec(@PathVariable Long techspecId, HttpSession session) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/members/login";

        try {
            memberTechspecService.removeTechspec(loginMember.getId(), techspecId);
            return "redirect:/members/techspecs";
        } catch (Exception e) {
            return "redirect:/members/techspecs?error=" + encode(e.getMessage());
        }
    }

    private String encode(String text) {
        return URLEncoder.encode(text, StandardCharsets.UTF_8);
    }
}