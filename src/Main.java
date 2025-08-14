import java.time.LocalDate;
import java.util.List;
import java.util.Arrays;


public class Main {
    public static void main(String[] args) {
        System.out.println("=== INTEGRATED TRANSPORT SYSTEM DEMO ===\n");


        List<String> stops = Arrays.asList("Mirpur-10", "Kochukhet", "Banani");
        List<String> stops1 = Arrays.asList("Uttara Center", "Uttara South", "Pallabi", "Mirpur-11", "Mirpur-10");


        Route route = new Route(1, "Mirpur", "UCSI University Campus", 2.5, "km", stops);
        Route route1 = new Route(2, "Uttara-North", "Kazipara", 12.0, "km", stops1);

        Route route2 = new Route(3, "Dhanmondi", "Uttara", 15.0);  // Constructor 4
        Route route3 = new Route(4, "Gulshan", "Motijheel", 85.0, true);  // Constructor 3

        Bus bus = new Bus("B001", 40, "AVAILABLE", 2020, LocalDate.now().minusMonths(6), "Green Line");
        Train train = new Train("T001", 300, "AVAILABLE", 2022, LocalDate.now().minusMonths(3),
                TrainClass.BUSINESS, "Dhaka Metro");


        bus.assignToRoute(route);
        train.assignToRoute(route1);

        System.out.println("--- ROUTES ---");
        route.displayRouteInfo();
        System.out.println();
        route1.displayRouteInfo();
        System.out.println();

        System.out.println("--- VEHICLES ---");
        bus.displayDetails();
        System.out.println();
        train.displayDetails();
        System.out.println();

        System.out.println("--- VEHICLE SPECIFICATIONS ---");
        System.out.println(bus.getVehicleSpecifications());
        System.out.println();
        System.out.println(train.getVehicleSpecifications());
        System.out.println();

        System.out.println("--- SAFETY CHECKS ---");
        System.out.println("Bus safety check: " + (bus.performSafetyCheck() ? "✅ PASSED" : "❌ FAILED"));
        System.out.println("Train safety check: " + (train.performSafetyCheck() ? "✅ PASSED" : "❌ FAILED"));
        System.out.println();

        System.out.println("--- OPERATIONAL COSTS ---");
        System.out.println("Bus operational cost: ৳" + String.format("%.2f", bus.calculateOperationalCost()));
        System.out.println("Train operational cost: ৳" + String.format("%.2f", train.calculateOperationalCost()));
        System.out.println();

        System.out.println("--- SEAT BOOKING DEMO ---");
        System.out.println("Bus available seats before booking: " + bus.getAvailableSeats());
        if (bus.bookSeat()) {
            System.out.println("✅ Bus seat booked successfully!");
            System.out.println("Bus available seats after booking: " + bus.getAvailableSeats());
        }

        System.out.println("Train available seats before booking: " + train.getAvailableSeats());
        if (train.bookSeat()) {
            System.out.println("✅ Train seat booked successfully!");
            System.out.println("Train available seats after booking: " + train.getAvailableSeats());
        }
        System.out.println();

        System.out.println("--- ROUTE INFORMATION ---");
        System.out.println("Route 1 base fare: ৳" + String.format("%.2f", route.getBaseFare()));
        System.out.println("Route 1 distance: " + route.getDistance() + " " + route.getDistanceUnit());
        System.out.println("Route 1 stops: " + route.getStops());
        System.out.println("Route 1 is active: " + route.isActive());
        System.out.println();

        System.out.println("Route 2 base fare: ৳" + String.format("%.2f", route1.getBaseFare()));
        System.out.println("Route 2 distance: " + route1.getDistance() + " " + route1.getDistanceUnit());
        System.out.println("Route 2 stops: " + route1.getStops());
        System.out.println("Route 2 is active: " + route1.isActive());
        System.out.println();

        System.out.println("--- PERFORMANCE METRICS ---");
        System.out.println("Bus utilization rate: " + String.format("%.2f", bus.getUtilizationRate()) + "%");
        System.out.println("Train utilization rate: " + String.format("%.2f", train.getUtilizationRate()) + "%");
        System.out.println("Bus performance status: " + bus.getPerformanceStatus());
        System.out.println("Train performance status: " + train.getPerformanceStatus());
        System.out.println();

        System.out.println("\n=== SYSTEM INTEGRATION COMPLETE ===");
    }
}
