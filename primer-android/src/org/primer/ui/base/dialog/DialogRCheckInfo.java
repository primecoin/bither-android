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

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import org.primer.R;
import org.primer.util.UIUtil;

/**
 * Created by songchenwen on 14-10-24.
 */
public class DialogRCheckInfo extends CenterDialog {
    public DialogRCheckInfo(Context context) {
        super(context);
        setContentView(R.layout.dialog_rcheck_info);
        ((TextView) findViewById(R.id.tv)).setMaxWidth(UIUtil.getScreenWidth() - UIUtil.dip2pix(80));
        findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }
}
