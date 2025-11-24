package knu.atoz.mbti.member;

import knu.atoz.mbti.MbtiDimension;
import knu.atoz.utils.Azconnection;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class MemberMbtiRepository {

    public List<MbtiDimension> findAllMbtiDimensions() {
        List<MbtiDimension> dimensions = new ArrayList<>();
        String sql = "SELECT id, dimension_type, option1, option2 FROM MBTI ORDER BY id";

        try (Connection conn = Azconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                dimensions.add(new MbtiDimension(
                        rs.getLong("id"),
                        rs.getString("dimension_type"),
                        rs.getString("option1"),
                        rs.getString("option2")
                ));
            }
        } catch (SQLException e) {
            System.err.println("MBTI 차원 조회 중 오류: " + e.getMessage());
            return null;
        }
        return dimensions;
    }


    public Map<Long, String> findMbtiMapByMemberId(Long memberId) {
        Map<Long, String> mbtiMap = new HashMap<>();
        String sql = "SELECT mbti_id, selected_option FROM MemberMbti WHERE member_id = ?";

        try (Connection conn = Azconnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setLong(1, memberId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    mbtiMap.put(rs.getLong("mbti_id"), rs.getString("selected_option"));
                }
            }
        } catch (SQLException e) {
            System.err.println("회원 MBTI 조회 중 오류: " + e.getMessage());
        }
        return mbtiMap;
    }

    public boolean saveMemberMbti(Long memberId, Map<Long, String> mbtiMap) {
        String sql = "MERGE INTO MemberMbti m " +
                "USING (SELECT ? AS m_id, ? AS b_id, ? AS sel_opt FROM dual) src " +
                "ON (m.member_id = src.m_id AND m.mbti_id = src.b_id) " +
                "WHEN MATCHED THEN " +
                "  UPDATE SET m.selected_option = src.sel_opt " +
                "WHEN NOT MATCHED THEN " +
                "  INSERT (member_id, mbti_id, selected_option) " +
                "  VALUES (src.m_id, src.b_id, src.sel_opt)";

        Connection conn = null;
        try {
            conn = Azconnection.getConnection();
            conn.setAutoCommit(false); 

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                for (Map.Entry<Long, String> entry : mbtiMap.entrySet()) {
                    pstmt.setLong(1, memberId);
                    pstmt.setLong(2, entry.getKey());
                    pstmt.setString(3, entry.getValue());
                    pstmt.executeUpdate();
                }
            }

            conn.commit(); 
            return true;

        } catch (SQLException e) {
            System.err.println("DB 저장 중 오류 발생: " + e.getMessage());
            try {
                if (conn != null) conn.rollback(); 
            } catch (SQLException ex) {
                System.err.println("Rollback 실패: " + ex.getMessage());
            }
            return false;
        } finally {
            try {
                if (conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("Connection 종료 실패: " + e.getMessage());
            }
        }
    }
}