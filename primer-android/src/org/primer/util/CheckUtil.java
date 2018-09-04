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

package org.primer.util;

import android.content.Intent;
import android.provider.Settings;

import org.primer.PrimerApplication;
import org.primer.R;
import org.primer.primerj.PrimerjSettings;
import org.primer.primerj.core.Address;
import org.primer.primerj.core.HDAccount;
import org.primer.primerj.core.HDMKeychain;
import org.primer.primerj.crypto.ECKey;
import org.primer.primerj.crypto.PasswordSeed;
import org.primer.primerj.crypto.SecureCharSequence;
import org.primer.primerj.utils.PrivateKeyUtil;
import org.primer.primerj.utils.Utils;
import org.primer.model.Check;
import org.primer.model.Check.CheckOperation;
import org.primer.model.Check.ICheckAction;
import org.primer.preference.AppSharedPreference;
import org.primer.runnable.CheckRunnable;
import org.primer.util.NetworkUtil.NetworkType;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CheckUtil {
    private CheckUtil() {

    }

    public static ExecutorService runChecks(List<Check> checks, int threadCount) {
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        for (Check check : checks) {
            executor.execute(new CheckRunnable(check));
        }
        return executor;
    }

    public static Check initCheckOfWifi() {
        Check check = new Check(R.string.wifi_is_close,
                R.string.wifi_not_close, R.string.wifi_is_close_checking,
                new ICheckAction() {

                    @Override
                    public boolean check() {
                        NetworkType networkType = NetworkUtil.isConnectedType();
                        if (networkType == NetworkType.Wifi) {
                            return false;
                        } else {
                            return true;
                        }
                    }
                });
        check.setCheckOperation(new CheckOperation() {
            @Override
            public void operate() {
                Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                PrimerApplication.mContext.startActivity(intent);
            }
        });
        return check;
    }

    public static Check initCheckOf3G() {
        Check check = new Check(R.string.threeg_is_close,
                R.string.threeg_not_close, R.string.threeg_is_close_checking,
                new ICheckAction() {

                    @Override
                    public boolean check() {
                        NetworkType networkType = NetworkUtil.isConnectedType();
                        if (networkType == NetworkType.Mobile) {
                            return false;
                        } else {
                            return true;
                        }
                    }
                });
        check.setCheckOperation(new CheckOperation() {
            @Override
            public void operate() {
                Intent intent = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                PrimerApplication.mContext.startActivity(intent);
            }
        });
        return check;
    }

    public static Check initCheckOfBluetooth() {
        Check check = new Check(R.string.bluetooth_is_close,
                R.string.bluetooth_not_close,
                R.string.bluetooth_is_close_checking, new ICheckAction() {
            @Override
            public boolean check() {
                if (NetworkUtil.BluetoothIsConnected()) {
                    return false;
                } else {
                    return true;
                }
            }
        });
        check.setCheckOperation(new CheckOperation() {
            @Override
            public void operate() {
                Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                PrimerApplication.mContext.startActivity(intent);
            }
        });
        return check;
    }

    public static Check initCheckForPrivateKey(
            final Address address, final SecureCharSequence password) {
        String title = String.format(PrimerApplication.mContext.getString(R.string
                .check_address_private_key_title), address.getShortAddress());
        Check check = new Check(title, new ICheckAction() {

            @Override
            public boolean check() {
                PasswordSeed passwordSeed = new PasswordSeed(address.getAddress(), address.getFullEncryptPrivKey());
                boolean result = passwordSeed.checkPassword(password);
                if (!result) {
                    try {
                        ECKey eckeyFromBackup = BackupUtil.getEckeyFromBackup(
                                address.getAddress(), password);
                        if (eckeyFromBackup != null) {
                            String encryptPrivateKey = PrivateKeyUtil.getEncryptedString(eckeyFromBackup);
                            eckeyFromBackup.clearPrivateKey();
                            if (!Utils.isEmpty(encryptPrivateKey)) {
                                address.recoverFromBackup(encryptPrivateKey);
                                result = true;
                            }

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                password.wipe();
                return result;
            }
        });
        return check;
    }

    public static Check initCheckForHDMKeychain(final HDMKeychain keychain, final SecureCharSequence password) {
        int titleResource = R.string.hdm_keychain_check_title_cold;
        if (AppSharedPreference.getInstance().getAppMode() == PrimerjSettings.AppMode.HOT) {
            titleResource = R.string.hdm_keychain_check_title_hot;
        }
        String title = PrimerApplication.mContext.getString(titleResource);
        Check check = new Check(title, new ICheckAction() {
            @Override
            public boolean check() {
                boolean result = false;
                try {
                    result = keychain.checkWithPassword(password);
                    //TODO need to check backup here?
                    if(result){
                        result = keychain.checkSingularBackupWithPassword(password);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                password.wipe();
                return result;
            }
        });
        return check;
    }

    public static Check initCheckForHDAccount(final HDAccount account, final SecureCharSequence
            password) {
        String title = PrimerApplication.mContext.getString(R.string.address_group_hd);
        Check check = new Check(title, new ICheckAction() {
            @Override
            public boolean check() {
                boolean result;
                try {
                    result = account.checkWithPassword(password);
                } catch (Exception e) {
                    result = false;
                }
                password.wipe();
                return result;
            }
        });
        return check;
    }
}
