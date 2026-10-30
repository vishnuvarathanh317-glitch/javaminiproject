package com.hostelmanagement.algorithms;

import com.hostelmanagement.enums.BedStatus;
import com.hostelmanagement.model.Bed;

import java.util.List;

/**
 * Occupancy Statistics Algorithm (Algorithm 4).
 *
 * Implements strict zero-division protection, consistent counting across
 * usable beds, occupied beds, and maintenance statuses.
 */
public class OccupancyCalculator {

    public static class OccupancyStats {
        private final int totalBeds;
        private final int usableBeds;
        private final int occupiedBeds;
        private final int availableBeds;
        private final int maintenanceBeds;
        private final double occupancyRate;

        public OccupancyStats(int totalBeds, int usableBeds, int occupiedBeds,
                              int availableBeds, int maintenanceBeds, double occupancyRate) {
            this.totalBeds = totalBeds;
            this.usableBeds = usableBeds;
            this.occupiedBeds = occupiedBeds;
            this.availableBeds = availableBeds;
            this.maintenanceBeds = maintenanceBeds;
            this.occupancyRate = occupancyRate;
        }

        public int getTotalBeds() { return totalBeds; }
        public int getUsableBeds() { return usableBeds; }
        public int getOccupiedBeds() { return occupiedBeds; }
        public int getAvailableBeds() { return availableBeds; }
        public int getMaintenanceBeds() { return maintenanceBeds; }
        public double getOccupancyRate() { return occupancyRate; }
    }

    public static OccupancyStats calculate(List<Bed> beds) {
        if (beds == null || beds.isEmpty()) {
            return new OccupancyStats(0, 0, 0, 0, 0, 0.0);
        }

        int total = beds.size();
        int occupied = 0;
        int available = 0;
        int maintenance = 0;

        for (Bed b : beds) {
            if (b.getStatus() == BedStatus.OCCUPIED) {
                occupied++;
            } else if (b.getStatus() == BedStatus.AVAILABLE) {
                available++;
            } else if (b.getStatus() == BedStatus.UNDER_MAINTENANCE) {
                maintenance++;
            }
        }

        int usable = occupied + available;
        // Occupancy percentage formula: (occupied / usable) * 100, guarding against usable == 0
        double rate = usable > 0 ? ((double) occupied / usable) * 100.0 : 0.0;

        return new OccupancyStats(total, usable, occupied, available, maintenance, Math.round(rate * 10.0) / 10.0);
    }
}
