package knu.atoz.project;

import knu.atoz.project.dto.MyProjectResponseDto;
import knu.atoz.utils.Azconnection;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ProjectRepository {

    public List<Project> findByTitleContaining(String keyword) {
        String sql = "SELECT * FROM project WHERE title LIKE ? ORDER BY created_at DESC";
        List<Project> projectList = new ArrayList<>();

        try (Connection conn = Azconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // Oracle DB의 LIKE 검색 패턴 설정 ('%keyword%')
            pstmt.setString(1, "%" + keyword + "%");

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Project project = new Project(
                            rs.getLong("id"),
                            rs.getString("title"),
                            rs.getString("description"),
                            rs.getInt("current_count"),
                            rs.getInt("max_count"),
                            rs.getObject("created_at", LocalDateTime.class),
                            rs.getObject("updated_at", LocalDateTime.class)
                    );
                    projectList.add(project);
                }
            }
        } catch (SQLException e) {
            System.err.println("프로젝트 검색 중 오류 발생: " + e.getMessage());
        }
        return projectList;
    }
    public Project findByIdWithLock(Connection conn, Long projectId) throws SQLException {
        String sql = "SELECT * FROM project WHERE id = ? FOR UPDATE";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, projectId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Project(
                            rs.getLong("id"),
                            rs.getString("title"),
                            rs.getString("description"),
                            rs.getInt("current_count"),
                            rs.getInt("max_count"),
                            rs.getObject("created_at", LocalDateTime.class),
                            rs.getObject("updated_at", LocalDateTime.class)
                    );
                }
            }
        }
        return null;
    }

    public List<Project> findAllProjects() {
        String sql = "SELECT * FROM project";
        List<Project> projectList = new ArrayList<>();

        try (Connection conn = Azconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Project project = new Project(
                            rs.getLong("id"),
                            rs.getString("title"),
                            rs.getString("description"),
                            rs.getInt("current_count"),
                            rs.getInt("max_count"),
                            rs.getObject("created_at", LocalDateTime.class),
                            rs.getObject("updated_at", LocalDateTime.class)
                    );
                    projectList.add(project);
                }
            }
        } catch (SQLException e) {
            System.err.println("DB 조회 중 오류 발생: " + e.getMessage());
        }
        return projectList;
    }

    public Project findById(Long projectId) {
        String sql = "select * from project where id=?";

        Project project = null;

        try (Connection conn = Azconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, projectId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    project = new Project(
                            rs.getLong("id"),
                            rs.getString("title"),
                            rs.getString("description"),
                            rs.getInt("current_count"),
                            rs.getInt("max_count"),
                            rs.getObject("created_at", LocalDateTime.class),
                            rs.getObject("updated_at", LocalDateTime.class)
                    );
                }
            }

        } catch (SQLException e) {
            System.err.println("DB 조회 중 오류 발생: " + e.getMessage());
        }
        return project;
    }

    public List<Project> findProjects(int cnt) {
        List<Project> projectList = new ArrayList<>();
        String sql = "SELECT * FROM project ORDER BY created_at DESC FETCH FIRST ? ROWS ONLY";

        try (Connection conn = Azconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, cnt);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Project project = new Project(
                            rs.getLong("id"),
                            rs.getString("title"),
                            rs.getString("description"),
                            rs.getInt("current_count"),
                            rs.getInt("max_count"),
                            rs.getObject("created_at", LocalDateTime.class),
                            rs.getObject("updated_at", LocalDateTime.class)
                    );
                    projectList.add(project);
                }
            }
        } catch (SQLException e) {
            System.err.println("DB 조회 중 오류 발생: " + e.getMessage());
        }
        return projectList;
    }

    public List<Project> findProjectsByMemberId(long memberId) {
        List<Project> projectList = new ArrayList<>();

        String sql = "SELECT p.* " +
                "FROM project p " +
                "JOIN participant pa ON p.id = pa.project_id " +
                "WHERE pa.member_id = ? " +
                "ORDER BY p.updated_at DESC";

        try (Connection conn = Azconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, memberId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Project project = new Project(
                            rs.getLong("id"),
                            rs.getString("title"),
                            rs.getString("description"),
                            rs.getInt("current_count"),
                            rs.getInt("max_count"),
                            rs.getObject("created_at", LocalDateTime.class),
                            rs.getObject("updated_at", LocalDateTime.class)
                    );
                    projectList.add(project);
                }
            }
        } catch (SQLException e) {
            System.err.println("DB 조회 중 오류 발생: " + e.getMessage());
        }
        return projectList;
    }


    public List<MyProjectResponseDto> findMyProjectDtos(Long memberId) {
        List<MyProjectResponseDto> list = new ArrayList<>();

        String sql = "SELECT p.id, p.title, p.description, p.updated_at, pa.role " +
                "FROM project p " +
                "JOIN participant pa ON p.id = pa.project_id " +
                "WHERE pa.member_id = ? " +
                "ORDER BY p.updated_at DESC";

        try (Connection conn = Azconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, memberId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {

                    MyProjectResponseDto dto = new MyProjectResponseDto(
                            rs.getLong("id"),
                            rs.getString("title"),
                            rs.getString("description"),
                            rs.getString("role"),
                            rs.getObject("updated_at", LocalDateTime.class)
                    );
                    list.add(dto);
                }
            }
        } catch (SQLException e) {
            System.err.println("내 프로젝트 조회 중 오류: " + e.getMessage());
        }
        return list;
    }

    public Project save(Connection conn, Project project) throws SQLException {
        String sql = "INSERT INTO project (title, description, current_count, max_count, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?)";
        String[] generatedColumns = {"id"};

        try (PreparedStatement pstmt = conn.prepareStatement(sql, generatedColumns)) {

            pstmt.setString(1, project.getTitle());
            pstmt.setString(2, project.getDescription());
            pstmt.setInt(3, project.getCurrentCount());
            pstmt.setInt(4, project.getMaxCount());
            pstmt.setTimestamp(5, java.sql.Timestamp.valueOf(project.getCreatedAt()));
            pstmt.setTimestamp(6, java.sql.Timestamp.valueOf(project.getModifiedAt()));

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("프로젝트 생성 실패: 영향받은 행이 없습니다.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    Long generatedId = generatedKeys.getLong(1);

                    
                    return new Project(
                            generatedId,
                            project.getTitle(),
                            project.getDescription(),
                            project.getCurrentCount(),
                            project.getMaxCount(),
                            project.getCreatedAt(),
                            project.getModifiedAt()
                    );

                } else {
                    throw new SQLException("프로젝트 생성 실패: ID를 가져오지 못했습니다.");
                }
            }
        }
    }

    public boolean updateProject(Project project) {
        String sql = "UPDATE project SET title = ?, description = ?, max_count = ?, updated_at = ? WHERE id = ?";

        try (Connection conn = Azconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, project.getTitle());
            pstmt.setString(2, project.getDescription());
            pstmt.setInt(3, project.getMaxCount());
            pstmt.setTimestamp(4, java.sql.Timestamp.valueOf(project.getModifiedAt()));
            pstmt.setLong(5, project.getId());

            
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                return true; 
            }

        } catch (SQLException e) {
            System.err.println("DB 업데이트 중 오류 발생: " + e.getMessage());
        }

        return false;
    }

    public boolean deleteProject(Long projectId) {
        String sql = "DELETE FROM project WHERE id = ?";

        try (Connection conn = Azconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, projectId);

            
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                return true;
            }

        } catch (SQLException e) {
            System.err.println("DB 업데이트 중 오류 발생: " + e.getMessage());
        }

        return false;
    }


    public Project findMyProjectByIdAndMemberId(Long memberId, Long projectId) {
        String sql = "SELECT p.* " +
                "FROM Project p " +
                "JOIN Participant pa ON p.id = pa.project_id " +
                "WHERE p.id = ? AND pa.member_id = ?";

        try (Connection conn = Azconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, projectId);
            pstmt.setLong(2, memberId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Project(
                            rs.getLong("id"),
                            rs.getString("title"),
                            rs.getString("description"),
                            rs.getInt("current_count"),
                            rs.getInt("max_count"),
                            rs.getObject("created_at", LocalDateTime.class),
                            rs.getObject("updated_at", LocalDateTime.class)
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("DB 조회 중 오류 발생: " + e.getMessage());
        }
        return null;
    }


    public Project findProjectById(Connection conn, Long projectId) throws SQLException {
        String sql = "SELECT * FROM Project WHERE id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, projectId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Project(
                            rs.getLong("id"),
                            rs.getString("title"),
                            rs.getString("description"),
                            rs.getInt("current_count"),
                            rs.getInt("max_count"),
                            rs.getObject("created_at", LocalDateTime.class),
                            rs.getObject("updated_at", LocalDateTime.class)
                    );
                } else {
                    throw new SQLException("Project 조회 실패: ID " + projectId + "를 찾을 수 없습니다.");
                }
            }
        }
    }

    public void incrementCurrentCount(Connection conn, Long projectId) throws SQLException {
        String sql = "UPDATE Project SET current_count = current_count + 1 WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, projectId);
            pstmt.executeUpdate();
        }
    }

    public void decrementCurrentCount(Connection conn, Long projectId) throws SQLException {
        String sql = "UPDATE project SET current_count = current_count - 1 WHERE id = ? AND current_count > 0";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, projectId);
            pstmt.executeUpdate();
        }
    }

}
