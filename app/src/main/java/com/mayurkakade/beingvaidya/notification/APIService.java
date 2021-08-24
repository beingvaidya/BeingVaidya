package com.mayurkakade.beingvaidya.notification;


import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
            "Content-Type:application/json",
            "Authorization:key=AAAAiZLgOOk:APA91bFyV-A2JVis2Sbt40p-S_qmX0xRkhOu0t3Iv4YtJ6IYron2KI3eJjyqXElyBBp0vfAAnqKyip_fvZX01F4ELgu3ebz-1NeF5_2ebgclObbv6NOXuLf9UlUDMnZlOuS6F4_r3RHf"
    }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
