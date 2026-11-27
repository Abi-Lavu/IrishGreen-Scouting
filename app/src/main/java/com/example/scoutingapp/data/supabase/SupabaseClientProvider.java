package com.example.scoutingapp.data.supabase;

import com.example.scoutingapp.BuildConfig;

import okhttp3.OkHttpClient;

/**
 * Shared Supabase connection settings and HTTP client. PostgrestClient builds requests
 * against Supabase's PostgREST HTTP API using this client.
 *
 * The URL and anon key come from local.properties at build time (see app/build.gradle);
 * they must point at the BUILT Supabase project, never the original REBUILT backend.
 *
 * Uses OkHttp's default TLS configuration, so server certificates and hostnames are
 * verified normally.
 */
public final class SupabaseClientProvider {

    public static final String SUPABASE_URL = BuildConfig.SUPABASE_URL;
    public static final String SUPABASE_ANON_KEY = BuildConfig.SUPABASE_ANON_KEY;

    public static final OkHttpClient okHttpClient = new OkHttpClient();

    private SupabaseClientProvider() {}
}
