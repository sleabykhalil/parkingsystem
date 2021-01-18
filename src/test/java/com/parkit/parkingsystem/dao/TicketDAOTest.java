package com.parkit.parkingsystem.dao;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.*;

import static org.assertj.core.api.Assertions.*;
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
     *
     * @throws SQLException
     */
    @Test
    @Tag("getTicket")
    @DisplayName("Test pass when ticket found")
    public void getTicketWhenTicketFoundShouldReturnTicket() throws SQLException {
        //given
        when(preparedStatementMock.executeQuery()).thenReturn(resultSetMock);
        when(resultSetMock.next()).thenReturn(true);
        when(resultSetMock.getInt(anyInt())).thenReturn(1);
        when(resultSetMock.getTimestamp(anyInt())).thenReturn(new Timestamp(System.currentTimeMillis()));
        when(resultSetMock.getString(6)).thenReturn("CAR");
        when(resultSetMock.getDouble(3)).thenReturn(1.5);

        //when
        Ticket resultTicket;
        resultTicket = ticketDAOUnderTest.getTicket("ABCDE");

        //then
        //test try statements are executed
        verify(connectionMock, times(1)).prepareStatement(anyString());
        verify(preparedStatementMock, times(1)).executeQuery();
        verify(resultSetMock, times(2)).getInt(anyInt());
        verify(resultSetMock, times(2)).getTimestamp(anyInt());
        verify(resultSetMock, times(1)).getString(6);
        verify(resultSetMock, times(1)).getDouble(3);
        //test try..finally statement is executed
        verify(dataBaseConfigMock, times(1)).closeConnection(connectionMock);
        assertThat(resultTicket.getId()).isEqualTo(1);
        assertThat(resultTicket.getVehicleRegNumber()).isEqualTo("ABCDE");

    }

    @Test
    @Tag("getTicket")
    public void getTicketWhenPreparedStatementThrowExceptionShouldCatchException() throws SQLException {
        //given
        when(connectionMock.prepareStatement(anyString())).thenThrow(SQLException.class);

        //when
        Ticket resultTicket;
        resultTicket = ticketDAOUnderTest.getTicket("ABCDE");

        //then
        //test try..catch throw exception
        assertThatThrownBy(() -> connectionMock.prepareStatement(anyString())).isInstanceOf(SQLException.class);
        //test that try statements are not executed
        verify(preparedStatementMock, times(0)).executeQuery();
        verify(resultSetMock, times(0)).getInt(anyInt());
        verify(resultSetMock, times(0)).getTimestamp(anyInt());
        verify(resultSetMock, times(0)).getString(6);
        verify(resultSetMock, times(0)).getDouble(3);
        //test try..finally statement is executed
        verify(dataBaseConfigMock, times(1)).closeConnection(connectionMock);
        assertThat(resultTicket).isNull();
    }

    /**
     * Test second branch when no result found no exception thrown
     *
     * @throws SQLException
     */
    @Test
    @Tag("getTicket")
    @DisplayName("Test pass when ticket found")
    public void getTicketWhenTicketNotFoundShouldResultSetGetIntNotRun() throws SQLException {
        //given
        when(preparedStatementMock.executeQuery()).thenReturn(resultSetMock);
        when(resultSetMock.next()).thenReturn(false);

        //when
        Ticket resultTicket;
        resultTicket = ticketDAOUnderTest.getTicket("ABCDE");

        //then
        //test try statements are executed
        verify(connectionMock, times(1)).prepareStatement(anyString());
        verify(preparedStatementMock, times(1)).executeQuery();
        verify(resultSetMock, times(0)).getInt(anyInt());
        verify(resultSetMock, times(0)).getTimestamp(anyInt());
        verify(resultSetMock, times(0)).getString(6);
        verify(resultSetMock, times(0)).getDouble(3);
        //test try..finally statement is executed
        verify(dataBaseConfigMock, times(1)).closeConnection(connectionMock);
        assertThat(resultTicket).isNull();
    }

    @Test
    @Tag("saveTicket")
    public void saveTicketWhenTicketPassedAsArgumentShouldExecuteUpdateQuery() throws SQLException, ClassNotFoundException {

        //given
        //prepare data for save query
        Ticket ticket = new Ticket();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber("ABCDE");
        ticket.setPrice(1.5);
        ticket.setInTime(new Timestamp(System.currentTimeMillis()));
        ticket.setOutTime(new Timestamp(System.currentTimeMillis()));
        when(preparedStatementMock.execute()).thenReturn(true);

        //when
        boolean resultSaveTicket;
        resultSaveTicket = ticketDAOUnderTest.saveTicket(ticket);

        //then
        assertThat(resultSaveTicket).isTrue();
        verify(dataBaseConfigMock, times(1)).getConnection();
        verify(preparedStatementMock, times(1)).setInt(1, 1);
        verify(preparedStatementMock, times(1)).setString(2, "ABCDE");
        verify(preparedStatementMock, times(1)).setDouble(3, 1.5);
        verify(preparedStatementMock, times(2)).setTimestamp(anyInt(), any(Timestamp.class));
        //verifying try..finally
        verify(dataBaseConfigMock, times(1)).closeConnection(connectionMock);
    }

    @Test
    @Tag("saveTicket")
    public void saveTicketWhenThrowSQLExceptionShouldCatchException() throws SQLException, ClassNotFoundException {

        //given
        //prepare data for save query
        Ticket ticket = new Ticket();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber("ABCDE");
        ticket.setPrice(1.5);
        ticket.setInTime(new Timestamp(System.currentTimeMillis()));
        ticket.setOutTime(new Timestamp(System.currentTimeMillis()));
        when(preparedStatementMock.execute()).thenThrow(SQLException.class);

        //when
        boolean resultSaveTicket;
        resultSaveTicket = ticketDAOUnderTest.saveTicket(ticket);

        //then
        verify(dataBaseConfigMock, times(1)).getConnection();
        verify(preparedStatementMock, times(1)).setInt(1, 1);
        verify(preparedStatementMock, times(1)).setString(2, "ABCDE");
        verify(preparedStatementMock, times(1)).setDouble(3, 1.5);
        verify(preparedStatementMock, times(2)).setTimestamp(anyInt(), any(Timestamp.class));
        assertThatExceptionOfType(SQLException.class).isThrownBy(()->preparedStatementMock.execute());
        //verifying try..finally
        verify(dataBaseConfigMock, times(1)).closeConnection(connectionMock);
        assertThat(resultSaveTicket).isFalse();

    }
    @Test
    @Tag("updateTicket")
    public void updateTicketWhenWhenTicketPassedAsArgumentShouldExecuteUpdateQuery() throws SQLException, ClassNotFoundException {
        //given
        //prepare data for update query
        Ticket ticket = new Ticket();
        ticket.setId(1);
        ticket.setPrice(1.5);
        ticket.setOutTime(new Timestamp(System.currentTimeMillis()));
        when(preparedStatementMock.execute()).thenReturn(true);

        //when
        boolean resultUpdateTicket;
        resultUpdateTicket = ticketDAOUnderTest.updateTicket(ticket);

        //then
        assertThat(resultUpdateTicket).isTrue();
        verify(dataBaseConfigMock, times(1)).getConnection();
        verify(preparedStatementMock, times(1)).setInt(3, 1);
        verify(preparedStatementMock, times(1)).setDouble(1, 1.5);
        //verify(preparedStatementMock, times(1)).setTimestamp(2, any(Timestamp.class));
        //verifying try..finally
        verify(dataBaseConfigMock, times(1)).closeConnection(connectionMock);
    }

    /**
     * Test get count for previous tickets for vehicle
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    @Test
    void getCountOfPreviousTicketsWhenTicketsFoundShouldReturnNumberOfTickets() throws SQLException, ClassNotFoundException {
        //given
        String vehicleRegNumber = "ABCDE";
        when(preparedStatementMock.executeQuery()).thenReturn(resultSetMock);
        when(resultSetMock.next()).thenReturn(true);
        when(resultSetMock.getInt(anyInt())).thenReturn(1);
        //when
        int resultTicketCounter;
        resultTicketCounter=ticketDAOUnderTest.getCountOfPreviousTickets(vehicleRegNumber);

        //then
        assertThat(resultTicketCounter).isEqualTo(1);
        verify(dataBaseConfigMock,times(1)).getConnection();
        verify(preparedStatementMock,times(1)).executeQuery();
        verify(preparedStatementMock,times(1)).setString(1,"ABCDE");
        verify(resultSetMock,times(1)).next();
        verify(resultSetMock,times(1)).getInt(anyInt());
        verify(dataBaseConfigMock,times(1)).closeConnection(connectionMock);
        verify(dataBaseConfigMock,times(1)).closePreparedStatement(preparedStatementMock);
    }
}