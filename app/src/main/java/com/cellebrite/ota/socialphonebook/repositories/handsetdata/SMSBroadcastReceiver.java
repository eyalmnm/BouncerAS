package com.cellebrite.ota.socialphonebook.repositories.handsetdata;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.em_projects.utils.Utils;

public class SMSBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context arg0, Intent intent) {
//		abortBroadcast();

        //#ifdef DEBUG
        Utils.debug("SMSBroadcastReceiver.onReceive() - SMS has arrived!");
        //#endif

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        DeviceContactsDataRepository.getInstance().onSmsReceived();
    }
}
