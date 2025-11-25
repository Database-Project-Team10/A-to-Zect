package knu.atoz.utils; // 패키지명 확인

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Azconnection {

    public static Connection getConnection() {
        Connection conn = null;
        try {
            // 1. 환경 변수에서 URL을 가져옴 (도커용)
            String dbUrl = System.getenv("SPRING_DATASOURCE_URL");
            String dbUser = System.getenv("SPRING_DATASOURCE_USERNAME");
            String dbPassword = System.getenv("SPRING_DATASOURCE_PASSWORD");

            // 2. 환경 변수가 없으면 로컬 설정 사용 (IntelliJ 실행용)
            if (dbUrl == null || dbUrl.isEmpty()) {
                dbUrl = "jdbc:oracle:thin:@localhost:1521:xe"; // 또는 1522 등 본인 로컬 설정
                dbUser = "az";       // 로컬 아이디
                dbPassword = "dtob"; // 로컬 비번
            }

            // 3. 드라이버 로드 및 연결
            Class.forName("oracle.jdbc.OracleDriver");
            conn = DriverManager.getConnection(dbUrl, dbUser, dbPassword);

        } catch (ClassNotFoundException e) {
            System.err.println("드라이버 로드 실패: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("DB 연결 실패: " + e.getMessage());
        }
        return conn;
    }
}