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

package net.bither;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;

import net.bither.R;
import net.bither.bitherj.AbstractApp;
import net.bither.bitherj.PrimerjSettings;
import net.bither.bitherj.ISetting;
import net.bither.bitherj.NotificationService;
import net.bither.bitherj.api.TrustCert;
import net.bither.bitherj.qrcode.QRCodeUtil;
import net.bither.preference.AppSharedPreference;
import net.bither.preference.PersistentCookieStore;

import org.apache.http.client.CookieStore;

import java.io.File;
import java.util.List;

public class AndroidImplAbstractApp extends AbstractApp {

    @Override
    public TrustCert initTrustCert() {
        return new TrustCert(PrimerApplication.mContext.getResources().openRawResource(R.raw
                .bithertruststore), "bither".toCharArray(), "BKS");
    }

    @Override
    public ISetting initSetting() {
        return new ISetting() {
            @Override
            public PrimerjSettings.AppMode getAppMode() {
                return AppSharedPreference.getInstance().getAppMode();
            }

            @Override
            public boolean getBitherjDoneSyncFromSpv() {
                return AppSharedPreference.getInstance().getBitherjDoneSyncFromSpv();
            }

            @Override
            public void setBitherjDoneSyncFromSpv(boolean isDone) {
                AppSharedPreference.getInstance().setBitherjDoneSyncFromSpv(isDone);
            }

            @Override
            public PrimerjSettings.TransactionFeeMode getTransactionFeeMode() {
                return AppSharedPreference.getInstance().getTransactionFeeMode();
            }

            @Override
            public boolean isTestNet() {
                return (AppSharedPreference.getInstance().getNetType() == PrimerjSettings.NetType.TESTNET);
            }

            @Override
            public PrimerjSettings.TransactionFeePrecision getTransactionFeePrecision() {
                return AppSharedPreference.getInstance().getTransactionFeePrecision();
            }

            @Override
            public PrimerjSettings.ApiConfig getApiConfig() {
                return AppSharedPreference.getInstance().getApiConfig();
            }

            @Override
            public File getPrivateDir(String dirName) {
                File file = PrimerApplication.mContext.getDir(dirName, Context.MODE_PRIVATE);
                if (!file.exists()) {
                    file.mkdirs();
                }
                return file;
            }

            @Override
            public boolean isApplicationRunInForeground() {
                if (PrimerApplication.mContext == null) {
                    return false;
                }
                ActivityManager am = (ActivityManager) PrimerApplication.mContext
                        .getSystemService(Context.ACTIVITY_SERVICE);
                List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
                if (tasks != null && !tasks.isEmpty()) {
                    ComponentName topActivity = tasks.get(0).topActivity;
                    if (!topActivity.getPackageName().equals(PrimerApplication.mContext.getPackageName())) {
                        return false;
                    }
                    if (topActivity.getClassName().equals(AdActivity.class.getName())) {
                        return false;
                    }
                }
                return true;
            }

            @Override
            public QRCodeUtil.QRQuality getQRQuality() {
                return AppSharedPreference.getInstance().getQRQuality();
            }

            @Override
            public boolean getDownloadSpvFinish() {
                return AppSharedPreference.getInstance().getDownloadSpvFinish();
            }

            @Override
            public void setDownloadSpvFinish(boolean finish) {
                AppSharedPreference.getInstance().setDownloadSpvFinish(finish);
            }

            @Override
            public CookieStore getCookieStore() {
                return PersistentCookieStore.getInstance();
            }


            
        };
    }

    @Override
    public NotificationService initNotification() {
        return new NotificationAndroidImpl();
    }
}
