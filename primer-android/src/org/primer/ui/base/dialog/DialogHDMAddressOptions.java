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

package org.primer.ui.base.dialog;

import android.app.Activity;
import android.support.v4.app.Fragment;

import org.primer.PrimerApplication;
import org.primer.R;
import org.primer.activity.hot.AddressDetailActivity;
import org.primer.primerj.PrimerjSettings;
import org.primer.primerj.api.http.PrimerUrl;
import org.primer.primerj.core.AddressManager;
import org.primer.primerj.core.HDMAddress;
import org.primer.primerj.crypto.SecureCharSequence;
import org.primer.fragment.cold.ColdAddressFragment;
import org.primer.fragment.hot.HotAddressFragment;
import org.primer.preference.AppSharedPreference;
import org.primer.ui.base.listener.IDialogPasswordListener;
import org.primer.util.ThreadUtil;
import org.primer.util.UIUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by songchenwen on 15/1/12.
 */
public class DialogHDMAddressOptions extends DialogWithActions {
    private HDMAddress address;
    private Activity activity;
    private boolean withAlias;

    public DialogHDMAddressOptions(Activity activity, HDMAddress address) {
        this(activity, address, false);
    }

    public DialogHDMAddressOptions(Activity activity, HDMAddress address, boolean withAlias) {
        super(activity);
        this.address = address;
        this.activity = activity;
        this.withAlias = withAlias;
    }

    @Override
    protected List<Action> getActions() {
        ArrayList<Action> acitons = new ArrayList<Action>();
        acitons.add(new Action(R.string.address_option_view_on_blockchain_info, new Runnable() {
            @Override
            public void run() {
                UIUtil.gotoBrower(activity, PrimerUrl.BCHAIN_INFO_ADDRESS_URL + address
                        .getAddress());
            }
        }));
       /* String defaultCountry = Locale.getDefault().getCountry();
        if (Utils.compareString(defaultCountry, "CN") || Utils.compareString(defaultCountry,
                "cn")) {
            acitons.add(new Action(R.string.address_option_view_on_btc, new Runnable() {
                @Override
                public void run() {
                    UIUtil.gotoBrower(activity, PrimerUrl.BTC_COM_ADDRESS_URL + address
                            .getAddress());
                }
            }));
        }*/
        Action moveToTrashAction = new Action(R.string.trash_private_key, new Runnable() {
            @Override
            public void run() {
                if (address.getBalance() > 0) {
                    new DialogConfirmTask(getContext(), getContext().getString(R.string
                            .trash_with_money_warn), null).show();
                    return;
                }
                if (AddressManager.getInstance().getHdmKeychain().getAddresses().size() <= 1) {
                    new DialogConfirmTask(getContext(), getContext().getString(R.string
                            .hdm_address_trash_at_least_one_warn), null).show();
                    return;
                }
                new DialogPassword(activity, new IDialogPasswordListener() {
                    @Override
                    public void onPasswordEntered(SecureCharSequence password) {
                        final DialogProgress dp = new DialogProgress(getContext(),
                                R.string.trashing_private_key, null);
                        dp.show();
                        new Thread() {
                            @Override
                            public void run() {
                                AddressManager.getInstance().trashPrivKey(address);
                                ThreadUtil.runOnMainThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        dp.dismiss();
                                        if (activity instanceof AddressDetailActivity) {
                                            activity.finish();
                                        }
                                        if (AppSharedPreference.getInstance().getAppMode() ==
                                                PrimerjSettings.AppMode.HOT) {
                                            Fragment f = PrimerApplication.hotActivity
                                                    .getFragmentAtIndex(1);
                                            if (f instanceof HotAddressFragment) {
                                                HotAddressFragment hotAddressFragment =
                                                        (HotAddressFragment) f;
                                                hotAddressFragment.refresh();
                                            }
                                        } else {
                                            Fragment f = PrimerApplication.coldActivity
                                                    .getFragmentAtIndex(1);
                                            if (f instanceof ColdAddressFragment) {
                                                ColdAddressFragment coldAddressFragment =
                                                        (ColdAddressFragment) f;
                                                coldAddressFragment.refresh();
                                            }
                                        }
                                    }
                                });
                            }
                        }.start();
                    }
                }).show();
            }
        });
        if (withAlias) {
            acitons.add(new Action(R.string.address_alias_manage, new Runnable() {
                @Override
                public void run() {
                    new DialogAddressAlias(getContext(), address,
                            activity instanceof AddressDetailActivity ? (AddressDetailActivity)
                                    activity : null).show();
                }
            }));
        }
        // Not support move hdm address to trash now
        // acitons.add(moveToTrashAction);
        return acitons;
    }
}
