package org.primer.activity.hot;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.TextView;

import org.primer.R;
import org.primer.primerj.core.SplitCoin;
import org.primer.primerj.utils.Utils;
import org.primer.fragment.hot.HotAddressFragment;
import org.primer.ui.base.SwipeRightFragmentActivity;
import org.primer.ui.base.listener.IBackClickListener;

import static org.primer.activity.hot.HotAdvanceActivity.SplitCoinKey;

/**
 * Created by ltq on 2017/7/26.
 */

public class SplitBccSelectAddressActivity extends SwipeRightFragmentActivity {
    public static final int SPLIT_BCC_HDACCOUNT_REQUEST_CODE = 777;
    HotAddressFragment hotAddressFragment;
    private TextView tvTitle;
    private SplitCoin splitCoin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.slide_in_right, 0);
        setContentView(R.layout.activity_split_bcc_address);

        Intent intent = getIntent();
        splitCoin = (SplitCoin) intent.getSerializableExtra(SplitCoinKey);
        initView();
    }

    private void initView() {
        findViewById(R.id.ibtn_back).setOnClickListener(
                new IBackClickListener(0, R.anim.slide_out_right));

        HotAddressFragment fragment = new HotAddressFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(SplitCoinKey, splitCoin);
        fragment.setArguments(bundle);
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction trans = manager.beginTransaction();
        trans.add(R.id.fl_split_address, fragment);
        trans.commit();
        hotAddressFragment = fragment;
        tvTitle = (TextView) findViewById(R.id.tv_split_coin_title);
        tvTitle.setText(Utils.format(getString(R.string.get_split_coin_setting_name), splitCoin.getName()));
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.slide_out_right);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (SplitBccSelectAddressActivity.
                SPLIT_BCC_HDACCOUNT_REQUEST_CODE == requestCode) {
            hotAddressFragment.doRefresh();
        }
    }
}
