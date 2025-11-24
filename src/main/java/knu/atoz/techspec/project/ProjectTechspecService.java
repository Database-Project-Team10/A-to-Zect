package knu.atoz.techspec.project;

import knu.atoz.techspec.Techspec;
import knu.atoz.techspec.TechspecRepository;
import knu.atoz.techspec.exception.TechspecAlreadyExistsException;
import knu.atoz.techspec.exception.TechspecInvalidException;
import knu.atoz.techspec.exception.TechspecNotFoundException;
import knu.atoz.utils.Azconnection;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProjectTechspecService {

    private final TechspecRepository techspecRepository;
    private final ProjectTechspecRepository projectTechspecRepository;

    // [변경] Project 객체 대신 ID를 받도록 수정 (오버로딩)
    public List<Techspec> getProjectTechspecs(Long projectId) {
        return projectTechspecRepository.findTechspecsByProjectId(projectId);
    }

    // [변경] Project 객체 대신 ID를 받음
    public void addTechspecToProject(Long projectId, String techName) {

        if (techName == null || techName.isBlank()) {
            throw new TechspecInvalidException("스택 이름은 비어 있을 수 없습니다.");
        }

        Connection conn = null;
        try {
            conn = Azconnection.getConnection();
            conn.setAutoCommit(false); // 트랜잭션 시작

            // 1. 기술 스택이 DB에 있는지 확인 (없으면 생성)
            Techspec techspec = techspecRepository.findTechspecIdByName(techName);
            Long techspecId;

            if (techspec == null) {
                techspecId = techspecRepository.createTechspec(conn, techName);
            } else {
                techspecId = techspec.getId();
            }

            // 2. 프로젝트-기술스택 연결 테이블에 추가
            boolean inserted = projectTechspecRepository.addProjectTechspec(
                    conn, projectId, techspecId
            );

            if (!inserted) {
                // 이미 연결되어 있는 경우 등
                throw new TechspecAlreadyExistsException("이미 추가된 스택입니다.");
            }

            conn.commit(); // 커밋

        } catch (SQLException e) {
            rollbackQuietly(conn);
            // 오라클 무결성 제약조건 에러 코드(1) 체크
            if (e.getErrorCode() == 1) {
                throw new TechspecAlreadyExistsException("이미 존재하는 스택입니다.");
            }
            throw new RuntimeException("DB 오류 발생: " + e.getMessage());
        } finally {
            closeQuietly(conn);
        }
    }

    // [변경] Project 객체 대신 ID를 받음
    public void removeTechspecFromProject(Long projectId, Long techspecId) {
        if (!projectTechspecRepository.deleteProjectTechspec(projectId, techspecId)) {
            throw new TechspecNotFoundException("삭제할 스택이 존재하지 않습니다.");
        }
    }

    private void rollbackQuietly(Connection conn) {
        try { if (conn != null) conn.rollback(); } catch (SQLException ignored) {}
    }

    private void closeQuietly(Connection conn) {
        try {
            if (conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        } catch (SQLException ignored) {}
    }
}