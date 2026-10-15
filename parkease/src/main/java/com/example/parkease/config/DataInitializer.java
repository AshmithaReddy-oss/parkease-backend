package com.example.parkease.config;

import com.example.parkease.entity.Booking;
import com.example.parkease.entity.ParkingLot;
import com.example.parkease.entity.Payment;
import com.example.parkease.entity.Slot;
import com.example.parkease.entity.User;
import com.example.parkease.enums.BookingStatus;
import com.example.parkease.enums.LotStatus;
import com.example.parkease.enums.PaymentStatus;
import com.example.parkease.enums.Role;
import com.example.parkease.enums.SlotStatus;
import com.example.parkease.repository.BookingRepository;
import com.example.parkease.repository.ParkingLotRepository;
import com.example.parkease.repository.PaymentRepository;
import com.example.parkease.repository.SlotRepository;
import com.example.parkease.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class DataInitializer {

    @Value("${app.admin.email:admin@parkease.com}")
    private String adminEmail;

    @Value("${app.admin.password:ChangeMe@Local1}")
    private String adminPassword;

    @Bean
    public CommandLineRunner initializeSeedData(
            UserRepository userRepository,
            ParkingLotRepository parkingLotRepository,
            SlotRepository slotRepository,
            BookingRepository bookingRepository,
            PaymentRepository paymentRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {
            // 1. Initialize Admin
            User admin = null;
            if (!userRepository.existsByEmail(adminEmail)) {
                admin = new User();
                admin.setName("ParkEase Administrator");
                admin.setEmail(adminEmail);
                admin.setPassword(passwordEncoder.encode(adminPassword));
                admin.setRole(Role.ADMIN);
                admin.setEnabled(true);
                admin = userRepository.save(admin);
                System.out.println("Initialized ParkEase Administrator (" + adminEmail + ")");
            } else {
                admin = userRepository.findByEmail(adminEmail).orElse(null);
            }

            // 2. Initialize Demo Owner
            String ownerEmail = "owner@parkease.com";
            User owner = null;
            if (!userRepository.existsByEmail(ownerEmail)) {
                owner = new User();
                owner.setName("Demo Parking Owner");
                owner.setEmail(ownerEmail);
                owner.setPassword(passwordEncoder.encode("Owner12345!"));
                owner.setRole(Role.OWNER);
                owner.setPhone("+91 98765 43210");
                owner.setEnabled(true);
                owner = userRepository.save(owner);
                System.out.println("Initialized Demo Owner (" + ownerEmail + ")");
            } else {
                owner = userRepository.findByEmail(ownerEmail).orElse(null);
            }

            // 3. Initialize Demo User
            String userEmail = "user@parkease.com";
            User regularUser = null;
            if (!userRepository.existsByEmail(userEmail)) {
                regularUser = new User();
                regularUser.setName("Alex Morgan");
                regularUser.setEmail(userEmail);
                regularUser.setPassword(passwordEncoder.encode("User12345!"));
                regularUser.setRole(Role.USER);
                regularUser.setPhone("+91 91234 56789");
                regularUser.setEnabled(true);
                regularUser = userRepository.save(regularUser);
                System.out.println("Initialized Demo User (" + userEmail + ")");
            } else {
                regularUser = userRepository.findByEmail(userEmail).orElse(null);
            }

            // 4. Initialize Parking Facilities if empty
            if (parkingLotRepository.count() == 0 && owner != null) {
                System.out.println("Seeding initial parking facilities and slots...");

                // Lot 1
                ParkingLot lot1 = new ParkingLot();
                lot1.setName("City Center Grand Galleria");
                lot1.setLocation("Downtown Financial District, 42 MG Road");
                lot1.setDescription("Premium indoor multi-level parking facility with 24/7 security surveillance and EV charging ports.");
                lot1.setTotalSlots(12);
                lot1.setPricePerHour(50.0);
                lot1.setStatus(LotStatus.APPROVED);
                lot1.setOwner(owner);
                lot1 = parkingLotRepository.save(lot1);

                List<Slot> lot1Slots = new ArrayList<>();
                for (int i = 1; i <= 12; i++) {
                    Slot slot = new Slot();
                    slot.setParkingLot(lot1);
                    slot.setSlotNumber("A" + i);
                    slot.setAvailable(true);
                    slot.setStatus(SlotStatus.AVAILABLE);
                    lot1Slots.add(slot);
                }
                lot1Slots = slotRepository.saveAll(lot1Slots);

                // Lot 2
                ParkingLot lot2 = new ParkingLot();
                lot2.setName("Metro Station Transit Deck");
                lot2.setLocation("Central Metro Hub, South Exit Junction");
                lot2.setDescription("Fast automated entry with direct covered pedestrian walkway to Metro platform. Perfect for daily commuters.");
                lot2.setTotalSlots(16);
                lot2.setPricePerHour(30.0);
                lot2.setStatus(LotStatus.APPROVED);
                lot2.setOwner(owner);
                lot2 = parkingLotRepository.save(lot2);

                List<Slot> lot2Slots = new ArrayList<>();
                for (int i = 1; i <= 16; i++) {
                    Slot slot = new Slot();
                    slot.setParkingLot(lot2);
                    slot.setSlotNumber("M" + i);
                    slot.setAvailable(true);
                    slot.setStatus(SlotStatus.AVAILABLE);
                    lot2Slots.add(slot);
                }
                slotRepository.saveAll(lot2Slots);

                // Lot 3
                ParkingLot lot3 = new ParkingLot();
                lot3.setName("Silicon Valley CyberTower Lot");
                lot3.setLocation("IT Tech Corridor Phase 1, Ring Road");
                lot3.setDescription("Spacious covered parking with CCTV, automated license plate recognition, and instant reservation.");
                lot3.setTotalSlots(20);
                lot3.setPricePerHour(40.0);
                lot3.setStatus(LotStatus.APPROVED);
                lot3.setOwner(owner);
                lot3 = parkingLotRepository.save(lot3);

                List<Slot> lot3Slots = new ArrayList<>();
                for (int i = 1; i <= 20; i++) {
                    Slot slot = new Slot();
                    slot.setParkingLot(lot3);
                    slot.setSlotNumber("C" + i);
                    slot.setAvailable(true);
                    slot.setStatus(SlotStatus.AVAILABLE);
                    lot3Slots.add(slot);
                }
                slotRepository.saveAll(lot3Slots);

                // Lot 4
                ParkingLot lot4 = new ParkingLot();
                lot4.setName("Airport Express Terminal Deck");
                lot4.setLocation("Airport Expressway, Terminal 2 Arrival Zone");
                lot4.setDescription("Long-term and short-term airport terminal parking with free luggage shuttle service.");
                lot4.setTotalSlots(15);
                lot4.setPricePerHour(75.0);
                lot4.setStatus(LotStatus.APPROVED);
                lot4.setOwner(owner);
                lot4 = parkingLotRepository.save(lot4);

                List<Slot> lot4Slots = new ArrayList<>();
                for (int i = 1; i <= 15; i++) {
                    Slot slot = new Slot();
                    slot.setParkingLot(lot4);
                    slot.setSlotNumber("T" + i);
                    slot.setAvailable(true);
                    slot.setStatus(SlotStatus.AVAILABLE);
                    lot4Slots.add(slot);
                }
                slotRepository.saveAll(lot4Slots);

                // 5. Seed sample bookings for the demo user
                if (regularUser != null && !lot1Slots.isEmpty()) {
                    Slot bookedSlot = lot1Slots.get(0);
                    bookedSlot.setAvailable(false);
                    bookedSlot.setStatus(SlotStatus.BOOKED);
                    slotRepository.save(bookedSlot);

                    Booking booking1 = new Booking();
                    booking1.setUser(regularUser);
                    booking1.setSlot(bookedSlot);
                    booking1.setStartTime(LocalDateTime.now().minusHours(1));
                    booking1.setEndTime(LocalDateTime.now().plusHours(3));
                    booking1.setTotalAmount(BigDecimal.valueOf(200.00));
                    booking1.setStatus(BookingStatus.CONFIRMED);
                    booking1 = bookingRepository.save(booking1);

                    Payment payment1 = new Payment();
                    payment1.setBooking(booking1);
                    payment1.setAmount(BigDecimal.valueOf(200.00));
                    payment1.setPaymentMethod("UPI");
                    payment1.setPaymentStatus(PaymentStatus.SUCCESS);
                    payment1.setTransactionReference("TXN-" + System.currentTimeMillis());
                    paymentRepository.save(payment1);

                    // A second completed booking
                    Slot completedSlot = lot1Slots.get(1);
                    Booking booking2 = new Booking();
                    booking2.setUser(regularUser);
                    booking2.setSlot(completedSlot);
                    booking2.setStartTime(LocalDateTime.now().minusDays(2).withHour(10).withMinute(0));
                    booking2.setEndTime(LocalDateTime.now().minusDays(2).withHour(14).withMinute(0));
                    booking2.setTotalAmount(BigDecimal.valueOf(200.00));
                    booking2.setStatus(BookingStatus.COMPLETED);
                    booking2 = bookingRepository.save(booking2);

                    Payment payment2 = new Payment();
                    payment2.setBooking(booking2);
                    payment2.setAmount(BigDecimal.valueOf(200.00));
                    payment2.setPaymentMethod("CREDIT_CARD");
                    payment2.setPaymentStatus(PaymentStatus.SUCCESS);
                    payment2.setTransactionReference("TXN-" + (System.currentTimeMillis() - 86400000));
                    paymentRepository.save(payment2);

                    System.out.println("Seeded demo bookings and payments successfully.");
                }
            }
        };
    }
}