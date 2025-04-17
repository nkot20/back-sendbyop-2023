package com.sendByOP.expedition.web.controller;

import com.sendByOP.expedition.services.iServices.IStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/statistics")
@CrossOrigin(origins = "*", maxAge = 3600)
public class StatisticsController {

    @Autowired
    private IStatisticsService statisticsService;

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboardStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        statistics.put("flightStatusDistribution", statisticsService.getFlightStatusDistribution());
        statistics.put("monthlyRevenue", statisticsService.getMonthlyRevenue());
        statistics.put("bookingTrends", statisticsService.getBookingTrends());
        statistics.put("popularRoutes", statisticsService.getPopularRoutes());
        statistics.put("averageBookingValueByMonth", statisticsService.getAverageBookingValueByMonth());
        statistics.put("totalActiveFlights", statisticsService.getTotalActiveFlights());
        statistics.put("totalRevenue", statisticsService.getTotalRevenue());
        statistics.put("totalBookings", statisticsService.getTotalBookings());

        return ResponseEntity.ok(statistics);
    }
}