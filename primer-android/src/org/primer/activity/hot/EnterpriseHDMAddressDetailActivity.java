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

package org.primer.activity.hot;

import android.os.Bundle;

import org.primer.PrimerSetting;
import org.primer.R;
import org.primer.primerj.api.http.PrimerUrl;
import org.primer.primerj.core.AddressManager;
import org.primer.primerj.utils.Utils;
import org.primer.ui.base.dialog.DialogWithActions;
import org.primer.util.UIUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by songchenwen on 15/6/12.
 */
public class EnterpriseHDMAddressDetailActivity extends AddressDetailActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void initAddress() {
        if (getIntent().getExtras().containsKey(PrimerSetting.INTENT_REF
                .ADDRESS_POSITION_PASS_VALUE_TAG)) {
            if (AddressManager.getInstance().hasEnterpriseHDMKeychain()) {
                addressPosition = getIntent().getExtras().getInt(PrimerSetting.INTENT_REF
                        .ADDRESS_POSITION_PASS_VALUE_TAG);
                address = AddressManager.getInstance().getEnterpriseHDMKeychain().getAddresses()
                        .get(addressPosition);
            }
        }

    }

    @Override
    protected void optionClicked() {
        new DialogWithActions(this) {

            @Override
            protected List<Action> getActions() {
                ArrayList<Action> actions = new ArrayList<Action>();
                actions.add(new Action(R.string.address_option_view_on_blockchain_info, new
                        Runnable() {
                    @Override
                    public void run() {
                        UIUtil.gotoBrower(EnterpriseHDMAddressDetailActivity.this, PrimerUrl
                                .BLOCKCHAIN_INFO_ADDRESS_URL + address.getAddress());
                    }
                }));
                String defaultCountry = Locale.getDefault().getCountry();
                if (Utils.compareString(defaultCountry, "CN") || Utils.compareString
                        (defaultCountry, "cn")) {
                    actions.add(new Action(R.string.address_option_view_on_btc, new
                            Runnable() {
                        @Override
                        public void run() {
                            UIUtil.gotoBrower(EnterpriseHDMAddressDetailActivity.this, PrimerUrl
                                    .BTC_COM_ADDRESS_URL + address.getAddress());
                        }
                    }));
                }
                return actions;
            }
        }.show();
    }

}