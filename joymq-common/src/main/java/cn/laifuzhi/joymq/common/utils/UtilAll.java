package cn.laifuzhi.joymq.common.utils;

import lombok.extern.slf4j.Slf4j;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.NumberFormat;
import java.util.Enumeration;
import java.util.Map;
import java.util.zip.CRC32;

@Slf4j
public class UtilAll {
    // refer to RFC 1918
    // 10/8 prefix
    // 172.16/12 prefix
    // 192.168/16 prefix
    private static String innerIp = "";
    private static int pid = -1;

    static {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface anInterface = interfaces.nextElement();
                Enumeration<InetAddress> inetAddresses = anInterface.getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    if (inetAddress.isSiteLocalAddress() && inetAddress instanceof Inet4Address) {
                        innerIp = inetAddress.getHostAddress();
                        log.debug("get innerIp:{}", innerIp);
                        break;
                    }
                }
            }
        } catch (SocketException e) {
            log.error("get innerIp error", e);
        }
        RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
        // format: "pid@hostname"
        String name = runtime.getName();
        try {
            pid = Integer.parseInt(name.substring(0, name.indexOf('@')));
            log.debug("get pid:{}", pid);
        } catch (Exception e) {
            log.error("get pid error name:{}", name, e);
        }
    }

    public static String getInnerIp() {
        return innerIp;
    }

    public static int getPid() {
        return pid;
    }

    public static String currentStackTrace() {
        StringBuilder sb = new StringBuilder();
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement ste : stackTrace) {
            sb.append("\n\t");
            sb.append(ste.toString());
        }
        return sb.toString();
    }

    public static String offset2FileName(final long offset) {
        final NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumIntegerDigits(20);
        nf.setMaximumFractionDigits(0);
        nf.setGroupingUsed(false);
        return nf.format(offset);
    }

    public static int crc32(byte[] array) {
        CRC32 crc32 = new CRC32();
        crc32.update(array);
        return (int) (crc32.getValue() & 0x7FFFFFFF);
    }

    public static String jstack() {
        Map<Thread, StackTraceElement[]> map = Thread.getAllStackTraces();
        StringBuilder result = new StringBuilder();
        for (Map.Entry<Thread, StackTraceElement[]> entry : map.entrySet()) {
            Thread thread = entry.getKey();
            StackTraceElement[] elements = entry.getValue();
            if (elements != null && elements.length > 0) {
                String threadName = entry.getKey().getName();
                result.append(String.format("%-40sTID: %d STATE: %s%n", threadName, thread.getId(), thread.getState()));
                for (StackTraceElement el : elements) {
                    result.append(String.format("%-40s%s%n", threadName, el.toString()));
                }
                result.append("\n");
            }
        }
        return result.toString();
    }
}
