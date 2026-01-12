package com.sendByOP.expedition.services.impl;

import com.sendByOP.expedition.models.entities.Booking;
import com.sendByOP.expedition.models.entities.Flight;
import com.sendByOP.expedition.models.entities.Payment;
import com.sendByOP.expedition.repositories.FlightRepository;
import com.sendByOP.expedition.repositories.PaymentRepository;
import com.sendByOP.expedition.repositories.ReservationRepository;
import com.sendByOP.expedition.services.iServices.IStatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements IStatisticsService {

    private final FlightRepository flightRepository;

    private final ReservationRepository bookingRepository;

    private final PaymentRepository paymentRepository;

    @Override
    public Map<String, Long> getFlightStatusDistribution() {
        List<Flight> flights = (List<Flight>) flightRepository.findAll();
        return flights.stream()
                .collect(Collectors.groupingBy(
                        flight -> getStatusLabel(flight.getValidationStatus()),
                        Collectors.counting()
                ));
    }

    @Override
    public Map<String, Double> getMonthlyRevenue() {
        List<Payment> payments = (List<Payment>) paymentRepository.findAll();
        return payments.stream()
                .collect(Collectors.groupingBy(
                        payment -> getMonthYear(payment.getPaymentDate()),
                        Collectors.summingDouble(Payment::getAmount)
                ));
    }

    @Override
    public Map<String, Long> getBookingTrends() {
        List<Booking> bookings = (List<Booking>) bookingRepository.findAll();
        return bookings.stream()
                .collect(Collectors.groupingBy(
                        booking -> getMonthYear(booking.getBookingDate()),
                        Collectors.counting()
                ));
    }

    @Override
    public Map<String, Long> getPopularRoutes() {
        List<Flight> flights = (List<Flight>) flightRepository.findAll();
        return flights.stream()
                .collect(Collectors.groupingBy(
                        flight -> flight.getDepartureAirport().getName() + " → " + flight.getArrivalAirport().getName(),
                        Collectors.counting()
                ));
    }

    @Override
    public Map<String, Double> getAverageBookingValueByMonth() {
        List<Payment> payments = (List<Payment>) paymentRepository.findAll();
        Map<String, List<Double>> monthlyPayments = payments.stream()
                .collect(Collectors.groupingBy(
                        payment -> getMonthYear(payment.getPaymentDate()),
                        Collectors.mapping(Payment::getAmount, Collectors.toList())
                ));

        return monthlyPayments.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().stream().mapToDouble(Double::doubleValue).average().orElse(0.0)
                ));
    }

    @Override
    public Long getTotalActiveFlights() {
        List<Flight> flights = (List<Flight>) flightRepository.findAll();
        return flights.stream()
                .filter(flight -> flight.getCancelled() == 0)
                .count();
    }

    @Override
    public Double getTotalRevenue() {
        return paymentRepository.findAll().stream()
                .mapToDouble(Payment::getAmount)
                .sum();
    }

    @Override
    public Long getTotalBookings() {
        return bookingRepository.count();
    }

    private String getStatusLabel(int status) {
        switch (status) {
            case 0: return "Pending";
            case 1: return "Approved";
            case 2: return "Rejected";
            default: return "Unknown";
        }
    }

    private String getMonthYear(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return String.format("%d-%02d", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1);
    }
}