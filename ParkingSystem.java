import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.io.*;

public class ParkingSystem {
    private static final String BLUE = "\u001B[34m";
    private static final String CYAN = "\u001B[36m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String RED = "\u001B[31m";
    private static final String RESET = "\u001B[0m";

    private static final Scanner SC = new Scanner(System.in);
    private static final DateTimeFormatter TF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter PRINTTF = DateTimeFormatter.ofPattern("MMM dd yyyy | hh:mm:ss a");

    public static void main(String[] args) {
        ParkingLot lot = new ParkingLot();
        while (true) {
            clear();
            printBoxHeader("SMART PARKING MANAGEMENT SYSTEM");
            printMainMenu();
            System.out.print(YELLOW + "Enter choice (1-8): " + RESET);
            String choice = SC.nextLine().trim();
            try {
                switch (choice) {
                    case "1": handlePark(lot); break;
                    case "2": handleUnpark(lot); break;
                    case "3": handleVacancy(lot); break;
                    case "4": handleSlotMap(lot); break;
                    case "5": handleReports(lot); break;
                    case "6": handleExportCsv(lot); break;
                    case "7": handleActiveList(lot); break;
                    case "8": System.out.println(GREEN + "Exiting. Goodbye!" + RESET); return;
                    default: System.out.println(RED + "Invalid option." + RESET); pause();
                }
            } catch (Exception ex) {
                System.out.println(RED + "Error: " + ex.getMessage() + RESET);
                pause();
            }
        }
    }

    private static void handlePark(ParkingLot lot) {
        clear();
        printBoxTitle("PARK VEHICLE");
        System.out.println("Select vehicle type:");
        System.out.println("[1] Motorcycle");
        System.out.println("[2] Four-wheel (Car)");
        System.out.print(YELLOW + "Type: " + RESET);
        String t = SC.nextLine().trim();
        if (!t.equals("1") && !t.equals("2")) { System.out.println(RED + "Invalid type." + RESET); pause(); return; }
        System.out.print("Plate number: ");
        String plate = SC.nextLine().trim().toUpperCase();
        if (plate.isEmpty()) { System.out.println(RED + "Plate cannot be empty." + RESET); pause(); return; }
        LocalDateTime now = LocalDateTime.now();
        if (t.equals("1")) {
            Vehicle v = new Motorcycle(plate);
            Result r = lot.park(v, now);
            printResult(r);
        } else {
            System.out.print("Preferred section (A/B) - leave blank for automatic: ");
            String s = SC.nextLine().trim().toUpperCase();
            Vehicle v = s.equals("B") ? new FourWheelB(plate) : new FourWheelA(plate);
            Result r = lot.park(v, now);
            printResult(r);
        }
        pause();
    }

    private static void handleUnpark(ParkingLot lot) {
        clear();
        printBoxTitle("UNPARK VEHICLE");
        System.out.print("Plate number: ");
        String plate = SC.nextLine().trim().toUpperCase();
        if (plate.isEmpty()) { System.out.println(RED + "Plate cannot be empty." + RESET); pause(); return; }
        LocalDateTime now = LocalDateTime.now();
        UnparkResult ur = lot.unparkWithPayment(plate, now);
        if (!ur.success) {
            System.out.println(RED + ur.message + RESET);
            pause();
            return;
        }
        printReceipt(ur.receipt);
        pause();
    }

    private static void handleVacancy(ParkingLot lot) {
        clear();
        printBoxTitle("VACANCY / OCCUPANCY");
        Map<String, Integer> m = lot.vacancySummary();
        System.out.printf("%-28s : %d%n", "Motorcycle vacancies", m.get("M"));
        System.out.printf("%-28s : %d%n", "4-wheel Section A vacancies", m.get("A"));
        System.out.printf("%-28s : %d%n", "4-wheel Section B vacancies", m.get("B"));
        System.out.printf("%-28s : %.2f%%%n", "Overall occupancy rate", lot.occupancyRate());
        pause();
    }

    private static void handleSlotMap(ParkingLot lot) {
        clear();
        printBoxTitle("SLOT MAP");
        int mUsed = lot.countUsedByPrefix("M");
        int aUsed = lot.countUsedByPrefix("A");
        int bUsed = lot.countUsedByPrefix("B");
        int mTotal = lot.totalMoto();
        int aTotal = lot.totalA();
        int bTotal = lot.totalB();
        String filled = "#";
        String empty = "-";
        System.out.println("Motorcycle Slots:");
        System.out.println("MC: " + filled.repeat(Math.max(0, mUsed)) + empty.repeat(Math.max(0, mTotal - mUsed)) + "  (" + mUsed + "/" + mTotal + ")");
        System.out.println();
        System.out.println("Section A:");
        printSlotBlocks(lot, "A", 10);
        System.out.println();
        System.out.println("Section B:");
        printSlotBlocks(lot, "B", 10);
        pause();
    }

    private static void printSlotBlocks(ParkingLot lot, String prefix, int perLine) {
        List<String> ids = lot.slotIdsByPrefix(prefix);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ids.size(); i++) {
            String id = ids.get(i);
            boolean occ = lot.isOccupied(id);
            sb.append(occ ? "#" : "-");
            if ((i+1) % perLine == 0) {
                System.out.println(sb.toString() + "  (" + countPrefixUsedTill(lot, prefix, i) + "/" + ids.size() + ")");
                sb.setLength(0);
            }
        }
        if (sb.length() > 0) System.out.println(sb.toString() + "  (" + countPrefixUsedTill(lot, prefix, ids.size()-1) + "/" + ids.size() + ")");
    }

    private static int countPrefixUsedTill(ParkingLot lot, String prefix, int idx) {
        List<String> ids = lot.slotIdsByPrefix(prefix);
        int c = 0;
        for (int i = 0; i <= idx && i < ids.size(); i++) if (lot.isOccupied(ids.get(i))) c++;
        return c;
    }

    private static void handleReports(ParkingLot lot) {
        clear();
        printBoxTitle("REPORTS");
        String now = LocalDateTime.now().format(PRINTTF);
        System.out.println("Report Generated: " + YELLOW + now + RESET);
        System.out.println("Total slots: " + lot.totalSlots());
        System.out.println("Currently parked: " + lot.getActiveCount());
        System.out.printf("Overall occupancy rate: %.2f%%%n", lot.occupancyRate());
        System.out.println("\nRecent history (last 10):");
        List<LogEntry> recent = lot.getRecentHistory(10);
        if (recent.isEmpty()) System.out.println("  No history yet.");
        else for (LogEntry e : recent) System.out.println("  " + e.toString());
        System.out.println("\nPeak hours (top 3):");
        lot.peakHours().entrySet().stream().sorted((a,b)->Long.compare(b.getValue(), a.getValue())).limit(3)
            .forEach(en -> System.out.printf("  Hour %02d:00 -> %d entries%n", en.getKey(), en.getValue()));
        System.out.println();
        System.out.println(GREEN + "Total Revenue: ₱" + String.format("%.2f", lot.totalRevenue()) + RESET);
        pause();
    }

    private static void handleExportCsv(ParkingLot lot) {
        clear();
        printBoxTitle("EXPORT TO CSV");
        System.out.print("Filename (default: parking_history.csv): ");
        String fn = SC.nextLine().trim();
        if (fn.isEmpty()) fn = "parking_history.csv";
        String res = lot.exportHistoryToCSV(fn);
        if (res.startsWith("Exported")) System.out.println(GREEN + res + RESET); else System.out.println(RED + res + RESET);
        pause();
    }

    private static void handleActiveList(ParkingLot lot) {
        clear();
        printBoxTitle("ACTIVE PARKED VEHICLES");
        List<LogEntry> active = lot.activeList();
        if (active.isEmpty()) System.out.println("No active parked vehicles.");
        else {
            for (LogEntry e : active) System.out.println("  " + e.getPlate() + " | " + e.getVehicleTypeShort() + " | Entered: " + e.getEntryTime().format(PRINTTF));
        }
        System.out.println();
        System.out.println("[S]earch by plate    [ENTER] to go back");
        String s = SC.nextLine().trim();
        if (s.equalsIgnoreCase("S")) {
            System.out.print("Enter plate to search: ");
            String plate = SC.nextLine().trim().toUpperCase();
            Optional<LogEntry> found = active.stream().filter(le -> le.getPlate().equals(plate)).findFirst();
            if (found.isPresent()) System.out.println(GREEN + "Found active: " + found.get().toString() + RESET);
            else System.out.println(RED + "No active vehicle with plate " + plate + RESET);
            pause();
        }
    }

    private static void printReceipt(Receipt r) {
        System.out.println();
        System.out.println("========================================");
        System.out.println("         PARKING PAYMENT RECEIPT");
        System.out.println("========================================");
        System.out.println("Plate Number : " + r.plate);
        System.out.println("Vehicle Type : " + r.vehicleType);
        System.out.println("Slot ID      : " + r.slotId);
        System.out.println("Time In      : " + r.entry.format(PRINTTF));
        System.out.println("Time Out     : " + r.exit.format(PRINTTF));
        System.out.println("Duration     : " + r.hoursBilled + " hour(s) billed");
        System.out.println("Amount Due   : ₱" + String.format("%.2f", r.amount));
        System.out.println("========================================");
        System.out.println("Thank you! Drive safely!");
    }

    private static void printResult(Result r) {
        if (r.success) System.out.println(GREEN + r.message + RESET);
        else System.out.println(RED + r.message + RESET);
    }

    private static void printMainMenu() {
        System.out.println("[1] Park vehicle");
        System.out.println("[2] Unpark vehicle");
        System.out.println("[3] Vacancy / Occupancy");
        System.out.println("[4] Slot Map");
        System.out.println("[5] Reports (usage, peak hours)");
        System.out.println("[6] Export logs to CSV");
        System.out.println("[7] Active parked vehicles (search)");
        System.out.println("[8] Exit");
        System.out.println();
    }

    private static void printBoxHeader(String title) {
        System.out.println(BLUE + "+--------------------------------------------------+" + RESET);
        System.out.println(CYAN + "|  " + center(title, 46) + "  |" + RESET);
        System.out.println(BLUE + "+--------------------------------------------------+" + RESET);
    }

    private static void printBoxTitle(String title) {
        System.out.println();
        System.out.println("========================================");
        System.out.println(CYAN + " " + title + RESET);
        System.out.println("========================================");
    }

    private static String center(String s, int width) {
        if (s.length() >= width) return s.substring(0, width);
        int left = (width - s.length())/2;
        return " ".repeat(left) + s + " ".repeat(width - s.length() - left);
    }

    private static void clear() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    private static void pause() {
        System.out.print("\nPress ENTER to continue...");
        SC.nextLine();
    }

    static abstract class Vehicle {
        private final String plate;
        private final LocalDateTime createdAt;
        protected Vehicle(String plate) { this.plate = plate; this.createdAt = LocalDateTime.now(); }
        public String getPlate() { return plate; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public abstract String getTypeName();
        public abstract String preferredSlotPrefix();
    }

    static class Motorcycle extends Vehicle {
        Motorcycle(String plate) { super(plate); }
        @Override public String getTypeName() { return "Motorcycle"; }
        @Override public String preferredSlotPrefix() { return "M"; }
    }

    static class FourWheelA extends Vehicle {
        FourWheelA(String plate) { super(plate); }
        @Override public String getTypeName() { return "FourWheel-A"; }
        @Override public String preferredSlotPrefix() { return "A"; }
    }

    static class FourWheelB extends Vehicle {
        FourWheelB(String plate) { super(plate); }
        @Override public String getTypeName() { return "FourWheel-B"; }
        @Override public String preferredSlotPrefix() { return "B"; }
    }

    static class ParkingSlot {
        private final String id;
        private Vehicle occupant;
        ParkingSlot(String id) { this.id = id; }
        public String getId() { return id; }
        public boolean isOccupied() { return occupant != null; }
        public void occupy(Vehicle v) { this.occupant = v; }
        public void vacate() { this.occupant = null; }
        public Vehicle getOccupant() { return occupant; }
    }

    static class LogEntry {
        private final String plate;
        private final String vehicleType;
        private final String slotId;
        private final LocalDateTime entryTime;
        private LocalDateTime exitTime;
        private double paidAmount;
        LogEntry(String plate, String vehicleType, String slotId, LocalDateTime entryTime) {
            this.plate = plate; this.vehicleType = vehicleType; this.slotId = slotId; this.entryTime = entryTime;
        }
        public void setExit(LocalDateTime t) { this.exitTime = t; }
        public void setPaid(double p) { this.paidAmount = p; }
        public double getPaid() { return paidAmount; }
        public String getPlate() { return plate; }
        public String getVehicleTypeShort() { return vehicleType; }
        public String getSlotId() { return slotId; }
        public LocalDateTime getEntryTime() { return entryTime; }
        public LocalDateTime getExitTime() { return exitTime; }
        public long getDurationMinutes() {
            LocalDateTime end = exitTime == null ? LocalDateTime.now() : exitTime;
            return Duration.between(entryTime, end).toMinutes();
        }
        @Override public String toString() {
            String exit = exitTime == null ? "ACTIVE" : exitTime.format(TF);
            String paid = paidAmount > 0 ? " | Paid: ₱" + String.format("%.2f", paidAmount) : "";
            return String.format("%s | %s | %s -> %s | %d mins%s", plate, vehicleType, entryTime.format(TF), exit, getDurationMinutes(), paid);
        }
    }

    static class Result { final boolean success; final String message; Result(boolean s, String m){ success=s; message=m; } }

    static class Receipt {
        final String plate;
        final String vehicleType;
        final String slotId;
        final LocalDateTime entry;
        final LocalDateTime exit;
        final int hoursBilled;
        final double amount;
        Receipt(String plate, String vehicleType, String slotId, LocalDateTime entry, LocalDateTime exit, int hoursBilled, double amount) {
            this.plate=plate; this.vehicleType=vehicleType; this.slotId=slotId; this.entry=entry; this.exit=exit; this.hoursBilled=hoursBilled; this.amount=amount;
        }
    }

    static class UnparkResult { final boolean success; final String message; final Receipt receipt; UnparkResult(boolean s, String m, Receipt r){ success=s; message=m; receipt=r; } }

    static class ParkingLot {
        private final Map<String, ParkingSlot> slots = new LinkedHashMap<>();
        private final Map<String, LogEntry> activeByPlate = new LinkedHashMap<>();
        private final List<LogEntry> history = new ArrayList<>();
        private double revenue = 0.0;
        ParkingLot() {
            for (int i = 1; i <= 60; i++) slots.put(String.format("M%02d", i), new ParkingSlot(String.format("M%02d", i)));
            for (int i = 1; i <= 30; i++) slots.put(String.format("A%02d", i), new ParkingSlot(String.format("A%02d", i)));
            for (int i = 1; i <= 30; i++) slots.put(String.format("B%02d", i), new ParkingSlot(String.format("B%02d", i)));
        }
        public synchronized Result park(Vehicle v, LocalDateTime entryTime) {
            if (activeByPlate.containsKey(v.getPlate())) return new Result(false, "Vehicle already parked (active).");
            Optional<ParkingSlot> opt = findAvailableSlot(v);
            if (opt.isEmpty()) return new Result(false, "No available slot for type: " + v.getTypeName());
            ParkingSlot s = opt.get();
            s.occupy(v);
            LogEntry le = new LogEntry(v.getPlate(), v.getTypeName(), s.getId(), entryTime);
            activeByPlate.put(v.getPlate(), le);
            history.add(le);
            return new Result(true, "Parked " + v.getPlate() + " at " + s.getId() + " (" + v.getTypeName() + ") at " + entryTime.format(TF));
        }
        private Optional<ParkingSlot> findAvailableSlot(Vehicle v) {
            String pref = v.preferredSlotPrefix();
            if (pref.equals("M")) {
                return slots.values().stream().filter(s -> s.getId().startsWith("M") && !s.isOccupied()).findFirst();
            }
            if (pref.equals("A")) {
                Optional<ParkingSlot> a = slots.values().stream().filter(s -> s.getId().startsWith("A") && !s.isOccupied()).findFirst();
                if (a.isPresent()) return a;
                return slots.values().stream().filter(s -> s.getId().startsWith("B") && !s.isOccupied()).findFirst();
            } else {
                Optional<ParkingSlot> b = slots.values().stream().filter(s -> s.getId().startsWith("B") && !s.isOccupied()).findFirst();
                if (b.isPresent()) return b;
                return slots.values().stream().filter(s -> s.getId().startsWith("A") && !s.isOccupied()).findFirst();
            }
        }
        public synchronized UnparkResult unparkWithPayment(String plate, LocalDateTime exitTime) {
            LogEntry active = activeByPlate.get(plate);
            if (active == null) return new UnparkResult(false, "No active parked vehicle with plate " + plate, null);
            ParkingSlot slot = slots.get(active.getSlotId());
            if (slot != null) slot.vacate();
            active.setExit(exitTime);
            long minutes = active.getDurationMinutes();
            int billedHours = (int) Math.ceil(minutes / 60.0);
            double amount = computeFee(active.getVehicleTypeShort(), billedHours);
            active.setPaid(amount);
            revenue += amount;
            activeByPlate.remove(plate);
            return new UnparkResult(true, String.format("Exited %s from %s | Duration: %d minutes | Amount: ₱%.2f", plate, active.getSlotId(), minutes, amount),
                    new Receipt(plate, active.getVehicleTypeShort(), active.getSlotId(), active.getEntryTime(), active.getExitTime(), billedHours, amount));
        }
        private double computeFee(String vehicleType, int hours) {
            if (vehicleType.startsWith("Motorcycle")) {
                int baseH = 3;
                double base = 20.0;
                double extraRate = 5.0;
                if (hours <= baseH) return base;
                return base + (hours - baseH) * extraRate;
            } else {
                int baseH = 3;
                double base = 40.0;
                double extraRate = 10.0;
                if (hours <= baseH) return base;
                return base + (hours - baseH) * extraRate;
            }
        }
        public Map<String, Integer> vacancySummary() {
            int mVac = (int) slots.values().stream().filter(s -> s.getId().startsWith("M") && !s.isOccupied()).count();
            int aVac = (int) slots.values().stream().filter(s -> s.getId().startsWith("A") && !s.isOccupied()).count();
            int bVac = (int) slots.values().stream().filter(s -> s.getId().startsWith("B") && !s.isOccupied()).count();
            Map<String,Integer> m = new LinkedHashMap<>(); m.put("M", mVac); m.put("A", aVac); m.put("B", bVac); return m;
        }
        public double occupancyRate() {
            long total = slots.size();
            long occ = slots.values().stream().filter(ParkingSlot::isOccupied).count();
            return total == 0 ? 0.0 : (occ * 100.0 / total);
        }
        public int totalSlots() { return slots.size(); }
        public int getActiveCount() { return activeByPlate.size(); }
        public List<LogEntry> getRecentHistory(int limit) {
            int size = history.size(); return history.stream().skip(Math.max(0, size - limit)).collect(java.util.stream.Collectors.toList());
        }
        public List<LogEntry> activeList() { return new ArrayList<>(activeByPlate.values()); }
        public Map<Integer, Long> peakHours() {
            Map<Integer, Long> counts = new TreeMap<>(); for (int h=0; h<24; h++) counts.put(h,0L);
            for (LogEntry e : history) { int hour = e.getEntryTime().getHour(); counts.put(hour, counts.get(hour)+1); } return counts;
        }
        public String exportHistoryToCSV(String filepath) {
            DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            try (BufferedWriter w = new BufferedWriter(new FileWriter(filepath))) {
                w.write("plate,vehicle_type,slot_id,entry_time,exit_time,duration_minutes,amount_paid\n");
                for (LogEntry le : history) {
                    String exit = le.getExitTime() == null ? "" : le.getExitTime().format(f);
                    String paid = String.format("%.2f", le.getPaid());
                    w.write(String.join(",", le.getPlate(), le.getVehicleTypeShort(), le.getSlotId(), le.getEntryTime().format(f), exit, String.valueOf(le.getDurationMinutes()), paid));
                    w.write("\n");
                }
            } catch (Exception ex) { return "Failed to export: " + ex.getMessage(); }
            return "Exported history to: " + filepath;
        }
        public int countUsedByPrefix(String prefix) { return (int) slots.values().stream().filter(s->s.getId().startsWith(prefix)&&s.isOccupied()).count(); }
        public int totalMoto() { return (int) slots.values().stream().filter(s->s.getId().startsWith("M")).count(); }
        public int totalA() { return (int) slots.values().stream().filter(s->s.getId().startsWith("A")).count(); }
        public int totalB() { return (int) slots.values().stream().filter(s->s.getId().startsWith("B")).count(); }
        public boolean isOccupied(String id) { ParkingSlot p = slots.get(id); return p != null && p.isOccupied(); }
        public List<String> slotIdsByPrefix(String prefix) {
            List<String> res = new ArrayList<>();
            for (String k : slots.keySet()) if (k.startsWith(prefix)) res.add(k);
            return res;
        }
        public int totalRevenue() { return (int)Math.round(revenue); }
    }
}
