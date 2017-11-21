package com.android.launcher3.net;

import java.util.Locale;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;

import com.android.launcher3.LauncherAppState;
import com.android.launcher3.LauncherApplication;

/**
 * 获取当前网络状态工具类
 */
public class NetworkStatus {

	/* 没有任何连线 */
	public final static int CONN_NONE = 0;
	/* WIFI */
	public final static int CONN_WIFI = 1;
	/* 2G：GPRS, CDMA, EDGE */
	public final static int CONN_2G = 2;
	/* 3G */
	public final static int CONN_3G = 3;
	/*4G*/
	public final static int CONN_4G = 4;
	    
	private Context mcontext;
	private ConnectivityManager connectivityManager;
	private TelephonyManager telephonyManager;
	private static String mNetworkType;
	private static NetworkStatus mInstance;
	
	public static NetworkStatus getInstance(){
		if(mInstance == null){
			Context context = LauncherApplication.getInstance();
			mInstance = new NetworkStatus(context);
		}
		return mInstance;
	}
	
	public NetworkStatus(Context context) {
		mcontext = context.getApplicationContext();
        connectivityManager = (ConnectivityManager)mcontext.getSystemService(Context.CONNECTIVITY_SERVICE);
        telephonyManager = (TelephonyManager)mcontext.getSystemService(Context.TELEPHONY_SERVICE);
	}
	

    /**
     * 是否已连接wifi网络
     * @return
     */
    public boolean isWiFiConnected() {
        NetworkInfo netInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (netInfo != null) {
            return netInfo.getState() == NetworkInfo.State.CONNECTED;
        } else {
            return false;
        }
    }

    /**
     * 是否连接手机网络（2G、3G等）
     * @return
     */
    public boolean isMobileConnected() {
        NetworkInfo netInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (netInfo != null) {
            return netInfo.getState() == NetworkInfo.State.CONNECTED;
        } else {
            return false;
        }
    }

    /**
     * 获取当前手机网络的类型（2G、3G等）
     * @return
     */
    private int getMobileNetworkType(){
    	int state;
    	switch(telephonyManager.getNetworkType()) {
		case TelephonyManager.NETWORK_TYPE_UNKNOWN:
		case TelephonyManager.NETWORK_TYPE_1xRTT:
		case TelephonyManager.NETWORK_TYPE_GPRS:
		case TelephonyManager.NETWORK_TYPE_CDMA:
		case TelephonyManager.NETWORK_TYPE_EDGE:
			state = CONN_2G;
			break;
		case TelephonyManager.NETWORK_TYPE_LTE:
			state = CONN_4G;
			break;
		default:
			state = CONN_3G;
			break;
		}
    	return state;
    }
    
    /**
     * 是否已连接网络（未连接、wifi、2G、3G等）
     * @return
     */
    public boolean isConnected() {
        NetworkInfo netInfo = connectivityManager.getActiveNetworkInfo();
        if (netInfo != null) {
            return netInfo.isConnected();
        } else {
            return false;
        }
    }
    
    /**
	 * 获取网络类型
	 * 
	 * @return 网络类型
	 */
	public static String getNetworkType(Context context) {
			isNetWorking(context);
		return mNetworkType;
	}
	/**
	 * 判断网络是否连接
	 * 
	 * @param context
	 *            环境对象
	 * @return true 有网络，false 无网络
	 */
	public static boolean isNetWorking(Context context) {
		try {
			ConnectivityManager cm = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = cm.getActiveNetworkInfo();
			if (networkInfo != null && networkInfo.isAvailable()) {
				if (networkInfo.getTypeName().equals("WIFI")) {
					mNetworkType = networkInfo.getTypeName().toLowerCase(
							Locale.ENGLISH);
				} else {
					if (networkInfo.getExtraInfo() == null) {
						mNetworkType = "";
					} else {
						mNetworkType = networkInfo.getExtraInfo().toLowerCase(
								Locale.ENGLISH);
					}
				}
				return true;
			} else {
				mNetworkType = "";
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

    /**
     * 获取当前所连接网络类型（未连接、wifi、2G、3G等）
     * @return
     */
    public int getNetWorkState(){
    	if(isConnected()){
    		if(isWiFiConnected()){
    			return CONN_WIFI;
    		}
    		if(isMobileConnected()){
    			return getMobileNetworkType();
    		}
    	}
    	return CONN_NONE;
    }

    public boolean isRadioOff() {
        ServiceState serviceState = new ServiceState();
        return serviceState.getState() == ServiceState.STATE_POWER_OFF;
    }
}
