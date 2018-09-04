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

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import org.primer.PrimerSetting;
import org.primer.R;
import org.primer.SendActivity;
import org.primer.primerj.core.AddressManager;
import org.primer.primerj.core.EnterpriseHDMAddress;
import org.primer.primerj.core.EnterpriseHDMTxSignaturePool;
import org.primer.primerj.core.Tx;
import org.primer.primerj.crypto.KeyCrypterException;
import org.primer.primerj.crypto.mnemonic.MnemonicException;
import org.primer.primerj.exception.TxBuilderException;
import org.primer.primerj.utils.Utils;
import org.primer.runnable.CompleteTransactionRunnable;
import org.primer.ui.base.DropdownMessage;
import org.primer.ui.base.dialog.DialogSendConfirm;

/**
 * Created by songchenwen on 15/6/9.
 */
public class EnterpriseHDMSendActivity extends SendActivity implements DialogSendConfirm
        .SendConfirmListener {
    private static final int SignCode = 1519;

    private long btcAmount;
    private String toAddress;
    private Tx tx;

    static {
        CompleteTransactionRunnable.registerTxBuilderExceptionMessages();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        etPassword.setVisibility(View.GONE);
        findViewById(R.id.tv_password).setVisibility(View.GONE);
        btnSend.setCompoundDrawablesWithIntrinsicBounds(R.drawable
                .unsigned_transaction_button_icon_mirror_transparent, 0, R.drawable
                .unsigned_transaction_button_icon, 0);
    }

    @Override
    protected void initAddress() {
        if (!AddressManager.getInstance().hasEnterpriseHDMKeychain()) {
            return;
        }
        if (getIntent().getExtras().containsKey(PrimerSetting.INTENT_REF
                .ADDRESS_POSITION_PASS_VALUE_TAG)) {
            addressPosition = getIntent().getExtras().getInt(PrimerSetting.INTENT_REF
                    .ADDRESS_POSITION_PASS_VALUE_TAG);
            if (addressPosition >= 0 && addressPosition < AddressManager.getInstance()
                    .getEnterpriseHDMKeychain().getAddresses().size()) {
                address = AddressManager.getInstance().getEnterpriseHDMKeychain().getAddresses()
                        .get(addressPosition);
            }
        }
    }

    @Override
    protected void sendClicked() {
        final long btc = amountCalculatorLink.getAmount();
        if (btc > 0) {
            btcAmount = btc;
            tx = null;
            String address = etAddress.getText().toString().trim();
            if (Utils.validBicoinAddress(address)) {
                toAddress = address;
                if (!dp.isShowing()) {
                    dp.show();
                }
                new Thread() {
                    @Override
                    public void run() {
                        send();
                    }
                }.start();
            } else {
                DropdownMessage.showDropdownMessage(EnterpriseHDMSendActivity.this, R.string
                        .send_failed);
            }
        }
    }

    private void send() {
        tx = null;
        String changeTo = getChangeAddress();
        try {
            tx = address.buildTx(btcAmount, toAddress, changeTo == null ? address.getAddress() : changeTo);
        } catch (Exception e) {
            e.printStackTrace();
            btcAmount = 0;
            tx = null;
            String msg = getString(R.string.send_failed);
            if (e instanceof KeyCrypterException || e instanceof MnemonicException
                    .MnemonicLengthException) {
                msg = getString(R.string.password_wrong);
            } else if (e instanceof TxBuilderException) {
                msg = e.getMessage();
            }
            final String m = msg;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (dp.isShowing()) {
                        dp.dismiss();
                    }
                    DropdownMessage.showDropdownMessage(EnterpriseHDMSendActivity.this, m);
                }
            });
        }
        if (tx != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showConfirm();
                }
            });
        }
    }

    private String getChangeAddress() {
        String change = dialogSelectChangeAddress.getChangeAddress().getAddress();
        if (Utils.compareString(change, address.getAddress())) {
            return null;
        }
        return change;
    }

    private void showConfirm() {
        if (dp.isShowing()) {
            dp.dismiss();
        }
        new DialogSendConfirm(this, tx, getChangeAddress(), this).show();
    }

    @Override
    public void onConfirm(Tx t) {
        EnterpriseHDMAddress a = (EnterpriseHDMAddress) address;
        startActivityForResult(EnterpriseHDMSendCollectSignatureActivity.start(this, new
                EnterpriseHDMTxSignaturePool(tx, a.threshold(), a.getPubkeys()), getChangeAddress
                (), a.getIndex()), SignCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SignCode) {
            if (resultCode == RESULT_OK) {
                setResult(RESULT_OK);
                finish();
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onCancel() {
        tx = null;
        toAddress = null;
        btcAmount = 0;
    }
}
