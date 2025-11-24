package knu.atoz.participant;

import knu.atoz.member.Member;
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
}
