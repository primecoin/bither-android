/*
 *
 *  * Copyright 2014 http://Bither.net
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *    http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package org.primer.activity.hot;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import org.primer.PrimerSetting;
import org.primer.R;
import org.primer.primerj.AbstractApp;
import org.primer.primerj.api.http.Http400Exception;
import org.primer.primerj.core.AddressManager;
import org.primer.primerj.core.HDMAddress;
import org.primer.primerj.core.HDMBId;
import org.primer.primerj.core.HDMKeychain;
import org.primer.primerj.crypto.SecureCharSequence;
import org.primer.primerj.delegate.IPasswordGetterDelegate;
import org.primer.primerj.utils.Utils;
import org.primer.qrcode.ScanActivity;
import org.primer.runnable.ThreadNeedService;
import org.primer.service.BlockchainService;
import org.primer.ui.base.DropdownMessage;
import org.primer.ui.base.dialog.DialogConfirmTask;
import org.primer.ui.base.dialog.DialogPassword;
import org.primer.ui.base.dialog.DialogProgress;
import org.primer.ui.base.listener.IBackClickListener;
import org.primer.util.ExceptionUtil;
import org.primer.util.LogUtil;
import org.primer.util.ThreadUtil;

import java.util.ArrayList;
import java.util.List;

import kankan.wheel.widget.WheelView;
import kankan.wheel.widget.adapters.AbstractWheelTextAdapter;

/**
 * Created by songchenwen on 15/1/12.
 */
public class AddHDMAddressActivity extends FragmentActivity implements IPasswordGetterDelegate {
    private static final int ColdPubRequestCode = 1609;

    private WheelView wvCount;
    private DialogProgress dp;
    private HDMKeychain keychain;
    private DialogPassword.PasswordGetter passwordGetter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_hdm_address);
        keychain = AddressManager.getInstance().getHdmKeychain();
        if (keychain == null) {
            finish();
            return;
        }
        initView();
    }

    private void initView() {
        findViewById(R.id.ibtn_cancel).setOnClickListener(new IBackClickListener());
        findViewById(R.id.btn_add).setOnClickListener(addClick);
        wvCount = (WheelView) findViewById(R.id.wv_count);
        wvCount.setViewAdapter(new CountAdapter(this));
        wvCount.setCurrentItem(0);
        dp = new DialogProgress(this, R.string.please_wait);
        dp.setCancelable(false);
        passwordGetter = new DialogPassword.PasswordGetter(this, this);
    }

    private View.OnClickListener addClick = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            int count = wvCount.getCurrentItem() + 1;
            if (keychain.uncompletedAddressCount() < count) {
                new DialogConfirmTask(v.getContext(),
                        getString(R.string.hdm_address_add_need_cold_pub), new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                startActivityForResult(new Intent(AddHDMAddressActivity.this,
                                        ScanActivity.class), ColdPubRequestCode);
                            }
                        });
                    }
                }).show();
                return;
            }
            performAdd();
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (ColdPubRequestCode == requestCode && resultCode == RESULT_OK) {
            final String result = data.getStringExtra(ScanActivity.INTENT_EXTRA_RESULT);
            try {
                final byte[] pub = Utils.hexStringToByteArray(result);
                final int count = Math.min(AbstractApp.bitherjSetting.hdmAddressPerSeedCount() -
                                keychain.getAllCompletedAddresses().size() - keychain
                                .uncompletedAddressCount(),
                        AbstractApp.bitherjSetting.hdmAddressPerSeedPrepareCount());
                new Thread() {
                    @Override
                    public void run() {
                        final SecureCharSequence password = passwordGetter.getPassword();
                        if (password == null) {
                            return;
                        }
                        try {
                            int prepared = keychain.prepareAddresses(count, password, pub);
                            LogUtil.i("Add", "try to prepare: " + count + ", prepared: " + prepared);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    performAdd();
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                            if (e instanceof HDMKeychain.HDMColdPubNotSameException) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        DropdownMessage.showDropdownMessage(AddHDMAddressActivity
                                                .this, R.string.hdm_address_add_cold_pub_not_match);
                                    }
                                });
                            }
                        }
                    }
                }.start();
            } catch (Exception e) {
                e.printStackTrace();
                DropdownMessage.showDropdownMessage(this, R.string.hdm_address_add_need_cold_pub);
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void performAdd() {
        final int count = wvCount.getCurrentItem() + 1;
        final DialogProgress dd = dp;
        new ThreadNeedService(null, this) {
            @Override
            public void runWithService(final BlockchainService service) {
                final SecureCharSequence password = passwordGetter.getPassword();
                if (password == null) {
                    return;
                }
                if (service != null) {
                    service.stopAndUnregister();
                }
                final List<HDMAddress> as = keychain.completeAddresses(count, password,
                        new HDMKeychain.HDMFetchRemotePublicKeys() {
                            @Override
                            public void completeRemotePublicKeys(CharSequence password,
                                                                 List<HDMAddress.Pubs>
                                                                         partialPubs) {
                                try {
                                    HDMBId hdmBid = HDMBId.getHDMBidFromDb();
                                    HDMKeychain.getRemotePublicKeys(hdmBid, password, partialPubs);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    int msg = R.string.network_or_connection_error;
                                    if (e instanceof Http400Exception) {
                                        msg = ExceptionUtil.getHDMHttpExceptionMessage((
                                                (Http400Exception) e).getErrorCode());
                                    }
                                    final int m = msg;
                                    ThreadUtil.runOnMainThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (dd.isShowing()) {
                                                dd.dismiss();
                                            }
                                            DropdownMessage.showDropdownMessage(AddHDMAddressActivity.this, m);
                                        }
                                    });
                                }
                            }
                        });
                LogUtil.i("Add", "try to complete: " + count + ", completed: " + as.size());
                if (service != null) {
                    service.startAndRegister();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (dd.isShowing()) {
                            dd.dismiss();
                        }
                        if (as.size() == 0) {
                            return;
                        }
                        ArrayList<String> s = new ArrayList<String>();
                        for (HDMAddress a : as) {
                            s.add(a.getAddress());
                        }
                        Intent intent = new Intent();
                        intent.putExtra(PrimerSetting.INTENT_REF.ADDRESS_POSITION_PASS_VALUE_TAG,
                                s);
                        setResult(Activity.RESULT_OK, intent);
                        finish();
                    }
                });
            }
        }.start();
    }

    private class CountAdapter extends AbstractWheelTextAdapter {

        protected CountAdapter(Context context) {
            super(context);
        }

        @Override
        public int getItemsCount() {
            int max = AbstractApp.bitherjSetting.hdmAddressPerSeedCount() - AddressManager.getInstance
                    ().getHdmKeychain().getAllCompletedAddresses().size();
            return max;
        }

        @Override
        protected CharSequence getItemText(int index) {
            return String.valueOf(index + 1);
        }
    }

    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.slide_out_bottom);
    }

    @Override
    public void beforePasswordDialogShow() {
        if (dp.isShowing()) {
            dp.dismiss();
        }
    }

    @Override
    public void afterPasswordDialogDismiss() {
        if (!dp.isShowing()) {
            dp.show();
        }
    }
}
