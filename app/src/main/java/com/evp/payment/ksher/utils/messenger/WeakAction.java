package com.evp.payment.ksher.utils.messenger;

import java.lang.ref.WeakReference;

import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;


/**
 * com.arke.messenger
 * <p>
 * Desc:
 *
 * @author kelin
 * @date 15-8-14
 * Modified by Linxy on 2017/1/18.
 */

public class WeakAction<T> {
    private Action action;
    private Consumer<T> consumer;
    private boolean isLive;
    private Object target;
    private WeakReference reference;

    public WeakAction(Object target, Action action) {
        reference = new WeakReference(target);
        this.action = action;

    }

    public WeakAction(Object target, Consumer<T> consumer) {
        reference = new WeakReference(target);
        this.consumer = consumer;
    }

    public void execute() throws Exception {
        if (action != null && isLive()) {
            action.run();
        }
    }

    public void execute(T parameter) throws Exception {
        if (consumer != null
            && isLive()) {
            consumer.accept(parameter);
        }
    }

    public void markForDeletion() {
        reference.clear();
        reference = null;
        action = null;
        consumer = null;
    }

    public Action getAction() {
        return action;
    }

    public Consumer<T> getConsumer() {
        return consumer;
    }

    public boolean isLive() {
        return reference != null && reference.get() != null;
    }


    public Object getTarget() {
        if (reference != null) {
            return reference.get();
        }
        return null;
    }
}
