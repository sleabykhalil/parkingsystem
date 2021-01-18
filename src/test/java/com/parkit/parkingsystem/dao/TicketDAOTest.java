package com.parkit.parkingsystem.dao;

import com.mysql.cj.protocol.InternalDate;
import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.Ticket;
import org.apache.logging.log4j.core.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.DATE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketDAOTest {

    TicketDAO ticketDAOUnderTest;

    @Mock
    DataBaseConfig dataBaseConfigMock;

    @Mock
    Connection connectionMock;

    @Mock
    PreparedStatement preparedStatementMock;

    @Mock
    ResultSet resultSetMock;

    @BeforeEach
    public void setup() throws SQLException, ClassNotFoundException {
        ticketDAOUnderTest = new TicketDAO();
        when(dataBaseConfigMock.getConnection()).thenReturn(connectionMock);
        when(connectionMock.prepareStatement(anyString())).thenReturn(preparedStatementMock);
        ticketDAOUnderTest.dataBaseConfig = dataBaseConfigMock;

    }

    /**
     * Test first branch when positive status no exception thrown
     * @throws SQLException
     */
    @Test
    @DisplayName("Test pass when ticket found")
    public void getTicketWhenTicketFoundShouldCreateResultSet() throws SQLException {
        //given
        when(preparedStatementMock.executeQuery()).thenReturn(resultSetMock);
        when(resultSetMock.next()).thenReturn(true);
        when(resultSetMock.getInt(anyInt())).thenReturn(1);
        when(resultSetMock.getTimestamp(anyInt())).thenReturn(new Timestamp(System.currentTimeMillis()));
        when(resultSetMock.getString(6)).thenReturn("CAR");
        when(resultSetMock.getDouble(3)).thenReturn(1.5);

        //when
        ticketDAOUnderTest.getTicket("ABCDE");

        //then
        //test try statements are executed
        verify(connectionMock, times(1)).prepareStatement(anyString());
        verify(preparedStatementMock, times(1)).executeQuery();
        verify(resultSetMock, times(2)).getInt(anyInt());
        verify(resultSetMock, times(2)).getTimestamp(anyInt());
        verify(resultSetMock, times(1)).getString(6);
        verify(resultSetMock, times(1)).getDouble(3);
        //test try..finally statement is executed
        verify(dataBaseConfigMock,times(1)).closeConnection(connectionMock);

    }

    @Test
    public void getTicketWhenPreparedStatementThrowExceptionShouldCatchException() throws SQLException {
        //given
        when(connectionMock.prepareStatement(anyString())).thenThrow(SQLException.class);

        //when
        ticketDAOUnderTest.getTicket("ABCDE");

        //then
        //test try..catch throw exception
        assertThatThrownBy(()->connectionMock.prepareStatement(anyString())).isInstanceOf(SQLException.class);
        //test that try statements are not executed
        verify(preparedStatementMock, times(0)).executeQuery();
        verify(resultSetMock, times(0)).getInt(anyInt());
        verify(resultSetMock, times(0)).getTimestamp(anyInt());
        verify(resultSetMock, times(0)).getString(6);
        verify(resultSetMock, times(0)).getDouble(3);
        //test try..finally statement is executed
        verify(dataBaseConfigMock,times(1)).closeConnection(connectionMock);
    }

    /**
     * Test second branch when no result found no exception thrown
     * @throws SQLException
     */
    @Test
    @DisplayName("Test pass when ticket found")
    public void getTicketWhenTicketNotFoundShouldResultSetGetIntNotRun() throws SQLException {
        //given
        when(preparedStatementMock.executeQuery()).thenReturn(resultSetMock);
        when(resultSetMock.next()).thenReturn(false);

        //when
        ticketDAOUnderTest.getTicket("ABCDE");

        //then
        //test try statements are executed
        verify(connectionMock, times(1)).prepareStatement(anyString());
        verify(preparedStatementMock, times(1)).executeQuery();
        verify(resultSetMock, times(0)).getInt(anyInt());
        verify(resultSetMock, times(0)).getTimestamp(anyInt());
        verify(resultSetMock, times(0)).getString(6);
        verify(resultSetMock, times(0)).getDouble(3);
        //test try..finally statement is executed
        verify(dataBaseConfigMock,times(1)).closeConnection(connectionMock);

    }

}