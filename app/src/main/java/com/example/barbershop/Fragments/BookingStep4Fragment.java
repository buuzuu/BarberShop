package com.example.barbershop.Fragments;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.barbershop.Common.Common;
import com.example.barbershop.Model.BookingInformation;
import com.example.barbershop.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;

public class BookingStep4Fragment extends Fragment {

    AlertDialog dialog;

    static BookingStep4Fragment instance;
    SimpleDateFormat simpleDateFormat;
    LocalBroadcastManager localBroadcastManager;
    @BindView(R.id.txt_booking_barber_text)
    TextView txt_booking_barber_text;
    @BindView(R.id.txt_booking_time_text)
    TextView txt_booking_time_text;
    @BindView(R.id.txt_salon_address)
    TextView txt_salon_address;
    @BindView(R.id.txt_salon_name)
    TextView txt_salon_name;
    @BindView(R.id.txt_salon_open_hours)
    TextView txt_salon_open_hours;
    @BindView(R.id.txt_salon_phone)
    TextView txt_salon_phone;
    @BindView(R.id.txt_salon_website)
    TextView txt_salon_website;
    Unbinder unbinder;

    @OnClick(R.id.btn_confirm)
    void confirmBooking() {

        dialog.show();

        //Process timestamp
        //we will use timestamp to filter all booking with date greater than today  to display all future booking
        String startTime = Common.convertTimeSlotToString(Common.currentTimeSlot);
        String[] convertTime = startTime.split("-");
        String[] startTimeConvert = convertTime[0].split(":");
        int startHourInt = Integer.parseInt(startTimeConvert[0].trim());
        int startMinInt = Integer.parseInt(startTimeConvert[1].trim());
        Calendar bookingDateWithourHouse = Calendar.getInstance();
        bookingDateWithourHouse.setTimeInMillis(Common.bookingDate.getTimeInMillis());
        bookingDateWithourHouse.set(Calendar.HOUR_OF_DAY, startHourInt);
        bookingDateWithourHouse.set(Calendar.MINUTE, startMinInt);
        //create timestamp object for booking information
        Timestamp timestamp = new Timestamp(bookingDateWithourHouse.getTime());


        final BookingInformation bookingInformation = new BookingInformation();
        bookingInformation.setDone(false); // always false coz will later use this as filter
        bookingInformation.setCityBook(Common.city);
        bookingInformation.setTimestamp(timestamp);
        bookingInformation.setBarberId(Common.currentBarber.getBarberId());
        bookingInformation.setBarberName(Common.currentBarber.getName());
        bookingInformation.setCustomerName(Common.currentUser.getName());
        bookingInformation.setCustomerPhone(Common.currentUser.getPhoneNumber());
        bookingInformation.setSalonId(Common.currentSalon.getSalonId());
        bookingInformation.setSalonAddress(Common.currentSalon.getAddress());
        bookingInformation.setSalonName(Common.currentSalon.getName());
        bookingInformation.setTime(new StringBuilder(Common.convertTimeSlotToString(Common.currentTimeSlot))
                .append(" at ")
                .append(simpleDateFormat.format(bookingDateWithourHouse.getTime())).toString());
        bookingInformation.setSlot(Long.valueOf(Common.currentTimeSlot));
        DocumentReference bookingDate = FirebaseFirestore.getInstance().collection("AllSalon").document(Common.city)
                .collection("Branch").document(Common.currentSalon.getSalonId()).collection("Barbers")
                .document(Common.currentBarber.getBarberId()).collection(Common.simpleFormatDate.format(Common.bookingDate.getTime()))
                .document(String.valueOf(Common.currentTimeSlot));

        bookingDate.set(bookingInformation).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                addToUserBooking(bookingInformation);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addToUserBooking(final BookingInformation bookingInformation) {


        //First create new collection
        final CollectionReference userBooking = FirebaseFirestore.getInstance()
                .collection("User").document(Common.currentUser.getPhoneNumber()).collection("Booking");

        // check if document exits in this collection
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 0);
        calendar.add(Calendar.HOUR_OF_DAY, 0);
        calendar.add(Calendar.MINUTE, 0);

        Timestamp todayTimeStamp = new Timestamp(calendar.getTime());

        userBooking
                .whereGreaterThanOrEqualTo("timestamp", todayTimeStamp)
                .whereEqualTo("done", false)
                .limit(1)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                if (task.getResult().isEmpty()) {
                    // set data
                    userBooking.document().set(bookingInformation).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            if (dialog.isShowing())
                                dialog.dismiss();

                          //  addToCalender(Common.bookingDate, Common.convertTimeSlotToString(Common.currentTimeSlot));
                            resetStaticData();
                            getActivity().finish();
                            Toast.makeText(getActivity(), "Success", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            if (dialog.isShowing())
                                dialog.dismiss();

                            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });


                } else {

                    if (dialog.isShowing())
                        dialog.dismiss();


                    resetStaticData();
                    getActivity().finish();
                    Toast.makeText(getActivity(), "Success", Toast.LENGTH_SHORT).show();
                }


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });


    }

    private void addToCalender(Calendar bookingDate, String startDate) {

        String startTime = Common.convertTimeSlotToString(Common.currentTimeSlot);
        String[] convertTime = startTime.split("-");
        String[] startTimeConvert = convertTime[0].split(":");
        int startHourInt = Integer.parseInt(startTimeConvert[0].trim());
        int startMinInt = Integer.parseInt(startTimeConvert[1].trim());

        String[] endTimeConvert = convertTime[1].split(":");
        int endHourInt = Integer.parseInt(endTimeConvert[0].trim());
        int endMinInt = Integer.parseInt(endTimeConvert[1].trim());


        Calendar startEvent = Calendar.getInstance();
        startEvent.setTimeInMillis(bookingDate.getTimeInMillis());
        startEvent.set(Calendar.HOUR_OF_DAY, startHourInt);//set event start time
        startEvent.set(Calendar.MINUTE, startMinInt);//set event start min

        Calendar endEvent = Calendar.getInstance();
        endEvent.setTimeInMillis(bookingDate.getTimeInMillis());
        endEvent.set(Calendar.HOUR_OF_DAY, endHourInt);//set event end time
        endEvent.set(Calendar.MINUTE, endMinInt);//set event end min

        // now convert it to format string
        SimpleDateFormat calenderDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        String startEventTime = calenderDateFormat.format(startEvent.getTime());
        String endEventTime = calenderDateFormat.format(endEvent.getTime());

        addToDeviceCalender(startEventTime, endEventTime, "Haircut Booking", new StringBuilder("Haircur from")
                .append(startTime)
                .append(" with ")
                .append(Common.currentBarber.getName())
                .append(" at ")
                .append(Common.currentSalon.getName()).toString(), new StringBuilder("Address: ")
                .append(Common.currentSalon.getAddress()).toString());


    }

    private void addToDeviceCalender(String startEventTime, String endEventTime, String title, String description, String location) {

        SimpleDateFormat calenderDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");


        try {
            Date start = calenderDateFormat.parse(startEventTime);
            Date end = calenderDateFormat.parse(endEventTime);
//
//            ContentValues event = new ContentValues();
//
//            //put
//            event.put(CalendarContract.Events.CALENDAR_ID, getCalender(getContext()));
//            event.put(CalendarContract.Events.TITLE, title);
//            event.put(CalendarContract.Events.DESCRIPTION, description);
//            event.put(CalendarContract.Events.EVENT_LOCATION, location);
//
//            //time
//            event.put(CalendarContract.Events.DTSTART, start.getTime());
//            event.put(CalendarContract.Events.DTEND, end.getTime());
//            event.put(CalendarContract.Events.ALL_DAY, 0);
//            event.put(CalendarContract.Events.HAS_ALARM, 1);
//
//            String timeZone = TimeZone.getDefault().getID();
//            event.put(CalendarContract.Events.EVENT_TIMEZONE, timeZone);
//
//
//            Uri calenders;
//            if (Build.VERSION.SDK_INT >=8)
//                calenders = Uri.parse("content://com.android.calender/events");
//            else
//                calenders = Uri.parse("content://calender/events");
//
//            getActivity().getContentResolver().insert(calenders, event);

            Intent intent = new Intent(Intent.ACTION_INSERT);
            intent.setType("vnd.android.cursor.item/event");

//            Calendar cal = Calendar.getInstance();
//            long startTime = cal.getTimeInMillis();
//            long endTime = cal.getTimeInMillis() + 60 * 60 * 1000;

            intent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, start.getTime());
            intent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, end.getTime());
            intent.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true);

            intent.putExtra(CalendarContract.Events.TITLE, title);
            intent.putExtra(CalendarContract.Events.DESCRIPTION, description);
            intent.putExtra(CalendarContract.Events.EVENT_LOCATION, location);
            intent.putExtra(CalendarContract.Events.RRULE, "FREQ=YEARLY");

            Paper.init(getActivity());
            Paper.book().write(Common.EVENT_URI_CACHE,start.getTime());

            startActivity(intent);


        } catch (ParseException e) {
            e.printStackTrace();
        }


    }

//    private String getCalender(Context context) {
//
//        // get default calender id of calender of gmail
//
//        String gmailIdCalender = "";
//        String projection[] = {"_id", "calender_displayName"};
//        Uri calenders = Uri.parse("content://com.android.calender/calenders");
//
//        Cursor managedCursor = null;
//        ContentResolver contentResolver = context.getContentResolver();
//        // select all calender
//         managedCursor = contentResolver.query(calenders, projection, null, null, null);
//
//        if (managedCursor.moveToFirst()) {
//
//            String calName;
//            int nameCol = managedCursor.getColumnIndex(projection[1]);
//            int idCol = managedCursor.getColumnIndex(projection[0]);
//
//            do {
//                calName = managedCursor.getString(nameCol);
//                if (calName.contains("@gmail.com")) {
//                    gmailIdCalender = managedCursor.getString(idCol);
//                    break;
//                }
//            } while (managedCursor.moveToNext());
//            managedCursor.close();
//        }
//
//
//        return gmailIdCalender;
//    }

    private void resetStaticData() {
        Common.step = 0;
        Common.currentTimeSlot = -1;
        Common.currentSalon = null;
        Common.currentBarber = null;
        Common.bookingDate.add(Calendar.DATE, 0);  //current date added
    }


    public static BookingStep4Fragment getInstance() {
        if (instance == null)
            instance = new BookingStep4Fragment();

        return instance;
    }

    BroadcastReceiver confirmBookingReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setData();
        }
    };

    private void setData() {

        txt_booking_barber_text.setText(Common.currentBarber.getName());
        txt_booking_time_text.setText(new StringBuilder(Common.convertTimeSlotToString(Common.currentTimeSlot))
                .append(" at ")
                .append(simpleDateFormat.format(Common.bookingDate.getTime())));

        txt_salon_address.setText(Common.currentSalon.getAddress());
        txt_salon_website.setText(Common.currentSalon.getWebsite());
        txt_salon_name.setText(Common.currentSalon.getName());
        txt_salon_open_hours.setText(Common.currentSalon.getOpenHours());
        txt_salon_phone.setText(Common.currentSalon.getPhone());


    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
        localBroadcastManager = LocalBroadcastManager.getInstance(getContext());
        localBroadcastManager.registerReceiver(confirmBookingReceiver, new IntentFilter(Common.KEY_CONFIRM_BOOKING));
        dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();
    }

    @Override
    public void onDestroy() {
        localBroadcastManager.unregisterReceiver(confirmBookingReceiver);
        super.onDestroy();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View itemView = inflater.inflate(R.layout.fragment_booking_step_four, container, false);
        unbinder = ButterKnife.bind(this, itemView);
        return itemView;
    }
}
