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

    
    public List<Techspec> getProjectTechspecs(Long projectId) {
        return projectTechspecRepository.findTechspecsByProjectId(projectId);
    }

    
    public void addTechspecToProject(Long projectId, String techName) {

        if (techName == null || techName.isBlank()) {
            throw new TechspecInvalidException("스택 이름은 비어 있을 수 없습니다.");
        }

        Connection conn = null;
        try {
            conn = Azconnection.getConnection();
            conn.setAutoCommit(false); 

            
            Techspec techspec = techspecRepository.findTechspecIdByName(techName);
            Long techspecId;

            if (techspec == null) {
                techspecId = techspecRepository.createTechspec(conn, techName);
            } else {
                techspecId = techspec.getId();
            }

            
            boolean inserted = projectTechspecRepository.addProjectTechspec(
                    conn, projectId, techspecId
            );

            if (!inserted) {
                
                throw new TechspecAlreadyExistsException("이미 추가된 스택입니다.");
            }

            conn.commit(); 

        } catch (SQLException e) {
            rollbackQuietly(conn);
            
            if (e.getErrorCode() == 1) {
                throw new TechspecAlreadyExistsException("이미 존재하는 스택입니다.");
            }
            throw new RuntimeException("DB 오류 발생: " + e.getMessage());
        } finally {
            closeQuietly(conn);
        }
    }

    
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