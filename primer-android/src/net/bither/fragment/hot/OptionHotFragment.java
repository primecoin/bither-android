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

package net.bither.fragment.hot;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import net.bither.BuildConfig;
import net.bither.ChooseModeActivity;
import net.bither.PrimerApplication;
import net.bither.PrimerSetting;
import net.bither.activity.cold.HdmImportWordListActivity;
import net.bither.activity.hot.CheckPrivateKeyActivity;
import net.bither.activity.hot.HotActivity;
import net.bither.activity.hot.HotAdvanceActivity;
import net.bither.activity.hot.NetworkMonitorActivity;
import net.bither.bitherj.db.AbstractDb;
import net.bither.factory.ImportHDSeedAndroid;
import net.bither.factory.ImportPrivateKeyAndroid;
import net.bither.fragment.Refreshable;
import net.bither.fragment.Selectable;
import net.bither.image.glcrop.CropImageGlActivity;
import net.bither.model.Market;
import net.bither.qrcode.ScanActivity;
import net.bither.qrcode.ScanQRCodeTransportActivity;
import net.bither.service.BlockchainService;
import net.bither.ui.base.DropdownMessage;
import net.bither.ui.base.SettingSelectorView;
import net.bither.ui.base.dialog.DialogConfirmTask;
import net.bither.ui.base.dialog.DialogDonate;
import net.bither.ui.base.dialog.DialogHDMonitorFirstAddressValidation;
import net.bither.ui.base.dialog.DialogImportPrivateKeyText;
import net.bither.ui.base.dialog.DialogPassword;
import net.bither.ui.base.dialog.DialogProgress;
import net.bither.ui.base.dialog.DialogSetAvatar;
import net.bither.ui.base.listener.ICheckPasswordListener;
import net.bither.ui.base.listener.IDialogPasswordListener;
import net.bither.util.ExchangeUtil;
import net.bither.util.FileUtil;
import net.bither.util.ImageFileUtil;
import net.bither.util.ImageManageUtil;
import net.bither.util.LogUtil;
import net.bither.util.MarketUtil;
import net.bither.util.MonitorPrimerColdUtil;
import net.bither.util.PermissionUtil;
import net.bither.util.ThreadUtil;
import net.bither.util.UIUtil;
import net.bither.util.UnitUtilWrapper;

import net.bither.PrimerApplication;
import net.bither.PrimerSetting;
import net.bither.ChooseModeActivity;
import net.bither.R;
import net.bither.activity.cold.HdmImportWordListActivity;
import net.bither.activity.hot.CheckPrivateKeyActivity;
import net.bither.activity.hot.HotActivity;
import net.bither.activity.hot.HotAdvanceActivity;
import net.bither.activity.hot.NetworkMonitorActivity;
import net.bither.bitherj.AbstractApp;
import net.bither.bitherj.PrimerjSettings;
import net.bither.bitherj.core.AbstractHD;
import net.bither.bitherj.core.AddressManager;
import net.bither.bitherj.core.HDAccount;
import net.bither.bitherj.crypto.ECKey;
import net.bither.bitherj.crypto.EncryptedData;
import net.bither.bitherj.crypto.SecureCharSequence;
import net.bither.bitherj.crypto.hd.DeterministicKey;
import net.bither.bitherj.crypto.mnemonic.MnemonicCode;
import net.bither.bitherj.crypto.mnemonic.MnemonicException;
import net.bither.bitherj.crypto.mnemonic.MnemonicWordList;
import net.bither.bitherj.exception.AddressFormatException;
import net.bither.bitherj.factory.ImportHDSeed;
import net.bither.bitherj.factory.ImportPrivateKey;
import net.bither.bitherj.qrcode.QRCodeUtil;
import net.bither.bitherj.utils.PrivateKeyUtil;
import net.bither.bitherj.utils.Utils;
import net.bither.factory.ImportHDSeedAndroid;
import net.bither.factory.ImportPrivateKeyAndroid;
import net.bither.fragment.Refreshable;
import net.bither.fragment.Selectable;
import net.bither.image.glcrop.CropImageGlActivity;
import net.bither.mnemonic.MnemonicCodeAndroid;
import net.bither.model.Market;
import net.bither.preference.AppSharedPreference;
import net.bither.qrcode.ScanActivity;
import net.bither.qrcode.ScanQRCodeTransportActivity;
import net.bither.runnable.ThreadNeedService;
import net.bither.runnable.UploadAvatarRunnable;
import net.bither.service.BlockchainService;
import net.bither.ui.base.DropdownMessage;
import net.bither.ui.base.SettingSelectorView;
import net.bither.ui.base.SettingSelectorView.SettingSelector;
import net.bither.ui.base.dialog.DialogConfirmTask;
import net.bither.ui.base.dialog.DialogDonate;
import net.bither.ui.base.dialog.DialogHDMonitorFirstAddressValidation;
import net.bither.ui.base.dialog.DialogImportPrivateKeyText;
import net.bither.ui.base.dialog.DialogPassword;
import net.bither.ui.base.dialog.DialogProgress;
import net.bither.ui.base.dialog.DialogSetAvatar;
import net.bither.ui.base.listener.ICheckPasswordListener;
import net.bither.ui.base.listener.IDialogPasswordListener;
import net.bither.util.ExchangeUtil;
import net.bither.util.FileUtil;
import net.bither.util.ImageFileUtil;
import net.bither.util.ImageManageUtil;
import net.bither.util.LogUtil;
import net.bither.util.MarketUtil;
import net.bither.util.MonitorPrimerColdUtil;
import net.bither.util.ThreadUtil;
import net.bither.util.UIUtil;
import net.bither.util.UnitUtilWrapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static net.bither.ui.base.dialog.DialogImportPrivateKeyText.ScanPrivateKeyQRCodeRequestCode;

public class OptionHotFragment extends Fragment implements Selectable,
        DialogSetAvatar.SetAvatarDelegate {
    private static final int MonitorCodeHDRequestCode = 1605;

    private static Uri imageUri;
    private SettingSelectorView ssvCurrency;
    private SettingSelectorView ssvMarket;
    private SettingSelectorView ssvTransactionFee;
    private SettingSelectorView ssvTransactionFeePrecision;
    private Button btnSwitchToCold;
    private Button btnAvatar;
    private Button btnCheck;
    private Button btnAdvance;
    private TextView tvWebsite;
    private TextView tvVersion;
    private ImageView ivLogo;
    private View llSwitchToCold;
    private TextView tvPrivacyPolicy;
    private SettingSelectorView ssvImportPrivateKey;
    private MonitorPrimerColdUtil monitorUtil;
    private HotActivity hotActivity;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        hotActivity= (HotActivity) activity;
    }


    private DialogProgress dp;
    private OnClickListener logoClickListener = new OnClickListener() {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getActivity(), NetworkMonitorActivity.class);
            startActivity(intent);
        }
    };

    /*默认单位选择器*/
    private SettingSelectorView.SettingSelector transactionFeePrecisionSelector = new SettingSelectorView.SettingSelector() {
        @Override
        public void onOptionIndexSelected(int index) {
            AppSharedPreference.getInstance().setTransactionFeePrecision(getModeByIndex(index));
        }

        @Override
        public String getSettingName() {
            return getString(R.string.setting_name_transaction_fee_precision);
        }

        @Override
        public String getOptionName(int index) {
            switch (index) {
                case 0:
                    return "1 XPM";
                case 1:
                    return "0.1 XPM";
                case 2:
                    return "0.01 XPM";
                case 3:
                    return "0.001 XPM";
                default:
                    return "0.01 XPM";
            }
        }

        @Override
        public int getOptionCount() {
            return PrimerjSettings.TransactionFeePrecision.values().length;
        }

        @Override
        public int getCurrentOptionIndex() {
            PrimerjSettings.TransactionFeePrecision mode = AppSharedPreference.getInstance()
                    .getTransactionFeePrecision();
            switch (mode) {
                case P0:
                    return 0;
                case P1:
                    return 1;
                case P2:
                    return 2;
                case P3:
                    return 3;
                default:
                    return 2;
            }
        }

        private PrimerjSettings.TransactionFeePrecision getModeByIndex(int index) {
            if (index >= 0 && index < PrimerjSettings.TransactionFeePrecision.values().length) {
                switch (index) {
                    case 0:
                        return PrimerjSettings.TransactionFeePrecision.P0;
                    case 1:
                        return PrimerjSettings.TransactionFeePrecision.P1;
                    case 2:
                        return PrimerjSettings.TransactionFeePrecision.P2;
                    case 3:
                        return PrimerjSettings.TransactionFeePrecision.P3;
                }
            }
            return PrimerjSettings.TransactionFeePrecision.P2;
        }

        @Override
        public String getOptionNote(int index) {
            return "";
        }

        @Override
        public Drawable getOptionDrawable(int index) {
            return null;
        }
    };
    /*默认货币选择器 （默认值为当前国家货币）*/
    private SettingSelectorView.SettingSelector currencySelector = new SettingSelectorView.SettingSelector() {
        private int length = ExchangeUtil.Currency.values().length;

        @Override
        public int getOptionCount() {
            return length;
        }

        @Override
        public void onOptionIndexSelected(int index) {
            if (index >= 0 && index < length) {
                AppSharedPreference.getInstance().setExchangeType(ExchangeUtil.Currency.values()[index]);
            }
        }

        @Override
        public String getSettingName() {
            return getString(R.string.setting_name_currency);
        }

        @Override
        public String getOptionName(int index) {
            if (index >= 0 && index < length) {
                return ExchangeUtil.Currency.values()[index].getSymbol() + " " + ExchangeUtil
                        .Currency.values()[index].getName();
            }
            return ExchangeUtil.Currency.values()[0].getSymbol() + " " + ExchangeUtil.Currency
                    .values()[0].getName();
        }


        @Override
        public int getCurrentOptionIndex() {
            return AppSharedPreference.getInstance().getDefaultExchangeType().ordinal();
        }

        @Override
        public String getOptionNote(int index) {
            return null;
        }

        @Override
        public Drawable getOptionDrawable(int index) {
            return null;
        }
    };
    /*默认交易所*/
    private SettingSelectorView.SettingSelector marketSelector = new SettingSelectorView.SettingSelector() {
        private List<Market> markets = MarketUtil.getMarkets();

        @Override
        public void onOptionIndexSelected(int index) {
            AppSharedPreference.getInstance().setMarketType(markets.get(index).getMarketType());
        }

        @Override
        public String getSettingName() {
            return getString(R.string.setting_name_market);
        }

        @Override
        public String getOptionName(int index) {
            return markets.get(index).getName();
        }

        @Override
        public int getOptionCount() {
            return markets.size();
        }

        @Override
        public int getCurrentOptionIndex() {
            return markets.indexOf(MarketUtil.getDefaultMarket());
        }

        @Override
        public String getOptionNote(int index) {
            return null;
        }

        @Override
        public Drawable getOptionDrawable(int index) {
            return null;
        }
    };
    /*默认手续费*/
    private SettingSelectorView.SettingSelector transactionFeeModeSelector = new SettingSelectorView.SettingSelector() {

        @Override
        public void onOptionIndexSelected(int index) {
            // This warning is no longer needed. As more and more mining pool upgrade their
            // bitcoin client to 0.9.+, low fee transactions get confirmed soon enough.
//            if (index == TransactionFeeMode.Low.ordinal()) {
//
//                DialogConfirmTask dialog = new DialogConfirmTask(getActivity(),
//                        getString(R.string.setting_name_transaction_fee_low_warn),
//new Runnable() {
//                    @Override
//                    public void run() {
//                        ssvTransactionFee.post(new Runnable() {
//                            @Override
//                            public void run() {
//                                AppSharedPreference.getInstance().setTransactionFeeMode
//                                        (TransactionFeeMode.Low);
//                                ssvTransactionFee.loadData();
//                            }
//                        });
//                    }
//                }
//                );
//                dialog.show();
//            }
            AppSharedPreference.getInstance().setTransactionFeeMode(getModeByIndex(index));
        }

        @Override
        public String getSettingName() {
            return getString(R.string.setting_name_transaction_fee);
        }

        @Override
        public String getOptionName(int index) {
            PrimerjSettings.TransactionFeeMode transactionFeeMode = getModeByIndex(index);
            switch (transactionFeeMode) {
                case Higher:
                    return getString(R.string.setting_name_transaction_fee_higher);
                case High:
                    return getString(R.string.setting_name_transaction_fee_high);
                default:
                    return getString(R.string.setting_name_transaction_fee_normal);
            }
        }

        @Override
        public int getOptionCount() {
            return PrimerjSettings.TransactionFeeMode.values().length;
        }

        @Override
        public int getCurrentOptionIndex() {
            PrimerjSettings.TransactionFeeMode mode = AppSharedPreference.getInstance()
                    .getTransactionFeeMode();
            switch (mode) {
                case High:
                    return 1;
                case Higher:
                    return 2;
                default:
                    return 0;
            }
        }

        private PrimerjSettings.TransactionFeeMode getModeByIndex(int index) {
            if (index >= 0 && index < PrimerjSettings.TransactionFeeMode.values().length) {
                switch (index) {
                    case 0:
                        return PrimerjSettings.TransactionFeeMode.Normal;
                    case 1:
                        return PrimerjSettings.TransactionFeeMode.High;
                    case 2:
                        return PrimerjSettings.TransactionFeeMode.Higher;
                }
            }
            return PrimerjSettings.TransactionFeeMode.Normal;
        }

        @Override
        public String getOptionNote(int index) {
            switch (getModeByIndex(index)) {
                case Higher:
                    return getFeeStr(PrimerjSettings.TransactionFeeMode.Higher);
                case High:
                    return getFeeStr(PrimerjSettings.TransactionFeeMode.High);
                default:
                    return getFeeStr(PrimerjSettings.TransactionFeeMode.Normal);
            }
        }

        private String getFeeStr(PrimerjSettings.TransactionFeeMode transactionFeeMode) {
            String unit = " mXPM/B";
            // Unit conversion of fee rate from Satoshi/kB to mXPM/B
            float fee = (float) transactionFeeMode.getFeeRateSatoshiPerKB() / 1000 / 100000;
            return String.valueOf(fee) + unit;
        }

        @Override
        public Drawable getOptionDrawable(int index) {
            return null;
        }
    };

    private OnClickListener switchToColdClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            DialogConfirmTask dialog = new DialogConfirmTask(getActivity(),
                    getStyledConfirmString(getString(R.string
                            .launch_sequence_switch_to_cold_warn)), new Runnable() {
                @Override
                public void run() {
                    ThreadUtil.runOnMainThread(new Runnable() {
                        @Override
                        public void run() {
                            AppSharedPreference.getInstance().setAppMode(PrimerjSettings.AppMode
                                    .COLD);
                            startActivity(new Intent(getActivity(), ChooseModeActivity.class));
                            getActivity().overridePendingTransition(R.anim.activity_in_drop, 0);
                            getActivity().finish();
                        }
                    });
                }
            });
            dialog.show();
        }

        private SpannableString getStyledConfirmString(String str) {
            int firstLineEnd = str.indexOf("\n");
            SpannableString spn = new SpannableString(str);
            spn.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.red)), 0,
                    firstLineEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spn.setSpan(new StyleSpan(Typeface.BOLD), 0, firstLineEnd,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            spn.setSpan(new RelativeSizeSpan(0.8f), firstLineEnd, str.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            return spn;
        }
    };

    private OnClickListener checkClick = new OnClickListener() {

        @Override
        public void onClick(View v) {
            if ((AddressManager.getInstance().getPrivKeyAddresses() == null
                    || AddressManager.getInstance().getPrivKeyAddresses().size() == 0)
                    && !AddressManager.getInstance().hasHDMKeychain()
                    && !AddressManager.getInstance().hasHDAccountHot()) {
                DropdownMessage.showDropdownMessage(getActivity(), R.string.private_key_is_empty);
                return;
            }
            Intent intent = new Intent(getActivity(), CheckPrivateKeyActivity.class);
            startActivity(intent);
        }
    };
    private OnClickListener donateClick = new OnClickListener() {

        @Override
        public void onClick(View v) {
            DialogDonate dialog = new DialogDonate(getActivity());
            dialog.show();
        }
    };
    /*高级选项*/
    private OnClickListener advanceClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getActivity(), HotAdvanceActivity.class);
            startActivity(intent);
        }
    };
    private OnClickListener avatarClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (!PermissionUtil.isWriteExternalStoragePermission(getActivity(), PrimerSetting.REQUEST_CODE_PERMISSION_WRITE_EXTERNAL_STORAGE)) {
                return;
            }
            if (!PermissionUtil.isCameraPermission(getActivity(), PrimerSetting.REQUEST_CODE_PERMISSION_CAMERA_AVATAR)) {
                return;
            }
            DialogSetAvatar dialog = new DialogSetAvatar(getActivity(), OptionHotFragment.this);
            dialog.show();
        }
    };
    private OnClickListener websiteClick = new OnClickListener() {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/primecoin/primer-android"))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
                DropdownMessage.showDropdownMessage(getActivity(), R.string.find_browser_error);
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

    private OnClickListener monitorClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            monitorUtil = new MonitorPrimerColdUtil(OptionHotFragment.this, new
                    MonitorPrimerColdUtil.MonitorPrimerColdUtilDelegate() {
                        @Override
                        public void onAddressMonitored(ArrayList<String> addresses) {
                            monitorUtil = null;
                            if (getActivity() instanceof HotActivity) {
                                HotActivity hot = (HotActivity) getActivity();
                                Intent intent = new Intent();
                                intent.putExtra(PrimerSetting.INTENT_REF.ADDRESS_POSITION_PASS_VALUE_TAG,
                                        addresses);
                                hot.onActivityResult(PrimerSetting.INTENT_REF.SCAN_REQUEST_CODE, Activity
                                        .RESULT_OK, intent);
                            }
                        }
                    });
            monitorUtil.scan();
        }
    };
    //监控冷HD账户
    private OnClickListener monitorColdHDClick = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            if (AddressManager.getInstance().hasHDAccountMonitored()) {
                DropdownMessage.showDropdownMessage(getActivity(), R.string
                        .monitor_cold_hd_account_limit);
                return;
            }
            startActivityForResult(new Intent(getActivity(), ScanActivity.class),
                    MonitorCodeHDRequestCode);
        }
    };
    //导入私钥选择器
    private SettingSelectorView.SettingSelector importPrivateKeySelector = new
            SettingSelectorView.SettingSelector() {
                @Override
                public int getOptionCount() {
                    return 2;
                   /* if (AddressManager.getInstance().getHDAccountHot() != null) {
                        return 2;
                    } else {
                        return 4;
                    }*/
                }

                @Override
                public String getOptionName(int index) {
                    switch (index) {
                        case 0:
                            return getString(R.string.import_private_key_qr_code);
                        case 1:
                            return getString(R.string.import_private_key_text);
                        case 2:
                            return getString(R.string.import_hd_account_seed_qr_code);
                        case 3:
                            return getString(R.string.import_hd_account_seed_phrase);
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
                        case 2:
                            return getResources().getDrawable(R.drawable.scan_button_icon);
                        case 1:
                        case 3:
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
                    switch (index) {
                        case 0:
                            importPrivateKeyFromQrCode();
                            break;
                        case 1:
                            DialogImportPrivateKeyText dialog= new DialogImportPrivateKeyText(getActivity());
                            dialog.show();
                            dialog.setOnBtnScanClickListener(new DialogImportPrivateKeyText.OnBtnScanClickListener() {
                                @Override
                                public void onBtnScanClick() {
                                    startActivityForResult(new Intent(getActivity(), ScanActivity.class),
                                            DialogImportPrivateKeyText.ScanPrivateKeyQRCodeRequestCode);
                                }
                            });
                            break;
                        case 2://来自HD种子二维码
                            importHDFromQRCode();
                            break;
                        case 3:
                            importHDFromPhrase();
                            break;
                        default:
                            return;
                    }
                }
            };

    private void importPrivateKeyFromQrCode() {
        Intent intent = new Intent(getActivity(), ScanQRCodeTransportActivity.class);
        intent.putExtra(PrimerSetting.INTENT_REF.TITLE_STRING,
                getString(R.string.import_private_key_qr_code_scan_title));
        startActivityForResult(intent, PrimerSetting.INTENT_REF
                .IMPORT_PRIVATE_KEY_REQUEST_CODE);
    }

    private void importHDFromQRCode() {
        Intent intent = new Intent(getActivity(), ScanQRCodeTransportActivity.class);
        intent.putExtra(PrimerSetting.INTENT_REF.TITLE_STRING,
                getString(R.string.import_hd_account_seed_qr_code));
        startActivityForResult(intent, PrimerSetting.INTENT_REF
                .IMPORT_HD_ACCOUNT_SEED_REQUEST_CODE);

    }

    private void importHDFromPhrase() {
        Intent intent = new Intent(getActivity(), HdmImportWordListActivity.class);
        intent.putExtra(PrimerSetting.INTENT_REF.IMPORT_HD_SEED_TYPE, ImportHDSeed.ImportHDSeedType.HDSeedPhrase);
        startActivityForResult(intent, PrimerSetting.INTENT_REF.IMPORT_ACCOUNT_SEED_FROM_PHRASE_REQUEST_CODE);
    }

    @Override
    public void avatarFromCamera() {
        if (FileUtil.existSdCardMounted()) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File file = ImageFileUtil.getImageForCamera(System.currentTimeMillis());
            imageUri = FileProvider.getUriForFile(hotActivity,
                    BuildConfig.APPLICATION_ID + ".provider", file);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(intent, PrimerSetting.REQUEST_CODE_CAMERA);
        } else {
            DropdownMessage.showDropdownMessage(getActivity(), R.string.no_sd_card);
        }
    }

    @Override
    public void avatarFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media
                .EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PrimerSetting.REQUEST_CODE_IMAGE);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (monitorUtil != null && monitorUtil.onActivityResult(requestCode, resultCode, data)) {
            return;
        }
        switch (requestCode) {
            case PrimerSetting.REQUEST_CODE_IMAGE:
                if (data != null) {
                    Intent intent = new Intent(getActivity(), CropImageGlActivity.class);
                    intent.setData(data.getData());
                    intent.setAction(data.getAction());
                    LogUtil.d("fragment", "REQUEST_CODE_IMAGE");
                    startActivityForResult(intent, PrimerSetting.REQUEST_CODE_CROP_IMAGE);
                }
                break;
            case PrimerSetting.REQUEST_CODE_CAMERA:
                Intent intent = new Intent(getActivity(), CropImageGlActivity.class);
                intent.putExtra("android.intent.extra.STREAM", imageUri);
                intent.setAction(Intent.ACTION_SEND);
                LogUtil.d("fragment", "REQUEST_CODE_CAMERA");
                startActivityForResult(intent, PrimerSetting.REQUEST_CODE_CROP_IMAGE);
                break;
            case PrimerSetting.REQUEST_CODE_CROP_IMAGE:
                if (resultCode == Activity.RESULT_OK) {
                    String photoName = "";
                    if (data != null && data.hasExtra(PrimerSetting.INTENT_REF
                            .PIC_PASS_VALUE_TAG)) {
                        photoName = data.getStringExtra(PrimerSetting.INTENT_REF
                                .PIC_PASS_VALUE_TAG);
                    }
                    LogUtil.d("fragment", "photoName:" + photoName);
                    if (!Utils.isEmpty(photoName)) {
                        AppSharedPreference.getInstance().setUserAvatar(photoName);
                        setAvatar(photoName);
                    }
                }
                break;
            case MonitorCodeHDRequestCode://监控冷HD账户
                if (data.getExtras().containsKey(ScanActivity.INTENT_EXTRA_RESULT)) {
                    String content = data.getStringExtra(ScanActivity.INTENT_EXTRA_RESULT);
                    if (!content.startsWith(QRCodeUtil.HD_MONITOR_QR_PREFIX)) {
                        try {
                            final boolean isXRandom = content.indexOf(QRCodeUtil.XRANDOM_FLAG) == 0;
                            Utils.hexStringToByteArray(isXRandom ? content.substring(1) : content);
                            DropdownMessage.showDropdownMessage(getActivity(), R.string.hd_account_monitor_xpub_need_to_upgrade);
                        } catch (Exception ex) {
                            if (dp.isShowing()) {
                                dp.dismiss();
                            }
                            DropdownMessage.showDropdownMessage(getActivity(), R.string
                                    .monitor_cold_hd_account_failed);
                        }
                        return;
                    }
                    final String c = content.substring(QRCodeUtil.HD_MONITOR_QR_PREFIX.length());
                    try {
                        new ThreadNeedService(dp, getActivity()) {
                            @Override
                            public void runWithService(final BlockchainService service) {
                                try {
                                    final DeterministicKey key = DeterministicKey.deserializeB58(c);
                                    final String firstAddress = key.deriveSoftened(AbstractHD
                                            .PathType.EXTERNAL_ROOT_PATH.getValue())
                                            .deriveSoftened(0).toAddress();
                                    ThreadUtil.runOnMainThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (dp.isShowing()) {
                                                dp.dismiss();
                                            }
                                            if (isRepeatHD(firstAddress)) {
                                                DropdownMessage
                                                        .showDropdownMessage(getActivity(), R.string.monitor_cold_hd_account_failed_duplicated);
                                            }
                                            new DialogHDMonitorFirstAddressValidation(getActivity
                                                    (), firstAddress, new Runnable() {

                                                @Override
                                                public void run() {
                                                    new ThreadNeedService(dp, getActivity()) {
                                                        @Override
                                                        public void runWithService(BlockchainService service) {
                                                            try {
                                                                final HDAccount account = new HDAccount(key.getPubKeyExtended(), false, false, null);
                                                                if (service != null) {
                                                                    service.stopAndUnregister();
                                                                }
                                                                AddressManager.getInstance()
                                                                        .setHDAccountMonitored
                                                                                (account);
                                                                if (service != null) {
                                                                    service.startAndRegister();
                                                                }
                                                                ThreadUtil.runOnMainThread(new Runnable() {

                                                                    @Override
                                                                    public void run() {
                                                                        if (dp.isShowing()) {
                                                                            dp.dismiss();
                                                                        }
                                                                        DropdownMessage
                                                                                .showDropdownMessage(getActivity(), R.string.monitor_cold_hd_account_success);
                                                                    }
                                                                });
                                                            } catch (MnemonicException
                                                                    .MnemonicLengthException e) {
                                                                e.printStackTrace();
                                                                ThreadUtil.runOnMainThread(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        if (dp.isShowing()) {
                                                                            dp.dismiss();
                                                                        }
                                                                        DropdownMessage
                                                                                .showDropdownMessage(getActivity(), R.string.monitor_cold_hd_account_failed);
                                                                    }
                                                                });
                                                            } catch (HDAccount
                                                                    .DuplicatedHDAccountException
                                                                    e) {
                                                                e.printStackTrace();
                                                                ThreadUtil.runOnMainThread(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        if (dp.isShowing()) {
                                                                            dp.dismiss();
                                                                        }
                                                                        DropdownMessage
                                                                                .showDropdownMessage(getActivity(), R.string.monitor_cold_hd_account_failed_duplicated);
                                                                    }
                                                                });
                                                            }
                                                        }
                                                    }.start();
                                                }
                                            }).show();
                                        }
                                    });
                                } catch (AddressFormatException e) {
                                    ThreadUtil.runOnMainThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (dp.isShowing()) {
                                                dp.dismiss();
                                            }
                                            DropdownMessage.showDropdownMessage(getActivity(), R
                                                    .string
                                                    .hd_account_monitor_xpub_need_to_upgrade);
                                        }
                                    });
                                }
                            }
                        }.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (dp.isShowing()) {
                            dp.dismiss();
                        }
                        DropdownMessage.showDropdownMessage(getActivity(), R.string
                                .monitor_cold_hd_account_failed);
                    }
                }
                break;
            //导入私钥
            case PrimerSetting.INTENT_REF.IMPORT_PRIVATE_KEY_REQUEST_CODE:
                final String content = data.getStringExtra(ScanActivity.INTENT_EXTRA_RESULT);
                if (content.indexOf(QRCodeUtil.HDM_QR_CODE_FLAG) == 0) {
                    DropdownMessage.showDropdownMessage(getActivity(), R.string.can_not_import_hdm_cold_seed);
                    return;
                }
                DialogPassword dialogPassword = new DialogPassword(getActivity(),
                        new ImportPrivateKeyPasswordListenerI(content, false));
                dialogPassword.setCheckPre(false);
                dialogPassword.setTitle(R.string.import_private_key_qr_code_password);
                dialogPassword.setCheckPasswordListener(new ICheckPasswordListener() {
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
                dialogPassword.show();
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
            case PrimerSetting.INTENT_REF.IMPORT_HD_ACCOUNT_SEED_REQUEST_CODE://扫描HD种子二维码回调
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
        }

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

    private boolean isRepeatHD(String firstAddress) {
        HDAccount hdAccountHot = AddressManager.getInstance().getHDAccountHot();
        if (hdAccountHot == null) {
            return false;
        }
        HDAccount.HDAccountAddress addressHot = hdAccountHot.addressForPath(AbstractHD.PathType.EXTERNAL_ROOT_PATH, 0);
        if (firstAddress.equals(addressHot.getAddress())) {
            return true;
        }
        return false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_hot_option, container, false);
        initView(view);
        hotActivity.setShowImportSuccessListener(new HotActivity.ShowImportSuccessListener() {
            @Override
            public void showImportSuccess() {
                ssvImportPrivateKey.loadData();
                DropdownMessage.showDropdownMessage(getActivity(),
                        R.string.import_private_key_qr_code_success, new Runnable() {
                            @Override
                            public void run() {
                                if (PrimerApplication.hotActivity != null) {
                                    Fragment f = PrimerApplication.hotActivity.getFragmentAtIndex(1);
                                    if (f != null && f instanceof Refreshable) {
                                        Refreshable r = (Refreshable) f;
                                        r.showProgressBar();
                                        r.doRefresh();
                                    }
                                }
                                if (PrimerApplication.hotActivity != null) {
                                    ThreadUtil.getMainThreadHandler().postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            PrimerApplication.hotActivity.scrollToFragmentAt(1);
                                        }
                                    }, 500);
                                }
                            }
                        });
            }
        });
        return view;
    }

    private void initView(View view) {
        ssvCurrency = (SettingSelectorView) view.findViewById(R.id.ssv_currency);
        ssvMarket = (SettingSelectorView) view.findViewById(R.id.ssv_market);
        ssvTransactionFee = (SettingSelectorView) view.findViewById(R.id.ssv_transaction_fee);
        ssvTransactionFeePrecision = (SettingSelectorView) view.findViewById(R.id.ssv_transaction_fee_precision);
        tvVersion = (TextView) view.findViewById(R.id.tv_version);
        tvWebsite = (TextView) view.findViewById(R.id.tv_website);
        tvWebsite.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        tvPrivacyPolicy = (TextView) view.findViewById(R.id.tv_privacy_policy);
        tvPrivacyPolicy.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);//下划线
        ivLogo = (ImageView) view.findViewById(R.id.iv_logo);
        btnSwitchToCold = (Button) view.findViewById(R.id.btn_switch_to_cold);
        llSwitchToCold = view.findViewById(R.id.ll_switch_to_cold);
        btnAvatar = (Button) view.findViewById(R.id.btn_avatar);
        btnCheck = (Button) view.findViewById(R.id.btn_check_private_key);
        btnAdvance = (Button) view.findViewById(R.id.btn_advance);
        view.findViewById(R.id.btn_monitor_hd).setOnClickListener(monitorColdHDClick);
        view.findViewById(R.id.btn_monitor).setOnClickListener(monitorClick);
        ssvCurrency.setSelector(currencySelector);
        ssvMarket.setSelector(marketSelector);
        ssvTransactionFee.setSelector(transactionFeeModeSelector);
        ssvTransactionFeePrecision.setSelector(transactionFeePrecisionSelector);
        dp = new DialogProgress(getActivity(), R.string.please_wait);
        dp.setCancelable(false);
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
        btnSwitchToCold.setOnClickListener(switchToColdClick);
        btnCheck.setOnClickListener(checkClick);
        btnAvatar.setOnClickListener(avatarClick);
        btnAdvance.setOnClickListener(advanceClick);
        tvWebsite.setOnClickListener(websiteClick);
//        ivLogo.setOnClickListener(logoClickListener);
        setAvatar(AppSharedPreference.getInstance().getUserAvatar());
        tvPrivacyPolicy.setOnClickListener(privacyPolicyClick);//隐私政策点击事件
        ssvImportPrivateKey = (SettingSelectorView) view.findViewById(R.id.ssv_import_private_key);
        ssvImportPrivateKey.setSelector(importPrivateKeySelector);
    }

    private void setAvatar(String photoName) {
        Bitmap avatar = null;
        if (!Utils.isEmpty(photoName)) {
            new UpdateAvatarThread(photoName).start();
        } else {
            btnAvatar.setCompoundDrawablesWithIntrinsicBounds(null, null,
                    getResources().getDrawable(R.drawable.avatar_button_icon), null);
        }
    }

    private class UpdateAvatarThread extends Thread {
        private String photoName;

        private UpdateAvatarThread(String photoName) {
            this.photoName = photoName;
        }

        @Override
        public void run() {
            Bitmap avatar = null;
            if (!Utils.isEmpty(photoName)) {
                File file = ImageFileUtil.getSmallAvatarFile(photoName);
                avatar = ImageManageUtil.getBitmapNearestSize(file, 150);
            }
            if (avatar != null) {
                int borderPadding = UIUtil.dip2pix(2);
                Bitmap bmpBorder = BitmapFactory.decodeResource(getResources(),
                        R.drawable.avatar_button_icon_border);
                Bitmap result = Bitmap.createBitmap(bmpBorder.getWidth(), bmpBorder.getHeight(),
                        bmpBorder.getConfig());
                Canvas c = new Canvas(result);
                c.drawBitmap(avatar, null, new Rect(borderPadding, borderPadding, result.getWidth
                        () - borderPadding, result.getHeight() - borderPadding), null);
                c.drawBitmap(bmpBorder, 0, 0, null);
                final BitmapDrawable d = new BitmapDrawable(getResources(), result);
                ThreadUtil.runOnMainThread(new Runnable() {
                    @Override
                    public void run() {
                        btnAvatar.setCompoundDrawablesWithIntrinsicBounds(null, null, d, null);
                    }

                });
            }
            UploadAvatarRunnable uploadAvatarRunnable = new UploadAvatarRunnable();
            uploadAvatarRunnable.run();
        }
    }

    private void configureSwitchToCold() {
        final Runnable check = new Runnable() {
            @Override
            public void run() {
                if (AddressManager.getInstance().getAllAddresses().size() > 0 || AddressManager
                        .getInstance().getTrashAddresses().size() > 0 || AddressManager
                        .getInstance().getHdmKeychain() != null || AddressManager.getInstance()
                        .hasHDAccountHot() || AddressManager.getInstance().hasHDAccountMonitored()) {
                    llSwitchToCold.setVisibility(View.GONE);
                } else {
                    llSwitchToCold.setVisibility(View.VISIBLE);
                }
            }
        };
        if (AbstractApp.addressIsReady) {
            check.run();
        } else {
            new Thread() {
                @Override
                public void run() {
                    AddressManager.getInstance().getAllAddresses();
                    ThreadUtil.runOnMainThread(check);
                }
            }.start();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        configureSwitchToCold();
    }

    @Override
    public void onSelected() {
        configureSwitchToCold();
    }
}
