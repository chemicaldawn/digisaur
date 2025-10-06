package info.dawns.bot;

import info.dawns.scheduling.ShiftType;
import info.dawns.scheduling.VerificationContext;
import info.dawns.utils.UserMap;

import java.util.Map;
import java.util.TreeMap;

public class Bot {

    public static Map<Long, VerificationContext> verificationMemory = new TreeMap<>();
    public static Map<Long, VerificationContext> approvalMemory = new TreeMap<>();
    public static UserMap<String> saleMemory = new UserMap<>();
    public static Map<Long, ShiftType> marketMemory = new UserMap<>();

}
