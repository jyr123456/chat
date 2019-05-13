package cn.ittiger.im.app;

import cn.ittiger.app.AppContext;
import cn.ittiger.database.SQLiteDBConfig;
import cn.ittiger.im.R;
import cn.ittiger.im.util.AppFileHelper;
import cn.ittiger.util.UnCaughtCrashExceptionHandler;

import com.orhanobut.logger.Logger;

import android.app.Application;
import android.content.Context;

import java.io.File;
import java.io.IOException;

public class App extends Application implements IDbApplication {
    /**
     * 本地数据库配置
     */
    private SQLiteDBConfig mDBConfig;

    public static App instace;
    @Override
    protected void attachBaseContext(Context base) {

        super.attachBaseContext(base);
        AppContext.init(base);
    }

    @Override
    public void onCreate() {

        super.onCreate();
        instace = this;
        UnCaughtCrashExceptionHandler handler = UnCaughtCrashExceptionHandler.getInstance();
        handler.init(this);
        handler.setLogPath(AppFileHelper.getAppCrashDir());

        Logger.init("Smack");
        initImageLoader();
    }

    private void initImageLoader() {

        File cacheDir = new File(AppFileHelper.getAppImageCacheDir());
        if(!cacheDir.exists()) {
            cacheDir.mkdirs();
        }

    }

    @Override
    public SQLiteDBConfig getGlobalDbConfig() {

        if(mDBConfig == null) {
            synchronized (App.class) {
                if(mDBConfig == null) {
                    mDBConfig = new SQLiteDBConfig(AppContext.getInstance());
                    mDBConfig.setDbName(getResources().getString(R.string.app_name) + ".db");
                    //本地数据库文件保存在应用文件目录
                    mDBConfig.setDbDirectoryPath(AppFileHelper.getAppDBDir());
                }
            }
        }
        return mDBConfig;
    }
}
