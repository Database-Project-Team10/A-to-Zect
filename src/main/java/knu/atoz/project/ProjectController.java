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

        // "projects"라는 이름으로 HTML에 전달
        model.addAttribute("projects", projectList);

        return "project/list";
    }

    // 2. 프로젝트 생성 '페이지' 보여주기 (GET /projects/create)
    @GetMapping("/create")
    public String showCreateForm(HttpSession session, Model model) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/members/login";
        }

        model.addAttribute("projectDto", new ProjectCreateRequestDto());
        return "project/create";
    }

    // 3. 프로젝트 생성 '처리' (POST /projects/create)
    @PostMapping("/create")
    public String createProject(@ModelAttribute ProjectCreateRequestDto dto,
                                HttpSession session,
                                Model model) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/members/login";
        }

        try {
            // 작성자 ID 주입
            dto.setMemberId(loginMember.getId());

            // 서비스 호출 (DTO 안에 테크스펙, MBTI 정보가 다 들어있다고 가정)
            projectService.createProject(dto);

            return "redirect:/"; // 목록으로 이동

        } catch (ProjectException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("projectDto", dto); // 입력했던 내용 유지
            return "project/create";
        }
    }

    // 4. 프로젝트 참여하기 (POST /projects/{id}/join)
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
            return "redirect:/projects/my"; // '내 프로젝트' 목록으로 이동
        } catch (ParticipantException | ProjectException e) {
            // 에러 발생 시 목록 페이지로 돌아가면서 에러 파라미터 전달 (간단한 처리)
            return "redirect:/projects?error=" + e.getMessage();
        }
    }

    // 5. 내가 참여 중인 프로젝트 보기 (GET /projects/my)
    @GetMapping("/my")
    public String showMyProjects(HttpSession session, Model model) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/members/login";
        }

        // DTO 리스트를 받아옴
        List<MyProjectResponseDto> myProjects = projectService.getMyProjectListAndRole(loginMember.getId());
        model.addAttribute("projects", myProjects);

        return "project/my-list";
    }

    // 6. 프로젝트 삭제 (POST /projects/{id}/delete)
    @PostMapping("/{projectId}/delete")
    public String deleteProject(@PathVariable Long projectId, HttpSession session) {
        Member loginMember = (Member) session.getAttribute("loginMember");
        if (loginMember == null) {
            return "redirect:/members/login";
        }

        // 본인 확인 로직은 Service 내부 혹은 여기서 처리
        try {
            projectService.deleteProject(projectId, loginMember.getId());
        } catch (Exception e) {
            // 삭제 실패 시 처리 (생략)
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
                    project.getDescription()
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
            model.addAttribute("updateDto", dto); // 입력했던 내용 유지
            model.addAttribute("projectId", projectId);
            return "project/edit";
        }
    }
}