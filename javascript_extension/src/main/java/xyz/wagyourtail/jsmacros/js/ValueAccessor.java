package xyz.wagyourtail.jsmacros.js;

import org.graalvm.polyglot.Value;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;

public class ValueAccessor {

    private static final MethodHandle GET_RECEIVER;

    public static Object getReceiver(Value value) {
        try {
            return GET_RECEIVER.invoke(value);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }

    static {
        try {
            Field f = Value.class.getSuperclass().getDeclaredField("receiver");
            f.setAccessible(true);
            GET_RECEIVER = MethodHandles.lookup().unreflectGetter(f);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
