package quickmotion.shared;

import java.util.Collection;

/**
 *
 */
public class SyncTools {
    @SuppressWarnings("unchecked")
    public static <T> Collection<T> synchronizedCopy(Collection<T> list) {
        if (list == null) {
            return null;
        }
        try {
            Collection<T> copy = list.getClass().newInstance();
            synchronized (list) {
                copy.addAll(list);
            }
            return copy;
        } catch (InstantiationException ignored) {
        } catch (IllegalAccessException ignored) {
        }
        return null;
    }

}
