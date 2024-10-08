package tech.bogomolov.incomingsmsgateway;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.Objects;

public class RequestWorker extends Worker {

    public final static String DATA_URL = "URL";
    public final static String DATA_TEXT = "TEXT";
    public final static String DATA_HEADERS = "HEADERS";
    public final static String DATA_IGNORE_SSL = "IGNORE_SSL";
    public final static String DATA_MAX_RETRIES = "MAX_RETRIES";
    public final static String DATA_CHUNKED_MODE = "CHUNKED_MODE";
    public final static String DATA_ENCRYPT_HMAC_SHA_256 = "ENCRYPT_HMAC_SHA_256";
    public final static String DATA_ENCRYPT_HMAC_SHA_256_KEY = "ENCRYPT_HMAC_SHA_256_KEY";

    public RequestWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        int maxRetries = getInputData().getInt(DATA_MAX_RETRIES, 10);

        if (getRunAttemptCount() > maxRetries) {
            return Result.failure();
        }

        String url = getInputData().getString(DATA_URL);
        String text = getInputData().getString(DATA_TEXT);
        String headers = getInputData().getString(DATA_HEADERS);
        boolean ignoreSsl = getInputData().getBoolean(DATA_IGNORE_SSL, false);
        boolean useChunkedMode = getInputData().getBoolean(DATA_CHUNKED_MODE, true);
        boolean encryptHmacSha256 = getInputData().getBoolean(DATA_ENCRYPT_HMAC_SHA_256, false);
        String encryptHmacSha256Key = getInputData().getString(DATA_ENCRYPT_HMAC_SHA_256_KEY);

        Request request = new Request(url, text);
        request.setJsonHeaders(headers);
        if (encryptHmacSha256) {
            request.setSignatureHeader(Objects.requireNonNull(encryptHmacSha256Key), Objects.requireNonNull(text));
        }

        request.setIgnoreSsl(ignoreSsl);
        request.setUseChunkedMode(useChunkedMode);

        String result = request.execute();

        if (result.equals(Request.RESULT_RETRY)) {
            return Result.retry();
        }

        if (result.equals(Request.RESULT_ERROR)) {
            return Result.failure();
        }

        return Result.success();
    }
}
