package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    private static void setUp()  {
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    private void setUpPerTest() throws Exception {
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");
        dataBasePrepareService.clearDataBaseEntries();
    }

    @AfterAll
    private static void tearDown() {
        dataBasePrepareService.closeConnection();
    }

    @Test
    public void testParkingACarIT() {
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();
        //TODO: check that a ticket is actualy saved in DB and Parking table is updated with availability
        Ticket ticketAfterEntering = ticketDAO.getTicket("ABCDEF");
        Date closeToInDate = new Date();
        assertThat(ticketAfterEntering.getId()).isNotZero();
        assertThat(ticketAfterEntering.getVehicleRegNumber()).isEqualTo("ABCDEF");
        assertThat(ticketAfterEntering.getInTime()).isCloseTo(closeToInDate, 1000);
        assertThat(ticketAfterEntering.getOutTime()).isNull();
        assertThat(ticketAfterEntering.getPrice()).isEqualTo(0.0);

        //assert that next parking spot id is greater than the Id taken by ticketAfterEntering
        int minimumParkingSpotID;
        minimumParkingSpotID = parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR);
        assertThat(minimumParkingSpotID).isGreaterThan(ticketAfterEntering.getParkingSpot().getId());
        assertThat(minimumParkingSpotID).isEqualTo(ticketAfterEntering.getParkingSpot().getId() + 1);

        int numberOfTicketsFromDataBase = ticketDAO.getCountOfPreviousTickets("ABCDEF");
        assertEquals(1, numberOfTicketsFromDataBase);
    }

    @Test
    public void testParkingLotExitIT() throws Exception {
        //testParkingACar(); not a good practice to use another test in ongoing test
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        // Copy the some of code from processIncomingVehicle to mocking inTime for car entering
        ParkingSpot parkingSpot = parkingService.getNextParkingNumberIfAvailable();
        String vehicleRegNumber = inputReaderUtil.readVehicleRegistrationNumber();
        parkingSpot.setAvailable(false);
        parkingSpotDAO.updateParking(parkingSpot);//allot this parking space and mark it's availability as false
        Date inTime = new Date();
        inTime.setTime(inTime.getTime() - (60 * 60 * 1000)); //Mocking 1 hour of parking
        Ticket ticket = new Ticket();
        //ID, PARKING_NUMBER, VEHICLE_REG_NUMBER, PRICE, IN_TIME, OUT_TIME)
        //ticket.setId(ticketID);
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber(vehicleRegNumber);
        ticket.setPrice(0);
        ticket.setInTime(inTime);
        ticket.setOutTime(null);
        ticketDAO.saveTicket(ticket);


        parkingService.processExitingVehicle();
        //TODO: check that the fare generated and out time are populated correctly in the database
        Ticket ticketAfterExit = ticketDAO.getTicket(vehicleRegNumber);
        Date closeToOutDate = new Date();
        assertThat(ticketAfterExit.getPrice()).isCloseTo(Fare.CAR_RATE_PER_HOUR, Assertions.withinPercentage(1));
        assertThat(ticketAfterExit.getOutTime()).isCloseTo(closeToOutDate, 10000);
    }

    //Test returning last entering for the same vehicle
    @Test
    public void testParkingLotExitForTheSecondTimeShouldReturnGreatestIdIT() throws Exception {
        //given
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();
        parkingService.processExitingVehicle();
        parkingService.processIncomingVehicle();
        String vehicleRegNumber = inputReaderUtil.readVehicleRegistrationNumber();

        //when
        parkingService.processExitingVehicle();
        Ticket ticketAfterExit = ticketDAO.getTicket(vehicleRegNumber);

        //then
        assertThat(ticketAfterExit.getId()).isEqualTo(2);

    }

    @Test
    public void testParkingLotExitForTheSameVehicleRegistrationNumberButDefiantTypeTimeShouldReturnGreatestIdIT() throws Exception {
        //given
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();//enter as car
        when(inputReaderUtil.readSelection()).thenReturn(2);
        parkingService.processIncomingVehicle();//enter as bike with the same Vehicle Registration Number
        lenient().when(inputReaderUtil.readSelection()).thenReturn(1);

        String vehicleRegNumber = inputReaderUtil.readVehicleRegistrationNumber();

        //when
        parkingService.processExitingVehicle();//sort as car
        Ticket ticketAfterExit = ticketDAO.getTicket(vehicleRegNumber);

        //then
        assertThat(ticketAfterExit.getId()).isEqualTo(1);

    }

}
