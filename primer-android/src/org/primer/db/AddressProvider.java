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

import android.content.ContentValues;
import android.database.sqlite.SQLiteOpenHelper;

import org.primer.PrimerApplication;
import org.primer.primerj.core.Address;
import org.primer.primerj.db.AbstractDb;
import org.primer.primerj.db.imp.AbstractAddressProvider;
import org.primer.primerj.db.imp.base.IDb;
import org.primer.primerj.utils.Base58;
import org.primer.primerj.utils.Utils;
import org.primer.db.base.AndroidDb;

public class AddressProvider extends AbstractAddressProvider {
    private static AddressProvider addressProvider = new AddressProvider(PrimerApplication.mAddressDbHelper);

    public static AddressProvider getInstance() {
        return addressProvider;
    }

    private SQLiteOpenHelper helper;

    public AddressProvider(SQLiteOpenHelper helper) {
        this.helper = helper;
    }

    @Override
    public IDb getReadDb() {
        return new AndroidDb(this.helper.getReadableDatabase());
    }

    @Override
    public IDb getWriteDb() {
        return new AndroidDb(this.helper.getWritableDatabase());
    }

    @Override
    protected int insertHDKeyToDb(IDb db, String encryptedMnemonicSeed, String encryptHdSeed, String firstAddress, boolean isXrandom) {
        AndroidDb mdb = (AndroidDb)db;
        ContentValues cv = new ContentValues();
        cv.put(AbstractDb.HDSeedsColumns.ENCRYPT_MNEMONIC_SEED, encryptedMnemonicSeed);
        cv.put(AbstractDb.HDSeedsColumns.ENCRYPT_HD_SEED, encryptHdSeed);
        cv.put(AbstractDb.HDSeedsColumns.IS_XRANDOM, isXrandom ? 1 : 0);
        cv.put(AbstractDb.HDSeedsColumns.HDM_ADDRESS, firstAddress);
        return (int) mdb.getSQLiteDatabase().insert(AbstractDb.Tables.HDSEEDS, null, cv);
    }

    @Override
    protected int insertEnterpriseHDKeyToDb(IDb db, String encryptedMnemonicSeed, String encryptHdSeed, String firstAddress, boolean isXrandom) {
        AndroidDb mdb = (AndroidDb)db;
        ContentValues cv = new ContentValues();
        cv.put(AbstractDb.EnterpriseHDAccountColumns.ENCRYPT_MNEMONIC_SEED, encryptedMnemonicSeed);
        cv.put(AbstractDb.EnterpriseHDAccountColumns.ENCRYPT_SEED, encryptHdSeed);
        cv.put(AbstractDb.EnterpriseHDAccountColumns.IS_XRANDOM, isXrandom ? 1 : 0);
        cv.put(AbstractDb.EnterpriseHDAccountColumns.HD_ADDRESS, firstAddress);
        return (int) mdb.getSQLiteDatabase().insert(AbstractDb.Tables.ENTERPRISE_HD_ACCOUNT, null, cv);
    }

    @Override
    protected void insertHDMAddressToDb(IDb db, String address, int hdSeedId, int index, byte[] pubKeysHot, byte[] pubKeysCold, byte[] pubKeysRemote, boolean isSynced) {
        AndroidDb mdb = (AndroidDb)db;
        ContentValues cv = new ContentValues();
        cv.put(AbstractDb.HDMAddressesColumns.HD_SEED_ID, hdSeedId);
        cv.put(AbstractDb.HDMAddressesColumns.HD_SEED_INDEX, index);
        cv.put(AbstractDb.HDMAddressesColumns.PUB_KEY_HOT, Base58.encode(pubKeysHot));
        cv.put(AbstractDb.HDMAddressesColumns.PUB_KEY_COLD, Base58.encode(pubKeysCold));
        if (Utils.isEmpty(address)) {
            cv.putNull(AbstractDb.HDMAddressesColumns.ADDRESS);
        } else {
            cv.put(AbstractDb.HDMAddressesColumns.ADDRESS, address);
        }
        if (pubKeysRemote == null) {
            cv.putNull(AbstractDb.HDMAddressesColumns.PUB_KEY_REMOTE);
        } else {
            cv.put(AbstractDb.HDMAddressesColumns.PUB_KEY_REMOTE, Base58.encode(pubKeysRemote));
        }
        cv.put(AbstractDb.HDMAddressesColumns.IS_SYNCED, isSynced ? 1 : 0);
        mdb.getSQLiteDatabase().insert(AbstractDb.Tables.HDMADDRESSES, null, cv);
    }

    @Override
    protected void insertAddressToDb(IDb db, Address address) {
        AndroidDb mdb = (AndroidDb)db;
        ContentValues cv = new ContentValues();
        cv.put(AbstractDb.AddressesColumns.ADDRESS, address.getAddress());
        if (address.hasPrivKey()) {
            cv.put(AbstractDb.AddressesColumns.ENCRYPT_PRIVATE_KEY, address.getEncryptPrivKeyOfDb());
        }
        cv.put(AbstractDb.AddressesColumns.PUB_KEY, Base58.encode(address.getPubKey()));
        cv.put(AbstractDb.AddressesColumns.IS_XRANDOM, address.isFromXRandom() ? 1 : 0);
        cv.put(AbstractDb.AddressesColumns.IS_SYNCED, address.isSyncComplete() ? 1 : 0);
        cv.put(AbstractDb.AddressesColumns.IS_TRASH, address.isTrashed() ? 1 : 0);
        cv.put(AbstractDb.AddressesColumns.SORT_TIME, address.getSortTime());
        mdb.getSQLiteDatabase().insert(AbstractDb.Tables.Addresses, null, cv);
    }
}
