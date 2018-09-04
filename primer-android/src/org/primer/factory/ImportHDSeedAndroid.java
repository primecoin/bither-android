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
import org.primer.activity.cold.HdmImportWordListActivity;
import org.primer.activity.hot.HotActivity;
import org.primer.activity.hot.HotAdvanceActivity;
import org.primer.primerj.PrimerjSettings;
import org.primer.primerj.core.HDAccount;
import org.primer.primerj.core.HDAccountCold;
import org.primer.primerj.core.HDMKeychain;
import org.primer.primerj.crypto.SecureCharSequence;
import org.primer.primerj.crypto.mnemonic.MnemonicCode;
import org.primer.primerj.factory.ImportHDSeed;
import org.primer.preference.AppSharedPreference;
import org.primer.runnable.ThreadNeedService;
import org.primer.service.BlockchainService;
import org.primer.ui.base.DropdownMessage;
import org.primer.ui.base.dialog.DialogProgress;
import org.primer.util.BackupUtil;
import org.primer.util.KeyUtil;
import org.primer.util.ThreadUtil;

import java.util.List;

public class ImportHDSeedAndroid extends ImportHDSeed {
    private DialogProgress dp;
    private Activity activity;

    public ImportHDSeedAndroid(Activity activity, DialogProgress dp, String content, SecureCharSequence password) {
        super(ImportHDSeedType.HDMColdSeedQRCode, content, null, password);
        this.activity = activity;
        this.dp = dp;
    }

    public ImportHDSeedAndroid(Activity activity, DialogProgress dp, List<String> worlds, SecureCharSequence password) {
        super(ImportHDSeedType.HDMColdPhrase, null, worlds, password);
        this.activity = activity;
        this.dp = dp;
    }


    public ImportHDSeedAndroid(Activity activity, ImportHDSeedType importHDSeedType,
                               DialogProgress dp, String content, List<String> worlds, SecureCharSequence password, MnemonicCode mnemonicCode) {
        super(importHDSeedType, content, worlds, password, mnemonicCode);
        this.activity = activity;
        this.dp = dp;
    }


    public void importHDMColdSeed() {

        new ThreadNeedService(dp, activity) {
            @Override
            public void runWithService(BlockchainService service) {
                HDMKeychain result = importHDMKeychain();
                if (result != null) {

                    KeyUtil.setHDKeyChain(result);
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
                            if (activity instanceof HdmImportWordListActivity) {
                                HdmImportWordListActivity hdmImportWordListActivity = (HdmImportWordListActivity) activity;
                                hdmImportWordListActivity.showImportSuccess();
                                hdmImportWordListActivity.finish();
                            }

                        }
                    });
                } else {
                    ThreadUtil.runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            if (dp != null && dp.isShowing()) {
                                dp.setThread(null);
                                dp.dismiss();
                            }
                        }
                    });
                }
            }
        }.start();

    }

    public void importHDSeed() {
        new ThreadNeedService(dp, activity) {
            @Override
            public void runWithService(BlockchainService service) {
                if (service != null) {
                    service.stopAndUnregister();
                }
                boolean success = false;
                if (AppSharedPreference.getInstance().getAppMode() == PrimerjSettings.AppMode
                        .COLD) {
                    HDAccountCold result = importHDAccountCold();
                    BackupUtil.backupColdKey(false);
                    if (result != null) {
                        success = true;
                    }
                } else {
                    HDAccount result = importHDAccount();
                    if (result != null) {
                        KeyUtil.setHDAccount(result);
                        success = true;
                    }
                }
                if (success) {
                    AppSharedPreference.getInstance().setMnemonicWordList(mnemonicCode.getMnemonicWordList());
                    MnemonicCode.setInstance(mnemonicCode);
                    ThreadUtil.runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            if (dp != null && dp.isShowing()) {
                                dp.setThread(null);
                                dp.dismiss();
                            }
                            if (activity instanceof HotAdvanceActivity) {
                                HotAdvanceActivity hotAdvanceActivity = (HotAdvanceActivity) activity;
                                hotAdvanceActivity.showImportSuccess();
                            }
                            if (activity instanceof HotActivity) {
                                ((HotActivity) activity).showImportSuccess();

                            }
                            if (activity instanceof ColdAdvanceActivity) {
                                ColdAdvanceActivity coldAdvanceActivity = (ColdAdvanceActivity) activity;
                                coldAdvanceActivity.showImportSuccess();
                            }
                            if (activity instanceof ColdActivity) {
                                ((ColdActivity) activity).showImportSuccess();
                            }

                            if (activity instanceof HdmImportWordListActivity) {
                                HdmImportWordListActivity hdmImportWordListActivity = (HdmImportWordListActivity) activity;
                                hdmImportWordListActivity.showImportSuccess();
                                hdmImportWordListActivity.setResult(Activity.RESULT_OK);
                                hdmImportWordListActivity.finish();
                            }
                        }
                    });

                } else {
                    ThreadUtil.runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            if (dp != null && dp.isShowing()) {
                                dp.setThread(null);
                                dp.dismiss();
                            }
                        }
                    });
                }
                if (service != null) {
                    service.startAndRegister();
                }
            }
        }.start();
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
                    case PASSWORD_IS_DIFFEREND_LOCAL:
                        DropdownMessage.showDropdownMessage(activity,
                                R.string.import_private_key_qr_code_failed_different_password);
                        break;
                    case NOT_HDM_COLD_SEED:
                        DropdownMessage.showDropdownMessage(activity,
                                R.string.import_hdm_cold_seed_format_error);
                        break;
                    case NOT_HD_ACCOUNT_SEED:
                        DropdownMessage.showDropdownMessage(activity,
                                R.string.import_hd_account_seed_format_error);
                        break;
                    case DUPLICATED_HD_ACCOUNT_SEED:
                        DropdownMessage.showDropdownMessage(activity,
                                R.string.import_hd_account_failed_duplicated);
                        break;
                    default:
                        DropdownMessage.showDropdownMessage(activity, R.string.import_private_key_qr_code_failed);
                        break;
                }

            }
        });

    }
}
