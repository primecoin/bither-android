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

import android.database.sqlite.SQLiteOpenHelper;

import org.primer.PrimerApplication;
import org.primer.primerj.db.imp.AbstractBlockProvider;
import org.primer.primerj.db.imp.base.IDb;
import org.primer.db.base.AndroidDb;

public class BlockProvider extends AbstractBlockProvider {
    private static BlockProvider blockProvider = new BlockProvider(PrimerApplication.mTxDbHelper);

    public static BlockProvider getInstance() {
        return blockProvider;
    }

    private SQLiteOpenHelper helper;

    public BlockProvider(SQLiteOpenHelper helper) {
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
}