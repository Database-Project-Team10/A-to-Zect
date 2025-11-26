package knu.atoz.utils;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class Azconnection {

    public static Connection getConnection() {
        Connection conn = null;
        try {
            // 1. 우선 환경 변수(도커/배포 환경)에서 가져오기 시도
            String dbUrl = System.getenv("SPRING_DATASOURCE_URL");
            String dbUser = System.getenv("SPRING_DATASOURCE_USERNAME");
            String dbPassword = System.getenv("SPRING_DATASOURCE_PASSWORD");

            // 2. 환경 변수가 없다면(로컬 환경), properties 파일에서 읽어오기
            if (dbUrl == null || dbUrl.isEmpty()) {
                try (InputStream input = Azconnection.class.getClassLoader().getResourceAsStream("application.properties")) {
                    if (input == null) {
                        System.err.println("오류: db.properties 파일을 찾을 수 없습니다.");
                        return null;
                    }

                    Properties prop = new Properties();
                    prop.load(input); // 파일 내용 읽기

                    dbUrl = prop.getProperty("spring.datasource.url");
                    dbUser = prop.getProperty("spring.datasource.username");
                    dbPassword = prop.getProperty("spring.datasource.password");
                } catch (IOException ex) {
                    System.err.println("설정 파일 읽기 실패: " + ex.getMessage());
                    return null;
                }
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