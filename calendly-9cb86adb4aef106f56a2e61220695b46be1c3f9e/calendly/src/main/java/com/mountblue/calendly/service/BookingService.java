package com.mountblue.calendly.service;



import com.mountblue.calendly.Entity.Bookings;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface BookingService {

    public List<String> getTimeSlots(Date date, int id);

    public void saveBooking(Bookings bookings);


    public Optional<Bookings> rescheduleBookings(int id);

}
