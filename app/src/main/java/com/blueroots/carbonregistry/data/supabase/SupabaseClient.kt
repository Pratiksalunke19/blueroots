package com.blueroots.carbonregistry.data.supabase

import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.gotrue.Auth
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.Realtime
import io.github.jan.supabase.storage.Storage

object SupabaseClient {

    // Replace with your Supabase project URL and anon key
    private const val SUPABASE_URL = "https://pvstimmfexsksbrovhsd.supabase.co"
    private const val SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InB2c3RpbW1mZXhza3Nicm92aHNkIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTg0NzY2MjUsImV4cCI6MjA3NDA1MjYyNX0.FKhpFQvJEd3YTv-oIXvz03hJyrxue9ELim6LsGKSxU4"

    val client = createSupabaseClient(
        supabaseUrl = SUPABASE_URL,
        supabaseKey = SUPABASE_ANON_KEY
    ) {
        install(Auth) {
            // Optional: Configure Auth settings
        }
        install(Postgrest)
        install(Realtime)
        install(Storage)
    }

    val auth: Auth get() = client.auth
    val database: Postgrest get() = client.postgrest
}
