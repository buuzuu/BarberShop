package com.example.barbershop.Interface;

import com.example.barbershop.Model.BookingInformation;

public interface IBookingInfoLoadListner {

    void onBookingInfoLoadEmpty();
    void onBookingInfoLoadSuccess(BookingInformation bookingInformation,String documentId);
    void onBookingInfoLoadFailed(String msg);


}
