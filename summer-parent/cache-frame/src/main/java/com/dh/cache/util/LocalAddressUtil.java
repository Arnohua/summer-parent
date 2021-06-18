package com.dh.cache.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.Enumeration;

/**
 * @author dinghua
 * @date 2021/6/18
 * @since v1.0.0
 */
public class LocalAddressUtil {

	private static Logger logger = LoggerFactory.getLogger(LocalAddressUtil.class);

	/**
	 * 获取本地地址，即优先拿site-local地址
	 */
	public static InetAddress getLocalHostLANAddress() {
		try {
			InetAddress candidateAddress = null;
			// 遍历所有的网络接口
			for (Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces(); ifaces
					.hasMoreElements();) {
				NetworkInterface iface = (NetworkInterface) ifaces.nextElement();
				// 在所有的接口下再遍历IP
				for (Enumeration<InetAddress> inetAddrs = iface.getInetAddresses(); inetAddrs.hasMoreElements();) {
					InetAddress inetAddr = (InetAddress) inetAddrs.nextElement();
					if (!inetAddr.isLoopbackAddress()) {// 排除loopback类型地址
						if (inetAddr.isSiteLocalAddress()) {
							// 如果是site-local地址，就是它了
							return inetAddr;
						} else if (candidateAddress == null) {
							// site-local类型的地址未被发现，先记录候选地址
							candidateAddress = inetAddr;
						}
					}
				}
			}
			if (candidateAddress != null) {
				return candidateAddress;
			}
		} catch (Exception e) {
			logger.error("Failed to determine LAN address: ", e);
		}
		// 如果没有发现 non-loopback地址.只能用最次选的方案
		InetAddress jdkSuppliedAddress = null;
		try {
			jdkSuppliedAddress = InetAddress.getLocalHost();
		} catch (UnknownHostException e) {
			logger.error("The JDK InetAddress.getLocalHost() method unexpectedly returned null. ");
		}
		return jdkSuppliedAddress;
	}
}
