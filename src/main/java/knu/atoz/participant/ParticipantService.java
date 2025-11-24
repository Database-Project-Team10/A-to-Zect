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

    // 1. 참가 신청
    public void applyProject(Long projectId, Long memberId) {
        // 이미 신청/참여 중인지 확인
        if (participantRepository.exists(projectId, memberId)) {
            throw new RuntimeException("이미 신청했거나 참여 중인 프로젝트입니다.");
        }
        // PENDING 상태로 저장
        participantRepository.save(projectId, memberId);
    }

    // 2. 대기자 목록 조회
    public List<Member> getPendingMembers(Long projectId) {
        return participantRepository.findPendingMembers(projectId);
    }

    // 3. 참가 승인 (핵심: 트랜잭션)
    public void acceptMember(Long projectId, Long targetMemberId) {
        Connection conn = null;
        try {
            conn = Azconnection.getConnection();
            conn.setAutoCommit(false); // 트랜잭션 시작

            // (1) 인원 마감 체크 (Lock을 걸면 더 좋지만 일단 조회)
            Project project = projectRepository.findById(projectId);
            if (project.getCurrentCount() >= project.getMaxCount()) {
                throw new RuntimeException("모집 인원이 마감되었습니다.");
            }

            // (2) 상태 변경 (PENDING -> MEMBER)
            // updateRole 메서드가 Connection을 받도록 오버로딩하거나 수정 필요
            participantRepository.updateRole(conn, projectId, targetMemberId, "MEMBER");

            // (3) 프로젝트 인원수 +1
            projectRepository.incrementCurrentCount(conn, projectId);

            conn.commit(); // 커밋

        } catch (Exception e) {
            try { if (conn != null) conn.rollback(); } catch (SQLException ex) {}
            throw new RuntimeException("승인 처리 실패: " + e.getMessage());
        } finally {
            try { if (conn != null) { conn.setAutoCommit(true); conn.close(); } } catch (SQLException e) {}
        }
    }

    // 4. 거절 (삭제)
    public void rejectMember(Long projectId, Long targetMemberId) {
        participantRepository.delete(projectId, targetMemberId);
    }

    // (참고) Controller의 joinProject는 이제 applyProject로 대체되거나 삭제
    public void joinProject(Long projectId, Long memberId) {
        applyProject(projectId, memberId);
    }
}
