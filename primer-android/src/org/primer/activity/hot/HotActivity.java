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

package org.primer.activity.hot;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;

import org.primer.PrimerApplication;
import org.primer.PrimerSetting;
import org.primer.NotificationAndroidImpl;
import org.primer.R;
import org.primer.adapter.hot.HotFragmentPagerAdapter;
import org.primer.primerj.AbstractApp;
import org.primer.primerj.PrimerjSettings;
import org.primer.primerj.core.Address;
import org.primer.primerj.core.AddressManager;
import org.primer.primerj.core.EnterpriseHDMAddress;
import org.primer.primerj.core.HDMAddress;
import org.primer.primerj.core.PeerManager;
import org.primer.primerj.utils.Utils;
import org.primer.fragment.Refreshable;
import org.primer.fragment.Selectable;
import org.primer.fragment.Unselectable;
import org.primer.fragment.hot.HotAddressFragment;
import org.primer.fragment.hot.MarketFragment;
import org.primer.preference.AppSharedPreference;
import org.primer.runnable.AddErrorMsgRunnable;
import org.primer.runnable.DownloadAvatarRunnable;
import org.primer.runnable.UploadAvatarRunnable;
import org.primer.ui.base.BaseFragmentActivity;
import org.primer.ui.base.DropdownMessage;
import org.primer.ui.base.SyncProgressView;
import org.primer.ui.base.TabButton;
import org.primer.ui.base.dialog.DialogFirstRunWarning;
import org.primer.ui.base.dialog.DialogGenerateAddressFinalConfirm;
import org.primer.ui.base.dialog.DialogProgress;
import org.primer.util.LogUtil;
import org.primer.util.StringUtil;
import org.primer.util.UIUtil;
import org.primer.util.WalletUtils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class HotActivity extends BaseFragmentActivity {
    private TabButton tbtnMessage;
    private TabButton tbtnMain;
    private TabButton tbtnMe;
    private FrameLayout flAddAddress;
    private HotFragmentPagerAdapter mAdapter;
    private ViewPager mPager;
    private SyncProgressView pbSync;
    private DialogProgress dp;

    private final TxAndBlockBroadcastReceiver txAndBlockBroadcastReceiver = new
            TxAndBlockBroadcastReceiver();
    private final ProgressBroadcastReceiver broadcastReceiver = new ProgressBroadcastReceiver();
    private final AddressIsLoadedReceiver addressIsLoadedReceiver = new AddressIsLoadedReceiver();

    protected void onCreate(Bundle savedInstanceState) {
        AbstractApp.notificationService.removeProgressState();
        initAppState();
        super.onCreate(savedInstanceState);
        PrimerApplication.hotActivity = this;
        setContentView(R.layout.activity_hot);
        initView();
        registerReceiver();
        mPager.postDelayed(new Runnable() {
            @Override
            public void run() {
                initClick();
                mPager.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Fragment f = getActiveFragment();
                        if (f instanceof Selectable) {
                            ((Selectable) f).onSelected();
                        }
                    }
                }, 100);

                onNewIntent(getIntent());

            }
        }, 500);
        DialogFirstRunWarning.show(this);
    }

    private void registerReceiver() {
        registerReceiver(broadcastReceiver, new IntentFilter(NotificationAndroidImpl
                .ACTION_SYNC_BLOCK_AND_WALLET_STATE));
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(NotificationAndroidImpl.ACTION_SYNC_LAST_BLOCK_CHANGE);
        intentFilter.addAction(NotificationAndroidImpl.ACTION_ADDRESS_BALANCE);
        registerReceiver(txAndBlockBroadcastReceiver, intentFilter);
        registerReceiver(addressIsLoadedReceiver,
                new IntentFilter(NotificationAndroidImpl.ACTION_ADDRESS_LOAD_COMPLETE_STATE));
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(broadcastReceiver);
        unregisterReceiver(txAndBlockBroadcastReceiver);
        unregisterReceiver(addressIsLoadedReceiver);
        super.onDestroy();
        PrimerApplication.hotActivity = null;

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        PrimerApplication.startBlockchainService();
        //通知节点更新
        PeerManager.instance().notifyMaxConnectedPeerCountChange();
        refreshTotalBalance();
    }

    private void deleteNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context
                .NOTIFICATION_SERVICE);
        notificationManager.cancel(PrimerSetting.NOTIFICATION_ID_COINS_RECEIVED);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        deleteNotification();
        if (intent != null && intent.getExtras() != null && intent.getExtras().containsKey
                (PrimerSetting.INTENT_REF.NOTIFICATION_ADDRESS)) {
            final String address = intent.getExtras().getString(PrimerSetting.INTENT_REF
                    .NOTIFICATION_ADDRESS);
            mPager.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mPager.getCurrentItem() != 1) {
                        mPager.setCurrentItem(1, false);
                    }
                    Fragment fragment = getFragmentAtIndex(1);
                    if (fragment != null && fragment instanceof HotAddressFragment) {
                        ((HotAddressFragment) fragment).scrollToAddress(address);
                    }
                }
            }, 400);
        }
    }

    private void initView() {
        pbSync = (SyncProgressView) findViewById(R.id.pb_sync);
        flAddAddress = (FrameLayout) findViewById(R.id.fl_add_address);

        tbtnMain = (TabButton) findViewById(R.id.tbtn_main);
        tbtnMessage = (TabButton) findViewById(R.id.tbtn_message);
        tbtnMe = (TabButton) findViewById(R.id.tbtn_me);

        configureTopBarSize();
        configureTabMainIcons();
        tbtnMain.setBigInteger(null, null, null, null, null, null);
        if (AbstractApp.addressIsReady) {
            refreshTotalBalance();
        }
        tbtnMessage.setIconResource(R.drawable.tab_market, R.drawable.tab_market_checked);
        tbtnMe.setIconResource(R.drawable.tab_option, R.drawable.tab_option_checked);
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.postDelayed(new Runnable() {
            @Override
            public void run() {
                mAdapter = new HotFragmentPagerAdapter(getSupportFragmentManager());
                mPager.setAdapter(mAdapter);
                mPager.setCurrentItem(1);
                mPager.setOffscreenPageLimit(2);
                mPager.setOnPageChangeListener(new PageChangeListener(new TabButton[]{tbtnMessage,
                        tbtnMain, tbtnMe}, mPager));
            }
        }, 100);
    }

    private void initClick() {
        flAddAddress.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                boolean isPrivateKeyLimit = AddressManager.isPrivateLimit();
                boolean isWatchOnlyLimit = AddressManager.isWatchOnlyLimit();
                if (isPrivateKeyLimit && isWatchOnlyLimit) {
                    DropdownMessage.showDropdownMessage(HotActivity.this,
                            R.string.private_key_count_limit);
                    DropdownMessage.showDropdownMessage(HotActivity.this,
                            R.string.watch_only_address_count_limit);
                    return;
                }
                //直接跳转到生成热钱包界面
//                Intent intent = new Intent(HotActivity.this, AddHotAddressActivity.class);
                Intent intent = new Intent(HotActivity.this, AddHotAddressPrivateKeyActivity.class);
                startActivityForResult(intent, PrimerSetting.INTENT_REF.SCAN_REQUEST_CODE);
                overridePendingTransition(R.anim.activity_in_drop, R.anim.activity_out_back);

            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PrimerSetting.INTENT_REF.SCAN_REQUEST_CODE && resultCode == RESULT_OK) {
            ArrayList<String> addresses = (ArrayList<String>) data.getExtras().getSerializable
                    (PrimerSetting.INTENT_REF.ADDRESS_POSITION_PASS_VALUE_TAG);
            if (addresses != null && addresses.size() > 0) {
                Address a = WalletUtils.findPrivateKey(addresses.get(0));
                if (a != null && a.hasPrivKey() && !a.isFromXRandom()) {
                    new DialogGenerateAddressFinalConfirm(this, addresses.size(),
                            a.isFromXRandom()).show();
                }

                Fragment f = getFragmentAtIndex(1);
                if (f != null && f instanceof HotAddressFragment) {
                    mPager.setCurrentItem(1, true);
                    HotAddressFragment af = (HotAddressFragment) f;
                    af.showAddressesAdded(addresses);
                }
                if (f != null && f instanceof Refreshable) {
                    Refreshable r = (Refreshable) f;
                    r.doRefresh();
                }
            }
            return;
        }

        if (requestCode == SelectAddressToSendActivity.SEND_REQUEST_CODE && resultCode ==
                RESULT_OK) {
            DropdownMessage.showDropdownMessage(this, R.string.donate_thanks);
        }

    }

    private class PageChangeListener implements OnPageChangeListener {
        private List<TabButton> indicators;
        private ViewPager pager;

        public PageChangeListener(TabButton[] buttons, ViewPager viewPager) {
            this.indicators = new ArrayList<TabButton>();
            this.pager = viewPager;
            int size = buttons.length;
            for (int i = 0;
                 i < size;
                 i++) {
                TabButton button = buttons[i];
                indicators.add(button);
                if (pager.getCurrentItem() == i) {
                    button.setChecked(true);
                }
                button.setOnClickListener(new IndicatorClick(i));
            }

        }

        public void onPageScrollStateChanged(int state) {

        }

        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        private class IndicatorClick implements OnClickListener {

            private int position;

            public IndicatorClick(int position) {
                this.position = position;
            }

            public void onClick(View v) {
                if (pager.getCurrentItem() != position) {
                    pager.setCurrentItem(position, true);
                } else {
                    if (getActiveFragment() instanceof Refreshable) {
                        ((Refreshable) getActiveFragment()).doRefresh();
                    }
                    if (position == 1) {
                        tbtnMain.showDialog();
                    }
                }
            }
        }

        public void onPageSelected(int position) {

            if (position >= 0 && position < indicators.size()) {
                for (int i = 0;
                     i < indicators.size();
                     i++) {
                    indicators.get(i).setChecked(i == position);
                    if (i != position) {
                        Fragment f = getFragmentAtIndex(i);
                        if (f instanceof Unselectable) {
                            ((Unselectable) f).onUnselected();
                        }
                    }
                }
            }
            Fragment mFragment = getActiveFragment();
            if (mFragment instanceof Selectable) {
                ((Selectable) mFragment).onSelected();
            }
        }
    }

    public void scrollToFragmentAt(int index) {
        if (mPager.getCurrentItem() != index) {
            mPager.setCurrentItem(index, true);
        }
    }

    private void configureTopBarSize() {
        int sideBarSize = UIUtil.getScreenWidth() / 3 - UIUtil.getScreenWidth() / 18;
        tbtnMessage.getLayoutParams().width = sideBarSize;
        tbtnMe.getLayoutParams().width = sideBarSize;
    }

    public Fragment getFragmentAtIndex(int i) {
        String str = StringUtil.makeFragmentName(this.mPager.getId(), i);
        return getSupportFragmentManager().findFragmentByTag(str);
    }

    public Fragment getActiveFragment() {
        Fragment localFragment = null;
        if (this.mPager == null) {
            return localFragment;
        }
        localFragment = getFragmentAtIndex(mPager.getCurrentItem());
        return localFragment;
    }

    private void initAppState() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                AppSharedPreference.getInstance().touchLastUsed();
                AddErrorMsgRunnable addErrorMsgRunnable = new AddErrorMsgRunnable();
                addErrorMsgRunnable.run();
                UploadAvatarRunnable uploadAvatarRunnable = new UploadAvatarRunnable();
                uploadAvatarRunnable.run();
                DownloadAvatarRunnable downloadAvatarRunnable = new DownloadAvatarRunnable();
                downloadAvatarRunnable.run();
            }
        }).start();
    }

    public void refreshTotalBalance() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                long totalPrivate = 0;
                long totalWatchOnly = 0;
                long totalHdm = 0;
                long totalEnterpriseHdm = 0;
                for (Address address : AddressManager.getInstance().getPrivKeyAddresses()) {
                    totalPrivate += address.getBalance();
                }
                for (Address address : AddressManager.getInstance().getWatchOnlyAddresses()) {
                    totalWatchOnly += address.getBalance();
                }
                if (AddressManager.getInstance().hasHDMKeychain()) {
                    for (HDMAddress address : AddressManager.getInstance().getHdmKeychain()
                            .getAddresses()) {
                        totalHdm += address.getBalance();
                    }
                }
                if (AddressManager.getInstance().hasEnterpriseHDMKeychain()) {
                    for (EnterpriseHDMAddress address : AddressManager.getInstance()
                            .getEnterpriseHDMKeychain().getAddresses()) {
                        totalEnterpriseHdm += address.getBalance();
                    }
                }
                final long btcPrivate = totalPrivate;
                final long btcWatchOnly = totalWatchOnly;
                final long btcHdm = totalHdm;
                final long btcEnterpriseHdm = totalEnterpriseHdm;
                final long btcHD = AddressManager.getInstance().hasHDAccountHot() ? AddressManager
                        .getInstance().getHDAccountHot().getBalance() : 0;
                final long btcHdMonitored = AddressManager.getInstance().hasHDAccountMonitored()
                        ? AddressManager.getInstance().getHDAccountMonitored().getBalance() : 0;
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        configureTabMainIcons();
                        tbtnMain.setBigInteger(BigInteger.valueOf(btcPrivate), BigInteger.valueOf
                                        (btcWatchOnly), BigInteger.valueOf(btcHdm), BigInteger.valueOf
                                        (btcHD), BigInteger.valueOf(btcHdMonitored),
                                BigInteger.valueOf(btcEnterpriseHdm));
                    }
                });
            }
        }).start();
    }

    private void configureTabMainIcons() {
        switch (AppSharedPreference.getInstance().getBitcoinUnit()) {
            case XPM:
            default:
                tbtnMain.setIconResource(R.drawable.tab_main, R.drawable.tab_main_checked);
        }
    }

    public void notifPriceAlert(PrimerjSettings.MarketType marketType) {
        if (mPager.getCurrentItem() != 0) {
            mPager.setCurrentItem(0);
        }
        Fragment fragment = getActiveFragment();
        if (fragment instanceof MarketFragment) {
            MarketFragment marketFragment = (MarketFragment) fragment;
            marketFragment.notifPriceAlert(marketType);
        }
    }

    //接收请求进度广播
    private final class ProgressBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (intent != null && intent.hasExtra(NotificationAndroidImpl.ACTION_PROGRESS_INFO)) {
                double progress = intent.getDoubleExtra(NotificationAndroidImpl.ACTION_PROGRESS_INFO, 0);
                LogUtil.d("progress", "BlockchainBroadcastReceiver" + progress);
                pbSync.setProgress(progress);
            }
        }
    }

    public void showProgressBar() {
        pbSync.setProgress(0.6);
    }

    //刷新TX
    private final class TxAndBlockBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent == null ||
                    (!Utils.compareString(NotificationAndroidImpl.ACTION_ADDRESS_BALANCE, intent.getAction())
                            && !Utils.compareString(NotificationAndroidImpl.ACTION_SYNC_LAST_BLOCK_CHANGE, intent.getAction()))) {
                return;
            }
            if (Utils.compareString(NotificationAndroidImpl.ACTION_ADDRESS_BALANCE, intent.getAction())) {
                refreshTotalBalance();
            }
            Fragment fragment = getFragmentAtIndex(1);
            if (fragment != null && fragment instanceof HotAddressFragment) {
                ((HotAddressFragment) fragment).refresh();
                pbSync.setProgress(-1);
            }
        }
    }

    private final class AddressIsLoadedReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null || !Utils.compareString(intent.getAction(), NotificationAndroidImpl.ACTION_ADDRESS_LOAD_COMPLETE_STATE)) {
                return;
            }
            refreshTotalBalance();
            Fragment fragment = getFragmentAtIndex(1);
            if (fragment != null && fragment instanceof HotAddressFragment) {
                ((HotAddressFragment) fragment).refresh();
            }
        }
    }

    private ShowImportSuccessListener showImportSuccessListener;

    public void setShowImportSuccessListener(ShowImportSuccessListener showImportSuccessListener) {
        this.showImportSuccessListener = showImportSuccessListener;
    }

    public interface ShowImportSuccessListener {

        void showImportSuccess();
    }

    public void showImportSuccess() {
        if (showImportSuccessListener != null)
            showImportSuccessListener.showImportSuccess();
    }

//    private void addNewPrivateKey() {
//        final AppSharedPreference preference = AppSharedPreference.getInstance();
//        if (!preference.hasPrivateKey()) {
//            dp = new DialogProgress(HotActivity.this, R.string.please_wait);
//            dp.setCancelable(false);
//            DialogPassword dialogPassword = new DialogPassword(HotActivity.this,
//                    new DialogPasswordListener() {
//
//                        @Override
//                        public void onPasswordEntered(final SecureCharSequence password) {
//                            ThreadNeedService thread = new ThreadNeedService(dp, HotActivity.this) {
//
//                                @Override
//                                public void runWithService(BlockchainService service) {
//
//                                    ECKey ecKey = PrivateKeyUtil.encrypt(new ECKey(), password);
//                                    Address address = new Address(ecKey);
//                                    List<Address> addressList = new ArrayList<Address>();
//                                    addressList.add(address);
//                                    if (!AddressManager.getInstance().getAllAddresses().contains(address)) {
//
//                                        password.wipe();
//                                        KeyUtil.addAddressListByDesc(service, addressList);
//                                        preference.setHasPrivateKey(true);
//                                    }
//                                    password.wipe();
//                                    HotActivity.this.runOnUiThread(new Runnable() {
//                                        @Override
//                                        public void run() {
//
//                                            if (dp.isShowing()) {
//                                                dp.dismiss();
//                                            }
//                                            Fragment fragment = getFragmentAtIndex(1);
//                                            if (fragment instanceof Refreshable) {
//                                                ((Refreshable) fragment).doRefresh();
//                                            }
//
//                                            new DialogConfirmTask(HotActivity.this,
//                                                    getString(R.string
//                                                            .first_add_private_key_check_suggest),
//                                                    new Runnable() {
//                                                        @Override
//                                                        public void run() {
//                                                            ThreadUtil.runOnMainThread(new Runnable() {
//                                                                @Override
//                                                                public void run() {
//                                                                    Intent intent = new Intent(HotActivity.this,
//                                                                            CheckPrivateKeyActivity.class);
//                                                                    intent.putExtra(PrimerSetting.INTENT_REF
//                                                                                    .ADD_PRIVATE_KEY_SUGGEST_CHECK_TAG, true
//                                                                    );
//                                                                    startActivity(intent);
//                                                                }
//                                                            });
//                                                        }
//                                                    }
//                                            ).show();
//                                        }
//                                    });
//                                }
//                            };
//                            thread.start();
//                        }
//                    }
//            );
//            dialogPassword.setCancelable(false);
//            dialogPassword.show();
//        }
//    }
}
