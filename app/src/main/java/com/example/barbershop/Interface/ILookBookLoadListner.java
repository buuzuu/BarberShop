package com.example.barbershop.Interface;

import com.example.barbershop.Model.Banner;

import java.util.List;

public interface ILookBookLoadListner {

    void onLookBookLoadSuccess(List<Banner> banners);
    void onLookBookLoadFailed(String message);

}
