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

package net.bither.ui.base;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nineoldandroids.animation.ArgbEvaluator;
import com.nineoldandroids.animation.ObjectAnimator;

import net.bither.PrimerSetting;
import net.bither.R;
import net.bither.activity.hot.MarketDetailActivity;
import net.bither.model.Market;
import net.bither.preference.AppSharedPreference;
import net.bither.util.ExchangeUtil;
import net.bither.util.MarketUtil;

public class MarketListHeader extends FrameLayout implements MarketTickerChangedObserver,
        ViewTreeObserver.OnGlobalLayoutListener {
    private static final int LightScanInterval = 1200;
    public static int BgAnimDuration = 600;
    private Animation refreshAnim = AnimationUtils.loadAnimation(getContext(),
            R.anim.check_light_scan);
    private View ivLight;
    private TextView tvName;
    private TextView tvSymbol;
    private TextView tvPrice;
    private TrendingGraphicView vTrending;
    private LinearLayout llTrending;
    private View vContainer;
    private View flContainer;
    private View parent;
    private BgHolder bg;
    private InputMethodManager imm;
    private Market mMarket;

    public MarketListHeader(Context context) {
        super(context);
        initView();
    }

    private void initView() {
        removeAllViews();
        imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        parent = LayoutInflater.from(getContext()).inflate(R.layout.layout_market_list_header,
                null);
        addView(parent, LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        flContainer = findViewById(R.id.fl_container);
        vContainer = findViewById(R.id.ll_container);
        ivLight = findViewById(R.id.iv_light);
        tvName = (TextView) findViewById(R.id.tv_market_name);
        tvSymbol = (TextView) findViewById(R.id.tv_currency_symbol);
        tvPrice = (TextView) findViewById(R.id.tv_new_price);
        vTrending = (TrendingGraphicView) findViewById(R.id.v_trending);
        llTrending = (LinearLayout) findViewById(R.id.ll_trending);
        getViewTreeObserver().addOnGlobalLayoutListener(this);
        refreshAnim.setDuration(LightScanInterval);
        refreshAnim.setFillBefore(false);
        refreshAnim.setRepeatCount(0);
        refreshAnim.setFillAfter(false);
//        llTrending.setOnClickListener(new MarketDetailClick());
        llTrending.setOnTouchListener(new TrendingTouch());
        setMarket(MarketUtil.getDefaultMarket());
    }

    private void showMarket() {
        bringToFront();
        if (mMarket == null) {
            return;
        }
        bg.setEndColor(mMarket.getMarketColor());

        String cnyPrice=String.format("%.2f", AppSharedPreference.getInstance().getCNYExchangeRate());
        String usdPrice=String.format("%.2f", AppSharedPreference.getInstance().getUSDExchangeRate());
        String symbol = "";
        ExchangeUtil.Currency currency=  AppSharedPreference.getInstance().getDefaultExchangeType();
        switch (currency){
            case CNY:
                symbol="\u00a5";
                if (!TextUtils.isEmpty(cnyPrice)){
                    tvSymbol.setText(symbol + cnyPrice);
                } else {
                    tvSymbol.setText(PrimerSetting.UNKNOWN_ADDRESS_STRING);
                }
                break;
            case USD:
                symbol="$";
                if (!TextUtils.isEmpty(usdPrice)) {
                    tvSymbol.setText(symbol + usdPrice);
                } else {
                    tvSymbol.setText(PrimerSetting.UNKNOWN_ADDRESS_STRING);
                }
                break;
        }
        if (mMarket != null) {
            vTrending.setMarketType(mMarket.getMarketType());
        }
        tvPrice.setText(AppSharedPreference.getInstance().getTotalSupply());
    }


    public MarketListHeader(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public MarketListHeader(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView();
    }

    public Market getMarket() {
        return mMarket;
    }

    public void setMarket(Market market) {
        if (mMarket == null) {
            bg = new BgHolder(market.getMarketColor());
        }
        mMarket = market;
        showMarket();
    }

    @Override
    public void onMarketTickerChanged() {
        if (mMarket == null) {
            return;
        }
        showMarket();
        if (!refreshAnim.hasStarted() || refreshAnim.hasEnded()) {
            ivLight.startAnimation(refreshAnim);
        }
    }

    public void onPause() {
    }

    public void onResume() {
        showMarket();
    }


    @Override
    public void onGlobalLayout() {
        if (parent.getLayoutParams().height <= 0 && parent.getHeight() > 0) {
            parent.getLayoutParams().height = parent.getHeight();
            vContainer.getLayoutParams().height = vContainer.getHeight();
            flContainer.getLayoutParams().height = flContainer.getHeight();
        }
    }


    private class MarketDetailClick implements OnClickListener {

        @Override
        public void onClick(View v) {
            if (mMarket != null) {
                Intent intent = new Intent(getContext(), MarketDetailActivity.class);
                intent.putExtra(PrimerSetting.INTENT_REF.MARKET_INTENT, mMarket.getMarketType());
                getContext().startActivity(intent);
            }
        }
    }

    private class TrendingTouch implements OnTouchListener {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            vTrending.causeDraw();
            return false;
        }
    }


    private class BgHolder {
        private float animProgress = 0;
        private ArgbEvaluator evaluator = new ArgbEvaluator();
        private int startColor;
        private int endColor;
        private int currentColor;
        private ObjectAnimator animator;
        private View bg;

        public BgHolder(int startColor) {
            this.startColor = startColor;
            this.endColor = startColor;
            bg = findViewById(R.id.fl_bg);
            setBg((Integer) evaluator.evaluate(0, startColor, startColor));
        }

        private void setBg(int color) {
            currentColor = color;
            bg.setBackgroundColor(color);
        }

        public float getAnimProgress() {
            return animProgress;
        }

        public void setAnimProgress(float animProgress) {
            this.animProgress = animProgress;
            if (currentColor == endColor) {
                setBg(currentColor);
            } else {
                setBg((Integer) evaluator.evaluate(animProgress, startColor, endColor));
            }
        }

        public void setEndColor(int endColor) {
            if (animator != null && animator.isRunning()) {
                animator.cancel();
            }
            startColor = currentColor;
            this.endColor = endColor;
            animProgress = 0;
            animator = ObjectAnimator.ofFloat(this, "animProgress", 1);
            animator.setDuration(BgAnimDuration);
            animator.start();
        }
    }

}
