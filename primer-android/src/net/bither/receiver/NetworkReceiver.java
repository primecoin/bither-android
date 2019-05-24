/*
 * Copyright 2014 http://Bither.net
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.bither.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import net.bither.ChooseModeActivity;
import net.bither.PrimerApplication;
import net.bither.util.LogUtil;
import net.bither.util.NetworkUtil;

import net.bither.PrimerApplication;
import net.bither.ChooseModeActivity;
import net.bither.bitherj.PrimerjSettings;
import net.bither.preference.AppSharedPreference;
import net.bither.util.LogUtil;
import net.bither.util.NetworkUtil;

public class NetworkReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) {
            return;
        }
        String action = intent.getAction();
        LogUtil.d("receiver", action);
        if (AppSharedPreference.getInstance().getAppMode() == PrimerjSettings.AppMode.COLD) {
            if (NetworkUtil.isConnected() || NetworkUtil.BluetoothIsConnected()) {
                Intent reIntent = new Intent(context, ChooseModeActivity.class);
                PendingIntent restartIntent = PendingIntent.getActivity(context, 0, reIntent,PendingIntent.FLAG_CANCEL_CURRENT);
                AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, restartIntent);
                android.os.Process.killProcess(android.os.Process.myPid());
            }

           /* if (NetworkUtil.isConnected() || NetworkUtil.BluetoothIsConnected()) {
                NotificationManager nm = (NotificationManager) context
                        .getSystemService(Context.NOTIFICATION_SERVICE);
                Intent intent2 = new Intent(context, ChooseModeActivity.class);

                String title = context.getString(R.string.cold_warning);
                String contentText = context
                        .getString(R.string.safe_your_wallet);
                SystemUtil.nmNotifyDefault(nm, context,
                        PrimerSetting.NOTIFICATION_ID_NETWORK_ALERT, intent2,
                        title, contentText, R.drawable.ic_launcher);
            }*/
        } else {
            if (NetworkUtil.isConnected()) {
                PrimerApplication.startBlockchainService();
            }
        }

    }
}
