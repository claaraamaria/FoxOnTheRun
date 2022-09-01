package com.example.foxontherun.server;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RESTClient {
    private static final String BASE_URL = "http://192.168.1.184:9090/";
    private static RESTClient mInstance;
    private Retrofit retrofit;

    private RESTClient(){
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static synchronized RESTClient getInstance(){
        if(mInstance == null){
            mInstance= new RESTClient();
        }
        return mInstance;
    }

    public synchronized GameService getApi(){
        return retrofit.create(GameService.class);
    }
}
