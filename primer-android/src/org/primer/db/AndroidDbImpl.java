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

package org.primer.db;

import org.primer.primerj.db.AbstractDb;
import org.primer.primerj.db.IAddressProvider;
import org.primer.primerj.db.IBlockProvider;
import org.primer.primerj.db.IDesktopAddressProvider;
import org.primer.primerj.db.IDesktopTxProvider;
import org.primer.primerj.db.IEnterpriseHDMProvider;
import org.primer.primerj.db.IHDAccountAddressProvider;
import org.primer.primerj.db.IHDAccountProvider;
import org.primer.primerj.db.IPeerProvider;
import org.primer.primerj.db.ITxProvider;

public class AndroidDbImpl extends AbstractDb {
    @Override
    public IBlockProvider initBlockProvider() {
        return BlockProvider.getInstance();
    }

    @Override
    public IPeerProvider initPeerProvider() {
        return PeerProvider.getInstance();
    }

    @Override
    public ITxProvider initTxProvider() {
        return TxProvider.getInstance();
    }

    @Override
    public IAddressProvider initAddressProvider() {
        return AddressProvider.getInstance();
    }

    @Override
    public IHDAccountAddressProvider initHDAccountAddressProvider() {
        return HDAccountAddressProvider.getInstance();
    }

    @Override
    public IHDAccountProvider initHDAccountProvider() {
        return HDAccountProvider.getInstance();
    }

    @Override
    public IEnterpriseHDMProvider initEnterpriseHDMProvider() {
        return EnterpriseHDMProvider.getInstance();
    }

    @Override
    public IDesktopAddressProvider initEnDesktopAddressProvider() {
        return null;
    }

    @Override
    public IDesktopTxProvider initDesktopTxProvider() {
        return null;
    }

}
