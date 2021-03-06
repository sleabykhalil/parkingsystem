package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket) {
        if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
            throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime().toString());
        }
        //getTime will return Time in milliseconds
        long inHour = ticket.getInTime().getTime();
        long outHour = ticket.getOutTime().getTime();

        //TODO: Some tests are failing here. Need to check if this logic is correct
        //Convert milliseconds to hours , using double ,to have hour fractions if exist
        double duration = ((double) outHour - (double) inHour) / 3600000;

        //If Duration mor than 30 for car and bike Minutes then will calculate price
        switch (ticket.getParkingSpot().getParkingType()) {
            case CAR: {
                if (duration > Fare.CAR_FREE_DURATION_PAR_HOUR) {
                        ticket.setPrice(duration * Fare.CAR_RATE_PER_HOUR);
                } else {
                    ticket.setPrice(0);
                }
                break;
            }
            case BIKE: {
                if (duration > Fare.BIKE_FREE_DURATION_PAR_HOUR) {
                        ticket.setPrice(duration * Fare.BIKE_RATE_PER_HOUR);
                } else {
                    ticket.setPrice(0);
                }
                break;
            }
            default:
                throw new IllegalArgumentException("Unknown Parking Type");
        }
    }

    public void calculateDiscount(Ticket ticket) {
        double discount = ticket.getPrice() * Fare.DISCOUNT_FOR_MORE_THAN_ONE_PREVIOUSLY_PARKING;
        ticket.setPrice(ticket.getPrice() - discount);
    }
}