package com.parkit.parkingsystem.dao;

import com.parkit.parkingsystem.config.DataBaseConfig;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParkingSpotDAOTest {

    ParkingSpotDAO parkingSpotDAOUnderTest;

    @Mock
    DataBaseConfig dataBaseConfigMock;

    @Mock
    Connection connectionMock;

    @Mock
    PreparedStatement preparedStatementMock;

    @Mock
    ResultSet resultSetMock;

    @BeforeEach
    void setUp() throws SQLException, ClassNotFoundException {
        parkingSpotDAOUnderTest = new ParkingSpotDAO();
        parkingSpotDAOUnderTest.dataBaseConfig = dataBaseConfigMock;
        when(dataBaseConfigMock.getConnection()).thenReturn(connectionMock);
        when(connectionMock.prepareStatement(anyString())).thenReturn(preparedStatementMock);
    }

    @Test
    @Tag("getNextAvailableSlot")
    void getNextAvailableSlotWhenParkingSpotFoundShouldReturnSpotId() throws SQLException {
        //given
        when(preparedStatementMock.executeQuery()).thenReturn(resultSetMock);
        //test resultSet has value
        when(resultSetMock.next()).thenReturn(true);
        when(resultSetMock.getInt(1)).thenReturn(1);

        //when
        int result;
        result = parkingSpotDAOUnderTest.getNextAvailableSlot(ParkingType.valueOf("CAR"));

        //then
        assertThat(result).isEqualTo(1);
        verify(connectionMock, times(1)).prepareStatement(anyString());
        verify(preparedStatementMock, times(1)).executeQuery();
        verify(resultSetMock, times(1)).getInt(1);
        verify(dataBaseConfigMock, times(1)).closePreparedStatement(preparedStatementMock);
        verify(dataBaseConfigMock, times(1)).closeResultSet(resultSetMock);
        //try..finally
        verify(dataBaseConfigMock, times(1)).closeConnection(connectionMock);

    }

    @Test
    @Tag("getNextAvailableSlot")
    void getNextAvailableSlotWhenParkingSpotNotFoundShouldReturnMinusOne() throws SQLException {
        //given
        when(preparedStatementMock.executeQuery()).thenReturn(resultSetMock);
        //test resultSet don't has value
        when(resultSetMock.next()).thenReturn(false);
        //when
        int result;
        result = parkingSpotDAOUnderTest.getNextAvailableSlot(ParkingType.valueOf("CAR"));

        //then
        assertThat(result).isEqualTo(-1);
        verify(connectionMock, times(1)).prepareStatement(anyString());
        verify(preparedStatementMock, times(1)).executeQuery();
        verify(resultSetMock, times(0)).getInt(1);
        verify(dataBaseConfigMock, times(1)).closePreparedStatement(preparedStatementMock);
        verify(dataBaseConfigMock, times(1)).closeResultSet(resultSetMock);
        //try..finally
        verify(dataBaseConfigMock, times(1)).closeConnection(connectionMock);

    }

    @Test
    @Tag("getNextAvailableSlot")
    void getNextAvailableSlotWhenExceptionTrowsShouldReturnMinusOne() throws SQLException {
        //given
        //test when exception throws
        when(preparedStatementMock.executeQuery()).thenThrow(SQLException.class);

        //when
        int result;
        result = parkingSpotDAOUnderTest.getNextAvailableSlot(ParkingType.valueOf("CAR"));

        //then
        verify(preparedStatementMock,times(1)).executeQuery();
        assertThatThrownBy(() -> preparedStatementMock.executeQuery()).isInstanceOf(SQLException.class);
        verify(connectionMock, times(1)).prepareStatement(anyString());
        verify(resultSetMock, times(0)).getInt(1);
        verify(dataBaseConfigMock, times(0)).closePreparedStatement(preparedStatementMock);
        verify(dataBaseConfigMock, times(0)).closeResultSet(resultSetMock);
        //try..finally
        verify(dataBaseConfigMock, times(1)).closeConnection(connectionMock);
        assertThat(result).isEqualTo(-1);

    }

    @Test
    @Tag("updateParking")
    void updateParkingWhenUpdateShouldReturnTrue() throws SQLException {
        //given
        when(preparedStatementMock.executeUpdate()).thenReturn(1);
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, true);
        //when
        boolean result;
        result = parkingSpotDAOUnderTest.updateParking(parkingSpot);

        //then
        assertThat(result).isTrue();
        verify(connectionMock, times(1)).prepareStatement(anyString());
        verify(preparedStatementMock, times(1)).setBoolean(1, true);
        verify(preparedStatementMock, times(1)).setInt(2, 1);
        verify(dataBaseConfigMock, times(1)).closePreparedStatement(preparedStatementMock);
        //test try..finally
        verify(dataBaseConfigMock, times(1)).closeConnection(connectionMock);
    }

    @Test
    @Tag("updateParking")
    void updateParkingWhenThrowExceptionShouldReturnFalse() throws SQLException {
        //given
        when(preparedStatementMock.executeUpdate()).thenThrow(SQLException.class);
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, true);

        //when
        boolean result;
        result = parkingSpotDAOUnderTest.updateParking(parkingSpot);

        //then
        assertThat(result).isFalse();
        verify(connectionMock, times(1)).prepareStatement(anyString());
        verify(preparedStatementMock, times(1)).executeUpdate();
        assertThatThrownBy(() -> {
            preparedStatementMock.executeUpdate();
        }).isInstanceOf(SQLException.class);
        verify(dataBaseConfigMock, times(0)).closePreparedStatement(preparedStatementMock);

        //test try..finally
        verify(dataBaseConfigMock, times(1)).closeConnection(connectionMock);

    }

}