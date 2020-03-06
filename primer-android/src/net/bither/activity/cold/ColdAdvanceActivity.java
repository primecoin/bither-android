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

package net.bither.activity.cold;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.bither.PrimerApplication;
import net.bither.PrimerSetting;
import net.bither.TrashCanActivity;
import net.bither.VerifyMessageSignatureActivity;
import net.bither.factory.ImportPrivateKeyAndroid;
import net.bither.pin.PinCodeChangeActivity;
import net.bither.pin.PinCodeDisableActivity;
import net.bither.pin.PinCodeEnableActivity;
import net.bither.qrcode.ScanActivity;
import net.bither.qrcode.ScanQRCodeTransportActivity;
import net.bither.qrcode.ScanQRCodeWithOtherActivity;
import net.bither.ui.base.DropdownMessage;
import net.bither.ui.base.SettingSelectorView;
import net.bither.ui.base.SwipeRightFragmentActivity;
import net.bither.ui.base.dialog.DialogConfirmTask;
import net.bither.ui.base.dialog.DialogEditPassword;
import net.bither.ui.base.dialog.DialogEnterpriseHDMEnable;
import net.bither.ui.base.dialog.DialogImportBip38KeyText;
import net.bither.ui.base.dialog.DialogPassword;
import net.bither.ui.base.dialog.DialogPasswordWithOther;
import net.bither.ui.base.dialog.DialogProgress;
import net.bither.ui.base.dialog.DialogSignHashSelectType;
import net.bither.ui.base.dialog.DialogSignMessageSelectType;
import net.bither.ui.base.listener.IBackClickListener;
import net.bither.ui.base.listener.ICheckPasswordListener;
import net.bither.ui.base.listener.IDialogPasswordListener;
import net.bither.util.FileUtil;
import net.bither.util.ThreadUtil;

import net.bither.*;
import net.bither.PrimerSetting;
import net.bither.bitherj.PrimerjSettings;
import net.bither.bitherj.core.Version;
import net.bither.bitherj.crypto.SecureCharSequence;
import net.bither.bitherj.crypto.bip38.Bip38;
import net.bither.bitherj.factory.ImportPrivateKey;
import net.bither.bitherj.qrcode.QRCodeUtil;
import net.bither.bitherj.utils.Utils;
import net.bither.factory.ImportPrivateKeyAndroid;
import net.bither.fragment.Refreshable;
import net.bither.pin.PinCodeChangeActivity;
import net.bither.pin.PinCodeDisableActivity;
import net.bither.pin.PinCodeEnableActivity;
import net.bither.preference.AppSharedPreference;
import net.bither.qrcode.ScanActivity;
import net.bither.qrcode.ScanQRCodeTransportActivity;
import net.bither.qrcode.ScanQRCodeWithOtherActivity;
import net.bither.rawprivatekey.RawPrivateKeyActivity;
import net.bither.ui.base.DropdownMessage;
import net.bither.ui.base.SettingSelectorView;
import net.bither.ui.base.SwipeRightFragmentActivity;
import net.bither.ui.base.dialog.DialogConfirmTask;
import net.bither.ui.base.dialog.DialogEditPassword;
import net.bither.ui.base.dialog.DialogEnterpriseHDMEnable;
import net.bither.ui.base.dialog.DialogImportBip38KeyText;
import net.bither.ui.base.dialog.DialogPassword;
import net.bither.ui.base.dialog.DialogPasswordWithOther;
import net.bither.ui.base.dialog.DialogProgress;
import net.bither.ui.base.dialog.DialogSignHashSelectType;
import net.bither.ui.base.dialog.DialogSignMessageSelectType;
import net.bither.ui.base.listener.IBackClickListener;
import net.bither.ui.base.listener.ICheckPasswordListener;
import net.bither.ui.base.listener.IDialogPasswordListener;
import net.bither.util.FileUtil;
import net.bither.util.ThreadUtil;

import java.io.File;


public class ColdAdvanceActivity extends SwipeRightFragmentActivity {
    private SettingSelectorView ssvPinCode;
    private Button btnEditPassword;
    private SettingSelectorView ssvImprotBip38Key;
    private SettingSelectorView ssvQrCodeQuality;
    private SettingSelectorView ssvPasswordStrengthCheck;
    private Button btnTrashCan;
    private DialogProgress dp;
    private TextView tvVserion;
    private LinearLayout llSignHash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.slide_in_right, 0);
        setContentView(R.layout.activity_cold_advance_options);
        initView();
    }

    private void initView() {
        findViewById(R.id.ibtn_back).setOnClickListener(new IBackClickListener());
        tvVserion = (TextView) findViewById(R.id.tv_version);
        ssvPinCode = (SettingSelectorView) findViewById(R.id.ssv_pin_code);
        ssvPinCode.setSelector(pinCodeSelector);
        btnEditPassword = (Button) findViewById(R.id.btn_edit_password);
        btnTrashCan = (Button) findViewById(R.id.btn_trash_can);
        ssvImprotBip38Key = (SettingSelectorView) findViewById(R.id.ssv_import_bip38_key);
        ssvImprotBip38Key.setSelector(importBip38KeySelector);
        ssvQrCodeQuality = (SettingSelectorView) findViewById(R.id.ssv_qr_code_quality);
        ssvQrCodeQuality.setSelector(qrCodeQualitySelector);
        btnEditPassword.setOnClickListener(editPasswordClick);
        ssvPasswordStrengthCheck = (SettingSelectorView) findViewById(R.id.ssv_password_strength_check);
        ssvPasswordStrengthCheck.setSelector(passwordStrengthCheckSelector);
        btnTrashCan.setOnClickListener(trashCanClick);
        ((SettingSelectorView) findViewById(R.id.ssv_message_signing)).setSelector
                (messageSigningSelector);
        findViewById(R.id.iv_logo).setOnClickListener(rawPrivateKeyClick);
        tvVserion.setText(Version.name + " " + Version.version);
        dp = new DialogProgress(this, R.string.please_wait);
        llSignHash = (LinearLayout) findViewById(R.id.ll_sign_hash);
        llSignHash.setOnClickListener(signHashClick);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ssvPinCode.loadData();
    }

    private View.OnClickListener signHashClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            new DialogSignHashSelectType(ColdAdvanceActivity.this, false).show();
        }
    };
    private View.OnClickListener rawPrivateKeyClick = new View.OnClickListener() {
        private int clickedTime;

        @Override
        public void onClick(View v) {
            v.removeCallbacks(delay);
            clickedTime++;
            if (clickedTime >= 7) {
                new DialogEnterpriseHDMEnable(ColdAdvanceActivity.this).show();
                clickedTime = 0;
                return;
            }
            v.postDelayed(delay, 400);
        }

        private Runnable delay = new Runnable() {
            @Override
            public void run() {
                if (clickedTime < 7) {
                    startActivity(new Intent(ColdAdvanceActivity.this, RawPrivateKeyActivity
                            .class));
                }
                clickedTime = 0;
            }
        };
    };

    private View.OnClickListener editPasswordClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (PrimerjSettings.DEV_DEBUG) {
                try {
                    final File logTagDir = FileUtil.getDiskDir("log", true);
                    SQLiteDatabase addressDB = PrimerApplication.mAddressDbHelper.getReadableDatabase();
                    FileUtil.copyFile(new File(addressDB.getPath()), new File(logTagDir, "address.db"));

                    SQLiteDatabase txDb = PrimerApplication.mTxDbHelper.getReadableDatabase();
                    FileUtil.copyFile(new File(txDb.getPath()), new File(logTagDir, "tx.db"));
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
            hasAnyAction = true;
            DialogEditPassword dialog = new DialogEditPassword(ColdAdvanceActivity.this);
            dialog.show();
        }
    };


    private View.OnClickListener trashCanClick = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            startActivity(new Intent(ColdAdvanceActivity.this, TrashCanActivity.class));
        }
    };

    private SettingSelectorView.SettingSelector messageSigningSelector = new SettingSelectorView
            .SettingSelector() {


        @Override
        public int getOptionCount() {
            return 2;
        }

        @Override
        public CharSequence getOptionName(int index) {
            switch (index) {
                case 0:
                    return getString(R.string.sign_message_activity_name);
                case 1:
                default:
                    return getString(R.string.verify_message_signature_activity_name);
            }
        }

        @Override
        public CharSequence getOptionNote(int index) {
            return null;
        }

        @Override
        public Drawable getOptionDrawable(int index) {
            return null;
        }

        @Override
        public CharSequence getSettingName() {
            return getString(R.string.sign_message_setting_name);
        }

        @Override
        public int getCurrentOptionIndex() {
            return -1;
        }

        @Override
        public void onOptionIndexSelected(int index) {
            switch (index) {
                case 0:
                    new DialogSignMessageSelectType(ColdAdvanceActivity.this,false).show();
                    break;
                case 1:
                default:
                    startActivity(new Intent(ColdAdvanceActivity.this,
                            VerifyMessageSignatureActivity.class));
                    break;
            }
        }
    };

    private SettingSelectorView.SettingSelector qrCodeQualitySelector = new SettingSelectorView
            .SettingSelector() {

        @Override
        public int getOptionCount() {
            return QRCodeUtil.QRQuality.values().length;
        }

        @Override
        public CharSequence getSettingName() {
            return getString(R.string.qr_code_quality_setting_name);
        }

        @Override
        public int getCurrentOptionIndex() {
            return AppSharedPreference.getInstance().getQRQuality().ordinal();
        }

        @Override
        public CharSequence getOptionName(int index) {
            switch (index) {
                case 1:
                    return getString(R.string.qr_code_quality_setting_low);
                case 0:
                default:
                    return getString(R.string.qr_code_quality_setting_normal);
            }
        }

        @Override
        public void onOptionIndexSelected(int index) {
            if (index >= 0 && index < getOptionCount()) {
                AppSharedPreference.getInstance().setQRQuality(QRCodeUtil.QRQuality.values()
                        [index]);
            }
        }

        @Override
        public CharSequence getOptionNote(int index) {
            return null;
        }

        @Override
        public Drawable getOptionDrawable(int index) {
            return null;
        }
    };

    private SettingSelectorView.SettingSelector pinCodeSelector = new SettingSelectorView
            .SettingSelector() {
        private AppSharedPreference p = AppSharedPreference.getInstance();
        private boolean hasPinCode;

        @Override
        public int getOptionCount() {
            hasPinCode = p.hasPinCode();
            return hasPinCode ? 2 : 1;
        }

        @Override
        public String getOptionName(int index) {
            if (hasPinCode) {
                switch (index) {
                    case 0:
                        return getString(R.string.pin_code_setting_close);
                    case 1:
                        return getString(R.string.pin_code_setting_change);
                }
            }
            return getString(R.string.pin_code_setting_open);
        }

        @Override
        public String getOptionNote(int index) {
            return null;
        }

        @Override
        public Drawable getOptionDrawable(int index) {
            return null;
        }

        @Override
        public String getSettingName() {
            return getString(R.string.pin_code_setting_name);
        }

        @Override
        public int getCurrentOptionIndex() {
            return -1;
        }

        @Override
        public void onOptionIndexSelected(int index) {
            if (hasPinCode) {
                switch (index) {
                    case 0:
                        startActivity(new Intent(ColdAdvanceActivity.this,
                                PinCodeDisableActivity.class));
                        return;
                    case 1:
                        startActivity(new Intent(ColdAdvanceActivity.this,
                                PinCodeChangeActivity.class));
                        return;
                }
            } else {
                startActivity(new Intent(ColdAdvanceActivity.this, PinCodeEnableActivity.class));
            }
        }
    };
    private SettingSelectorView.SettingSelector passwordStrengthCheckSelector = new
            SettingSelectorView.SettingSelector() {

                @Override
                public int getOptionCount() {
                    return 2;
                }

                @Override
                public CharSequence getOptionName(int index) {
                    if (index == 0) {
                        return getString(R.string.password_strength_check_on);
                    }
                    return getString(R.string.password_strength_check_off);
                }

                @Override
                public CharSequence getOptionNote(int index) {
                    return null;
                }

                @Override
                public Drawable getOptionDrawable(int index) {
                    return null;
                }

                @Override
                public CharSequence getSettingName() {
                    return getString(R.string.password_strength_check);
                }

                @Override
                public int getCurrentOptionIndex() {
                    return AppSharedPreference.getInstance().getPasswordStrengthCheck() ? 0 : 1;
                }

                @Override
                public void onOptionIndexSelected(int index) {
                    boolean check = index == 0;
                    if (check) {
                        AppSharedPreference.getInstance().setPasswordStrengthCheck(check);
                    } else {
                        new DialogConfirmTask(ColdAdvanceActivity.this, getString(R.string
                                .password_strength_check_off_warn), new Runnable() {
                            @Override
                            public void run() {
                                ThreadUtil.runOnMainThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        AppSharedPreference.getInstance().setPasswordStrengthCheck(false);
                                        ssvPasswordStrengthCheck.loadData();
                                    }
                                });
                            }
                        }).show();
                    }
                }
            };


    private SettingSelectorView.SettingSelector importBip38KeySelector = new SettingSelectorView
            .SettingSelector() {
        @Override
        public int getOptionCount() {

            return 2;
        }

        @Override
        public String getOptionName(int index) {
            switch (index) {
                case 0:
                    return getString(R.string.import_bip38_key_qr_code);
                case 1:
                    return getString(R.string.import_bip38_key_text);
                default:
                    return "";
            }
        }

        @Override
        public String getOptionNote(int index) {
            return null;
        }

        @Override
        public Drawable getOptionDrawable(int index) {
            switch (index) {
                case 0:
                    return getResources().getDrawable(R.drawable.scan_button_icon);
                case 1:
                    return getResources().getDrawable(R.drawable.import_private_key_text_icon);
                default:
                    return null;
            }
        }

        @Override
        public String getSettingName() {
            return getString(R.string.setting_name_import_bip38_key);
        }

        @Override
        public int getCurrentOptionIndex() {
            return -1;
        }

        @Override
        public void onOptionIndexSelected(int index) {
            switch (index) {
                case 0:
                    importPrivateKeyFromQrCode(true);
                    return;
                case 1:
                    new DialogImportBip38KeyText(ColdAdvanceActivity.this).show();
                    return;
                default:
                    return;
            }
        }
    };

    private void importPrivateKeyFromQrCode(boolean isFromBip38) {
        if (isFromBip38) {
            Intent intent = new Intent(this, ScanQRCodeWithOtherActivity.class);
            intent.putExtra(PrimerSetting.INTENT_REF.TITLE_STRING,
                    getString(R.string.import_bip38private_key_qr_code_scan_title));
            intent.putExtra(PrimerSetting.INTENT_REF.QRCODE_TYPE, PrimerSetting.QRCodeType.Bip38);
            startActivityForResult(intent, PrimerSetting.INTENT_REF
                    .IMPORT_BIP38PRIVATE_KEY_REQUEST_CODE);
        } else {
            Intent intent = new Intent(this, ScanQRCodeTransportActivity.class);
            intent.putExtra(PrimerSetting.INTENT_REF.TITLE_STRING,
                    getString(R.string.import_private_key_qr_code_scan_title));
            startActivityForResult(intent, PrimerSetting.INTENT_REF
                    .IMPORT_PRIVATE_KEY_REQUEST_CODE);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        switch (requestCode) {

            case PrimerSetting.INTENT_REF.IMPORT_BIP38PRIVATE_KEY_REQUEST_CODE:
                final String bip38Content = data.getStringExtra(ScanActivity.INTENT_EXTRA_RESULT);
                DialogPasswordWithOther dialogPasswordWithOther = new DialogPasswordWithOther
                        (this, new ImportPrivateKeyPasswordListenerI(null, true));
                dialogPasswordWithOther.setCheckPre(false);
                dialogPasswordWithOther.setCheckPasswordListener(new ICheckPasswordListener() {
                    @Override
                    public boolean checkPassword(SecureCharSequence password) {
                        try {
                            bip38DecodeString = Bip38.decrypt(bip38Content, password, Utils.getNetType()).toString();
                            return bip38DecodeString != null;
                        } catch (Exception e) {
                            e.printStackTrace();
                            return false;
                        }
                    }
                });
                dialogPasswordWithOther.setTitle(R.string.enter_bip38_key_password);
                dialogPasswordWithOther.show();
                break;

        }
    }





    private String bip38DecodeString;

    private class ImportPrivateKeyPasswordListenerI implements IDialogPasswordListener {
        private String content;
        private boolean isFromBip38;

        public ImportPrivateKeyPasswordListenerI(String content, boolean isFromBip38) {
            this.content = content;
            this.isFromBip38 = isFromBip38;
        }

        @Override
        public void onPasswordEntered(SecureCharSequence password) {
            if (dp != null && !dp.isShowing()) {
                dp.setMessage(R.string.import_private_key_qr_code_importing);
                if (isFromBip38) {
                    DialogPassword dialogPassword = new DialogPassword(ColdAdvanceActivity.this,
                            walletIDialogPasswordListener);
                    dialogPassword.show();
                } else {
                    ImportPrivateKeyAndroid importPrivateKey = new ImportPrivateKeyAndroid(ColdAdvanceActivity
                            .this, ImportPrivateKey.ImportPrivateKeyType.BitherQrcode, dp,
                            content, password);
                    importPrivateKey.importPrivateKey();
                }

            }
        }
    }


    private IDialogPasswordListener walletIDialogPasswordListener = new IDialogPasswordListener() {
        @Override
        public void onPasswordEntered(SecureCharSequence password) {
            ImportPrivateKeyAndroid importPrivateKey = new ImportPrivateKeyAndroid(ColdAdvanceActivity.this,
                    ImportPrivateKey.ImportPrivateKeyType.Bip38, dp, bip38DecodeString, password);
            importPrivateKey.importPrivateKey();
        }
    };

    private boolean hasAnyAction = false;

    public void showImportSuccess() {
        hasAnyAction = false;
        ssvImprotBip38Key.loadData();
        DropdownMessage.showDropdownMessage(ColdAdvanceActivity.this,
                R.string.import_private_key_qr_code_success, new Runnable() {
                    @Override
                    public void run() {
                        if (PrimerApplication.coldActivity != null) {
                            Fragment f = PrimerApplication.coldActivity.getFragmentAtIndex(1);
                            if (f != null && f instanceof Refreshable) {
                                Refreshable r = (Refreshable) f;
                                r.doRefresh();
                            }
                        }
                        if (hasAnyAction) {
                            return;
                        }
                        finish();
                        if (PrimerApplication.coldActivity != null) {
                            ThreadUtil.getMainThreadHandler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    PrimerApplication.coldActivity.scrollToFragmentAt(1);
                                }
                            }, getFinishAnimationDuration());
                        }
                    }
                });
    }


}
