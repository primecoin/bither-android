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

package net.bither.activity.hot;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import net.bither.PrimerSetting;
import net.bither.qrcode.ScanActivity;
import net.bither.ui.base.CurrencyAmountView;
import net.bither.ui.base.CurrencyCalculatorLink;
import net.bither.ui.base.DropdownMessage;
import net.bither.ui.base.SwipeRightActivity;
import net.bither.ui.base.dialog.DialogConfirmTask;
import net.bither.ui.base.dialog.DialogRCheck;
import net.bither.ui.base.dialog.DialogSelectChangeAddress;
import net.bither.ui.base.dialog.DialogSendConfirm;
import net.bither.ui.base.dialog.DialogSendOption;
import net.bither.ui.base.keyboard.EntryKeyboardView;
import net.bither.ui.base.keyboard.amount.AmountEntryKeyboardView;
import net.bither.ui.base.listener.IBackClickListener;
import net.bither.util.BroadcastUtil;
import net.bither.util.ExchangeUtil;
import net.bither.util.InputParser;
import net.bither.util.ThreadUtil;
import net.bither.util.UnitUtilWrapper;

import net.bither.PrimerSetting;
import net.bither.R;
import net.bither.bitherj.PrimerjSettings;
import net.bither.bitherj.core.Address;
import net.bither.bitherj.core.AddressManager;
import net.bither.bitherj.core.Tx;
import net.bither.bitherj.core.UnSignTransaction;
import net.bither.bitherj.qrcode.QRCodeTxTransport;
import net.bither.bitherj.utils.TransactionsUtil;
import net.bither.bitherj.utils.Utils;
import net.bither.preference.AppSharedPreference;
import net.bither.qrcode.ScanActivity;
import net.bither.runnable.CommitTransactionThread;
import net.bither.runnable.CompleteTransactionRunnable;
import net.bither.runnable.HandlerMessage;
import net.bither.runnable.RCheckRunnable;
import net.bither.ui.base.CurrencyAmountView;
import net.bither.ui.base.CurrencyCalculatorLink;
import net.bither.ui.base.DropdownMessage;
import net.bither.ui.base.SwipeRightActivity;
import net.bither.ui.base.dialog.DialogConfirmTask;
import net.bither.ui.base.dialog.DialogRCheck;
import net.bither.ui.base.dialog.DialogSelectChangeAddress;
import net.bither.ui.base.dialog.DialogSendConfirm;
import net.bither.ui.base.dialog.DialogSendConfirm.SendConfirmListener;
import net.bither.ui.base.dialog.DialogSendOption;
import net.bither.ui.base.keyboard.EntryKeyboardView;
import net.bither.ui.base.keyboard.amount.AmountEntryKeyboardView;
import net.bither.ui.base.listener.IBackClickListener;
import net.bither.util.BroadcastUtil;
import net.bither.util.ExchangeUtil;
import net.bither.util.InputParser.StringInputParser;
import net.bither.util.ThreadUtil;
import net.bither.util.UnitUtilWrapper;


public class GenerateUnsignedTxActivity extends SwipeRightActivity implements EntryKeyboardView.EntryKeyboardViewListener, CommitTransactionThread.CommitTransactionListener,
        DialogSendOption.DialogSendOptionListener {
    private static final String ADDRESS_POSITION_SAVE_KEY = "address_position";
    private int addressPosition;
    private Address address;
    private TextView tvAddressLabel;
    private EditText etAddress;
    private ImageButton ibtnScan;
    private CurrencyCalculatorLink amountCalculatorLink;
    private Button btnSend;
    private DialogRCheck dp;
    private TextView tvBalance;
    private ImageView ivBalanceSymbol;
    private AmountEntryKeyboardView kvAmount;
    private View vKeyboardContainer;
    private DialogSelectChangeAddress dialogSelectChangeAddress;

    private Tx tx;

    private boolean needConfirm = true;

    private boolean isDonate = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.slide_in_right, 0);
        setContentView(R.layout.activity_generate_unsigned_tx);
        if (getIntent().getExtras().containsKey(PrimerSetting.INTENT_REF
                .ADDRESS_POSITION_PASS_VALUE_TAG)) {
            addressPosition = getIntent().getExtras().getInt(PrimerSetting.INTENT_REF
                    .ADDRESS_POSITION_PASS_VALUE_TAG);
            if (addressPosition >= 0 && addressPosition < AddressManager.getInstance()
                    .getWatchOnlyAddresses().size()) {
                address = AddressManager.getInstance().getWatchOnlyAddresses().get(addressPosition);
            }
        }
        if (savedInstanceState != null && savedInstanceState.containsKey
                (ADDRESS_POSITION_SAVE_KEY)) {
            addressPosition = savedInstanceState.getInt(ADDRESS_POSITION_SAVE_KEY);
            if (addressPosition >= 0 && addressPosition < AddressManager.getInstance()
                    .getWatchOnlyAddresses().size()) {
                address = AddressManager.getInstance().getWatchOnlyAddresses().get(addressPosition);
            }
            if (address != null) {
                UnSignTransaction utx = TransactionsUtil.getUnsignTxFromCache(address.getAddress());
                if (utx != null) {
                    tx = utx.getTx();
                }
            }
        }
        if (address == null) {
            finish();
            return;
        }
        initView();
        processIntent();
        configureDonate();
    }

    private void initView() {
        findViewById(R.id.ibtn_cancel).setOnClickListener(new IBackClickListener());
        tvAddressLabel = (TextView) findViewById(R.id.tv_address_label);
        etAddress = (EditText) findViewById(R.id.et_address);
        ibtnScan = (ImageButton) findViewById(R.id.ibtn_scan);
        btnSend = (Button) findViewById(R.id.btn_send);
        tvBalance = (TextView) findViewById(R.id.tv_balance);
        ivBalanceSymbol = (ImageView) findViewById(R.id.iv_balance_symbol);
        tvBalance.setText(UnitUtilWrapper.formatValue(address.getBalance()));
        ivBalanceSymbol.setImageBitmap(UnitUtilWrapper.getBtcSymbol(tvBalance));
        kvAmount = (AmountEntryKeyboardView) findViewById(R.id.kv_amount);
        vKeyboardContainer = findViewById(R.id.v_keyboard_container);
        findViewById(R.id.ibtn_option).setOnClickListener(optionClick);
        dialogSelectChangeAddress = new DialogSelectChangeAddress(this, address);
        final CurrencyAmountView btcAmountView = (CurrencyAmountView) findViewById(R.id.cav_btc);
        btcAmountView.setCurrencySymbol(getString(R.string.bitcoin_symbol));
        int precision = (int) Math.floor(Math.log10(AppSharedPreference.getInstance()
                .getBitcoinUnit().satoshis));
        btcAmountView.setInputPrecision(precision);
        btcAmountView.setHintPrecision(Math.min(4, precision));
        btcAmountView.setShift(8 - precision);

        final CurrencyAmountView localAmountView = (CurrencyAmountView) findViewById(R.id
                .cav_local);
        localAmountView.setInputPrecision(2);
        localAmountView.setHintPrecision(2);
        amountCalculatorLink = new CurrencyCalculatorLink(btcAmountView, localAmountView);
        ReceivingAddressListener addressListener = new ReceivingAddressListener();
        etAddress.setOnFocusChangeListener(addressListener);
        etAddress.addTextChangedListener(addressListener);
        dp = new DialogRCheck(this);
        ibtnScan.setOnClickListener(scanClick);
        btnSend.setOnClickListener(sendClick);
        kvAmount.registerEditText((EditText) findViewById(R.id.send_coins_amount_btc_edittext),
                (EditText) findViewById(R.id.send_coins_amount_local_edittext)).setListener(this);
        findViewById(R.id.ll_balance).setOnClickListener(balanceClick);
    }

    private OnClickListener balanceClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            amountCalculatorLink.setBtcAmount(address.getBalance());
        }
    };

    private OnClickListener scanClick = new OnClickListener() {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(GenerateUnsignedTxActivity.this, ScanActivity.class);
            startActivityForResult(intent, PrimerSetting.INTENT_REF.SCAN_REQUEST_CODE);
        }
    };

    private DialogSendConfirm.SendConfirmListener sendConfirmListener = new DialogSendConfirm.SendConfirmListener() {

        @Override
        public void onConfirm(Tx tx) {
            GenerateUnsignedTxActivity.this.tx = tx;
            String addressCannotBtParsed = getString(R.string.address_cannot_be_parsed);
            Intent intent = new Intent(GenerateUnsignedTxActivity.this,
                    UnsignedTxQrCodeActivity.class);
            String changeAddress = dialogSelectChangeAddress
                    .getChangeAddress().equals(address) ? null :
                    dialogSelectChangeAddress.getChangeAddress().getAddress();
            intent.putExtra(PrimerSetting.INTENT_REF.QR_CODE_STRING,
                    QRCodeTxTransport.getPresignTxString(tx, changeAddress, addressCannotBtParsed, QRCodeTxTransport.NO_HDM_INDEX));
            if (Utils.isEmpty(changeAddress)) {
                intent.putExtra(PrimerSetting.INTENT_REF.OLD_QR_CODE_STRING,
                        QRCodeTxTransport.oldGetPreSignString(tx, addressCannotBtParsed));
            } else {
                intent.putExtra(PrimerSetting.INTENT_REF.QR_CODE_HAS_CHANGE_ADDRESS_STRING
                        , true);
            }

            intent.putExtra(PrimerSetting.INTENT_REF.TITLE_STRING,
                    getString(R.string.unsigned_transaction_qr_code_title));
            startActivityForResult(intent, PrimerSetting.INTENT_REF.SIGN_TX_REQUEST_CODE);
            btnSend.setEnabled(true);
        }

        @Override
        public void onCancel() {

        }
    };

    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(ADDRESS_POSITION_SAVE_KEY, addressPosition);
        super.onSaveInstanceState(outState);
    }

    private Handler completeTransactionHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case HandlerMessage.MSG_SUCCESS:
                    if (dp.isShowing()) {
                        dp.dismiss();
                    }
                    if (msg.obj != null && msg.obj instanceof Tx) {
                        Tx tx = (Tx) msg.obj;
                        if (needConfirm) {
                            DialogSendConfirm dialog = new DialogSendConfirm
                                    (GenerateUnsignedTxActivity.this, tx,
                                            dialogSelectChangeAddress.getChangeAddress().equals
                                                    (address) ? null : dialogSelectChangeAddress
                                                    .getChangeAddress().getAddress(),
                                            sendConfirmListener);
                            dialog.show();
                        } else {
                            sendConfirmListener.onConfirm(tx);
                        }
                    } else {
                        DropdownMessage.showDropdownMessage(GenerateUnsignedTxActivity.this,
                                R.string.password_wrong);
                    }
                    break;
                case HandlerMessage.MSG_PASSWORD_WRONG:
                    if (dp.isShowing()) {
                        dp.dismiss();
                    }
                    DropdownMessage.showDropdownMessage(GenerateUnsignedTxActivity.this,
                            R.string.password_wrong);
                    break;
                case HandlerMessage.MSG_FAILURE:
                    if (dp.isShowing()) {
                        dp.dismiss();
                    }
                    String msgError = getString(R.string.send_failed);
                    if (msg.obj instanceof String) {
                        msgError = (String) msg.obj;
                    }
                    DropdownMessage.showDropdownMessage(GenerateUnsignedTxActivity.this, msgError);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onCommitTransactionSuccess(Tx tx) {
        needConfirm = true;
        if (dp.isShowing()) {
            dp.dismiss();
        }
        Intent intent = getIntent();
        if (tx != null) {
            intent.putExtra(SelectAddressToSendActivity.INTENT_EXTRA_TRANSACTION,
                    tx.getHashAsString());
        }
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onCommitTransactionFailed() {
        needConfirm = true;
        btnSend.setEnabled(true);
        DropdownMessage.showDropdownMessage(GenerateUnsignedTxActivity.this, R.string.send_failed);
    }

    private OnClickListener sendClick = new OnClickListener() {

        @Override
        public void onClick(View v) {
            final long btc = amountCalculatorLink.getAmount();
            if (btc > 0) {
                if (Utils.validBicoinAddress(etAddress.getText().toString().trim())) {
                    if (Utils.compareString(etAddress.getText().toString().trim(),
                            dialogSelectChangeAddress.getChangeAddress().getAddress())) {
                        DropdownMessage.showDropdownMessage(GenerateUnsignedTxActivity.this,
                                R.string.select_change_address_change_to_same_warn);
                        return;
                    }
                    try {
                        CompleteTransactionRunnable completeRunnable = new
                                CompleteTransactionRunnable(addressPosition, amountCalculatorLink
                                .getAmount(), etAddress.getText().toString().trim(),
                                dialogSelectChangeAddress.getChangeAddress().getAddress(), null);
                        completeRunnable.setHandler(completeTransactionHandler);
                        Thread thread = new Thread(completeRunnable);
                        dp.setThread(thread);
                        if (!dp.isShowing()) {
                            dp.show();
                        }
                        thread.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                        DropdownMessage.showDropdownMessage(GenerateUnsignedTxActivity.this,
                                R.string.send_failed);
                    }
                } else {
                    DropdownMessage.showDropdownMessage(GenerateUnsignedTxActivity.this,
                            R.string.send_failed);
                }
            }
        }
    };

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PrimerSetting.INTENT_REF.SCAN_REQUEST_CODE && resultCode == Activity
                .RESULT_OK) {
            final String input = data.getStringExtra(ScanActivity.INTENT_EXTRA_RESULT);
            new InputParser.StringInputParser(input, null) {
                @Override
                protected void bitcoinRequest(final String address, final String addressLabel,
                                              final long amount, final String bluetoothMac) {
                    etAddress.setText(address.toString());
                    if (amount > 0) {
                        amountCalculatorLink.setBtcAmount(amount);
                    }
                    amountCalculatorLink.requestFocus();
                    validateValues();
                }

                @Override
                protected void error(final int messageResId, final Object... messageArgs) {
                    DropdownMessage.showDropdownMessage(GenerateUnsignedTxActivity.this,
                            R.string.scan_watch_only_address_error);
                }
            }.parse();
            return;
        }
        if (requestCode == PrimerSetting.INTENT_REF.SIGN_TX_REQUEST_CODE && resultCode ==
                RESULT_OK) {
            final String qr = data.getStringExtra(ScanActivity.INTENT_EXTRA_RESULT);
            btnSend.setEnabled(false);
            btnSend.postDelayed(new Runnable() {
                @Override
                public void run() {
                    boolean success;
                    try {
                        success = TransactionsUtil.signTransaction(tx, qr);
                    } catch (Exception e) {
                        success = false;
                        e.printStackTrace();
                    }
                    if (success) {
                        RCheckRunnable runnable = new RCheckRunnable(address, tx);
                        runnable.setHandler(rcheckHandler);
                        new Thread(runnable).start();
                        // dp.setRChecking();
                        if (!dp.isShowing()) {
                            dp.show();
                        }
                        return;
                    }
                    if (dp.isShowing()) {
                        dp.dismiss();
                    }
                    btnSend.setEnabled(true);
                    DropdownMessage.showDropdownMessage(GenerateUnsignedTxActivity.this,
                            R.string.unsigned_transaction_sign_failed);
                }
            }, 500);
        }
    }

    private Handler rcheckHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HandlerMessage.MSG_SUCCESS:
                    if (msg.obj != null && msg.obj instanceof Tx) {
                        final Tx tx = (Tx) msg.obj;
                        // dp.setRCheckSuccess();
                        if (!dp.isShowing()) {
                            dp.show();
                        }
                        tvAddressLabel.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                dp.setWait();
                                try {
                                    new CommitTransactionThread(dp, addressPosition, tx, false,
                                            false, GenerateUnsignedTxActivity.this).start();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }, 800);
                        break;
                    }
                case HandlerMessage.MSG_FAILURE:
                    if (dp.isShowing()) {
                        dp.dismiss();
                    }
                    new DialogConfirmTask(GenerateUnsignedTxActivity.this,
                            getString(R.string.rcheck_fail_recalculate_confirm), new Runnable() {
                        @Override
                        public void run() {
                            needConfirm = false;
                            ThreadUtil.runOnMainThread(new Runnable() {
                                @Override
                                public void run() {
                                    // dp.setRecalculatingR();
                                    sendClick.onClick(btnSend);
                                }
                            });
                        }
                    }, new Runnable() {
                        @Override
                        public void run() {
                            needConfirm = true;
                            ThreadUtil.runOnMainThread(new Runnable() {
                                @Override
                                public void run() {
                                    btnSend.setEnabled(true);
                                }
                            });
                        }
                    }).show();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onEntryKeyboardShow(EntryKeyboardView v) {
        vKeyboardContainer.setVisibility(View.VISIBLE);
    }

    @Override
    public void onEntryKeyboardHide(EntryKeyboardView v) {
        vKeyboardContainer.setVisibility(View.GONE);
    }

    @Override
    public void onSelectChangeAddress() {
        dialogSelectChangeAddress.show();
    }

    private final class ReceivingAddressListener implements OnFocusChangeListener, TextWatcher {
        @Override
        public void onFocusChange(final View v, final boolean hasFocus) {
            if (!hasFocus) {
                validateValues();
            }
        }

        @Override
        public void afterTextChanged(final Editable s) {
            validateValues();
        }

        @Override
        public void beforeTextChanged(final CharSequence s, final int start, final int count,
                                      final int after) {
        }

        @Override
        public void onTextChanged(final CharSequence s, final int start, final int before,
                                  final int count) {
        }
    }

    private final CurrencyAmountView.Listener amountsListener = new CurrencyAmountView.Listener() {
        @Override
        public void changed() {
            validateValues();
        }

        @Override
        public void done() {
            validateValues();
            btnSend.requestFocusFromTouch();
        }

        @Override
        public void focusChanged(final boolean hasFocus) {
            if (!hasFocus) {
                validateValues();
            }
        }
    };

    private void validateValues() {
        boolean isValidAmounts = false;

        final long amount = amountCalculatorLink.getAmount();

        if (amount > 0) {
            isValidAmounts = true;
        }
        String address = etAddress.getText().toString().trim();
        boolean isValidAddress = Utils.validBicoinAddress(address);
        btnSend.setEnabled(isValidAddress && isValidAmounts);
    }

    private double getExchangeRate() {
        // 获取汇率
       /* Ticker ticker = MarketUtil.getTickerOfDefaultMarket();
        if (ticker != null) {
            return ticker.getDefaultExchangePrice();
        }*/
        return  ExchangeUtil.getCurrentRate();
    }

    private BroadcastReceiver marketBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            amountCalculatorLink.setExchangeRate(getExchangeRate());
        }
    };

    private OnClickListener optionClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            new DialogSendOption(GenerateUnsignedTxActivity.this, address,
                    GenerateUnsignedTxActivity.this).show();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        amountCalculatorLink.setListener(amountsListener);
        amountCalculatorLink.setExchangeRate(getExchangeRate());
        IntentFilter marketFilter = new IntentFilter(BroadcastUtil.ACTION_MARKET);
        registerReceiver(marketBroadcastReceiver, marketFilter);
    }

    @Override
    protected void onPause() {
        amountCalculatorLink.setListener(null);
        unregisterReceiver(marketBroadcastReceiver);
        super.onPause();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.slide_out_right);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void processIntent() {
        isDonate = false;
        Intent intent = getIntent();
        if (intent.hasExtra(SelectAddressToSendActivity.INTENT_EXTRA_ADDRESS)) {
            String address = intent.getExtras().getString(SelectAddressToSendActivity
                    .INTENT_EXTRA_ADDRESS);
            if (Utils.validBicoinAddress(address)) {
                if (Utils.compareString(address, PrimerjSettings.DONATE_ADDRESS)) {
                    isDonate = true;
                }
                etAddress.setText(address);
                long btc = intent.getExtras().getLong(SelectAddressToSendActivity
                        .INTENT_EXTRA_AMOUNT, 0);
                if (btc > 0) {
                    amountCalculatorLink.setBtcAmount(btc);
                }
                validateValues();
            }
        }
    }

    private void configureDonate() {
        if (isDonate) {
            btnSend.setText(R.string.donate_unsigned_transaction_verb);
            tvAddressLabel.setText(R.string.donate_receiving_address_label);
            etAddress.setEnabled(false);
            etAddress.clearFocus();
            ibtnScan.setVisibility(View.GONE);
        } else {
            btnSend.setText(R.string.address_detail_send);
            tvAddressLabel.setText(R.string.send_coins_fragment_receiving_address_label);
            etAddress.setEnabled(true);
            ibtnScan.setVisibility(View.VISIBLE);
        }
    }
}