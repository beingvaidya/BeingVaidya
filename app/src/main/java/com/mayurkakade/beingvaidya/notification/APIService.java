package com.mayurkakade.beingvaidya.notification;


import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
            "Content-Type:application/json",
            "Authorization:key=AAAAh8u7D1k:APA91bHtHTSG6S8vFByUFojHnMsazk0BZNfUS1Pu8FAZzXYGRmbAxzGC9I-f5UXiuKgTbit-VihDBjyJtc14eyspOL0XCyrKFvrA7lUd0VxPza69SN4GqE06g6KQagQX3d7ZKylh4EuZ"
    }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}
