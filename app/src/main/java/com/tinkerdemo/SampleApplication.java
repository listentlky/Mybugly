package com.tinkerdemo;

import com.tencent.tinker.loader.app.TinkerApplication;
import com.tencent.tinker.loader.shareutil.ShareConstants;


/**
 * Created by AED on 2018/7/5.
 */

public class SampleApplication extends TinkerApplication {

    public SampleApplication() {
        super(ShareConstants.TINKER_ENABLE_ALL, "com.tinkerdemo.SampleApplicationLike",
                "com.tencent.tinker.loader.TinkerLoader", false);
    }

}