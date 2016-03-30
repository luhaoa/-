package com.sagereal.unlockpin;

import java.lang.reflect.Method;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Log;

public class UnlockPinReceiver extends BroadcastReceiver {

    private static final int GEMINI_SIM_1 = 0;
    private static final int GEMINI_SIM_2 = 1;

    private static final String GEMINI_SIM_1_PIN = "1234";//卡1的pin码
    private static final String GEMINI_SIM_2_PIN = "1234";//卡2的pin码
    
    private static boolean isFirst = true;
	private int count = 0;

    @Override
    public void onReceive(Context context, Intent intent) {
        
        Log.d("liuhanling", "action:" + intent.getAction());
        Log.d("liuhanling", "isFirst:" + isFirst);

        if (isFirst) {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (tm != null) {
                unlockSimPin(tm, GEMINI_SIM_1_PIN, GEMINI_SIM_1);
                unlockSimPin(tm, GEMINI_SIM_2_PIN, GEMINI_SIM_2);
            } else {
                Log.d("liuhanling", "tm is null!");
            }
        }
    }
    
    /**
     * 解锁SIM PIN
     * @param tm
     * @param pin
     * @param simId
     */
    private void unlockSimPin(final TelephonyManager tm, final String pin, final int simId) {

        final int state = getSimStateGemini(tm, simId);
		android.util.Log.d("luhao","state=" + state);		
        if (state == TelephonyManager.SIM_STATE_PIN_REQUIRED) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    supplyPinGemini(tm, pin, simId);
					android.util.Log.d("luhao","luhao=" + "luhao2");
					isFirst = false;	
                }
            }).start();
        } else if (state == TelephonyManager.SIM_STATE_PUK_REQUIRED) {
            // We do not need to deal with the PUK currently.
        } else if (state == TelephonyManager.SIM_STATE_READY) {
            // 激活并解锁
			new Thread(new Runnable() {
                @Override
                public void run() {
					tryChangeIccLockState(tm, null, true, pin, simId);
					android.util.Log.d("luhao","luhao=" + "luhao");
					count++;
					if(count == 2) {
						isFirst = false;
					} 			
                }
			}).start();
        }
    }
    
    /**
     * 双卡得到SIM状态
     * @param tm
     * @param pin
     * @param simId
     * @return
     */
    private static int getSimStateGemini(TelephonyManager tm, int simId) {
        
        int state = 0;
        try {
            Class<?> clazz = Class.forName(tm.getClass().getName());
            Method method = clazz.getDeclaredMethod("getSimStateGemini", int.class);
            method.setAccessible(true);
            
            state = ((Integer) method.invoke(tm, new Object[] { simId })).intValue();
        } catch (Exception e) {
            Log.e("liuhanling", "Exception: " + e);
        }
        Log.d("liuhanling", "getSimStateGemini, SIM" + (simId + 1) + ": " + state);
        
        return state;
    }

    /**
     * 双卡解锁PIN
     * @param tm
     * @param pin
     * @param simId
     * @return
     */
    private static boolean supplyPinGemini(TelephonyManager tm, String pin, int simId) {
        
        boolean result = false;
        try {
            Class<?> clazz = Class.forName(tm.getClass().getName());
            Method method = clazz.getDeclaredMethod("getITelephony");
            method.setAccessible(true);

            Class<?> clazz1 = Class.forName("com.android.internal.telephony.ITelephony");
            Method method1 = clazz1.getDeclaredMethod("supplyPinGemini", new Class[] { String.class, int.class });
            method1.setAccessible(true);

            result = ((Boolean) method1.invoke(method.invoke(tm), new Object[] { pin, simId })).booleanValue();
        } catch (Exception e) {
            Log.e("liuhanling", "Exception: " + e);
        }
        Log.d("liuhanling", "supplyPinGemini, Unlocked SIM" + (simId + 1) + ": " + result);
        
        return result;
    }
    
    /**
     * 单卡解锁PIN
     * @param tm
     * @param pin
     * @param simId
     * @return
     */
    private static boolean supplyPin(TelephonyManager tm, String pin) {

        try {
            Class<?> clazz = Class.forName(tm.getClass().getName());
            Method method = clazz.getDeclaredMethod("getITelephony");
            method.setAccessible(true);

            Class<?> clazz1 = Class.forName("com.android.internal.telephony.ITelephony");
            Method method1 = clazz1.getDeclaredMethod("supplyPin", String.class);
            method1.setAccessible(true);

            return ((Boolean) method1.invoke(method.invoke(tm), pin)).booleanValue();
        } catch (Exception e) {
            Log.e("liuhanling", "Exception: " + e);
        }
        return false;
    }
    
    /**
     * 激活/关闭PIN
     * @param tm
     * @param pin
     * @param simId
     * @return
     */
    private void tryChangeIccLockState(TelephonyManager tm, Message callback, boolean toState, String pin, int simId) {
        
        try {
        	callback = null;
        	toState = true;
            Class<?> clazz = Class.forName(tm.getClass().getName());
            Method method = clazz.getDeclaredMethod("getITelephony");
            method.setAccessible(true);

            Class<?> clazz1 = Class
                    .forName("com.android.internal.telephony.ITelephony");
            Method method1 = clazz1
                    .getDeclaredMethod("tryChangeIccLockState", new Class [] { Message.class, boolean.class, String.class, int.class });
            method1.setAccessible(true);

            method1.invoke(method.invoke(tm), new Object[] { callback, toState, pin, simId});
        } catch (Exception e) {
            Log.e("liuhanling", "Exception: " + e);
        }
    }

    /**
     * 检测SIM状态
     * @param tm
     * @param pin
     * @param simId
     * @return
     */
    private boolean isIccLockEnabled(TelephonyManager tm, int simId) {
        
        boolean islockEnabled = false;
        try {
            Class<?> clazz = Class.forName(tm.getClass().getName());
            Method method = clazz.getDeclaredMethod("getITelephony");
            method.setAccessible(true);

            Class<?> clazz1 = Class
                    .forName("com.android.internal.telephony.ITelephony");
            Method method1 = clazz1
                    .getDeclaredMethod("isIccLockEnabled", int.class);
            method1.setAccessible(true);

            islockEnabled = ((Boolean) method1.invoke(method.invoke(tm), new Object[] { simId })).booleanValue();
        } catch (Exception e) {
            Log.e("liuhanling", "Exception: " + e);
        }
        return islockEnabled;
    }
    
}
