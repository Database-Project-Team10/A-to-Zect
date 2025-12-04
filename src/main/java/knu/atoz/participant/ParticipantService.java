package knu.atoz.participant;

import knu.atoz.member.Member;
import knu.atoz.participant.dto.ParticipantResponseDto;
import knu.atoz.participant.exception.ParticipantAlreadyExistsException;
import knu.atoz.project.Project;
import knu.atoz.project.ProjectRepository;
import knu.atoz.project.ProjectService;
import knu.atoz.project.exception.ProjectNotFoundException;
import knu.atoz.project.exception.UnauthorizedProjectAccessException;
import knu.atoz.utils.Azconnection;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@Service
public class ParticipantService {
    private final ParticipantRepository participantRepository;
    private final ProjectRepository projectRepository;
    private final ProjectService projectService;

    public ParticipantService(ParticipantRepository participantRepository,
                              ProjectRepository projectRepository,
                              ProjectService projectService) {
        this.participantRepository = participantRepository;
        this.projectRepository = projectRepository;
        this.projectService = projectService;
    }

    public String getMyRole(Long projectId, Long memberId) {
        return participantRepository.findRole(projectId, memberId);
    }

    public List<ParticipantResponseDto> getTeamMembers(Long projectId) {
        return participantRepository.findProjectMembers(projectId);
    }

    public List<Member> getPendingMembers(Long projectId) {
        return participantRepository.findPendingMembers(projectId);
    }

    public void rejectMember(Long projectId, Long targetMemberId) {
        participantRepository.delete(projectId, targetMemberId);
    }

    public void leaveProject(Long projectId, Long memberId) {
        Connection conn = null;
        try {
            conn = Azconnection.getConnection();
            conn.setAutoCommit(false);

            String role = participantRepository.findRole(projectId, memberId);
            if ("LEADER".equals(role)) {
                throw new RuntimeException("프로젝트 리더는 나갈 수 없습니다. (프로젝트 삭제만 가능)");
            }

            participantRepository.delete(conn, projectId, memberId);
            projectRepository.decrementCurrentCount(conn, projectId);

            conn.commit();

        } catch (Exception e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) {}
            throw new RuntimeException("프로젝트 나가기 처리 실패: " + e.getMessage());
        } finally {
            try { if (conn != null) { conn.setAutoCommit(true); conn.close(); } } catch (SQLException e) {}
        }
    }

    public void cancelApplication(Long projectId, Long memberId) {
        String role = participantRepository.findRole(projectId, memberId);
        if (!"PENDING".equals(role)) {
            throw new RuntimeException("대기 중(PENDING)인 상태에서만 신청을 취소할 수 있습니다.");
        }
        participantRepository.delete(projectId, memberId);
    }

    public void applyProject(Long projectId, Long memberId) {
        Connection conn = null;
        try {
            conn = Azconnection.getConnection();
            conn.setAutoCommit(false);

            Project project = projectRepository.findByIdWithLock(conn, projectId);

            if (project == null) {
                throw new ProjectNotFoundException();
            }

            if (project.getCurrentCount() >= project.getMaxCount()) {
                throw new UnauthorizedProjectAccessException("아쉽지만 모집 인원이 마감되었습니다.");
            }

            if (participantRepository.existsWithTx(conn, projectId, memberId)) {
                throw new ParticipantAlreadyExistsException();
            }

            participantRepository.saveWithTx(conn, projectId, memberId);

            conn.commit();

        } catch (Exception e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) {}
            throw new RuntimeException("프로젝트 신청 실패: " + e.getMessage());
        } finally {
            try { if (conn != null) { conn.setAutoCommit(true); conn.close(); } } catch (SQLException e) {}
        }
    }

    public void acceptMember(Long projectId, Long targetMemberId) {
        Connection conn = null;
        try {
            conn = Azconnection.getConnection();
            conn.setAutoCommit(false);

            Project project = projectRepository.findByIdWithLock(conn, projectId);
            
            if (project == null) {
                throw new ProjectNotFoundException();
            }

            if (project.getCurrentCount() >= project.getMaxCount()) {
                throw new UnauthorizedProjectAccessException("모집 인원이 마감되었습니다.");
            }

            participantRepository.updateRole(conn, projectId, targetMemberId, "MEMBER");

            projectRepository.incrementCurrentCount(conn, projectId);

            conn.commit();
        } catch (Exception e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) {}
            throw new RuntimeException("승인 처리 실패: " + e.getMessage());
        } finally {
            try { if (conn != null) { conn.setAutoCommit(true); conn.close(); } } catch (SQLException e) {}
        }
    }

    public void kickMember(Long projectId, Long targetMemberId, Long requesterId) {
        Connection conn = null;
        try {
            conn = Azconnection.getConnection();
            conn.setAutoCommit(false);

            boolean isLeader = participantRepository.isLeader(projectId, requesterId);
            if (!isLeader) {
                throw new UnauthorizedProjectAccessException("멤버 추방 권한이 없습니다. (리더만 가능)");
            }

            if (targetMemberId.equals(requesterId)) {
                throw new UnauthorizedProjectAccessException("자기 자신을 추방할 수 없습니다.");
            }

            participantRepository.delete(conn, projectId, targetMemberId);

            projectRepository.decrementCurrentCount(conn, projectId);

            conn.commit();
        } catch (Exception e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) {}
            throw new RuntimeException("멤버 추방 실패: " + e.getMessage());
        } finally {
            try { if (conn != null) { conn.setAutoCommit(true); conn.close(); } } catch (SQLException e) {}
        }
    }
}