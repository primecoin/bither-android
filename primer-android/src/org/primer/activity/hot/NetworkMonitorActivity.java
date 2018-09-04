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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import org.primer.R;
import org.primer.primerj.db.AbstractDb;
import org.primer.fragment.hot.BlockListFragment;
import org.primer.fragment.hot.PeerListFragment;
import org.primer.runnable.ThreadNeedService;
import org.primer.service.BlockchainService;
import org.primer.ui.base.BaseFragmentActivity;
import org.primer.ui.base.ViewPagerTabs;
import org.primer.ui.base.dialog.DialogWithActions;
import org.primer.ui.base.listener.IBackClickListener;

import java.util.ArrayList;
import java.util.List;

public final class NetworkMonitorActivity extends BaseFragmentActivity {
    private PeerListFragment peerListFragment;
	private BlockListFragment blockListFragment;
	// private FrameLayout flTitleBar;
	private ImageButton ibtnBack;

	@Override
	protected void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		overridePendingTransition(R.anim.slide_in_right, 0);
		setContentView(R.layout.network_monitor_content);

		// final ActionBar actionBar = getSupportActionBar();
		// actionBar.setDisplayHomeAsUpEnabled(true);

		final ViewPager pager = (ViewPager) findViewById(R.id.network_monitor_pager);

		final FragmentManager fm = getSupportFragmentManager();

		if (pager != null) {
			final ViewPagerTabs pagerTabs = (ViewPagerTabs) findViewById(R.id.network_monitor_pager_tabs);
			pagerTabs.addTabLabels(R.string.network_monitor_peer_list_title,
					R.string.network_monitor_block_list_title);

			final PagerAdapter pagerAdapter = new PagerAdapter(fm);

			pager.setAdapter(pagerAdapter);
			pager.setOnPageChangeListener(pagerTabs);
			pager.setPageMargin(2);
			pager.setPageMarginDrawable(R.color.bg);

			peerListFragment = new PeerListFragment();
			blockListFragment = new BlockListFragment();
		} else {
			peerListFragment = (PeerListFragment) fm
					.findFragmentById(R.id.peer_list_fragment);
			blockListFragment = (BlockListFragment) fm
					.findFragmentById(R.id.block_list_fragment);
		}
		// flTitleBar = (FrameLayout) findViewById(R.id.fl_title_bar);
		ibtnBack = (ImageButton) findViewById(R.id.ibtn_back);
		ibtnBack.setOnClickListener(new IBackClickListener());
        findViewById(R.id.ibtn_option).setOnClickListener(optionClick);
    }

    private View.OnClickListener optionClick = new View.OnClickListener() {

        @Override
        public void onClick(View v) {
            new DialogWithActions(v.getContext()) {

                @Override
                protected List<Action> getActions() {
                    ArrayList<Action> actions = new ArrayList<Action>();
                    actions.add(new Action(R.string.network_monitor_clear_peer, new Runnable() {
                        @Override
                        public void run() {
                            refreshPeer();
                        }
                    }));
                    return actions;
                }
            }.show();
        }
    };
	//刷新节点
    public void refreshPeer(){
        new ThreadNeedService(null, NetworkMonitorActivity.this) {

            @Override
            public void runWithService(BlockchainService service) {
                if (service != null) {
                    service.stopAndUnregister();
                }
                AbstractDb.peerProvider.recreate();
                if (service != null) {
                    service.startAndRegister();
                }
            }
        }.start();
    }
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class PagerAdapter extends FragmentStatePagerAdapter {
        public PagerAdapter(final FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public Fragment getItem(final int position) {
            if (position == 0) {
                return peerListFragment;
            } else {
                return blockListFragment;
            }
        }
    }
}
