package com.hostelmanagement.ai;

import com.hostelmanagement.enums.BedStatus;
import com.hostelmanagement.enums.RoomType;
import com.hostelmanagement.model.Bed;
import com.hostelmanagement.model.RoomRecommendation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Room Recommendation Engine (AI Algorithm & PriorityQueue)")
class RoomRecommendationEngineTest {

    private RoomRecommendationEngine engine;

    @BeforeEach
    void setUp() {
        engine = new RoomRecommendationEngine();
    }

    @Test
    @DisplayName("Empty or null bed list returns empty recommendations")
    void testEmptyList() {
        assertTrue(engine.recommendBeds(null, null).isEmpty());
        assertTrue(engine.recommendBeds(new ArrayList<>(), null).isEmpty());
    }

    @Test
    @DisplayName("PriorityQueue orders beds with matching preferences at the top")
    void testRankedByPreference() {
        List<Bed> beds = new ArrayList<>();

        // Bed 1: Block A, Floor 2, Room 201 (Bed 1A)
        Bed b1 = new Bed(101, "A-1");
        b1.setId(1);
        b1.setStatus(BedStatus.AVAILABLE);
        b1.setBlockName("Block A");
        b1.setFloorNumber(2);
        b1.setRoomNumber("201");
        beds.add(b1);

        // Bed 2: Block B, Floor 1 (lower floor), Room 102 (Bed 2A)
        Bed b2 = new Bed(102, "B-1");
        b2.setId(2);
        b2.setStatus(BedStatus.AVAILABLE);
        b2.setBlockName("Block B");
        b2.setFloorNumber(1);
        b2.setRoomNumber("102");
        beds.add(b2);

        // Bed 3: Block A, Floor 1 (Matches Block + Floor + Ground Bonus)
        Bed b3 = new Bed(103, "A-2");
        b3.setId(3);
        b3.setStatus(BedStatus.AVAILABLE);
        b3.setBlockName("Block A");
        b3.setFloorNumber(1);
        b3.setRoomNumber("103");
        beds.add(b3);

        RoomRecommendationEngine.Preference pref =
                new RoomRecommendationEngine.Preference("Block A", 1, RoomType.DOUBLE);

        List<RoomRecommendation> ranked = engine.recommendBeds(beds, pref);

        assertEquals(3, ranked.size());
        // b3 gets: Base 50 + Block match 25 + Floor match 15 + Floor 1 bonus 5 = 95.0
        assertEquals(3, ranked.get(0).getBed().getId());
        assertEquals(95.0, ranked.get(0).getScore());
        assertTrue(ranked.get(0).getExplanation().contains("Matches preferred block (Block A)"));
        assertTrue(ranked.get(0).getExplanation().contains("Matches preferred floor (Floor 1)"));
        assertTrue(ranked.get(0).getExplanation().contains("Convenient ground/lower floor access"));

        // Ranked strictly descending by score
        assertTrue(ranked.get(0).getScore() >= ranked.get(1).getScore());
        assertTrue(ranked.get(1).getScore() >= ranked.get(2).getScore());
    }

    @Test
    @DisplayName("Beds have transparent, explainable rationale")
    void testExplainableRationale() {
        Bed b = new Bed(201, "Bed-X");
        b.setId(10);
        b.setStatus(BedStatus.AVAILABLE);
        b.setFloorNumber(3);
        b.setBlockName("North Wing");
        b.setRoomNumber("301");

        List<RoomRecommendation> recommendations = engine.recommendBeds(List.of(b), null);

        assertFalse(recommendations.isEmpty());
        RoomRecommendation top = recommendations.get(0);
        assertNotNull(top.getExplanation());
        assertTrue(top.getExplanation().contains("Bed is verified available"));
    }
}
