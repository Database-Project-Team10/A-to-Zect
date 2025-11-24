package knu.atoz.project;

import jakarta.servlet.http.HttpSession;
import knu.atoz.mbti.project.ProjectMbtiService;
import knu.atoz.member.Member;
import knu.atoz.participant.ParticipantService;
import knu.atoz.participant.exception.ParticipantException;
import knu.atoz.project.dto.MyProjectResponseDto;
import knu.atoz.project.dto.ProjectCreateRequestDto;
import knu.atoz.project.dto.ProjectUpdateRequestDto;
import knu.atoz.project.exception.ProjectException;
import knu.atoz.techspec.Techspec;
import knu.atoz.techspec.project.ProjectTechspecService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final ParticipantService participantService;
    private final ProjectTechspecService projectTechspecService;
    private final ProjectMbtiService projectMbtiService;

    @GetMapping("")
    public String showAllProjects(Model model) {

        List<Project> projectList = projectService.getAllProjects();

        
        model.addAttribute("projects", projectList);

        return "project/list";
    }

    
    @GetMapping("/create")
    public String showCreateForm(HttpSession session, Model model) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/members/login";
        }

        model.addAttribute("projectDto", new ProjectCreateRequestDto());
        return "project/create";
    }

    
    @PostMapping("/create")
    public String createProject(@ModelAttribute ProjectCreateRequestDto dto,
                                HttpSession session,
                                Model model) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/members/login";
        }

        try {
            
            dto.setMemberId(loginMember.getId());

            
            projectService.createProject(dto);

            return "redirect:/"; 

        } catch (ProjectException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("projectDto", dto); 
            return "project/create";
        }
    }

    
    @PostMapping("/{projectId}/join")
    public String joinProject(@PathVariable Long projectId,
                              HttpSession session,
                              Model model) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/members/login";
        }

        try {
            participantService.joinProject(projectId, loginMember.getId());
            return "redirect:/projects/my"; 
        } catch (ParticipantException | ProjectException e) {
            
            return "redirect:/projects?error=" + e.getMessage();
        }
    }

    
    @GetMapping("/my")
    public String showMyProjects(HttpSession session, Model model) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/members/login";
        }

        
        List<MyProjectResponseDto> myProjects = projectService.getMyProjectListAndRole(loginMember.getId());
        model.addAttribute("projects", myProjects);

        return "project/my-list";
    }

    
    @PostMapping("/{projectId}/delete")
    public String deleteProject(@PathVariable Long projectId, HttpSession session) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/members/login";
        }

        
        try {
            projectService.deleteProject(projectId, loginMember.getId());
        } catch (Exception e) {
            
        }

        return "redirect:/projects/my";
    }

    @GetMapping("/{projectId}")
    public String showProjectDetail(@PathVariable Long projectId,
                                    HttpSession session,
                                    Model model) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/members/login";
        }

        try {
            Project project = projectService.getProject(projectId);
            model.addAttribute("project", project);

            String myRole = participantService.getMyRole(projectId, loginMember.getId());
            model.addAttribute("myRole", myRole);

            List<Techspec> techSpecs = projectTechspecService.getProjectTechspecs(projectId);
            model.addAttribute("techSpecs", techSpecs);

            Map<Long, String> mbtiMap = projectMbtiService.getMbtiMapByProjectId(projectId);
            model.addAttribute("mbtiMap", mbtiMap);

            return "project/detail";

        } catch (Exception e) {
            return "redirect:/projects/my?error=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
        }
    }

    @GetMapping("/{projectId}/edit")
    public String showEditForm(@PathVariable Long projectId,
                               HttpSession session,
                               Model model) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/members/login";
        }

        try {

            Project project = projectService.getMyProjectById(loginMember.getId(), projectId);

            ProjectUpdateRequestDto updateDto = new ProjectUpdateRequestDto(
                    project.getTitle(),
                    project.getDescription(),
                    project.getMaxCount()
            );

            model.addAttribute("updateDto", updateDto);
            model.addAttribute("projectId", projectId);

            return "project/edit";

        } catch (Exception e) {
            return "redirect:/projects/my?error=" + URLEncoder.encode("수정 권한이 없거나 존재하지 않는 프로젝트입니다.", StandardCharsets.UTF_8);
        }
    }

    @PostMapping("/{projectId}/edit")
    public String updateProject(@PathVariable Long projectId,
                                @ModelAttribute ProjectUpdateRequestDto dto,
                                HttpSession session,
                                Model model) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/members/login";
        }

        try {
            projectService.updateProjectInfo(projectId, loginMember.getId(), dto);

            return "redirect:/projects/" + projectId;

        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("updateDto", dto); 
            model.addAttribute("projectId", projectId);
            return "project/edit";
        }
    }

    @PostMapping("/{projectId}/apply")
    public String applyProject(@PathVariable Long projectId, HttpSession session) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/members/login";

        try {
            participantService.applyProject(projectId, loginMember.getId());
            return "redirect:/projects/" + projectId;
        } catch (Exception e) {
            return "redirect:/projects/" + projectId + "?error=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
        }
    }

    
    @GetMapping("/{projectId}/manage")
    public String manageParticipants(@PathVariable Long projectId, HttpSession session, Model model) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) return "redirect:/members/login";

        try {
            
            String myRole = participantService.getMyRole(projectId, loginMember.getId());
            if (!"LEADER".equals(myRole)) {
                throw new RuntimeException("관리자 권한이 없습니다.");
            }

            
            Project project = projectService.getProject(projectId);
            List<Member> pendingMembers = participantService.getPendingMembers(projectId);

            model.addAttribute("project", project);
            model.addAttribute("pendingList", pendingMembers);

            return "participant/manage";

        } catch (Exception e) {
            return "redirect:/projects/" + projectId + "?error=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
        }
    }

    
    @PostMapping("/{projectId}/accept/{targetMemberId}")
    public String acceptMember(@PathVariable Long projectId,
                               @PathVariable Long targetMemberId,
                               HttpSession session) {
        
        try {
            participantService.acceptMember(projectId, targetMemberId);
            return "redirect:/projects/" + projectId + "/manage";
        } catch (Exception e) {
            return "redirect:/projects/" + projectId + "/manage?error=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
        }
    }

    
    @PostMapping("/{projectId}/reject/{targetMemberId}")
    public String rejectMember(@PathVariable Long projectId,
                               @PathVariable Long targetMemberId) {
        try {
            participantService.rejectMember(projectId, targetMemberId);
            return "redirect:/projects/" + projectId + "/manage";
        } catch (Exception e) {
            return "redirect:/projects/" + projectId + "/manage?error=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
        }
    }
}