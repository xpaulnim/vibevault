package com.code.android.vibevault.repository;

import android.content.Context;

import com.code.android.vibevault.StaticDb;
import com.code.android.vibevault.dao.PrefDao;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.code.android.vibevault.StaticDataStore.sanitize;

public class PrefRepository {
    private PrefDao prefDao;

    public PrefRepository(Context context) {
        prefDao = StaticDb.getDatabase(context).prefDao();
    }

    public String getPref(final String prefName) {
        Future<String> future = StaticDb.DB_EXECUTOR_SERVICE.submit(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return prefDao.getPref(prefName);
            }
        });

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return "NULL";
        }
    }

    public void updatePref(final String pref_name, final String pref_value) {
        StaticDb.DB_EXECUTOR_SERVICE.execute(new Runnable() {
            @Override
            public void run() {
                prefDao.updatePref(sanitize(pref_name), sanitize(pref_value));
            }
        });
    }
}
