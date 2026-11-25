package com.jarvis.assistant;

import android.content.Context;

import io.appwrite.Client;
import io.appwrite.services.Account;

/**
 * Central Appwrite client for M01 owner authentication.
 *
 * This client intentionally contains no API key. Mobile apps must use the
 * public endpoint and project ID; privileged API keys stay on the backend.
 */
public final class AppwriteAuthManager {
    public static final String ENDPOINT = "https://sgp.cloud.appwrite.io/v1";
    public static final String PROJECT_ID = "6b06d3aa003d717934a0";

    private final Client client;
    private final Account account;

    public AppwriteAuthManager(Context context) {
        client = new Client(context.getApplicationContext())
                .setEndpoint(ENDPOINT)
                .setProject(PROJECT_ID);
        account = new Account(client);
    }

    public Client getClient() {
        return client;
    }

    public Account getAccount() {
        return account;
    }
}
