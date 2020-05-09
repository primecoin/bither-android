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

package net.bither.fragment.cold;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import net.bither.PrimerApplication;
import net.bither.PrimerSetting;
import net.bither.activity.cold.ColdActivity;
import net.bither.activity.cold.ColdAdvanceActivity;
import net.bither.activity.cold.HdmImportWordListActivity;
import net.bither.activity.cold.SignTxActivity;
import net.bither.factory.ImportHDSeedAndroid;
import net.bither.factory.ImportPrivateKeyAndroid;
import net.bither.fragment.Refreshable;
import net.bither.fragment.Selectable;
import net.bither.qrcode.PrimerQRCodeActivity;
import net.bither.qrcode.ScanActivity;
import net.bither.qrcode.ScanQRCodeTransportActivity;
import net.bither.ui.base.DropdownMessage;
import net.bither.ui.base.SettingSelectorView;
import net.bither.ui.base.dialog.DialogConfirmTask;
import net.bither.ui.base.dialog.DialogImportPrivateKeyText;
import net.bither.ui.base.dialog.DialogPassword;
import net.bither.ui.base.dialog.DialogProgress;
import net.bither.ui.base.listener.ICheckPasswordListener;
import net.bither.ui.base.listener.IDialogPasswordListener;
import net.bither.util.AnimationUtil;
import net.bither.util.BackupUtil;
import net.bither.util.DateTimeUtil;
import net.bither.util.FileUtil;
import net.bither.util.KeyUtil;
import net.bither.util.LogUtil;
import net.bither.util.PermissionUtil;
import net.bither.util.ThreadUtil;
import net.bither.util.UnitUtilWrapper;

import net.bither.PrimerApplication;
import net.bither.PrimerSetting;
import net.bither.R;
import net.bither.activity.cold.ColdActivity;
import net.bither.activity.cold.ColdAdvanceActivity;
import net.bither.activity.cold.HdmImportWordListActivity;
import net.bither.activity.cold.SignTxActivity;
import net.bither.bitherj.core.Address;
import net.bither.bitherj.core.AddressManager;
import net.bither.bitherj.core.EnterpriseHDMSeed;
import net.bither.bitherj.core.HDAccountCold;
import net.bither.bitherj.core.HDMKeychain;
import net.bither.bitherj.crypto.ECKey;
import net.bither.bitherj.crypto.EncryptedData;
import net.bither.bitherj.crypto.SecureCharSequence;
import net.bither.bitherj.crypto.mnemonic.MnemonicCode;
import net.bither.bitherj.crypto.mnemonic.MnemonicWordList;
import net.bither.bitherj.factory.ImportHDSeed;
import net.bither.bitherj.factory.ImportPrivateKey;
import net.bither.bitherj.qrcode.QRCodeEnodeUtil;
import net.bither.bitherj.qrcode.QRCodeUtil;
import net.bither.bitherj.utils.PrivateKeyUtil;
import net.bither.bitherj.utils.Utils;
import net.bither.factory.ImportHDSeedAndroid;
import net.bither.factory.ImportPrivateKeyAndroid;
import net.bither.fragment.Refreshable;
import net.bither.fragment.Selectable;
import net.bither.mnemonic.MnemonicCodeAndroid;
import net.bither.preference.AppSharedPreference;
import net.bither.qrcode.PrimerQRCodeActivity;
import net.bither.qrcode.ScanActivity;
import net.bither.qrcode.ScanQRCodeTransportActivity;
import net.bither.ui.base.DropdownMessage;
import net.bither.ui.base.SettingSelectorView;
import net.bither.ui.base.dialog.DialogConfirmTask;
import net.bither.ui.base.dialog.DialogImportPrivateKeyText;
import net.bither.ui.base.dialog.DialogPassword;
import net.bither.ui.base.dialog.DialogProgress;
import net.bither.ui.base.listener.ICheckPasswordListener;
import net.bither.ui.base.listener.IDialogPasswordListener;
import net.bither.util.AnimationUtil;
import net.bither.util.BackupUtil;
import net.bither.util.BackupUtil.BackupListener;
import net.bither.util.DateTimeUtil;
import net.bither.util.FileUtil;
import net.bither.util.KeyUtil;
import net.bither.util.LogUtil;
import net.bither.util.ThreadUtil;
import net.bither.util.UnitUtilWrapper;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import static net.bither.ui.base.dialog.DialogImportPrivateKeyText.ScanPrivateKeyQRCodeRequestCode;

public class OptionColdFragment extends Fragment implements Selectable {
    private int ONE_HOUR = 1 * 60 * 60 * 1000;
    private final int duration = 1000;

    private Button btnGetSign;
    private Button btnCloneTo;
    private Button btnCloneFrom;
    private TextView tvBackupTime;
    private TextView tvBackupPath;
    private Button btnAdvance;
    private SettingSelectorView ssvBitcoinUnit;
    private FrameLayout flBackTime;
    private ProgressBar pbBackTime;
    private TextView tvVersion;
    private LinearLayout llQrForAll;
    private DialogProgress dp;
    private TextView tvPrivacyPolicy;
    private SettingSelectorView ssvImportPrivateKey;
    private ColdActivity coldActivity;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        coldActivity= (ColdActivity) activity;
    }

    private SettingSelectorView.SettingSelector bitcoinUnitSelector = new SettingSelectorView
            .SettingSelector() {
        @Override
        public int getOptionCount() {
            return UnitUtilWrapper.PrimecoinUnitWrapper.values().length;
        }

        @Override
        public CharSequence getOptionName(int index) {
            UnitUtilWrapper.PrimecoinUnitWrapper unit = UnitUtilWrapper.PrimecoinUnitWrapper.values()
                    [index];
            SpannableString s = new SpannableString("  " + unit.name());
            Bitmap bmp = UnitUtilWrapper.getBtcSlimSymbol(getResources().getColor(R.color
                    .text_field_text_color), getResources().getDisplayMetrics().scaledDensity *
                    15.6f, unit);
            s.setSpan(new ImageSpan(getActivity(), bmp, ImageSpan.ALIGN_BASELINE), 0, 1,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            return s;
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
            return getString(R.string.setting_name_bitcoin_unit);
        }

        @Override
        public int getCurrentOptionIndex() {
            return AppSharedPreference.getInstance().getBitcoinUnit().ordinal();
        }

        @Override
        public void onOptionIndexSelected(int index) {
            AppSharedPreference.getInstance().setBitcoinUnit(UnitUtilWrapper.PrimecoinUnitWrapper
                    .values()[index]);
        }
    };

    private OnClickListener toSignActivityClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if ((AddressManager.getInstance().getPrivKeyAddresses() == null
                        || AddressManager.getInstance().getPrivKeyAddresses().size() == 0)
                    && !AddressManager.getInstance().hasHDMKeychain()
                    && !EnterpriseHDMSeed.hasSeed()
                    && !AddressManager.getInstance().hasHDAccountCold()) {
                DropdownMessage.showDropdownMessage(getActivity(), R.string.private_key_is_empty);
                return;
            }
            Intent intent = new Intent(getActivity(), SignTxActivity.class);
            startActivity(intent);
        }
    };
    private OnClickListener cloneToClick = new OnClickListener() {

        @Override
        public void onClick(View v) {
            new DialogPassword(getActivity(), new IDialogPasswordListener() {
                @Override
                public void onPasswordEntered(SecureCharSequence password) {
                    password.wipe();
                    String content = PrivateKeyUtil.getEncryptPrivateKeyStringFromAllAddresses();
                    Intent intent = new Intent(getActivity(), PrimerQRCodeActivity.class);
                    intent.putExtra(PrimerSetting.INTENT_REF.TITLE_STRING,
                            getString(R.string.clone_to_title));
                    intent.putExtra(PrimerSetting.INTENT_REF.QR_CODE_STRING, content);
                    startActivity(intent);
                }
            }).show();
        }
    };
    private OnClickListener cloneFromClick = new OnClickListener() {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getActivity(), ScanQRCodeTransportActivity.class);
            intent.putExtra(PrimerSetting.INTENT_REF.TITLE_STRING,
                    getString(R.string.clone_from_title));
            startActivityForResult(intent, PrimerSetting.INTENT_REF.CLONE_FROM_REQUEST_CODE);
        }

        ;
    };
    private OnClickListener qrForAllClick = new OnClickListener() {

        @Override
        public void onClick(View v) {
            String content = QRCodeEnodeUtil.getPublicKeyStrOfPrivateKey();
            Intent intent = new Intent(getActivity(), PrimerQRCodeActivity.class);
            intent.putExtra(PrimerSetting.INTENT_REF.QR_CODE_STRING, content);
            intent.putExtra(PrimerSetting.INTENT_REF.TITLE_STRING,
                    getString(R.string.qr_code_for_all_addresses_title));
            startActivity(intent);
        }
    };

    private OnClickListener advanceClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getActivity(), ColdAdvanceActivity.class);
            startActivity(intent);
        }
    };

    private OnClickListener backupTimeListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (FileUtil.existSdCardMounted()) {
                if (!PermissionUtil.isWriteExternalStoragePermission(getActivity(), PrimerSetting.REQUEST_CODE_PERMISSION_WRITE_EXTERNAL_STORAGE)) {
                    return;
                }
                long backupTime = AppSharedPreference.getInstance().getLastBackupkeyTime().getTime();
                if (backupTime + ONE_HOUR < System.currentTimeMillis()) {
                    backupPrivateKey();
                } else {
                    DialogConfirmTask dialogConfirmTask = new DialogConfirmTask(getActivity(),
                            getString(R.string.backup_again), new Runnable() {
                        public void run() {
                            backupPrivateKey();
                        }
                    }
                    );
                    dialogConfirmTask.show();
                }
            }
        }
    };

    private OnClickListener privacyPolicyClick = new OnClickListener() {
        @Override
        public void onClick(View v) {

            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/bither/bither-android/wiki/PrivacyPolicy"))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
                DropdownMessage.showDropdownMessage(getActivity(), R.string.find_browser_error);
            }
        }
    };

    @Override
    public void onSelected() {
        configureCloneButton();
        configureQrForAll();
    }

    private void configureCloneButton() {
        if ((AddressManager.getInstance().getPrivKeyAddresses() != null && AddressManager.getInstance().getPrivKeyAddresses()
                .size() > 0) || AddressManager.getInstance().hasHDMKeychain() || AddressManager.getInstance().hasHDAccountCold()) {
            btnCloneFrom.setVisibility(View.GONE);
            btnCloneTo.setVisibility(View.VISIBLE);
        } else {
            btnCloneFrom.setVisibility(View.VISIBLE);
            btnCloneTo.setVisibility(View.GONE);
        }
    }

    private void configureQrForAll() {
        if (AddressManager.getInstance().getPrivKeyAddresses() != null && AddressManager.getInstance().getPrivKeyAddresses()
                .size() > 0) {
            llQrForAll.setVisibility(View.VISIBLE);
        } else {
            llQrForAll.setVisibility(View.GONE);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            String contentP;
            DialogPassword dialogPassword;
            switch (requestCode) {
                case PrimerSetting.INTENT_REF.CLONE_FROM_REQUEST_CODE:
                    contentP = data.getStringExtra(ScanActivity.INTENT_EXTRA_RESULT);
                    dialogPassword = new DialogPassword(getActivity(),
                            new CloneFromPasswordListenerI(contentP));
                    dialogPassword.setCheckPre(false);
                    dialogPassword.setTitle(R.string.clone_from_password);
                    dialogPassword.show();
                    break;
                    //导入秘钥 TODO
                case PrimerSetting.INTENT_REF.IMPORT_PRIVATE_KEY_REQUEST_CODE:
                    final String content = data.getStringExtra(ScanActivity.INTENT_EXTRA_RESULT);
                    if (content.indexOf(QRCodeUtil.HDM_QR_CODE_FLAG) == 0) {
                        DropdownMessage.showDropdownMessage(getActivity(), R.string.can_not_import_hdm_cold_seed);
                        return;
                    }
                    DialogPassword passwordDialog = new DialogPassword(getActivity(),
                            new ImportPrivateKeyPasswordListenerI(content, false));
                    passwordDialog.setCheckPre(false);
                    passwordDialog.setCheckPasswordListener(new ICheckPasswordListener() {
                        @Override
                        public boolean checkPassword(SecureCharSequence password) {
                            ECKey ecKey = PrivateKeyUtil.getECKeyFromSingleString(content, password);
                            boolean result = ecKey != null;
                            if (ecKey != null) {
                                ecKey.clearPrivateKey();
                            }
                            return result;
                        }
                    });
                    passwordDialog.setTitle(R.string.import_private_key_qr_code_password);
                    passwordDialog.show();
                    break;
                case DialogImportPrivateKeyText.ScanPrivateKeyQRCodeRequestCode:
                    final String priv = data.getStringExtra(ScanActivity.INTENT_EXTRA_RESULT);
                    if (!Utils.validBitcoinPrivateKey(priv)) {
                        DropdownMessage.showDropdownMessage(getActivity(),
                                R.string.import_private_key_text_format_error);
                        break;
                    }
                    new DialogPassword(getActivity(), new IDialogPasswordListener() {
                        @Override
                        public void onPasswordEntered(SecureCharSequence password) {
                            ImportPrivateKeyAndroid importPrivateKey = new ImportPrivateKeyAndroid
                                    (getActivity(), ImportPrivateKey.ImportPrivateKeyType
                                            .Text, dp, priv, password);
                            importPrivateKey.importPrivateKey();
                        }
                    }).show();
                    break;
                case PrimerSetting.INTENT_REF.IMPORT_HDM_COLD_SEED_REQUEST_CODE:
                    final String hdmSeed = data.getStringExtra(ScanActivity.INTENT_EXTRA_RESULT);
                    if (hdmSeed.indexOf(QRCodeUtil.HDM_QR_CODE_FLAG) == 0) {
                        dialogPassword = new DialogPassword(getActivity(),
                                new ImportHDSeedPasswordListener(hdmSeed));
                        dialogPassword.setCheckPre(false);
                        dialogPassword.setCheckPasswordListener(new ICheckPasswordListener() {
                            @Override
                            public boolean checkPassword(SecureCharSequence password) {
                                String keyString = hdmSeed.substring(1);
                                String[] passwordSeeds = QRCodeUtil.splitOfPasswordSeed(keyString);
                                String encreyptString = Utils.joinString(new String[]{passwordSeeds[0], passwordSeeds[1], passwordSeeds[2]}, QRCodeUtil.QR_CODE_SPLIT);
                                EncryptedData encryptedData = new EncryptedData(encreyptString);
                                byte[] result = null;
                                try {
                                    result = encryptedData.decrypt(password);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                return result != null;
                            }
                        });
                        dialogPassword.setTitle(R.string.import_private_key_qr_code_password);
                        dialogPassword.show();
                    } else {
                        DropdownMessage.showDropdownMessage(getActivity()
                                , R.string.import_hdm_cold_seed_format_error);
                    }

                    break;
                case PrimerSetting.INTENT_REF.IMPORT_HD_ACCOUNT_SEED_REQUEST_CODE:
                    final String hdAccountSeed = data.getStringExtra(ScanActivity.INTENT_EXTRA_RESULT);
                    final MnemonicWordList mnemonicWordList = MnemonicWordList.getMnemonicWordListForHdSeed(hdAccountSeed);
                    if (mnemonicWordList != null) {
                        try {
                            MnemonicCode mnemonicCode = new MnemonicCodeAndroid();
                            mnemonicCode.setMnemonicWordList(mnemonicWordList);
                            dialogPassword = new DialogPassword(getActivity(),
                                    new ImportHDAccountPasswordListener(hdAccountSeed, mnemonicCode));
                            dialogPassword.setCheckPre(false);
                            dialogPassword.setCheckPasswordListener(new ICheckPasswordListener() {
                                @Override
                                public boolean checkPassword(SecureCharSequence password) {
                                    String keyString = hdAccountSeed.substring(mnemonicWordList.getHdQrCodeFlag().length());
                                    String[] passwordSeeds = QRCodeUtil.splitOfPasswordSeed(keyString);
                                    String encreyptString = Utils.joinString(new String[]{passwordSeeds[0], passwordSeeds[1], passwordSeeds[2]}, QRCodeUtil.QR_CODE_SPLIT);
                                    EncryptedData encryptedData = new EncryptedData(encreyptString);
                                    byte[] result = null;
                                    try {
                                        result = encryptedData.decrypt(password);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    return result != null;
                                }
                            });
                            dialogPassword.setTitle(R.string.import_private_key_qr_code_password);
                            dialogPassword.show();
                        } catch (IOException e) {
                            e.printStackTrace();
                            DropdownMessage.showDropdownMessage(getActivity(), R.string.import_hd_account_seed_format_error);
                        }
                    } else {
                        DropdownMessage.showDropdownMessage(getActivity(), R.string.import_hd_account_seed_format_error);
                    }
                    break;
                case PrimerSetting.INTENT_REF.IMPORT_ACCOUNT_SEED_FROM_PHRASE_REQUEST_CODE:
                    ssvImportPrivateKey.loadData();
                    break;

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
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
                if (!isFromBip38) {
                    ImportPrivateKeyAndroid importPrivateKey = new ImportPrivateKeyAndroid(getActivity()
                            , ImportPrivateKey.ImportPrivateKeyType.BitherQrcode, dp,
                            content, password);
                    importPrivateKey.importPrivateKey();
                }

            }
        }
    }
    private class ImportHDSeedPasswordListener implements IDialogPasswordListener {
        private String content;


        public ImportHDSeedPasswordListener(String content) {
            this.content = content;
        }

        @Override
        public void onPasswordEntered(SecureCharSequence password) {
            if (dp != null && !dp.isShowing()) {
                dp.setMessage(R.string.import_private_key_qr_code_importing);
                ImportHDSeedAndroid importHDSeedAndroid = new ImportHDSeedAndroid
                        (getActivity(), dp, content, password);
                importHDSeedAndroid.importHDMColdSeed();
            }
        }
    }
    private class ImportHDAccountPasswordListener implements IDialogPasswordListener {
        private String content;
        private MnemonicCode mnemonicCode;

        public ImportHDAccountPasswordListener(String content, MnemonicCode mnemonicCode) {
            this.content = content;
            this.mnemonicCode = mnemonicCode;
        }

        @Override
        public void onPasswordEntered(SecureCharSequence password) {
            if (dp != null && !dp.isShowing()) {
                dp.setMessage(R.string.import_private_key_qr_code_importing);
                LogUtil.d("importhdseed", "onPasswordEntered");
                ImportHDSeedAndroid importHDSeedAndroid = new ImportHDSeedAndroid
                        (getActivity(), ImportHDSeed.ImportHDSeedType.HDSeedQRCode, dp, content, null, password, mnemonicCode);
                importHDSeedAndroid.importHDSeed();
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cold_option, container, false);
        initView(view);
        coldActivity.setShowImportSuccessListener(new ColdActivity.ShowImportSuccessListener() {
            @Override
            public void showImportSuccess() {
                ssvImportPrivateKey.loadData();
                DropdownMessage.showDropdownMessage(getActivity(),
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

                                if (PrimerApplication.coldActivity != null) {
                                    ThreadUtil.getMainThreadHandler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            PrimerApplication.coldActivity.scrollToFragmentAt(1);
                                        }
                                    }, 500);
                                }
                            }
                        });
            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        configureCloneButton();
        configureQrForAll();
        showBackupTime();
    }

    private void initView(View view) {
        btnGetSign = (Button) view.findViewById(R.id.btn_get_sign);
        btnCloneTo = (Button) view.findViewById(R.id.btn_clone_to);
        btnCloneFrom = (Button) view.findViewById(R.id.btn_clone_from);
        btnAdvance = (Button) view.findViewById(R.id.btn_advance);
        ssvBitcoinUnit = (SettingSelectorView) view.findViewById(R.id.ssv_bitcoin_unit);
        llQrForAll = (LinearLayout) view.findViewById(R.id.ll_qr_all_keys);
        tvVersion = (TextView) view.findViewById(R.id.tv_version);
        flBackTime = (FrameLayout) view.findViewById(R.id.ll_back_up);
        pbBackTime = (ProgressBar) view.findViewById(R.id.pb_back_up);
        setPbBackTimeSize();
        String version = null;
        try {
            version = getActivity().getPackageManager().getPackageInfo(getActivity()
                    .getPackageName(), 0).versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        if (version != null) {
            tvVersion.setText(version);
            tvVersion.setVisibility(View.VISIBLE);
        } else {
            tvVersion.setVisibility(View.GONE);
        }
        tvPrivacyPolicy = (TextView) view.findViewById(R.id.tv_privacy_policy);
        tvPrivacyPolicy.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        dp = new DialogProgress(getActivity(), R.string.please_wait);
        btnGetSign.setOnClickListener(toSignActivityClickListener);
        btnCloneTo.setOnClickListener(cloneToClick);
        btnCloneFrom.setOnClickListener(cloneFromClick);
        llQrForAll.setOnClickListener(qrForAllClick);
        btnAdvance.setOnClickListener(advanceClick);
        ssvBitcoinUnit.setSelector(bitcoinUnitSelector);
        tvBackupTime = (TextView) view.findViewById(R.id.tv_backup_time);
        tvBackupPath = (TextView) view.findViewById(R.id.tv_backup_path);
        flBackTime.setOnClickListener(backupTimeListener);
        showBackupTime();
        tvPrivacyPolicy.setOnClickListener(privacyPolicyClick);
        ssvImportPrivateKey = (SettingSelectorView) view.findViewById(R.id.ssv_import_private_key);
        ssvImportPrivateKey.setSelector(importPrivateKeySelector);
    }
    //导入私钥
    private SettingSelectorView.SettingSelector importPrivateKeySelector = new
            SettingSelectorView.SettingSelector() {
                @Override
                public int getOptionCount() {
                    int count = 2;
                   /* if (!AddressManager.getInstance().hasHDMKeychain()) {
                        count += 2;
                    }*/
                    /*if (!AddressManager.getInstance().hasHDAccountCold()) {
                        count += 2;
                    }*/
                    return count;
                }

                @Override
                public String getOptionName(int index) {
                    int resource = getStringResouceForIndex(index);
                    if (resource != 0) {
                        return getString(resource);
                    }
                    return "";
                }

                @Override
                public String getOptionNote(int index) {
                    return null;
                }

                @Override
                public Drawable getOptionDrawable(int index) {
                    switch (index) {
                        case 0:
                        case 2:
                        case 4:
                            return getResources().getDrawable(R.drawable.scan_button_icon);
                        case 1:
                        case 3:
                        case 5:
                            return getResources().getDrawable(R.drawable.import_private_key_text_icon);
                        default:
                            return null;
                    }
                }

                @Override
                public String getSettingName() {
                    return getString(R.string.setting_name_import_private_key);
                }

                @Override
                public int getCurrentOptionIndex() {
                    return -1;
                }

                @Override
                public void onOptionIndexSelected(int index) {
                    switch (getStringResouceForIndex(index)) {
                        case R.string.import_private_key_qr_code:
                            importPrivateKeyFromQRCode();
                            return;
                        case R.string.import_private_key_text:
                            importPrivateKeyFromText();
                            return;
                        case R.string.import_hdm_cold_seed_qr_code:
                            importHDMColdFromQRCode();
                            return;
                        case R.string.import_hdm_cold_seed_phrase:
                            importHDMColdFromPhrase();
                            return;
                        case R.string.import_cold_hd_account_seed_qr_code:
                            importHDFromQRCode();
                            return;
                        case R.string.import_cold_hd_account_seed_phrase:
                            importHDFromPhrase();
                            return;
                        default:
                            return;
                    }
                }

                private int getStringResouceForIndex(int index) {
                    switch (index) {
                        case 0:
                            return R.string.import_private_key_qr_code;
                        case 1:
                            return R.string.import_private_key_text;
                    }
                   /* if (!AddressManager.getInstance().hasHDMKeychain()) {
                        switch (index) {
                            case 2:
                                return R.string.import_hdm_cold_seed_qr_code;
                            case 3:
                                return R.string.import_hdm_cold_seed_phrase;
                        }
                        index -= 2;
                    }*/
                    if (!AddressManager.getInstance().hasHDAccountCold()) {
                        switch (index) {
                            case 2:
                                return R.string.import_cold_hd_account_seed_qr_code;
                            case 3:
                                return R.string.import_cold_hd_account_seed_phrase;
                        }
                    }
                    return 0;
                }
            };
    private void importHDMColdFromQRCode() {
        Intent intent = new Intent(getActivity(), ScanQRCodeTransportActivity.class);
        intent.putExtra(PrimerSetting.INTENT_REF.TITLE_STRING,
                getString(R.string.import_hdm_cold_seed_qr_code));
        startActivityForResult(intent, PrimerSetting.INTENT_REF
                .IMPORT_HDM_COLD_SEED_REQUEST_CODE);

    }

    private void importHDMColdFromPhrase() {
        Intent intent = new Intent(getActivity(), HdmImportWordListActivity.class);
        intent.putExtra(PrimerSetting.INTENT_REF.IMPORT_HDM_SEED_TYPE, ImportHDSeed
                .ImportHDSeedType.HDMColdPhrase);
        startActivityForResult(intent, PrimerSetting.INTENT_REF.IMPORT_ACCOUNT_SEED_FROM_PHRASE_REQUEST_CODE);
    }

    private void importHDFromQRCode() {
        Intent intent = new Intent(getActivity(), ScanQRCodeTransportActivity.class);
        intent.putExtra(PrimerSetting.INTENT_REF.TITLE_STRING, getString(R.string
                .import_cold_hd_account_seed_qr_code));
        startActivityForResult(intent, PrimerSetting.INTENT_REF
                .IMPORT_HD_ACCOUNT_SEED_REQUEST_CODE);

    }

    private void importHDFromPhrase() {
        Intent intent = new Intent(getActivity(), HdmImportWordListActivity.class);
        intent.putExtra(PrimerSetting.INTENT_REF.IMPORT_HD_SEED_TYPE, ImportHDSeed
                .ImportHDSeedType.HDSeedPhrase);
        startActivityForResult(intent, PrimerSetting.INTENT_REF.IMPORT_ACCOUNT_SEED_FROM_PHRASE_REQUEST_CODE);
    }

    private void importPrivateKeyFromQRCode() {
        Intent intent = new Intent(getActivity(), ScanQRCodeTransportActivity.class);
        intent.putExtra(PrimerSetting.INTENT_REF.TITLE_STRING,
                getString(R.string.import_private_key_qr_code_scan_title));
        startActivityForResult(intent, PrimerSetting.INTENT_REF.IMPORT_PRIVATE_KEY_REQUEST_CODE);
    }

    private void importPrivateKeyFromText() {
//        new DialogImportPrivateKeyText(getActivity()).show();
        DialogImportPrivateKeyText dialog= new DialogImportPrivateKeyText(getActivity());
        dialog.show();
        dialog.setOnBtnScanClickListener(new DialogImportPrivateKeyText.OnBtnScanClickListener() {
            @Override
            public void onBtnScanClick() {
                startActivityForResult(new Intent(getActivity(), ScanActivity.class),
                        DialogImportPrivateKeyText.ScanPrivateKeyQRCodeRequestCode);
            }
        });
    }


    private void showBackupTime() {
        if (FileUtil.existSdCardMounted()) {
            Date date = AppSharedPreference.getInstance().getLastBackupkeyTime();
            if (date == null) {
                flBackTime.setVisibility(View.GONE);
            } else {
                flBackTime.setVisibility(View.VISIBLE);
                final List<File> files = FileUtil.getBackupFileListOfCold();
                if (files != null && files.size() > 0) {
                    String relativeDate = DateTimeUtil.getRelativeDate(getActivity(), date).toString();
                    tvBackupTime.setText(Utils.format(getString(R.string.last_time_of_back_up) + " ", relativeDate));
                } else {
                    tvBackupTime.setText(R.string.no_backup);
                }
                tvBackupPath.setText(FileUtil.getBackupSdCardDir().getAbsolutePath());
            }
        } else {
            flBackTime.setVisibility(View.VISIBLE);
            tvBackupTime.setText(R.string.no_sd_card_of_back_up);
            tvBackupPath.setVisibility(View.GONE);
        }
    }

    private void setPbBackTimeSize() {
        Drawable drawable = btnGetSign.getCompoundDrawables()[2];
        int w = drawable.getIntrinsicWidth();
        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) pbBackTime
                .getLayoutParams();
        layoutParams.width = w;
        layoutParams.height = w;
        pbBackTime.setLayoutParams(layoutParams);
    }

    private void backupFinish() {
        pbBackTime.setVisibility(View.INVISIBLE);
        final List<File> files = FileUtil.getBackupFileListOfCold();
        if (files != null && files.size() > 0) {
            tvBackupTime.setText(R.string.backup_finish);
        } else {
            tvBackupTime.setText(R.string.backup_failed);
        }
        AnimationUtil.fadeOut(tvBackupTime, new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                fadeinBackupTime();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        }, duration);

    }

    private void fadeinBackupTime() {
        AnimationUtil.fadeIn(tvBackupTime, new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                tvBackupTime.setVisibility(View.INVISIBLE);
                showBackupTime();
                AnimationUtil.fadeOut(tvBackupTime);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        }, duration);


    }

    private void backupPrivateKey() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pbBackTime.setVisibility(View.VISIBLE);
            }
        });
        BackupUtil.backupColdKey(false, new BackupUtil.BackupListener() {

            @Override
            public void backupSuccess() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        backupFinish();

                    }
                }, 1000);
            }

            @Override
            public void backupError() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pbBackTime.setVisibility(View.INVISIBLE);
                        showBackupTime();
                    }
                }, 1000);

            }
        });
    }

    private class CloneFromPasswordListenerI implements IDialogPasswordListener {
        private String content;

        public CloneFromPasswordListenerI(String content) {
            this.content = content;
        }

        @Override
        public void onPasswordEntered(SecureCharSequence password) {
            if (dp != null && !dp.isShowing()) {
                dp.setMessage(R.string.clone_from_waiting);
                CloneThread cloneThread = new CloneThread(content, password);
                dp.setThread(cloneThread);
                dp.show();
                cloneThread.start();
            }
        }
    }

    private class CloneThread extends Thread {
        private String content;
        private SecureCharSequence password;

        public CloneThread(String content, SecureCharSequence password) {
            this.content = content;
            this.password = password;
        }

        public void run() {
            List<Address> addressList = PrivateKeyUtil.getECKeysFromBackupString(content, password);
            HDMKeychain hdmKeychain = PrivateKeyUtil.getHDMKeychain(content, password);
            HDAccountCold hdAccountCold = null;
            MnemonicWordList mnemonicWordList = MnemonicWordList.getMnemonicWordListForHdSeed(content);
            MnemonicCode mnemonicCode = null;
            if (mnemonicWordList != null) {
                try {
                    mnemonicCode = new MnemonicCodeAndroid();
                    mnemonicCode.setMnemonicWordList(mnemonicWordList);
                    hdAccountCold = PrivateKeyUtil.getHDAccountCold(mnemonicCode, content, password);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if ((addressList == null || addressList.size() == 0) && (hdmKeychain == null) &&
                    hdAccountCold == null) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (dp != null && dp.isShowing()) {
                            dp.setThread(null);
                            dp.dismiss();
                        }
                        DropdownMessage.showDropdownMessage(getActivity(),
                                R.string.clone_from_failed);
                    }
                });
                return;
            }

            KeyUtil.addAddressListByDesc(null, addressList);
            if (hdmKeychain != null) {
                KeyUtil.setHDKeyChain(hdmKeychain);
            }
            password.wipe();
            if (mnemonicCode != null && mnemonicWordList != null) {
                AppSharedPreference.getInstance().setMnemonicWordList(mnemonicWordList);
                MnemonicCode.setInstance(mnemonicCode);
            }

            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    configureCloneButton();
                    configureQrForAll();
                    if (dp != null && dp.isShowing()) {
                        dp.setThread(null);
                        dp.dismiss();
                    }
                    DropdownMessage.showDropdownMessage(getActivity(), R.string.clone_from_success);
                    if (getActivity() instanceof ColdActivity) {
                        ColdActivity activity = (ColdActivity) getActivity();
                        Fragment f = activity.getFragmentAtIndex(1);
                        if (f != null && f instanceof Refreshable) {
                            Refreshable r = (Refreshable) f;
                            r.doRefresh();
                        }
                        activity.scrollToFragmentAt(1);
                    }
                }
            });
        }
    }
}
