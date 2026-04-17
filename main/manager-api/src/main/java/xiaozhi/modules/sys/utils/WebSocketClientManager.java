package xiaozhi.modules.sys.utils;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.springframework.util.StopWatch;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import xiaozhi.common.utils.DateUtils;

/**
 * WebSocketClientResource：support try-with-resources mode
 */
@Slf4j
public class WebSocketClientManager implements Closeable {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    // 全局callback线程池
    private static final ExecutorService CALLBACK_EXECUTOR = Executors
            .newFixedThreadPool(Runtime.getRuntime().availableProcessors(), new ThreadFactory() {
                private final AtomicInteger cnt = new AtomicInteger();

                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r, "ws-callback-" + cnt.getAndIncrement());
                    t.setDaemon(true);
                    return t;
                }
            });

    private volatile WebSocketSession session;
    private final BlockingQueue<String> textMessageQueue;
    private final BlockingQueue<byte[]> binaryMessageQueue;
    private final CompletableFuture<Void> errorFuture;
    private final long maxSessionDuration;
    private final TimeUnit maxSessionDurationUnit;

    private volatile Consumer<String> onText;
    private volatile Consumer<byte[]> onBinary;
    private volatile Consumer<Throwable> onError;

    private final int queueCapacity;

    // 私hasconstruct，onlyby Builder call
    private WebSocketClientManager(Builder b) {
        this.maxSessionDuration = b.maxSessionDuration;
        this.maxSessionDurationUnit = b.maxSessionDurationUnit;
        this.queueCapacity = b.queueCapacity;
        this.textMessageQueue = new LinkedBlockingQueue<>(queueCapacity);
        this.binaryMessageQueue = new LinkedBlockingQueue<>(queueCapacity);
        this.errorFuture = new CompletableFuture<>();
    }

    public static WebSocketClientManager build(Builder b)
            throws InterruptedException, ExecutionException, TimeoutException, IOException {
        WebSocketClientManager ws = new WebSocketClientManager(b);
        StandardWebSocketClient client = new StandardWebSocketClient();
        CompletableFuture<WebSocketSession> future = client.execute(ws.new InternalHandler(b.uri), b.headers,
                URI.create(b.uri));
        WebSocketSession sess = future.get(b.connectTimeout, b.connectUnit);
        if (sess == null || !sess.isOpen()) {
            throw new IOException("握手failedorsessionnot打开");
        }
        // set缓冲区
        sess.setTextMessageSizeLimit(b.bufferSize);
        sess.setBinaryMessageSizeLimit(b.bufferSize);
        ws.session = sess;
        return ws;
    }


    /**
     * send Text
     */
    public void sendText(String text) throws IOException {
        session.sendMessage(new TextMessage(text));
    }

    public void sendBinary(byte[] data) throws IOException {
        session.sendMessage(new BinaryMessage(data));
    }

    public void sendJson(Object payload) throws IOException {
        String json = OBJECT_MAPPER.writeValueAsString(payload);
        session.sendMessage(new TextMessage(json));
    }

    private <T> List<T> listenerCustom(
            BlockingQueue<T> queue,
            Predicate<T> predicate)
            throws InterruptedException, TimeoutException, ExecutionException {
        List<T> collected = new ArrayList<>();
        long deadline = System.currentTimeMillis() + maxSessionDurationUnit.toMillis(maxSessionDuration);

        while (true) {
            if (errorFuture.isDone()) {
                errorFuture.get();
            }

            long remaining = deadline - System.currentTimeMillis();
            if (remaining <= 0) {
                throw new TimeoutException("waitbatchmessagetimeout");
            }

            T msg = queue.poll(remaining, TimeUnit.MILLISECONDS);
            if (msg == null) {
                throw new TimeoutException("waitbatchmessagetimeout");
            }

            collected.add(msg);
            if (predicate.test(msg)) {
                break;
            }
        }
        close();
        return collected;
    }

    private <T> List<T> listenerCustomWithoutClose(
            BlockingQueue<T> queue,
            Predicate<T> predicate)
            throws InterruptedException, TimeoutException, ExecutionException {
        List<T> collected = new ArrayList<>();
        long deadline = System.currentTimeMillis() + maxSessionDurationUnit.toMillis(maxSessionDuration);

        while (true) {
            if (errorFuture.isDone()) {
                errorFuture.get();
            }

            long remaining = deadline - System.currentTimeMillis();
            if (remaining <= 0) {
                throw new TimeoutException("waitbatchmessagetimeout");
            }

            T msg = queue.poll(remaining, TimeUnit.MILLISECONDS);
            if (msg == null) {
                throw new TimeoutException("waitbatchmessagetimeout");
            }

            collected.add(msg);
            if (predicate.test(msg)) {
                break;
            }
        }
        // not call close()，保持connection开放
        return collected;
    }

    /**
     * synchronousreceive多itemsmessage，直to predicate as true ortimeout抛exception；
     * 
     * @return return监听期间 allmessagelist
     */
    public List<String> listener(Predicate<String> predicate)
            throws InterruptedException, TimeoutException, ExecutionException {
        return listenerCustom(textMessageQueue, predicate);
    }

    /**
     * synchronousreceive多itemsmessage，直to predicate as true ortimeout抛exception；
     * not automaticcloseconnection，适used forneedin同oneconnection上send多message 场景
     * 
     * @return return监听期间 allmessagelist
     */
    public List<String> listenerWithoutClose(Predicate<String> predicate)
            throws InterruptedException, TimeoutException, ExecutionException {
        return listenerCustomWithoutClose(textMessageQueue, predicate);
    }

    public List<byte[]> listenerBinary(Predicate<byte[]> predicate)
            throws InterruptedException, TimeoutException, ExecutionException {
        return listenerCustom(binaryMessageQueue, predicate);
    }

    /**
     * registertextcallback
     */
    public WebSocketClientManager onText(Consumer<String> c) {
        this.onText = c;
        return this;
    }

    /**
     * register二进制callback
     */
    public WebSocketClientManager onBinary(Consumer<byte[]> c) {
        this.onBinary = c;
        return this;
    }

    /**
     * registererrorcallback
     */
    public WebSocketClientManager onError(Consumer<Throwable> c) {
        this.onError = c;
        return this;
    }

    /**
     * closesession，try-with-resources / finally automaticcall
     */
    @Override
    public void close() {
        try {
            if (session != null && session.isOpen()) {
                session.close(CloseStatus.NORMAL);
            }
        } catch (IOException ignored) {
        }
        textMessageQueue.clear();
        binaryMessageQueue.clear();
        errorFuture.completeExceptionally(new IOException("WebSocket alreadyclose"));
    }

    private class InternalHandler extends AbstractWebSocketHandler {
        private final String targetUri;
        private final StopWatch stopWatch;

        InternalHandler(String targetUri) {
            this.targetUri = targetUri;
            this.stopWatch = new StopWatch();
        }

        /**
         * connection建立whencallback
         */
        @Override
        public void afterConnectionEstablished(WebSocketSession session) {
            // savesession
            WebSocketClientManager.this.session = session;
            this.stopWatch.start();
            log.info("wsconnectionsuccess, targetURI: {}, connectiontime: {}", targetUri,
                    DateUtils.getDateTimeNow(DateUtils.DATE_TIME_MILLIS_PATTERN));
        }

        /**
         * processtextmessage
         */
        @Override
        protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
            String payload = message.getPayload();
            // 入队
            textMessageQueue.offer(payload);
            // callbackUser registration  onText
            if (onText != null) {
                CALLBACK_EXECUTOR.submit(() -> onText.accept(payload));
            }
        }

        /**
         * process二进制message
         */
        @Override
        protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
            ByteBuffer buf = message.getPayload();
            byte[] data = new byte[buf.remaining()];
            buf.get(data);
            // 入队
            binaryMessageQueue.offer(data);
            // callbackUser registration  onBinary
            if (onBinary != null) {
                CALLBACK_EXECUTOR.submit(() -> onBinary.accept(data));
            }
        }

        /**
         * 传输errorwhencallback
         */
        @Override
        public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
            super.handleTransportError(session, exception);
            // 保持原has逻辑：complete errorFuture、callback onError、closesession、asynchronous通知connectionfailed
            errorFuture.completeExceptionally(exception);
            if (onError != null) {
                CALLBACK_EXECUTOR.submit(() -> onError.accept(exception));
            }
            session.close(CloseStatus.SERVER_ERROR);
        }

        /**
         * connectionclosewhencallback
         */
        @Override
        public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
            super.afterConnectionClosed(session, status);
            if (stopWatch.isRunning()) {
                stopWatch.stop();
            }
            log.info("wsconnectionclose, targetURI: {}, closetime: {}, connectiontotalwhen长: {}s,断开reason：{}",
                    targetUri, DateUtils.getDateTimeNow(DateUtils.DATE_TIME_MILLIS_PATTERN),
                    DateUtils.millsToSecond(stopWatch.getTotalTimeMillis()),status);
        }

    }

    public static class Builder {
        private String uri; // target WS URI
        private long connectTimeout = 3; // requestconnectionwaittime
        private TimeUnit connectUnit = TimeUnit.SECONDS; // requestconnectionwaittimeunit
        private long maxSessionDuration = 5; // mostlarge连线time，default5seconds
        private TimeUnit maxSessionDurationUnit = TimeUnit.SECONDS; // mostlarge连线timeunit
        private int queueCapacity = 100; // message队column容量
        private int bufferSize = 8 * 1024; //default 8kb
        private WebSocketHttpHeaders headers; // requestheader

        /**
         * target WS URI
         */
        public Builder uri(String uri) {
            this.uri = Objects.requireNonNull(uri);
            return this;
        }

        public Builder headers(WebSocketHttpHeaders h) {
            this.headers = h;
            return this;
        }

        public Builder connectTimeout(long t, TimeUnit u) {
            this.connectTimeout = t;
            this.connectUnit = u;
            return this;
        }

        public Builder maxSessionDuration(long t, TimeUnit u) {
            this.maxSessionDuration = t;
            this.maxSessionDurationUnit = u;
            return this;
        }

        public Builder queueCapacity(int c) {
            this.queueCapacity = c;
            return this;
        }
        public Builder bufferSize(int c) {
            this.bufferSize = c;
            return this;
        }

        public WebSocketClientManager build()
                throws InterruptedException, ExecutionException, TimeoutException, IOException {
            return WebSocketClientManager.build(this);
        }

    }
}
