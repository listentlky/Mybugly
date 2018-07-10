# bugly
 
#相信大家 看到这里，都已经对比过并明白bugly 的优点，这里不再啰嗦，直接进入配置流程： ♪(＾∀＾●)ﾉ
 
 
#第一步：添加插件依赖 
工程根目录下“build.gradle”文件中添加：

buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:2.3.3'
        classpath "com.tencent.bugly:tinker-support:latest.release"
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}


      
#第二步：在app目录下创建------tinker-support.gradle内容如下所示（示例配置）：

apply plugin: 'com.tencent.bugly.tinker-support'

def bakPath = file("${buildDir}/bakApk/")

/**
 * 此处填写每次构建生成的基准包目录
 */
def baseApkDir = "app-0709-17-36-49"

/**
 * 对于插件各参数的详细解析请参考
 */
tinkerSupport {

    // 开启tinker-support插件，默认值true
    enable = true

    // 指定归档目录，默认值当前module的子目录tinker
    autoBackupApkDir = "${bakPath}"

    // 是否启用覆盖tinkerPatch配置功能，默认值false
    // 开启后tinkerPatch配置不生效，即无需添加tinkerPatch
    overrideTinkerPatchConfiguration = true

    // 编译补丁包时，必需指定基线版本的apk，默认值为空
    // 如果为空，则表示不是进行补丁包的编译
    // @{link tinkerPatch.oldApk }
    baseApk = "${bakPath}/${baseApkDir}/app-release.apk"

    // 对应tinker插件applyMapping
    baseApkProguardMapping = "${bakPath}/${baseApkDir}/app-release-mapping.txt"

    // 对应tinker插件applyResourceMapping
    baseApkResourceMapping = "${bakPath}/${baseApkDir}/app-release-R.txt"

    // 构建基准包和补丁包都要指定不同的tinkerId，并且必须保证唯一性
    tinkerId = "patch-1.3.5"

    // 构建多渠道补丁时使用
    // buildAllFlavorsDir = "${bakPath}/${baseApkDir}"

    // 是否启用加固模式，默认为false.(tinker-spport 1.0.7起支持）
    isProtectedApp = true

    // 是否开启反射Application模式
    enableProxyApplication = false

}

/**
 * 一般来说,我们无需对下面的参数做任何的修改
 * 对于各参数的详细介绍请参考:
 * https://github.com/Tencent/tinker/wiki/Tinker-%E6%8E%A5%E5%85%A5%E6%8C%87%E5%8D%97
 */
tinkerPatch {
    //oldApk ="${bakPath}/${appName}/app-release.apk"
    ignoreWarning = false
    useSign = true
    dex {
        dexMode = "jar"
        pattern = ["classes*.dex"]
        loader = []
    }
    lib {
        pattern = ["lib/*/*.so"]
    }

    res {
        pattern = ["res/*", "r/*", "assets/*", "resources.arsc", "AndroidManifest.xml"]
        ignoreChange = []
        largeModSize = 100
    }

    packageConfig {
    }
    sevenZip {
        zipArtifact = "com.tencent.mm:SevenZip:1.1.10"
//        path = "/usr/local/bin/7za"
    }
    buildConfig {
        keepDexApply = false
        //tinkerId = "1.0.1-base"
        //applyMapping = "${bakPath}/${appName}/app-release-mapping.txt" //  可选，设置mapping文件，建议保持旧apk的proguard混淆方式
        //applyResourceMapping = "${bakPath}/${appName}/app-release-R.txt" // 可选，设置R.txt文件，通过旧apk文件保持ResId的分配
    }
}


 
#第三步：集成SDK 
gradle配置

在app module的“build.gradle”文件中添加（示例配置）：

// 依赖插件脚本
apply from: 'tinker-support.gradle'

  defaultConfig {
        applicationId "com.tinkerdemo"
        minSdkVersion 15
        targetSdkVersion 26
        versionCode 135
        versionName "1.3.5"
        testInstrumentationRunner "android.support.test.runner.AndroidJUnitRunner"

        multiDexEnabled true
        ndk {
            //设置支持的SO库架构
            abiFilters 'armeabi' //, 'x86', 'armeabi-v7a', 'x86_64', 'arm64-v8a'
        }
    }
    buildTypes {
        release {
            minifyEnabled true
            signingConfig signingConfigs.bugly
            proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
        }
    }
    
dependencies {
    compile 'com.android.support:multidex:1.0.1'
    // 多dex配置
    compile 'com.tencent.tinker:tinker-android-lib:1.9.6'
    compile 'com.tencent.bugly:crashreport_upgrade:latest.release'
    compile 'com.tencent.bugly:nativecrashreport:latest.release'
    //其中latest.release指代最新版本号，也可以指定明确的版本号，例如2.2.0
}
      
#第四步：初始化SDK 
enableProxyApplication = false 的情况 
这是Tinker推荐的接入方式，一定程度上会增加接入成本，但具有更好的兼容性。

集成Bugly升级SDK之后，我们需要按照以下方式自定义ApplicationLike来实现Application的代码（以下是示例）：

自定义Application

public class SampleApplication extends TinkerApplication {

    public SampleApplication() {
        super(ShareConstants.TINKER_ENABLE_ALL, "com.tinkerdemo.SampleApplicationLike",
                "com.tencent.tinker.loader.TinkerLoader", false);
    }

}


注意：这个类集成TinkerApplication类，这里面不做任何操作，所有Application的代码都会通过 映射 放到ApplicationLike继承类当中 
参数解析 
参数1：tinkerFlags 表示Tinker支持的类型 dex only、library only or all suuport，default: TINKER_ENABLE_ALL 
参数2：delegateClassName Application代理类 这里填写你自定义的ApplicationLike 
参数3：loaderClassName Tinker的加载器，使用默认即可 
参数4：tinkerLoadVerifyFlag 加载dex或者lib是否验证md5，默认为false
      
      
 自定义ApplicationLike

package com.tinkerdemo;


import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.multidex.MultiDex;
import android.text.TextUtils;

import com.tencent.bugly.Bugly;
import com.tencent.bugly.beta.Beta;
import com.tencent.bugly.crashreport.CrashReport;
import com.tencent.tinker.loader.app.DefaultApplicationLike;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by AED on 2018/7/5.
 */

public class SampleApplicationLike extends DefaultApplicationLike {

    public static final String TAG = "Tinker.SampleApplicationLike";

    public SampleApplicationLike(Application application, int tinkerFlags,
                                 boolean tinkerLoadVerifyFlag, long applicationStartElapsedTime,
                                 long applicationStartMillisTime, Intent tinkerResultIntent) {
        super(application, tinkerFlags, tinkerLoadVerifyFlag, applicationStartElapsedTime, applicationStartMillisTime, tinkerResultIntent);
    }


    @Override
    public void onCreate() {
        super.onCreate();
        // 这里实现SDK初始化，appId替换成你的在Bugly平台申请的appId
        // 调试时，将第三个参数改为true
        Bugly.init(getApplication(), "ecc597add5", true);
        initCrashReport();
    }

    private void initCrashReport() {
        Context context = getApplication().getApplicationContext();
       // 获取当前包名
        String packageName = context.getPackageName();
        // 获取当前进程名
        String processName = getProcessName(android.os.Process.myPid());
        // 设置是否为上报进程
        CrashReport.UserStrategy strategy = new CrashReport.UserStrategy(context);
        strategy.setUploadProcess(processName == null || processName.equals(packageName));
        // 初始化Bugly
        CrashReport.initCrashReport(context, "ecc597add5", true, strategy);
        // 如果通过“AndroidManifest.xml”来配置APP信息，初始化方法如下
        // CrashReport.initCrashReport(context, strategy);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onBaseContextAttached(Context base) {
        super.onBaseContextAttached(base);
        // you must install multiDex whatever tinker is installed!
        MultiDex.install(base);

        // 安装tinker
        // TinkerManager.installTinker(this); 替换成下面Bugly提供的方法
        Beta.installTinker(this);
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public void registerActivityLifecycleCallback(Application.ActivityLifecycleCallbacks callbacks) {
        getApplication().registerActivityLifecycleCallbacks(callbacks);
    }


    /**
     * 获取进程号对应的进程名
     *
     * @param pid 进程号
     * @return 进程名
     */
    private static String getProcessName(int pid) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("/proc/" + pid + "/cmdline"));
            String processName = reader.readLine();
            if (!TextUtils.isEmpty(processName)) {
                processName = processName.trim();
            }
            return processName;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        return null;
    }
}



 
# 权限配置

    <uses-permission android:name="android.permission.READ_PHONE_STATE" />
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
    <uses-permission android:name="android.permission.READ_LOGS" />
    <uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />

 
# tinker混淆规则

-dontwarn com.tencent.bugly.**
-keep public class com.tencent.bugly.**{*;}

-dontwarn com.tencent.tinker.**
-keep class com.tencent.tinker.** { *; }

 
#编译 通过AS右边 gradle/app/Tasks/build/assembleRelease 打包基础包；
#补丁包如何打呢？
 
1：需要 替换 tinker-support.gradle 内 baseApkDir 目录名称与 打基础包生成的目录一致； 基础包在app/build/bakapk/
否则打补丁包找不到旧的apk; 
2：需要修改我们的tinkerId,这个ID 是唯一的，打包之后都会在我们的AndroidManifest.xml 中生成；一般 基础包tinkerID 为 base-"版本号";补丁包tinkerId 为patch-"版本号"
3:将需要修复的代码或者资源文件修改
4：通过gradle/app/Tasks/tinker-support/buildTinkerPatchRelease  打补丁包；补丁包生成 在 build/outputs/patch/    尾椎为 7zip.apk 的包则为我们的补丁包
 
****注意知识点干货来了*****
这里 补丁包的的尾椎 官方给出的说明建议为： 避免使用.apk 结尾，防止被拦截。  那么我们不用 .apk  bugly上传热更新就能识别么？  答案是有的，  比如 .zip；
 
这样操作的话，正常来讲，我们使用bugly 热更新 会很简单的。  我看网上有同僚讲，配置阿里的 Sophix 只用了一天， 配置bugly 用了一周，着实恐怖。  

