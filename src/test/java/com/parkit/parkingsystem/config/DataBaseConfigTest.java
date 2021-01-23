package com.parkit.parkingsystem.config;

import mockit.MockUp;
import mockit.integration.junit5.JMockitExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith({MockitoExtension.class, JMockitExtension.class})
class DataBaseConfigTest {

    @Mock
    Connection connectionMock;

    @Mock
    PreparedStatement preparedStatementMock;

    @Mock
    ResultSet resultSetMock;

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
    void closeConnection_WhenConnectionNotNull_ShouldCloseConnection() throws SQLException, ClassNotFoundException {
        //given create connection at setup

        //when
        Connection resultConnection;
        resultConnection = dataBaseConfigUnderTest.getConnection();
        dataBaseConfigUnderTest.closeConnection(resultConnection);

        //then
        verify(connectionMock, times(1)).close();
        assertDoesNotThrow(() -> connectionMock.close());

    }

    @Test
    void closePreparedStatement_WhenPreparedStatementNotNull_ShouldClosePreparedStatement() throws SQLException {
        //given
        //when
        assertDoesNotThrow(()->dataBaseConfigUnderTest.closePreparedStatement(preparedStatementMock));
        //then
        verify(preparedStatementMock,times(1)).close();
    }

    @Test
    void closeResultSet_WhenResultSetNotNull_ShouldCloseResultSet() throws SQLException {
        //given
        //when
        assertDoesNotThrow(()->dataBaseConfigUnderTest.closeResultSet(resultSetMock));
        //then
        verify(resultSetMock,times(1)).close();

    }
}