package com.sendByOP.expedition.services.iServices;

import java.util.Map;
import java.util.List;

public interface IStatisticsService {
    Map<String, Long> getFlightStatusDistribution();
    Map<String, Double> getMonthlyRevenue();
    Map<String, Long> getBookingTrends();
    Map<String, Long> getPopularRoutes();
    Map<String, Double> getAverageBookingValueByMonth();
    Long getTotalActiveFlights();
    Double getTotalRevenue();
    Long getTotalBookings();
}