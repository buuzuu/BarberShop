package com.example.barbershop.Interface;

import com.example.barbershop.Model.Salon;

import java.util.List;

public interface IBranchLoadListner {

    void onBranchLoadSuccess(List<Salon> salonList);
    void onBranchFailed(String message);
}
