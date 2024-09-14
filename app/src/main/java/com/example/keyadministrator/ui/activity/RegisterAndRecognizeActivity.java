package com.example.keyadministrator.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Point;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.arcsoft.face.ErrorInfo;
import com.example.keyadministrator.AddUserActivity;
import com.example.keyadministrator.CardLoginActivity;
import com.example.keyadministrator.DatabaseHelper;
import com.example.keyadministrator.ImageUtils;
import com.example.keyadministrator.MediaMp3Player;
import com.example.keyadministrator.MenuListActivity;
import com.example.keyadministrator.QueryFaceTask;
import com.example.keyadministrator.R;
import com.example.keyadministrator.SocketClient;
import com.example.keyadministrator.User;
import com.example.keyadministrator.UserControl;
import com.example.keyadministrator.UserDetailEditActivity;
import com.example.keyadministrator.UserLoginActivity;
import com.example.keyadministrator.databinding.ActivityRegisterAndRecognizeBinding;
import com.example.keyadministrator.facedb.dao.FaceDao;
import com.example.keyadministrator.ui.model.PreviewConfig;
import com.example.keyadministrator.ui.viewmodel.RecognizeViewModel;
import com.example.keyadministrator.util.ConfigUtil;
import com.example.keyadministrator.util.ErrorCodeUtil;
import com.example.keyadministrator.util.FaceRectTransformer;
import com.example.keyadministrator.util.camera.CameraListener;
import com.example.keyadministrator.util.camera.DualCameraHelper;
import com.example.keyadministrator.util.face.constants.LivenessType;
import com.example.keyadministrator.util.face.model.FacePreviewInfo;
import com.example.keyadministrator.widget.FaceRectView;
import com.example.keyadministrator.widget.RecognizeAreaView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class RegisterAndRecognizeActivity extends BaseActivity implements ViewTreeObserver.OnGlobalLayoutListener {
    private static final String TAG = "RegisterAndRecognize";
    private static final int ACTION_REQUEST_PERMISSIONS = 0x001;
    private static final int NAVIGATE_TO_RECOGNIZE_SETTINGS_ACTIVITY = 1;
    private static final int NAVIGATE_TO_RECOGNIZE_DEBUG_ACTIVITY = 2;
    /**
     * 所需的所有权限信息
     */
    private static final String[] NEEDED_PERMISSIONS = new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_PHONE_STATE};
    private final DatabaseHelper dbHelper = new DatabaseHelper(this);
    private FaceDao faceDao;
    private QueryFaceTask queryFaceTask;
    int actionAfterFinish = 0;
    private MediaPlayer mediaPlayer;
    private DualCameraHelper rgbCameraHelper;
    private DualCameraHelper irCameraHelper;
    private FaceRectTransformer rgbFaceRectTransformer;
    private FaceRectTransformer irFaceRectTransformer;
    private ActivityRegisterAndRecognizeBinding binding;
    private RecognizeViewModel recognizeViewModel;
    private LivenessType livenessType;
    private boolean enableLivenessDetect = false;
    private RecognizeAreaView recognizeAreaView;
    private TextView textViewRgb;
    private TextView textViewIr;
    private boolean openRectInfoDraw;
    private SocketClient socketClient;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_register_and_recognize);
        // 加载语音
        mediaPlayer = MediaPlayer.create(this, R.raw.tips);
        //保持亮屏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WindowManager.LayoutParams attributes = getWindow().getAttributes();
            attributes.systemUiVisibility = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            getWindow().setAttributes(attributes);
        }


        queryFaceTask = new QueryFaceTask(faceDao);
        socketClient = SocketClient.getInstance();

        // Activity启动后就锁定为启动时的方向
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);
        initButton();
        initData();
        initViewModel();
        initView();
        // 语音播报
        mediaPlayer.start();
        openRectInfoDraw = true;
        recognizeViewModel.setDrawRectInfoTextValue(true);

    }

    // 根据跳转的页面不同，展示的按钮也不同
    private void initButton() {
        Button button1 = findViewById(R.id.open_rect_info_draw);
        Button button2 = findViewById(R.id.register);
        Button button3 = findViewById(R.id.setting);
        Button button4 = findViewById(R.id.recognize_debug);
        Button button5 = findViewById(R.id.return_back);
        String loginType = getIntent().getStringExtra("loginType");
        String facialRec = getIntent().getStringExtra("facialRec");
        String registerRec = getIntent().getStringExtra("registerRec");
        String editRec = getIntent().getStringExtra("editRec");
        if (Objects.equals(loginType, "FaceRectView")) {
            button1.setVisibility(View.GONE);
            button2.setVisibility(View.GONE);
            button3.setVisibility(View.GONE);
            button4.setVisibility(View.GONE);
            button5.setVisibility(View.VISIBLE);
            button5.setOnClickListener(v->{
                finish();
            });
        }
        if (Objects.equals(facialRec, "BackMenu")) {
            button5.setVisibility(View.GONE);
        }
        if (Objects.equals(registerRec, "BackRegister")) {
            button1.setVisibility(View.GONE);
            button4.setVisibility(View.GONE);
            button5.setVisibility(View.GONE);
        }
        if (Objects.equals(editRec, "BackEditRegister")) {
            button1.setVisibility(View.GONE);
            button4.setVisibility(View.GONE);
            button5.setVisibility(View.VISIBLE);
            button5.setOnClickListener(v->{
                int getUserId = getIntent().getIntExtra("userId", -1);
                // 点击通过后跳转
                Intent intent = new Intent(this, UserDetailEditActivity.class);
                intent.putExtra("user_id", getUserId);
                startActivity(intent);
                finish();
            });
        }
    }

    private void initData() {
        String livenessTypeStr = ConfigUtil.getLivenessDetectType(this);
        if (livenessTypeStr.equals((getString(R.string.value_liveness_type_rgb)))) {
            livenessType = LivenessType.RGB;
        } else if (livenessTypeStr.equals(getString(R.string.value_liveness_type_ir))) {
            livenessType = LivenessType.IR;
        } else {
            livenessType = null;
        }
        enableLivenessDetect = !ConfigUtil.getLivenessDetectType(this).equals(getString(R.string.value_liveness_type_disable));
    }

    private void initViewModel() {
        recognizeViewModel = new ViewModelProvider(getViewModelStore(), new ViewModelProvider.AndroidViewModelFactory(getApplication())).get(RecognizeViewModel.class);

        recognizeViewModel.setLiveType(livenessType);

        recognizeViewModel.getFtInitCode().observe(this, ftInitCode -> {
            if (ftInitCode != ErrorInfo.MOK) {
                String error = getString(R.string.specific_engine_init_failed, "ftEngine", ftInitCode, ErrorCodeUtil.arcFaceErrorCodeToFieldName(ftInitCode));
                Log.i(TAG, "initEngine: " + error);
                showToast(error);
            }
        });
        recognizeViewModel.getFrInitCode().observe(this, frInitCode -> {
            if (frInitCode != ErrorInfo.MOK) {
                String error = getString(R.string.specific_engine_init_failed, "frEngine", frInitCode, ErrorCodeUtil.arcFaceErrorCodeToFieldName(frInitCode));
                Log.i(TAG, "initEngine: " + error);
                showToast(error);
            }
        });
        recognizeViewModel.getFlInitCode().observe(this, flInitCode -> {
            if (flInitCode != ErrorInfo.MOK) {
                String error = getString(R.string.specific_engine_init_failed, "flEngine", flInitCode, ErrorCodeUtil.arcFaceErrorCodeToFieldName(flInitCode));
                Log.i(TAG, "initEngine: " + error);
                showToast(error);
            }
        });
        recognizeViewModel.getFaceItemEventMutableLiveData().observe(this, faceItemEvent -> {
            RecyclerView.Adapter adapter = binding.dualCameraRecyclerViewPerson.getAdapter();
            switch (faceItemEvent.getEventType()) {
                case REMOVED:
                    if (adapter != null) {
                        adapter.notifyItemRemoved(faceItemEvent.getIndex());
                    }
                    break;
                case INSERTED:
                    if (adapter != null) {
                        adapter.notifyItemInserted(faceItemEvent.getIndex());
                    }
                    break;
                default:
                    break;
            }
        });

        recognizeViewModel.getRecognizeConfiguration().observe(this, recognizeConfiguration -> {
            Log.i(TAG, "initViewModel recognizeConfiguration: " + recognizeConfiguration.toString());
        });

        recognizeViewModel.setOnRegisterFinishedCallback((facePreviewInfo, success) -> {
            showToast(success ? "注册成功" : "注册失败");
        });

        recognizeViewModel.getRecognizeNotice().observe(this, notice -> {
            binding.setRecognizeNotice(notice);
        });

        recognizeViewModel.getDrawRectInfoText().observe(this, drawRectInfoText -> {
            binding.setDrawRectInfoText(drawRectInfoText);
        });
        // 监测获取的头像id和图片路径
        recognizeViewModel.getFaceIdAndImagePath().observe(this, faceIdAndImagePath -> {
            String registerRec = getIntent().getStringExtra("registerRec");
            String editRec = getIntent().getStringExtra("editRec");
            if (faceIdAndImagePath instanceof Object[]) {
                Object[] faceIdAndImagePathArray = (Object[]) faceIdAndImagePath;
                if (Objects.equals(registerRec, "BackRegister")){
                    boolean ck0 = getIntent().getBooleanExtra("ck0status", false);
                    boolean ck1 = getIntent().getBooleanExtra("ck1status", false);
                    boolean ck2 = getIntent().getBooleanExtra("ck2status", false);
                    boolean ck3 = getIntent().getBooleanExtra("ck3status", false);
                    boolean ck4 = getIntent().getBooleanExtra("ck4status", false);
                    boolean ck5 = getIntent().getBooleanExtra("ck5status", false);
                    int ck0Int = ck0 ? 1 : 0;
                    int ck1Int = ck1 ? 1 : 0;
                    int ck2Int = ck2 ? 1 : 0;
                    int ck3Int = ck3 ? 1 : 0;
                    int ck4Int = ck4 ? 1 : 0;
                    int ck5Int = ck5 ? 1 : 0;
                    String getUserName = getIntent().getStringExtra("userName");
                    String getUserPart = getIntent().getStringExtra("partName");
                    String getUserPhone = getIntent().getStringExtra("phoneNum");
                    String getUserPassword = getIntent().getStringExtra("password");
                    String getUserCardId = getIntent().getStringExtra("cardId");
                    long saveFaceId = (Long) faceIdAndImagePathArray[0];
                    String saveFaceIdString = String.valueOf(saveFaceId);
                    String saveFacePath = (String) faceIdAndImagePathArray[1];
                    byte[] getFeatureData = (byte[]) faceIdAndImagePathArray[2];
                    // 通过人脸识别获得的唯一faceId传递回去以用来完成注册
                    dbHelper.addUser(getUserName, getUserPassword, getUserPart, getUserPhone, saveFaceIdString, getUserCardId, ck0Int, ck1Int, ck2Int, ck3Int, ck4Int, ck5Int, saveFacePath, null, null, 1);
                    try {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                        // 创建内部data JSON对象并添加Ids数组
                        JSONObject innerData = new JSONObject();
                        innerData.put("userName", getUserName);
                        innerData.put("nickName", "");
                        innerData.put("email", "");
                        innerData.put("remark", "");
                        innerData.put("department", getUserPart);
                        innerData.put("phonenumber", getUserPhone);
                        String base64String = ImageUtils.imageToBase64(saveFacePath);
                        innerData.put("avatar", base64String);
                        innerData.put("sex", 0);
                        innerData.put("password", getUserPassword);
                        innerData.put("cardnumber", getUserCardId);
                        innerData.put("faceData", getFeatureData);
                        socketClient.sendParameterData(dateFormat.format(new Date()), "add_user", innerData.toString());
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }else if(Objects.equals(editRec, "BackEditRegister")) {
                    int getUserId = getIntent().getIntExtra("userId", -1);
                    User registeredUser;
                    long saveFaceId = (Long) faceIdAndImagePathArray[0];
                    String saveFacePath = (String) faceIdAndImagePathArray[1];
                    registeredUser = dbHelper.getUserById(getUserId);
                    registeredUser.setFaceId(String.valueOf(saveFaceId));
                    registeredUser.setImagePath(saveFacePath);
                    dbHelper.updateUser(registeredUser);
                }
            } else {
                Log.e(TAG, "faceIdAndImagePath is not an array");
            }
        });
        // 监测识别后获取的人脸id
        recognizeViewModel.getCompareFaceId().observe(this, compareFaceId -> {
            String stringFaceValue = String.valueOf(compareFaceId);
            User user = dbHelper.getUserByIdOrFaceId(stringFaceValue);
            String loginType = getIntent().getStringExtra("loginType");
            String facialRec = getIntent().getStringExtra("facialRec");
            String registerRec = getIntent().getStringExtra("registerRec");
            String editRec = getIntent().getStringExtra("editRec");
            String globalImagePath = getIntent().getStringExtra("imagePath");
            int getUserId = getIntent().getIntExtra("userId", -1);
            int code = -1;

            // 根据识别情况不同跳转到不同界面，人脸登录后应该跳转到用户借用主界面
            if (Objects.equals(loginType, "FaceRectView") && !stringFaceValue.isEmpty()) {
                try{
                    if (user.getEnabled() == 1) {
                        if (user.getStartTime() != null && !user.getStartTime().isEmpty() && user.getEndTime() != null && !user.getEndTime().isEmpty()) {
                            Date startDate = dateFormat.parse(user.getStartTime()); // 获取开始时间
                            Date endDate = dateFormat.parse(user.getEndTime());; // 获取结束时间
                            boolean isBetween = isCurrentTimeBetween(startDate, endDate);
                            if (!isBetween) {
                                code = user.isCk0();
                                Intent intent = new Intent(this, UserLoginActivity.class);
                                intent.putExtra("userId", user.getId());
                                intent.putExtra("userAdm", code);
                                startActivity(intent);
                                finish();
                            }else{
                                showToast("在该时间段用户没有权限");
                            }
                        }else{
                            code = user.isCk0();
                            Intent intent = new Intent(this, UserLoginActivity.class);
                            intent.putExtra("userId", user.getId());
                            intent.putExtra("userAdm", code);
                            startActivity(intent);
                            finish();
                        }
                    }else{
                        showToast("该用户已被禁用");
                    }
                }catch(Exception e){
                    showToast("用户数据存在异常，请检查");
                }
            }
            // 根据识别情况不同跳转到不同界面，人脸登录后应该跳转到用户注册后返回菜单选择页面
            if (Objects.equals(facialRec, "BackMenu") && !stringFaceValue.isEmpty()) {
                // 点击通过后跳转
                Intent intent = new Intent(this, MenuListActivity.class);
                startActivity(intent);
                finish();
            }
            // 根据识别情况不同跳转到不同界面，人脸登录后应该跳转到用户注册后返回添加用户页面
            if (Objects.equals(registerRec, "BackRegister") && !stringFaceValue.isEmpty()) {
                // 点击通过后跳转
                Intent intent = new Intent(this, UserControl.class);
                startActivity(intent);
                finish();
            }
            // 根据识别情况不同跳转到不同界面，人脸登录后应该跳转到用户注册后返回编辑用户页面
            if (Objects.equals(editRec, "BackEditRegister") && !stringFaceValue.isEmpty() && globalImagePath == null) {
                // 点击通过后跳转
                Intent intent = new Intent(this, UserDetailEditActivity.class);
                intent.putExtra("user_id", getUserId);
                startActivity(intent);
                finish();
            }
        });
    }

    private boolean isCurrentTimeBetween(Date startDate, Date endDate) {
        Date currentTime = new Date();
        return currentTime.after(startDate) && currentTime.before(endDate);
    }

    private void initView() {
        if (!DualCameraHelper.hasDualCamera() || livenessType != LivenessType.IR) {
            binding.flRecognizeIr.setVisibility(View.GONE);
        }
        //在布局结束后才做初始化操作
        binding.dualCameraTexturePreviewRgb.getViewTreeObserver().addOnGlobalLayoutListener(this);
        binding.setCompareResultList(recognizeViewModel.getCompareResultList().getValue());
    }

    @Override
    protected void onDestroy() {
        if (irCameraHelper != null) {
            irCameraHelper.release();
            irCameraHelper = null;
        }

        if (rgbCameraHelper != null) {
            rgbCameraHelper.release();
            rgbCameraHelper = null;
        }

        recognizeViewModel.destroy();
        switch (actionAfterFinish) {
            case NAVIGATE_TO_RECOGNIZE_DEBUG_ACTIVITY:
                navigateToNewPage(RecognizeDebugActivity.class);
                break;
            case NAVIGATE_TO_RECOGNIZE_SETTINGS_ACTIVITY:
                navigateToNewPage(RecognizeSettingsActivity.class);
                break;
            default:
                break;
        }
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (dbHelper != null) {
            dbHelper.close();
        }
        // 关闭后台任务线程
        if (queryFaceTask != null) {
            queryFaceTask.cancel();
        }
    }


    /**
     * 调整View的宽高，使2个预览同时显示
     *
     * @param previewView        显示预览数据的view
     * @param faceRectView       画框的view
     * @param previewSize        预览大小
     * @param displayOrientation 相机旋转角度
     * @return 调整后的LayoutParams
     */
    private ViewGroup.LayoutParams adjustPreviewViewSize(View rgbPreview, View previewView, FaceRectView faceRectView, Camera.Size previewSize, int displayOrientation, float scale) {
        ViewGroup.LayoutParams layoutParams = previewView.getLayoutParams();
        int measuredWidth = previewView.getMeasuredWidth();
        int measuredHeight = previewView.getMeasuredHeight();
        float ratio = ((float) previewSize.height) / (float) previewSize.width;
        if (ratio > 1) {
            ratio = 1 / ratio;
        }
        if (displayOrientation % 180 == 0) {
            layoutParams.width = measuredWidth;
            layoutParams.height = (int) (measuredWidth * ratio);
        } else {
            layoutParams.height = measuredHeight;
            layoutParams.width = (int) (measuredHeight * ratio);
        }
        if (scale < 1f) {
            ViewGroup.LayoutParams rgbParam = rgbPreview.getLayoutParams();
            layoutParams.width = (int) (rgbParam.width * scale);
            layoutParams.height = (int) (rgbParam.height * scale);
        } else {
            layoutParams.width *= scale;
            layoutParams.height *= scale;
        }

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        if (layoutParams.width >= metrics.widthPixels) {
            float viewRatio = layoutParams.width / ((float) metrics.widthPixels);
            layoutParams.width /= viewRatio;
            layoutParams.height /= viewRatio;
        }
        if (layoutParams.height >= metrics.heightPixels) {
            float viewRatio = layoutParams.height / ((float) metrics.heightPixels);
            layoutParams.width /= viewRatio;
            layoutParams.height /= viewRatio;
        }

        previewView.setLayoutParams(layoutParams);
        faceRectView.setLayoutParams(layoutParams);
        return layoutParams;
    }

    private void initRgbCamera() {
        CameraListener cameraListener = new CameraListener() {
            @Override
            public void onCameraOpened(Camera camera, int cameraId, int displayOrientation, boolean isMirror) {
                runOnUiThread(() -> {
                    Camera.Size previewSizeRgb = camera.getParameters().getPreviewSize();
                    ViewGroup.LayoutParams layoutParams = adjustPreviewViewSize(binding.dualCameraTexturePreviewRgb, binding.dualCameraTexturePreviewRgb, binding.dualCameraFaceRectView, previewSizeRgb, displayOrientation, 1.0f);
                    rgbFaceRectTransformer = new FaceRectTransformer(previewSizeRgb.width, previewSizeRgb.height, layoutParams.width, layoutParams.height, displayOrientation, cameraId, isMirror, ConfigUtil.isDrawRgbRectHorizontalMirror(RegisterAndRecognizeActivity.this), ConfigUtil.isDrawRgbRectVerticalMirror(RegisterAndRecognizeActivity.this));

                    FrameLayout parentView = ((FrameLayout) binding.dualCameraTexturePreviewRgb.getParent());
                    if (textViewRgb == null) {
                        textViewRgb = new TextView(RegisterAndRecognizeActivity.this, null);
                    } else {
                        parentView.removeView(textViewRgb);
                    }
                    textViewRgb.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                    textViewRgb.setText(getString(R.string.camera_rgb_preview_size, previewSizeRgb.width, previewSizeRgb.height));
                    textViewRgb.setTextColor(Color.WHITE);
                    textViewRgb.setBackgroundColor(getResources().getColor(R.color.color_black_shadow));
                    parentView.addView(textViewRgb);
                    // 父View宽度和子View一致，保持居中
                    ViewGroup.LayoutParams parentLayoutParams = parentView.getLayoutParams();
                    parentLayoutParams.width = layoutParams.width;
                    parentView.setLayoutParams(parentLayoutParams);

                    // 添加recognizeAreaView，在识别区域发生变更时，更新数据给FaceHelper
                    if (ConfigUtil.isRecognizeAreaLimited(RegisterAndRecognizeActivity.this)) {
                        if (recognizeAreaView == null) {
                            recognizeAreaView = new RecognizeAreaView(RegisterAndRecognizeActivity.this);
                            recognizeAreaView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                        } else {
                            parentView.removeView(recognizeAreaView);
                        }
                        recognizeAreaView.setOnRecognizeAreaChangedListener(recognizeArea -> recognizeViewModel.setRecognizeArea(recognizeArea));
                        parentView.addView(recognizeAreaView);
                    }

                    recognizeViewModel.onRgbCameraOpened(camera);
                    recognizeViewModel.setRgbFaceRectTransformer(rgbFaceRectTransformer);
                });
            }


            @Override
            public void onPreview(final byte[] nv21, Camera camera) {
                binding.dualCameraFaceRectView.clearFaceInfo();
                List<FacePreviewInfo> facePreviewInfoList = recognizeViewModel.onPreviewFrame(nv21, true);
                if (facePreviewInfoList != null && rgbFaceRectTransformer != null) {
                    drawPreviewInfo(facePreviewInfoList);
                }
                recognizeViewModel.clearLeftFace(facePreviewInfoList);
            }

            @Override
            public void onCameraClosed() {
                Log.i(TAG, "onCameraClosed: ");
            }

            @Override
            public void onCameraError(Exception e) {
                Log.i(TAG, "onCameraError: " + e.getMessage());
                e.printStackTrace();
            }

            @Override
            public void onCameraConfigurationChanged(int cameraID, int displayOrientation) {
                Log.i(TAG, "onCameraConfigurationChanged:" + Thread.currentThread().getName());
                if (rgbFaceRectTransformer != null) {
                    rgbFaceRectTransformer.setCameraDisplayOrientation(displayOrientation);
                }
                Log.i(TAG, "onCameraConfigurationChanged: " + cameraID + "  " + displayOrientation);
            }
        };

        PreviewConfig previewConfig = recognizeViewModel.getPreviewConfig();
        rgbCameraHelper = new DualCameraHelper.Builder().previewViewSize(new Point(binding.dualCameraTexturePreviewRgb.getMeasuredWidth(), binding.dualCameraTexturePreviewRgb.getMeasuredHeight())).rotation(getWindowManager().getDefaultDisplay().getRotation()).additionalRotation(previewConfig.getRgbAdditionalDisplayOrientation()).previewSize(recognizeViewModel.loadPreviewSize()).specificCameraId(previewConfig.getRgbCameraId()).isMirror(ConfigUtil.isDrawRgbPreviewHorizontalMirror(this)).previewOn(binding.dualCameraTexturePreviewRgb).cameraListener(cameraListener).build();
        rgbCameraHelper.init();
        rgbCameraHelper.start();
    }

    /**
     * 初始化红外相机，若活体检测类型是可见光活体检测或不启用活体，则不需要启用
     */
    private void initIrCamera() {
        if (livenessType == LivenessType.RGB || !enableLivenessDetect) {
            return;
        }
        CameraListener irCameraListener = new CameraListener() {
            @Override
            public void onCameraOpened(Camera camera, int cameraId, int displayOrientation, boolean isMirror) {
                Camera.Size previewSizeIr = camera.getParameters().getPreviewSize();
                ViewGroup.LayoutParams layoutParams = adjustPreviewViewSize(binding.dualCameraTexturePreviewRgb, binding.dualCameraTexturePreviewIr, binding.dualCameraFaceRectViewIr, previewSizeIr, displayOrientation, 0.25f);

                irFaceRectTransformer = new FaceRectTransformer(previewSizeIr.width, previewSizeIr.height, layoutParams.width, layoutParams.height, displayOrientation, cameraId, isMirror, ConfigUtil.isDrawIrRectHorizontalMirror(RegisterAndRecognizeActivity.this), ConfigUtil.isDrawIrRectVerticalMirror(RegisterAndRecognizeActivity.this));

                FrameLayout parentView = ((FrameLayout) binding.dualCameraTexturePreviewIr.getParent());
                if (textViewIr == null) {
                    textViewIr = new TextView(RegisterAndRecognizeActivity.this, null);
                } else {
                    parentView.removeView(textViewIr);
                }
                textViewIr.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                textViewIr.setText(getString(R.string.camera_ir_preview_size, previewSizeIr.width, previewSizeIr.height));
                textViewIr.setTextColor(Color.WHITE);
                textViewIr.setBackgroundColor(getResources().getColor(R.color.color_black_shadow));
                parentView.addView(textViewIr);

                recognizeViewModel.onIrCameraOpened(camera);
                recognizeViewModel.setIrFaceRectTransformer(irFaceRectTransformer);
            }


            @Override
            public void onPreview(final byte[] nv21, Camera camera) {
                recognizeViewModel.refreshIrPreviewData(nv21);
            }

            @Override
            public void onCameraClosed() {
                Log.i(TAG, "onCameraClosed: ");
            }

            @Override
            public void onCameraError(Exception e) {
                Log.i(TAG, "onCameraError: " + e.getMessage());
                e.printStackTrace();
            }

            @Override
            public void onCameraConfigurationChanged(int cameraID, int displayOrientation) {
                if (irFaceRectTransformer != null) {
                    irFaceRectTransformer.setCameraDisplayOrientation(displayOrientation);
                }
                Log.i(TAG, "onCameraConfigurationChanged: " + cameraID + "  " + displayOrientation);
            }
        };

        PreviewConfig previewConfig = recognizeViewModel.getPreviewConfig();
        irCameraHelper = new DualCameraHelper.Builder().previewViewSize(new Point(binding.dualCameraTexturePreviewIr.getMeasuredWidth(), binding.dualCameraTexturePreviewIr.getMeasuredHeight())).rotation(getWindowManager().getDefaultDisplay().getRotation()).specificCameraId(previewConfig.getIrCameraId()).previewOn(binding.dualCameraTexturePreviewIr).cameraListener(irCameraListener).isMirror(ConfigUtil.isDrawIrPreviewHorizontalMirror(this)).previewSize(recognizeViewModel.loadPreviewSize()) //相机预览大小设置，RGB与IR需使用相同大小
                .additionalRotation(previewConfig.getIrAdditionalDisplayOrientation()) //额外旋转角度
                .build();
        irCameraHelper.init();
        try {
            irCameraHelper.start();
        } catch (RuntimeException e) {
            showToast(e.getMessage() + getString(R.string.camera_error_notice));
        }
    }


    /**
     * 绘制RGB、IR画面的实时人脸信息
     *
     * @param facePreviewInfoList RGB画面的实时人脸信息
     */
    private void drawPreviewInfo(List<FacePreviewInfo> facePreviewInfoList) {
        if (rgbFaceRectTransformer != null) {
            List<FaceRectView.DrawInfo> rgbDrawInfoList = recognizeViewModel.getDrawInfo(facePreviewInfoList, LivenessType.RGB, openRectInfoDraw);
            binding.dualCameraFaceRectView.drawRealtimeFaceInfo(rgbDrawInfoList);
        }
        if (irFaceRectTransformer != null) {
            List<FaceRectView.DrawInfo> irDrawInfoList = recognizeViewModel.getDrawInfo(facePreviewInfoList, LivenessType.IR, openRectInfoDraw);
            binding.dualCameraFaceRectViewIr.drawRealtimeFaceInfo(irDrawInfoList);
        }
    }

    @Override
    protected void afterRequestPermission(int requestCode, boolean isAllGranted) {
        if (requestCode == ACTION_REQUEST_PERMISSIONS) {
            if (isAllGranted) {
                recognizeViewModel.init();
                initRgbCamera();
                if (DualCameraHelper.hasDualCamera() && livenessType == LivenessType.IR) {
                    initIrCamera();
                }
            } else {
                showToast(getString(R.string.permission_denied));
            }
        }
    }

    public void openRectInfoDraw(View view) {
        openRectInfoDraw = !openRectInfoDraw;
        recognizeViewModel.setDrawRectInfoTextValue(openRectInfoDraw);
    }

    /**
     * 将准备注册的状态置为待注册
     *
     * @param view 注册按钮
     */
    public void register(View view) {
        recognizeViewModel.prepareRegister();
        String editRec = getIntent().getStringExtra("editRec");
        String faceId = getIntent().getStringExtra("faceId");
        // 点击注册按钮后，删除替换旧的人脸信息
        if(Objects.equals(editRec, "BackEditRegister")) {
            if (faceId != null) {
                int faceIdInt = Integer.parseInt(faceId);
                queryFaceTask.execute(faceIdInt);
            }
        }
        Button button2 = findViewById(R.id.register);
        button2.setVisibility(View.GONE);
    }

    /**
     * 参数配置
     *
     * @param view
     */
    public void setting(View view) {
        this.actionAfterFinish = NAVIGATE_TO_RECOGNIZE_SETTINGS_ACTIVITY;
        showLongToast(getString(R.string.please_wait));
        finish();
    }

    /**
     * 识别分析界面
     *
     * @param view 注册按钮
     */
    public void recognizeDebug(View view) {
        this.actionAfterFinish = NAVIGATE_TO_RECOGNIZE_DEBUG_ACTIVITY;
        showLongToast(getString(R.string.please_wait));
        finish();
    }

    /**
     * 返回按钮
     */
    public void goToBack() {

    }

    /**
     * 在{@link ActivityRegisterAndRecognizeBinding#dualCameraTexturePreviewRgb}第一次布局完成后，去除该监听，并且进行引擎和相机的初始化
     */
    @Override
    public void onGlobalLayout() {
        binding.dualCameraTexturePreviewRgb.getViewTreeObserver().removeOnGlobalLayoutListener(this);
        if (!checkPermissions(NEEDED_PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, NEEDED_PERMISSIONS, ACTION_REQUEST_PERMISSIONS);
        } else {
            recognizeViewModel.init();
            initRgbCamera();
            if (DualCameraHelper.hasDualCamera() && livenessType == LivenessType.IR) {
                initIrCamera();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        resumeCamera();
    }

    private void resumeCamera() {
        if (rgbCameraHelper != null) {
            rgbCameraHelper.start();
        }
        if (irCameraHelper != null) {
            irCameraHelper.start();
        }
    }

    @Override
    protected void onPause() {
        pauseCamera();
        super.onPause();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    private void pauseCamera() {
        if (rgbCameraHelper != null) {
            rgbCameraHelper.stop();
        }
        if (irCameraHelper != null) {
            irCameraHelper.stop();
        }
    }
}
