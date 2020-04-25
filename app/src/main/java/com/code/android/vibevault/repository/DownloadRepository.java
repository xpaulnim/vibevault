package com.code.android.vibevault.repository;

import android.content.Context;

import com.code.android.vibevault.ArchiveShowObj;
import com.code.android.vibevault.StaticDb;
import com.code.android.vibevault.dao.DownloadDao;
import com.code.android.vibevault.data.DownloadStatus;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class DownloadRepository {
    private DownloadDao downloadDao;

    public DownloadRepository(Context context) {
        StaticDb staticDb = StaticDb.getDatabase(context);
        downloadDao = staticDb.downloadDao();
    }

    public List<ArchiveShowObj> getDownloads() {
        return downloadDao.getDownloadShows();
    }

    public List<DownloadStatus> getShowDownloadStatus(final String show_identifier) {
        Future<List<DownloadStatus>> future = StaticDb.DB_EXECUTOR_SERVICE.submit(new Callable<List<DownloadStatus>>() {
            @Override
            public List<DownloadStatus> call() throws Exception {
                return downloadDao.getShowDownloadStatus(show_identifier);
            }
        });

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

}
