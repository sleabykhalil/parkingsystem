package com.parkit.parkingsystem.config;

import mockit.MockUp;
import mockit.integration.junit5.JMockitExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith({MockitoExtension.class, JMockitExtension.class})
class DataBaseConfigTest {

    @Mock
    Connection connectionMock;

    DataBaseConfig dataBaseConfigUnderTest;

    @BeforeEach
    void setUp() {
        dataBaseConfigUnderTest = new DataBaseConfig();

        // Mock static method DriverManager.getConnection
        new mockit.MockUp<DriverManager>() {
            @mockit.Mock
            public Connection getConnection(String url, String user, String password) {
                return connectionMock;
            }
        };
    }


    @Test
    void getConnection_whenGetConnection_ConnectionReturn() throws SQLException, ClassNotFoundException {
        //given

        //when
        Connection resultConnection;
        resultConnection = dataBaseConfigUnderTest.getConnection();

        //then
        assertNotNull(resultConnection);
        assertEquals(connectionMock, resultConnection);
    }

    @Test
    void closeConnection_WhenConnectionNotNull_ShouldCloseConnection() {

    }

    @Test
    void closePreparedStatement() {
    }

    @Test
    void closeResultSet() {
    }
}