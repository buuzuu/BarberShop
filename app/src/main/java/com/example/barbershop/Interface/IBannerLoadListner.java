package com.example.barbershop.Interface;

import com.example.barbershop.Model.Banner;

import java.util.List;

public interface IBannerLoadListner {

    void onBannerLoadSuccess(List<Banner> banners);
    void onBannerLoadFailed(String message);

}
