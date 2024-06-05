package com.example.myapplication;

import android.content.Intent;

public interface NfcListener {
    void onNfcDetected(Intent intent);
}
