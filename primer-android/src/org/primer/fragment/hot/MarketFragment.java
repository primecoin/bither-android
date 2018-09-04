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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.ListView;

import org.primer.R;
import org.primer.adapter.hot.MarketFragmentListAdapter;
import org.primer.primerj.PrimerjSettings;
import org.primer.fragment.Refreshable;
import org.primer.fragment.Selectable;
import org.primer.fragment.Unselectable;
import org.primer.model.Market;
import org.primer.model.MarketTicket;
import org.primer.net.OkHttpHelper;
import org.primer.net.RequestCallback;
import org.primer.net.UrlManager;
import org.primer.ui.base.MarketFragmentListItemView;
import org.primer.ui.base.MarketListHeader;
import org.primer.ui.base.MarketTickerChangedObserver;
import org.primer.util.BroadcastUtil;
import org.primer.util.MarketUtil;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Response;

public class MarketFragment extends Fragment implements Refreshable,
        Selectable, Unselectable {
    private View v;
    private List<Market> markets;
    private MarketListHeader header;
    private ImageView ivMarketPriceAnimIcon;
    private ListView lv;
    private MarketFragmentListAdapter mAdaper;

    private SelectedThread selectedThread;

    private IntentFilter broadcastIntentFilter = new IntentFilter(BroadcastUtil.ACTION_MARKET);
    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            //刷新频率 1分钟
            getExchangeMarketTicker();
            int itemCount = lv.getChildCount();
            for (int i = 0; i < itemCount; i++) {
                View v = lv.getChildAt(i);
                if (v instanceof MarketTickerChangedObserver) {
                    MarketTickerChangedObserver o = (MarketTickerChangedObserver) v;
                    o.onMarketTickerChanged();
                }
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        markets = new ArrayList<Market>(MarketUtil.getMarkets());
        mAdaper = new MarketFragmentListAdapter(getActivity(), markets);
        getExchangeMarketTicker();
    }
    //TODO 获取新的ticket
    private void getExchangeMarketTicker(){
        // 获取ticket
        OkHttpHelper.getInstance().get(UrlManager.TICKER_CNY_URL, new RequestCallback<MarketTicket>(getActivity()) {

            @Override
            public void onSuccess(Response response, MarketTicket marketTicket) {
                MarketUtil.setMarketTicket(marketTicket);

                mAdaper.notifyDataSetChanged();
                header.onMarketTickerChanged();
            }
        });
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_market, container, false);
        header = (MarketListHeader) v.findViewById(R.id.v_header);
        lv = (ListView) v.findViewById(R.id.lv);
        ivMarketPriceAnimIcon = (ImageView) v.findViewById(R.id.iv_market_price_anim_icon);
        lv.setAdapter(mAdaper);
        lv.setEnabled(false);
        return v;
    }

    public void onResume() {
        super.onResume();
        header.onResume();
        int listItemCount = lv.getChildCount();
        for (int i = 0;
             i < listItemCount;
             i++) {
            View v = lv.getChildAt(i);
            if (v instanceof MarketFragmentListItemView) {
                MarketFragmentListItemView av = (MarketFragmentListItemView) v;
                av.onResume();
            }
        }
        getActivity().registerReceiver(broadcastReceiver, broadcastIntentFilter);
    }

    @Override
    public void onPause() {
        header.onPause();
        int listItemCount = lv.getChildCount();
        for (int i = 0;
             i < listItemCount;
             i++) {
            View v = lv.getChildAt(i);
            if (v instanceof MarketFragmentListItemView) {
                MarketFragmentListItemView av = (MarketFragmentListItemView) v;
                av.onPause();
            }
        }
        getActivity().unregisterReceiver(broadcastReceiver);
        super.onPause();
    }
    @Override
    public void onSelected() {
        if (lv != null) {
            header.onResume();
            int listItemCount = lv.getChildCount();
            for (int i = 0;
                 i < listItemCount;
                 i++) {
                View v = lv.getChildAt(i);
                if (v instanceof MarketFragmentListItemView) {
                    MarketFragmentListItemView av = (MarketFragmentListItemView) v;
                    av.onResume();
                }
            }
        } else {
            if (selectedThread == null || !selectedThread.isAlive()) {
                selectedThread = new SelectedThread();
                selectedThread.start();
            }
        }
    }

    public void showPriceAlertAnimTo(int fromX, int fromY, Market toMarket) {
        int[] containerOffset = new int[2];
        v.getLocationInWindow(containerOffset);
        containerOffset[0] += v.getPaddingLeft();
        containerOffset[1] += v.getPaddingTop();
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) ivMarketPriceAnimIcon
                .getLayoutParams();
        lp.topMargin = fromY - containerOffset[1];
        lp.leftMargin = fromX - containerOffset[0];
        int marketIndex = markets.indexOf(toMarket);
        if (marketIndex < lv.getFirstVisiblePosition() || marketIndex > lv.getLastVisiblePosition
                ()) {
            lv.setSelection(marketIndex);
        }
        View marketView = lv.getChildAt(marketIndex - lv.getFirstVisiblePosition());
        View toIconView = marketView.findViewById(R.id.iv_price_alert);
        int[] toLocation = new int[2];
        toIconView.getLocationInWindow(toLocation);
        TranslateAnimation anim = new TranslateAnimation(0, toLocation[0] - fromX, 0,
                toLocation[1] - fromY);
        anim.setDuration(300);
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                doRefresh();
                ivMarketPriceAnimIcon.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        ivMarketPriceAnimIcon.setVisibility(View.VISIBLE);
        ivMarketPriceAnimIcon.startAnimation(anim);
    }

    @Override
    public void doRefresh() {
        if (lv == null) {
            return;
        }
        refresh();
    }

    @Override
    public void showProgressBar() {

    }

    public void refresh() {
        if (mAdaper != null) {
            mAdaper.notifyDataSetChanged();
        }
    }

    @Override
    public void onUnselected() {
        if (header != null) {
        }
    }

    public void notifPriceAlert(PrimerjSettings.MarketType marketType) {
        final Market market = MarketUtil.getMarket(marketType);
        header.setMarket(market);
    }

    private class SelectedThread extends Thread {
        @Override
        public void run() {
            for (int i = 0; i < 20; i++) {
                if (lv != null) {
                    header.onResume();
                    int listItemCount = lv.getChildCount();
                    for (int j = 0; j < listItemCount; j++) {
                        View v = lv.getChildAt(i);
                        if (v instanceof MarketFragmentListItemView) {
                            MarketFragmentListItemView av = (MarketFragmentListItemView) v;
                            av.onResume();
                        }
                    }
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
            }
        }
    }
}
