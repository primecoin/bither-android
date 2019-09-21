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

package net.bither.ui.base.dialog;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import net.bither.R;

public class DialogChooseNet extends CenterDialog implements View.OnClickListener {

    private TextView tvOk;
    private TextView tvMessage;

    private View clickedView;

    public DialogChooseNet(Context context) {
        super(context);
        this.setContentView(R.layout.dialog_choose_net);
        initView();
    }

    private void initView() {
        tvOk = (TextView) findViewById(R.id.tv_ok);
        tvMessage = (TextView) findViewById(R.id.tv_suggestion_message);
        tvMessage.setText(R.string.choose_net_warn);
        tvOk.setOnClickListener(this);
    }

    @Override
    public void show() {
        clickedView = null;
        super.show();
    }

    @Override
    public void onClick(View v) {
        clickedView = v;
        dismiss();
    }
}