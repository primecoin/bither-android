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

package org.primer.fragment.hot;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ExpandableListView;

import org.primer.R;
import org.primer.activity.hot.HotActivity;
import org.primer.activity.hot.SplitBccSelectAddressActivity;
import org.primer.adapter.hot.HotAddressFragmentListAdapter;
import org.primer.primerj.AbstractApp;
import org.primer.primerj.core.Address;
import org.primer.primerj.core.AddressManager;
import org.primer.primerj.core.HDAccount;
import org.primer.primerj.core.HDMAddress;
import org.primer.primerj.core.SplitCoin;
import org.primer.primerj.utils.Utils;
import org.primer.fragment.Refreshable;
import org.primer.fragment.Selectable;
import org.primer.runnable.HandlerMessage;
import org.primer.ui.base.AddressFragmentListItemView;
import org.primer.ui.base.AddressInfoChangedObserver;
import org.primer.ui.base.DropdownMessage;
import org.primer.ui.base.MarketTickerChangedObserver;
import org.primer.ui.base.PinnedHeaderAddressExpandableListView;
import org.primer.ui.base.SmoothScrollListRunnable;
import org.primer.util.BroadcastUtil;
import org.primer.util.UIUtil;

import java.util.ArrayList;
import java.util.List;

import static org.primer.activity.hot.HotAdvanceActivity.SplitCoinKey;

public class HotAddressFragment extends Fragment implements Refreshable, Selectable {
    private HotAddressFragmentListAdapter mAdapter;
    private PinnedHeaderAddressExpandableListView lv;
    private View ivNoAddress;
    private List<Address> watchOnlys;
    private List<Address> privates;//热钱包列表数据
    private List<HDMAddress> hdms;
    private boolean isLoading = false;

    private SelectedThread selectedThread;
    private IntentFilter broadcastIntentFilter = new IntentFilter();
    private List<String> addressesToShowAdded;
    private String notifyAddress = null;
    public boolean isSplitCoinAddress;
    public SplitCoin splitCoin = SplitCoin.BCC;
    private HotActivity hotActivity;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        hotActivity= (HotActivity) activity;
    }

    @Override
    public void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        broadcastIntentFilter.addAction(BroadcastUtil.ACTION_MARKET);
        watchOnlys = new ArrayList<Address>();
        privates = new ArrayList<Address>();
        hdms = new ArrayList<HDMAddress>();
    }

    public void refresh() {
        if (AbstractApp.addressIsReady) {
            List<Address> ps = AddressManager.getInstance().getPrivKeyAddresses();
            List<Address> ws = AddressManager.getInstance().getWatchOnlyAddresses();
            List<HDMAddress> hs = AddressManager.getInstance().hasHDMKeychain() ? AddressManager
                    .getInstance().getHdmKeychain().getAddresses() : null;
            watchOnlys.clear();
            privates.clear();
            hdms.clear();
            if (ws != null) {
                watchOnlys.addAll(ws);
            }
            if (ps != null) {
                privates.addAll(ps);
            }
            if (hs != null) {
                hdms.addAll(hs);
            }
            mAdapter.notifyDataSetChanged();
            if (watchOnlys.size() + privates.size() + hdms.size() + (AddressManager.getInstance()
                    .hasHDAccountHot() ? 1 : 0) + (AddressManager.getInstance()
                    .hasHDAccountMonitored() ? 1 : 0) +
                    (AddressManager.getInstance().hasEnterpriseHDMKeychain() ? 1 : 0) == 0) {
                ivNoAddress.setVisibility(View.VISIBLE);
                lv.setVisibility(View.GONE);
            } else {
                ivNoAddress.setVisibility(View.GONE);
                lv.setVisibility(View.VISIBLE);
            }
            for (int i = 0;
                 i < mAdapter.getGroupCount();
                 i++) {
                lv.expandGroup(i);
            }
            if (notifyAddress != null) {
                scrollToAddress(notifyAddress);
            }
            lv.removeCallbacks(showAddressesAddedRunnable);
            if (addressesToShowAdded != null) {
                lv.postDelayed(showAddressesAddedRunnable, 600);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_hot_address, container, false);
        lv = (PinnedHeaderAddressExpandableListView) view.findViewById(R.id.lv);
        lv.setOnScrollListener(listScroll);
        if (getActivity() instanceof SplitBccSelectAddressActivity) {
            isSplitCoinAddress = true;
        } else {
            isSplitCoinAddress = false;
        }
        Bundle bundle= this.getArguments();
        if (bundle != null) {
            splitCoin = (SplitCoin) bundle.get(SplitCoinKey);
        }
        mAdapter = new HotAddressFragmentListAdapter(getActivity(), watchOnlys, privates, hdms, lv,
                isSplitCoinAddress, splitCoin);
        lv.setAdapter(mAdapter);
        ivNoAddress = view.findViewById(R.id.iv_no_address);
        refresh();
        return view;

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        int listItemCount = lv.getChildCount();
        for (int i = 0;
             i < listItemCount;
             i++) {
            View v = lv.getChildAt(i);
            if (v instanceof AddressFragmentListItemView) {
                AddressFragmentListItemView av = (AddressFragmentListItemView) v;
                av.onResume();
            }
        }
        getActivity().registerReceiver(broadcastReceiver, broadcastIntentFilter);
    }

    @Override
    public void onPause() {
        int listItemCount = lv.getChildCount();
        for (int i = 0;
             i < listItemCount;
             i++) {
            View v = lv.getChildAt(i);
            if (v instanceof AddressFragmentListItemView) {
                AddressFragmentListItemView av = (AddressFragmentListItemView) v;
                av.onPause();
            }
        }
        getActivity().unregisterReceiver(broadcastReceiver);
        super.onPause();
    }

    public void scrollToAddress(final String address) {
        ArrayList<String> addresses = new ArrayList<String>();
        addresses.add(address);
        showAddressesAdded(addresses);
        doRefresh();
    }

    public void showAddressesAdded(List<String> addresses) {
        addressesToShowAdded = addresses;
        if (addressesToShowAdded == null || addressesToShowAdded.size() == 0) {
            DropdownMessage.showDropdownMessage(getActivity(),
                    getString(R.string.addresses_already_monitored));
        }
    }

    @Override
    public void doRefresh() {
        if (lv == null) {
            return;
        }
        if (lv.getFirstVisiblePosition() != 0) {
            lv.post(new SmoothScrollListRunnable(lv, 0, new Runnable() {
                @Override
                public void run() {
                    refresh();
                }
            }));
        } else {
            refresh();
        }
    }

    @Override
    public void showProgressBar() {
        hotActivity.showProgressBar();
    }

    private void lvRefreshing() {
        lv.postDelayed(new Runnable() {
            @Override
            public void run() {
                refresh();
            }
        }, 150);
    }

    @Override
    public void onSelected() {
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
        if ((privates == null || privates.size() == 0) && (watchOnlys == null || watchOnlys.size
                () == 0) && !isLoading) {
            if (lv != null) {
                lvRefreshing();
            } else {
                if (selectedThread == null || !selectedThread.isAlive()) {
                    selectedThread = new SelectedThread();
                    selectedThread.start();
                }
            }
        }
    }

    private class SelectedThread extends Thread {
        @Override
        public void run() {
            super.run();
            for (int i = 0;
                 i < 20;
                 i++) {
                if (lv != null) {
                    lvRefreshing();
                    return;
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
            }
        }
    }

    private Runnable showAddressesAddedRunnable = new Runnable() {
        @Override
        public void run() {
            if (addressesToShowAdded == null || addressesToShowAdded.size() == 0) {
                return;
            }
            boolean isHDM = false;
            boolean isPrivate = false;
            boolean isHD = false;
            boolean isHDMonitored = false;
            int position = 0;
            if (Utils.compareString(addressesToShowAdded.get(0), HDAccount.HDAccountPlaceHolder)) {
                isHD = true;
            } else if (Utils.compareString(addressesToShowAdded.get(0), HDAccount
                    .HDAccountMonitoredPlaceHolder)) {
                isHDMonitored = true;
            } else if (addressesToShowAdded.get(0).startsWith("3")) {
                isHDM = true;
            }
            if (isHD) {
                position = 0;
            } else if (isHDMonitored) {
                position = 0;
            } else if (isHDM) {
                if (addressesToShowAdded.size() == 1) {
                    boolean found = false;
                    if(hdms != null && hdms.size() > 0) {
                        for (int i = 0;
                             i < hdms.size();
                             i++) {
                            if (Utils.compareString(hdms.get(i).getAddress(), addressesToShowAdded.get(0))) {
                                found = true;
                                position = i;
                                break;
                            }
                        }
                    }
                    if (!found) {
                        addressesToShowAdded.clear();
                        return;
                    }
                }
            } else {
                if (privates != null && privates.size() > 0) {
                    for (int i = 0;
                         i < privates.size();
                         i++) {
                        if (Utils.compareString(privates.get(i).getAddress(),
                                addressesToShowAdded.get(0))) {
                            isPrivate = true;
                            position = i;
                            break;
                        }
                    }
                }
                if (!isPrivate) {
                    if (watchOnlys == null || watchOnlys.size() == 0) {
                        addressesToShowAdded = null;
                        return;
                    }
                    boolean foundWatchonly = false;
                    for (int i = 0;
                         i < watchOnlys.size();
                         i++) {
                        if (Utils.compareString(watchOnlys.get(i).getAddress(),
                                addressesToShowAdded.get(0))) {
                            foundWatchonly = true;
                            position = i;
                            break;
                        }
                    }
                    if (!foundWatchonly) {
                        addressesToShowAdded = null;
                        return;
                    }
                }
            }
            int group = mAdapter.getWatchOnlyGroupIndex();
            if (isHD) {
                group = mAdapter.getHDAccountGroupIndex();
            } else if (isHDMonitored) {
                group = mAdapter.getHDAccountMonitoredGroupIndex();
            } else if (isHDM) {
                group = mAdapter.getHDMGroupIndex();
            } else if (isPrivate) {
                group = mAdapter.getPrivateGroupIndex();
            }

            if (addressesToShowAdded.size() > 1) {
                position = 0;
                if (isHDM) {
                    position = hdms.size() - addressesToShowAdded.size();
                }
            }

            if (position == 0) {
                lv.setSelection(lv.getFlatListPosition(ExpandableListView
                        .getPackedPositionForGroup(group)));
            } else {
                lv.setSelectionFromTop(lv.getFlatListPosition(ExpandableListView
                        .getPackedPositionForChild(group, position)), UIUtil.dip2pix(35));
            }

            final int g = group;
            final int p = position;
            lv.postDelayed(new Runnable() {

                @Override
                public void run() {
                    for (int i = 0;
                         i < addressesToShowAdded.size();
                         i++) {
                        int position = lv.getFlatListPosition(ExpandableListView
                                .getPackedPositionForChild(g, p + i));
                        if (position >= lv.getFirstVisiblePosition() && position <= lv
                                .getLastVisiblePosition()) {
                            View v = lv.getChildAt(position - lv.getFirstVisiblePosition());
                            v.startAnimation(AnimationUtils.loadAnimation(getActivity(),
                                    R.anim.address_notification));
                        }
                    }
                    addressesToShowAdded = null;
                }
            }, 400);
        }
    };

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // getString("","") is api 12
            String a = null;

            if (intent.hasExtra(BroadcastUtil.ACTION_ADDRESS_ERROR)) {
                int errorCode = intent.getExtras().getInt(BroadcastUtil.ACTION_ADDRESS_ERROR);

                if (HandlerMessage.MSG_ADDRESS_NOT_MONITOR == errorCode) {
                    int id = R.string.address_monitor_failed_multiple_address;
                    DropdownMessage.showDropdownMessage(getActivity(), id);
                    doRefresh();

                }
            }
            int itemCount = lv.getChildCount();
            for (int i = 0;
                 i < itemCount;
                 i++) {
                View v = lv.getChildAt(i);
                if (v instanceof AddressInfoChangedObserver) {
                    AddressInfoChangedObserver o = (AddressInfoChangedObserver) v;
                    o.onAddressInfoChanged(a);
                }
                if (v instanceof MarketTickerChangedObserver) {
                    MarketTickerChangedObserver o = (MarketTickerChangedObserver) v;
                    o.onMarketTickerChanged();
                }
            }
        }
    };

    private OnScrollListener listScroll = new OnScrollListener() {
        public void onScrollStateChanged(AbsListView view, int scrollState) {
        }

        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                             int totalItemCount) {
            PinnedHeaderAddressExpandableListView v = (PinnedHeaderAddressExpandableListView) view;
            final long flatPos = v.getExpandableListPosition(firstVisibleItem);
            int groupPosition = ExpandableListView.getPackedPositionGroup(flatPos);
            int childPosition = ExpandableListView.getPackedPositionChild(flatPos);
            v.configureHeaderView(groupPosition, childPosition);
        }
    };


}
