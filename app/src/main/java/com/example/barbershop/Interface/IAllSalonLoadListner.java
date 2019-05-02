package com.example.barbershop.Interface;

import java.util.List;

public interface IAllSalonLoadListner {

    void onAllSalonLoadSuccess(List<String> areaNameList);
    void onAllSalonLoadFailed(String message);
}
