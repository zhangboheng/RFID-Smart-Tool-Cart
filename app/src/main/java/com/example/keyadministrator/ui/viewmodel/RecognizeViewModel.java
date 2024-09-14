package com.example.keyadministrator.ui.viewmodel;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.util.Log;
import android.widget.Toast;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.keyadministrator.ArcFaceApplication;
import com.example.keyadministrator.R;
import com.example.keyadministrator.ui.model.CompareResult;
import com.example.keyadministrator.faceserver.FaceServer;
import com.example.keyadministrator.ui.callback.OnRegisterFinishedCallback;
import com.example.keyadministrator.ui.model.PreviewConfig;
import com.example.keyadministrator.util.ConfigUtil;
import com.example.keyadministrator.util.FaceRectTransformer;
import com.example.keyadministrator.util.face.FaceHelper;
import com.example.keyadministrator.util.face.model.FacePreviewInfo;
import com.example.keyadministrator.util.face.constants.LivenessType;
import com.example.keyadministrator.util.face.RecognizeCallback;
import com.example.keyadministrator.util.face.constants.RecognizeColor;
import com.example.keyadministrator.util.face.model.RecognizeConfiguration;
import com.example.keyadministrator.util.face.constants.RequestFeatureStatus;
import com.example.keyadministrator.widget.FaceRectView;
import com.arcsoft.face.AgeInfo;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceAttributeParam;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceInfo;
import com.arcsoft.face.GenderInfo;
import com.arcsoft.face.LivenessInfo;
import com.arcsoft.face.LivenessParam;
import com.arcsoft.face.MaskInfo;
import com.arcsoft.face.enums.DetectFaceOrientPriority;
import com.arcsoft.face.enums.DetectMode;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class RecognizeViewModel extends ViewModel implements RecognizeCallback {
    /**
     * 人脸识别过程中数据的更新类型
     */
    public enum EventType {
        /**
         * 人脸插入
         */
        INSERTED,
        /**
         * 人脸移除
         */
        REMOVED
    }

    public static class FaceItemEvent {
        private int index;
        private EventType eventType;

        public FaceItemEvent(int index, EventType eventType) {
            this.index = index;
            this.eventType = eventType;
        }

        public int getIndex() {
            return index;
        }

        public void setIndex(int index) {
            this.index = index;
        }

        public EventType getEventType() {
            return eventType;
        }

        public void setEventType(EventType eventType) {
            this.eventType = eventType;
        }
    }

    private static final String TAG = "RecognizeViewModel";


    private OnRegisterFinishedCallback onRegisterFinishedCallback;

    /**
     * 注册人脸状态码，准备注册
     */
    public static final int REGISTER_STATUS_READY = 0;
    /**
     * 注册人脸状态码，注册中
     */
    public static final int REGISTER_STATUS_PROCESSING = 1;
    /**
     * 注册人脸状态码，注册结束（无论成功失败）
     */
    public static final int REGISTER_STATUS_DONE = 2;

    /**
     * 人脸识别的状态，预设值为：已结束
     */
    private int registerStatus = REGISTER_STATUS_DONE;

    private static final int MAX_DETECT_NUM = 10;
    /**
     * 相机预览的分辨率
     */
    private Camera.Size previewSize;
    /**
     * 用于头像RecyclerView显示的信息
     */
    private MutableLiveData<List<CompareResult>> compareResultList;

    private MutableLiveData<FaceItemEvent> faceItemEventMutableLiveData = new MutableLiveData<>();

    /**
     * 各个引擎初始化的错误码
     */
    private MutableLiveData<Integer> ftInitCode = new MutableLiveData<>();
    private MutableLiveData<Integer> frInitCode = new MutableLiveData<>();
    private MutableLiveData<Integer> flInitCode = new MutableLiveData<>();

    /**
     * 人脸操作辅助类，推帧即可，内部会进行特征提取、识别
     */
    private FaceHelper faceHelper;
    /**
     * VIDEO模式人脸检测引擎，用于预览帧人脸追踪及图像质量检测
     */
    private FaceEngine ftEngine;
    /**
     * 用于特征提取的引擎
     */
    private FaceEngine frEngine;
    /**
     * IMAGE模式活体检测引擎，用于预览帧人脸活体检测
     */
    private FaceEngine flEngine;

    private PreviewConfig previewConfig;

    private MutableLiveData<RecognizeConfiguration> recognizeConfiguration = new MutableLiveData<>();

    private MutableLiveData<String> recognizeNotice = new MutableLiveData<>();

    private MutableLiveData<String> drawRectInfoText = new MutableLiveData<>();

    private MutableLiveData<Object> faceIdAndImagePath = new MutableLiveData<>();

    private MutableLiveData<Long> compareFaceId = new MutableLiveData<>();

    /**
     * 检测ir活体前，是否需要更新faceData
     */
    private boolean needUpdateFaceData;
    /**
     * 当前活体检测的检测类型
     */
    private LivenessType livenessType;

    /**
     * IR活体数据
     */
    private byte[] irNV21 = null;

    /**
     * 人脸库数据加载完成
     */
    private boolean loadFaceList;

    private Disposable registerNv21Disposable;

    public void refreshIrPreviewData(byte[] irPreviewData) {
        irNV21 = irPreviewData;
    }

    /**
     * 设置当前活体检测的检测类型
     *
     * @param liveType 活体检测的检测类型
     */
    public void setLiveType(LivenessType liveType) {
        this.livenessType = liveType;
    }

    public void setRgbFaceRectTransformer(FaceRectTransformer rgbFaceRectTransformer) {
        faceHelper.setRgbFaceRectTransformer(rgbFaceRectTransformer);
    }

    public void setIrFaceRectTransformer(FaceRectTransformer irFaceRectTransformer) {
        faceHelper.setIrFaceRectTransformer(irFaceRectTransformer);
    }


    /**
     * 注册实时NV21数据
     *
     * @param nv21            实时相机预览的NV21数据
     * @param facePreviewInfo 人脸信息
     */
    private void registerFace(final byte[] nv21, FacePreviewInfo facePreviewInfo) {
        updateRegisterStatus(REGISTER_STATUS_PROCESSING);
        registerNv21Disposable = Observable.create((ObservableOnSubscribe<Boolean>) emitter -> {
                    FaceEngine registerEngine = new FaceEngine();
                    int res = registerEngine.init(ArcFaceApplication.getApplication(), DetectMode.ASF_DETECT_MODE_IMAGE, DetectFaceOrientPriority.ASF_OP_0_ONLY,
                            1, FaceEngine.ASF_FACE_RECOGNITION);
                    if (res == ErrorInfo.MOK) {
                        boolean success = FaceServer.getInstance().registerNv21(ArcFaceApplication.getApplication(), nv21.clone(), previewSize.width,
                                previewSize.height, facePreviewInfo, "registered_" + faceHelper.getTrackedFaceCount(), frEngine, registerEngine);
                        Object[] pair = new Object[]{FaceServer.getInstance().getFaceIdNumber(), FaceServer.getInstance().getImageFullPath(), FaceServer.getInstance().getFaceFeature()};
                        faceIdAndImagePath.postValue(pair);
                        registerEngine.unInit();
                        emitter.onNext(success);
                    } else {
                        emitter.onNext(false);
                    }
                    emitter.onComplete();
                })
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Boolean>() {

                    @Override
                    public void onNext(Boolean success) {
                        if (onRegisterFinishedCallback != null) {
                            onRegisterFinishedCallback.onRegisterFinished(facePreviewInfo, success);
                        }
                        updateRegisterStatus(REGISTER_STATUS_DONE);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        if (onRegisterFinishedCallback != null) {
                            onRegisterFinishedCallback.onRegisterFinished(facePreviewInfo, false);
                        }
                        updateRegisterStatus(REGISTER_STATUS_DONE);
                    }

                    @Override
                    public void onComplete() {
                    }
                });

    }

    public MutableLiveData<List<CompareResult>> getCompareResultList() {
        if (compareResultList == null) {
            compareResultList = new MutableLiveData<>();
            compareResultList.setValue(new ArrayList<>());
        }
        return compareResultList;
    }

    /**
     * 初始化引擎
     */
    public void init() {
        Context context = ArcFaceApplication.getApplication();
        boolean switchCamera = ConfigUtil.isSwitchCamera(context);
        previewConfig = new PreviewConfig(
                switchCamera ? Camera.CameraInfo.CAMERA_FACING_FRONT : Camera.CameraInfo.CAMERA_FACING_BACK,
                switchCamera ? Camera.CameraInfo.CAMERA_FACING_BACK : Camera.CameraInfo.CAMERA_FACING_FRONT,
                Integer.parseInt(ConfigUtil.getRgbCameraAdditionalRotation(context)),
                Integer.parseInt(ConfigUtil.getIrCameraAdditionalRotation(context))
        );

        // 填入在设置界面设置好的配置信息
        boolean enableLive = !ConfigUtil.getLivenessDetectType(context).equals(context.getString(R.string.value_liveness_type_disable));
        boolean enableFaceQualityDetect = ConfigUtil.isEnableImageQualityDetect(context);
        boolean enableFaceMoveLimit = ConfigUtil.isEnableFaceMoveLimit(context);
        boolean enableFaceSizeLimit = ConfigUtil.isEnableFaceSizeLimit(context);
        RecognizeConfiguration configuration = new RecognizeConfiguration.Builder()
                .enableFaceMoveLimit(enableFaceMoveLimit)
                .enableFaceSizeLimit(enableFaceSizeLimit)
                .faceSizeLimit(ConfigUtil.getFaceSizeLimit(context))
                .faceMoveLimit(ConfigUtil.getFaceMoveLimit(context))
                .enableLiveness(enableLive)
                .enableImageQuality(enableFaceQualityDetect)
                .maxDetectFaces(ConfigUtil.getRecognizeMaxDetectFaceNum(context))
                .keepMaxFace(ConfigUtil.isKeepMaxFace(context))
                .similarThreshold(ConfigUtil.getRecognizeThreshold(context))
                .imageQualityNoMaskRecognizeThreshold(ConfigUtil.getImageQualityNoMaskRecognizeThreshold(context))
                .imageQualityMaskRecognizeThreshold(ConfigUtil.getImageQualityMaskRecognizeThreshold(context))
                .livenessParam(new LivenessParam(ConfigUtil.getRgbLivenessThreshold(context), ConfigUtil.getIrLivenessThreshold(context),
                        ConfigUtil.getLivenessFqThreshold(context)))
                .build();
        int cameraOffsetX = ConfigUtil.getDualCameraHorizontalOffset(context);
        int cameraOffsetY = ConfigUtil.getDualCameraVerticalOffset(context);
        needUpdateFaceData = (livenessType == LivenessType.IR && (cameraOffsetX != 0 || cameraOffsetY != 0));

        ftEngine = new FaceEngine();
        int ftEngineMask = FaceEngine.ASF_FACE_DETECT | FaceEngine.ASF_MASK_DETECT;
        ftInitCode.postValue(ftEngine.init(context, DetectMode.ASF_DETECT_MODE_VIDEO, ConfigUtil.getFtOrient(context),
                ConfigUtil.getRecognizeMaxDetectFaceNum(context), ftEngineMask));
        FaceAttributeParam attributeParam = new FaceAttributeParam(
                ConfigUtil.getRecognizeEyeOpenThreshold(context), ConfigUtil.getRecognizeMouthCloseThreshold(context),
                ConfigUtil.getRecognizeWearGlassesThreshold(context));
        ftEngine.setFaceAttributeParam(attributeParam);

        frEngine = new FaceEngine();
        int frEngineMask = FaceEngine.ASF_FACE_RECOGNITION;
        if (enableFaceQualityDetect) {
            frEngineMask |= FaceEngine.ASF_IMAGEQUALITY;
        }
        frInitCode.postValue(frEngine.init(context, DetectMode.ASF_DETECT_MODE_IMAGE, DetectFaceOrientPriority.ASF_OP_0_ONLY,
                10, frEngineMask));
        FaceServer.getInstance().initFaceList(context, frEngine, faceCount -> loadFaceList = true, true);

        //启用活体检测时，才初始化活体引擎
        if (enableLive) {
            flEngine = new FaceEngine();
            int flEngineMask = (livenessType == LivenessType.RGB ? FaceEngine.ASF_LIVENESS : (FaceEngine.ASF_IR_LIVENESS | FaceEngine.ASF_FACE_DETECT));
            if (needUpdateFaceData) {
                flEngineMask |= FaceEngine.ASF_UPDATE_FACEDATA;
            }
            flInitCode.postValue(flEngine.init(context, DetectMode.ASF_DETECT_MODE_IMAGE,
                    DetectFaceOrientPriority.ASF_OP_ALL_OUT, 10, flEngineMask));
            LivenessParam livenessParam = new LivenessParam(ConfigUtil.getRgbLivenessThreshold(context), ConfigUtil.getIrLivenessThreshold(context), ConfigUtil.getLivenessFqThreshold(context));
            flEngine.setLivenessParam(livenessParam);
        }

        recognizeConfiguration.setValue(configuration);
    }

    /**
     * 销毁引擎，faceHelper中可能会有特征提取耗时操作仍在执行，加锁防止crash
     */
    private void unInit() {
        if (ftEngine != null) {
            synchronized (ftEngine) {
                int ftUnInitCode = ftEngine.unInit();
                Log.i(TAG, "unInitEngine: " + ftUnInitCode);
            }
        }
        if (frEngine != null) {
            synchronized (frEngine) {
                int frUnInitCode = frEngine.unInit();
                Log.i(TAG, "unInitEngine: " + frUnInitCode);
            }
        }
        if (flEngine != null) {
            synchronized (flEngine) {
                int flUnInitCode = flEngine.unInit();
                Log.i(TAG, "unInitEngine: " + flUnInitCode);
            }
        }
    }

    /**
     * 删除已经离开的人脸
     *
     * @param facePreviewInfoList 人脸和trackId列表
     */
    public void clearLeftFace(List<FacePreviewInfo> facePreviewInfoList) {
        List<CompareResult> compareResults = compareResultList.getValue();
        if (compareResults != null) {
            for (int i = compareResults.size() - 1; i >= 0; i--) {
                boolean contains = false;
                for (FacePreviewInfo facePreviewInfo : facePreviewInfoList) {
                    if (facePreviewInfo.getTrackId() == compareResults.get(i).getTrackId()) {
                        contains = true;
                        break;
                    }
                }
                if (!contains) {
                    compareResults.remove(i);
                    getFaceItemEventMutableLiveData().postValue(new FaceItemEvent(i, EventType.REMOVED));
                }
            }
        }
    }

    /**
     * 释放操作
     */
    public void destroy() {
        unInit();
        if (faceHelper != null) {
            ConfigUtil.setTrackedFaceCount(ArcFaceApplication.getApplication().getApplicationContext(), faceHelper.getTrackedFaceCount());
            faceHelper.release();
            faceHelper = null;
        }
        FaceServer.getInstance().release();
        if (registerNv21Disposable != null) {
            registerNv21Disposable.dispose();
            registerNv21Disposable = null;
        }
    }

    /**
     * 当相机打开时由activity调用，进行一些初始化操作
     *
     * @param camera 相机实例
     */
    public void onRgbCameraOpened(Camera camera) {
        Camera.Size lastPreviewSize = previewSize;
        previewSize = camera.getParameters().getPreviewSize();
        // 切换相机的时候可能会导致预览尺寸发生变化
        initFaceHelper(lastPreviewSize);
    }

    /**
     * 当相机打开时由activity调用，进行一些初始化操作
     *
     * @param camera 相机实例
     */
    public void onIrCameraOpened(Camera camera) {
        Camera.Size lastPreviewSize = previewSize;
        previewSize = camera.getParameters().getPreviewSize();
        // 切换相机的时候可能会导致预览尺寸发生变化
        initFaceHelper(lastPreviewSize);
    }

    private void initFaceHelper(Camera.Size lastPreviewSize) {
        if (faceHelper == null || lastPreviewSize == null ||
                lastPreviewSize.width != previewSize.width || lastPreviewSize.height != previewSize.height) {
            Integer trackedFaceCount = null;
            // 记录切换时的人脸序号
            if (faceHelper != null) {
                trackedFaceCount = faceHelper.getTrackedFaceCount();
                faceHelper.release();
            }
            Context context = ArcFaceApplication.getApplication().getApplicationContext();
            int horizontalOffset = ConfigUtil.getDualCameraHorizontalOffset(context);
            int verticalOffset = ConfigUtil.getDualCameraVerticalOffset(context);
            int maxDetectFaceNum = ConfigUtil.getRecognizeMaxDetectFaceNum(context);
            faceHelper = new FaceHelper.Builder()
                    .ftEngine(ftEngine)
                    .frEngine(frEngine)
                    .flEngine(flEngine)
                    .needUpdateFaceData(needUpdateFaceData)
                    .frQueueSize(maxDetectFaceNum)
                    .flQueueSize(maxDetectFaceNum)
                    .previewSize(previewSize)
                    .recognizeCallback(this)
                    .recognizeConfiguration(recognizeConfiguration.getValue())
                    .trackedFaceCount(trackedFaceCount == null ? ConfigUtil.getTrackedFaceCount(context) : trackedFaceCount)
                    .dualCameraFaceInfoTransformer(faceInfo -> {
                        FaceInfo irFaceInfo = new FaceInfo(faceInfo);
                        irFaceInfo.getRect().offset(horizontalOffset, verticalOffset);
                        return irFaceInfo;
                    })
                    .build();
        }
    }

    @Override
    public void onRecognized(CompareResult compareResult, Integer live, boolean similarPass) {
        Disposable disposable = Observable.just(true).observeOn(AndroidSchedulers.mainThread()).subscribe(aBoolean -> {
            if (similarPass) {
                boolean isAdded = false;
                List<CompareResult> compareResults = compareResultList.getValue();
                if (compareResults != null && !compareResults.isEmpty()) {
                    for (CompareResult compareResult1 : compareResults) {
                        if (compareResult1.getTrackId() == compareResult.getTrackId()) {
                            isAdded = true;
                            break;
                        }
                    }
                }
                // 传递识别出的人脸ID
                compareFaceId.postValue(FaceServer.getInstance().getCompareFaceIdNumber());
                if (!isAdded) {
                    //对于多人脸搜索，假如最大显示数量为 MAX_DETECT_NUM 且有新的人脸进入，则以队列的形式移除
                    if (compareResults != null && compareResults.size() >= MAX_DETECT_NUM) {
                        compareResults.remove(0);
                        getFaceItemEventMutableLiveData().postValue(new FaceItemEvent(0, EventType.REMOVED));
                    }
                    if (compareResults != null) {
                        compareResults.add(compareResult);
                        getFaceItemEventMutableLiveData().postValue(new FaceItemEvent(compareResults.size() - 1, EventType.INSERTED));
                    }
                }
            }
        });
    }

    @Override
    public void onNoticeChanged(String notice) {
        if (recognizeNotice != null) {
            recognizeNotice.postValue(notice);
        }
    }

    public void setDrawRectInfoTextValue(boolean openDrawRect) {
        String stringDrawText = openDrawRect ? "关闭绘制" : "开启绘制";
        if (drawRectInfoText != null) {
            drawRectInfoText.postValue(stringDrawText);
        }
    }

    /**
     * 设置实时注册的结果回调
     *
     * @param onRegisterFinishedCallback 实时注册的结果回调
     */
    public void setOnRegisterFinishedCallback(OnRegisterFinishedCallback onRegisterFinishedCallback) {
        this.onRegisterFinishedCallback = onRegisterFinishedCallback;
    }

    public MutableLiveData<Integer> getFtInitCode() {
        return ftInitCode;
    }

    public MutableLiveData<Integer> getFrInitCode() {
        return frInitCode;
    }

    public MutableLiveData<Integer> getFlInitCode() {
        return flInitCode;
    }

    public MutableLiveData<String> getRecognizeNotice() {
        return recognizeNotice;
    }

    public MutableLiveData<String> getDrawRectInfoText() {
        return drawRectInfoText;
    }

    public MutableLiveData<Object> getFaceIdAndImagePath() {return faceIdAndImagePath;}

    public MutableLiveData<Long> getCompareFaceId() { return compareFaceId;}

    public MutableLiveData<FaceItemEvent> getFaceItemEventMutableLiveData() {
        return faceItemEventMutableLiveData;
    }

    /**
     * 准备注册，将注册的状态值修改为待注册
     */
    public void prepareRegister() {
        if (registerStatus == REGISTER_STATUS_DONE) {
            updateRegisterStatus(REGISTER_STATUS_READY);
        }
    }

    private void updateRegisterStatus(int status) {
        registerStatus = status;
    }

    /**
     * 根据预览信息生成绘制信息
     *
     * @param facePreviewInfoList 预览信息
     * @return 绘制信息
     */
    public List<FaceRectView.DrawInfo> getDrawInfo(List<FacePreviewInfo> facePreviewInfoList, LivenessType livenessType, boolean drawRectInfo) {
        List<FaceRectView.DrawInfo> drawInfoList = new ArrayList<>();
        for (int i = 0; i < facePreviewInfoList.size(); i++) {
            int trackId = facePreviewInfoList.get(i).getTrackId();
            String name = faceHelper.getName(trackId);
            Integer liveness = faceHelper.getLiveness(trackId);
            Integer recognizeStatus = faceHelper.getRecognizeStatus(trackId);

            // 根据识别结果和活体结果设置颜色
            int color = RecognizeColor.COLOR_UNKNOWN;
            if (recognizeStatus != null) {
                if (recognizeStatus == RequestFeatureStatus.FAILED) {
                    color = RecognizeColor.COLOR_FAILED;
                }
                if (recognizeStatus == RequestFeatureStatus.SUCCEED) {
                    color = RecognizeColor.COLOR_SUCCESS;
                }
            }
            if (liveness != null && liveness == LivenessInfo.NOT_ALIVE) {
                color = RecognizeColor.COLOR_FAILED;
            }

            drawInfoList.add(new FaceRectView.DrawInfo(
                    livenessType == LivenessType.RGB ? facePreviewInfoList.get(i).getRgbTransformedRect() : facePreviewInfoList.get(i).getIrTransformedRect(),
                    GenderInfo.UNKNOWN, AgeInfo.UNKNOWN_AGE, liveness == null ? LivenessInfo.UNKNOWN : liveness, color,
                    name == null ? "" : name, facePreviewInfoList.get(i).getFaceInfoRgb().getIsWithinBoundary(),
                    facePreviewInfoList.get(i).getForeRect(), facePreviewInfoList.get(i).getFaceInfoRgb().getFaceAttributeInfo(), drawRectInfo,
                    livenessType == LivenessType.RGB));
        }
        return drawInfoList;
    }


    /**
     * 传入可见光相机预览数据
     *
     * @param nv21        可见光相机预览数据
     * @param doRecognize 是否进行识别
     * @return 当前帧的检测结果信息
     */
    public List<FacePreviewInfo> onPreviewFrame(byte[] nv21, boolean doRecognize) {
        if (faceHelper != null) {
            if (!loadFaceList) {
                return null;
            }
            if (livenessType == LivenessType.IR && irNV21 == null) {
                return null;
            }
            List<FacePreviewInfo> facePreviewInfoList = faceHelper.onPreviewFrame(nv21, irNV21, doRecognize);
            if (registerStatus == REGISTER_STATUS_READY && !facePreviewInfoList.isEmpty()) {
                FacePreviewInfo facePreviewInfo = facePreviewInfoList.get(0);
                if (facePreviewInfo.getMask() != MaskInfo.WORN) {
                    registerFace(nv21, facePreviewInfoList.get(0));
                } else {
                    Toast.makeText(ArcFaceApplication.getApplication(), "注册照要求不戴口罩", Toast.LENGTH_SHORT).show();
                    updateRegisterStatus(REGISTER_STATUS_DONE);
                }
            }
            return facePreviewInfoList;
        }
        return null;
    }

    /**
     * 设置可识别区域（相对于View）
     *
     * @param recognizeArea 可识别区域
     */
    public void setRecognizeArea(Rect recognizeArea) {
        if (faceHelper != null) {
            faceHelper.setRecognizeArea(recognizeArea);
        }
    }

    public MutableLiveData<RecognizeConfiguration> getRecognizeConfiguration() {
        return recognizeConfiguration;
    }

    public PreviewConfig getPreviewConfig() {
        return previewConfig;
    }

    public Point loadPreviewSize() {
        String[] size = ConfigUtil.getPreviewSize(ArcFaceApplication.getApplication()).split("x");
        return new Point(Integer.parseInt(size[0]), Integer.parseInt(size[1]));
    }
}
