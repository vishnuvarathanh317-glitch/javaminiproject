package com.hostelmanagement.algorithms;

import com.hostelmanagement.enums.BedStatus;
import com.hostelmanagement.model.Bed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Occupancy Calculator (Algorithm 4 & Division-by-Zero Defense)")
class OccupancyCalculatorTest {

    @Test
    @DisplayName("Empty or null bed list returns 0% without division-by-zero")
    void testEmptyList() {
        OccupancyCalculator.OccupancyStats statsNull = OccupancyCalculator.calculate(null);
        assertEquals(0, statsNull.getTotalBeds());
        assertEquals(0.0, statsNull.getOccupancyRate());

        OccupancyCalculator.OccupancyStats statsEmpty = OccupancyCalculator.calculate(new ArrayList<>());
        assertEquals(0, statsEmpty.getTotalBeds());
        assertEquals(0.0, statsEmpty.getOccupancyRate());
    }

    @Test
    @DisplayName("Beds only under maintenance yield 0 usable beds without division-by-zero")
    void testAllMaintenanceBeds() {
        Bed b1 = new Bed(101, "A1");
        b1.setStatus(BedStatus.UNDER_MAINTENANCE);
        Bed b2 = new Bed(101, "A2");
        b2.setStatus(BedStatus.UNDER_MAINTENANCE);

        List<Bed> beds = List.of(b1, b2);

        OccupancyCalculator.OccupancyStats stats = OccupancyCalculator.calculate(beds);
        assertEquals(2, stats.getTotalBeds());
        assertEquals(0, stats.getUsableBeds());
        assertEquals(2, stats.getMaintenanceBeds());
        assertEquals(0.0, stats.getOccupancyRate());
    }

    @Test
    @DisplayName("100% full occupancy correctly computed")
    void testFullOccupancy() {
        Bed b1 = new Bed(101, "A1");
        b1.setStatus(BedStatus.OCCUPIED);
        Bed b2 = new Bed(101, "A2");
        b2.setStatus(BedStatus.OCCUPIED);
        Bed b3 = new Bed(101, "A3");
        b3.setStatus(BedStatus.OCCUPIED);

        List<Bed> beds = List.of(b1, b2, b3);

        OccupancyCalculator.OccupancyStats stats = OccupancyCalculator.calculate(beds);
        assertEquals(3, stats.getTotalBeds());
        assertEquals(3, stats.getUsableBeds());
        assertEquals(3, stats.getOccupiedBeds());
        assertEquals(0, stats.getAvailableBeds());
        assertEquals(100.0, stats.getOccupancyRate());
    }

    @Test
    @DisplayName("Mixed occupancy: 1 occupied, 1 available, 1 maintenance gives 50% rate of usable beds")
    void testMixedOccupancy() {
        Bed b1 = new Bed(101, "A1");
        b1.setStatus(BedStatus.OCCUPIED);
        Bed b2 = new Bed(101, "A2");
        b2.setStatus(BedStatus.AVAILABLE);
        Bed b3 = new Bed(101, "A3");
        b3.setStatus(BedStatus.UNDER_MAINTENANCE);

        List<Bed> beds = List.of(b1, b2, b3);

        OccupancyCalculator.OccupancyStats stats = OccupancyCalculator.calculate(beds);
        assertEquals(3, stats.getTotalBeds());
        assertEquals(2, stats.getUsableBeds());
        assertEquals(1, stats.getOccupiedBeds());
        assertEquals(1, stats.getAvailableBeds());
        assertEquals(1, stats.getMaintenanceBeds());
        assertEquals(50.0, stats.getOccupancyRate());
    }
}
