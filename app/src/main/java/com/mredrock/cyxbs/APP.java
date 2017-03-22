package com.mredrock.cyxbs;

import android.content.Context;
import android.support.multidex.MultiDexApplication;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;

import com.google.gson.Gson;
import com.marswin89.marsdaemon.DaemonClient;
import com.marswin89.marsdaemon.DaemonConfigurations;
import com.mredrock.cyxbs.component.remind_service.service.DemonReceiver1;
import com.mredrock.cyxbs.component.remind_service.service.DemonReceiver2;
import com.mredrock.cyxbs.component.remind_service.service.DemonService2;
import com.mredrock.cyxbs.component.remind_service.service.NotificationService;
import com.mredrock.cyxbs.config.Const;
import com.mredrock.cyxbs.model.Course;
import com.mredrock.cyxbs.model.User;
import com.mredrock.cyxbs.network.RequestManager;
import com.mredrock.cyxbs.network.encrypt.UserInfoEncryption;
import com.mredrock.cyxbs.util.SPUtils;
import com.orhanobut.logger.Logger;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import rx.Subscriber;


/**
 * Created by cc on 16/3/18.
 */
public class APP extends MultiDexApplication {
    private static Context context;
    private static User mUser;
    private static boolean login;

    public static final String TAG = "myAPP";

    public static Context getContext() {
        return context;
    }

    private static UserInfoEncryption userInfoEncryption;

    private DaemonClient mDaemonClient;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        mDaemonClient = new DaemonClient(createDaemonConfigurations());
        mDaemonClient.onAttachBaseContext(base);
    }

    private DaemonConfigurations createDaemonConfigurations(){
        DaemonConfigurations.DaemonConfiguration configuration1 = new DaemonConfigurations.DaemonConfiguration(
                "com.mredrock.cyxbs:process1",
                NotificationService.class.getCanonicalName(),
                DemonReceiver1.class.getCanonicalName());
        DaemonConfigurations.DaemonConfiguration configuration2 = new DaemonConfigurations.DaemonConfiguration(
                "com.mredrock.cyxbs:process2",
                DemonService2.class.getCanonicalName(),
                DemonReceiver2.class.getCanonicalName());
        DaemonConfigurations.DaemonListener listener = new MyDaemonListener();
        return new DaemonConfigurations(configuration1, configuration2, listener);
    }

    private class MyDaemonListener implements DaemonConfigurations.DaemonListener{
        @Override
        public void onPersistentStart(Context context) {
        }

        @Override
        public void onDaemonAssistantStart(Context context) {
        }

        @Override
        public void onWatchDaemonDaed() {
        }
    }

    public static void setUser(Context context, User user) {
        String userJson;
        mUser = user;
        if (user == null) {
            APP.setLogin(false);
            userJson = "";
        } else {
            userJson = new Gson().toJson(user);
            APP.setLogin(true);
        }
        String encryptedJson = userInfoEncryption.encrypt(userJson);
        SPUtils.set(context, Const.SP_KEY_USER, encryptedJson);
    }

    /**
     * @param context context
     * @return mUser with stuNum and idNum
     */
    public static User getUser(Context context) {
        if (mUser == null) {
            String encryptedJson = (String) SPUtils.get(context, Const.SP_KEY_USER, "");
            String json = userInfoEncryption.decrypt(encryptedJson);
            Log.d("userinfo", json);
            mUser = new Gson().fromJson(json, User.class);

            if (mUser == null || mUser.stuNum == null || mUser.idNum == null) {
                initializeFakeUser();
            }
        }
        return mUser;
    }

    public static boolean isLogin() {
        if (!login) {
            String encryptedJson = (String) SPUtils.get(context, Const.SP_KEY_USER, "");
            String json = userInfoEncryption.decrypt(encryptedJson);
            User user = new Gson().fromJson(json, User.class);
            if (user != null && !user.stuNum.equals("0")) {
                return true;
            } else {
                initializeFakeUser();
            }
        }
        return login;
    }

    private static void initializeFakeUser() {
        mUser = new User();
        //  mUser.id = "0";
        mUser.idNum = "0";
        mUser.stuNum = "0";
    }

    public static boolean isFresh() {
        return isLogin() && getUser(getContext()).stuNum.substring(0, 4).equals("2016");
    }

    public static void setLogin(boolean login) {
        APP.login = login;
    }

    public static boolean hasSetInfo() {
        User user = getUser(getContext());
        return user != null && StringUtils.isNotBlank(user.id);
    }

    public static boolean hasNickName() {
        return getUser(getContext()).nickname != null && !getUser(getContext()).nickname.equals("");
    }

    @Override
    public void onCreate() {
        super.onCreate();

        context = getApplicationContext();
        initThemeMode();
        //  FIR.init(this);
        Logger.init("cyxbs_mobile");
        // Initialize UserInfoEncrypted
        userInfoEncryption = new UserInfoEncryption();
        // Refresh Course List When Start
        reloadCourseList();
    }

    public void reloadCourseList() {
        if (isLogin()) {
            User user = getUser(getContext());
            RequestManager.getInstance().getCourseList(new Subscriber<List<Course>>() {
                                                           @Override
                                                           public void onCompleted() {}

                                                           @Override
                                                           public void onError(Throwable e) {
                                                               Log.e("CSET", "reloadCourseList", e);
                                                           }

                                                           @Override
                                                           public void onNext(List<Course> courses) {}
                                                       }, user.stuNum, user.idNum, 0, true);
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    private void initThemeMode() {
        boolean isNight = (boolean) SPUtils.get(this, Const.SP_KEY_IS_NIGHT, false);
        if (isNight) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}
