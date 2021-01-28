package com.parkit.parkingsystem.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DataBaseConfigTest {

    @Mock
    Connection connectionMock;

    @Mock
    PreparedStatement preparedStatementMock;

    @Mock
    ResultSet resultSetMock;


    @Test
    void getConnection_whenGetConnection_ConnectionReturn() throws SQLException, ClassNotFoundException {
        //given

        try (MockedStatic<DriverManager> mocked = Mockito.mockStatic(DriverManager.class)) {
            DataBaseConfig dataBaseConfigUnderTest = new DataBaseConfig();
            mocked.when(() -> DriverManager.getConnection("jdbc:mysql://localhost:3306/prod?serverTimezone=UTC",
                    "root",
                    "rootroot"))
                    .thenReturn(connectionMock);
            //when
            Connection result = dataBaseConfigUnderTest.getConnection();

            //then
            mocked.verify(() -> DriverManager.getConnection("jdbc:mysql://localhost:3306/prod?serverTimezone=UTC",
                    "root",
                    "rootroot"));
            assertThat(result).isEqualTo(connectionMock);
        }


    }

    @Test
    void closeConnection_WhenConnectionNotNull_ShouldCloseConnection() throws SQLException, ClassNotFoundException {
        try (MockedStatic<DriverManager> mocked = Mockito.mockStatic(DriverManager.class)) {
            DataBaseConfig dataBaseConfigUnderTest = new DataBaseConfig();
            mocked.when(() -> DriverManager.getConnection("jdbc:mysql://localhost:3306/prod?serverTimezone=UTC",
                    "root",
                    "rootroot"))
                    .thenReturn(connectionMock);
            //when
            Connection resultConnection = dataBaseConfigUnderTest.getConnection();
            dataBaseConfigUnderTest.closeConnection(resultConnection);

            verify(connectionMock, times(1)).close();
            assertDoesNotThrow(() -> connectionMock.close());
        }
    }

    @Test
    void closePreparedStatement_WhenPreparedStatementNotNull_ShouldClosePreparedStatement() throws SQLException {

        //given
        DataBaseConfig dataBaseConfigUnderTest = new DataBaseConfig();

        //when
        assertDoesNotThrow(() -> dataBaseConfigUnderTest.closePreparedStatement(preparedStatementMock));
        //then
        verify(preparedStatementMock, times(1)).close();

    }

    @Test
    void closeResultSet_WhenResultSetNotNull_ShouldCloseResultSet() throws SQLException {
        //given
        DataBaseConfig dataBaseConfigUnderTest = new DataBaseConfig();

        //when
        assertDoesNotThrow(() -> dataBaseConfigUnderTest.closeResultSet(resultSetMock));
        //then
        verify(resultSetMock, times(1)).close();

    }
}