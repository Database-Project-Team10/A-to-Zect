package knu.atoz.participant;


import knu.atoz.member.Member;
import knu.atoz.utils.Azconnection;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ParticipantRepository {

    public boolean save(Long projectId, Long memberId){
        String sql = "INSERT INTO participant (member_id, project_id, role) Values (?, ?, 'PENDING')";
        try (Connection conn = Azconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, memberId);
            pstmt.setLong(2, projectId);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                return true; 
            }

        } catch (SQLException e) {
            System.err.println("DB 업데이트 중 오류 발생: " + e.getMessage());
        }

        return false;
    }

    public void saveLeader(Connection conn, long memberId, long projectId) throws SQLException {
        
        String sql = "INSERT INTO participant (member_id, project_id, role) VALUES (?, ?, 'LEADER')";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, memberId);
            pstmt.setLong(2, projectId);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("참가자(리더) 추가 실패: 영향받은 행이 없습니다.");
            }
        }
    }

    public boolean exists(Long projectId, Long memberId){
        String sql = "SELECT * FROM participant WHERE project_id = ? AND member_id = ?";
        try (Connection conn = Azconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, projectId);
            pstmt.setLong(2, memberId);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            System.err.println("DB 조회 중 오류 발생: " + e.getMessage());
        }

        return false;
    }

    public boolean isLeader(Long projectId, Long memberId){
        String sql = "SELECT * FROM participant WHERE project_id = ? AND member_id = ? AND role = 'LEADER'";
        try (Connection conn = Azconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, projectId);
            pstmt.setLong(2, memberId);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return true;
            }
        } catch (SQLException e) {
            System.err.println("DB 조회 중 오류 발생: " + e.getMessage());
        }

        return false;
    }

    
    public String findRole(Long projectId, Long memberId) {
        String sql = "SELECT role FROM Participant WHERE project_id = ? AND member_id = ?";

        try (Connection conn = Azconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, projectId);
            pstmt.setLong(2, memberId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("role"); 
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; 
    }

    
    public List<Member> findPendingMembers(Long projectId) {
        List<Member> list = new ArrayList<>();
        String sql = "SELECT m.id, m.name, m.email, m.birth_date, m.created_at " +
                "FROM Participant pa " +
                "JOIN Member m ON pa.member_id = m.id " +
                "WHERE pa.project_id = ? AND pa.role = 'PENDING' ";

        try (Connection conn = Azconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, projectId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    
                    list.add(new Member(
                            rs.getLong("id"),
                            rs.getString("email"),
                            null, 
                            rs.getString("name"),
                            rs.getObject("birth_date", LocalDate.class),
                            rs.getObject("created_at", LocalDateTime.class)
                    ));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    
    public void updateRole(Connection conn, Long projectId, Long memberId, String newRole) throws SQLException {
        String sql = "UPDATE Participant SET role = ? WHERE project_id = ? AND member_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newRole);
            pstmt.setLong(2, projectId);
            pstmt.setLong(3, memberId);
            int result = pstmt.executeUpdate();
            if (result == 0) throw new SQLException("대상 회원을 찾을 수 없습니다.");
        }
    }

    /**
     * 참가자 삭제 (거절 또는 나가기)
     * PK인 project_id와 member_id를 모두 만족하는 행을 삭제합니다.
     */
    public void delete(Long projectId, Long memberId) {
        String sql = "DELETE FROM Participant WHERE project_id = ? AND member_id = ?";

        try (Connection conn = Azconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, projectId);
            pstmt.setLong(2, memberId);

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                
                System.out.println("[WARN] 삭제할 참가자 내역이 없습니다. (ID: " + memberId + ")");
            }

        } catch (SQLException e) {
            System.err.println("참가자 삭제 중 오류 발생: " + e.getMessage());
            throw new RuntimeException("참가자 삭제 실패", e);
        }
    }
}
