package com.example.keyadministrator;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.keyadministrator.facedb.dao.FaceDao;
import com.example.keyadministrator.facedb.entity.FaceEntity;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class QueryFaceTask {

    private final FaceDao faceDao;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private boolean isCancelled = false;
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    public QueryFaceTask(FaceDao faceDao) {
        this.faceDao = faceDao;
    }

    public void execute(int faceId) {
        executor.execute(() -> {
            if(!isCancelled) { // 检查任务是否被取消
                FaceEntity faceEntity = faceDao.queryByFaceId(faceId);
                if (faceEntity != null) {
                    faceDao.deleteFace(faceEntity);
                    boolean delete = new File(faceEntity.getImagePath()).delete();
                    if (!delete) {
                        Log.w("TAG", "deleteFace: failed to delete headImageFile '" + faceEntity.getImagePath() + "'");
                    }
                }
            }
        });
    }

    // 根据 FaceId 获取 FaceEntity，并在主线程回调
    public void getFaceById(int faceId, OnFaceLoadedListener listener) {
        executor.execute(() -> {
            if (!isCancelled) {
                FaceEntity faceEntity = faceDao.queryByFaceId(faceId);
                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onFaceLoaded(faceEntity);
                    }
                });
            }});
    }

    // 新增方法，用于更新 FaceEntity
    public void updateFaceEntity(FaceEntity faceEntity) {
        executor.execute(() -> {
            if (!isCancelled) {
                faceDao.updateFaceEntity(faceEntity); // 调用 faceDao 的更新方法
            }
        });
    }

    // 新增方法，用于获取所有人脸信息
    public List<FaceEntity> getAllFaces() {
        Future<List<FaceEntity>> future = executor.submit(new Callable<List<FaceEntity>>() {
            @Override
            public List<FaceEntity> call() throws Exception {
                if (!isCancelled) {
                    return faceDao.getAllFaces(); // 在后台线程中查询所有人脸信息
                }
                return null;
            }
        });

        try {
            return future.get(); // 等待后台线程返回结果
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            return null; // 处理异常，返回 null
        }
    }


    public void cancel() { // 添加一个终止方法
        isCancelled = true;
        executor.shutdown(); // 关闭线程池
    }

    // 回调接口，用于在主线程返回 FaceEntity
    public interface OnFaceLoadedListener {
        void onFaceLoaded(FaceEntity faceEntity);
    }
}