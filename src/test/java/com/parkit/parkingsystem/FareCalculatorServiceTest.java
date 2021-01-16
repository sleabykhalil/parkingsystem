package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Date;

@ExtendWith(MockitoExtension.class)
public class FareCalculatorServiceTest {

    private static FareCalculatorService fareCalculatorService;
    private Ticket ticket;
    private Boolean discount;


    @Mock
    private static TicketDAO ticketDAOMock;

    @BeforeAll
    private static void setUp() {
        fareCalculatorService = new FareCalculatorService();
    }

    @BeforeEach
    private void setUpPerTest() {
        ticket = new Ticket();
        discount = false;
        ticket.setVehicleRegNumber("ABCD");
        fareCalculatorService.setTicketDAO(ticketDAOMock);
    }

    @Test
    public void calculateFareCar() {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (60 * 60 * 1000));
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals(ticket.getPrice(), Fare.CAR_RATE_PER_HOUR);
    }

    @Test
    public void calculateFareBike() {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (60 * 60 * 1000));
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals(ticket.getPrice(), Fare.BIKE_RATE_PER_HOUR);
    }

    @Test
    public void calculateFareUnkownType() {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (60 * 60 * 1000));
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, null, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        assertThrows(NullPointerException.class, () -> fareCalculatorService.calculateFare(ticket));
    }

    @Test
    public void calculateFareBikeWithFutureInTime() {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() + (60 * 60 * 1000));
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        assertThrows(IllegalArgumentException.class, () -> fareCalculatorService.calculateFare(ticket));
    }

    @Test
    public void calculateFareBikeWithLessThanOneHourParkingTime() {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (45 * 60 * 1000));//45 minutes parking time should give 3/4th parking fare
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals((0.75 * Fare.BIKE_RATE_PER_HOUR), ticket.getPrice());
    }

    @Test
    public void calculateFareCarWithLessThanOneHourParkingTime() {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (45 * 60 * 1000));//45 minutes parking time should give 3/4th parking fare
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals((0.75 * Fare.CAR_RATE_PER_HOUR), ticket.getPrice());
    }

    @Test
    public void calculateFareCarWithMoreThanADayParkingTime() {
        Date inTime = new Date();
        inTime.setTime(System.currentTimeMillis() - (24 * 60 * 60 * 1000));//24 hours parking time should give 24 * parking fare per hour
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals((24 * Fare.CAR_RATE_PER_HOUR), ticket.getPrice());
    }

    @Test
    public void freeForCarWithLessThanThirtyMinuets() {
        Date outTime = new Date();
        Date inTime = new Date();
        inTime.setTime(outTime.getTime() - (30 * 60 * 1000));//30 Minutes parking time should give 0
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals((0), ticket.getPrice());
    }

    @Test
    public void freeForBikeWithLessThanThirtyMinuets() {
        Date outTime = new Date();
        Date inTime = new Date();
        inTime.setTime(outTime.getTime() - (30 * 60 * 1000));//30 Minutes parking time should give 0
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals((0), ticket.getPrice());
    }

    /**
     * Test fare after discounting for Bick
     */
    @Test
    public void fivePerCentDiscountCalculateFeesForBikeWithPreviseParking() {
        //given
        when(ticketDAOMock.getPreviousTicketCount(anyString())).thenReturn(2);

        Date outTime = new Date();
        Date inTime = new Date();
        inTime.setTime(outTime.getTime() - (60 * 60 * 1000));//60 Minutes parking time
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        //when
        fareCalculatorService.calculateFare(ticket);

        //then
        assertEquals(Fare.BIKE_RATE_PER_HOUR - (Fare.DISCOUNT_FOR_MORE_THAN_ONE_PREVIOUSLY_PARKING * Fare.BIKE_RATE_PER_HOUR), ticket.getPrice());
    }

    /**
     * Test fare after discounting for Car
     */
    @Test
    public void fivePerCentDiscountCalculateFeesForCarWithPreviseParking() {
        //given
        when(ticketDAOMock.getPreviousTicketCount(anyString())).thenReturn(2);

        Date outTime = new Date();
        Date inTime = new Date();
        inTime.setTime(outTime.getTime() - (60 * 60 * 1000));//60 Minutes parking time
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        //when
        fareCalculatorService.calculateFare(ticket);

        //then
        assertEquals((Fare.CAR_RATE_PER_HOUR - (Fare.DISCOUNT_FOR_MORE_THAN_ONE_PREVIOUSLY_PARKING * Fare.CAR_RATE_PER_HOUR)), ticket.getPrice());
    }

    /**
     * Test if there are previous tickets
     * if there are previous tickets then the discount will be adapt
     *
     * @throws Exception
     */
    @Test
    //@DisplayName("Test if there are previous tickets to display discount message")
    public void givenFindPreviousParkTicket_whenDisplayDiscountMessage_thenDiscountMessageReturn() throws Exception {
        //given
        when(ticketDAOMock.getPreviousTicketCount("ABCD")).thenReturn(2);
        //when
        Double discount = fareCalculatorService.getDiscount("ABCD");
        //then
        assertThat(discount).isEqualTo(Fare.DISCOUNT_FOR_MORE_THAN_ONE_PREVIOUSLY_PARKING);
    }
}
