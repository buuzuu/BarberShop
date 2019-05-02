package com.example.barbershop.Common;

import com.example.barbershop.Model.Barber;
import com.example.barbershop.Model.Salon;
import com.example.barbershop.Model.User;

public class Common {

    public static String IS_LOGIN = "IsLogin";
    public static User currentUser;
    public static final String KEY_ENABLE_BUTTON_NEXT = "ENABLE_BUTTON_NEXT";
    public static final String KEY_SALON_STORE = "SALON_SAVE";
    public static Salon currentSalon;
    public static int step = 0;
    public static String city = "";
    public static String KEY_BARBER_LOAD_DONE = "BARBER_LOAD_DONE";
    public static Barber currentBarber;
    public static String KEY_DISPLAY_TIME_OUT = "DISPLAY_TIME_OUT";
    public static String KEY_STEP = "STEP";
    public static String KEY_BARBER_SELECTED = "BARBER_SELECTED";
    public static int TIME_SLOT_TOTAL = 20;

    public static String convertTimeSlotToString(int slot) {

        switch (slot) {

            case 0:
                return "9:00-9:30";
            case 1:
                return "9:30-10:00";
            case 2:
                return "10:00-10:30";
            case 3:
                return "10:30-11:00";
            case 4:
                return "11:00-11:30";
            case 5:
                return "11:30-12:00";
            case 6:
                return "12:00-12:30";
            case 7:
                return "12:30-1:00";
            case 8:
                return "1:00-1:30";
            case 9:
                return "1:30-2:00";
            case 10:
                return "2:00-2:30";
            case 11:
                return "2:30-3:00";
            case 12:
                return "3:00-3:30";
            case 13:
                return "3:30-4:00";
            case 14:
                return "4:00-4:30";
            case 15:
                return "4:30-5:00";
            case 16:
                return "5:00-5:30";
            case 17:
                return "5:30-6:00";
            case 18:
                return "6:00-6:30";
            case 19:
                return "6:30-7:00";

            default:
                return "Closed";

        }

    }
}
