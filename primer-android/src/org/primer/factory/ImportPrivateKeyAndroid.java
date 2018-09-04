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

package org.primer.factory;

import android.app.Activity;

import org.primer.R;
import org.primer.activity.cold.ColdActivity;
import org.primer.activity.cold.ColdAdvanceActivity;
import org.primer.activity.hot.HotActivity;
import org.primer.activity.hot.HotAdvanceActivity;
import org.primer.primerj.core.Address;
import org.primer.primerj.crypto.ECKey;
import org.primer.primerj.crypto.SecureCharSequence;
import org.primer.primerj.factory.ImportPrivateKey;
import org.primer.runnable.ThreadNeedService;
import org.primer.service.BlockchainService;
import org.primer.ui.base.DropdownMessage;
import org.primer.ui.base.dialog.DialogImportPrivateKeyAddressValidation;
import org.primer.ui.base.dialog.DialogProgress;
import org.primer.util.KeyUtil;
import org.primer.util.ThreadUtil;

import java.util.ArrayList;
import java.util.List;

public class ImportPrivateKeyAndroid extends ImportPrivateKey {

    private DialogProgress dp;
    private Activity activity;

    public ImportPrivateKeyAndroid(Activity activity, ImportPrivateKeyType importPrivateKeyType, DialogProgress dp, String content, SecureCharSequence password) {
        super(importPrivateKeyType, content, password);
        this.activity = activity;
        this.dp = dp;
    }

    @Override
    public void importError(final int errorCode) {
        ThreadUtil.runOnMainThread(new Runnable() {
            @Override
            public void run() {
                if (dp != null && dp.isShowing()) {
                    dp.setThread(null);
                    dp.dismiss();
                }
                switch (errorCode) {
                    case PASSWORD_WRONG:
                        DropdownMessage.showDropdownMessage(activity, R.string.password_wrong);
                        break;
                    case NETWORK_FAILED:
                        DropdownMessage.showDropdownMessage(activity, R.string.network_or_connection_error);
                        break;
                    case CAN_NOT_IMPORT_BITHER_COLD_PRIVATE_KEY:
                        DropdownMessage.showDropdownMessage(activity,
                                R.string.import_private_key_qr_code_failed_monitored);
                        break;
                    case PRIVATE_KEY_ALREADY_EXISTS:
                        DropdownMessage.showDropdownMessage(activity,
                                R.string.import_private_key_qr_code_failed_duplicate);
                        break;
                    case PASSWORD_IS_DIFFEREND_LOCAL:
                        DropdownMessage.showDropdownMessage(activity,
                                R.string.import_private_key_qr_code_failed_different_password);
                        break;
                    case CONTAIN_SPECIAL_ADDRESS:
                        DropdownMessage.showDropdownMessage(activity,
                                R.string.import_private_key_failed_special_address);
                        break;
                    case TX_TOO_MUCH:
                        DropdownMessage.showDropdownMessage(activity,
                                R.string.import_private_key_failed_tx_too_mush);
                        break;
                    default:
                        DropdownMessage.showDropdownMessage(activity, R.string.import_private_key_qr_code_failed);
                        break;
                }

            }
        });

    }


    public void importPrivateKey() {
        if (importPrivateKeyType != ImportPrivateKeyType.Text) {
            Address address = initPrivateKey();
            importPrivateKey(address);
            return;
        }

        boolean isCompressed = getIsCompressed();
        final ECKey compressKey = initEcKey(true);
        if (compressKey == null) {
            return;
        }
        final ECKey uncompressedKey = initEcKey(false);
        if (uncompressedKey == null) {
            return;
        }
        DialogImportPrivateKeyAddressValidation dialogImportPrivateKeyAddressValidation = new DialogImportPrivateKeyAddressValidation(activity, compressKey.toAddress(), uncompressedKey.toAddress(), isCompressed, new Runnable() {
            @Override
            public void run() {
                Address compressAddress = initPrivateKey(true);
                if (compressAddress != null) {
                    importPrivateKey(compressAddress);
                }
            }
        }, new Runnable() {
            @Override
            public void run() {
                Address uncompressedAddress = initPrivateKey(false);
                if (uncompressedAddress != null) {
                    importPrivateKey(uncompressedAddress);
                }
            }
        });
        dialogImportPrivateKeyAddressValidation.show();
    }

    private void importPrivateKey(final Address address) {
        if (address==null)return;
        new ThreadNeedService(dp, activity) {
            @Override
            public void runWithService(BlockchainService service) {
                if (address != null) {
                    List<Address> addressList = new ArrayList<Address>();
                    addressList.add(address);
                    KeyUtil.addAddressListByDesc(service, addressList);
                    ThreadUtil.runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            if (dp != null && dp.isShowing()) {
                                dp.setThread(null);
                                dp.dismiss();
                            }
                            if (activity instanceof HotAdvanceActivity) {
                                ((HotAdvanceActivity) activity).showImportSuccess();
                            }
                            if (activity instanceof HotActivity) {
                                ((HotActivity) activity).showImportSuccess();
                            }
                            if (activity instanceof ColdAdvanceActivity) {
                                ((ColdAdvanceActivity) activity).showImportSuccess();
                            }
                            if (activity instanceof ColdActivity) {
                                ((ColdActivity) activity).showImportSuccess();
                            }
                        }
                    });
                }
            }
        }.start();
    }

}
