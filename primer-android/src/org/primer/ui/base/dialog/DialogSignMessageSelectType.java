package org.primer.ui.base.dialog;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.LinearLayout;

import org.primer.R;
import org.primer.SignMessageAddressListActivity;
import org.primer.primerj.core.AddressManager;
import org.primer.primerj.core.HDAccount;
import org.primer.primerj.core.HDAccountCold;
import org.primer.primerj.crypto.SecureCharSequence;
import org.primer.enums.SignMessageTypeSelect;
import org.primer.ui.base.listener.IDialogPasswordListener;

import static org.primer.SignMessageAddressListActivity.IsHdAccountHot;
import static org.primer.SignMessageAddressListActivity.PassWord;
import static org.primer.SignMessageAddressListActivity.SignMgsTypeSelect;

/**
 * Created by ltq on 2017/7/21.
 */

public class DialogSignMessageSelectType extends CenterDialog {

    private LinearLayout llHdReceive;
    private LinearLayout llHdChange;
    private LinearLayout llHot;
    private View vLineReceive;
    private View vLineChange;
    private HDAccount hdAccount;
    private HDAccountCold hdAccountCold;

    public DialogSignMessageSelectType(Context context, final boolean isHot) {
        super(context);
        setContentView(R.layout.dialog_sign_message_select_type);
        llHdReceive = (LinearLayout) findViewById(R.id.ll_hd_receive);
        llHdChange = (LinearLayout) findViewById(R.id.ll_hd_change);
        llHot = (LinearLayout) findViewById(R.id.ll_hot);
        vLineReceive = (View) findViewById(R.id.v_line_receive);
        vLineChange = (View) findViewById(R.id.v_line_change);
        hdAccount = AddressManager.getInstance().getHDAccountHot();
        hdAccountCold = AddressManager.getInstance().getHDAccountCold();

        if (isHot) {
            if (hdAccount == null) {
                llHdReceive.setVisibility(View.GONE);
                vLineReceive.setVisibility(View.GONE);
                llHdChange.setVisibility(View.GONE);
                vLineChange.setVisibility(View.GONE);
            }
        } else {
            if (hdAccountCold == null) {
                llHdReceive.setVisibility(View.GONE);
                vLineReceive.setVisibility(View.GONE);
                llHdChange.setVisibility(View.GONE);
                vLineChange.setVisibility(View.GONE);
            }
        }

        if (AddressManager.getInstance().getPrivKeyAddresses().size() <= 0) {
            llHot.setVisibility(View.GONE);
        }

        llHdReceive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DialogPassword(getContext(), new IDialogPasswordListener() {
                    @Override
                    public void onPasswordEntered(final SecureCharSequence password) {
                        Intent intent = new Intent(getContext(), SignMessageAddressListActivity.class);
                        intent.putExtra(SignMgsTypeSelect, SignMessageTypeSelect.HdReceive);
                        intent.putExtra(PassWord, password.toString());
                        intent.putExtra(IsHdAccountHot, isHot);
                        getContext().startActivity(intent);
                    }
                }).show();
                dismiss();
            }
        });

        llHdChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new DialogPassword(getContext(), new IDialogPasswordListener() {
                    @Override
                    public void onPasswordEntered(final SecureCharSequence password) {
                        Intent intent = new Intent(getContext(), SignMessageAddressListActivity.class);
                        intent.putExtra(SignMgsTypeSelect, SignMessageTypeSelect.HdChange);
                        intent.putExtra(PassWord, password);
                        intent.putExtra(IsHdAccountHot, isHot);
                        getContext().startActivity(intent);
                    }
                }).show();
                dismiss();
            }
        });

        llHot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), SignMessageAddressListActivity.class);
                intent.putExtra(SignMgsTypeSelect, SignMessageTypeSelect.Hot);
                intent.putExtra(IsHdAccountHot, isHot);
                getContext().startActivity(intent);
                dismiss();
            }
        });
    }

    @Override
    public void show() {
        super.show();
    }
}
