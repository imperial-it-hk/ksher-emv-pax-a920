package com.evp.payment.ksher.utils.messenger;

import com.evp.eos.utils.LogUtil;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

public class RxBus {

    private static final String TAG = "RxBus";

    private static RxBus defaultInstance;

    private HashMap<Type, List<WeakActionAndToken>> recipientsOfSubclassesAction;

    private HashMap<Type, List<WeakActionAndToken>> recipientsStrictAction;

    public static RxBus getDefault() {
        if (defaultInstance == null) {
            defaultInstance = new RxBus();
        }
        return defaultInstance;
    }


    public static void overrideDefault(RxBus newRxBus) {
        defaultInstance = newRxBus;
    }

    public static void reset() {
        defaultInstance = null;
    }

    private static <T> void sendToList(
            T message,
            Collection<WeakActionAndToken> list,
            Type messageTargetType,
            Object token) throws Exception {
        if (list != null) {
            // Clone to protect from people registering in a "receive message" method
            // Bug correction Messaging BL0004.007
            ArrayList<WeakActionAndToken> listClone = new ArrayList<>();
            listClone.addAll(list);

            for (WeakActionAndToken item : listClone) {
                WeakAction executeAction = item.getAction();
                boolean enableExecute = executeAction != null
                        && item.getAction().isLive()
                        && item.getAction().getTarget() != null
                        && (messageTargetType == null
                        || item.getAction().getTarget().getClass() == messageTargetType
                        || classImplements(item.getAction().getTarget().getClass(), messageTargetType))
                        && ((item.getToken() == null && token == null)
                        || item.getToken() != null && item.getToken().equals(token));
                if (enableExecute) {
                    executeAction.execute(message);
                }
            }
        }
    }

    private synchronized static void unregisterFromLists(Object recipient,
                                                         HashMap<Type, List<WeakActionAndToken>> lists) {
        if (recipient == null
                || lists == null
                || lists.size() == 0) {
            return;
        }
        for (Type messageType : lists.keySet()) {
            for (WeakActionAndToken item : lists.get(messageType)) {
                WeakAction weakAction = item.getAction();

                if (weakAction != null
                        && recipient == weakAction.getTarget()) {
                    weakAction.markForDeletion();
                }
            }
        }
        cleanupList(lists);
    }

    private synchronized static <T> void unregisterFromLists(
            Object recipient,
            Consumer<T> consumer,
            HashMap<Type, List<WeakActionAndToken>> lists,
            Class<T> tClass) {
        Type messageType = tClass;

        if (recipient == null
                || lists == null
                || lists.size() == 0
                || !lists.containsKey(messageType)) {
            return;
        }

        for (WeakActionAndToken item : lists.get(messageType)) {
            WeakAction<T> weakActionCasted = (WeakAction<T>) item.getAction();

            boolean markForDeletion = weakActionCasted != null
                    && recipient == weakActionCasted.getTarget()
                    && (consumer == null
                    || consumer == weakActionCasted.getConsumer());
            if (markForDeletion) {
                item.getAction().markForDeletion();
            }
        }
    }

    private synchronized static void unregisterFromLists(
            Object recipient,
            Action action,
            HashMap<Type, List<WeakActionAndToken>> lists
    ) {
        Type messageType = NotMsgType.class;

        if (recipient == null
                || lists == null
                || lists.size() == 0
                || !lists.containsKey(messageType)) {
            return;
        }

        for (WeakActionAndToken item : lists.get(messageType)) {
            WeakAction weakActionCasted = item.getAction();

            boolean markForDeletion = weakActionCasted != null
                    && recipient == weakActionCasted.getTarget()
                    && (action == null
                    || action == weakActionCasted.getAction());
            if (markForDeletion) {
                item.getAction().markForDeletion();
            }
        }
    }

    private synchronized static <T> void unregisterFromLists(
            Object recipient,
            Object token,
            Consumer<T> consumer,
            HashMap<Type, List<WeakActionAndToken>> lists, Class<T> tClass) {
        Type messageType = tClass;

        if (recipient == null
                || lists == null
                || lists.size() == 0
                || !lists.containsKey(messageType)) {
            return;
        }

        for (WeakActionAndToken item : lists.get(messageType)) {
            WeakAction<T> weakActionCasted = (WeakAction<T>) item.getAction();

            boolean markForDeletion = weakActionCasted != null
                    && recipient == weakActionCasted.getTarget()
                    && (consumer == null
                    || consumer == weakActionCasted.getConsumer())
                    && (token == null
                    || token.equals(item.getToken()));
            if (markForDeletion) {
                item.getAction().markForDeletion();
            }
        }
    }

    private synchronized static void unregisterFromLists(
            Object recipient,
            Object token,
            Action action,
            HashMap<Type, List<WeakActionAndToken>> lists) {
        Type messageType = NotMsgType.class;

        if (recipient == null
                || lists == null
                || lists.size() == 0
                || !lists.containsKey(messageType)) {
            return;
        }

        for (WeakActionAndToken item : lists.get(messageType)) {
            WeakAction weakActionCasted = item.getAction();

            boolean markForDeletion = weakActionCasted != null
                    && recipient == weakActionCasted.getTarget()
                    && (action == null
                    || action == weakActionCasted.getAction())
                    && (token == null
                    || token.equals(item.getToken()));
            if (markForDeletion) {
                item.getAction().markForDeletion();
            }
        }
    }

    private static boolean classImplements(Type instanceType, Type interfaceType) {
        if (interfaceType == null
                || instanceType == null) {
            return false;
        }
        Class[] interfaces = ((Class) instanceType).getInterfaces();
        for (Class currentInterface : interfaces) {
            if (currentInterface == interfaceType) {
                return true;
            }
        }

        return false;
    }

    private static void cleanupList(HashMap<Type, List<WeakActionAndToken>> lists) {
        if (lists == null) {
            return;
        }
        for (Map.Entry<Type, List<WeakActionAndToken>> key : lists.entrySet()) {
            List<WeakActionAndToken> itemList = lists.get(key);
            if (itemList != null) {
                for (WeakActionAndToken item : itemList) {
                    if (item.getAction() == null
                            || !item.getAction().isLive()) {
                        itemList.remove(item);
                    }
                }
                if (itemList.size() == 0) {
                    lists.remove(key);
                }
            }
        }
    }

    private static void sendToList(
            Collection<WeakActionAndToken> list,
            Type messageTargetType,
            Object token) {
        if (list != null) {
            // Clone to protect from people registering in a "receive message" method
            // Bug correction Messaging BL0004.007
            ArrayList<WeakActionAndToken> listClone = new ArrayList<>();
            listClone.addAll(list);

            for (WeakActionAndToken item : listClone) {
                WeakAction executeAction = item.getAction();
                boolean enableExecute = executeAction != null
                        && item.getAction().isLive()
                        && item.getAction().getTarget() != null
                        && (messageTargetType == null
                        || item.getAction().getTarget().getClass() == messageTargetType
                        || classImplements(item.getAction().getTarget().getClass(), messageTargetType))
                        && ((item.getToken() == null && token == null)
                        || item.getToken() != null && item.getToken().equals(token));
                if (enableExecute) {
                    try {
                        executeAction.execute();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    /**
     * @param recipient the receiver,if register in activity the recipient always set "this", and
     *                  "RxBus.getDefault().unregister(this)" in onDestroy,if in ViewModel, you can also register
     *                  with Activity context and also in onDestroy to unregister.
     * @param action    do something on message received
     */
    public void register(Object recipient, Action action) {
        register(recipient, null, false, action);
    }

    /**
     * @param recipient                 the receiver,if register in activity the recipient always set "this", and
     *                                  "RxBus.getDefault().unregister(this)" in onDestroy,if in ViewModel, you can also register
     *                                  with Activity context and also in onDestroy to unregister.
     * @param receiveDerivedMessagesToo whether Derived class of recipient can receive the message
     * @param action                    do something on message received
     */
    public void register(Object recipient, boolean receiveDerivedMessagesToo, Action action) {
        register(recipient, null, receiveDerivedMessagesToo, action);
    }

    /**
     * @param recipient the receiver,if register in activity the recipient always set "this", and
     *                  "RxBus.getDefault().unregister(this)" in onDestroy,if in ViewModel, you can also register
     *                  with Activity context and also in onDestroy to unregister.
     * @param token     register with a unique token,when a messenger send a msg with same token,it will
     *                  receive this msg
     * @param action    do something on message received
     */
    public void register(Object recipient, Object token, Action action) {
        register(recipient, token, false, action);
    }

    /**
     * @param recipient                 the receiver,if register in activity the recipient always set "this", and
     *                                  "RxBus.getDefault().unregister(this)" in onDestroy,if in ViewModel, you can also register
     *                                  with Activity context and also in onDestroy to unregister.
     * @param token                     register with a unique token,when a messenger send a msg with same token,it will
     *                                  receive this msg
     * @param receiveDerivedMessagesToo whether Derived class of recipient can receive the message
     * @param action                    do something on message received
     */
    private void register(Object recipient, Object token, boolean receiveDerivedMessagesToo,
                          Action action) {

        Type messageType = NotMsgType.class;

        HashMap<Type, List<WeakActionAndToken>> recipients;

        if (receiveDerivedMessagesToo) {
            if (recipientsOfSubclassesAction == null) {
                recipientsOfSubclassesAction = new HashMap<>(16);
            }

            recipients = recipientsOfSubclassesAction;
        } else {
            if (recipientsStrictAction == null) {
                recipientsStrictAction = new HashMap<>(16);
            }

            recipients = recipientsStrictAction;
        }

        List<WeakActionAndToken> list;

        if (!recipients.containsKey(messageType)) {
            list = new ArrayList<>();
            recipients.put(messageType, list);
        } else {
            list = recipients.get(messageType);
        }

        WeakAction weakAction = new WeakAction(recipient, action);

        WeakActionAndToken item = new WeakActionAndToken(weakAction, token);
        list.add(item);
        cleanup();
    }

    /**
     * @param recipient {@link RxBus#register(Object, Action)}
     * @param tClass    class of T
     * @param consumer  this action has one params that type of tClass
     * @param <T>       message data type
     */
    public <T> void register(Object recipient, Class<T> tClass, Consumer<T> consumer) {
        register(recipient, null, false, tClass, consumer);
    }

    /**
     * see {@link RxBus#register(Object, Class, Consumer)}
     *
     * @param recipient                 receiver of message
     * @param receiveDerivedMessagesToo whether derived class of recipient can receive the message
     * @param tClass                    class of T
     * @param consumer                  this action has one params that type of tClass
     * @param <T>                       message data type
     */
    public <T> void register(Object recipient, boolean receiveDerivedMessagesToo, Class<T> tClass,
                             Consumer<T> consumer) {
        register(recipient, null, receiveDerivedMessagesToo, tClass, consumer);
    }

    /**
     * see {@link RxBus#register(Object, Object, Action)}
     *
     * @param recipient receiver of message
     * @param token     register with a unique token,when a messenger send a msg with same token,it will
     *                  receive this msg
     * @param tClass    class of T for Consumer
     * @param consumer  this action has one params that type of tClass
     * @param <T>       message data type
     */
    public <T> void register(Object recipient, Object token, Class<T> tClass,
                             Consumer<T> consumer) {
        register(recipient, token, false, tClass, consumer);
    }

    /**
     * see {@link RxBus#register(Object, Object, Class, Consumer)}
     *
     * @param recipient                 receiver of message
     * @param token                     register with a unique token,when a messenger send a msg with same token,it will
     *                                  receive this msg
     * @param receiveDerivedMessagesToo whether derived class of recipient can receive the message
     * @param consumer                  this action has one params that type of tClass
     * @param tClass                    class of T for Consumer
     * @param <T>                       message data type
     */
    private <T> void register(Object recipient, Object token, boolean receiveDerivedMessagesToo,
                              Class<T> tClass, Consumer<T> consumer) {

        Type messageType = tClass;

        HashMap<Type, List<WeakActionAndToken>> recipients;

        if (receiveDerivedMessagesToo) {
            if (recipientsOfSubclassesAction == null) {
                recipientsOfSubclassesAction = new HashMap<>(16);
            }

            recipients = recipientsOfSubclassesAction;
        } else {
            if (recipientsStrictAction == null) {
                recipientsStrictAction = new HashMap<>(16);
            }

            recipients = recipientsStrictAction;
        }

        List<WeakActionAndToken> list;

        if (!recipients.containsKey(messageType)) {
            list = new ArrayList<>();
            recipients.put(messageType, list);
        } else {
            list = recipients.get(messageType);
        }

        WeakAction weakAction = new WeakAction<>(recipient, consumer);

        WeakActionAndToken item = new WeakActionAndToken(weakAction, token);
        list.add(item);
        cleanup();
    }

    private void cleanup() {
        cleanupList(recipientsOfSubclassesAction);
        cleanupList(recipientsStrictAction);
    }

    /**
     * @param token send with a unique token,when a receiver has register with same token,it will
     *              receive this msg
     */
    public void sendNoMsg(Object token) {
        sendToTargetOrType(null, token);
    }

    /**
     * send to recipient directly with has not any message
     *
     * @param target RxBus.getDefault().register(this, ..) in a activity,if target set this activity
     *               it will receive the message
     */
    public void sendNoMsgToTarget(Object target) {
        sendToTargetOrType(target.getClass(), null);
    }

    /**
     * send message to target with token,when a receiver has register with same token,it will
     * receive this msg
     *
     * @param token  send with a unique token,when a receiver has register with same token,it will
     *               receive this msg
     * @param target send to recipient directly with has not any message,
     *               RxBus.getDefault().register(this, ..) in a activity,if target set this activity it will
     *               receive the message
     */
    public void sendNoMsgToTargetWithToken(Object token, Object target) {
        sendToTargetOrType(target.getClass(), token);
    }

    /**
     * send the message type of T, all receiver can receive the message
     *
     * @param message any object can to be a message
     * @param <T>     message data type
     */
    public <T> void send(T message) {
        sendToTargetOrType(message, null, null);
    }

    /**
     * send the message type of T, all receiver can receive the message
     *
     * @param message any object can to be a message
     * @param token   send with a unique token,when a receiver has register with same token,it will
     *                receive this message
     * @param <T>     message data type
     */
    public <T> void send(T message, Object token) {
        sendToTargetOrType(message, null, token);
    }

    /**
     * send message to recipient directly
     *
     * @param message any object can to be a message
     * @param target  send to recipient directly with has not any message,
     *                RxBus.getDefault().register(this, ..) in a activity,if target set this activity it will
     *                receive the message
     * @param <T>     message data type
     * @param <R>     target
     */
    public <T, R> void sendToTarget(T message, R target) {
        sendToTargetOrType(message, target.getClass(), null);
    }

    /**
     * Unregister the receiver such as: RxBus.getDefault().unregister(this)" in onDestroy in the
     * Activity is required avoid to memory leak!
     *
     * @param recipient receiver of message
     */
    public void unregister(Object recipient) {
        unregisterFromLists(recipient, recipientsOfSubclassesAction);
        unregisterFromLists(recipient, recipientsStrictAction);
        cleanup();
    }

    public <T> void unregister(Object recipient, Object token) {
        unregisterFromLists(recipient, token, null, recipientsStrictAction);
        unregisterFromLists(recipient, token, null, recipientsOfSubclassesAction);
        cleanup();
    }

    private void sendToTargetOrType(Type messageTargetType, Object token) {
        Class messageType = NotMsgType.class;
        if (recipientsOfSubclassesAction != null) {
            // Clone to protect from people registering in a "receive message" method
            // Bug correction Messaging BL0008.002
            List<Type> listClone = new ArrayList<>();
            listClone.addAll(recipientsOfSubclassesAction.keySet());
            for (Type type : listClone) {
                List<WeakActionAndToken> list = null;

                if (messageType == type
                        || ((Class) type).isAssignableFrom(messageType)
                        || classImplements(messageType, type)) {
                    list = recipientsOfSubclassesAction.get(type);
                }

                sendToList(list, messageTargetType, token);
            }
        }

        if (recipientsStrictAction != null) {
            if (recipientsStrictAction.containsKey(messageType)) {
                List<WeakActionAndToken> list = recipientsStrictAction.get(messageType);
                sendToList(list, messageTargetType, token);
            }
        }

        cleanup();
    }

    private <T> void sendToTargetOrType(T message, Type messageTargetType, Object token) {
        Class messageType = message.getClass();

        if (recipientsOfSubclassesAction != null) {
            // Clone to protect from people registering in a "receive message" method
            // Bug correction Messaging BL0008.002
            List<Type> listClone = new ArrayList<>();
            listClone.addAll(recipientsOfSubclassesAction.keySet());
            for (Type type : listClone) {
                List<WeakActionAndToken> list = null;

                if (messageType == type
                        || ((Class) type).isAssignableFrom(messageType)
                        || classImplements(messageType, type)) {
                    list = recipientsOfSubclassesAction.get(type);
                }

                try {
                    sendToList(message, list, messageTargetType, token);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        if (recipientsStrictAction != null) {
            if (recipientsStrictAction.containsKey(messageType)) {
                List<WeakActionAndToken> list = recipientsStrictAction.get(messageType);
                try {
                    sendToList(message, list, messageTargetType, token);
                } catch (Exception e) {
                    LogUtil.e(TAG, e);
                    throw new RuntimeException(e);
                }
            }
        }

        cleanup();
    }

    private static class NotMsgType {

    }

    private class WeakActionAndToken {

        private WeakAction action;
        private Object token;

        public WeakActionAndToken(WeakAction action, Object token) {
            this.action = action;
            this.token = token;
        }

        public WeakAction getAction() {
            return action;
        }

        public void setAction(WeakAction action) {
            this.action = action;
        }

        public Object getToken() {
            return token;
        }

        public void setToken(Object token) {
            this.token = token;
        }
    }
}