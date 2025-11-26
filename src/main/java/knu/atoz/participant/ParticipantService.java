package knu.atoz.participant;

import knu.atoz.member.Member;
import knu.atoz.participant.dto.ParticipantResponseDto;
import knu.atoz.project.Project;
import knu.atoz.project.ProjectRepository;
import knu.atoz.project.ProjectService;
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
        
        if (participantRepository.exists(projectId, memberId)) {
            throw new RuntimeException("이미 신청했거나 참여 중인 프로젝트입니다.");
        }
        
        participantRepository.save(projectId, memberId);
    }

    
    public List<Member> getPendingMembers(Long projectId) {
        return participantRepository.findPendingMembers(projectId);
    }

    
    public void acceptMember(Long projectId, Long targetMemberId) {
        Connection conn = null;
        try {
            conn = Azconnection.getConnection();
            conn.setAutoCommit(false); 

            
            Project project = projectRepository.findById(projectId);
            if (project.getCurrentCount() >= project.getMaxCount()) {
                throw new RuntimeException("모집 인원이 마감되었습니다.");
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

    
    public void rejectMember(Long projectId, Long targetMemberId) {
        participantRepository.delete(projectId, targetMemberId);
    }

    
    public void joinProject(Long projectId, Long memberId) {
        applyProject(projectId, memberId);
    }

    public List<ParticipantResponseDto> getTeamMembers(Long projectId) {
        return participantRepository.findProjectMembers(projectId);
    }

    // [추가] 멤버 추방 (트랜잭션 필수: 삭제 + 인원감소)
    public void kickMember(Long projectId, Long targetMemberId) {
        Connection conn = null;
        try {
            conn = Azconnection.getConnection();
            conn.setAutoCommit(false);

            // 1. 리더인지 확인하는 로직은 Controller나 앞단에서 수행했다고 가정
            // (본인을 추방하려는 경우 막는 로직 추가 가능)

            // 2. 멤버 삭제
            participantRepository.delete(conn, projectId, targetMemberId);

            // 3. 인원수 감소
            projectRepository.decrementCurrentCount(conn, projectId);

            conn.commit();
        } catch (Exception e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) {}
            throw new RuntimeException("멤버 추방 처리 실패: " + e.getMessage());
        } finally {
            try { if (conn != null) { conn.setAutoCommit(true); conn.close(); } } catch (SQLException e) {}
        }
    }
}
