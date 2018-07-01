package io.evercam.androidapp.test.utils;

import android.content.Context;
import android.test.AndroidTestCase;

import io.evercam.androidapp.utils.Commons;

public class CommonsTest extends AndroidTestCase {
    Context mContext;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContext = this.getContext();
    }

    public void testIsLocalIp()
    {
        final String EXTERNAL_IP = "89.101.133.66";
        final String DOMAIN_NAME = "hello.com";
        assertTrue(Commons.isLocalIp("127.0.0.1"));
        assertTrue(Commons.isLocalIp("172.16.0.26"));
        assertTrue(Commons.isLocalIp("192.168.1.101"));
        assertTrue(Commons.isLocalIp("10.255.255.255"));
        assertFalse(Commons.isLocalIp("172.33.0.0"));
        assertFalse(Commons.isLocalIp("1089888"));
        assertFalse(Commons.isLocalIp("192.1688"));
        assertFalse(Commons.isLocalIp("109.70.215.111"));
        assertFalse(Commons.isLocalIp(EXTERNAL_IP));
        assertFalse(Commons.isLocalIp(DOMAIN_NAME));
    }
}
