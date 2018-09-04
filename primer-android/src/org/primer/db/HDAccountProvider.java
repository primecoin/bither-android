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
import org.primer.primerj.crypto.PasswordSeed;
import org.primer.primerj.db.AbstractDb;
import org.primer.primerj.db.imp.AbstractHDAccountProvider;
import org.primer.primerj.db.imp.base.IDb;
import org.primer.primerj.utils.Base58;
import org.primer.db.base.AndroidDb;

public class HDAccountProvider extends AbstractHDAccountProvider {
    private static HDAccountProvider hdAccountProvider = new HDAccountProvider(PrimerApplication.mAddressDbHelper);

    public static HDAccountProvider getInstance() {
        return hdAccountProvider;
    }

    private SQLiteOpenHelper helper;

    public HDAccountProvider(SQLiteOpenHelper helper) {
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
    protected int insertHDAccountToDb(IDb db, String encryptedMnemonicSeed, String encryptSeed, String firstAddress, boolean isXrandom, byte[] externalPub, byte[] internalPub) {
        AndroidDb mdb = (AndroidDb)db;
        ContentValues cv = new ContentValues();
        cv.put(AbstractDb.HDAccountColumns.ENCRYPT_SEED, encryptSeed);
        cv.put(AbstractDb.HDAccountColumns.ENCRYPT_MNMONIC_SEED, encryptedMnemonicSeed);
        cv.put(AbstractDb.HDAccountColumns.IS_XRANDOM, isXrandom ? 1 : 0);
        cv.put(AbstractDb.HDAccountColumns.HD_ADDRESS, firstAddress);
        cv.put(AbstractDb.HDAccountColumns.EXTERNAL_PUB, Base58.encode(externalPub));
        cv.put(AbstractDb.HDAccountColumns.INTERNAL_PUB, Base58.encode(internalPub));
        return  (int) mdb.getSQLiteDatabase().insert(AbstractDb.Tables.HD_ACCOUNT, null, cv);
    }

    @Override
    protected int insertMonitorHDAccountToDb(IDb db, String firstAddress, boolean isXrandom, byte[] externalPub, byte[] internalPub) {
        AndroidDb mdb = (AndroidDb)db;
        ContentValues cv = new ContentValues();
        cv.put(AbstractDb.HDAccountColumns.HD_ADDRESS, firstAddress);
        cv.put(AbstractDb.HDAccountColumns.IS_XRANDOM, isXrandom ? 1 : 0);
        cv.put(AbstractDb.HDAccountColumns.EXTERNAL_PUB, Base58.encode(externalPub));
        cv.put(AbstractDb.HDAccountColumns.INTERNAL_PUB, Base58.encode(internalPub));
        return (int) mdb.getSQLiteDatabase().insert(AbstractDb.Tables.HD_ACCOUNT, null, cv);
    }

    @Override
    protected boolean hasPasswordSeed(IDb db) {
        return AddressProvider.getInstance().hasPasswordSeed(db);
    }

    @Override
    protected void addPasswordSeed(IDb db, PasswordSeed passwordSeed) {
        AddressProvider.getInstance().addPasswordSeed(db, passwordSeed);
    }
}
