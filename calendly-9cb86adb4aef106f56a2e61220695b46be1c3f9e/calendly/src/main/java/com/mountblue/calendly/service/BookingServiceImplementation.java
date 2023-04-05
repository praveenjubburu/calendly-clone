package com.mountblue.calendly.service;


import com.mountblue.calendly.Entity.Bookings;
import com.mountblue.calendly.dao.BookingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class BookingServiceImplementation implements BookingService {

    @Autowired
    BookingsRepository bookingRepository;
    @Override
    public List<String> getTimeSlots(Date date , int id) {
        return bookingRepository.getTimeSlots(date,id);
    }

    @Override
    public void saveBooking(Bookings bookings) {
        bookingRepository.save(bookings);
    }

    @Override
    public Optional<Bookings> rescheduleBookings(int id) {
        return bookingRepository.findById(id);
    }
}
