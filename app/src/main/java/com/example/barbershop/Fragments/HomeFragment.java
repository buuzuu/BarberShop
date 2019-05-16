package com.example.barbershop.Fragments;


import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.barbershop.Adapter.HomeSliderAdapter;
import com.example.barbershop.Adapter.LookBookAdapter;
import com.example.barbershop.BookingActivity;
import com.example.barbershop.Common.Common;
import com.example.barbershop.Interface.IBannerLoadListner;
import com.example.barbershop.Interface.IBookingInfoLoadListner;
import com.example.barbershop.Interface.ILookBookLoadListner;
import com.example.barbershop.Model.Banner;
import com.example.barbershop.Model.BookingInformation;
import com.example.barbershop.R;
import com.example.barbershop.Service.PicassoImageLoadingService;
import com.facebook.accountkit.AccountKit;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
import ss.com.bannerslider.Slider;

/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment implements IBannerLoadListner, ILookBookLoadListner, IBookingInfoLoadListner {

    private Unbinder unbinder;
    private static final String TAG = "HomeFragment";
    @BindView(R.id.layout_user_information)
    LinearLayout layout_user_information;
    AlertDialog dialog;
    @BindView(R.id.txt_user_name)
    TextView txt_user_name;

    @BindView(R.id.banner_slider)
    Slider banner_slider;
    @BindView(R.id.recycler_look_book)
    RecyclerView recycler_look_book;

    @BindView(R.id.card_booking_info)
    CardView card_booking_info;
    @BindView(R.id.txt_salon_address)
    TextView txt_salon_address;
    @BindView(R.id.txt_salon_barber)
    TextView txt_salon_barber;
    @BindView(R.id.txt_time)
    TextView txt_time;
    @BindView(R.id.txt_time_remain)
    TextView txt_time_remain;


    @OnClick(R.id.btn_delete_booking)
    void deteleBooking() {
        deleteBookingFromBarber();
    }

    private void deleteBookingFromBarber() {


         /*
         to delete booking we first need to delete from Barber collection.
         After that we will delete from user booking collections
                   */
        if (Common.currentBooking != null) {
            dialog.show();

            //get booking information in barber object
            DocumentReference barberBookingInfo = FirebaseFirestore.getInstance().collection("AllSalon")
                    .document(Common.currentBooking.getCityBook()).collection("Branch")
                    .document(Common.currentBooking.getSalonId())
                    .collection("Barbers")
                    .document(Common.currentBooking.getBarberId())
                    .collection(Common.convertTimeStampToStringKey(Common.currentBooking.getTimestamp()))
                    .document(Common.currentBooking.getSlot().toString());
            barberBookingInfo.delete().addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    deleteBookingFromUser();
                }
            });

        } else {
            Toast.makeText(getActivity(), "No booking done", Toast.LENGTH_SHORT).show();
        }


    }

    private void deleteBookingFromUser() {
        // first we need to get information from user object
        if (Common.currentBookingId.length() > 5) {
            DocumentReference userBookingInfo = FirebaseFirestore.getInstance()
                    .collection("User")
                    .document(Common.currentUser.getPhoneNumber()).collection("Booking")
                    .document(Common.currentBookingId);
            userBookingInfo.delete().addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    //After delete from user ,delete from calender

                    //    Paper.init(getActivity());
                    //    String data = Paper.book().read(Common.EVENT_URI_CACHE);
                        /*
                        Write code here to remove from calender
                         */
                    //refresh
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            loadUserBooking();
                            if (dialog.isShowing()) {
                                dialog.dismiss();
                            }
                            Toast.makeText(getActivity(), "Deleted", Toast.LENGTH_SHORT).show();
                        }
                    });


                }
            });

        } else {
            Toast.makeText(getActivity(), "Booking Information ID is Empty", Toast.LENGTH_SHORT).show();
        }

    }


    @OnClick(R.id.card_view_booking)
    void booking() {
        startActivity(new Intent(getActivity(), BookingActivity.class));
    }

    CollectionReference bannerRef, lookbookRef;

    IBannerLoadListner iBannerLoadListner;
    ILookBookLoadListner iLookBookLoadListner;
    IBookingInfoLoadListner iBookingInfoLoadListner;

    public HomeFragment() {
        bannerRef = FirebaseFirestore.getInstance().collection("Banner");
        lookbookRef = FirebaseFirestore.getInstance().collection("Lookbook");
    }


    @Override
    public void onResume() {
        super.onResume();
        loadUserBooking();
    }

    private void loadUserBooking() {

        CollectionReference userBooking = FirebaseFirestore.getInstance()
                .collection("User").document(Common.currentUser.getPhoneNumber()).collection("Booking");
        // get current date
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 0);
        calendar.add(Calendar.HOUR_OF_DAY, 0);
        calendar.add(Calendar.MINUTE, 0);

        Timestamp todayTimeStamp = new Timestamp(calendar.getTime());
        // select booking informaton from firebse with done==false and time stamp greater than today

        userBooking
                //.whereGreaterThanOrEqualTo("timestamp", todayTimeStamp)
                .whereEqualTo("done", false)
                .limit(1)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                if (task.isSuccessful()) {

                    if (!task.getResult().isEmpty()) {

                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {

                            BookingInformation bookingInformation = queryDocumentSnapshot.toObject(BookingInformation.class);
                            Log.d(TAG, "onComplete: " + queryDocumentSnapshot.getId());
                            Common.currentBookingId = queryDocumentSnapshot.getId();
                            iBookingInfoLoadListner.onBookingInfoLoadSuccess(bookingInformation, queryDocumentSnapshot.getId());
                            break;//exit loop
                        }

                    } else {
                        iBookingInfoLoadListner.onBookingInfoLoadEmpty();
                    }
                } else {
                    Toast.makeText(getActivity(), "Task was not successfull", Toast.LENGTH_SHORT).show();
                }


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                iBookingInfoLoadListner.onBookingInfoLoadFailed(e.getMessage());

            }
        });


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_home, container, false);
        unbinder = ButterKnife.bind(this, view);

        Slider.init(new PicassoImageLoadingService());
        dialog = new SpotsDialog.Builder().setCancelable(false).setContext(getContext()).build();
        iBannerLoadListner = this;
        iLookBookLoadListner = this;
        iBookingInfoLoadListner = this;


        //check if logged

        if (AccountKit.getCurrentAccessToken() != null) {
            setUserInformation();
            loadBanner();
            loadLookBook();
            loadUserBooking();
        }
        return view;
    }

    private void loadLookBook() {
        lookbookRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                List<Banner> lookbooks = new ArrayList<>();
                if (task.isSuccessful()) {

                    for (QueryDocumentSnapshot bannerSnapShot : task.getResult()) {

                        Banner banner = bannerSnapShot.toObject(Banner.class);
                        lookbooks.add(banner);

                    }
                    iLookBookLoadListner.onLookBookLoadSuccess(lookbooks);

                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                iLookBookLoadListner.onLookBookLoadFailed(e.getMessage());
            }
        });
    }

    private void loadBanner() {

        bannerRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {

                List<Banner> banners = new ArrayList<>();
                if (task.isSuccessful()) {

                    for (QueryDocumentSnapshot bannerSnapShot : task.getResult()) {

                        Banner banner = bannerSnapShot.toObject(Banner.class);
                        banners.add(banner);

                    }

                    iBannerLoadListner.onBannerLoadSuccess(banners);

                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                iBannerLoadListner.onBannerLoadFailed(e.getMessage());


            }
        });
    }

    private void setUserInformation() {

        layout_user_information.setVisibility(View.VISIBLE);
        txt_user_name.setText(Common.currentUser.getName());
    }


    @Override
    public void onBannerLoadSuccess(List<Banner> banners) {
        banner_slider.setAdapter(new HomeSliderAdapter(banners));
    }

    @Override
    public void onBannerLoadFailed(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onLookBookLoadSuccess(List<Banner> banners) {
        recycler_look_book.setHasFixedSize(true);
        recycler_look_book.setLayoutManager(new LinearLayoutManager(getActivity()));
        recycler_look_book.setAdapter(new LookBookAdapter(banners, getActivity()));
    }

    @Override
    public void onLookBookLoadFailed(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBookingInfoLoadEmpty() {

        card_booking_info.setVisibility(View.GONE);
    }

    @Override
    public void onBookingInfoLoadSuccess(BookingInformation bookingInformation, String bookingId) {

        Common.currentBooking = bookingInformation;
        Common.currentBookingId = bookingId;
        txt_salon_address.setText(bookingInformation.getSalonAddress());
        txt_salon_barber.setText(bookingInformation.getBarberName());
        txt_time.setText(bookingInformation.getTime());
        String dateRemain = DateUtils.getRelativeTimeSpanString(
                Long.valueOf(bookingInformation.getTimestamp().toDate().getTime()),
                Calendar.getInstance().getTimeInMillis(), 0).toString();


        txt_time_remain.setText(dateRemain);
        card_booking_info.setVisibility(View.VISIBLE);


    }

    @Override
    public void onBookingInfoLoadFailed(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }
}
