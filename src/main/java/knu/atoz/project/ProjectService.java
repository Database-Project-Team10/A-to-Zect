package knu.atoz.project;

import knu.atoz.mbti.project.ProjectMbtiRepository;
import knu.atoz.participant.ParticipantRepository;
import knu.atoz.project.dto.MyProjectResponseDto;
import knu.atoz.project.dto.ProjectCreateRequestDto;
import knu.atoz.project.dto.ProjectUpdateRequestDto;
import knu.atoz.project.exception.ProjectDescriptionInvalidException;
import knu.atoz.project.exception.ProjectNotFoundException;
import knu.atoz.project.exception.ProjectTitleInvalidException;
import knu.atoz.project.exception.UnauthorizedProjectAccessException;
import knu.atoz.techspec.Techspec;
import knu.atoz.techspec.TechspecRepository;
import knu.atoz.techspec.project.ProjectTechspecRepository;
import knu.atoz.utils.Azconnection;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ParticipantRepository participantRepository;
    private final TechspecRepository techspecRepository;
    private final ProjectTechspecRepository projectTechspecRepository;
    private final ProjectMbtiRepository projectMbtiRepository;

    public ProjectService(
            ProjectRepository projectRepository,
            ParticipantRepository participantRepository,
            TechspecRepository techspecRepository,
            ProjectTechspecRepository projectTechspecRepository,
            ProjectMbtiRepository projectMbtiRepository
    ) {
        this.projectRepository = projectRepository;
        this.participantRepository = participantRepository;
        this.techspecRepository = techspecRepository;
        this.projectTechspecRepository = projectTechspecRepository;
        this.projectMbtiRepository = projectMbtiRepository;
    }

    public List<Project> getAllProjects() {
        return projectRepository.findAllProjects();
    }

    public List<Project> getProjectList(int cnt) {
        return projectRepository.findProjects(cnt);
    }

    public List<Project> getMyProjectList(Long memberId) {
        return projectRepository.findProjectsByMemberId(memberId);
    }

    public List<MyProjectResponseDto> getMyProjectListAndRole(Long memberId) {
        return projectRepository.findMyProjectDtos(memberId);
    }

    public Project getProject(Long projectId) {
        Project project = projectRepository.findById(projectId);
        if  (project == null) {
            throw new ProjectNotFoundException();
        }
        return project;
    }

    public Project getMyProjectById(Long memberId, Long projectId) {
        Project project = projectRepository.findMyProjectByIdAndMemberId(memberId, projectId);
        if  (project == null) {
            throw new ProjectNotFoundException();
        }
        return project;
    }

    public Project createProject(ProjectCreateRequestDto requestDto) {
        if (requestDto.getTitle() == null || requestDto.getTitle().isBlank()) {
            throw new ProjectTitleInvalidException("프로젝트 제목은 비어 있을 수 없습니다.");
        }

        if (requestDto.getDescription() == null || requestDto.getDescription().isBlank()) {
            throw new ProjectDescriptionInvalidException("프로젝트 설명은 비어 있을 수 없습니다.");
        }

        Connection conn = null;
        try {
            conn = Azconnection.getConnection();
            conn.setAutoCommit(false); // 트랜잭션 시작

            // 1. Project 생성
            Project newProject = projectRepository.save(conn, new Project(requestDto.getTitle(), requestDto.getDescription(), requestDto.getMaxCount()));

            // 2. Participant 추가
            participantRepository.saveLeader(conn, requestDto.getMemberId(), newProject.getId());

            // 3. 스택 저장
            if (!requestDto.getTechSpecs().isEmpty()) {
                //System.out.println("\n[DB 저장 시작 - 스택]");
                for (String techName : requestDto.getTechSpecs()) {
                    Techspec techspec = techspecRepository.findTechspecIdByName(techName);
                    Long techspecId = null;
                    if (techspec == null) {
                        techspecId = techspecRepository.createTechspec(conn, techName);
                    }
                    else {
                        techspecId = techspec.getId();
                    }
                    projectTechspecRepository.addProjectTechspec(conn, newProject.getId(), techspecId);
                }
            }

            // 4. MBTI 저장
            if (!requestDto.getMbtiMap().isEmpty()) {
                //System.out.println("[DB 저장 시작 - MBTI]");
                projectMbtiRepository.saveProjectMbti(conn, newProject.getId(), requestDto.getMbtiMap());
            }

            conn.commit();
            System.out.println("\n'" + newProject.getTitle() + "' 프로젝트 생성 및 설정이 완료되었습니다.");
            return projectRepository.findProjectById(conn, newProject.getId());

        } catch (SQLException e) {
            System.err.println("프로젝트 생성 중 오류 발생: " + e.getMessage());
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) {}
        } finally {
            try { if (conn != null) { conn.setAutoCommit(true); conn.close(); } } catch (SQLException e) {}
        }
        return null;
    }

    public void updateProjectInfo(Long projectId, Long memberId , ProjectUpdateRequestDto requestDto) {
        Project project = projectRepository.findById(projectId);
        if (project == null) {
            throw new ProjectNotFoundException();
        }
        if (!participantRepository.exists(projectId, memberId)) {
            throw new UnauthorizedProjectAccessException("해당 프로젝트에 대한 수정 권한이 없습니다.");
        }
        Project newProject = new Project(
                projectId,
                requestDto.getTitle(),
                requestDto.getDescription(),
                project.getCurrentCount(),
                requestDto.getMaxCount(),
                project.getCreatedAt()
        );
        projectRepository.updateProject(newProject);
    }

    public void deleteProject(Long projectId, Long memberId) {
        Project project = projectRepository.findById(projectId);
        if (project == null) {
            throw new ProjectNotFoundException();
        }

        if (!participantRepository.isLeader(projectId, memberId)) {
            throw new UnauthorizedProjectAccessException("해당 프로젝트에 대한 삭제 권한이 없습니다.");
        }

        projectRepository.deleteProject(projectId);
    }
}

